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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * TODO:
 */
public class NCModelPipelineBuilder {
    private final NCTokenParser tokParser;
    private final List<NCTokenEnricher> tokEnrichers = new ArrayList<>();
    private final List<NCEntityEnricher> entEnrichers = new ArrayList<>();
    private final List<NCEntityParser> entParsers = new ArrayList<>();
    private final List<NCTokenValidator> tokenValidators = new ArrayList<>();
    private final List<NCEntityValidator> entityValidators = new ArrayList<>();
    private final List<NCVariantValidator> variantValidators = new ArrayList<>();

    /**
     * TODO:
     * 
     * @param id
     * @param name
     * @param version
     */
    public NCModelPipelineBuilder(NCTokenParser tokParser, List<NCEntityParser> entParsers) {
        // TODO: error texts.
        Objects.requireNonNull(tokParser, "Token parser cannot be null.");
        Objects.requireNonNull(entParsers, "Entity parsers cannot be null.");
        if (entParsers.isEmpty())
            throw new IllegalArgumentException("At least one entity parser must be defined.");

        this.tokParser = tokParser;
        this.entParsers.addAll(entParsers);
    }

    /**
     *
     * @param tokParser
     * @param entParsers
     */
    public NCModelPipelineBuilder(NCTokenParser tokParser, NCEntityParser... entParsers) {
        this(tokParser, Arrays.asList(entParsers));
    }


    /**
     * @param tokEnrichers
     * @return
     */
    public NCModelPipelineBuilder withTokenEnrichers(List<NCTokenEnricher> tokEnrichers) {
        // TODO: error texts.
        Objects.requireNonNull(tokEnrichers, "Enrichers cannot be null.");
        tokEnrichers.forEach(p -> Objects.requireNonNull(p, "Enrichers cannot be null."));

        this.tokEnrichers.addAll(tokEnrichers);

        return this;
    }

    /**
     * @param tokEnricher
     * @return
     */
    public NCModelPipelineBuilder withTokenEnricher(NCTokenEnricher tokEnricher) {
        // TODO: error texts.
        Objects.requireNonNull(tokEnricher, "Enricher cannot be null.");

        this.tokEnrichers.add(tokEnricher);

        return this;
    }

    /**
     * @param entEnrichers
     * @return
     */
    public NCModelPipelineBuilder withEntityEnrichers(List<NCEntityEnricher> entEnrichers) {
        // TODO: error texts.
        Objects.requireNonNull(entEnrichers, "Enrichers cannot be null.");
        entEnrichers.forEach(p -> Objects.requireNonNull(p, "Enrichers cannot be null."));

        this.entEnrichers.addAll(entEnrichers);

        return this;
    }

    /**
     * @param entEnricher
     * @return
     */
    public NCModelPipelineBuilder withEntityEnricher(NCEntityEnricher entEnricher) {
        // TODO: error texts.
        Objects.requireNonNull(entEnricher, "Enricher cannot be null.");

        this.entEnrichers.add(entEnricher);

        return this;
    }

    /**
     * @param entParsers
     * @return
     */
    public NCModelPipelineBuilder withEntityParsers(List<NCEntityParser> entParsers) {
        // TODO: error texts.
        Objects.requireNonNull(entParsers, "Parsers cannot be null.");
        entParsers.forEach(p -> Objects.requireNonNull(p, "Parsers cannot be null."));

        this.entParsers.addAll(entParsers);

        return this;
    }

    /**
     * @param entParser
     * @return
     */
    public NCModelPipelineBuilder withEntityParser(NCEntityParser entParser) {
        // TODO: error texts.
        Objects.requireNonNull(entParser, "Parser cannot be null.");

        this.entParsers.add(entParser);

        return this;
    }

    /**
     * @param tokenValidators
     * @return
     */
    public NCModelPipelineBuilder withTokenValidators(List<NCTokenValidator> tokenValidators) {
        // TODO: error texts.
        Objects.requireNonNull(tokenValidators, "Validators cannot be null.");
        tokenValidators.forEach(p -> Objects.requireNonNull(p, "Validators cannot be null."));

        this.tokenValidators.addAll(tokenValidators);

        return this;
    }

    /**
     * @param tokenValidator
     * @return
     */
    public NCModelPipelineBuilder withTokenValidator(NCTokenValidator tokenValidator) {
        // TODO: error texts.
        Objects.requireNonNull(tokenValidator, "Validator cannot be null.");

        this.tokenValidators.add(tokenValidator);

        return this;
    }

    /**
     * @param entityValidators
     * @return
     */
    public NCModelPipelineBuilder withEntityValidators(List<NCEntityValidator> entityValidators) {
        // TODO: error texts.
        Objects.requireNonNull(entityValidators, "Validators cannot be null.");
        entityValidators.forEach(p -> Objects.requireNonNull(p, "Validators cannot be null."));

        this.entityValidators.addAll(entityValidators);

        return this;
    }

    /**
     * @param entityValidator
     * @return
     */
    public NCModelPipelineBuilder withEntityValidator(NCEntityValidator entityValidator) {
        Objects.requireNonNull(entityValidator, "Validators cannot be null.");

        this.entityValidators.add(entityValidator);

        return this;
    }

    /**
     * @param variantValidators
     * @return
     */
    public NCModelPipelineBuilder withVariantValidators(List<NCVariantValidator> variantValidators) {
        Objects.requireNonNull(variantValidators, "Validators cannot be null.");
        variantValidators.forEach(p -> Objects.requireNonNull(p, "Validators cannot be null."));

        this.variantValidators.addAll(variantValidators);

        return this;
    }

    /**
     * @param variantValidator
     * @return
     */
    public NCModelPipelineBuilder withVariantValidator(NCVariantValidator variantValidator) {
        Objects.requireNonNull(variantValidator, "Validator cannot be null.");

        this.variantValidators.add(variantValidator);

        return this;
    }

    /**
     * @return
     */
    public NCModelPipeline build() {
        return new NCModelPipeline() {
            @Override
            public NCTokenParser getTokenParser() {
                return tokParser;
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
            public List<NCEntityParser> getEntityParsers() {
                return entParsers;
            }

            @Override
            public List<NCTokenValidator> getTokenValidators() {
                return tokenValidators;
            }

            @Override
            public List<NCEntityValidator> getEntityValidators() {
                return entityValidators;
            }

            @Override
            public List<NCVariantValidator> getVariantValidators() {
                return variantValidators;
            }
        };
    }
}
