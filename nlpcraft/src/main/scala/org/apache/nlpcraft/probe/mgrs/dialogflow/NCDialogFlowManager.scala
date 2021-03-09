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
import org.apache.nlpcraft.model.intent.solver.NCIntentSolverResult
import org.apache.nlpcraft.model.{NCContext, NCDialogFlowItem, NCIntentMatch}
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection._

/**
 * Dialog flow manager.
 */
object NCDialogFlowManager extends NCService {
    /**
      *
      * @param usrId
      * @param mdlId
      */
    case class Key(
        usrId: Long,
        mdlId: String
    )

    private final val flow = mutable.HashMap.empty[Key, mutable.ArrayBuffer[NCDialogFlowItem]]

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
      * Adds matched (winning) intent to the dialog flow.
      *
      * @param intentMatch
      * @param res Intent match result.
      * @param ctx Original query context.
      */
    def addMatchedIntent(intentMatch: NCIntentMatch, res: NCIntentSolverResult, ctx: NCContext, parent: Span = null): Unit = {
        val usrId = ctx.getRequest.getUser.getId
        val mdlId = ctx.getModel.getId
        val intentId = res.intentId
        
        startScopedSpan("addMatchedIntent", parent, "usrId" → usrId, "mdlId" → mdlId, "intentId" → intentId) { _ ⇒
            flow.synchronized {
                val req = ctx.getRequest
                
                val key = Key(usrId, mdlId)
                val item: NCDialogFlowItem = new NCDialogFlowItem {
                    override val getIntentId = intentId
                    override val getIntentTokens = intentMatch.getIntentTokens
                    override def getTermTokens(idx: Int) = intentMatch.getTermTokens(idx)
                    override def getTermTokens(termId: String) = intentMatch.getTermTokens(termId)
                    override val getVariant = intentMatch.getVariant
                    override val isAmbiguous = !res.isExactMatch // TODO: rename for consistency?
                    override val getUser = req.getUser
                    override val getCompany = req.getCompany
                    override val getServerRequestId = req.getServerRequestId
                    override val getNormalizedText = req.getNormalizedText
                    override val getReceiveTimestamp = req.getReceiveTimestamp
                    override val getRemoteAddress = req.getRemoteAddress
                    override val getClientAgent = req.getClientAgent
                    override val getRequestData = req.getRequestData
                }
                
                flow.getOrElseUpdate(key, mutable.ArrayBuffer.empty[NCDialogFlowItem]).append(item)
                flow.notifyAll()
            }

            logger.trace(s"Added matched intent to dialog flow [" +
                s"mdlId=$mdlId, " +
                s"intentId=$intentId, " +
                s"userId=$usrId" +
            s"]")
        }
    }

    /**
      * Gets sequence of dialog flow items sorted from oldest to newest (i.e. dialog flow) for given user and model IDs.
      *
      * @param usrId User ID.
      * @param mdlId Model ID.
      * @return Dialog flow.
      */
    def getDialogFlow(usrId: Long, mdlId: String, parent: Span = null): Seq[NCDialogFlowItem] =
        startScopedSpan("getDialogFlow", parent, "usrId" → usrId, "mdlId" → mdlId) { _ ⇒
            flow.synchronized {
                flow.getOrElseUpdate(Key(usrId, mdlId), mutable.ArrayBuffer.empty[NCDialogFlowItem])
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

                        values --= values.filter(_.getReceiveTimestamp < ms)

                        timeouts += mdl.model.getId → mdl.model.getConversationTimeout

                        // https://github.com/scala/bug/issues/10151
                        // Scala bug workaround.
                        ()

                    case None ⇒ delKeys += key
                }

            delKeys ++= flow.filter(_._2.isEmpty).keySet

            flow --= delKeys

            val times = (for ((key, values) ← flow) yield values.map(v ⇒ v.getReceiveTimestamp + timeouts(key.mdlId))).flatten

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

            logger.trace(s"Dialog flow history is cleared [" +
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
                flow(key) = flow(key).filterNot(v ⇒ pred(v.getIntentId))

                flow.notifyAll()
            }

            logger.trace(s"Dialog flow history is cleared [" +
                s"usrId=$usrId, " +
                s"mdlId=$mdlId" +
            s"]")
        }
}
