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

package org.apache.nlpcraft.examples.solarsystem

import com.typesafe.scalalogging.LazyLogging
import org.apache.nlpcraft.examples.solarsystem.tools.SolarSystemOpenApiService
import org.apache.nlpcraft.model.{NCIntent, NCIntentSample, NCIntentTerm, NCModelFileAdapter, NCResult, NCToken}

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException}
import java.time.temporal.ChronoField._
import java.time.{LocalDate, ZoneOffset}

class SolarSystemModel extends NCModelFileAdapter("solarsystem_model.yaml") with LazyLogging {
    private var api: SolarSystemOpenApiService = _

    override def onInit(): Unit = {
        api = SolarSystemOpenApiService.getInstance()

        logger.info("Solar System Model started.")
    }

    override def onDiscard(): Unit = {
        if (api != null)
            api.stop()

        logger.info("Solar System Model stopped.")
    }

    @NCIntentSample(
        Array(
            "Moon!",
            "give me information about Larissa",
        )
    )
    @NCIntent(
        "intent=planetInfo " +
        "    options={" +
        "        'unused_usr_toks': false " +
        "    }" +
        "    term(planet)={tok_id() == 'planet'}"
    )
    def planetInfo(@NCIntentTerm("planet") planet: NCToken): NCResult =
        NCResult.text(api.bodyRequest().withFilter("id", "eq", planet.getNormalizedText).execute().toString())

    @NCIntentSample(
        Array(
            "What was discovered by Asaph Hall",
            "What was discovered by Hall",
            "Galileo Galilei planets",
            "Galilei planets",
        )
    )
    @NCIntent(
        "intent=discoverer " +
        "    options={" +
        "        'unused_usr_toks': true " +
        "    }" +
        "    term(discoverer)={tok_id() == 'discoverer'}"
    )
    def discoverer(@NCIntentTerm("discoverer") discoverer: NCToken): NCResult =
        NCResult.text(api.bodyRequest().withFilter("discoveredBy", "cs", discoverer.getNormalizedText).execute().toString())

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

            if (dateStr.nonEmpty) {
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
            }
            else
                false
        })

        NCResult.text(res.toString())
    }
}