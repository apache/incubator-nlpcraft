/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.probe.mgrs.conn

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.crypto._
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.socket._
import org.apache.nlpcraft.common.version.NCVersion
import org.apache.nlpcraft.probe.mgrs.NCProbeMessage
import org.apache.nlpcraft.probe.mgrs.cmd.NCCommandManager
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import java.io.{EOFException, IOException, InterruptedIOException}
import java.net.{InetAddress, NetworkInterface}
import java.{lang, util}
import java.util.concurrent.CountDownLatch
import java.util.{Collections, Properties, TimeZone}
import scala.collection.mutable
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, SetHasAsJava, SetHasAsScala}

/**
  * Probe down/up link connection manager.
  */
object NCConnectionManager extends NCService {
    // Uplink retry timeout.
    private final val RETRY_TIMEOUT = 10 * 1000
    // SO_TIMEOUT.
    private final val SO_TIMEOUT = 5 * 1000
    // Ping timeout.
    private final val PING_TIMEOUT = 5 * 1000

    // Internal probe GUID.
    @volatile private var probeGuid: String = _

    private final val sysProps: Properties = System.getProperties
    private final val localHost: InetAddress = InetAddress.getLocalHost
    @volatile private var hwAddrs: String = _
    
    // Holding downlink queue.
    @volatile private var dnLinkQueue: mutable.Queue[Serializable] = _
    
    // Control thread.
    @volatile private var ctrlThread: Thread = _

    private object Config extends NCConfigurable {
        private final val pre = "nlpcraft.probe"

        def id: String = getString(s"$pre.id")
        def token: String = getString(s"$pre.token")
        def upLink: (String, Integer) = getHostPort(s"$pre.upLink")
        def downLink: (String, Integer) = getHostPort(s"$pre.downLink")
        def upLinkString = s"${upLink._1}:${upLink._2}"
        def downLinkString = s"${downLink._1}:${downLink._2}"
    }

    /**
      * Schedules message for sending to the server.
      *
      * @param msg Message to send to server.
      */
    def send(msg: NCProbeMessage, parent: Span = null): Unit = startScopedSpan("send", parent) { span =>
        addTags(
            span,
            "probeId" -> Config.id,
            "token" -> Config.token,
            "probeGuid" -> probeGuid,
            "msgType" -> msg.getType,
            "msgGuid" -> msg.getGuid
        )
        
        // Set probe identification for each message, if necessary.
        msg.setProbeToken(Config.token)
        msg.setProbeId(Config.id)
        msg.setProbeGuid(probeGuid)
    
        dnLinkQueue.synchronized {
            if (!isStopping) {
                dnLinkQueue += msg
    
                dnLinkQueue.notifyAll()
            }
            else
                logger.trace(s"Message sending ignored b/c of stopping: $msg")
        }
    }
    
    class HandshakeError(msg: String) extends RuntimeException(msg)
    
    /**
      * Opens down link socket.
      */
    @throws[Exception]
    private def openDownLinkSocket(): NCSocket = {
        val (host, port) = Config.downLink
        
        val cryptoKey = NCCipher.makeTokenKey(Config.token)
    
        logger.trace(s"Opening downlink to '$host:$port'")
    
        // Connect down socket.
        val sock = NCSocket(host, port)
    
        sock.write(U.mkSha256Hash(Config.token)) // Hash.
        sock.write(NCProbeMessage( // Handshake.
            // Type.
            "INIT_HANDSHAKE",
        
            // Payload.
            // Probe identification.
            "PROBE_TOKEN" -> Config.token,
            "PROBE_ID" -> Config.id,
            "PROBE_GUID" -> probeGuid
        ), cryptoKey)
    
        val resp = sock.read[NCProbeMessage](cryptoKey) // Get handshake response.
    
        resp.getType match {
            case "P2S_PROBE_OK" => logger.trace("Downlink handshake OK.") // Bingo!
            case "P2S_PROBE_NOT_FOUND" =>  throw new HandshakeError("Probe failed to start due to unknown error.")
            case _ =>  throw new HandshakeError(s"Unexpected REST server message: ${resp.getType}")
        }
    
        sock
    }
    
