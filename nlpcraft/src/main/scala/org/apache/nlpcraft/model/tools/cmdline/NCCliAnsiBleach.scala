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

package org.apache.nlpcraft.model.tools.cmdline

import java.io.{BufferedInputStream, BufferedReader}

import org.apache.nlpcraft.common._
import org.jline.utils.InputStreamReader
import scala.util.Using

/**
 * Pipe filter to remove ANSI escape sequences.
 * Used by 'start-server' command of NLPCraft CLI.
 */
object NCCliAnsiBleach extends App {
    Using.resource(
        new BufferedReader(
            new InputStreamReader(
                new BufferedInputStream(System.in)
            )
        )
    ) { in =>
        var line = in.readLine()

        while (line != null) {
            System.out.println(U.stripAnsi(line))

            line = in.readLine()
        }
    }

    // NOTE: IO exceptions are thrown and they will stop the app (by design).

    System.exit(0)
}
