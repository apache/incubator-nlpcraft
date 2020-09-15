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

package org.apache.nlpcraft.examples.time

import java.io.IOException

import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe
import org.apache.nlpcraft.model.tools.test.{NCTestClient, NCTestClientBuilder}
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

class NCTimeModelSpec {
    private var cli: NCTestClient = _

    @BeforeEach
    @throws[NCException]
    @throws[IOException]
    private[time] def setUp(): Unit = {
        NCEmbeddedProbe.start(classOf[TimeModel])

        cli = new NCTestClientBuilder().newBuilder.build

        cli.open("nlpcraft.time.ex")
    }

    @AfterEach
    @throws[NCException]
    @throws[IOException]
    private[time] def tearDown(): Unit = {
        if (cli != null)
            cli.close()

        NCEmbeddedProbe.stop()
    }

    @Test
    @throws[NCException]
    @throws[IOException]
    private[time] def testIntentsPriorities(): Unit = {
        def check(txt: String, id: String): Unit = {
            val res = cli.ask(txt)

            assertTrue(res.isOk)
            assertTrue(res.getIntentId == id)
        }

        // intent1 must be winner for `What's the local time?` question, because exact matching.
        // Accumulated history (geo:city information) must be ignored.

        // 1. Without conversation.
        check("Show me time of the day in London.", "intent2")
        cli.clearConversation()
        check("What's the local time?", "intent1")

        // 2. The same with conversation.
        check("Show me time of the day in London.", "intent2")
        check("What's the local time?", "intent1")
    }
}
