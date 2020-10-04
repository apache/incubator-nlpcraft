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

import java.io.PrintStream
import java.util.Random

import NCAnsi._
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsiSpinner.RND

/**
 *
 */
class NCAnsiSpinner(out: PrintStream = System.out, ansiColor: String = ansiCyanFg, useAnsi: Boolean = true) {
    @volatile var thread: Thread = _

    final val SPIN_CHAR_SETS = Seq(
//        Seq('-', '\\', '|', '/'),
//        Seq('.', 'o', 'O', '@', '*'),
//        Seq('←', '↖', '↑', '↗', '→', '↘', '↓', '↙'),
//        Seq('▁', '▂', '▃', '▄', '▅', '▆', '▇', '█', '▇', '▆', '▅', '▄', '▃', '▁'),
//        Seq('▖', '▘', '▝', '▗'),
//        Seq('┤', '┘', '┴', '└', '├', '┌', '┬', '┐'),
        Seq('\u25E2', '\u25E3', '\u25E4', '\u25E5')
//        Seq('◰', '◳', '◲', '◱'),
//        Seq('◴', '◷', '◶', '◵'),
//        Seq('◐', '◓', '◑', '◒'),
//        Seq('◡', '⊙', '◠', '⊙'),
//        Seq('⣾', '⣽', '⣻', '⢿', '⡿', '⣟', '⣯', '⣷'),
//        Seq('⠁', '⠂', '⠄', '⡀', '⢀', '⠠', '⠐', '⠈')
    )

    private val SPIN_CHARS = SPIN_CHAR_SETS(RND.nextInt(SPIN_CHAR_SETS.size))
    private var rightPrompt = ""
    private var leftPrompt = ""
    private var lastLength = 0
    private var frame = 0

    /**
     *
     * @param p
     */
    def setRightPrompt(p: String): Unit =
        this.rightPrompt = if (p == null) "" else p

    /**
     *
     * @param p
     */
    def setLeftPrompt(p: String): Unit =
        this.leftPrompt = if (p == null) "" else p

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
                out.print(ansiCursorHide)

                while (!t.isInterrupted) {
//                    if (frame > 0)
//                        clean()

                    out.print(s"$leftPrompt$ansiColor${SPIN_CHARS(frame % SPIN_CHARS.size)}$ansiReset$rightPrompt")

                    lastLength = U.stripAnsi(leftPrompt).length + 1 + U.stripAnsi(rightPrompt).length

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
            out.print(ansiCursorShow)
        }
    }
}

object NCAnsiSpinner {
    private val RND = new Random()
}
