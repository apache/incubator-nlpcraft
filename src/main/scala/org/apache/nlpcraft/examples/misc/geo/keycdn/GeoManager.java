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

package org.apache.nlpcraft.examples.misc.geo.keycdn;

import com.google.gson.Gson;
import org.apache.nlpcraft.examples.misc.geo.keycdn.beans.GeoDataBean;
import org.apache.nlpcraft.examples.misc.geo.keycdn.beans.ResponseBean;
import org.apache.nlpcraft.model.NCRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * Geo data finder.
 *
 * There are following restrictions to simplify example:
 *
 * 1. Finder's cache is never cleared.
 * 2. Implementation is not thread safe.
 * 3. Errors just forwarded to error console.
 * 4. Cache, which used to avoid rate-limiting requests (3 requests per second, see https://tools.keycdn.com/geo),
 *    applied only to successfully received GEO data.
 */
public class GeoManager {
    private static final String URL = "https://tools.keycdn.com/geo.json?host=";
    private static final Gson GSON = new Gson();
    
    private final Map<String, GeoDataBean> cache = new HashMap<>();
    private String externalIp = null;
    
    /**
     * Gets optional geo data by given sentence.
     *
     * @param sen Sentence.
     * @return Geo data. Optional.
     */
    public Optional<GeoDataBean> get(NCRequest sen) {
        if (!sen.getRemoteAddress().isPresent()) {
            System.err.println("Geo data can't be found because remote address is not available in the sentence.");

            return Optional.empty();
        }
    
        String host = sen.getRemoteAddress().get();
    
        if (host.equalsIgnoreCase("localhost") || host.equalsIgnoreCase("127.0.0.1")) {
            if (externalIp == null) {
                try {
                    externalIp = getExternalIp();
                }
                catch (IOException e) {
                    System.err.println("External IP cannot be detected for localhost.");
        
                    return Optional.empty();
                }
            }
    
            host = externalIp;
        }
    
        try {
            GeoDataBean geo = cache.get(host);
    
            if (geo != null)
                return Optional.of(geo);
            
            HttpURLConnection conn = (HttpURLConnection)(new URL(URL + host).openConnection());
    
            // This service requires "User-Agent" property for some reasons.
            conn.setRequestProperty("User-Agent", "rest");
    
            try (InputStream in = conn.getInputStream()) {
                String enc = conn.getContentEncoding();
    
                InputStream stream = enc != null && enc.equals("gzip") ? new GZIPInputStream(in) : in;
                
                ResponseBean resp =
                    GSON.fromJson(new BufferedReader(new InputStreamReader(stream)), ResponseBean.class);
        
                if (!resp.getStatus().equals("success"))
                    throw new IOException(
                        MessageFormat.format(
                            "Unexpected response [status={0}, description={1}]",
                            resp.getStatus(),
                            resp.getDescription())
                    );
        
                geo = resp.getData().getGeo();
                
                cache.put(host, geo);
        
                return Optional.of(geo);
            }
        }
        catch (Exception e) {
            System.err.println(
                MessageFormat.format(
                    "Unable to answer due to IP location finder (keycdn) error for host: {0}",
                    host
                )
            );
    
            e.printStackTrace(System.err);
    
            return Optional.empty();
        }
    }
    
    /**
     * Gets external IP.
     *
     * @return External IP.
     * @throws IOException If any errors occur.
     */
    private static String getExternalIp() throws IOException {
        try (BufferedReader in =
            new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()))) {
            return in.readLine();
        }
    }
    
    /**
     * Gets Silicon Valley location. Used as default value for each example service.
     * This default location definition added here just for accumulating all GEO manipulation logic in one class.
     *
     * @return Silicon Valley location.
     */
    public GeoDataBean getSiliconValley() {
        GeoDataBean geo = new GeoDataBean();
        
        geo.setCityName("");
        geo.setCountryName("United States");
        geo.setTimezoneName("America/Los_Angeles");
        geo.setTimezoneName("America/Los_Angeles");
        geo.setLatitude(37.7749);
        geo.setLongitude(122.4194);
        
        return geo;
    }
}
