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

package org.apache.nlpcraft.examples.solarsystem.intents

import org.apache.nlpcraft.model.{NCIntent, NCIntentSample, NCIntentTerm, NCResult, NCToken}

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.time.temporal.ChronoField.{DAY_OF_MONTH, MONTH_OF_YEAR}
import java.time.{LocalDate, ZoneOffset}

class SolarSystemDate extends SolarSystemIntentsAdapter {
    @NCIntentSample(
        Array(
            "After 1900 year",
            "After 1900 year",
        )
    )
    @NCIntent(
        "intent=date " +
            "    options={" +
            "        'unused_usr_toks': true " +
            "    }" +
            "    term(date)={tok_id() == 'nlpcraft:date'} "
    )
    def date(@NCIntentTerm("date") date: NCToken): NCResult = {
        // API doesn't support filter by dates.
        // We do it here.
        var res = api.bodyRequest().execute()

        val supportedFmts =
            Seq (
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                new DateTimeFormatterBuilder().
                    appendPattern("yyyy").
                    parseDefaulting(MONTH_OF_YEAR, 1).
                    parseDefaulting(DAY_OF_MONTH, 1).
                    toFormatter(),
                new DateTimeFormatterBuilder().
                    appendPattern("??/MM/yyyy").
                    parseDefaulting(DAY_OF_MONTH, 1).
                    toFormatter()
            )

        val from: Long = date.metax("nlpcraft:date:from")
        val to: Long = date.metax("nlpcraft:date:to")

        res = res.filter(row => {
            val dateStr = row("discoveryDate").asInstanceOf[String]

            if (dateStr.nonEmpty)
                supportedFmts.flatMap(p =>
                    try {
                        val ms = LocalDate.parse(dateStr, p).atStartOfDay(ZoneOffset.UTC).toInstant.toEpochMilli

                        Some(ms >= from && ms <= to)
                    }
                    catch {
                        case _: DateTimeParseException => None
                    }
                ).
                    to(LazyList).
                    headOption.
                    getOrElse(throw new AssertionError(s"Template not found for: $dateStr"))
            else
                false
        })

        NCResult.text(res.toString())
    }
}