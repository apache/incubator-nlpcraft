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
import org.apache.commons.text.WordUtils;
import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.examples.misc.geo.cities.CitiesDataProvider;
import org.apache.nlpcraft.examples.misc.geo.cities.City;
import org.apache.nlpcraft.examples.misc.geo.cities.CityData;
import org.apache.nlpcraft.examples.misc.geo.keycdn.GeoManager;
import org.apache.nlpcraft.examples.misc.geo.keycdn.beans.GeoDataBean;
import org.apache.nlpcraft.model.*;

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
 * See 'README.md' file in the same folder for running instructions.
 *
 * @see TimeTest
 */
public class TimeModel extends NCModelFileAdapter {
    // Medium data formatter.
    static private final DateTimeFormatter FMT = DateTimeFormatter.ofLocalizedDateTime(MEDIUM);

    // Map of cities and their geo and timezone information.
    static private final Map<City, CityData> citiesData = CitiesDataProvider.get();

    // Geo manager.
    static private final GeoManager geoMrg = new GeoManager();

    /**
     * Initializes model.
     *
     * @throws NCException If any errors occur.
     */
    public TimeModel() throws NCException {
        super("org/apache/nlpcraft/examples/time/time_model.yaml");
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
        Map<String, Object> res = new HashMap<>();

        res.put("city", WordUtils.capitalize(city));
        res.put("country", WordUtils.capitalize(cntry));
        res.put("timezone", tmz);
        res.put("lat", lat);
        res.put("lon", lon);
        res.put("localTime", ZonedDateTime.now(ZoneId.of(tmz)).format(FMT));

        try {
            return NCResult.yaml(new ObjectMapper(new YAMLFactory()).writeValueAsString(res));
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("YAML conversion error.", e);
        }
    }

    /**
     * Callback on local time intent match.
     *
     * @param ctx Intent solver context.
     * @return Query result.
     */
    @NCIntent("intent=intent1 conv=false term={id=='x:time'}")
    private NCResult onLocalMatch(NCIntentMatch ctx) {
        // NOTE:
        // We need to have two intents vs. one intent with an optional GEO. The reason is that
        // first intent isn't using the conversation to make sure we can always ask
        // for local time **no matter** what was asked before... Note that non-conversational
        // intent always "wins" over the conversational one given otherwise equal weight because
        // non-conversational intent is more specific (i.e. using only the most current user input).

        // Check for exactly one 'x:time' token **without** looking into the conversation.
        // That's an indication of asking for local time only.

        if (ctx.isAmbiguous()) throw new NCRejection("Not exact match.");

        Optional<GeoDataBean> geoOpt = geoMrg.get(ctx.getContext().getRequest());

        // Get local GEO data from sentence metadata defaulting to
        // Silicon Valley location in case we are missing that info.
        GeoDataBean geo = geoOpt.orElseGet(geoMrg::getSiliconValley);

        return mkResult(
            geo.getCityName(), geo.getCountryName(), geo.getTimezoneName(), geo.getLatitude(), geo.getLongitude()
        );
    }

    /**
     * Callback on remote time intent match.
     *
     * @param cityTok Token for 'geo' term.
     * @return Query result.
     */
    @NCIntent("intent=intent2 term={id=='x:time'} term(city)={id=='nlpcraft:city'}")
    private NCResult onRemoteMatch(@NCIntentTerm("city") NCToken cityTok) {
        String city = cityTok.meta("nlpcraft:city:city");
        String cntry = cityTok.meta("nlpcraft:city:country");

        CityData data = citiesData.get(new City(city, cntry));

        if (data != null)
            return mkResult(city, cntry, data.getTimezone(), data.getLatitude(), data.getLongitude());

        // We don't have timezone mapping for parsed GEO location.
        // Instead of defaulting to a local time - we reject with a specific error message for cleaner UX.
        throw new NCRejection(String.format("No timezone mapping for %s, %s.", city, cntry));
    }
}
