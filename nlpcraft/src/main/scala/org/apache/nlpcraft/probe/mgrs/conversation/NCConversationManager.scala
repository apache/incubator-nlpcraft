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

package org.apache.nlpcraft.probe.mgrs.conversation

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection._
import scala.collection.mutable.ArrayBuffer

/**
  * Conversation manager.
  */
object NCConversationManager extends NCService {
    case class Key(userId: Long, mdlId: String)
    case class Value(conv: NCConversation, var tstamp: Long = 0)

    private object Config extends NCConfigurable {
        def periodMs: Long = getInt(s"nlpcraft.probe.conversation.check.period.secs") * 1000

        def check(): Unit =
            if (periodMs <= 0)
                abortWith(s"Value of 'nlpcraft.probe.conversation.check.period.secs' must be positive.")
    }

    Config.check()

    @volatile private var convs: mutable.Map[Key, Value] = _
    @volatile private var gc: ScheduledExecutorService = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        gc = Executors.newSingleThreadScheduledExecutor
        convs = mutable.HashMap.empty[Key, Value]
        
        gc.scheduleWithFixedDelay(() ⇒ clearForTimeout(), Config.periodMs, Config.periodMs, TimeUnit.MILLISECONDS)
        
        logger.info(s"Conversation manager GC started [checkPeriodMs=${Config.periodMs}]") // TODO

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        U.shutdownPools(gc)
    
        logger.info("Conversation manager GC stopped.")

        super.stop()
    }

    /**
      *
      */
    private def clearForTimeout(): Unit =
        startScopedSpan("clearForTimeout", "checkPeriodMs" → Config.periodMs) { _ ⇒
            try
                convs.synchronized {
                    val delKeys = ArrayBuffer.empty[Key]

                    for ((key, value) ← convs)
                        NCModelManager.getModelDataOpt(key.mdlId) match {
                            case Some(data) ⇒
                                if (value.tstamp < System.currentTimeMillis() - data.model.getConversationTimeout)
                                    delKeys += key

                            case None ⇒ delKeys += key
                        }

                    convs --= delKeys
                }
            catch {
                case e: Throwable ⇒ logger.error("Clean method unexpected error", e)
            }

        }

    /**
      * Gets conversation for given key.
      *
      * @param usrId User ID.
      * @param mdlId Model ID.
      * @return New or existing conversation.
      */
    def getConversation(usrId: Long, mdlId: String, parent: Span = null): NCConversation =
        startScopedSpan("getConversation", parent, "usrId" → usrId, "modelId" → mdlId) { _ ⇒
            val mdl = NCModelManager.getModelData(mdlId).model

            convs.synchronized {
                val v = convs.getOrElseUpdate(
                    Key(usrId, mdlId),
                    Value(NCConversation(usrId, mdlId, mdl.getConversationStmThreshold, mdl.getConversationMaxDepth))
                )
                
                v.tstamp = U.nowUtcMs()
                
                v
            }.conv
        }
}
