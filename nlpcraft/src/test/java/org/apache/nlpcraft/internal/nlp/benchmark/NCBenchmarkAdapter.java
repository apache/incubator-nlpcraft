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

package org.apache.nlpcraft.internal.nlp.benchmark;

import org.apache.nlpcraft.NCRequest;
import org.apache.nlpcraft.internal.nlp.util.NCTestRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
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
@Disabled
public class NCBenchmarkAdapter {
    @State(Scope.Thread)
    public static class NCBenchmarkAdapterState {
        public NCRequest request = NCTestRequest.apply(
            "I am developing an integrated Benchmarking into an application, I want to use JMH as my framework."
        );
    }

    /**
     *
     * @param args
     * @throws RunnerException
     */
    @Test
    public void benchmark() throws RunnerException {
        new Runner(new OptionsBuilder().include(this.getClass().getSimpleName()).build()).run();
    }
}