    /**
      * Opens uplink socket.
      */
    @throws[Exception]
    private def openUplinkSocket(): NCSocket = {
        val netItf = NetworkInterface.getByInetAddress(localHost)
    
        hwAddrs = ""
    
        if (netItf != null) {
            val addrs = netItf.getHardwareAddress
        
            if (addrs != null)
                hwAddrs = addrs.foldLeft("")((s, b) => s + (if (s == "") f"$b%02X" else f"-$b%02X"))
        }
    
        val (host, port) = Config.upLink
        
        val cryptoKey = NCCipher.makeTokenKey(Config.token)

        logger.trace(s"Opening uplink to '$host:$port'")

        // Connect up socket.
        val sock = NCSocket(host, port)
    
        sock.write(U.mkSha256Hash(Config.token)) // Hash, sent clear text.
    
        val hashResp = sock.read[NCProbeMessage]()

        hashResp.getType match { // Get hash check response.
            case "S2P_HASH_CHECK_OK" =>
                val ver = NCVersion.getCurrent
                val tmz = TimeZone.getDefault
    
                val srvNlpEng: String =
                    hashResp.dataOpt[String]("NLP_ENGINE").
                        getOrElse(throw new HandshakeError("NLP engine parameter missed in response."))

                val probeNlpEng = NCNlpCoreManager.getEngine

                if (srvNlpEng != probeNlpEng)
                    logger.warn(s"Invalid NLP engines configuration [server=$srvNlpEng, probe=$probeNlpEng]")

                sock.write(NCProbeMessage( // Handshake.
                    // Type.
                    "INIT_HANDSHAKE",
        
                    // Payload.
                    // Probe identification.
                    "PROBE_TOKEN" -> Config.token,
                    "PROBE_ID" -> Config.id,
                    "PROBE_GUID" -> probeGuid,
        
                    // Handshake data,
                    "PROBE_API_DATE" -> ver.date,
                    "PROBE_API_VERSION" -> ver.version,
                    "PROBE_OS_VER" -> sysProps.getProperty("os.version"),
                    "PROBE_OS_NAME" -> sysProps.getProperty("os.name"),
                    "PROBE_OS_ARCH" -> sysProps.getProperty("os.arch"),
                    "PROBE_START_TSTAMP" -> U.nowUtcMs(),
                    "PROBE_TMZ_ID" -> tmz.getID,
                    "PROBE_TMZ_ABBR" -> tmz.getDisplayName(false, TimeZone.SHORT),
                    "PROBE_TMZ_NAME" -> tmz.getDisplayName(),
                    "PROBE_SYS_USERNAME" -> sysProps.getProperty("user.name"),
                    "PROBE_JAVA_VER" -> sysProps.getProperty("java.version"),
                    "PROBE_JAVA_VENDOR" -> sysProps.getProperty("java.vendor"),
                    "PROBE_HOST_NAME" -> localHost.getHostName,
                    "PROBE_HOST_ADDR" -> localHost.getHostAddress,
                    "PROBE_HW_ADDR" -> hwAddrs,
                    "PROBE_MODELS" ->
                        NCModelManager.getAllModels().map(wrapper => {
                            val mdl = wrapper.model

                            val ctxWordElems = mdl.getElements.asScala.filter(_.getContextWordStrictLevel.isPresent)

                            val (
                                values,
                                samples,
                                levels
                            ): (
                                java.util.Map[String, java.util.Map[String, java.util.Set[String]]],
                                java.util.Set[String],
                                java.util.Map[String, lang.Double]
                            ) =
                                if (ctxWordElems.isEmpty)
                                    (Collections.emptyMap(), Collections.emptySet(), Collections.emptyMap())
                                else {
                                    (
                                        ctxWordElems.map(e =>
                                            e.getId ->
                                                e.getValues.asScala.map(p => p.getName -> {
                                                    val set: util.Set[String] = new util.HashSet(p.getSynonyms)

                                                    set
                                                }).toMap.asJava
                                        ).toMap.asJava,
                                        wrapper.samples.flatMap(_._2.flatMap(p => p)).asJava,
                                        ctxWordElems.map(e => e.getId -> e.getContextWordStrictLevel.get()).toMap.asJava
                                    )
                                }

                            // Model already validated.

                            // util.HashSet created to avoid scala collections serialization error.
                            // Seems to be a Scala bug.
                            (
                                mdl.getId,
                                mdl.getName,
                                mdl.getVersion,
                                new util.HashSet[String](mdl.getEnabledBuiltInTokens),
                                values,
                                samples,
                                levels
                            )
                        })
                ), cryptoKey)
    
                val resp = sock.read[NCProbeMessage](cryptoKey) // Get handshake response.
    
                resp.getType match {
                    case "S2P_PROBE_MULTIPLE_INSTANCES" =>  throw new HandshakeError("Duplicate probes ID detected. Each probe has to have a unique ID.")
                    case "S2P_PROBE_NOT_FOUND" =>  throw new HandshakeError("Probe failed to start due to unknown error.")
                    case "S2P_PROBE_VERSION_MISMATCH" =>  throw new HandshakeError(s"REST server does not support probe version: ${ver.version}")
                    case "S2P_PROBE_UNSUPPORTED_TOKENS_TYPES" =>  throw new HandshakeError(s"REST server does not support some model enabled tokes types.")
                    case "S2P_PROBE_OK" => logger.trace("Uplink handshake OK.") // Bingo!
                    case _ =>  throw new HandshakeError(s"Unknown REST server message: ${resp.getType}")
                }
    
                sock

            case "S2P_HASH_CHECK_UNKNOWN" => throw new HandshakeError(s"Sever does not recognize probe token: ${Config.token}.")
        }
    }
    
