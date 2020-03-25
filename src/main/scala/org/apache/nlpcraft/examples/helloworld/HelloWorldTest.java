/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.examples.helloworld;

import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.model.tools.test.NCTestClient;
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder;
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hello World example data model test.
 *
 * @see HelloWorldModel
 */
class HelloWorldTest {
    private NCTestClient cli;

    @BeforeEach
    void setUp() throws NCException, IOException {
        NCEmbeddedProbe.start(HelloWorldModel.class);

        // Use all defaults.
        cli = new NCTestClientBuilder().newBuilder().build();

        cli.open("nlpcraft.helloworld.ex");
    }

    @AfterEach
    void tearDown() throws NCException, IOException {
        if (cli != null)
            cli.close();

        NCEmbeddedProbe.stop();
    }

    @Test
    void test() throws NCException, IOException {
        // Empty parameter.
        assertTrue(cli.ask("").isFailed());

        // Only latin charset is supported.
        assertTrue(cli.ask("El tiempo en España").isFailed());

        // Should be passed.
        assertTrue(cli.ask("Hi!").isOk());
    }
}
