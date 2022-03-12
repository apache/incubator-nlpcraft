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

package org.apache.nlpcraft.nlp.entity.parser;

import opennlp.tools.stemmer.PorterStemmer;
import org.apache.nlpcraft.NCTokenParser;
import org.apache.nlpcraft.nlp.token.parser.NCENOpenNLPTokenParser;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class NCEnSemanticEntityParser extends NCSemanticEntityParser {
    private static final NCSemanticStemmer porterStemmer = new NCSemanticStemmer() {
        private final PorterStemmer stemmerImpl = new PorterStemmer();

        @Override
        public synchronized String stem(String s) {
            return stemmerImpl.stem(s.toLowerCase());
        }
    };

    private static final NCTokenParser opennlpParser = new NCENOpenNLPTokenParser();

    public NCEnSemanticEntityParser(List<NCSemanticElement> elms) {
        super(porterStemmer, opennlpParser, elms);
    }

    public NCEnSemanticEntityParser(Map<String, String> macros, List<NCSemanticElement> elms) {
        super(porterStemmer, opennlpParser, macros, elms);
    }

    public NCEnSemanticEntityParser(String src) {
        super(porterStemmer, opennlpParser, src);
    }
}
