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

package org.apache.nlpcraft.server.rest

import org.apache.nlpcraft.examples.time.TimeModel
import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

import scala.collection.JavaConverters._

class NCRestAskSpec extends NCRestSpec {
    @BeforeEach
    def setUp(): Unit = NCEmbeddedProbe.start(classOf[TimeModel])

    @AfterEach
    def tearDown(): Unit = NCEmbeddedProbe.stop()

    @Test
    def testSync(): Unit = {
        post(
            "ask/sync",
            "txt" → "What's the local time?",
            "mdlId" → "nlpcraft.time.ex"
        )(("$.state.status", (status: String) ⇒ assertEquals("QRY_READY", status)))
    }

    @Test
    def testAsync(): Unit = {
        // Asks.
        askAsync()
        askAsync()

        // Checks non empty states.
        post("check")(("$.states", (states: DataMap) ⇒ assertFalse(states.isEmpty)))

        // Cancels all.
        post("cancel")()

        // Checks empty states.
        post("check")(("$.states", (states: DataMap) ⇒ assertTrue(states.isEmpty)))

        // Asks.
        val id1 = askAsync()
        val id2 = askAsync()
        val id3 = askAsync()

        // Cancels two.
        post("cancel", "srvReqIds" → Set(id1, id2).asJava)()

        // Checks states.
        post("check")(("$.states", (states: DataMap) ⇒ {
            assertEquals(1, states.size())
            assertEquals(id3, states.get(0).get("srvReqId").asInstanceOf[String])
        }))

        // Cancels last one.
        post("cancel", "srvReqIds" → Set(id3).asJava)()

        // Checks empty states.
        post("check")(("$.states", (states: DataMap) ⇒ assertTrue(states.isEmpty)))
    }

    private def askAsync(): String = {
        var id: String = null

        post("ask", "txt" → "What's the local time?", "mdlId" → "nlpcraft.time.ex")(
            ("$.srvReqId", (srvReqId: String) ⇒ id = srvReqId)
        )

        assertNotNull(id)

        id
    }
}
