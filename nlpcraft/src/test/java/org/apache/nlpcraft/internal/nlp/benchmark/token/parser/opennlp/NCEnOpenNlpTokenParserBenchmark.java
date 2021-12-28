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

package org.apache.nlpcraft.internal.nlp.benchmark.token.parser.opennlp;

import org.apache.nlpcraft.internal.nlp.benchmark.NCBenchmarkAdapter;
import org.apache.nlpcraft.internal.nlp.token.parser.opennlp.NCEnOpenNlpTokenParser;
import org.junit.jupiter.api.Disabled;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;


/**
 *
 */
@Disabled
public class NCEnOpenNlpTokenParserBenchmark extends NCBenchmarkAdapter  {
    private NCEnOpenNlpTokenParser parser;

    @Setup
    public void setUp() {
        parser = prepareParser();
    }

    @Benchmark
    public void testInitialize(Blackhole bh) {
        bh.consume(prepareParser());
    }

    @Benchmark
    public void testParse(Blackhole bh, NCBenchmarkAdapterState state) {
        bh.consume(parser.parse(state.request, null));
    }

    /**
     *
     * @return
     */
    private static NCEnOpenNlpTokenParser prepareParser() {
        NCEnOpenNlpTokenParser p = new NCEnOpenNlpTokenParser(
            "opennlp/en-token.bin",
            "opennlp/en-pos-maxent.bin",
            "opennlp/en-lemmatizer.dict"
        );

        p.start(null); // TODO: fix it.

        return p;
    }
}
