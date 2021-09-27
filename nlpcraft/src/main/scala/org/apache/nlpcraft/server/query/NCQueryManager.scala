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

package org.apache.nlpcraft.server.query

import io.opencensus.trace.Span
import org.apache.ignite.IgniteCache
import org.apache.ignite.events.{CacheEvent, EventType}
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.pool.NCThreadPoolManager
import org.apache.nlpcraft.common.util.NCUtils.{jsonToJavaMap, uncompress}
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.server.apicodes.NCApiStatusCode._
import org.apache.nlpcraft.server.company.NCCompanyManager
import org.apache.nlpcraft.server.ignite.NCIgniteHelpers._
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.mdo.NCQueryStateMdo
import org.apache.nlpcraft.server.nlp.enrichers.NCServerEnrichmentManager
import org.apache.nlpcraft.server.opencensus._
import org.apache.nlpcraft.server.probe.NCProbeManager
import org.apache.nlpcraft.server.proclog.NCProcessLogManager
import org.apache.nlpcraft.server.tx.NCTxManager
import org.apache.nlpcraft.server.user.NCUserManager

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.Exception._
import scala.util.{Failure, Success}

/**
  * Query state machine.
  */
object NCQueryManager extends NCService with NCIgniteInstance with NCOpenCensusServerStats {
    private final val MAX_WORDS = 100

    @volatile private var cache: IgniteCache[String/*Server request ID*/, NCQueryStateMdo] = _
    
    // Promises cannot be used in cache.
    @volatile private var asyncAsks: ConcurrentHashMap[String, Promise[NCQueryStateMdo]] = _
    
    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        asyncAsks = new ConcurrentHashMap[String/*Server request ID*/, Promise[NCQueryStateMdo]]()

        catching(wrapIE) {
            cache = ignite.cache[String/*Server request ID*/, NCQueryStateMdo]("qry-state-cache")
    
            cache.addListener(
                (evt: CacheEvent) =>
                    try {
                        val srvReqId: String = evt.key()
                        
                        cache(srvReqId) match {
                            case Some(state) =>
                                if (state.status == QRY_READY.toString) {
                                    val promise = asyncAsks.remove(srvReqId)

                                    if (promise != null)
                                        promise.success(state)
                                }
                                
                            case None => // No-op.
                        }
                    }
                    catch {
                        case e: Throwable => U.prettyError(logger,"Error processing cache events:", e)
                    }
                ,
                EventType.EVT_CACHE_OBJECT_PUT
            )
        }
        
