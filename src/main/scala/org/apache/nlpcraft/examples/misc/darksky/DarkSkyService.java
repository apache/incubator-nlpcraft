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

package org.apache.nlpcraft.examples.misc.darksky;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
 * Dark Sky API weather provider. See https://darksky.net/dev/docs#overview for details.
 */
public class DarkSkyService {
    /** */
    private final String key;

    /** */
    private final int maxDaysSecs;

    /** */
    private final CloseableHttpClient httpClient;

    /** */
    private static final Type TYPE_RESP = new TypeToken<HashMap<String, Object>>() {}.getType();

    /** */
    private static final Gson GSON = new Gson();

    /** */
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault());

    // Can be configured.
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     *
     */
    private static final Logger log = LoggerFactory.getLogger(DarkSkyService.class);

    /**
     *
     */
    private static final ResponseHandler<String> GET_HANDLER = resp -> {
        int code = resp.getStatusLine().getStatusCode();

        if (resp.getEntity() == null)
            throw new DarkSkyException(String.format("Unexpected empty response [code=%d]", code));

        String js = EntityUtils.toString(resp.getEntity());

        if (code != 200)
            throw new DarkSkyException(String.format("Unexpected response [code=%d, text=%s]", code, js));

        return js;
    };

    /**
     * Constructor.
     *
     * @param key     Service key.
     * @param maxDays Max days configuration value.
     */
    public DarkSkyService(String key, int maxDays) {
        this.key = key;
        this.maxDaysSecs = maxDays * 24 * 60 * 60;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Stop method.
     */
    public void stop() {
        pool.shutdown();

        try {
            pool.awaitTermination(Long.MAX_VALUE, MILLISECONDS);
        }
        catch (InterruptedException e) {
            log.error("Error stopping pool.", e);
        }
    }

    /**
     *
     * @param lat
     * @param lon
     * @param d
     * @return
     */
    private Map<String, Object> get(double lat, double lon, Instant d) {
        return get(
            "https://api.darksky.net/forecast/" + key + '/' + lat + ',' + lon + ',' + FMT.format(d) +
                "?exclude=currently,minutely,hourly,alerts,flags?lang=en"
        );
    }

    /**
     *
     * @param url
     * @return
     */
    private Map<String, Object> get(String url) {
        // Ack.
        System.out.println("REST URL prepared: " + url);

        HttpGet get = new HttpGet(url);

        try {
            return GSON.fromJson(httpClient.execute(get, GET_HANDLER), TYPE_RESP);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);

            throw new DarkSkyException("Unable to answer due to weather data provider error.");
        }
        finally {
            get.releaseConnection();
        }
    }

    /**
     * See https://darksky.net/dev/docs#response-format to extract fields.
     *
     * @param lat
     * @param lon
     * @param from
     * @param to
     * @return
     */
    public List<Map<String, Object>> getTimeMachine(double lat, double lon, Instant from, Instant to) {
        assert from != null;
        assert to != null;

        log.debug("DarkSky time machine API call [lat={}, lon={}, from={}, to={}]", lat, lon, from, to);

        if (Duration.between(from, to).get(SECONDS) > maxDaysSecs)
            throw new DarkSkyException(String.format("Too long request period [from=%s, to=%s]", from, to));

        long durMs = to.toEpochMilli() - from.toEpochMilli();

        int n = (int) (durMs / 86400000 + (durMs % 86400000 == 0 ? 0 : 1));

        return IntStream.range(0, n).
            mapToObj(shift -> pool.submit(() -> Pair.of(shift, get(lat, lon, from.plus(shift, DAYS))))).
            map(p -> {
                try {
                    return p.get();
                }
                catch (ExecutionException | InterruptedException e) {
                    throw new DarkSkyException("Error execution weather request.", e);
                }
            }).
            sorted(Comparator.comparing(Pair::getLeft)).
            map(Pair::getRight).
            collect(Collectors.toList());
    }

    /**
     * See https://darksky.net/dev/docs#response-format to extract fields.
     *
     * @param lat
     * @param lon
     * @return
     * @throws DarkSkyException
     */
    public Map<String, Object> getCurrent(double lat, double lon) throws DarkSkyException {
        return get("https://api.darksky.net/forecast/" + key + '/' + lat + ',' + lon +
            "?exclude=minutely,hourly,daily,alerts,flags?lang=en");
    }
}
