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

package org.apache.nlpcraft.nlp.entity.parser.stanford;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.nlpcraft.NCEntity;
import org.apache.nlpcraft.NCEntityParser;
import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.NCRequest;
import org.apache.nlpcraft.NCToken;
import org.apache.nlpcraft.nlp.entity.parser.stanford.impl.NCStanfordNLPEntityParserImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * Generates entities with
 *  - ID `stanford:{name}` where 'name' is element name from configured StanfordCoreNLP instance, from supported set
 *  - property `stanford:{name}:confidence`, where confidence is double value between 0 and 1. Optional.
 *  - property `stanford:{name}:nne`, where nne is normalized value. Optional.
 */
public class NCStanfordNLPEntityParser implements NCEntityParser {
    private final NCStanfordNLPEntityParserImpl impl;

    /**
     *
     * Requires configured StanfordCoreNLP instance.
     * Example:
     *   Properties props = new Properties()
     *   props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
     *   StanfordCoreNLP stanford = new StanfordCoreNLP(props)
     * Look at https://stanfordnlp.github.io/CoreNLP/ner.html#java-api-example for more details.
     * @param stanford
     * @param supported
     */
    public NCStanfordNLPEntityParser(StanfordCoreNLP stanford, Set<String> supported) {
        Objects.requireNonNull(stanford, "Stanford instance cannot be null.");
        Objects.requireNonNull(supported, "Supported elements set cannot be null.");

        this.impl = new NCStanfordNLPEntityParserImpl(stanford, supported);
    }

    /**
     *
     * @param stanford
     * @param supported
     */
    public NCStanfordNLPEntityParser(StanfordCoreNLP stanford, String... supported) {
        Objects.requireNonNull(stanford, "Stanford instance cannot be null.");
        Objects.requireNonNull(supported, "Supported element cannot be null.");

        this.impl = new NCStanfordNLPEntityParserImpl(stanford, new HashSet<>(Arrays.asList(supported)));
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
