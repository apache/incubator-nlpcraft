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
    private var usrId: Long = 0

    @BeforeEach
    def setUp(): Unit = {
        NCEmbeddedProbe.start(classOf[TimeModel])

        post("user/get")(("$.id", (id: Number) ⇒ usrId = id.longValue()))

        assertTrue(usrId > 0)
    }

    @AfterEach
    def tearDown(): Unit = NCEmbeddedProbe.stop()

    @Test
    def testSync(): Unit = {
        post(
            "ask/sync",
            "txt" → "What's the local time?",
            "mdlId" → "nlpcraft.time.ex"
        )(("$.state.status", (status: String) ⇒ assertEquals("QRY_READY", status)))

        post(
            "ask/sync",
            "txt" → "What's the local time?",
            "enableLog" → true,
            "usrId" → usrId,
            "data" → Map("k1" → "v1", "k1" → "v2").asJava,
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
        val id2 = askAsync(enableLog = Some(true))
        val id3 = askAsync(data = Some(Map[String, Any]("k1" → "v1", "k1" → "v2").asJava))
        val id4 = askAsync(usrId = Some(usrId))

        // Cancels two.
        post("cancel", "srvReqIds" → Set(id1, id2).asJava)()

        // Checks states.
        post("check")(("$.states", (states: DataMap) ⇒ {
            assertEquals(2, states.size())
            assertEquals(Set(id3, id4), states.asScala.map(p ⇒ p.get("srvReqId").asInstanceOf[String]).toSet)
        }))

        // Cancels others.
        post("cancel", "srvReqIds" → Set(id3).asJava)()
        post("cancel", "srvReqIds" → Set(id4).asJava)()

        // Checks empty states.
        post("check")(("$.states", (states: DataMap) ⇒ assertTrue(states.isEmpty)))
    }

    private def askAsync(
        enableLog: Option[java.lang.Boolean] = None,
        usrId: Option[java.lang.Long] = None,
        data: Option[java.util.Map[String, Any]] = None
    ): String = {
        var id: String = null

        post(
            "ask",
            "txt" → "What's the local time?",
            "mdlId" → "nlpcraft.time.ex",
            "enableLog" → enableLog.orNull,
            "usrId" → usrId.orNull,
            "data" → data.orNull,

        )(
            ("$.srvReqId", (srvReqId: String) ⇒ id = srvReqId)
        )

        assertNotNull(id)

        id
    }
}
