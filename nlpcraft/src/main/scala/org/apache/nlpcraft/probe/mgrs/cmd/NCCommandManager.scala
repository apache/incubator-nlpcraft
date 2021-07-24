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

package org.apache.nlpcraft.probe.mgrs.cmd

import com.google.gson.Gson
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.probe.mgrs.NCProbeMessage
import org.apache.nlpcraft.probe.mgrs.conn.NCConnectionManager
import org.apache.nlpcraft.probe.mgrs.conversation.NCConversationManager
import org.apache.nlpcraft.probe.mgrs.dialogflow.NCDialogFlowManager
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager
import org.apache.nlpcraft.probe.mgrs.nlp.NCProbeEnrichmentManager

import java.io.{Serializable => JSerializable}
import java.util
import java.util.{Collections, List => JList}
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, MapHasAsScala, SeqHasAsJava, SetHasAsScala}

/**
  * Probe commands processor.
  */
object NCCommandManager extends NCService {
    private final val GSON = new Gson()

    /**
     * Starts this service.
     *
     * @param parent Optional parent span.
     */
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()
        ackStarted()
    }

    /**
     * Stops this service.
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }

    /**
      *
      * @param mkMsg
      * @param mkErrorMsg
      */
    private def send0(mkMsg: () => NCProbeMessage, mkErrorMsg: Throwable => NCProbeMessage, parent: Span = null): Unit = {
        val msgOpt: Option[NCProbeMessage] =
            try
                Some(mkMsg())
            catch {
                case e: Throwable =>
                    NCConnectionManager.send(mkErrorMsg(e), parent)

                    None
            }

        msgOpt match {
            case Some(msg) => NCConnectionManager.send(msg, parent)
            case None => // No-op.
        }
    }

    /**
      *
      * @param msg Server message to process.
      * @param parent Optional parent span.
      */
    def processServerMessage(msg: NCProbeMessage, parent: Span = null): Unit = {
        require(isStarted)
        
        startScopedSpan("processServerMessage", parent,
            "msgType" -> msg.getType,
            "srvReqId" -> msg.dataOpt[String]("srvReqId").getOrElse(""),
            "usrId" -> msg.dataOpt[Long]("userId").getOrElse(-1),
            "mdlId" -> msg.dataOpt[String]("mdlId").getOrElse("")) { span =>
            if (msg.getType != "S2P_PING")
                logger.trace(s"Probe server message received: $msg")
            
            try
                msg.getType match {
                    case "S2P_PING" => ()
    
                    case "S2P_CLEAR_CONV" =>
                        NCConversationManager.getConversation(
                            msg.data[Long]("usrId"),
                            msg.data[String]("mdlId"),
                            span
                        ).clearTokens((_: NCToken) => true)

                    case "S2P_CLEAR_DLG" =>
                        NCDialogFlowManager.clear(
                            msg.data[Long]("usrId"),
                            msg.data[String]("mdlId"),
                            span
                        )

                    case "S2P_ASK" =>
                        NCProbeEnrichmentManager.ask(
                            srvReqId = msg.data[String]("srvReqId"),
                            txt = msg.data[String]("txt"),
                            nlpSens = msg.data[JList[NCNlpSentence]]("nlpSens").asScala,
                            usrId = msg.data[Long]("userId"),
                            senMeta = msg.data[java.util.Map[String, JSerializable]]("senMeta").asScala.toMap,
                            mdlId = msg.data[String]("mdlId"),
                            enableLog = msg.data[Boolean]("enableLog"),
                            span
                    )

                    case "S2P_MODEL_INFO" =>
                        send0(
                            mkMsg = () => {
                                val mdlId = msg.data[String]("mdlId")

                                val mdlData = NCModelManager.getModel(mdlId)

                                val macros: util.Map[String, String] = mdlData.model.getMacros
                                val syns: util.Map[String, util.List[String]] =
                                    mdlData.model.getElements.asScala.map(p => p.getId -> p.getSynonyms).toMap.asJava
                                val samples: util.Map[String, util.List[util.List[String]]] =
                                    mdlData.samples.map(p => p._1 -> p._2.map(_.asJava).asJava).toMap.asJava

                                NCProbeMessage(
                                    "P2S_MODEL_INFO",
                                    "reqGuid" -> msg.getGuid,
                                    "resp" -> GSON.toJson(
                                        Map(
                                            "macros" -> macros.asInstanceOf[JSerializable],
                                            "synonyms" -> syns.asInstanceOf[JSerializable],
                                            "samples" -> samples.asInstanceOf[JSerializable]
                                        ).asJava
                                    )
                                )
                            },
                            mkErrorMsg = e =>
                                NCProbeMessage(
                                    "P2S_MODEL_INFO",
                                    "reqGuid" -> msg.getGuid,
                                    "error" -> e.getLocalizedMessage
                                ),
                            span
                        )

                    case "S2P_MODEL_ELEMENT_INFO" =>
                        send0(
                            mkMsg = () => {
                                val mdlId = msg.data[String]("mdlId")
                                val elmId = msg.data[String]("elmId")

                                val elm = NCModelManager.
                                    getModel(mdlId).
                                    model.getElements.asScala.find(_.getId == elmId).
                                    getOrElse(throw new NCE(s"Element not found in model: $elmId"))

                                val vals: util.Map[String, JList[String]] =
                                    if (elm.getValues != null)
                                        elm.getValues.asScala.map(e => e.getName -> e.getSynonyms).toMap.asJava
                                    else
                                        Collections.emptyMap()

                                NCProbeMessage(
                                    "P2S_MODEL_ELEMENT_INFO",
                                    "reqGuid" -> msg.getGuid,
                                    "resp" -> GSON.toJson(
                                        Map(
                                            "synonyms" -> elm.getSynonyms.asInstanceOf[JSerializable],
                                            "values" -> vals.asInstanceOf[JSerializable]
                                        ).asJava
                                    )
                                )
                            },
                            mkErrorMsg = e =>
                                NCProbeMessage(
                                    "P2S_MODEL_ELEMENT_INFO",
                                    "reqGuid" -> msg.getGuid,
                                    "error" -> e.getLocalizedMessage
                                ),
                            span
                        )

                    case _ =>
                        logger.error(s"Received unknown server message (you need to update the probe): ${msg.getType}")
                }
            catch {
                case e: Throwable => U.prettyError(logger, s"Error while processing server message (ignoring): ${msg.getType}", e)
            }
        }
    }
}
