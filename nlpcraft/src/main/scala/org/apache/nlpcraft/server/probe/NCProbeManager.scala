/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.server.probe

import java.io._
import java.net.{InetSocketAddress, ServerSocket, Socket, SocketTimeoutException}
import java.security.Key
import java.util
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ExecutorService, Executors}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.crypto.NCCipher
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.socket.NCSocket
import org.apache.nlpcraft.common.version.NCVersion
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.probe.mgrs.NCProbeMessage
import org.apache.nlpcraft.server.company.NCCompanyManager
import org.apache.nlpcraft.server.mdo.{NCCompanyMdo, NCProbeMdo, NCProbeModelMdo, NCUserMdo}
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnrichmentManager
import org.apache.nlpcraft.server.proclog.NCProcessLogManager
import org.apache.nlpcraft.server.query.NCQueryManager
import org.apache.nlpcraft.server.sql.NCSql

import scala.collection.JavaConverters._
import scala.collection.{Map, mutable}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Probe manager.
  */
object NCProbeManager extends NCService {
    // Type safe and eager configuration container.
    private[probe] object Config extends NCConfigurable {
        final private val pre = "nlpcraft.server.probe"

        def getDnHostPort = getHostPort(s"$pre.links.downLink")
        def getUpHostPort = getHostPort(s"$pre.links.upLink")

        def poolSize: Int = getInt(s"$pre.poolSize")
        def reconnectTimeoutMs: Long = getLong(s"$pre.reconnectTimeoutMs")
        def pingTimeoutMs: Long = getLong(s"$pre.pingTimeoutMs")
        def soTimeoutMs: Int = getInt(s"$pre.soTimeoutMs")
    
        /**
          *
          */
        def check(): Unit = {
            val (_, dnPort) =  getDnHostPort
            val (_, upPort) =  getUpHostPort

            if (!(dnPort >= 0 && dnPort <= 65535))
                abortWith(s"Configuration property '$pre.links.upLink' must be >= 0 and <= 65535: $dnPort")
            if (!(upPort >= 0 && upPort <= 65535))
                abortWith(s"Configuration property '$pre.links.downLink' must be >= 0 and <= 65535: $upPort")
            if (reconnectTimeoutMs <= 0)
                abortWith(s"Configuration property '$pre.reconnectTimeoutMs' must be > 0: $reconnectTimeoutMs")
            if (poolSize <= 0)
                abortWith(s"Configuration property '$pre.poolSize' must be > 0: $poolSize")
            if (soTimeoutMs <= 0)
                abortWith(s"Configuration property '$pre.soTimeoutMs' must be > 0: $soTimeoutMs")
            if (pingTimeoutMs <= 0)
                abortWith(s"Configuration property '$pre.pingTimeoutMs' timeout must be > 0: $pingTimeoutMs")
        }
    }
    
    Config.check()
    
    // Compound probe key.
    private case class ProbeKey(
        probeToken: String, // Probe token.
        probeId: String, // Unique probe ID.
        probeGuid: String // Runtime unique ID (to disambiguate different instances of the same probe).
    ) {
        override def toString: String = s"Probe key [" +
            s"probeId=$probeId, " +
            s"probeGuid=$probeGuid, " +
            s"probeToken=$probeToken" +
            s"]"
    }
    
    // Immutable probe holder.
    private case class ProbeHolder(
        probeKey: ProbeKey,
        probe: NCProbeMdo,
        var dnSocket: NCSocket,
        var upSocket: NCSocket,
        var dnThread: Thread, // Separate thread listening for messages from the probe.
        cryptoKey: Key, // Encryption key.
        timestamp: Long = U.nowUtcMs()
    ) {
        /**
          *
          */
        def close(): Unit = {
            if (dnThread != null)
                U.stopThread(dnThread)
            
            if (upSocket != null)
                upSocket.close()
            
            if (dnSocket != null)
                dnSocket.close()
        }
    }

    @volatile private var dnSrv: Thread = _
    @volatile private var upSrv: Thread = _
    @volatile private var pingSrv: Thread = _
    
