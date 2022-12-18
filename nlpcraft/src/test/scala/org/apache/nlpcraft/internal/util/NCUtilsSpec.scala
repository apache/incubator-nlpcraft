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

package org.apache.nlpcraft.internal.util

import org.scalatest.funsuite.AnyFunSuite
import org.apache.nlpcraft.internal.util.NCUtils as U

import java.io.*

/**
  *
  */
class NCUtilsSpec extends AnyFunSuite:
    test("test sources") {
        val res = "moby/354984si.ngl"
        val file = NCResourceReader.get("badfilter/swear_words.txt")
        val is = new FileInputStream(file)

        require(U.readLines(res).toList.nonEmpty)
        require(U.readLines(file).toList.nonEmpty)
        require(U.readLines(is).toList.nonEmpty)
    }

    test("test parameters") {
        val data = Seq(
            "#",
            "",
            " AA",
            "a"
        )

        val arr = data.mkString("\n").getBytes

        var lines = U.readLines(new ByteArrayInputStream(arr), strip = false).toList

        require(lines.length == 4)
        require(lines.exists(_.contains(" AA")))

        lines = U.readLines(new ByteArrayInputStream(arr), convert = _.toLowerCase, filterText = true).toList

        require(lines.length == 2)
        require(!lines.exists(_.contains(" AA")))
        require(lines.exists(_.contains("aa")))
    }