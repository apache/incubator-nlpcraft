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

package org.apache.nlpcraft.common

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.common.ansi.NCAnsiColor._

import scala.compat.Platform._

/**
  * Basic abstract class defining internal service/manager/component lifecycle. Components that
  * extend this class are typically called 'managers'.
  */
abstract class NCService extends LazyLogging with NCOpenCensusTrace {
    private val startMs = currentTime

    @volatile private var started = false
    
    private final val clsName = U.cleanClassName(getClass)
    
    /**
      * Checks if this service is started.
      */
    def isStarted: Boolean = started

    /**
      * Starts this service.
      *
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def start(parent: Span = null): NCService =
        // Make sure this is not called by subclass.
        throw new AssertionError(s"NCService#start() should not be called directly in '${U.cleanClassName(getClass)}' service.")

    /**
      * Stops this service.
      *
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def stop(parent: Span = null): Unit =
        // Make sure this is not called by subclass.
        throw new AssertionError(s"NCService#stop() should not be called directly in '${U.cleanClassName(getClass)}' service.")

    /**
     * Gets name of this service (as its class name).
     *
     * @return Name of this service.
     */
    def name: String = clsName

    /**
     * Acks started service. Should be called at the end of the `start()` method.
     */
    protected def ackStart(): NCService = {
        require(!started)

        started = true

        val dur = s"$ansiBlueFg[${currentTime - startMs}ms]$ansiReset"

        addTags(
            currentSpan(),
            "startDurationMs" → (currentTime - startMs), "state" → started
        )

        logger.info(s"$clsName started $dur")

        this
    }

    /**
     * Acks stopped service. Should be called at the end of the `stop()` method.
     */
    protected def ackStop(): Unit = {
        started = false

        addTags(currentSpan(),
            "state" → started
        )

        logger.info(s"$clsName stopped.")
    }
}
