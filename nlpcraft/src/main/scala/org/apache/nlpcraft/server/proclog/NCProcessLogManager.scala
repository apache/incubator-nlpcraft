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

package org.apache.nlpcraft.server.proclog

import java.sql.Timestamp

import io.opencensus.trace.Span
import org.apache.ignite.{IgniteAtomicSequence, IgniteSemaphore}
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.server.apicodes.NCApiStatusCode.NCApiStatusCode
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.mdo.NCProbeMdo
import org.apache.nlpcraft.server.sql.{NCSql, NCSqlManager}

import scala.util.control.Exception.catching

/**
  * Process log manager.
  */
object NCProcessLogManager extends NCService with NCIgniteInstance {
    @volatile private var logSeq: IgniteAtomicSequence = _
    @volatile private var logLock: IgniteSemaphore = _

    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        catching(wrapIE) {
            logSeq = NCSql.mkSeq(ignite, "logSeq", "proc_log", "id")

            logLock = ignite.semaphore("logSemaphore", 1, true, true)
        }
     
        super.start()
    }

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
      * 
      * @param srvReqId Server request ID.
      * @param tstamp
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateCancel(
        srvReqId: String,
        tstamp: Timestamp,
        parent: Span = null): Unit =
        startScopedSpan("updateCancel", parent, "srvReqId" → srvReqId) { span ⇒
            NCSql.sql {
                NCSqlManager.updateCancelProcessingLog(
                    srvReqId,
                    tstamp,
                    span
                )
            }
        }
    
    /**
      * Updates log entry with given result parameters.
      * 
      * @param srvReqId ID of the server request to update.
      * @param tstamp
      * @param errMsg
      * @param resType
      * @param resBody
      * @param intentId
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateReady(
        srvReqId: String,
        tstamp: Timestamp,
        errMsg: Option[String],
        resType: Option[String],
        resBody: Option[String],
        intentId: Option[String],
        parent: Span = null
    ): Unit =
        startScopedSpan("updateReady", parent,
            "srvReqId" → srvReqId,
            "errMsg" → errMsg.getOrElse(""),
            "resType" → resType.getOrElse(""),
            "resBody" → resBody.getOrElse("")) { span ⇒
            NCSql.sql {
                NCSqlManager.updateReadyProcessingLog(
                    srvReqId,
                    errMsg.orNull,
                    resType.orNull,
                    resBody.orNull,
                    intentId.orNull,
                    tstamp,
                    span
                )
            }
        }
    
    /**
      * Updates log entry with given result parameters.
      *
      * @param srvReqId ID of the server request to update.
      * @param probe
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateProbe(
        srvReqId: String,
        probe: NCProbeMdo,
        parent: Span = null): Unit =
        startScopedSpan("updateProbe", parent,
            "srvReqId" → srvReqId,
            "probeId" → probe.probeId) { span ⇒
                NCSql.sql {
                    NCSqlManager.updateProbeProcessingLog(
                        srvReqId,
                        probe.probeToken,
                        probe.probeId,
                        probe.probeGuid,
                        probe.probeApiVersion,
                        probe.probeApiDate,
                        probe.osVersion,
                        probe.osName,
                        probe.osArch,
                        probe.startTstamp,
                        probe.tmzId,
                        probe.tmzAbbr,
                        probe.tmzName,
                        probe.userName,
                        probe.javaVersion,
                        probe.javaVendor,
                        probe.hostName,
                        probe.hostAddr,
                        probe.macAddr,
                        span
                    )
                }
        }

    /**
      * Adds new processing log entry.
      * 
      * @param usrId User ID.
      * @param srvReqId Server request ID.
      * @param txt
      * @param mdlId Model ID.
      * @param status
      * @param usrAgent
      * @param rmtAddr
      * @param rcvTstamp
      * @param data
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def newEntry(
        usrId: Long,
        srvReqId: String,
        txt: String,
        mdlId: String,
        status: NCApiStatusCode,
        usrAgent: String,
        rmtAddr: String,
        rcvTstamp: Timestamp,
        data: String,
        parent: Span = null
    ): Unit =
        startScopedSpan("newEntry", parent, "srvReqId" → srvReqId, "usrId" → usrId, "modelId" → mdlId) { span ⇒
            val id = logSeq.incrementAndGet()

            logLock.acquire()

            try {
                NCSql.sql {
                    if (NCSqlManager.isLogExists(srvReqId, span))
                        throw new NCE(s"Log with given server request ID already exists: $srvReqId")

                    NCSqlManager.newProcessingLog(
                        id,
                        usrId,
                        srvReqId,
                        txt,
                        mdlId,
                        status,
                        usrAgent,
                        rmtAddr,
                        rcvTstamp,
                        data,
                        span
                    )
                }
            }
            finally
                logLock.release()
        }
}
