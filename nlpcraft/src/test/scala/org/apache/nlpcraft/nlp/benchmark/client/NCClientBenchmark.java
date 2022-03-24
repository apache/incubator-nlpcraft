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

package org.apache.nlpcraft.nlp.benchmark.client;

import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.NullConfiguration;
import org.apache.nlpcraft.NCEntity;
import org.apache.nlpcraft.NCIntent;
import org.apache.nlpcraft.NCIntentTerm;
import org.apache.nlpcraft.NCModel;
import org.apache.nlpcraft.NCModelAdapter;
import org.apache.nlpcraft.NCModelClient;
import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.NCModelPipelineBuilder;
import org.apache.nlpcraft.NCRequest;
import org.apache.nlpcraft.NCResult;
import org.apache.nlpcraft.NCResultType;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
public class NCClientBenchmark {
    // It disables logging. Should be here to be applied before logger initialized.
    static {
        Configurator.initialize(new NullConfiguration());
    }

    @State(Scope.Thread)
    public static class NCBenchmarkAdapterState {
        final NCRequest request = NCTestRequest.apply(
            "I want to use JMH framework for test the system performance, so - lights on at second floor kitchen!"
        );
    }

    private NCModelClient client;

    @Setup
    public void setUp() {
        // Models contains all EN components, including
        //  - openNLP based tokens parser,
        //  - stopword and lemma tokens enrichers,
        //  - set of unused here tokens enrichers (they just added by default in 'en' builder profile) and
        //  - one semantic entities parser.
        NCModel mdl =
            new NCModelAdapter(
                new NCModelConfig("testId", "test", "1.0", "Test description", "Test origin"),
                new NCModelPipelineBuilder().withSemantic("en", "models/lightswitch_model.yaml").build()
            ) {
                private final NCResult res = new NCResult("OK", NCResultType.ASK_RESULT);

                @NCIntent("intent=ls term(act)={# == 'ls:on'} term(loc)={# == 'ls:loc'}*")
                public NCResult onMatch(@NCIntentTerm("act") NCEntity act, @NCIntentTerm("loc")List<NCEntity> locs) {
                    return res;
                }
            };

        client = new NCModelClient(mdl);
    }

    @TearDown
    public void tearDown() {
        if (client != null)
            client.close();
    }

    @Benchmark
    public void testParse(Blackhole bh, NCBenchmarkAdapterState state) {
        bh.consume(client.ask(state.request.getText(), null, state.request.getUserId()));
    }

    /**
     *
     * @param args
     * @throws RunnerException
     */
    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().
            include(NCClientBenchmark.class.getSimpleName()).
            shouldFailOnError(true).
            resultFormat(ResultFormatType.JSON).
            result("nlpcraft/src/test/scala/org/apache/nlpcraft/nlp/benchmark/client/" + DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()) + ".json").
            build()
        ).run();
    }
}
