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
class NCAnsiSpinner(out: PrintStream = System.out, ansiColor: String = ansiCyanFg) {
    @volatile var thread: Thread = _

    final val IS_ANSI = NCAnsi.isEnabled

    final val SPIN_CHARS = Seq('-', '\\', '|', '/')
    final val SPIN_CHARS_SIZE = SPIN_CHARS.size

    /**
     *
     */
    def start(): Unit = {
        thread =  U.mkThread("ansi-spinner") { t ⇒
            var i = 0

            if (IS_ANSI)
                out.print(s"$ansiCursorHide")

            while (!t.isInterrupted) {
                if (IS_ANSI) {
                    if (i > 0)
                        out.print(s"$ansiCursorLeft$ansiClearLineAfter")

                    out.print(s"$ansiColor${SPIN_CHARS(i % SPIN_CHARS_SIZE)}$ansiReset")

                    i += 1
                }
                else
                    out.print(".")

                Thread.sleep(if (IS_ANSI) 200 else 1000)
            }
        }

        thread.start()
    }

    /**
     *
     */
    def stop(): Unit = {
        U.stopThread(thread)

        if (IS_ANSI)
            out.print(s"$ansiCursorLeft$ansiClearLineAfter$ansiCursorShow")
    }
}
