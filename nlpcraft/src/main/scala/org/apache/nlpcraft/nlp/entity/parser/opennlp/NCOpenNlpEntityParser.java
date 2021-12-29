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

package org.apache.nlpcraft.nlp.entity.parser.opennlp;

import org.apache.nlpcraft.*;
import org.apache.nlpcraft.nlp.entity.parser.opennlp.impl.NCOpenNlpEntityParserImpl;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * TODO
 * Generates entities with
 *  - ID `opennlp:{name}` where 'name' is element model name (from trained file or resource) and
 *  - one property `opennlp:{name}:probability`, where probability is double value between 0 and 1.
 *
 * <p>
 * Models can be download here: http://opennlp.sourceforge.net/models-1.5/ or trained.
 * <p>
 * Component is language independent.
 * <p>
 */
public class NCOpenNlpEntityParser implements NCEntityParser {
    private final NCOpenNlpEntityParserImpl impl;

    /**
     * @param modelSrc
     */
    public NCOpenNlpEntityParser(String modelSrc) {
        Objects.requireNonNull(modelSrc, "Model source cannot be null.");

        this.impl = NCOpenNlpEntityParserImpl.apply(modelSrc);
    }

    /**
     * @param modelFile
     */
    public NCOpenNlpEntityParser(File modelFile) {
        Objects.requireNonNull(modelFile, "Model file cannot be null.");

        this.impl = NCOpenNlpEntityParserImpl.apply(modelFile);
    }

    @Override
    public void start(NCModelConfig cfg) {
        impl.start(cfg);
    }

    @Override
    public void stop() {
        impl.stop();
    }

    @Override
    public List<NCEntity> parse(NCRequest req, NCModelConfig cfg, List<NCToken> toks) {
        return impl.parse(req, cfg, toks);
    }
}
