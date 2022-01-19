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

package org.apache.nlpcraft.nlp.token.parser.opennlp;

import org.apache.nlpcraft.NCException;
import org.apache.nlpcraft.NCToken;
import org.apache.nlpcraft.NCTokenParser;
import org.apache.nlpcraft.nlp.token.parser.opennlp.impl.NCOpenNlpTokenParserImpl;

import java.util.List;
import java.util.Objects;

/*
 *
 * Models can be downloaded from the following resources:
 *  - tokenizer: http://opennlp.sourceforge.net/models-1.5/en-token.bin
 *  - tagger: http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin
 *  - lemmatizer: https://raw.githubusercontent.com/richardwilly98/elasticsearch-opennlp-auto-tagging/master/src/main/resources/models/en-lemmatizer.dict
 */
public class NCOpenNlpTokenParser implements NCTokenParser {
    private final NCOpenNlpTokenParserImpl impl;

    /**
     *
     *
     * @param tokMdlSrc Local filesystem path, resources file path or URL for OpenNLP tokenizer model.
     * @param posMdlSrc Local filesystem path, resources file path or URL for OpenNLP tagger model.
     * @param lemmaDicSrc Local filesystem path, resources file path or URL for OpenNLP lemmatizer dictionary.
     * @throws NCException
     */
    public NCOpenNlpTokenParser(String tokMdlSrc, String posMdlSrc, String lemmaDicSrc) {
        Objects.requireNonNull(tokMdlSrc, "Tokenizer model path cannot be null.");
        Objects.requireNonNull(posMdlSrc, "POS model path cannot be null.");
        Objects.requireNonNull(lemmaDicSrc, "Lemmatizer model path cannot be null.");

        impl = new NCOpenNlpTokenParserImpl(tokMdlSrc, posMdlSrc, lemmaDicSrc);
    }

    @Override
    public List<NCToken> tokenize(String text) {
        return impl.tokenize(text);
    }
}
