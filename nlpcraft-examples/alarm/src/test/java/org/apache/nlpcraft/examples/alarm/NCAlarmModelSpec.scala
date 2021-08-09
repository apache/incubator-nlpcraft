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

package org.apache.nlpcraft.examples.alarm

import org.apache.nlpcraft.examples.alarm.AlarmModel.calculateTime
import org.apache.nlpcraft.model.{NCIntentRef, NCIntentTerm, NCResult, NCToken}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MILLIS
import scala.jdk.CollectionConverters.SeqHasAsJava

/**
 * Model for alarm spec.
 */
class AlarmModelWrapper extends AlarmModel {
    @NCIntentRef("alarm")
    def onMatch(@NCIntentTerm("nums") numToks: List[NCToken]): NCResult =
        NCResult.text(String.valueOf(calculateTime(numToks.asJava)))
}

/**
 * Unit tests that checks that alarm model produces correct time calculations.
 */
@NCTestEnvironment(model = classOf[AlarmModelWrapper], startClient = true)
class NCAlarmModelSpec extends NCTestContext {
    // Checks with 1 second precision. Enough to be sure that calculation result is correct.
    private def check(req: String, expectedTime: Long): Unit =
        checkResult(req, _.toLong, (calcTime: Long) => Math.abs(expectedTime - calcTime) <= 1000)

    @Test
    def test(): Unit = {
        val now = LocalDateTime.now

        /**
         *
         * @param hours
         * @param mins
         * @param secs
         * @return
         */
        def mkPeriod(hours: Int, mins: Int, secs: Int = 0): Long =
            now.until(now.plusHours(hours).plusMinutes(mins).plusSeconds(secs), MILLIS)

        // Fuzzy `single`.
        check("Buzz me in a minute or two.", mkPeriod(0, 1))
        check("Buzz me in hour or two.", mkPeriod(1, 0))
        check("Buzz me in an hour.", mkPeriod(1, 0))

        // Fuzzy `few`.
        check("Buzz me in few minutes.", mkPeriod(0, 2))
        check("Buzz me in one or two minutes.", mkPeriod(0, 2))
        check("Buzz me in one or two hours.", mkPeriod(2, 0))
        check("Buzz me in a couple of minutes.", mkPeriod(0, 2))

        // Fuzzy `bit`.
        check("Buzz me in a bit.", mkPeriod(0, 2))

        // Complex periods.
        check("Buzz me in an hour and 15mins", mkPeriod(1, 15))
        check("Buzz me in one hour and 15mins", mkPeriod(1, 15))
        check("Buzz me in 1 hour and 15mins", mkPeriod(1, 15))
        check("Buzz me in 1h and 15mins", mkPeriod(1, 15))

        check("Buzz me in one day, 1h and 15mins", mkPeriod(25, 15))
        check("Buzz me in a day, 1h and 15mins", mkPeriod(25, 15))
    }
}
