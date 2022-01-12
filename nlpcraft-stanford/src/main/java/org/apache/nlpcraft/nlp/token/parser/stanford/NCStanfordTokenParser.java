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

package org.apache.nlpcraft.nlp.token.parser.stanford;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.nlpcraft.NCToken;
import org.apache.nlpcraft.NCTokenParser;
import org.apache.nlpcraft.nlp.token.parser.stanford.impl.NCStanfordNlpImpl;

import java.util.List;
import java.util.Objects;

/**
 * TODO:
 */
public class NCStanfordTokenParser implements NCTokenParser {
    private final NCStanfordNlpImpl impl;

    /**
     * TODO: add javadoc based on comments below.
     *
     * Requires configured StanfordCoreNLP instance.
     * Example:
     *   Properties props = new Properties()
     *   props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
     *   StanfordCoreNLP stanford = new StanfordCoreNLP(props)
     * Look at https://stanfordnlp.github.io/CoreNLP/ner.html#java-api-example for more details.
     *
     * @param stanford
     */
    public NCStanfordTokenParser(StanfordCoreNLP stanford) {
        // TODO: error texts.
        Objects.requireNonNull(stanford, "Stanford instance cannot be null.");

        impl = new NCStanfordNlpImpl(stanford);
    }

    @Override
    public List<NCToken> tokenize(String text) {
        return impl.tokenize(text);
    }
}
