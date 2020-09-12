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

package org.apache.nlpcraft.probe.mgrs.dialogflow

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Dialog flow manager.
 */
object NCDialogFlowManager extends NCService {
    case class Key(usrId: Long, mdlId: String)
    case class Value(intent: String, tstamp: Long)

    private object Config extends NCConfigurable {
        def periodMs: Long = getInt(s"nlpcraft.probe.dialog.check.period.secs") * 1000

        def check(): Unit =
            if (periodMs <= 0)
                abortWith(s"Value of 'nlpcraft.probe.conversation.check.period.secs' must be positive")
    }

    Config.check()

    @volatile private var flow: mutable.Map[Key, ArrayBuffer[Value]] = _
    @volatile private var gc: ScheduledExecutorService = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        flow = mutable.HashMap.empty[Key, ArrayBuffer[Value]]
        gc = Executors.newSingleThreadScheduledExecutor

        gc.scheduleWithFixedDelay(() ⇒ clearForTimeout(), Config.periodMs, Config.periodMs, TimeUnit.MILLISECONDS)

        logger.info(s"Dialog flow manager GC started [checkPeriodMs=${Config.periodMs}]")

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        U.shutdownPools(gc)

        logger.info("Dialog flow manager GC stopped")

        super.stop()
    }

    /**
     * Adds matched (winning) intent to the dialog flow for the given user and model IDs.
     *
     * @param intent Intent ID.
     * @param usrId User ID.
     * @param mdlId Model ID.
     */
    def addMatchedIntent(intent: String, usrId: Long, mdlId: String, parent: Span = null): Unit = {
        startScopedSpan("addMatchedIntent", parent, "usrId" → usrId, "modelId" → mdlId) { _ ⇒
            flow.synchronized {
                flow.getOrElseUpdate(Key(usrId, mdlId), ArrayBuffer.empty[Value]).append(
                    Value(intent, System.currentTimeMillis())
                )
            }

            logger.trace(s"Added to dialog flow [intent=$intent, userId=$usrId, modelId=$mdlId]")
        }
    }

    /**
     * Gets sequence of intent ID sorted from oldest to newest (i.e. dialog flow) for given user and model IDs.
     *
     * @param usrId User ID.
     * @param mdlId Model ID.
     * @return Dialog flow.
     */
    def getDialogFlow(usrId: Long, mdlId: String, parent: Span = null): Seq[String] = {
        startScopedSpan("getDialogFlow", parent, "usrId" → usrId, "modelId" → mdlId) { _ ⇒
            flow.synchronized {
                flow.getOrElseUpdate(Key(usrId, mdlId), ArrayBuffer.empty[Value]).map(_.intent)
            }
        }
    }

    /**
     *
     */
    private def clearForTimeout(): Unit =
        startScopedSpan("clearForTimeout", "checkPeriodMs" → Config.periodMs) { _ ⇒
            try
                flow.synchronized {
                    val delKeys = ArrayBuffer.empty[Key]

                    for ((key, values) ← flow)
                        NCModelManager.getModelDataOpt(key.mdlId) match {
                            case Some(data) ⇒
                                val ms = System.currentTimeMillis() -data.model.getDialogTimeout

                                values --= values.filter(_.tstamp < ms)

                            case None ⇒ delKeys += key
                        }

                    flow --= delKeys
                }
            catch {
                case e: Throwable ⇒ logger.error("Clean method unexpected error", e)
            }
        }
    
    /**
     * Clears dialog for given user and model IDs.
     *
     * @param usrId User ID.
     * @param mdlId Model ID.
     * @param parent Parent span, if any.
     */
    def clear(usrId: Long, mdlId: String, parent: Span = null): Unit =
        startScopedSpan("clear", parent, "usrId" → usrId, "modelId" → mdlId) { _ ⇒
            flow.synchronized {
                flow -= Key(usrId, mdlId)
            }
        }
    
    /**
     * Clears dialog for given use, model IDs and predicate.
     *
     * @param usrId User ID.
     * @param mdlId Model ID.
     * @param pred Intent ID predicate.
     * @param parent Parent span, if any.
     */
    def clearForPredicate(usrId: Long, mdlId: String, pred: String ⇒ Boolean, parent: Span = null): Unit =
        startScopedSpan("clearForPredicate", parent, "usrId" → usrId, "modelId" → mdlId) { _ ⇒
            val key = Key(usrId, mdlId)

            flow.synchronized {
                flow(key) = flow(key).filterNot(v ⇒ pred(v.intent))
            }
        }
}
