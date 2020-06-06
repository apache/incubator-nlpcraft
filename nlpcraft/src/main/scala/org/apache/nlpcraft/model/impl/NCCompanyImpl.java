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

package org.apache.nlpcraft.model.impl;

import org.apache.nlpcraft.model.*;
import java.util.*;

/**
 * User descriptor implementation.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NCCompanyImpl implements NCCompany {
    private long id;
    private String name;
    private Optional<String> website;
    private Optional<String> country;
    private Optional<String> region;
    private Optional<String> address;
    private Optional<String> city;
    private Optional<String> postalCode;

    /**
     * 
     * @param id
     * @param name
     * @param website
     * @param country
     * @param region
     * @param address
     * @param city
     * @param postalCode
     */
    public NCCompanyImpl(
        long id,
        String name,
        Optional<String> website,
        Optional<String> country,
        Optional<String> region,
        Optional<String> address,
        Optional<String> city,
        Optional<String> postalCode
    ) {
        this.id = id;
        this.name = name;
        this.website = website;
        this.country = country;
        this.region = region;
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getWebsite() {
        return website;
    }

    @Override
    public Optional<String> getCountry() {
        return country;
    }

    @Override
    public Optional<String> getRegion() {
        return region;
    }

    @Override
    public Optional<String> getCity() {
        return city;
    }

    @Override
    public Optional<String> getAddress() {
        return address;
    }

    @Override
    public Optional<String> getPostalCode() {
        return postalCode;
    }
}
