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

package org.apache.nlpcraft.examples.weather;

import com.google.gson.Gson;
import org.apache.nlpcraft.examples.misc.darksky.DarkSkyException;
import org.apache.nlpcraft.examples.misc.darksky.DarkSkyService;
import org.apache.nlpcraft.examples.misc.geo.keycdn.GeoManager;
import org.apache.nlpcraft.examples.misc.geo.keycdn.beans.GeoDataBean;
import org.apache.nlpcraft.model.*;

import java.time.Instant;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Weather example data model.
 * <p>
 * This is a relatively complete weather service with JSON output and a non-trivial
 * intent matching logic. It uses Apple's Dark Sky API weather provider REST service for the actual
 * weather information (https://darksky.net/dev/docs#overview)
 * <p>
 * Note that this example uses class-based intent DSL to demonstrate its usage pattern.
 * Note also that it also returns intent ID together with execution result which can be used in testing.
 * <p>
 * See 'README.md' file in the same folder for running & testing instructions.
 */
public class WeatherModel extends NCModelFileAdapter {
    // Please register your own account at https://darksky.net/dev/docs/libraries and
    // replace this demo token with your own.
    private final DarkSkyService darkSky = new DarkSkyService("097e1aad75b22b88f494cf49211975aa", 31);

    // Geo manager.
    private final GeoManager geoMrg = new GeoManager();

    // Default shift in days for history and forecast.
    private static final int DAYS_SHIFT = 5;

    // GSON instance.
    private static final Gson GSON = new Gson();

    // Keywords for 'local' weather.
    private static final Set<String> LOCAL_WORDS = new HashSet<>(Arrays.asList("my", "local", "hometown"));

    /**
     * Coordinates holder.
     */
    private static class Coordinate {
        private final double latitude;
        private final double longitude;

        Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Extracts geo location (city) from given solver context that is suitable for Dark Sky API weather service.
     *
     * @param ctx Intent solver context.
     * @param geoTokOpt Optional geo token.
     * @return Geo location.
     */
    private Coordinate prepGeo(NCIntentMatch ctx, Optional<NCToken> geoTokOpt) throws NCRejection {
        if (geoTokOpt.isPresent()) {
            NCToken geoTok = geoTokOpt.get();

            Map<String, Object> cityMeta = geoTok.meta("nlpcraft:city:citymeta");

            Double lat = (Double)cityMeta.get("latitude");
            Double lon = (Double)cityMeta.get("longitude");

            if (lat == null || lon == null) {
                String city = geoTok.meta("nlpcraft:city:city");

                throw new NCRejection(String.format("Latitude and longitude not found for: %s", city));
            }

            return new Coordinate(lat, lon);
        }

        Optional<GeoDataBean> geoOpt = geoMrg.get(ctx.getContext().getRequest());

        if (!geoOpt.isPresent())
            throw new NCRejection("City cannot be determined.");

        // Manually process request for local weather. We need to separate between 'local Moscow weather'
        // and 'local weather' which are different. Basically, if there is word 'local/my/hometown' in the user
        // input and there is no city in the current sentence - this is a request for the weather at user's
        // current location, i.e. we should implicitly assume user's location and clear conversion context.
        // In all other cases - we take location from either current sentence or conversation STM.

        // NOTE: we don't do this separation on intent level as it is easier to do it here instead of
        // creating more intents with almost identical callbacks.

        @SuppressWarnings("SuspiciousMethodCalls")
        boolean hasLocalWord =
            ctx.getVariant().stream().anyMatch(t -> LOCAL_WORDS.contains(t.meta("nlpcraft:nlp:origtext")));

        if (hasLocalWord)
            // Because we implicitly assume user's current city at this point we need to clear
            // 'nlpcraft:city' tokens from conversation since they would no longer be valid.
            ctx.getContext().getConversation().clearStm(t -> t.getId().equals("nlpcraft:city"));

        // Try current user location.
        GeoDataBean geo = geoOpt.get();

        return new Coordinate(geo.getLatitude(), geo.getLongitude());
    }

    /**
     * Strict check for an exact match (i.e. no dangling unused system or user defined tokens) and
     * maximum number of free words left unmatched. In both cases user input will be rejected.
     *
     * @param ctx Solver context.
     */
    private void checkMatch(NCIntentMatch ctx) {
        // Reject if intent match is not exact (at least one "dangling" token remain).
        if (ctx.isAmbiguous())
            throw new NCRejection("Please clarify your request.");
    }

    /**
     *
     * @param ctx
     * @return
     */
    @NCIntent(
        "intent=req " +
        "conv=true " + // Support conversation context (i.e. short term memory).
        "term={id == 'wt:phen'}+ " + // One or more weather phenomenon (at least is mandatory).
        "term(ind)={groups @@ 'indicator'}* " + // Optional indicator words (zero or more).
        "term(city)={id == 'nlpcraft:city'}? " + // Optional city.
        "term(date)={id == 'nlpcraft:date'}?" // Optional date (overrides indicator words).
    )
    @NCIntentSample({
        "What's the local weather forecast?",
        "What's the weather in Moscow?",
        "What is the weather like outside?",
        "How's the weather?",
        "What's the weather forecast for the rest of the week?",
        "What's the weather forecast this week?",
        "What's the weather out there?",
        "Is it cold outside?",
        "Is it hot outside?",
        "Will it rain today?",
        "When it will rain in Delhi?",
        "Is there any possibility of rain in Delhi?",
        "Is it raining now?",
        "Is there any chance of rain today?"
    })
    public NCResult onMatch(
        NCIntentMatch ctx,
        @NCIntentTerm("ind") List<NCToken> indToksOpt,
        @NCIntentTerm("city") Optional<NCToken> cityTokOpt,
        @NCIntentTerm("date") Optional<NCToken> dateTokOpt
    ) {
        checkMatch(ctx);

        try {
            Instant now = Instant.now();

            Instant from = now;
            Instant to = now;

            if (indToksOpt.stream().anyMatch(tok -> tok.getId().equals("wt:hist")))
                from = from.minus(DAYS_SHIFT, DAYS);
            else if (indToksOpt.stream().anyMatch(tok -> tok.getId().equals("wt:fcast")))
                to = from.plus(DAYS_SHIFT, DAYS);

            if (dateTokOpt.isPresent()) { // Date token overrides any indicators.
                NCToken dateTok = dateTokOpt.get();

                from = Instant.ofEpochMilli(dateTok.meta("nlpcraft:date:from"));
                to = Instant.ofEpochMilli(dateTok.meta("nlpcraft:date:to"));
            }

            Coordinate latLon = prepGeo(ctx, cityTokOpt); // Handles optional city too.

            return NCResult.json(GSON.toJson(from == to ? darkSky.getCurrent(latLon.latitude, latLon.longitude) :
                darkSky.getTimeMachine(latLon.latitude, latLon.longitude, from, to)));
        }
        catch (DarkSkyException e) {
            throw new NCRejection(e.getLocalizedMessage());
        }
        catch (NCRejection e) {
            throw e;
        }
        catch (Exception e) {
            throw new NCRejection("Weather provider error.", e);
        }
    }

    /**
     * Loads the model.
     */
    public WeatherModel() {
        // Load model from external JSON file on classpath.
        super("org/apache/nlpcraft/examples/weather/weather_model.json");
    }

    @Override
    public void onDiscard() {
        darkSky.stop();
    }
}