        require(cache != null)
        
        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }
    
    /**
      * Handler for `/ask/sync` REST call.
      *
      * @param usrId User ID.
      * @param txt
      * @param mdlId Model ID.
      * @param usrAgent
      * @param rmtAddr
      * @param data
      * @param enableLog
      * @return
      */
    @throws[NCE]
    def futureAsk(
        usrId: Long,
        txt: String,
        mdlId: String,
        usrAgent: Option[String],
        rmtAddr: Option[String],
        data: Option[String],
        enableLog: Boolean,
        parent: Span = null
    ): Future[NCQueryStateMdo] = {
        val srvReqId = U.genGuid()

        startScopedSpan("syncAsk", parent,
            "srvReqId" -> srvReqId,
            "usrId" -> usrId,
            "txt" -> txt,
            "mdlId" -> mdlId,
            "enableLog" -> enableLog,
            "usrAgent" -> usrAgent.orNull,
            "rmtAddr" -> rmtAddr.orNull
        ) { span =>
            val promise = Promise[NCQueryStateMdo]()

            asyncAsks.put(srvReqId, promise)

            spawnAskFuture(srvReqId, usrId, txt, mdlId, usrAgent, rmtAddr, data, enableLog, span)
            
            promise.future
        }
    }
    
    /**
      * Asynchronous handler for `/ask` REST call.
      *
      * @param usrId User ID.
      * @param txt
      * @param mdlId Model ID.
      * @param usrAgent
      * @param rmtAddr
      * @param data
      * @param enableLog
      * @return Server request ID for newly submitted request.
      */
    @throws[NCE]
    def asyncAsk(
        usrId: Long,
        txt: String,
        mdlId: String,
        usrAgent: Option[String],
        rmtAddr: Option[String],
        data: Option[String],
        enableLog: Boolean,
        parent: Span = null
    ): String = {
        val srvReqId = U.genGuid()

        startScopedSpan("asyncAsk", parent,
            "srvReqId" -> srvReqId,
            "usrId" -> usrId,
            "txt" -> txt,
            "mdlId" -> mdlId,
            "enableLog" -> enableLog,
            "usrAgent" -> usrAgent.orNull,
            "rmtAddr" -> rmtAddr.orNull) { span =>
            spawnAskFuture(srvReqId, usrId, txt, mdlId, usrAgent, rmtAddr, data, enableLog, span)
            
            srvReqId
        }
    }

    /**
      * @return
      */
    private implicit def getContext: ExecutionContext = NCThreadPoolManager.getContext("probe.requests")
    
    /**
      * @param srvReqId Server request ID.
      * @param usrId User ID.
      * @param txt
      * @param mdlId Model ID.
      * @param usrAgent
      * @param rmtAddr
      * @param data
      * @param enableLog
      * @return
      */
    @throws[NCE]
    private def spawnAskFuture(
        srvReqId: String,
        usrId: Long,
        txt: String,
        mdlId: String,
        usrAgent: Option[String],
        rmtAddr: Option[String],
        data: Option[String],
        enableLog: Boolean,
        parent: Span = null
    ): Unit = {
        val txt0 = txt.trim()

        val rcvTstamp = U.nowUtcTs()

        val usr = NCUserManager.getUserById(usrId, parent).getOrElse(throw new NCE(s"Unknown user ID: $usrId"))
        val company = NCCompanyManager.getCompany(usr.companyId, parent).getOrElse(throw new NCE(s"Unknown company ID: ${usr.companyId}"))

        // Check input length.
        if (txt0.split(" ").length > MAX_WORDS)
            throw new NCE(s"User input is too long (max is $MAX_WORDS words).")

        catching(wrapIE) {
            // Enlist for tracking.
            cache += srvReqId -> NCQueryStateMdo(
                srvReqId,
                modelId = mdlId,
                userId = usrId,
                companyId = company.id,
                email = usr.email,
                status = QRY_ENLISTED, // Initial status.
                enableLog = enableLog,
                text = txt0,
                userAgent = usrAgent,
                remoteAddress = rmtAddr,
                createTstamp = rcvTstamp,
                updateTstamp = rcvTstamp
            )
        }

        // Add processing log.
        NCProcessLogManager.newEntry(
            usrId,
            srvReqId,
            txt0,
            mdlId,
            QRY_ENLISTED,
            usrAgent.orNull,
            rmtAddr.orNull,
            rcvTstamp,
            data.orNull,
            parent
        )

        Future {
            startScopedSpan("future", parent, "srvReqId" -> srvReqId) { span =>
                val tbl = NCAsciiTable()

                tbl += (s"${b("Text")}", rv(" " + txt0 + " "))
                tbl += (s"${b("User ID")}", usr.id)
                tbl += (s"${b("Model ID")}", mdlId)
                tbl += (s"${b("Agent")}", usrAgent.getOrElse("<n/a>"))
                tbl += (s"${b("Remote Address")}", rmtAddr.getOrElse("<n/a>"))
                tbl += (s"${b("Server Request ID")}", m(srvReqId))
                tbl += (s"${b("Data")}", U.prettyJson(data.orNull).split("\n").toSeq)

                logger.info(s"New user request received:\n$tbl")

                val enabledBuiltInToks = NCProbeManager.getModel(mdlId, span).enabledBuiltInTokens

                @throws[NCE]
                def unzipProperties(gzipOpt: Option[String]): Option[JavaMeta] =
                    gzipOpt match {
                        case Some(gzip) => Some(jsonToJavaMap(uncompress(gzip)))
                        case None => None
                    }

                // Enrich the user input and send it to the probe.
                NCProbeManager.askProbe(
                    srvReqId,
                    usr,
                    company,
                    mdlId,
                    txt0,
                    NCServerEnrichmentManager.enrichPipeline(srvReqId, txt0, enabledBuiltInToks),
                    usrAgent,
                    rmtAddr,
                    data,
                    unzipProperties(usr.propertiesGzip),
                    unzipProperties(company.propertiesGzip),
                    enableLog,
                    span
                )
            }
        } onComplete {
            case Success(_) => // No-op.

            case Failure(e: NCE) =>
                logger.error(s"Query processing failed: ${e.getLocalizedMessage}")

                setError(
                    srvReqId,
                    e.getLocalizedMessage,
                    NCErrorCodes.SYSTEM_ERROR
                )

            case Failure(e: Throwable) =>
                U.prettyError(logger,s"System error processing query: ${e.getLocalizedMessage}", e)

                setError(
                    srvReqId,
                    "Processing failed due to a system error.",
                    NCErrorCodes.UNEXPECTED_ERROR
                )
        }
    }

    /**
      *
      * @param srvReqId Server request ID.
      * @param errMsg
      * @param errCode
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def setError(srvReqId: String, errMsg: String, errCode: Int, parent: Span = null): Unit = {
        startScopedSpan("setError", parent,
            "srvReqId" -> srvReqId,
            "errMsg" -> errMsg,
            "errCode" -> errCode) { span =>
            val now = U.nowUtcTs()
        
            val found = catching(wrapIE) {
                cache(srvReqId) match {
                    case Some(copy) =>
                        copy.updateTstamp = now
                        copy.status = QRY_READY.toString
                        copy.error = Some(errMsg)
                        copy.errorCode = Some(errCode)
    
                        recordStats(M_ROUND_TRIP_LATENCY_MS -> (copy.updateTstamp.getTime - copy.createTstamp.getTime))
    
                        cache += srvReqId -> copy
    
                        true
                    case None =>
                        // Safely ignore missing status (cancelled before).
                        ignore(srvReqId)
    
                        false
                }
            }
            
            if (found)
                NCProcessLogManager.updateReady(
                    srvReqId,
                    now,
                    Some(errMsg),
                    None,
                    None,
                    None,
                    None,
                    span
                )
        }
    }
    
    /**
      * 
      * @param srvReqId Server request ID.
      * @param resType
      * @param resBody
      * @param resMeta
      * @param logJson
      * @param intentId
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def setResult(
        srvReqId: String,
        resType: String,
        resBody: String,
        resMeta: Option[JavaMeta],
        logJson: Option[String],
        intentId: Option[String],
        parent: Span = null
    ): Unit = {
        startScopedSpan("setResult", parent,
            "srvReqId" -> srvReqId,
            "resType" -> resType,
            "resBody" -> resBody,
            "intentId" -> intentId
        ) { span =>
            val now = U.nowUtcTs()
            
            val found = catching(wrapIE) {
                cache(srvReqId) match {
                    case Some(copy) =>
                        copy.updateTstamp = now
                        copy.status = QRY_READY.toString
                        copy.resultType = Some(resType)
                        copy.resultBody = Some(resBody)
                        copy.resultMeta = resMeta
                        copy.logJson = logJson
                        copy.intentId = intentId
    
                        recordStats(M_ROUND_TRIP_LATENCY_MS -> (copy.updateTstamp.getTime - copy.createTstamp.getTime))
    
                        cache += srvReqId -> copy
    
                        true
                    case None =>
                        // Safely ignore missing status (cancelled before).
                        ignore(srvReqId)
    
                        false
                }
            }
            
            if (found)
                NCProcessLogManager.updateReady(
                    srvReqId,
                    now,
                    None,
                    resType = Some(resType),
                    resBody = Some(resBody),
                    resMeta = resMeta,
                    intentId = intentId,
                    span
                )
        }
    }

    /**
      *
      * @param srvReqId Server request ID.
      */
    private def ignore(srvReqId: String): Unit =
        logger.warn(s"Server request not found - safely ignoring (expired or cancelled): $srvReqId")

    /**
      *
      * @param arg User ID or server request IDs.
      */
    @throws[NCE]
    private def cancel0(arg: Either[Long/*User ID*/, Set[String]/*Server request IDs*/]): Unit = {
        val now = U.nowUtcTs()

        val srvReqIds = catching(wrapIE) {
            NCTxManager.startTx {
                val srvReqIds =
                    if (arg.isLeft)
                        cache.values.filter(_.userId == arg.swap.toOption.get).map(_.srvReqId).toSet
                    else
                        arg.toOption.get

                cache --= srvReqIds.toSeq

                srvReqIds
            }
        }

        for (srvReqId <- srvReqIds)
            NCProcessLogManager.updateCancel(srvReqId, now)
    }

    /**
      * Handler for `/cancel` REST call.
      *
      * @param srvReqIds Server request IDs.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def cancelForServerRequestIds(srvReqIds: Set[String], parent: Span = null): Unit =
        startScopedSpan("cancel", parent, "srvReqIds" -> srvReqIds.mkString(",")) { _ =>
            cancel0(Right(srvReqIds))
        }

    /**
      * Handler for `/cancel` REST call.
      *
      * @param usrId User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def cancelForUserId(usrId: Long, parent: Span = null): Unit =
        startScopedSpan("cancel", parent, "usrId" -> usrId) { _ =>
            cancel0(Left(usrId))
        }

    /**
      *
      * @param srvReqIds
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getForServerRequestIds(srvReqIds: Set[String], parent: Span = null): Set[NCQueryStateMdo] =
        startScopedSpan("getForSrvReqIds", parent, "srvReqIds" -> srvReqIds.mkString(",")) { _ =>
            catching(wrapIE) {
                srvReqIds.flatMap(id => cache(id))
            }
        }

    /**
      *
      * @param usrId User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getForUserId(usrId: Long, parent: Span = null): Set[NCQueryStateMdo] =
        startScopedSpan("getForUserId", parent, "isrId" -> usrId) { _ =>
            catching(wrapIE) {
                cache.values.filter(_.userId == usrId).toSet
            }
        }

    /**
      *
      * @param srvReqId Server request ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def contains(srvReqId: String, parent: Span = null): Boolean =
        startScopedSpan("contains", parent, "srvReqId" -> srvReqId) { _ =>
            catching(wrapIE) {
                cache.containsKey(srvReqId)
            }
        }
}
