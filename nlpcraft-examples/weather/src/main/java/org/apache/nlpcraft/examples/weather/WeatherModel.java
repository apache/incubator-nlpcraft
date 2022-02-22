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
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.nlpcraft.NCEntity;
import org.apache.nlpcraft.NCIntent;
import org.apache.nlpcraft.NCIntentMatch;
import org.apache.nlpcraft.NCIntentSample;
import org.apache.nlpcraft.NCIntentTerm;
import org.apache.nlpcraft.NCModel;
import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.NCModelPipeline;
import org.apache.nlpcraft.NCModelPipelineBuilder;
import org.apache.nlpcraft.NCRejection;
import org.apache.nlpcraft.NCResult;
import org.apache.nlpcraft.NCResultType;
import org.apache.nlpcraft.examples.utils.cities.CitiesDataProvider;
import org.apache.nlpcraft.examples.utils.cities.City;
import org.apache.nlpcraft.examples.utils.cities.CityData;
import org.apache.nlpcraft.examples.utils.keycdn.GeoManager;
import org.apache.nlpcraft.examples.utils.keycdn.GeoData;
import org.apache.nlpcraft.examples.weather.openweathermap.OpenWeatherMapException;
import org.apache.nlpcraft.examples.weather.openweathermap.OpenWeatherMapService;
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser;
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.en.NCEnSemanticPorterStemmer;
import org.apache.nlpcraft.nlp.entity.parser.stanford.NCStanfordNLPEntityParser;
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Weather example data model.
 * <p>
 * This is a relatively complete weather service with JSON output and a non-trivial
 * intent matching logic. It uses OpenWeather API weather provider REST service for the actual
 * weather information (https://openweathermap.org/api/one-call-api).
 * <p>
 * NOTE: you must provide OpenWeather API key in 'OWM_API_KEY' system property.
 * See  https://openweathermap.org/api for more information.
 * <p>
 * See 'README.md' file in the same folder for running and testing instructions.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class WeatherModel implements NCModel {
    // System property for OpenWeatherMap API key.
    public final String OWM_API_KEY = "OWM_API_KEY";

    // Please register your own account at https://openweathermap.org/api and
    // replace this demo token with your own.
    // We are using the One Call API (https://openweathermap.org/api/one-call-api) in this example
    private final OpenWeatherMapService openWeather;

    // Geo manager.
    private final GeoManager geoMrg = new GeoManager();

    // Default shift in days for history and forecast.
    private static final int DAYS_SHIFT_BACK = 5;
    private static final int DAYS_SHIFT_FORWARD = 7;

    // GSON instance.
    private static final Gson GSON = new Gson();

    static private final Map<City, CityData> citiesData = CitiesDataProvider.get();

    // Keywords for 'local' weather.
    private static final Set<String> LOCAL_WORDS = new HashSet<>(Arrays.asList("my", "local", "hometown"));

    private final NCModelConfig cfg;
    private final NCModelPipeline pipeline;

    /**
     * Extracts geolocation (city) from given solver context that is suitable for Dark Sky API weather service.
     *
     * @param ctx Intent solver context.
     * @param geoEntOpt Optional geo entity.
     * @return Geo location.
     */
    private CityData prepGeo(NCIntentMatch ctx, Optional<NCEntity> geoEntOpt) throws NCRejection {
        if (geoEntOpt.isPresent()) {
            String cityName = geoEntOpt.get().mkText();

            Optional<Map.Entry<City, CityData>> dataOpt =
                citiesData.entrySet().stream().filter(p -> p.getKey().getName().equalsIgnoreCase(cityName)).findAny();

            if (!dataOpt.isPresent()) {
                throw new NCRejection(String.format("Latitude and longitude not found for: %s", cityName));
            }

            return  dataOpt.get().getValue();
        }

        Optional<GeoData> geoOpt = geoMrg.get(ctx.getContext().getRequest());

        if (geoOpt.isEmpty())
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
            ctx.getVariant().getEntities().stream().anyMatch(t -> LOCAL_WORDS.contains(t.mkText().toLowerCase()));

        if (hasLocalWord)
            // Because we implicitly assume user's current city at this point we need to clear
            // 'nlpcraft:city' tokens from conversation since they would no longer be valid.
            ctx.getContext().getConversation().clearStm(t -> t.getId().equals("nlpcraft:city"));

        // Try current user location.
        GeoData geo = geoOpt.get();

        return new CityData(geo.getTimezoneName(), geo.getLatitude(), geo.getLongitude());
    }

    /**
     * A callback for the intent match.
     *
     * @param ctx Intent match context.
     * @param indEntsOpt List of optional indicator elements.
     * @param cityEntOpt Optional GEO token for city.
     * @param dateEntOpt Optional date token.
     * @return Callback result.
     */
    @NCIntent(
        "intent=req " +
        "term~{# == 'wt:phen'}* " + // Zero or more weather phenomenon.
        "term(ind)~{" +
            "@isIndicator = has(ent_groups, 'indicator') " + // Just to demo term variable usage.
            "@isIndicator" +
        "}* " + // Optional indicator words (zero or more).
        "term(city)~{# == 'nlpcraft:city'}? " + // Optional city.
        "term(date)~{# == 'nlpcraft:date'}?" // Optional date (overrides indicator words).
    )
    // NOTE: each samples group will reset conversation STM during auto-testing.
    @NCIntentSample({
        "Current forecast?",
        "Chance of rain in Berlin now?"
    })
    // NOTE: each samples group will reset conversation STM during auto-testing.
    @NCIntentSample({
        "Moscow forecast?",
        "Chicago history"
    })
    // NOTE: each samples group will reset conversation STM during auto-testing.
    @NCIntentSample({
        "What's the local weather forecast?",
        "What's the weather in Moscow?",
        "What's the current forecast for LA?",
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
        "Is there any chance of rain today?",
        "Was it raining in Beirut three days ago?",
        "How about yesterday?"
    })
    public NCResult onMatch(
        NCIntentMatch ctx,
        @NCIntentTerm("ind") List<NCEntity> indEntsOpt,
        @NCIntentTerm("city") Optional<NCEntity> cityEntOpt,
        @NCIntentTerm("date") Optional<NCEntity> dateEntOpt
    ) {
        try {
            Instant now = Instant.now();

            Instant from = now;
            Instant to = now;

            if (indEntsOpt.stream().anyMatch(tok -> tok.getId().equals("wt:hist")))
                from = from.minus(DAYS_SHIFT_BACK, DAYS);
            else if (indEntsOpt.stream().anyMatch(tok -> tok.getId().equals("wt:fcast")))
                to = from.plus(DAYS_SHIFT_FORWARD, DAYS);

            if (dateEntOpt.isPresent()) { // Date token overrides any indicators.
                NCEntity dateEnt = dateEntOpt.get();

                // TODO: from NNE ?
                from = Instant.now();
                to = Instant.now();
            }

            CityData cd = prepGeo(ctx, cityEntOpt); // Handles optional city too.

            double lat = cd.getLatitude();
            double lon = cd.getLongitude();

            NCResult res = new NCResult();

            res.setType(NCResultType.ASK_RESULT);
            res.setBody(GSON.toJson(from == to ? openWeather.getCurrent(lat, lon) : openWeather.getTimeMachine(lat, lon, from, to)));

            return res;
        }
        catch (OpenWeatherMapException e) {
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
     *
     * @param apiKey OpenWeatherMap API key.
     */
    public WeatherModel(String apiKey) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP stanford = new StanfordCoreNLP(props);

        NCStanfordNLPTokenParser tp = new NCStanfordNLPTokenParser(stanford);

        this.cfg = new NCModelConfig("nlpcraft.weather.ex", "Weather Example Model", "1.0");
        this.pipeline = new NCModelPipelineBuilder(
            tp,
            new NCStanfordNLPEntityParser(stanford, "date", "city"),
            new NCSemanticEntityParser(new NCEnSemanticPorterStemmer(), tp, "weather_model.json")
        ).build();

        openWeather = new OpenWeatherMapService(apiKey, 5, 7);
    }

    /**
     *
     */
    public void close() {
        openWeather.stop();
    }

    @Override
    public NCModelConfig getConfig() {
        return cfg;
    }

    @Override
    public NCModelPipeline getPipeline() {
        return pipeline;
    }
}
