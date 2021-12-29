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

package org.apache.nlpcraft.nlp.token.enricher.en;

import org.apache.nlpcraft.*;
import org.apache.nlpcraft.nlp.token.enricher.impl.en.NCEnBracketsImpl;

import java.util.List;

/**
 * TODO: enriches with <code>brackets:en</code> property.
 */
public class NCEnBracketsTokenEnricher implements NCTokenEnricher {
    private final NCEnBracketsImpl impl = new NCEnBracketsImpl();

    @Override
    public void start(NCModelConfig cfg) {
        impl.start(cfg);
    }

    @Override
    public void stop() {
        impl.stop();
    }

    @Override
    public void enrich(NCRequest req, NCModelConfig cfg, List<NCToken> toks) {
        assert impl != null;
        impl.enrich(req, cfg, toks);
    }
}
