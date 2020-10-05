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

package org.apache.nlpcraft.common.ansi

import java.io.PrintWriter

import NCAnsi._
import org.apache.nlpcraft.common.ansi.NCAnsiProgressBar._

/**
 * Forward-only, bound ANSI-based progress bar.
 *
 * @param out
 * @param totalTicks Number of ticks to complete.
 * @param dispSize Visual size of the progress bar.
 * @param clearOnComplete
 * @param useAnsi
 */
class NCAnsiProgressBar(
    out: PrintWriter,
    totalTicks: Int,
    dispSize: Int,
    clearOnComplete: Boolean = true,
    useAnsi: Boolean = true) {
    require(dispSize <= totalTicks)

    @volatile private var tick = 0

    //noinspection ZeroIndexToHead
    private final val PB_LEFT = s"$ansiBlueFg${CHAR_SET(0)}$ansiReset"
    private final val PB_RIGHT = s"$ansiBlueFg${CHAR_SET(3)}$ansiReset"
    private final val PB_EMPTY =s"$ansiWhiteFg${CHAR_SET(2)}$ansiReset"
    private final val PB_FULL = s"$ansiRedFg$ansiBold${CHAR_SET(1)}$ansiReset"

    /**
     *
     */
    private def clean(): Unit = {
        out.print(ansiCursorLeft * (dispSize + 2))
        out.print(ansiClearLineAfter)
        out.flush()
    }

    /**
     * Starts progress bar.
     */
    def start(): Unit = {
        tick = 0

        if (useAnsi) {
            // Hide cursor to avoid blinking.
            out.print(ansiCursorHide)

            out.print(PB_LEFT)
            out.print(PB_EMPTY * dispSize)
            out.print(PB_RIGHT)

            out.flush()
        }
    }

    /**
     * Ticks progress bar one tick at a time.
     */
    def ticked(): Unit = {
        tick += 1

        if (useAnsi) {
            clean()

            val bar = Math.round((tick.toFloat / totalTicks.toFloat) * dispSize)

            out.print(PB_LEFT)
            for (i ← 0 until dispSize)
                out.print(if (i < bar) PB_FULL else PB_EMPTY)
            out.print(PB_RIGHT)
            out.flush()
        }
        else if (tick % (totalTicks / dispSize) == 0) {
            out.print(NON_ANSI_CHAR)
            out.flush()
        }
    }

    /**
     * Whether progress is complete.
     *
     * @return
     */
    def completed: Boolean =
        tick == totalTicks

    /**
     * Stops progress bar.
     */
    def stop(): Unit = {
        if (useAnsi && clearOnComplete) {
            clean()

            // Show cursor.
            out.print(ansiCursorShow)
            out.flush()
        }
    }
}

/**
 *
 */
object NCAnsiProgressBar{
    // Set of UNICODE charsets options for the progress bar.
    private final val PB_CHAR_SETS = Seq(
        Seq('[', '=', '.', ']'),
        Seq('/', '▰', '▱', '/'),
        Seq('[', '▰', '▱', ']'),
        Seq('[', '◼', '◽', ']'),
        Seq('[', '█', '_', ']'),
        Seq('⟮', '▰', '.', '⟯')
    )

    private final val NON_ANSI_CHAR = '='

    // Active charset to use.
    private val CHAR_SET = PB_CHAR_SETS(5)
}
