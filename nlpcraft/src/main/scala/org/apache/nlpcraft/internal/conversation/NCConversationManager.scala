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

package org.apache.nlpcraft.internal.conversation

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager

import scala.collection._

/**
  * Conversation manager.
  */
object NCConversationManager:
    case class Key(usrId: Long, mdlId: String)
    case class Value(conv: NCConversation, var tstamp: Long = 0)

    private final val convs: mutable.Map[Key, Value] = mutable.HashMap.empty[Key, Value]

    @volatile private var gc: Thread = _

    /**
      *
      * @return
      */
    def start(): NCService =
        gc = NCUtils.mkThread("conversation-manager-gc") { t =>
            while (!t.isInterrupted)
                try
                    convs.synchronized {
                        val sleepTime = clearForTimeout() - U.now()
                        if sleepTime > 0 then convs.wait(sleepTime)
                    }
                catch
                    case _: InterruptedException => // No-op.
                    case e: Throwable => U.prettyError(logger, s"Unexpected error for thread: ${t.getName}", e)
        }
        gc.start()

    /**
      *
      */
    def stop(): Unit =
        NCUtils.stopThread(gc)
        gc = null
        convs.clear()

    /**
      * Gets next clearing time.
      */
    private def clearForTimeout(): Long =
        require(Thread.holdsLock(convs))

        val now = U.now()
        val delKeys = mutable.HashSet.empty[Key]

        for ((key, value) <- convs)
            val del =
                NCModelManager.getModelOpt(key.mdlId) match
                    case Some(mdl) => value.tstamp < now - mdl.model.getConversationTimeout
                    case None => true

            if del then
                value.conv.getUserData.clear()
                delKeys += key

        convs --= delKeys

        if convs.nonEmpty then convs.values.map(v => v.tstamp + v.conv.timeoutMs).min
        else Long.MaxValue

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
