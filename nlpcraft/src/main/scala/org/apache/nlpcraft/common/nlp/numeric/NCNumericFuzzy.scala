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

package org.apache.nlpcraft.common.nlp.numeric

import org.apache.nlpcraft.common.makro.NCMacroParser

case class NCNumericFuzzyPeriod(unit: NCNumericUnit, value: Int)

/**
  * Fuzzy numeric configuration.
  */
object NCNumericFuzzy {
    val NUMS: Map[String, NCNumericFuzzyPeriod] = {
        val parser = new NCMacroParser

        def make(txt: String, unit: NCNumericUnit, value: Int): Seq[(String, NCNumericFuzzyPeriod)] =
            parser.expand(txt).map(_ -> NCNumericFuzzyPeriod(unit, value))

        import org.apache.nlpcraft.common.nlp.numeric.{NCNumericUnit => U}

        val singleDt =
            Map(
                "in {a|an|_} second {or two|_}" -> U("second", "datetime"),
                "in {a|an|_} hour {or two|_}" -> U("hour", "datetime"),
                "in {a|an|_} minute {or two|_}" -> U("minute", "datetime"),
                "in {a|an|_} day {or two|_}" -> U("day", "datetime"),
                "in {a|an|_} week {or two|_}" -> U("week", "datetime"),
                "in {a|an|_} month {or two|_}" -> U("month", "datetime"),
                "in {a|an|_} year {or two|_}" -> U("year", "datetime")
            ).flatMap { case (txt, u) => make(txt, u, 1) }

        val bitDt =
            Map(
                "in {a|an} bit" -> U("minute", "datetime")
            ).flatMap { case (txt, u) => make(txt, u, 2) }

        val fewDt =
            Map(
                "in {a|an|_} {few|couple of|one or two|two or three} seconds" -> U("second", "datetime"),
                "in {a|an|_} {few|couple of|one or two|two or three} minutes" -> U("minute", "datetime"),
                "in {a|an|_} {few|couple of|one or two|two or three} hours" -> U("hour", "datetime"),
                "in {a|an|_} {few|couple of|one or two|two or three} days" -> U("day", "datetime"),
                "in {a|an|_} {few|couple of|one or two|two or three} weeks" -> U("week", "datetime"),
                "in {a|an|_} {few|couple of|one or two|two or three} months" -> U("month", "datetime"),
                "in {a|an|_} {few|couple of|one or two|two or three} years" -> U("year", "datetime")
            ).flatMap { case (txt, u) => make(txt, u, 2) }

        singleDt ++ bitDt ++ fewDt
    }
}
