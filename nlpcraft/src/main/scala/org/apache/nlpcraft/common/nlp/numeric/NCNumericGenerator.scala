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

package org.apache.nlpcraft.common.nlp.numeric

import java.text.DecimalFormat

/**
 * Java source http://www.rgagnon.com/javadetails/java-0426.html
 */
object NCNumericGenerator {
    private final val FORMATTER = new DecimalFormat("000000000000")

    private final val TENS_NAMES = IndexedSeq(
        "",
        " ten",
        " twenty",
        " thirty",
        " forty",
        " fifty",
        " sixty",
        " seventy",
        " eighty",
        " ninety")

    private final val NUM_NAMES = IndexedSeq(
        "",
        " one",
        " two",
        " three",
        " four",
        " five",
        " six",
        " seven",
        " eight",
        " nine",
        " ten",
        " eleven",
        " twelve",
        " thirteen",
        " fourteen",
        " fifteen",
        " sixteen",
        " seventeen",
        " eighteen",
        " nineteen"
    )

    // Less 1000.
    private def convertSmall(n: Int): String = {
        val (s, i) =
            if (n % 100 < 20)
                (NUM_NAMES(n % 100), n / 100)
            else {
                val s1 = NUM_NAMES(n % 10)
                val i1 = n / 10

                (TENS_NAMES(i1 % 10) + s1, i1 / 10)
            }

        if (i == 0) s else s"${NUM_NAMES(i)} hundred $s"
    }

    def toWords(n: Long): String = {
        // 0 to 999 999 999 999
        if (n == 0)
            "zero"
        else {
            val sNum = FORMATTER.format(n)

            val s10x9 = Integer.parseInt(sNum.substring(0, 3))
            val s10x6 = Integer.parseInt(sNum.substring(3, 6))
            val s10x3 = Integer.parseInt(sNum.substring(6, 9))
            val s10x1 = Integer.parseInt(sNum.substring(9, 12))

            val n10x9 = s10x9 match {
                case 0 ⇒ ""
                case _ ⇒ convertSmall(s10x9) + " billion "
            }

            val n10x6 = s10x6 match {
                case 0 ⇒ ""
                case _ ⇒ convertSmall(s10x6) + " million "
            }

            val n10x3 = s10x3 match {
                case 0 ⇒ ""
                case 1 ⇒ "one thousand "
                case _ ⇒ convertSmall(s10x3) + " thousand "
            }

            val n10x1 = convertSmall(s10x1)

            (n10x9 + n10x6 + n10x3 + n10x1).split(" ").filter(!_.isEmpty).mkString(" ")
        }
    }

    def generate(n: Int): Map[Int, String] = (1 to n).map(i ⇒ i → toWords(i)).toMap
}