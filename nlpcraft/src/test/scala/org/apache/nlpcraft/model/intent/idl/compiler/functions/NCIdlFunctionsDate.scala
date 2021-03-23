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

package org.apache.nlpcraft.model.intent.idl.compiler.functions

import org.junit.jupiter.api.Test

import java.time.temporal.IsoFields
import java.time.{LocalDate, LocalTime}
import java.util.Calendar

/**
  * Tests for 'dates' functions.
  */
class NCIdlFunctionsDate extends NCIdlFunctions {
    @Test
    def test(): Unit = {
        def test0(): Unit = {
            val d = LocalDate.now
            val t = LocalTime.now
            val c = Calendar.getInstance()

            test(
                TestData(truth = s"year() - ${d.getYear} == 0"),
                TestData(truth = s"month() - ${d.getMonthValue} == 0"),
                TestData(truth = s"day_of_month() - ${d.getDayOfMonth} == 0"),
                TestData(truth = s"day_of_week() - ${d.getDayOfWeek.getValue} == 0"),
                TestData(truth = s"day_of_year() - ${d.getDayOfYear} == 0"),
                TestData(truth = s"hour() - ${t.getHour} == 0"),
                TestData(truth = s"minute() - ${t.getMinute} == 0"),
                TestData(truth = s"second() - ${t.getSecond} < 5"),
                TestData(truth = s"week_of_month() - ${c.get(Calendar.WEEK_OF_MONTH)} == 0"),
                TestData(truth = s"week_of_year() - ${c.get(Calendar.WEEK_OF_YEAR)} == 0"),
                TestData(truth = s"quarter() - ${d.get(IsoFields.QUARTER_OF_YEAR)} == 0"),
                TestData(truth = s"now() - ${System.currentTimeMillis()} < 5000")
            )
        }

        try
            test0()
        catch {
            case _: AssertionError ⇒
                // Some field more than `second` can be changed. One more attempt.
                test0()
        }
    }
}
