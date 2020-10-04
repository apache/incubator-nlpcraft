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
import java.util.Random

import NCAnsi._
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsiSpinner.RND

/**
 *
 */
class NCAnsiSpinner(out: PrintWriter, ansiColor: String = ansiCyanFg, useAnsi: Boolean = true) {
    @volatile var thread: Thread = _

    final val SPIN_CHAR_SETS = Seq(
        Seq('-', '\\', '|', '/'),
        Seq('.', 'o', 'O', '@', '*'),
        Seq('←', '↖', '↑', '↗', '→', '↘', '↓', '↙'),
        Seq('▁', '▂', '▃', '▄', '▅', '▆', '▇', '█', '▇', '▆', '▅', '▄', '▃', '▁'),
        Seq('▖', '▘', '▝', '▗'),
        Seq('┤', '┘', '┴', '└', '├', '┌', '┬', '┐'),
        Seq('◢', '◣', '◤', '◥'),
        Seq('◰', '◳', '◲', '◱'),
        Seq('◴', '◷', '◶', '◵'),
        Seq('◐', '◓', '◑', '◒'),
        Seq('◡', '⊙', '◠', '⊙'),
        Seq('⣾', '⣽', '⣻', '⢿', '⡿', '⣟', '⣯', '⣷'),
        Seq('⠁', '⠂', '⠄', '⡀', '⢀', '⠠', '⠐', '⠈')
    )

    private var suffix = ""
    private var prefix = ""
    private var lastLength = 0
    private var frame = 0

    /**
     *
     * @param p
     */
    def setSuffix(p: String): Unit =
        this.suffix = if (p == null) "" else p

    /**
     *
     * @param p
     */
    def setPrefix(p: String): Unit =
        this.prefix = if (p == null) "" else p

    /**
     *
     */
    private def clean(): Unit = {
        out.print(ansiCursorLeft * lastLength)
        out.print(ansiClearLineAfter)
    }

    /**
     *
     */
    def start(): Unit =
        if (useAnsi) {
            thread =  U.mkThread("ansi-spinner") { t ⇒
                frame = 0
                lastLength = 0

                val chars = SPIN_CHAR_SETS(RND.nextInt(SPIN_CHAR_SETS.size))

                // Hide cursor to avoid blinking.
                out.print(ansiCursorHide)

                while (!t.isInterrupted) {
                    if (frame > 0)
                        clean()

                    out.print(s"$prefix$ansiColor${chars(frame % chars.size)}$ansiReset$suffix")

                    lastLength = U.stripAnsi(prefix).length + 1 + U.stripAnsi(suffix).length

                    frame += 1

                    Thread.sleep(200)
                }
            }

            thread.start()
        }
        else
            out.print("... ")

    /**
     *
     */
    def stop(): Unit = {
        U.stopThread(thread)

        if (useAnsi && frame > 0) {
            clean()

            // Show cursor.
            out.print(ansiCursorShow)
        }
    }
}

object NCAnsiSpinner {
    private val RND = new Random()
}
