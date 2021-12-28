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
public class NCModelConfigAdapter extends NCPropertyMapAdapter implements NCModelConfig {
    private final String id;
    private final String name;
    private final String version;

    private List<NCTokenParser> tokParsers = new ArrayList<>();
    private List<NCTokenEnricher> tokEnrichers = new ArrayList<>();
    private List<NCEntityEnricher> entEnrichers = new ArrayList<>();
    private List<NCEntityParser> entParsers = new ArrayList<>();

    /**
     *
     * @param id
     * @param name
     * @param version
     * @param tokParser
     */
    public NCModelConfigAdapter(String id, String name, String version, NCTokenParser tokParser, NCEntityParser entParser) {
        Objects.requireNonNull(tokParser, "Token parser cannot be null.");
        Objects.requireNonNull(entParser, "Entity parser cannot be null.");

        this.id = id;
        this.name = name;
        this.version = version;

        tokParsers.add(tokParser);
        entParsers.add(entParser);
    }

    /**
     *
     * @param tokParser
     */
    public void addTokenParser(NCTokenParser tokParser) {
        Objects.requireNonNull(tokParser, "Token parser cannot be null.");

        tokParsers.add(tokParser);
    }

    /**
     *
     * @param entParser
     */
    public void addEntityParser(NCEntityParser entParser) {
        Objects.requireNonNull(entParser, "Entity parser cannot be null.");

        entParsers.add(entParser);
    }

    /**
     *
     * @param tokEnricher
     */
    public void addTokenEnricher(NCTokenEnricher tokEnricher) {
        Objects.requireNonNull(tokEnricher, "Token enricher cannot be null.");

        tokEnrichers.add(tokEnricher);
    }

    /**
     *
     * @param entEnricher
     */
    public void addEntityEnricher(NCEntityEnricher entEnricher) {
        Objects.requireNonNull(entEnricher, "Entity enricher cannot be null.");

        entEnrichers.add(entEnricher);
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
        return tokEnrichers;
    }

    @Override
    public List<NCEntityEnricher> getEntityEnrichers() {
        return entEnrichers;
    }

    @Override
    public List<NCTokenParser> getTokenParsers() {
        return tokParsers;
    }

    @Override
    public List<NCEntityParser> getEntityParsers() {
        return entParsers;
    }
}
