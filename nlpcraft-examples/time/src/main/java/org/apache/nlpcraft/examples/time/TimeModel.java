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

package org.apache.nlpcraft.examples.time;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.nlpcraft.NCEntity;
import org.apache.nlpcraft.NCIntent;
import org.apache.nlpcraft.NCIntentMatch;
import org.apache.nlpcraft.NCIntentRef;
import org.apache.nlpcraft.NCIntentSample;
import org.apache.nlpcraft.NCIntentTerm;
import org.apache.nlpcraft.NCModelAdapter;
import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.NCModelPipelineBuilder;
import org.apache.nlpcraft.NCRejection;
import org.apache.nlpcraft.NCResult;
import org.apache.nlpcraft.NCResultType;
import org.apache.nlpcraft.examples.time.utils.cities.CitiesDataProvider;
import org.apache.nlpcraft.examples.time.utils.cities.City;
import org.apache.nlpcraft.examples.time.utils.cities.CityData;
import org.apache.nlpcraft.examples.time.utils.keycdn.GeoData;
import org.apache.nlpcraft.examples.time.utils.keycdn.GeoManager;
import org.apache.nlpcraft.nlp.entity.parser.NCEnSemanticEntityParser;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.format.FormatStyle.MEDIUM;

/**
 * Time example data model.
 * <p>
 * This example answers the questions about current time, either local or at some city.
 * It provides YAML response with time and timezone information.
 * <p>
 * See 'README.md' file in the same folder for running and testing instructions.
 */
// Declaring intents on the class level + fragment usage for demo purposes.
@NCIntent("fragment=city term(city)~{# == 'opennlp:location'}")
@NCIntent("intent=intent2 term~{# == 'x:time'} fragment(city)")
@NCIntent("intent=intent1 term={# == 'x:time'}")
public class TimeModel extends NCModelAdapter {
    // Medium data formatter.
    static private final DateTimeFormatter FMT = DateTimeFormatter.ofLocalizedDateTime(MEDIUM);

    // Map of cities and their geo and timezone information.
    static private final Map<City, CityData> citiesData = CitiesDataProvider.get();

    // Geo manager.
    static private final GeoManager geoMrg = new GeoManager();

    /**
     * Initializes model.
     */
    public TimeModel() {
        super(
            new NCModelConfig("nlpcraft.time.ex", "Time Example Model", "1.0"),
            new NCModelPipelineBuilder().withLanguage("EN").withEntityParser(new NCEnSemanticEntityParser("time_model.yaml")).build()
        );
    }

    /**
     * Gets YAML query result.
     *
     * @param city  Detected city.
     * @param cntry Detected country.
     * @param tmz Timezone ID.
     * @param lat City latitude.
     * @param lon City longitude.
     */
    private static NCResult mkResult(String city, String cntry, String tmz, double lat, double lon) {
        Map<String, Object> m = new HashMap<>();

        m.put("city", capitalize(city));
        m.put("country", capitalize(cntry));
        m.put("timezone", tmz);
        m.put("lat", lat);
        m.put("lon", lon);
        m.put("localTime", ZonedDateTime.now(ZoneId.of(tmz)).format(FMT));

        NCResult res = new NCResult();

        res.setType(NCResultType.ASK_RESULT);

        try {
            res.setBody(new ObjectMapper(new YAMLFactory()).writeValueAsString(m));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("YAML conversion error.", e);
        }

        return res;
    }

    /**
     * 
     * @param s
     * @return
     */
    private static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
    }

    /**
     * Callback on remote time intent match.
     *
     * @param cityEnt Token for 'geo' term.
     * @return Query result.
     */
    @NCIntentRef("intent2")
    @NCIntentSample({
        "What time is it now in New York City?",
        "What's the current time in Moscow?",
        "Show me time of the day in London.",
        "Can you please give me the Tokyo's current date and time."
    })
    private NCResult onRemoteMatch(@NCIntentTerm("city") NCEntity cityEnt) {
        String cityName = cityEnt.mkText();

        Optional<Map.Entry<City, CityData>> dataOpt =
            citiesData.entrySet().stream().filter(p -> p.getKey().getName().equalsIgnoreCase(cityName)).findAny();

        if (dataOpt.isPresent()) {
            Map.Entry<City, CityData> e = dataOpt.get();
            City city = e.getKey();
            CityData data = e.getValue();

            return mkResult(city.getName(), city.getCountry(), data.getTimezone(), data.getLatitude(), data.getLongitude());
        }

        // We don't have timezone mapping for parsed GEO location.
        // Instead of defaulting to a local time - we reject with a specific error message for cleaner UX.
        throw new NCRejection(String.format("No timezone mapping for %s.", cityName));
    }

    /**
     * Callback on local time intent match.
     *
     * @param ctx Intent solver context.
     * @return Query result.
     */
    @NCIntentRef("intent1")
    @NCIntentSample({
        "What's the local time?"
    })
    private NCResult onLocalMatch(NCIntentMatch ctx) {
        // NOTE:
        // We need to have two intents vs. one intent with an optional GEO. The reason is that
        // first intent isn't using the conversation to make sure we can always ask
        // for local time **no matter** what was asked before... Note that non-conversational
        // intent always "wins" over the conversational one given otherwise equal weight because
        // non-conversational intent is more specific (i.e. using only the most current user input).

        // Check for exactly one 'x:time' token **without** looking into the conversation.
        // That's an indication of asking for local time only.

        Optional<GeoData> geoOpt = geoMrg.get(ctx.getContext().getRequest());

        // Get local GEO data from sentence metadata defaulting to
        // Silicon Valley location in case we are missing that info.
        GeoData geo = geoOpt.orElseGet(geoMrg::getSiliconValley);

        return mkResult(
            geo.getCityName(), geo.getCountryName(), geo.getTimezoneName(), geo.getLatitude(), geo.getLongitude()
        );
    }
}
