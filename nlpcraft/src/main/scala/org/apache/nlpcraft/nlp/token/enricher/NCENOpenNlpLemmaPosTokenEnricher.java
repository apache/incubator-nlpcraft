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

package org.apache.nlpcraft.nlp.token.enricher;

import org.apache.nlpcraft.internal.util.NCResourceReader;
import org.apache.nlpcraft.nlp.token.enricher.NCOpenNlpLemmaPosTokenEnricher;

/**
 * TODO: enriches with <code>lemma</code> and <code>pos</code> properties.
 *
 * Models can be downloaded from the following resources:
 *  - tagger: http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin
 *  - lemmatizer: https://raw.githubusercontent.com/richardwilly98/elasticsearch-opennlp-auto-tagging/master/src/main/resources/models/en-lemmatizer.dict
 */
public class NCENOpenNlpLemmaPosTokenEnricher extends NCOpenNlpLemmaPosTokenEnricher {
    /**
     *
     */
    public NCENOpenNlpLemmaPosTokenEnricher() {
        super(
            NCResourceReader.getPath("opennlp/en-pos-maxent.bin"),
            NCResourceReader.getPath("opennlp/en-lemmatizer.dict")
        );
    }
}
