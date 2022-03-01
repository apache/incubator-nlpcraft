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
import org.apache.nlpcraft.nlp.token.enricher.en.*;
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default EN implementation based on Open Nlp token parser, and set of built token enrichers including stopword enricher.
 * Also at least one entity parser must be defined.
 */
public class NCENDefaultPipeline implements NCModelPipeline {
    private static final NCResourceReader reader = new NCResourceReader();

    private final NCTokenParser tokParser = new NCOpenNLPTokenParser(reader.getPath("opennlp/en-token.bin"));

    private List<NCTokenEnricher> tokenEnrichers = Arrays.asList(
        new NCLemmaPosTokenEnricher(
            reader.getPath("opennlp/en-pos-maxent.bin"),
            reader.getPath("opennlp/en-lemmatizer.dict")
        ),
        new NCStopWordsTokenEnricher(),
        new NСSwearWordsTokenEnricher(reader.getPath("badfilter/swear_words.txt")),
        new NCQuotesTokenEnricher(),
        new NCDictionaryTokenEnricher(),
        new NCBracketsTokenEnricher()
    );

    private final List<NCEntityParser> entParsers;

    /**
     *
     * @param entParsers
     */
    public NCENDefaultPipeline(List<NCEntityParser> entParsers) {
        this.entParsers = entParsers;
    }

    /**
     *
     * @param parser
     */
    public NCENDefaultPipeline(NCEntityParser... parsers) {
        this.entParsers = Arrays.asList(parsers);
    }

    @Override
    public NCTokenParser getTokenParser() {
        return tokParser;
    }

    @Override
    public List<NCEntityParser> getEntityParsers() {
        return entParsers;
    }

    @Override
    public List<NCTokenEnricher> getTokenEnrichers() {
        return tokenEnrichers;
    }
}
