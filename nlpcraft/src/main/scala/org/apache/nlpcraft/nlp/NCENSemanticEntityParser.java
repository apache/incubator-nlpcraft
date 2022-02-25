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

import org.apache.nlpcraft.NCTokenParser;
import org.apache.nlpcraft.internal.util.NCResourceReader;
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticElement;
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser;
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticStemmer;
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.en.NCEnSemanticPorterStemmer;
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class NCENSemanticEntityParser extends NCSemanticEntityParser {
    private static final NCResourceReader reader = new NCResourceReader();

    private static NCSemanticStemmer mkStemmer() {
        return new NCEnSemanticPorterStemmer();
    }

    private static NCOpenNLPTokenParser mkParser() {
        return new NCOpenNLPTokenParser(
            reader.getPath("opennlp/en-token.bin"),
            reader.getPath("opennlp/en-pos-maxent.bin"),
            reader.getPath("opennlp/en-lemmatizer.dict")
        );
    }

    /**
     *
     * @param elms
     */
    public NCENSemanticEntityParser(List<NCSemanticElement> elms) {
        super(mkStemmer(), mkParser(), elms);
    }

    /**
     *
     * @param macros
     * @param elms
     */
    public NCENSemanticEntityParser(Map<String, String> macros, List<NCSemanticElement> elms) {
        super(mkStemmer(), mkParser(), macros, elms);
    }

    /**
     *
     * @param src
     */
    public NCENSemanticEntityParser(String src) {
        super(mkStemmer(), mkParser(), src);
    }
}
