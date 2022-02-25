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

package org.apache.nlpcraft.nlp;

import org.apache.nlpcraft.NCEntityParser;
import org.apache.nlpcraft.NCModelPipeline;
import org.apache.nlpcraft.NCTokenEnricher;
import org.apache.nlpcraft.NCTokenParser;
import org.apache.nlpcraft.internal.util.NCResourceReader;
import org.apache.nlpcraft.nlp.token.enricher.en.NCBracketsTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.en.NCDictionaryTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.en.NCQuotesTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.en.NCStopWordsTokenEnricher;
import org.apache.nlpcraft.nlp.token.enricher.en.NСSwearWordsTokenEnricher;
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class NCENDefaultPipeline implements NCModelPipeline {
    private static final NCResourceReader reader = new NCResourceReader();

    private final NCTokenParser tp = new NCOpenNLPTokenParser(
        reader.getPath("opennlp/en-token.bin"),
        reader.getPath("opennlp/en-pos-maxent.bin"),
        reader.getPath("opennlp/en-lemmatizer.dict")
    );

    private List<NCTokenEnricher> tokenEnrichers = Arrays.asList(
        new NCStopWordsTokenEnricher(),
        new NСSwearWordsTokenEnricher(reader.getPath("badfilter/swear_words.txt")),
        new NCQuotesTokenEnricher(),
        new NCDictionaryTokenEnricher(),
        new NCBracketsTokenEnricher()

    );

    private final List<NCEntityParser> parsers;

    /**
     *
     * @param parsers
     */
    public NCENDefaultPipeline(List<NCEntityParser> parsers) {
        this.parsers = parsers;
    }

    /**
     *
     * @param parser
     */
    public NCENDefaultPipeline(NCEntityParser parser) {
        this.parsers = Collections.singletonList(parser);
    }

    @Override
    public NCTokenParser getTokenParser() {
        return tp;
    }

    @Override
    public List<NCEntityParser> getEntityParsers() {
        return parsers;
    }

    @Override
    public List<NCTokenEnricher> getTokenEnrichers() {
        return tokenEnrichers;
    }
}
