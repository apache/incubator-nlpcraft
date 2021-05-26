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

package org.apache.nlpcraft.common.ascii

import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.junit.jupiter.api.Test

/**
 * Test for ASCII text table.
 */
class NCAsciiTableSpec {
    @Test
    def test() {
        val t = NCAsciiTable()
        
        t.defaultHeaderStyle = "leftPad: 10, rightPad: 5"

        t.margin(5, 5, 5, 5)

        t.defaultHeaderStyle = s"${t.defaultHeaderStyle}, maxWidth: 10"
        t.defaultRowStyle = s"${t.defaultRowStyle}, maxWidth: 10"

        t #= ("Header 1", Seq("Header 2.1", "Header 2.2"), "Header 3")
        t += ("Row 1", Seq("Row 2"), Seq("Row 3.1", "Row 3.2"))
        t += ("1234567890zxcvbnmasdASDFGHJKLQ", Seq("Row 2"), Seq("Row 3.1", "Row 3.2"))
        t += (Seq(s"${ansiRedFg}Row 31.1$ansiReset", s"${ansiYellowFg}Row 31.2$ansiReset"), "Row 11", "Row 21")

        t.render()
    }

    @Test
    def testSmallTable(): Unit = {
        val t = NCAsciiTable()

        t.margin(top = 1, bottom = 1)

        t += s"Small table with ANSI ${ansiBlueFg}code$ansiReset"

        t.render()
    }

    @Test
    def testWithSequenceHeader() {
        val t = NCAsciiTable()
        
        t.defaultHeaderStyle = "leftPad: 10, rightPad: 5"

        t.margin(5, 5, 5, 5)

        t.defaultHeaderStyle = s"${t.defaultHeaderStyle}, maxWidth: 10"
        t.defaultRowStyle = s"${t.defaultRowStyle}, maxWidth: 10"

        t #= (Seq("Header 1", "Header 2", "Header 3", "Header 4"): _*)
        t += ("Column 1", "Column 2", "Column 3", "Column 4")

        t.render()
    }

    @Test
    def testWithVeryBigTable() {
        val NUM = 100

        val start = U.now()

        val t = NCAsciiTable()
        
        t.defaultHeaderStyle = "leftPad: 10, rightPad: 5"

        t #= (Seq("Header 1", "Header 2", "Header 3"): _*)

        for (i <- 0 to NUM)
            t += (s"${ansiGreenFg}Value 1:$i$ansiReset", s"${ansiReversed}Value 2:$i$ansiReset", s"${ansiRedFg}Value 3:$i$ansiReset")

        t.render()

        val dur = U.now() - start

        println(s"Rendered in ${dur}ms.")
    }
}
