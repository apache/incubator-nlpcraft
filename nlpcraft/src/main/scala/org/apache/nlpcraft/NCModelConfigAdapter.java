/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft;

import java.util.*;

/**
 *
 */
// TODO: validation for constructor and all setters.
public class NCModelConfigAdapter extends NCParameterizedAdapter implements NCModelConfig {
    private final String id;
    private final String name;
    private final String version;
    private final NCTokenParser tokParser;

    private List<NCTokenEnricher> tokenEnrichers;
    private List<NCEntityEnricher> entityEnrichers;
    private List<NCEntityParser> entityParsers;

    public NCModelConfigAdapter(String id, String name, String version, NCTokenParser tokParser) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.tokParser = tokParser;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public List<NCTokenEnricher> getTokenEnrichers() {
        return tokenEnrichers;
    }

    @Override
    public List<NCEntityEnricher> getEntityEnrichers() {
        return entityEnrichers;
    }

    @Override
    public NCTokenParser getTokenParser() {
        return tokParser;
    }

    @Override
    public List<NCEntityParser> getEntityParsers() {
        return entityParsers;
    }

    public void setTokenEnrichers(List<NCTokenEnricher> tokenEnrichers) {
        this.tokenEnrichers = tokenEnrichers;
    }

    public void setEntityEnrichers(List<NCEntityEnricher> entityEnrichers) {
        this.entityEnrichers = entityEnrichers;
    }

    public void setEntityParsers(List<NCEntityParser> entityParsers) {
        this.entityParsers = entityParsers;
    }
}
