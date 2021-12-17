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

package org.apache.nlpcraft.internal.nlp.token.parser.opennlp;

import org.apache.nlpcraft.*;
import org.apache.nlpcraft.internal.nlp.token.parser.opennlp.impl.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;


/*
 * Models can be downloaded from the following resources:
 *  - tokenizer: http://opennlp.sourceforge.net/models-1.5/en-token.bin
 *  - tagger: http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin
 *  - lemmatizer: https://raw.githubusercontent.com/richardwilly98/elasticsearch-opennlp-auto-tagging/master/src/main/resources/models/en-lemmatizer.dict
 */

/**
 *
 */
public class NCOpenNlpTokenParser implements NCTokenParser {
    private final NCOpenNlpImpl impl;

    /**
     *
     * @param tokMdl
     * @param posMdl
     * @param lemmaDic
     * @throws NCException
     */
    public NCOpenNlpTokenParser(File tokMdl, File posMdl, File lemmaDic) {
        try {
            impl = new NCOpenNlpImpl(
                new BufferedInputStream(new FileInputStream(tokMdl)),
                new BufferedInputStream(new FileInputStream(posMdl)),
                new BufferedInputStream(new FileInputStream(lemmaDic))
            );
        }
        catch (Exception e) {
            throw new NCException("Failed to create OpenNLP token parser.", e);
        }
    }

    /**
     *
     * @param tokMdlSrc Local filesystem path, resources file path or URL for OpenNLP tokenizer model.
     * @param posMdlSrc Local filesystem path, resources file path or URL for OpenNLP tagger model.
     * @param lemmaDicSrc Local filesystem path, resources file path or URL for OpenNLP lemmatizer dictionary.
     * @throws NCException
     */
    public NCOpenNlpTokenParser(String tokMdlSrc, String posMdlSrc, String lemmaDicSrc) {
        try {
            impl = NCOpenNlpImpl.apply(tokMdlSrc, posMdlSrc, lemmaDicSrc);
        }
        catch (Exception e) {
            throw new NCException("Failed to create OpenNLP token parser.", e);
        }
    }

    @Override
    public List<NCToken> parse(NCRequest req) {
        assert impl != null;
        return impl.parse(req);
    }

    /**
     *
     * @return
     */
    public List<String> getAdditionalStopWords() {
        return impl.getAdditionalStopWords();
    }

    /**
     *
     * @param addStopWords
     */
    public void setAdditionalStopWords(List<String> addStopWords) {
        impl.setAdditionalStopWords(addStopWords);
    }

    /**
     *
     * @return
     */
    public List<String> getExcludedStopWords() {
        return impl.getExcludedStopWords();
    }

    /**
     *
     * @param exclStopWords
     */
    public void setExcludedStopWords(List<String> exclStopWords) {
        impl.setExcludedStopWords(exclStopWords);
    }
}
