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

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection._

/**
  * Conversation manager.
  */
object NCConversationManager extends NCService {
    case class Key(userId: Long, mdlId: String)
    case class Value(conv: NCConversation, var tstamp: Long = 0)

    private final val convs: mutable.Map[Key, Value] = mutable.HashMap.empty[Key, Value]

    @volatile private var gc: Thread = _

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        gc = U.mkThread("conversation-manager-gc") { t =>
            while (!t.isInterrupted)
                try
                    convs.synchronized {
                        val sleepTime = clearForTimeout() - System.currentTimeMillis()

                        if (sleepTime > 0)
                            convs.wait(sleepTime)
                    }
                catch {
                    case _: InterruptedException => // No-op.
                    case e: Throwable => U.prettyError(logger, s"Unexpected error for thread: ${t.getName}", e)
                }
        }

        gc.start()

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        U.stopThread(gc)

        gc = null

        convs.clear()

        ackStopped()
    }

    /**
      * Gets next clearing time.
      */
    private def clearForTimeout(): Long =
        startScopedSpan("clearForTimeout") { _ =>
            require(Thread.holdsLock(convs))

            val now = System.currentTimeMillis()
            val delKeys = mutable.HashSet.empty[Key]

            for ((key, value) <- convs) {
                val del =
                    NCModelManager.getModelOpt(key.mdlId) match {
                        case Some(mdl) => value.tstamp < now - mdl.model.getConversationTimeout
                        case None => true
                    }

                if (del) {
                    value.conv.getUserData.clear()

                    delKeys += key
                }
            }

            convs --= delKeys

            if (convs.nonEmpty)
                convs.values.map(v => v.tstamp + v.conv.timeoutMs).min
            else
                Long.MaxValue
        }

    /**
      * Gets conversation for given key.
      *
      * @param usrId User ID.
      * @param mdlId Model ID.
      * @return New or existing conversation.
      */
    def getConversation(usrId: Long, mdlId: String, parent: Span = null): NCConversation =
        startScopedSpan("getConversation", parent, "usrId" -> usrId, "mdlId" -> mdlId) { _ =>
            val mdl = NCModelManager.getModel(mdlId).model

            convs.synchronized {
                val v = convs.getOrElseUpdate(
                    Key(usrId, mdlId),
                    Value(NCConversation(usrId, mdlId, mdl.getConversationTimeout, mdl.getConversationDepth))
                )

                v.tstamp = U.nowUtcMs()

                convs.notifyAll()

                v.conv
            }
        }
}
