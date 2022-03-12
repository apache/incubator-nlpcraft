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

import org.apache.nlpcraft.NCEntity;
import org.apache.nlpcraft.NCEntityParser;
import org.apache.nlpcraft.NCException;
import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.NCRequest;
import org.apache.nlpcraft.NCToken;
import org.apache.nlpcraft.nlp.entity.parser.impl.NCOpenNLPEntityParserImpl;

import java.util.List;
import java.util.Objects;

/**
 *
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
public class NCOpenNLPEntityParser implements NCEntityParser {
    private final NCOpenNLPEntityParserImpl impl;

    /**
     * @param mdlSrc
     */
    public NCOpenNLPEntityParser(String mdlSrc) {
        Objects.requireNonNull(mdlSrc, "Model source cannot be null.");

        this.impl = new NCOpenNLPEntityParserImpl(java.util.Collections.singletonList(mdlSrc));
    }

    /**
     * @param mdlSrcs
     */
    public NCOpenNLPEntityParser(List<String> mdlSrcs) {
        Objects.requireNonNull(mdlSrcs, "Model sources cannot be null.");
        if (mdlSrcs.size() == 0) throw new NCException("Model sources cannot be empty.");

        this.impl = new NCOpenNLPEntityParserImpl(mdlSrcs);
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
