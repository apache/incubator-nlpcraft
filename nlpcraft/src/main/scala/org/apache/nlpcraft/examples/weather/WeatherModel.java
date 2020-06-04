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
 * This is relatively complete weather service with JSON output and a non-trivial
 * intent matching logic. It uses Apple's Dark Sky API weather provider REST service for the actual
 * weather information (https://darksky.net/dev/docs#overview)
 * <p>
 * Note that this example uses class-based intent DSL to demonstrate its usage pattern.
 * Note also that it also returns intent ID together with execution result which can be used in testing.
 * <p>
 * See 'README.md' file in the same folder for running instructions.
 *
 * @see WeatherTest
 */
public class WeatherModel extends NCModelFileAdapter {
    // Please register your own account at https://darksky.net/dev/docs/libraries and
    // replace this demo token with your own.
    private final DarkSkyService srv = new DarkSkyService("097e1aad75b22b88f494cf49211975aa", 31);

    // Geo manager.
    private final GeoManager geoMrg = new GeoManager();
    
    private static final Gson GSON = new Gson();
    
    // Keywords for 'local' weather.
    private static final Set<String> LOCAL_WORDS = new HashSet<>(Arrays.asList("my", "local", "hometown"));

    /**
     * Date range holder.
     */
    private static class DateRange {
        private final Instant from;
        private final Instant to;

        DateRange(Instant from, Instant to) {
            this.from = from;
            this.to = to;
        }
    }

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
     * Makes JSON result.
     *
     * @param res Weather holder.
     * @param intentId Intent ID.
     * @return Query result.
     */
    private NCResult makeResult(Object res, String intentId) {
        return NCResult.json(GSON.toJson(new WeatherResultWrapper(intentId, res)));
    }

    /**
     * Extracts date range from given solver context.
     *
     * @param tok date token.
     * @return Pair of dates.
     */
    private DateRange extractDate(NCToken tok) {
        return new DateRange(
            Instant.ofEpochMilli(tok.meta("nlpcraft:date:from")),
            Instant.ofEpochMilli(tok.meta("nlpcraft:date:to"))
        );
    }

    /**
     * Extracts geo location (city) from given solver context that is suitable for Dark Sky API weather service.
     *
     * @param ctx Intent solver context.
     * @param dateTokOpt Optional date token.
     * @return Geo location.
     */
    private Coordinate prepGeo(NCIntentMatch ctx, Optional<NCToken> dateTokOpt) throws NCRejection {
        if (dateTokOpt.isPresent()) {
            NCToken tok = dateTokOpt.get();

            Map<String, Object> cityMeta = tok.meta("nlpcraft:city:citymeta");

            Double lat = (Double)cityMeta.get("latitude");
            Double lon = (Double)cityMeta.get("longitude");

            if (lat == null || lon == null) {
                String city = tok.meta("nlpcraft:city:city");

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
     * @param shift
     * @return
     */
    private NCResult onPeriodMatch(
        NCIntentMatch ctx,
        Optional<NCToken> cityTokOpt,
        Optional<NCToken> dateTokOpt,
        int shift
    ) {
        assert shift != 0;

        checkMatch(ctx);

        try {
            Coordinate cr = prepGeo(ctx, cityTokOpt);

            Instant now = Instant.now();

            DateRange range = dateTokOpt.map(this::extractDate).orElseGet(
                () -> shift > 0 ?
                    new DateRange(now, now.plus(shift, DAYS)) :
                    new DateRange(now.plus(shift, DAYS), now)
            );

            return makeResult(
                srv.getTimeMachine(cr.latitude, cr.longitude, range.from, range.to), ctx.getIntentId()
            );
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
     * Callback on forecast intent match.
     *
     * @param ctx Intent solver context.
     * @return Query result.
     */
    @NCIntent("intent=fcast term={id == 'wt:fcast'} term(city)={id == 'nlpcraft:city'}? term(date)={id == 'nlpcraft:date'}?")
    public NCResult onForecastMatch(
        NCIntentMatch ctx,
        @NCIntentTerm("city") Optional<NCToken> cityTokOpt,
        @NCIntentTerm("date") Optional<NCToken> dateTokOpt
    ) {
        return onPeriodMatch(ctx, cityTokOpt, dateTokOpt, 5);
    }

    /**
     * Callback on history intent match.
     *
     * @param ctx Intent solver context.
     * @return Query result.
     */
    @NCIntent("intent=hist term={id == 'wt:hist'} term(city)={id == 'nlpcraft:city'}? term(date)={id == 'nlpcraft:date'}?")
    public NCResult onHistoryMatch(
        NCIntentMatch ctx,
        @NCIntentTerm("city") Optional<NCToken> cityTokOpt,
        @NCIntentTerm("date") Optional<NCToken> dateTokOpt
    ) {
        return onPeriodMatch(ctx, cityTokOpt, dateTokOpt, -5);
    }

    /**
     * Callback on current date intent match.
     *
     * @param ctx Intent solver context.
     * @return Query result.
     */
    @NCIntent("intent=curr term={id == 'wt:curr'} term(city)={id == 'nlpcraft:city'}? term(date)={id == 'nlpcraft:date'}?")
    public NCResult onCurrentMatch(
        NCIntentMatch ctx,
        @NCIntentTerm("city") Optional<NCToken> cityTokOpt,
        @NCIntentTerm("date") Optional<NCToken> dateTokOpt
    ) {
        checkMatch(ctx);

        try {
            Coordinate cr = prepGeo(ctx, cityTokOpt);

            if (dateTokOpt.isPresent()) {
                DateRange range = extractDate(dateTokOpt.get());

                return makeResult(srv.getTimeMachine(cr.latitude, cr.longitude, range.from, range.to), ctx.getIntentId());
            }

            return makeResult(srv.getCurrent(cr.latitude, cr.longitude), ctx.getIntentId());
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
     * Initializes the model.
     */
    public WeatherModel() {
        // Load model from external JSON file on classpath.
        super("org/apache/nlpcraft/examples/weather/weather_model.json");
    }
}
