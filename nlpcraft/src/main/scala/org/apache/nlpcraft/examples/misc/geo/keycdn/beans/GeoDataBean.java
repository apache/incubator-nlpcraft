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

package org.apache.nlpcraft.examples.misc.geo.keycdn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * Service https://tools.keycdn.com/geo response part bean. Geo data holder.
 */
public class GeoDataBean {
    @SerializedName("country_name") private String countryName;
    @SerializedName("city") private String cityName;
    @SerializedName("latitude") private double latitude;
    @SerializedName("longitude") private double longitude;
    @SerializedName("timezone") private String timezoneName;

    /**
     * Gets country name.
     *
     * @return Country name.
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * Sets country name.
     *
     * @param countryName Country name to set.
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    /**
     * Gets city name.
     *
     * @return City name.
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * Set city name.
     *
     * @param cityName City name to set.
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * Gets latitude.
     *
     * @return Latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets latitude.
     *
     * @param latitude Latitude to set.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets longitude.
     *
     * @return Longitude.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets longitude.
     *
     * @param longitude Longitude to set.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Get timezone name.
     *
     * @return Timezone name.
     */
    public String getTimezoneName() {
        return timezoneName;
    }

    /**
     * Sets timezone name.
     *
     * @param timezoneName Timezone name to set.
     */
    public void setTimezoneName(String timezoneName) {
        this.timezoneName = timezoneName;
    }
}