    /**
      *
      */
    private def abort(): Unit = {
        // Make sure to exit & stop this thread.
        ctrlThread.interrupt()
        
        // Exit the probe with error code.
        System.exit(1)
    }
    
    /**
      *
      * @return
      */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        require(NCCommandManager.isStarted)
        require(NCModelManager.isStarted)

        ackStarting()

        probeGuid = U.genGuid()
        dnLinkQueue = mutable.Queue.empty[Serializable]

        val ctrlLatch = new CountDownLatch(1)
     
        ctrlThread = U.mkThread("probe-ctrl-thread") { t =>
            var dnSock: NCSocket = null
            var upSock: NCSocket = null
            var dnThread: Thread = null
            var upThread: Thread = null
    
            /**
              *
              */
            def closeAll(): Unit = {
                U.stopThread(dnThread)
                U.stopThread(upThread)
                
                dnThread = null
                upThread = null
                
                if (dnSock != null) dnSock.close()
                if (upSock != null) upSock.close()
                
                dnSock = null
                upSock = null
            }
    
            /**
              *
              */
            def timeout(): Unit = if (!t.isInterrupted) U.ignoreInterrupt {
                Thread.sleep(RETRY_TIMEOUT)
            }
    
            val cryptoKey = NCCipher.makeTokenKey(Config.token)
            
            while (!t.isInterrupted)
                try {
                    logger.info(s"Connecting to REST server [" +
                        s"uplink=${Config.upLinkString}, " +
                        s"downlink=${Config.downLinkString}" +
                        s"]")
                    
                    upSock = openUplinkSocket()
                    dnSock = openDownLinkSocket()
                    
                    upSock.socket.setSoTimeout(SO_TIMEOUT)
            
                    val exitLatch = new CountDownLatch(1)
            
                    /**
                      *
                      * @param caller Caller thread to interrupt.
                      * @param msg    Error message.
                      * @param cause  Optional cause of the error.
                      */
                    def exit(caller: Thread, msg: String, cause: Exception = null): Unit = {
                        if (cause != null)
                            U.prettyError(logger, msg, cause)
                        else
                            logger.error(msg)

                        caller.interrupt() // Interrupt current calling thread.
                
                        exitLatch.countDown()
                    }
            
                    upThread = U.mkThread("probe-uplink") { t =>
                        // Main reading loop.
                        while (!t.isInterrupted)
                            try
                                NCCommandManager.processServerMessage(upSock.read[NCProbeMessage](cryptoKey))
                            catch {
                                case _: InterruptedIOException | _: InterruptedException => ()
                                case _: EOFException => exit(t, s"Uplink REST server connection closed.")
                                case e: Exception => exit(t, s"Uplink connection failed.", e)
                            }
                    }
                    
                    dnThread = U.mkThread("probe-downlink") { t =>
                        while (!t.isInterrupted)
                            try {
                                dnLinkQueue.synchronized {
                                    if (dnLinkQueue.isEmpty) {
                                        dnLinkQueue.wait(PING_TIMEOUT)
                    
                                        if (!dnThread.isInterrupted && dnLinkQueue.isEmpty) {
                                            val pingMsg = NCProbeMessage("P2S_PING")
                    
                                            pingMsg.setProbeToken(Config.token)
                                            pingMsg.setProbeId(Config.id)
                                            pingMsg.setProbeGuid(probeGuid)
                    
                                            dnSock.write(pingMsg, cryptoKey)
                                        }
                                    }
                                    else {
                                        val msg = dnLinkQueue.head
                                
                                        // Write head first (without actually removing from queue).
                                        dnSock.write(msg, cryptoKey)
                                
                                        // If sent ok - remove from queue.
                                        dnLinkQueue.dequeue()
                                    }
                                }
                            }
                            catch {
                                case _: InterruptedIOException | _: InterruptedException => ()
                                case _: EOFException => exit(t, s"Downlink REST server connection closed.")
                                case e: Exception => exit(t, s"Downlink connection failed.", e)
                            }
                    }
            
                    // Bingo - start downlink and uplink!
                    upThread.start()
                    dnThread.start()
                    
                    // Indicate that server connection is established.
                    ctrlLatch.countDown()
                    
                    logger.info("REST server connected.")
                    
                    // Wait until probe connection is closed.
                    while (!t.isInterrupted && exitLatch.getCount > 0) U.ignoreInterrupt {
                        exitLatch.await()
                    }
                    
                    closeAll()
                    
                    if (!isStopping) {
                        logger.warn(s"REST server connection closed (retry in ${RETRY_TIMEOUT / 1000}s).")
                    
                        timeout()
                    }
                    else
                        logger.info(s"REST server connection closed.")
                }
                catch {
                    case e: HandshakeError =>
                        // Clean up.
                        closeAll()
                    
                        // Ack the handshake error message.
                        U.prettyError(logger, s"Failed REST server connection handshake (aborting).", e)
                    
                        abort()
            
                    case e: IOException =>
                        // Clean up.
                        closeAll()

                        // Ack the error message.
                        U.prettyError(logger, s"Failed to connect to REST server (retry in ${RETRY_TIMEOUT / 1000}s).", e)

                        timeout()
            
                    case e: Exception =>
                        // Clean up.
                        closeAll()
                
                        // Ack the error message.
                        U.prettyError(logger, "Unexpected error connecting to REST server.", e)
                    
                        abort()
                }
            
            closeAll()
        }
     
        ctrlThread.start()
        
        // Only return when probe successfully connected to the server.
        ctrlLatch.await()
     
        ackStarted()
    }
    
    /**
      *
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        U.stopThread(ctrlThread)
        
        ackStopped()
    }
}
