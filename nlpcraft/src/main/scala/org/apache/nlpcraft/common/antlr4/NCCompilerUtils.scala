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

package org.apache.nlpcraft.common.antlr4

import org.apache.nlpcraft.common.*
import org.apache.nlpcraft.common.ansi.NCAnsi.*

case class CompilerErrorHolder(
    ptrStr: String,
    origStr: String
)

object NCCompilerUtils:
    /**
      *
      * @param in
      * @param charPos
      * @return
      */
    def mkErrorHolder(in: String, charPos: Int): CompilerErrorHolder =
        val charPos0 = charPos - (in.length - in.stripLeading().length)
        val in0 = in.strip()
        val pos = Math.max(0, charPos0)
        val dash = "-" * in0.length
        var ptrStr = dash.substring(0, pos) + r("^")

        if pos < dash.length - 1 then
            ptrStr = ptrStr + y("~") + y(dash.substring(pos + 2))
        else
            ptrStr = ptrStr + y(dash.substring(pos + 1))

        val origStr = in0.substring(0, pos) + r(in0.charAt(pos)) + y(in0.substring(pos + 1))

        CompilerErrorHolder(ptrStr, origStr)
