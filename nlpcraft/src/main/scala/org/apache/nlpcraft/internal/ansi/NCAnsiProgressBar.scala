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

package org.apache.nlpcraft.internal.ansi

import java.io.PrintWriter
import org.apache.nlpcraft.internal.*
import NCAnsi.*
import org.apache.commons.lang3.StringUtils
import NCAnsiProgressBar.*

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
    useAnsi: Boolean = true):
    require(dispSize <= totalTicks)

    @volatile private var tick = 0

    private final val mux = new Object()
    private final val PB_LEFT = s"$B${CHAR_SET.head}$RST"
    private final val PB_RIGHT = s"$B${CHAR_SET(3)}$RST"
    private final val PB_EMPTY = s"$W${CHAR_SET(2)}$RST"
    private final val PB_FULL = s"$R$BO${CHAR_SET(1)}$RST"
    private final val PB_LEAD = s"$Y$BO${CHAR_SET(4)}$RST"

    /**
      *
      */
    private def clean(): Unit =
        out.print(ansiCursorLeft * (dispSize + 2/* Left & right brackets. */ + 5/* % string. */))
        out.print(ansiClearLineAfter)
        out.flush()

    /**
      * Starts progress bar.
      */
    def start(): Unit =
        tick = 0
        if useAnsi then
            mux.synchronized {
                // Hide cursor to avoid blinking.
                out.print(ansiCursorHide)
                out.print(PB_LEFT)
                out.print(PB_EMPTY * dispSize)
                out.print(PB_RIGHT)
                out.print(" ")
                out.print(s"${W}0%  $RST")
                out.flush()
            }

    /**
      * Ticks progress bar one tick at a time.
      */
    def ticked(): Unit =
        mux.synchronized {
            tick += 1

            if useAnsi then
                clean()
                val ratio = tick.toFloat / totalTicks.toFloat
                val bar = if tick == 1 then 1 else Math.round(ratio * dispSize)
                val pct = Math.round(ratio * 100)
                out.print(PB_LEFT)
                for (i <- 0 until dispSize)
                    if i < bar then out.print(PB_FULL)
                    else if i == bar then out.print(PB_LEAD)
                    else out.print(PB_EMPTY)

                out.print(PB_RIGHT)
                out.print(" ")
                out.print(W + StringUtils.rightPad(s"$pct%",4) + RST)
                out.flush()
            else if tick == 1 || tick % (totalTicks / dispSize) == 0 then
                out.print(NON_ANSI_CHAR)
                out.flush()
        }

    /**
      * Whether progress is complete.
      *
      * @return
      */
    def completed: Boolean = tick == totalTicks

    /**
      * Stops progress bar.
      */
    def stop(): Unit =
        if useAnsi && clearOnComplete then mux.synchronized {
            clean()

            // Show cursor.
            out.print(ansiCursorShow)
            out.flush()
        }

/**
  *
  */
object NCAnsiProgressBar:
    // Active charset to use.
    private final val NON_ANSI_CHAR = '='
    private val CHAR_SET = Seq('[', '=', '.', ']', '>')
