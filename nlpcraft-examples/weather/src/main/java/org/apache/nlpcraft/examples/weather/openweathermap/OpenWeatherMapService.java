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

package org.apache.nlpcraft.examples.weather.openweathermap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * OpenWeather API weather provider. See https://openweathermap.org/api for details.
 */
public class OpenWeatherMapService {
    // GSON response type.
    private static final Type TYPE_RESP = new TypeToken<HashMap<String, Object>>() { }.getType();

    // Access key.
    private final String key;

    // Maximum days (looking backwards) in seconds.
    private final int maxDaysBackSecs;

    // Maximum days (looking forwards) in seconds.
    private final int maxDaysForwardSecs;

    // HTTP client instance.
    private final HttpClient httpClient;

    // GSON instance.
    private static final Gson GSON = new Gson();

    // Can be configured.
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    /**
     *
     */
    private static final Logger log = LoggerFactory.getLogger(OpenWeatherMapService.class);

    /**
     * Constructor.
     *
     * @param key Service key.
     * @param maxDaysBack Max days (looking back) configuration value.
     * @param maxDaysForward Max days (looking forward) configuration value.
     */
    public OpenWeatherMapService(String key, int maxDaysBack, int maxDaysForward) {
        this.key = key;
        this.maxDaysBackSecs = maxDaysBack * 24 * 60 * 60;
        this.maxDaysForwardSecs = maxDaysForward * 24 * 60 * 60;
        this.httpClient = HttpClient.newBuilder().build();
    }

    /**
     * Stop method.
     */
    public void stop() {
        pool.shutdown();

        try {
            //noinspection ResultOfMethodCallIgnored
            pool.awaitTermination(Long.MAX_VALUE, MILLISECONDS);
        }
        catch (InterruptedException e) {
            log.error("Error stopping pool.", e);
        }
    }

    /**
     * @param lat Latitude.
     * @param lon Longitude.
     * @param d Date.
     * @return REST call result.
     */
    private Map<String, Object> get(double lat, double lon, long d) {
        return get("https://api.openweathermap.org/data/2.5/onecall?" +
            "lat=" + lat +
            "&lon=" + lon +
            "&dt=" + d +
            "&exclude=current,minutely,hourly,daily,alerts&appid=" + key
        );
    }

    /**
     * @param url REST endpoint URL.
     * @return REST call result.
     */
    private Map<String, Object> get(String url) {
        // Ack.
        System.out.println("REST URL prepared: " + url);

        HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();

        try {
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            return GSON.fromJson(resp.body(), TYPE_RESP);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);

            throw new OpenWeatherMapException("Unable to answer due to weather data provider error.");
        }
    }

    /**
     * See https://openweathermap.org/api/one-call-api#hist_parameter to extract fields.
     *
     * @param lat Latitude.
     * @param lon Longitude.
     * @param from From date.
     * @param to  To date.
     * @return List of REST call results.
     * @throws OpenWeatherMapException Thrown in case of any provider errors.
     */
    public List<Map<String, Object>> getTimeMachine(double lat, double lon, Instant from, Instant to) throws OpenWeatherMapException {
        assert from != null;
        assert to != null;

        log.debug("OpenWeather time machine API call [lat={}, lon={}, from={}, to={}]", lat, lon, from, to);

        Instant now = Instant.now();
        long forwardSeconds = to.getEpochSecond() - now.getEpochSecond();
        long backSeconds = now.getEpochSecond() - from.getEpochSecond();

        if (Duration.between(from, to).get(SECONDS) > maxDaysForwardSecs && forwardSeconds > 0)
            throw new OpenWeatherMapException(String.format("Forward Request period is too long [from=%s, to=%s]", from, to));

        if (Duration.between(from, to).get(SECONDS) > maxDaysBackSecs && backSeconds > 0 && to.getEpochSecond() <= now.getEpochSecond())
            throw new OpenWeatherMapException(String.format("Backward Request period is too long [from=%s, to=%s]", from, to));

        long durMs = to.toEpochMilli() - from.toEpochMilli();

        int n = (int) (durMs / 86400000 + (durMs % 86400000 == 0 ? 0 : 1));

        class Pair {
            private final int shift;
            private final Map<String, Object> reguest;

            Pair(int left, Map<String, Object> reguest) {
                this.shift = left;
                this.reguest = reguest;
            }

            int getShift() {
                return this.shift;
            }
            Map<String, Object> getReguest() {
                return this.reguest;
            }
        }

        return IntStream.range(0, n).
            mapToObj(shift -> pool.submit(() -> new Pair(shift, get(lat, lon, from.plus(shift, DAYS).getEpochSecond())))).
            map(p -> {
                try {
                    return p.get();
                }
                catch (ExecutionException | InterruptedException e) {
                    throw new OpenWeatherMapException("Error executing weather request.", e);
                }
            }).
            sorted(Comparator.comparing(Pair::getShift)).
            map(Pair::getReguest).
            collect(Collectors.toList());
    }

    /**
     * See https://openweathermap.org/api/one-call-api#hist_parameter to extract fields.
     *
     * @param lat Latitude.
     * @param lon Longitude.
     * @return REST call result.
     * @throws OpenWeatherMapException Thrown in case of any provider errors.
     */
    public Map<String, Object> getCurrent(double lat, double lon) throws OpenWeatherMapException {
        return get("https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + key);
    }
}