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

import opennlp.tools.stemmer.PorterStemmer;
import org.apache.nlpcraft.internal.util.NCResourceReader;
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticElement;
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser;
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticStemmer;
import org.apache.nlpcraft.nlp.token.enricher.NCEnBracketsTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.NCEnDictionaryTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.NCEnQuotesTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.NCEnStopWordsTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.NCOpenNLPLemmaPosTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.NCEnSwearWordsTokenEnricher;
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class NCPipelineBuilder {
    private NCTokenParser tokParser;
    private final List<NCTokenEnricher> tokEnrichers = new ArrayList<>();
    private final List<NCEntityEnricher> entEnrichers = new ArrayList<>();
    private final List<NCEntityParser> entParsers = new ArrayList<>();
    private final List<NCTokenValidator> tokVals = new ArrayList<>();
    private final List<NCEntityValidator> entVals = new ArrayList<>();
    private final List<NCEntityMapper> entMappers = new ArrayList<>();
    private Optional<NCVariantFilter> varFilter = Optional.empty();

    /**
     *
     * @return
     */
    private static NCSemanticStemmer mkEnStemmer() {
        return new NCSemanticStemmer() {
            private final PorterStemmer ps = new PorterStemmer();

            @Override
            public synchronized String stem(String txt) {
                return ps.stem(txt);
            }
        };
    }

    /**
     *
     * @return
     */
    private NCOpenNLPTokenParser mkEnOpenNLPTokenParser() {
        return new NCOpenNLPTokenParser(NCResourceReader.getPath("opennlp/en-token.bin"));
    }


    /**
     * @param tokEnrichers
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withTokenEnrichers(List<NCTokenEnricher> tokEnrichers) {
        Objects.requireNonNull(tokEnrichers, "List of token enrichers cannot be null.");
        tokEnrichers.forEach(p -> Objects.requireNonNull(p, "Token enricher cannot be null."));

        this.tokEnrichers.addAll(tokEnrichers);

        return this;
    }

    /**
     * @param tokEnricher
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withTokenEnricher(NCTokenEnricher tokEnricher) {
        Objects.requireNonNull(tokEnricher, "Token enricher cannot be null.");

        this.tokEnrichers.add(tokEnricher);

        return this;
    }

    /**
     * @param entEnrichers
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntityEnrichers(List<NCEntityEnricher> entEnrichers) {
        Objects.requireNonNull(entEnrichers, "List of entity enrichers cannot be null.");
        entEnrichers.forEach(p -> Objects.requireNonNull(p, "Entity enrichers cannot be null."));

        this.entEnrichers.addAll(entEnrichers);

        return this;
    }

    /**
     * @param entEnricher
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntityEnricher(NCEntityEnricher entEnricher) {
        Objects.requireNonNull(entEnricher, "Entity enricher cannot be null.");

        this.entEnrichers.add(entEnricher);

        return this;
    }

    /**
     * @param entParsers
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntityParsers(List<NCEntityParser> entParsers) {
        Objects.requireNonNull(entParsers, "List of entity parsers cannot be null.");
        entParsers.forEach(p -> Objects.requireNonNull(p, "Entity parser cannot be null."));

        this.entParsers.addAll(entParsers);

        return this;
    }

    /**
     * @param entParser
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntityParser(NCEntityParser entParser) {
        Objects.requireNonNull(entParser, "Entity parser cannot be null.");

        this.entParsers.add(entParser);

        return this;
    }

    /**
     * @param tokVals
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withTokenValidators(List<NCTokenValidator> tokVals) {
        Objects.requireNonNull(tokVals, "List of token validators cannot be null.");
        tokVals.forEach(p -> Objects.requireNonNull(p, "Token validator cannot be null."));

        this.tokVals.addAll(tokVals);

        return this;
    }

    /**
     * @param tokVal
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withTokenValidator(NCTokenValidator tokVal) {
        Objects.requireNonNull(tokVal, "Token validator cannot be null.");

        this.tokVals.add(tokVal);

        return this;
    }

    /**
     * @param entVals
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntityValidators(List<NCEntityValidator> entVals) {
        Objects.requireNonNull(entVals, "List of entity validators cannot be null.");
        entVals.forEach(p -> Objects.requireNonNull(p, "Entity validators cannot be null."));

        this.entVals.addAll(entVals);

        return this;
    }

    /**
     * @param entVal
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntityValidator(NCEntityValidator entVal) {
        Objects.requireNonNull(entVal, "Entity validator cannot be null.");

        this.entVals.add(entVal);

        return this;
    }

    /**
     * @param varFilter
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withVariantFilter(NCVariantFilter varFilter) {
        this.varFilter = Optional.of(varFilter);

        return this;
    }

    /**
     *
     * @param tokParser
     * @return
     */
    public NCPipelineBuilder withTokenParser(NCTokenParser tokParser) {
        Objects.requireNonNull(tokParser, "Token parser cannot be null.");

        this.tokParser = tokParser;

        return this;
    }

    /**
     *
     * @param entMappers
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntityMappers(List<NCEntityMapper> entMappers) {
        Objects.requireNonNull(entMappers, "List of entity mappers cannot be null.");
        entMappers.forEach(p -> Objects.requireNonNull(p, "Entity mapper cannot be null."));

        this.entMappers.addAll(entMappers);

        return this;
    }

    /**
     * @param entMapper
     * @return This instance for call chaining.
     */
    public NCPipelineBuilder withEntitMapper(NCEntityMapper entMapper) {
        Objects.requireNonNull(entMapper, "Entity mapper cannot be null.");

        this.entMappers.add(entMapper);

        return this;
    }
    /**
     *
     */
    private void setEnComponents() {
        tokParser = mkEnOpenNLPTokenParser();

        tokEnrichers.add(new NCOpenNLPLemmaPosTokenEnricher(
            NCResourceReader.getPath("opennlp/en-pos-maxent.bin"),
            NCResourceReader.getPath("opennlp/en-lemmatizer.dict")
        ));
        tokEnrichers.add(new NCEnStopWordsTokenEnricher());

        tokEnrichers.add(new NCEnSwearWordsTokenEnricher(NCResourceReader.getPath("badfilter/swear_words.txt")));
        tokEnrichers.add(new NCEnQuotesTokenEnricher());
        tokEnrichers.add(new NCEnDictionaryTokenEnricher());
        tokEnrichers.add(new NCEnBracketsTokenEnricher());
    }

    /**
     *
     * @param lang
     * @param macros
     * @param elms
     * @return
     */
    public NCPipelineBuilder withSemantic(String lang, Map<String, String> macros, List<NCSemanticElement> elms) {
        Objects.requireNonNull(lang, "Language cannot be null.");
        Objects.requireNonNull(elms, "Model elements cannot be null.");
        if (elms.isEmpty()) throw new IllegalArgumentException("Model elements cannot be empty.");

        switch (lang.toUpperCase()) {
            case "EN":
                setEnComponents();

                this.entParsers.add(new NCSemanticEntityParser(mkEnStemmer(), mkEnOpenNLPTokenParser(), macros, elms));

                break;

            default:
                throw new IllegalArgumentException("Unsupported language: " + lang);
        }

        return this;
    }

    /**
     *
     * @param lang
     * @param elms
     * @return
     */
    public NCPipelineBuilder withSemantic(String lang, List<NCSemanticElement> elms) {
        return withSemantic(lang, null, elms);
    }

    /**
     *
     * @param lang
     * @param src
     * @return
     */
    public NCPipelineBuilder withSemantic(String lang, String src) {
        Objects.requireNonNull(lang, "Language cannot be null.");
        Objects.requireNonNull(src, "Model source cannot be null.");

        switch (lang.toUpperCase()) {
            case "EN":
                setEnComponents();

                this.entParsers.add(new NCSemanticEntityParser(mkEnStemmer(), mkEnOpenNLPTokenParser(), src));

                break;

            default:
                throw new IllegalArgumentException("Unsupported language: " + lang);
        }

        return this;
    }


    /**
     * @return
     */
    public NCPipeline build() {
        Objects.requireNonNull(tokParser, "Token parser cannot be null.");

        return new NCPipeline() {
            @Override public NCTokenParser getTokenParser() {
                return tokParser;
            }
            @Override public List<NCTokenEnricher> getTokenEnrichers() {
                return tokEnrichers;
            }
            @Override public List<NCEntityEnricher> getEntityEnrichers() {
                return entEnrichers;
            }
            @Override public List<NCEntityParser> getEntityParsers() {
                return entParsers;
            }
            @Override public List<NCTokenValidator> getTokenValidators() {
                return tokVals;
            }
            @Override public List<NCEntityValidator> getEntityValidators() {
                return entVals;
            }
            @Override public Optional<NCVariantFilter> getVariantFilter() {
                return varFilter;
            }

            @Override
            public List<NCEntityMapper> getEntityMappers() {
                return entMappers;
            }
        };
    }
}
