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

package org.apache.nlpcraft.model.intent.dsl

import java.io.IOException

import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.model.tools.test.{NCTestClient, NCTestClientBuilder}
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

/**
 * DSL test model test. Make sure to start up the NLPCraft server before running this test.
 */
class NCDslTest {
    private var cli: NCTestClient = _

    @BeforeEach
    @throws[NCException]
    @throws[IOException]
    private[dsl] def setUp(): Unit = {
        // Start embedded probe with the test model.
        NCEmbeddedProbe.start(classOf[NCDslTestModel])

        cli = new NCTestClientBuilder().newBuilder.build

        cli.open("nlpcraft.dsl.test")
    }

    @AfterEach
    @throws[NCException]
    @throws[IOException]
    private[dsl] def tearDown(): Unit = {
        if (cli != null)
            cli.close()

        NCEmbeddedProbe.stop()
    }

    @Test
    @throws[NCException]
    @throws[IOException]
    private[dsl] def test(): Unit = assertTrue(cli.ask("aa").isOk)
}