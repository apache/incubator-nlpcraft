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

package org.apache.nlpcraft.internal.antlr4

import org.apache.nlpcraft.internal.*

object NCCompilerUtils:
    /**
      *
      * @param in
      * @param charPos
      */
    def mkErrorHolder(in: String, charPos: Int): NCCompilerErrorHolder =
        val in0 = in.strip()
        if in0.isEmpty || charPos < 0 then NCCompilerErrorHolder("<empty>", "<empty>")
        else
            val charPos0 = charPos - (in.length - in.stripLeading().length)
            val len = in0.length
            val pos = Math.min(Math.max(0, charPos0), len)

            if pos == len then
                NCCompilerErrorHolder(s"${"-" * len}^", in0)
            else
                val dash = "-" * len
                val ptrStr = s"${dash.substring(0, pos)}^${dash.substring(pos + 1)}"
                NCCompilerErrorHolder(ptrStr, in0)
