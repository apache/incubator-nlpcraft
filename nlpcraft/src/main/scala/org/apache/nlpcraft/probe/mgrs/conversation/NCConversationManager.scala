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

import scala.collection._
import scala.concurrent.duration._

/**
  * Conversation manager.
  */
object NCConversationManager extends NCService {
    case class Key(userId: Long, mdlId: String)
    case class Value(conv: NCConversationDescriptor, var tstamp: Long = 0)

    // Check frequency and timeout.
    private final val CHECK_PERIOD = 5.minutes.toMillis
    private final val TIMEOUT = 1.hour.toMillis

    @volatile private var convs: mutable.Map[Key, Value] = _

    @volatile private var gc: ScheduledExecutorService = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        gc = Executors.newSingleThreadScheduledExecutor
        convs = mutable.HashMap.empty[Key, Value]
        
        gc.scheduleWithFixedDelay(() ⇒ clearForTimeout(), CHECK_PERIOD, CHECK_PERIOD, TimeUnit.MILLISECONDS)
        
        logger.info(s"Conversation manager GC started [checkPeriodMs=$CHECK_PERIOD, timeoutMs=$TIMEOUT]")

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        U.shutdownPools(gc)
    
        logger.info("Conversation manager GC stopped")

        super.stop()
    }

    /**
      *
      */
    private def clearForTimeout(): Unit = {
        val ms = U.nowUtcMs() - TIMEOUT
    
        startScopedSpan("clear", "checkPeriodMs" → CHECK_PERIOD, "timeoutMs" → TIMEOUT) { _ ⇒
            convs.synchronized {
                convs --= convs.filter(_._2.tstamp < ms).keySet
            }
        }
    }

    /**
      * Gets conversation for given key.
      *
      * @param usrId User ID.
      * @param mdlId Model ID.
      * @return New or existing conversation.
      */
    def getConversation(usrId: Long, mdlId: String, parent: Span = null): NCConversationDescriptor =
        startScopedSpan("getConversation", parent, "usrId" → usrId, "modelId" → mdlId) { _ ⇒
            convs.synchronized {
                val v = convs.getOrElseUpdate(Key(usrId, mdlId), Value(NCConversationDescriptor(usrId, mdlId)))
                
                v.tstamp = U.nowUtcMs()
                
                v
            }.conv
        }
}
