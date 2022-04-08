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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils

import scala.collection.*
import scala.jdk.CollectionConverters.*

/**
  * Conversation manager.
  */
class NCConversationManager(cfg: NCModelConfig) extends LazyLogging:
    case class Value(conv: NCConversationData, var tstamp: Long = 0)
    private final val convs: mutable.Map[String, Value] = mutable.HashMap.empty[String, Value]
    @volatile private var gc: Thread = _

    /**
      * Gets conversation for given user ID.
      *
      * @param usrId User ID.
      * @return New or existing conversation.
      */
    def getConversation(usrId: String): NCConversationData =
        convs.synchronized {
            val v = convs.getOrElseUpdate(
                usrId,
                Value(NCConversationData(usrId, cfg.getId, cfg.getConversationTimeout, cfg.getConversationDepth))
            )

            v.tstamp = NCUtils.nowUtcMs()
            convs.notifyAll()
            v.conv
        }

    /**
      * Gets next clearing time.
      */
    private def clearForTimeout(): Long =
        require(Thread.holdsLock(convs))

        val now = NCUtils.now()
        val delKeys = mutable.HashSet.empty[String]

        for ((key, value) <- convs)
            if value.tstamp < now - cfg.getConversationTimeout then
                value.conv.clear()
                delKeys += key

        convs --= delKeys

        if convs.nonEmpty then convs.values.map(v => v.tstamp + v.conv.timeoutMs).min
        else Long.MaxValue

    /**
      *
      * @return
      */
    def start(): Unit =
        gc = NCUtils.mkThread("conv-mgr-gc", cfg.getId) { t =>
            while (!t.isInterrupted)
                try
                    convs.synchronized {
                        val sleepTime = clearForTimeout() - NCUtils.now()
                        if sleepTime > 0 then
                            logger.trace(s"${t.getName} waits for $sleepTime ms.")
                            convs.wait(sleepTime)
                    }
                catch
                    case _: InterruptedException => // No-op.
                    case e: Throwable => logger.error(s"Unexpected error for thread: ${t.getName}", e)
        }
        gc.start()

    /**
      *
      */
    def close(): Unit =
        NCUtils.stopThread(gc)
        gc = null
        convs.clear()
