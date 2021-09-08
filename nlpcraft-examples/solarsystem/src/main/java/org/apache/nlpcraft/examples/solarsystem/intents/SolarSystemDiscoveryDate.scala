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

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.examples.solarsystem.api.SolarSystemOpenApiService
import org.apache.nlpcraft.model.{NCIntent, NCIntentSample, NCIntentTerm, NCResult, NCToken}

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.time.temporal.ChronoField.{DAY_OF_MONTH, MONTH_OF_YEAR}
import java.time.{LocalDate, ZoneOffset}

class SolarSystemDiscoveryDate extends LazyLogging {
    @NCIntentSample(
        Array(
            "After 1900 year",
            "Before 1900 year",
            "Between 1800 and 1900 years"
        )
    )
    @NCIntent(
        "intent=discoveryDate " +
            "    options={'unused_usr_toks': true}" +
            "    term(year)={" +
            "        tok_id() == 'nlpcraft:num' && " +
            "        (" +
            "            meta_tok('nlpcraft:num:unit') == 'year' || " +
            "            meta_tok('nlpcraft:num:from') >= 1610 && meta_tok('nlpcraft:num:from') <= year" +
            "         )" +
            "    }"
    )
    def date(@NCIntentTerm("year") year: NCToken): NCResult = {
        // API doesn't support filter by dates.
        // We do it here.
        var res = SolarSystemOpenApiService.getInstance().bodyRequest().execute()

        val supportedFmts =
            Seq(
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

        val fromYear: Double = year.metax("nlpcraft:num:from")
        val toYear: Double = year.metax("nlpcraft:num:to")

        res = res.filter(row => {
            val dateStr = row("discoveryDate").asInstanceOf[String]

            // TODO: fix condition.
            if (dateStr.nonEmpty)
                supportedFmts.flatMap(p =>
                    try {
                        val years = LocalDate.parse(dateStr, p).atStartOfDay(ZoneOffset.UTC).getYear

                        Some(years >= fromYear && years <= toYear)
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

        logger.info(s"Request result filtered with years range ${fromYear.toLong}-${toYear.toLong}, rows=${res.size}")

        NCResult.text(res.toString())
    }
}