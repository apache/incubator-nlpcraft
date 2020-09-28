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

import NCAnsi._
import org.apache.nlpcraft.common._

/**
 *
 */
class NCAnsiSpinner(out: PrintStream = System.out, ansiColor: String = ansiCyanFg, useAnsi: Boolean = true) {
    @volatile var thread: Thread = _

    final val SPIN_CHARS = Seq('-', '\\', '|', '/')
    final val SPIN_CHARS_SIZE = SPIN_CHARS.size

    var frame = 0

    /**
     *
     */
    def start(): Unit =
        if (useAnsi) {
            thread =  U.mkThread("ansi-spinner") { t ⇒
                out.print(s"$ansiCursorHide")

                while (!t.isInterrupted) {
                    if (frame > 0)
                        out.print(s"$ansiCursorLeft$ansiClearLineAfter")

                    out.print(s"$ansiColor${SPIN_CHARS(frame % SPIN_CHARS_SIZE)}$ansiReset")

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

        if (useAnsi && frame > 0)
            out.print(s"$ansiCursorLeft$ansiClearLineAfter$ansiCursorShow")
    }
}
