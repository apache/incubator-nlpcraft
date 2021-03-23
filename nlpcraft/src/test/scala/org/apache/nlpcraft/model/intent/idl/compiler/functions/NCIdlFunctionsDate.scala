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
        def test0(): Unit =
            test(
                TrueFunc(truth = s"year() - ${LocalDate.now.getYear} == 0"),
                TrueFunc(truth = s"month() - ${LocalDate.now.getMonthValue} == 0"),
                TrueFunc(truth = s"day_of_month() - ${LocalDate.now.getDayOfMonth} == 0"),
                TrueFunc(truth = s"day_of_week() - ${LocalDate.now.getDayOfWeek.getValue} == 0"),
                TrueFunc(truth = s"day_of_year() - ${LocalDate.now.getDayOfYear} == 0"),
                TrueFunc(truth = s"hour() - ${LocalTime.now.getHour} == 0"),
                TrueFunc(truth = s"minute() - ${LocalTime.now.getMinute} == 0"),
                TrueFunc(truth = s"second() - ${LocalTime.now.getSecond} < 5"),
                TrueFunc(truth = s"week_of_month() - ${Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)} == 0"),
                TrueFunc(truth = s"week_of_year() - ${Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)} == 0"),
                TrueFunc(truth = s"quarter() - ${LocalDate.now().get(IsoFields.QUARTER_OF_YEAR)} == 0"),
                TrueFunc(truth = s"now() - ${System.currentTimeMillis()} < 5000")
            )

        try
            test0()
        catch {
            case _: AssertionError ⇒
                // Some field more than `second` can be changed. One more attempt.
                test0()
        }
    }
}