    // All known probes keyed by probe key.
    @volatile private var probes: mutable.Map[ProbeKey, ProbeHolder] = _
    @volatile private var mdls: mutable.Map[String, NCProbeModelMdo] = _
    // All probes pending complete handshake keyed by probe key.
    @volatile private var pending: mutable.Map[ProbeKey, ProbeHolder] = _

    @volatile private var pool: ExecutorService = _
    @volatile private var isStopping: AtomicBoolean = _
    
    /**
      *
      * @return
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        probes = mutable.HashMap.empty[ProbeKey, ProbeHolder]
        mdls = mutable.HashMap.empty[String, NCProbeModelMdo]
        pending = mutable.HashMap.empty[ProbeKey, ProbeHolder]

        val (dnHost, dnPort) = Config.getDnHostPort
        val (upHost, upPort) =  Config.getUpHostPort

        addTags(span,
            "uplink" → s"$upHost:$upPort",
            "downlink" → s"$dnHost:$dnPort"
        )
    
        isStopping = new AtomicBoolean(false)
        
        pool = Executors.newFixedThreadPool(Config.poolSize)
        
        dnSrv = startServer("Downlink", dnHost, dnPort, downLinkHandler)
        upSrv = startServer("Uplink", upHost, upPort, upLinkHandler)
        
        dnSrv.start()
        upSrv.start()
        
        pingSrv = U.mkThread("probe-pinger") { t ⇒
            while (!t.isInterrupted) {
                U.sleep(Config.pingTimeoutMs)
        
                val pingMsg = NCProbeMessage("S2P_PING")
        
                probes.synchronized {
                    probes.values
                }
                .map(_.probeKey).foreach(sendToProbe(_, pingMsg, span))
            }
        }
        
        pingSrv.start()
        
        super.start()
    }
    
    /**
      *
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        isStopping = new AtomicBoolean(true)
     
        U.shutdownPools(pool)
     
        U.stopThread(pingSrv)
        U.stopThread(dnSrv)
        U.stopThread(upSrv)
     
        super.stop()
    }

    /**
      *
      * @param tokHash
      */
    @throws[NCE]
    private def mkKey(tokHash: String): Key = {
        val company = NCSql.sql {
            NCCompanyManager.getCompanyByHashToken(tokHash).
                getOrElse(throw new NCE(s"Unknown probe token hash: $tokHash"))
        }

        NCCipher.makeTokenKey(company.authToken)
    }

    /**
      *
      * @param probeKey Probe key.
      */
    private def closeAndRemoveHolder(probeKey: ProbeKey): Unit =
        // Check pending queue first.
        pending.synchronized { pending.remove(probeKey) } match {
            case None ⇒
                // Check active probes second.
                probes.synchronized { probes.remove(probeKey) } match {
                    case None ⇒
                    case Some(holder) ⇒
                        holder.close()
            
                        logger.info(s"Probe removed: $probeKey")

                        // Clears unused models.
                        mdls --= mdls.keys.filter(id ⇒ !probes.exists { case (_, p) ⇒ p.probe.models.exists(_.id == id) })
                }

            case Some(hld) ⇒
                hld.close()
                
                logger.info(s"Pending probe removed: $probeKey")
        }

    /**
      *
      * @param probeKey Probe key.
      * @param probeMsg Probe message to send.
      */
    private def sendToProbe(probeKey: ProbeKey, probeMsg: NCProbeMessage, parent: Span): Unit =
        startScopedSpan("sendToProbe", parent, "probeId" → probeKey.probeId, "msgType" → probeMsg.getType) { _ ⇒
            val (sock, cryptoKey) = probes.synchronized {
                probes.get(probeKey) match {
                    case None ⇒ (null, null)
                    case Some(h) ⇒ (h.upSocket, h.cryptoKey)
                }
            }
         
            if (sock != null)
                Future {
                    try {
                        sock.write(probeMsg, cryptoKey)
                    }
                    catch {
                        case _: EOFException ⇒
                            logger.trace(s"Probe closed connection: $probeKey")
         
                            closeAndRemoveHolder(probeKey)

                        case e: Throwable ⇒
                            logger.error(s"Uplink socket error [" +
                                s"sock=$sock, " +
                                s"probeKey=$probeKey, " +
                                s"probeMsg=$probeMsg" +
                                s"error=${e.getLocalizedMessage}" +
                                s"]")
         
                            closeAndRemoveHolder(probeKey)
                    }
                }
            else
                logger.warn(s"Sending message to unknown probe (ignoring) [" +
                    s"probeKey=$probeKey, " +
                    s"probeMsg=$probeMsg" +
                s"]")
        }
    
