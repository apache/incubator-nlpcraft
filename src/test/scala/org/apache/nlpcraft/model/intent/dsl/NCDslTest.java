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

package org.apache.nlpcraft.model.intent.dsl;

import org.junit.jupiter.api.*;
import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.model.tools.test.*;
import org.apache.nlpcraft.probe.embedded.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DSL test model test. Make sure to start up the NLPCraft server before running this test.
 */
class NCDslTest {
    private NCTestClient cli;

    @BeforeEach
    void setUp() throws NCException, IOException {
        // Start embedded probe with the test model.
        NCEmbeddedProbe.start(NCDslTestModel.class);

        cli = new NCTestClientBuilder().newBuilder().build();

        cli.open("nlpcraft.dsl.test");
    }

    @AfterEach
    void tearDown() throws NCException, IOException {
        if (cli != null)
            cli.close();
    
        NCEmbeddedProbe.stop();
    }

    @Test
    void test() throws NCException, IOException {
        assertTrue(cli.ask("aa").isOk());
    }
}
