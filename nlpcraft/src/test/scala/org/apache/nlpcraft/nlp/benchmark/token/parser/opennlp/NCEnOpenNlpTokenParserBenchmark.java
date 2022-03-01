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

package org.apache.nlpcraft.nlp.benchmark.token.parser.opennlp;

import org.apache.nlpcraft.NCRequest;
import org.apache.nlpcraft.internal.util.NCResourceReader;
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser;
import org.apache.nlpcraft.nlp.util.NCTestRequest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 10)
@Measurement(iterations = 5, time = 5)
public class NCEnOpenNlpTokenParserBenchmark {
    @State(Scope.Thread)
    public static class NCBenchmarkAdapterState {
        final NCRequest request = NCTestRequest.apply(
            "I am developing an integrated Benchmarking into an application, I want to use JMH as my framework."
        );
    }

    private NCOpenNLPTokenParser parser;

    @Setup
    public void setUp() {
        NCResourceReader reader = new NCResourceReader();

        parser = new NCOpenNLPTokenParser(reader.getPath("opennlp/en-token.bin"));
    }

    @Benchmark
    public void testParse(Blackhole bh, NCBenchmarkAdapterState state) {
        bh.consume(parser.tokenize(state.request.getText()));
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(NCEnOpenNlpTokenParserBenchmark.class.getSimpleName()).shouldFailOnError(true).build()).run();
    }
}