    /**
      * Starts a server (thread) with given name, local bind port and processing function.
      *
      * @param name Server name.
      * @param host Local host/IP to bind.
      * @param port Local port to bind.
      * @param fn Function.
      */
    private def startServer(name: String, host: String, port: Int, fn: NCSocket ⇒ Unit): Thread = {
        val thName = name.toLowerCase
        
        new Thread(s"probe-mgr-$thName") {
            private var srv: ServerSocket = _
    
            @volatile private var stopped = false
    
            override def isInterrupted: Boolean = super.isInterrupted || stopped
    
            override def interrupt(): Unit = {
                super.interrupt()
                
                U.close(srv)
                
                stopped = true
            }
    
            override def run(): Unit = {
                logger.trace(s"Thread started: $thName")
                
                try {
                    body()
                    
                    logger.trace(s"Thread exited: $thName")
                }
                catch {
                    case _: InterruptedException ⇒ logger.trace(s"Thread interrupted: $thName")
                    case e: Throwable ⇒ logger.error(s"Unexpected error during thread execution: $thName", e)
                }
                finally
                    stopped = true
            }
    
            private def body(): Unit =
                while (!isInterrupted)
                    try {
                        srv = new ServerSocket()

                        srv.bind(new InetSocketAddress(host, port))
                        
                        logger.info(s"$name server is listening on '$host:$port'")
                        
                        srv.setSoTimeout(Config.soTimeoutMs)
                        
                        while (!isInterrupted) {
                            var sock: Socket = null
                            
                            try {
                                sock = srv.accept()
                                
                                logger.trace(s"'$name' server accepted new connection.")
                            }
                            catch {
                                case _: InterruptedIOException ⇒ // No-op.
                                
                                // Note that server socket must be closed and created again.
                                // So, error should be thrown.
                                case e: Exception ⇒
                                    U.close(sock)
                                    
                                    throw e
                            }
                            if (sock != null) {
                                val fut = Future {
                                    fn(NCSocket(sock, sock.getRemoteSocketAddress.toString))
                                }

                                fut.onComplete {
                                    case Success(_) ⇒ // No-op.

                                    case Failure(e: NCE) ⇒ logger.warn(e.getMessage, e)
                                    case Failure(_: EOFException) ⇒ () // Just ignoring.
                                    case Failure(e: Throwable) ⇒ logger.warn(s"Ignoring socket error: ${e.getLocalizedMessage}")
                                }
                            }
                        }
                    }
                    catch {
                        case e: Exception ⇒
                            if (!isStopping.get) {
                                // Release socket asap.
                                U.close(srv)
                                
                                val ms = Config.reconnectTimeoutMs
    
                                // Server socket error must be logged.
                                logger.warn(s"$name server error, re-starting in ${ms / 1000} sec.", e)
                                
                                U.sleep(ms)
                            }
                    }
                    finally {
                        U.close(srv)
                    }
        }
    }
    
