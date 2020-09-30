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
import org.apache.nlpcraft.common.ansi.NCAnsi._

import scala.compat.Platform._

/**
  * Basic abstract class defining internal service/manager/component lifecycle. Components that
  * extend this class are typically called 'managers'.
  */
abstract class NCService extends LazyLogging with NCOpenCensusTrace {
    @volatile private var timeStampMs = -1L

    @volatile private var started = false
    @volatile private var starting = false
    @volatile private var stopping = false
    
    private final val clsName = U.cleanClassName(getClass)
    
    def isStarted: Boolean = started && !stopping && !starting
    def isStopped: Boolean = !started && !stopping && !starting
    def isStarting: Boolean = starting
    def isStopping: Boolean = stopping

    /**
      * Starts this service.
      *
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def start(parent: Span = null): NCService =
        // Make sure this is not called by subclass.
        throw new AssertionError(s"NCService#start() should not be called directly in '$name' service.")

    /**
      * Stops this service.
      *
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def stop(parent: Span = null): Unit =
        // Make sure this is not called by subclass.
        throw new AssertionError(s"NCService#stop() should not be called directly in '$name' service.")

    /**
     * Gets name of this service (as its class name).
     *
     * @return Name of this service.
     */
    def name: String = clsName

    /**
     * Acks the beginning of this service startup.
     *
     * @return This instance.
     */
    protected def ackStarting(): NCService = {
        starting = true
        timeStampMs = currentTime

        logger.trace("$name staring...")

        addTags(currentSpan(),
            "state" → "starting"
        )

        this
    }

    /**
     * Acks the beginning of this service shutdown.
     *
     * @return This instance.
     */
    protected def ackStopping(): NCService = {
        stopping = true
        timeStampMs = currentTime

        logger.trace("$name stopping...")

        addTags(currentSpan(),
            "state" → "stopping"
        )

        this
    }

    /**
     * Acks started service. Should be called at the end of the `start()` method.
     */
    protected def ackStarted(): NCService = {
        assert(!started, s"Service '$name' is already started.")
        assert(timeStampMs != -1, "Method 'NCService#ackStarting()' wasn't called.")

        starting = false
        started = true

        addTags(
            currentSpan(),
            "startDurationMs" → (currentTime - timeStampMs),
            "state" → "started"
        )

        val dur = s"$ansiGreenFg[${currentTime - timeStampMs}ms]$ansiReset"

        logger.info(s"$name started $dur")

        timeStampMs = -1L

        this
    }

    /**
     * Acks stopped service. Should be called at the end of the `stop()` method.
     */
    protected def ackStopped(): Unit = {
        assert(timeStampMs != -1, "Method 'NCService#ackStopping()' wasn't called.")

        stopping = false
        started = false

        addTags(currentSpan(),
            "stopDurationMs" → (currentTime - timeStampMs),
            "state" → "stopped"
        )

        logger.info(s"$name stopped.")

        timeStampMs = -1L
    }
}
