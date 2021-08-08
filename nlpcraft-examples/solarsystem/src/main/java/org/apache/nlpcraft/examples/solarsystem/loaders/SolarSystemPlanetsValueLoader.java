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

package org.apache.nlpcraft.examples.solarsystem.loaders;

import org.apache.nlpcraft.examples.solarsystem.api.SolarSystemOpenApiService;
import org.apache.nlpcraft.model.NCElement;
import org.apache.nlpcraft.model.NCValue;
import org.apache.nlpcraft.model.NCValueLoader;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SolarSystemPlanetsValueLoader implements NCValueLoader {
    private SolarSystemOpenApiService api;

    public SolarSystemPlanetsValueLoader(SolarSystemOpenApiService api) {
        this.api = api;
    }

    private NCValue mkValue(String id, String v) {
        return new NCValue() {

            @Override
            public String getName() {
                return id;
            }

            @Override
            public List<String> getSynonyms() {
                return List.of(id.toLowerCase(), v.toLowerCase());
            }
        };
    }

    @Override
    public Set<NCValue> load(NCElement owner) {
        return api.getAllPlanets().entrySet().stream().map(p -> mkValue(p.getKey(), p.getValue())).collect(Collectors.toSet());
    }
}