    /**
      * Processes socket for downlink messages.
      *
      * @param sock Downlink socket.
      */
    @throws[NCE]
    @throws[IOException]
    private def downLinkHandler(sock: NCSocket): Unit = {
        // Read header token hash message.
        val tokHash = sock.read[String]()
        val cryptoKey = mkKey(tokHash)

        // Read handshake probe message.
        val hsMsg = sock.read[NCProbeMessage](cryptoKey)
    
        require(hsMsg.getType == "INIT_HANDSHAKE")
    
        // Probe key components.
        val probeTkn = hsMsg.getProbeToken
        val probeId = hsMsg.getProbeId
        val probeGuid = hsMsg.getProbeGuid
    
        logger.trace(s"Downlink handshake received [" +
            s"probeToken=$probeTkn, " +
            s"probeId=$probeId, " +
            s"proveGuid=$probeGuid" +
            s"]")
    
        val probeKey = ProbeKey(probeTkn, probeId, probeGuid)
    
        val threadName = "probe-downlink-" + probeId.toLowerCase + "-" + probeGuid.toLowerCase
    
        val p2sThread = U.mkThread(threadName) { t ⇒
            try {
                sock.socket.setSoTimeout(Config.soTimeoutMs)
            
                while (!t.isInterrupted)
                    try {
                        processMessageFromProbe(sock.read[NCProbeMessage](cryptoKey))
                    }
                    catch {
                        // Normal thread interruption.
                        case _: SocketTimeoutException | _: InterruptedException | _: InterruptedIOException ⇒ ()
                    
                        case _: EOFException ⇒
                            logger.info(s"Probe closed downlink connection: $probeKey")
                        
                            t.interrupt()
                    
                        case e: Throwable ⇒
                            logger.info(s"Error reading probe downlink socket (${e.getMessage}): $probeKey")
                        
                            t.interrupt()
                    }
            }
            finally {
                closeAndRemoveHolder(probeKey)
            }
        }
    
        def respond(typ: String): Unit = {
            val msg = NCProbeMessage(typ)
        
            logger.trace(s"Sending to probe ($typ): $msg")
        
            sock.write(msg, cryptoKey)
        }
    
        // Update probe holder.
        val holder = pending.synchronized {
            pending.remove(probeKey) match {
                case None ⇒ () // Probe has been removed already?
                    respond("P2S_PROBE_NOT_FOUND")
                
                    null
            
                case Some(h) ⇒
                    h.dnThread = p2sThread
                    h.dnSocket = sock
                
                    h
            }
        }
    
        if (holder != null)
            probes.synchronized {
                probes += probeKey → holder

                holder.probe.models.foreach(m ⇒ mdls += m.id → m)
    
                val tbl = NCAsciiTable()
    
                tbl #= (
                    "Probe ID",
                    "OS",
                    "Timezone",
                    "Host",
                    "Models"
                )
            
                addProbeToTable(tbl, holder).info(logger, Some("New probe registered:"))
            
                // Bingo!
                respond("P2S_PROBE_OK")
            
                p2sThread.start()
            }
    }

    /**
      *
      * @param probeKey Probe key.
      */
    private def isMultipleProbeRegistrations(probeKey: ProbeKey): Boolean =
        probes.synchronized {
            probes.values.count(p ⇒
                p.probeKey.probeToken == probeKey.probeToken &&
                    p.probeKey.probeId == probeKey.probeId
            ) > 1
        }
    
    /**
      * Processes socket for uplink messages.
      *
      * @param sock Uplink socket.
      */
    @throws[NCE]
    @throws[IOException]
    private def upLinkHandler(sock: NCSocket): Unit = {
        // Read header probe token hash message.
        val tokHash = sock.read[String]()
        var cryptoKey: Key = null

        def respond(typ: String, pairs: (String, Serializable)*): Unit = {
            val msg = NCProbeMessage(typ, pairs:_*)

            logger.trace(s"Sending to probe ($typ): $msg")

            sock.write(msg, cryptoKey)
        }

        try {
            val k = mkKey(tokHash)

            respond(
                "S2P_HASH_CHECK_OK",
                "NLP_ENGINE" → NCNlpCoreManager.getEngine
            )

            cryptoKey = k
        }
        catch {
            case _: NCE ⇒
                respond("S2P_HASH_CHECK_UNKNOWN")

                throw new NCE(s"Rejecting probe connection due to unknown probe token hash: $tokHash")
        }

        // Read handshake probe message.
        val hsMsg = sock.read[NCProbeMessage](cryptoKey)
    
        require(hsMsg.getType == "INIT_HANDSHAKE")
    
        // Probe key components.
        val probeTkn = hsMsg.getProbeToken
        val probeId = hsMsg.getProbeId
        val probeGuid = hsMsg.getProbeGuid
    
        val probeKey = ProbeKey(probeTkn, probeId, probeGuid)
    
        logger.trace(s"Uplink handshake received [" +
            s"probeToken=$probeTkn, " +
            s"probeId=$probeId, " +
            s"proveGuid=$probeGuid" +
            s"]")
    
        if (isMultipleProbeRegistrations(probeKey))
            respond("S2P_PROBE_MULTIPLE_INSTANCES")
        else {
            val probeApiVer = hsMsg.data[String]("PROBE_API_VERSION")
            val srvApiVer = NCVersion.getCurrent
            
            if (probeApiVer != srvApiVer.version)
                respond("S2P_PROBE_VERSION_MISMATCH")
            else {
                val models =
                    hsMsg.data[List[(String, String, String, java.util.Set[String])]]("PROBE_MODELS").
                        map { case (mdlId, mdlName, mdlVer, enabledBuiltInToks) ⇒
                            NCProbeModelMdo(
                                id = mdlId,
                                name = mdlName,
                                version = mdlVer,
                                enabledBuiltInTokens = enabledBuiltInToks.asScala.toSet
                            )
                        }.toSet

                val probeTokTypes = models.flatMap(_.enabledBuiltInTokens).map(_.takeWhile(_ != ':'))
                val tokProviders = NCServerEnrichmentManager.getSupportedProviders

                if (probeTokTypes.exists(typ ⇒ !tokProviders.contains(typ)))
                    respond("S2P_PROBE_UNSUPPORTED_TOKENS_TYPES")
                else {
                    val probeApiDate = hsMsg.data[java.time.LocalDate]("PROBE_API_DATE")

                    val holder = ProbeHolder(
                        probeKey,
                        NCProbeMdo(
                            probeToken = hsMsg.data[String]("PROBE_TOKEN"),
                            probeId = hsMsg.data[String]("PROBE_ID"),
                            probeGuid = probeGuid,
                            probeApiVersion = probeApiVer,
                            probeApiDate = java.sql.Date.valueOf(probeApiDate),
                            osVersion = hsMsg.data[String]("PROBE_OS_VER"),
                            osName = hsMsg.data[String]("PROBE_OS_NAME"),
                            osArch = hsMsg.data[String]("PROBE_OS_ARCH"),
                            startTstamp = new java.sql.Timestamp(hsMsg.data[Long]("PROBE_START_TSTAMP")),
                            tmzId = hsMsg.data[String]("PROBE_TMZ_ID"),
                            tmzAbbr = hsMsg.data[String]("PROBE_TMZ_ABBR"),
                            tmzName = hsMsg.data[String]("PROBE_TMZ_NAME"),
                            userName = hsMsg.data[String]("PROBE_SYS_USERNAME"),
                            javaVersion = hsMsg.data[String]("PROBE_JAVA_VER"),
                            javaVendor = hsMsg.data[String]("PROBE_JAVA_VENDOR"),
                            hostName = hsMsg.data[String]("PROBE_HOST_NAME"),
                            hostAddr = hsMsg.data[String]("PROBE_HOST_ADDR"),
                            macAddr = hsMsg.dataOpt[String]("PROBE_HW_ADDR").getOrElse(""),
                            models = models
                        ),
                        null, // No downlink socket yet.
                        sock,
                        null, // No downlink thread yet.
                        cryptoKey
                    )

                    pending.synchronized {
                        pending += probeKey → holder
                    }

                    // Bingo!
                    respond("S2P_PROBE_OK")
                }
            }
        }
    }
    
    /**
      * Processes the messages received from the probe.
      *
      * @param probeMsg Probe's message to process.
      */
    private def processMessageFromProbe(probeMsg: NCProbeMessage): Unit = {
        val probeKey = ProbeKey(
            probeMsg.getProbeToken,
            probeMsg.getProbeId,
            probeMsg.getProbeGuid
        )
        
        val knownProbe = probes.synchronized {
            probes.contains(probeKey)
        }
        
        if (!knownProbe)
            logger.error(s"Received message from unknown probe (ignoring): $probeKey]")
        else {
            val typ = probeMsg.getType
            
            typ match {
                case "P2S_PING" ⇒ ()
                
                case "P2S_ASK_RESULT" ⇒
                    val srvReqId = probeMsg.data[String]("srvReqId")
                    
                    try {
                        val errOpt = probeMsg.dataOpt[String]("error")
                        val errCodeOpt = probeMsg.dataOpt[Int]("errorCode")
                        val resTypeOpt = probeMsg.dataOpt[String]("resType")
                        val resBodyOpt = probeMsg.dataOpt[String]("resBody")
                        val logJson = probeMsg.dataOpt[String]("log")
                        val intentId = probeMsg.dataOpt[String]("intentId")

                        if (errOpt.isDefined) { // Error.
                            val err = errOpt.get
                            val errCode = errCodeOpt.get
                     
                            NCQueryManager.setError(
                                srvReqId,
                                err,
                                errCode
                            )
                     
                            logger.trace(s"Error result processed [srvReqId=$srvReqId, error=$err, code=$errCode]")
                        }
                        else { // OK result.
                            require(resTypeOpt.isDefined && resBodyOpt.isDefined, "Result defined")
                     
                            val resType = resTypeOpt.get
                            val resBody = resBodyOpt.get
                     
                            NCQueryManager.setResult(
                                srvReqId,
                                resType,
                                resBody,
                                logJson,
                                intentId
                            )
                     
                            logger.trace(s"OK result processed [srvReqId=$srvReqId]")
                        }
                    }
                    catch {
                        case e: Throwable ⇒
                            logger.error(s"Failed to process probe message: $typ", e)
        
                            NCQueryManager.setError(
                                srvReqId,
                                "Processing failed due to a system error.",
                                NCErrorCodes.UNEXPECTED_ERROR
                            )
                    }
                case _ ⇒
                    logger.error(s"Received unrecognized probe message (ignoring): $probeMsg")
            }
        }
    }
    
    /**
      *
      * @param modelId
      * @return
      */
    private def getProbeForModelId(modelId: String): Option[ProbeHolder] = {
        val candidates = probes.synchronized {
            probes.values.filter(_.probe.models.exists(_.id == modelId)).toSeq
        }
        
        val sz = candidates.size
        
        if (sz == 1)
            Some(candidates.head)
        else if (sz == 0)
            None
        else
            // Load balance using random indexing.
            Some(candidates(scala.util.Random.nextInt(sz)))
    }
    
    /**
      *
      * @param tbl ASCII table to add to.
      * @param hol Probe holder to add.
      */
    private def addProbeToTable(tbl: NCAsciiTable, hol: ProbeHolder): NCAsciiTable = {
        val probe = hol.probe
        
        tbl += (
            probe.probeId,
            s"${probe.osName} ver. ${probe.osVersion}",
            s"${probe.tmzAbbr}, ${probe.tmzId}",
            s"${probe.hostName} (${probe.hostAddr})",
            probe.models.map(m ⇒ s"ID: ${m.id}, ver.${m.version}").toSeq
        )
        
        tbl
    }

    /**
      *
      * @param srvReqId Server request ID.
      * @param usr
      * @param company
      * @param txt
      * @param nlpSen
      * @param usrAgent
      * @param rmtAddr
      * @param data
      * @param logEnable
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def askProbe(
        srvReqId: String,
        usr: NCUserMdo,
        company: NCCompanyMdo,
        mdlId: String,
        txt: String,
        nlpSen: NCNlpSentence,
        usrAgent: Option[String],
        rmtAddr: Option[String],
        data: Option[String],
        usrPropsOpt: Option[Map[String, String]],
        logEnable: Boolean,
        parent: Span = null): Unit = {
        startScopedSpan("askProbe", parent, "srvReqId" → srvReqId, "usrId" → usr.id, "modelId" → mdlId, "txt" → txt) { span ⇒
            val userProps =
                usrPropsOpt match {
                    case Some(props) ⇒
                        val m = new util.HashMap[String, String]()
    
                        props.foreach { case (k, v) ⇒ m.put(k, v) }
    
                        m
                    case None ⇒ null
                }

            val senMeta = new util.HashMap[String, java.io.Serializable]()
    
            Map(
                "NORMTEXT" → nlpSen.text,
                "USER_AGENT" → usrAgent.orNull,
                "USER_ID" → usr.id,
                "REMOTE_ADDR" → rmtAddr.orNull,
                "RECEIVE_TSTAMP" → U.nowUtcMs(),
                "FIRST_NAME" → usr.firstName.orNull,
                "LAST_NAME" → usr.lastName.orNull,
                "EMAIL" → usr.email.orNull,
                "SIGNUP_TSTAMP" → usr.createdOn.getTime,
                "IS_ADMIN" → usr.isAdmin,
                "AVATAR_URL" → usr.avatarUrl.orNull,
                "DATA" → data.orNull,
                "USER_PROPS" → userProps,
                "COMPANY_ID" → company.id,
                "COMPANY_NAME" → company.name,
                "COMPANY_WEBSITE" → company.website.orNull,
                "COMPANY_COUNTRY" → company.country.orNull,
                "COMPANY_REGION" → company.region.orNull,
                "COMPANY_CITY" → company.city.orNull,
                "COMPANY_ADDRESS" → company.address.orNull,
                "COMPANY_POSTAL" → company.postalCode.orNull
            ).
                filter(_._2 != null).
                foreach(p ⇒ senMeta.put(p._1, p._2.asInstanceOf[java.io.Serializable]))
            
            getProbeForModelId(mdlId) match {
                case Some(holder) ⇒
                    sendToProbe(
                        holder.probeKey,
                        NCProbeMessage(
                            "S2P_ASK",
                            "srvReqId" → srvReqId,
                            "txt" → txt,
                            // Initial sentence can be extended with synonyms etc.
                            "nlpSens" → Collections.singletonList(nlpSen.asInstanceOf[java.io.Serializable]).asInstanceOf[java.io.Serializable],
                            "senMeta" → senMeta,
                            "userId" → usr.id,
                            "mdlId" → mdlId,
                            "logEnable" → logEnable
                        ),
                        span
                    )
                    
                    logger.info(s"Sentence sent to probe [" +
                        s"txt='$txt', " +
                        s"mdlId=$mdlId, " +
                        s"probeId=${holder.probeKey.probeId}, " +
                        s"srvReqId=$srvReqId" +
                    s"]")
                    
                    addTags(
                        span,
                        "probeId" → holder.probeKey.probeId
                    )
    
                    NCProcessLogManager.updateProbe(
                        srvReqId,
                        holder.probe
                    )
    
                case None ⇒ throw new NCE(s"Unknown model ID: $mdlId")
            }
        }
    }
    
    /**
      * Gets all active probes.
      *
      * @param compId
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getAllProbes(compId: Long, parent: Span = null): Seq[NCProbeMdo] =
        startScopedSpan("getAllProbes", parent, "compId" → compId) { _ ⇒
            val comp = NCCompanyManager.getCompany(compId).getOrElse(throw new NCE(s"Company mot found: $compId"))
         
            probes.synchronized {
                probes.filter(_._1.probeToken == comp.authToken).values
            }.map(_.probe).toSeq
        }
    
    /**
      *
      * @param usrId User ID.
      * @param mdlId Model ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def clearConversation(usrId: Long, mdlId: String, parent: Span = null): Unit =
        startScopedSpan("clearConversation", parent, "usrId" → usrId, "modelId" → mdlId) { span ⇒
            val msg = NCProbeMessage("S2P_CLEAR_CONV",
                "usrId" → usrId,
                "mdlId" → mdlId
            )
    
            // Send to all probes.
            probes.synchronized {
                probes.values
            }
            .map(_.probeKey).foreach(sendToProbe(_, msg, span))
        }
    
    /**
     *
     * @param usrId User ID.
     * @param mdlId Model ID.
     * @param parent Optional parent span.
     */
    @throws[NCE]
    def clearDialog(usrId: Long, mdlId: String, parent: Span = null): Unit =
        startScopedSpan("clearDialog", parent, "usrId" → usrId, "modelId" → mdlId) { span ⇒
            val msg = NCProbeMessage("S2P_CLEAR_DLG",
                "usrId" → usrId,
                "mdlId" → mdlId
            )
            
            // Send to all probes.
            probes.synchronized {
                probes.values
            }
            .map(_.probeKey).foreach(sendToProbe(_, msg, span))
        }

    /**
      *
      * @param mdlId Model ID.
      * @param parent Optional parent span.
      * @return
      */
    def getModel(mdlId: String, parent: Span = null): NCProbeModelMdo =
        startScopedSpan("getModel", parent, "modelId" → mdlId) { _ ⇒
            probes.synchronized {
                mdls.getOrElse(mdlId, throw new NCE(s"Unknown model ID: $mdlId"))
            }
        }
}
