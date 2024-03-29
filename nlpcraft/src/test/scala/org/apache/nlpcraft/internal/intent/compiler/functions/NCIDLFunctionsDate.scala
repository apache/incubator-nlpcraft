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

package org.apache.nlpcraft.internal.intent.compiler.functions

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.intent.compiler.functions.NCIDLFunctions.*
import org.apache.nlpcraft.internal.util.NCUtils

import java.time.*
import java.time.temporal.IsoFields
import java.util.Calendar as C
import scala.language.implicitConversions

/**
  * Tests for 'dates' functions.
  */
class NCIDLFunctionsDate extends NCIDLFunctions:
    test("test") {
        def test0(): Unit =
            val d = LocalDate.now
            val t = LocalTime.now
            val c = C.getInstance()

            test(
                s"year() - ${d.getYear} == 0",
                s"month() - ${d.getMonthValue} == 0",
                s"day_of_month() - ${d.getDayOfMonth} == 0",
                s"day_of_week() - ${d.getDayOfWeek.getValue} == 0",
                s"day_of_year() - ${d.getDayOfYear} == 0",
                s"hour() - ${t.getHour} == 0",
                s"minute() - ${t.getMinute} == 0",
                s"second() - ${t.getSecond} < 5",
                s"week_of_month() - ${c.get(C.WEEK_OF_MONTH)} == 0",
                s"week_of_year() - ${c.get(C.WEEK_OF_YEAR)} == 0",
                s"quarter() - ${d.get(IsoFields.QUARTER_OF_YEAR)} == 0",
                s"now() - ${NCUtils.now()} < 5000"
            )

        try test0()
        // Some field more than `second` can be changed. One more attempt.
        catch case _: AssertionError => test0()
    }