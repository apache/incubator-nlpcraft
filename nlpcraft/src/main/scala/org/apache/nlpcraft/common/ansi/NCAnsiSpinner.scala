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
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsiSpinner._

/**
 * ANSI-based hourglass spinner.
 *
 * @param out
 * @param useAnsi
 */
class NCAnsiSpinner(out: PrintWriter, useAnsi: Boolean = true) {
    @volatile private var thread: Thread = _
    @volatile private var suffix = ""
    @volatile private var prefix = ""
    @volatile private var lastLength = 0
    @volatile private var frame = 0

    private final val mux = new Object()

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
        out.flush()
    }

    /**
     * Starts spinner.
     */
    def start(): Unit =
        if (useAnsi) {
            thread =  U.mkThread("ansi-spinner") { t =>
                frame = 0
                lastLength = 0

                // Hide cursor to avoid blinking.
                out.print(ansiCursorHide)
                out.flush()

                while (!t.isInterrupted) {
                    mux.synchronized {
                        if (frame > 0)
                            clean()

                        out.print(s"$prefix$ansiCyanFg${CHAR_SET(frame % CHAR_SET.size)}$ansiReset$suffix")
                        out.flush()
                    }

                    lastLength = U.stripAnsi(prefix).length + 1 + U.stripAnsi(suffix).length

                    frame += 1

                    Thread.sleep(CHAR_SET.size.fps) // Full rotation per second.
                }
            }

            thread.start()
        }
        else mux.synchronized {
            out.print("... ")
            out.flush()
        }

    /**
     * Stops spinner.
     */
    def stop(): Unit = {
        U.stopThread(thread)
        
        if (useAnsi && frame > 0) mux.synchronized {
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
object NCAnsiSpinner {
    // An active charset to use.
    private final val CHAR_SET = Seq('-', '\\', '|', '/')
}
