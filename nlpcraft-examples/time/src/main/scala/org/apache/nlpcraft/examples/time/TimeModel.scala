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
package org.apache.nlpcraft.examples.time

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.*
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.examples.time.utils.cities.*
import org.apache.nlpcraft.examples.time.utils.keycdn.GeoManager
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.parsers.NCOpenNLPEntityParser

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM
import java.time.*

/**
  * This example provides very simple implementation for NLI-powered time answering bot.
  *
  * It reports about current time value in asked GEO location or in your local area.
  *
  * You can ask something like this:
  * - What time is it now in New York City?
  * - Can you please give me the Tokyo's current date and time.
  * - What's the local time?
  *
  * See 'README.md' file in the same folder for running and testing instructions.
  */
@NCIntent("fragment=city term(city)~{# == 'opennlp:location'}")
@NCIntent("intent=intent2 term~{# == 'x:time'} fragment(city)")
@NCIntent("intent=intent1 term={# == 'x:time'}")
class TimeModel extends NCModel(
    NCModelConfig("nlpcraft.time.ex", "Time Example Model", "1.0"),
    new NCPipelineBuilder().
        withSemantic("en", "time_model.yaml").
        withEntityParser(NCOpenNLPEntityParser(NCResourceReader.getPath("opennlp/en-ner-location.bin"))).
        build
):
    // Medium data formatter.
    private val FMT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(MEDIUM)

    // Map of cities and their geo and timezone information.
    private val citiesData: Map[City, CityData] = CitiesDataProvider.get

    /**
      * Gets YAML query result.
      *
      * @param city  Detected city.
      * @param cntry Detected country.
      * @param tmz Timezone ID.
      * @param lat City latitude.
      * @param lon City longitude. */
    private def mkResult(city: String, cntry: String, tmz: String, lat: Double, lon: Double): NCResult =
        val m =
            Map(
                "city" -> capitalize(city),
                "country" -> capitalize(cntry),
                "timezone" -> tmz,
                "lat" -> lat,
                "lon" -> lon,
                "localTime" -> ZonedDateTime.now(ZoneId.of(tmz)).format(FMT)
            )

        try {
            val mapper = new ObjectMapper(new YAMLFactory)
            mapper.registerModule(DefaultScalaModule)
            NCResult(mapper.writeValueAsString(m))
        }
        catch
            case e: JsonProcessingException => throw new RuntimeException("YAML conversion error.", e)

    /**
      *
      * @param s
      * @return */
    private def capitalize(s: String): String =
        if s == null || s.isEmpty then s else s"${s.substring(0, 1).toUpperCase}${s.substring(1, s.length)}"

    /**
      * Callback on remote time intent match.
      *
      * @param cityEnt Token for 'geo' term.
      * @return Query result. */
    @NCIntentRef("intent2")
    private def onRemoteMatch(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("city") cityEnt: NCEntity): NCResult =
        val cityName: String = cityEnt.mkText

        // We don't have timezone mapping for parsed GEO location.
        // Instead of defaulting to a local time - we reject with a specific error message for cleaner UX.

        val (city, data) = citiesData.find(_._1.name.equalsIgnoreCase(cityName)).getOrElse(throw new NCRejection(s"No timezone mapping for $cityName."))

        mkResult(city.name, city.country, data.timezone, data.latitude, data.longitude)

    /**
      * Callback on local time intent match.
      *
      * @param ctx Intent solver context.
      * @return Query result. */
    @NCIntentRef("intent1")
    private def onLocalMatch(ctx: NCContext, im: NCIntentMatch): NCResult =  // NOTE:
        // We need to have two intents vs. one intent with an optional GEO. The reason is that
        // first intent isn't using the conversation to make sure we can always ask
        // for local time **no matter** what was asked before... Note that non-conversational
        // intent always "wins" over the conversational one given otherwise equal weight because
        // non-conversational intent is more specific (i.e. using only the most current user input).
        // Check for exactly one 'x:time' token **without** looking into the conversation.
        // That's an indication of asking for local time only.
        // Get local GEO data from sentence metadata defaulting to
        // Silicon Valley location in case we are missing that info.
        val geo = GeoManager.get(ctx.getRequest).getOrElse(GeoManager.getSiliconValley)

        mkResult(geo.city, geo.country_name, geo.timezone, geo.latitude, geo.longitude)

