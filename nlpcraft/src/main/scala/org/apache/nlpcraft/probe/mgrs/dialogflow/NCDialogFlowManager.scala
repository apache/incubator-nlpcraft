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

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection._

/**
 * Dialog flow manager.
 */
object NCDialogFlowManager extends NCService {
    case class Key(usrId: Long, mdlId: String)
    case class Value(intent: String, tstamp: Long)

    private final val flow = mutable.HashMap.empty[Key, mutable.ArrayBuffer[Value]]

    @volatile private var gc: Thread = _

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        ackStarting()

        gc = U.mkThread("dialog-flow-manager-gc") { t ⇒
            while (!t.isInterrupted)
                try
                    flow.synchronized {
                        val sleepTime = clearForTimeout() - System.currentTimeMillis()

                        if (sleepTime > 0)
                            flow.wait(sleepTime)
                    }
                catch {
                    case _: InterruptedException ⇒ // No-op.
                    case e: Throwable ⇒ U.prettyError(logger, s"Unexpected error for thread: ${t.getName}", e)
                }
        }

        gc.start()

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        ackStopping()

        U.stopThread(gc)

        gc = null

        flow.clear()

        ackStopped()
    }

    /**
     * Adds matched (winning) intent to the dialog flow for the given user and model IDs.
     *
     * @param intId Intent ID.
     * @param usrId User ID.
     * @param mdlId Model ID.
     */
    def addMatchedIntent(intId: String, usrId: Long, mdlId: String, parent: Span = null): Unit = {
        startScopedSpan("addMatchedIntent", parent, "usrId" → usrId, "mdlId" → mdlId, "intId" → intId) { _ ⇒
            flow.synchronized {
                flow.getOrElseUpdate(Key(usrId, mdlId), mutable.ArrayBuffer.empty[Value]).append(
                    Value(intId, System.currentTimeMillis())
                )

                flow.notifyAll()
            }

            logger.trace(s"Added to dialog flow [mdlId=$mdlId, intId=$intId, userId=$usrId]")
        }
    }

    /**
     * Gets sequence of intent ID sorted from oldest to newest (i.e. dialog flow) for given user and model IDs.
     *
     * @param usrId User ID.
     * @param mdlId Model ID.
     * @return Dialog flow.
     */
    def getDialogFlow(usrId: Long, mdlId: String, parent: Span = null): Seq[String] =
        startScopedSpan("getDialogFlow", parent, "usrId" → usrId, "mdlId" → mdlId) { _ ⇒
            flow.synchronized {
                flow.getOrElseUpdate(Key(usrId, mdlId), mutable.ArrayBuffer.empty[Value]).map(_.intent)
            }
        }

    /**
     *  Gets next clearing time.
     */
    private def clearForTimeout(): Long =
        startScopedSpan("clearForTimeout") { _ ⇒
            require(Thread.holdsLock(flow))

            val now = System.currentTimeMillis()
            val delKeys = mutable.HashSet.empty[Key]
            val timeouts = mutable.HashMap.empty[String, Long]

            for ((key, values) ← flow)
                NCModelManager.getModelOpt(key.mdlId) match {
                    case Some(mdl) ⇒
                        val ms = now - mdl.model.getConversationTimeout

                        values --= values.filter(_.tstamp < ms)

                        timeouts += mdl.model.getId → mdl.model.getConversationTimeout

                        // https://github.com/scala/bug/issues/10151
                        // Scala bug workaround.
                        ()

                    case None ⇒ delKeys += key
                }

            delKeys ++= flow.filter(_._2.isEmpty).keySet

            flow --= delKeys

            val times = (for ((key, values) ← flow) yield values.map(v ⇒ v.tstamp + timeouts(key.mdlId))).flatten

            if (times.nonEmpty)
                times.min
            else
                Long.MaxValue
        }
    
    /**
     * Clears dialog history for given user and model IDs.
     *
     * @param usrId User ID.
     * @param mdlId Model ID.
     * @param parent Parent span, if any.
     */
    def clear(usrId: Long, mdlId: String, parent: Span = null): Unit =
        startScopedSpan("clear", parent, "usrId" → usrId, "mdlId" → mdlId) { _ ⇒
            flow.synchronized {
                flow -= Key(usrId, mdlId)

                flow.notifyAll()
            }

            logger.trace(s"Dialog history is cleared [" +
                s"usrId=$usrId, " +
                s"mdlId=$mdlId" +
            s"]")
        }
    
    /**
     * Clears dialog history for given user, model IDs and predicate.
     *
     * @param usrId User ID.
     * @param mdlId Model ID.
     * @param pred Intent ID predicate.
     * @param parent Parent span, if any.
     */
    def clearForPredicate(usrId: Long, mdlId: String, pred: String ⇒ Boolean, parent: Span = null): Unit =
        startScopedSpan("clearForPredicate", parent, "usrId" → usrId, "mdlId" → mdlId) { _ ⇒
            val key = Key(usrId, mdlId)

            flow.synchronized {
                flow(key) = flow(key).filterNot(v ⇒ pred(v.intent))

                flow.notifyAll()
            }

            logger.trace(s"Dialog history is cleared [" +
                s"usrId=$usrId, " +
                s"mdlId=$mdlId" +
            s"]")
        }
}
