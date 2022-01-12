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

package org.apache.nlpcraft.nlp.entity.parser.semantic;

import org.apache.nlpcraft.NCEntity;
import org.apache.nlpcraft.NCEntityParser;
import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.NCRequest;
import org.apache.nlpcraft.NCToken;
import org.apache.nlpcraft.NCTokenParser;
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.NCSemanticEntityParserImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class NCSemanticEntityParser implements NCEntityParser {
    private final NCSemanticEntityParserImpl impl;

    /**
     *
     * @param stemmer
     * @param parser
     * @param elems
     */
    public NCSemanticEntityParser(NCSemanticStemmer stemmer, NCTokenParser parser, List<NCSemanticElement> elems) {
        // TODO: error texts.
        Objects.requireNonNull(stemmer, "Stemmer cannot be null.");
        Objects.requireNonNull(parser, "Parser cannot be null.");
        Objects.requireNonNull(elems, "Elements cannot be null.");

        impl = NCSemanticEntityParserImpl.apply(stemmer, parser, Collections.emptyMap(), elems);
    }

    /**
     *
     * @param stemmer
     * @param parser
     * @param macros
     * @param elems
     */
    public NCSemanticEntityParser(
        NCSemanticStemmer stemmer, NCTokenParser parser, Map<String, String> macros, List<NCSemanticElement> elems
    ) {
        // TODO: error texts.
        Objects.requireNonNull(stemmer, "Stemmer cannot be null.");
        Objects.requireNonNull(parser, "Parser cannot be null.");
        Objects.requireNonNull(elems, "Elements cannot be null.");

        impl = NCSemanticEntityParserImpl.apply(stemmer, parser, macros, elems);
    }

    /**
     *
     * @param stemmer
     * @param mdlSrc
     */
    public NCSemanticEntityParser(NCSemanticStemmer stemmer, NCTokenParser parser, String mdlSrc) {
        // TODO: error texts.
        Objects.requireNonNull(stemmer, "Stemmer cannot be null.");
        Objects.requireNonNull(parser, "Parser cannot be null.");
        Objects.requireNonNull(mdlSrc, "Source cannot be null.");

        impl = NCSemanticEntityParserImpl.apply(stemmer, parser, mdlSrc);
    }

    @Override
    public List<NCEntity> parse(NCRequest req, NCModelConfig cfg, List<NCToken> toks) {
        return impl.parse(req, cfg, toks);
    }

    @Override
    public void onStart(NCModelConfig cfg) {
        impl.onStart(cfg);
    }

    @Override
    public void onStop(NCModelConfig cfg) {
        impl.onStop(cfg);
    }
}
