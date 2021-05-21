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

import org.apache.nlpcraft.common._
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
    
    private final val mux = new Object()

    //noinspection ZeroIndexToHead
    private final val PB_LEFT = s"$B${CHAR_SET(0)}$RST"
    private final val PB_RIGHT = s"$B${CHAR_SET(3)}$RST"
    private final val PB_EMPTY =s"$W${CHAR_SET(2)}$RST"
    private final val PB_FULL = s"$R$BO${CHAR_SET(1)}$RST"

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

        if (useAnsi) mux.synchronized {
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
        mux.synchronized {
            tick += 1

            if (useAnsi) {
                clean()

                val bar = if (tick == 1) 1 else Math.round((tick.toFloat / totalTicks.toFloat) * dispSize)

                out.print(PB_LEFT)
                for (i <- 0 until dispSize)
                    out.print(if (i < bar) PB_FULL else PB_EMPTY)
                out.print(PB_RIGHT)
                out.flush()
            }
            else if (tick == 1 || tick % (totalTicks / dispSize) == 0) {
                out.print(NON_ANSI_CHAR)
                out.flush()
            }
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
        if (useAnsi && clearOnComplete) mux.synchronized {
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
    // Active charset to use.
    private final val NON_ANSI_CHAR = '='
    private val CHAR_SET = Seq('[', '=', '.', ']')
}
