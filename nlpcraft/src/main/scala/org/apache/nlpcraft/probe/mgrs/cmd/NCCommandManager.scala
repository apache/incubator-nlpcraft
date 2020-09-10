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

package org.apache.nlpcraft.probe.mgrs.cmd

import java.io.Serializable
import java.util.concurrent.{ExecutorService, Executors}

import com.google.gson.Gson
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.probe.mgrs.NCProbeMessage
import org.apache.nlpcraft.probe.mgrs.conn.NCConnectionManager
import org.apache.nlpcraft.probe.mgrs.conversation.NCConversationManager
import org.apache.nlpcraft.probe.mgrs.dialogflow.NCDialogFlowManager
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnrichmentManager

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * Probe commands processor.
  */
object NCCommandManager extends NCService {
    private final val GSON = new Gson()

    @volatile private var pool: ExecutorService = _
    @volatile private var executor: ExecutionContextExecutor = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        pool = Executors.newCachedThreadPool()
        executor = ExecutionContext.fromExecutor(pool)

        super.start()
    }
    
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()

        NCUtils.shutdownPools(pool)

        executor = null
        pool = null
    }
    
    /**
      *
      * @param msg Server message to process.
      * @param parent Optional parent span.
      */
    def processServerMessage(msg: NCProbeMessage, parent: Span = null): Unit =
        startScopedSpan("processServerMessage", parent,
            "msgType" → msg.getType,
            "srvReqId" → msg.dataOpt[String]("srvReqId").getOrElse(""),
            "usrId" → msg.dataOpt[Long]("userId").getOrElse(-1),
            "modelId" → msg.dataOpt[String]("mdlId").getOrElse("")) { span ⇒
            if (msg.getType != "S2P_PING")
                logger.trace(s"Probe server message received: $msg")
            
            try
                msg.getType match {
                    case "S2P_PING" ⇒ ()
    
                    case "S2P_CLEAR_CONV" ⇒
                        NCConversationManager.getConversation(
                            msg.data[Long]("usrId"),
                            msg.data[String]("mdlId"),
                            span
                        ).clearTokens((_: NCToken) ⇒ true)

                    case "S2P_CLEAR_DLG" ⇒
                        NCDialogFlowManager.clear(
                            msg.data[Long]("usrId"),
                            msg.data[String]("mdlId"),
                            span
                        )

                    case "S2P_ASK" ⇒
                        NCProbeEnrichmentManager.ask(
                            srvReqId = msg.data[String]("srvReqId"),
                            txt = msg.data[String]("txt"),
                            nlpSens = msg.data[java.util.List[NCNlpSentence]]("nlpSens").asScala,
                            usrId = msg.data[Long]("userId"),
                            senMeta = msg.data[java.util.Map[String, Serializable]]("senMeta").asScala,
                            mdlId = msg.data[String]("mdlId"),
                            logEnable = msg.data[Boolean]("logEnable"),
                            span
                    )

                    case "S2P_MODEL_INFO" ⇒
                        val mdlId = msg.data[String]("mdlId")

                        val mdlData = NCModelManager.getModelData(mdlId)

                        val macros = mdlData.model.getMacros.asInstanceOf[Serializable]
                        val syns = mdlData.model.getElements.asScala.
                            map(p ⇒ p.getId → p.getSynonyms).toMap.asJava.asInstanceOf[Serializable]
                        val samples = mdlData.samples.map(p ⇒ p._1 → p._2.asJava).
                            asJava.asInstanceOf[Serializable]

                        NCConnectionManager.send(
                            NCProbeMessage(
                                "P2S_MODEL_INFO",
                                "reqGuid" → msg.getGuid,
                                "resp" → GSON.toJson(
                                    Map(
                                        "macros" → macros,
                                        "synonyms" → syns,
                                        "samples" → samples
                                    ).asJava
                                )
                            ),
                            span
                        )

                    case _ ⇒
                        logger.error(s"Received unknown server message (you need to update the probe): ${msg.getType}")
                }
            catch {
                case e: Throwable ⇒ logger.error(s"Error while processing server message (ignoring): ${msg.getType}", e)
            }
        }
}
