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

package org.apache.nlpcraft.server.rest

import org.apache.nlpcraft.NCTestEnvironment
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{BeforeEach, Test}

import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, SetHasAsJava}

@NCTestEnvironment(model = classOf[RestTestModel], startClient = false)
class NCRestAskSpec extends NCRestSpec {
    private var usrId: Long = 0

    @BeforeEach
    def setUp(): Unit = {
        post("user/get")(("$.id", (id: Number) => usrId = id.longValue()))

        assertTrue(usrId > 0)
    }

    @Test
    def testSync(): Unit = {
        post(
            "ask/sync",
            "txt" -> "a",
            "mdlId" -> "rest.test.model"
        )(
            ("$.state.status", (status: String) => assertEquals("QRY_READY", status))
        )

        post(
            "ask/sync",
            "txt" -> "b",
            "enableLog" -> true,
            "usrId" -> usrId,
            "data" -> Map("k1" -> "v1", "k1" -> "v2").asJava,
            "mdlId" -> "rest.test.model"
        )(
            ("$.state.status", (status: String) => assertEquals("QRY_READY", status))
        )
    }

    @Test
    def testAsync(): Unit = {
        // Asks.
        askAsync()
        askAsync()

        // Checks non empty states.
        post("check")(("$.states", (states: ResponseList) => assertFalse(states.isEmpty)))

        // Cancels all.
        post("cancel")()

        // Checks empty states.
        post("check")(("$.states", (states: ResponseList) => assertTrue(states.isEmpty)))

        // Asks.
        val id1 = askAsync()
        val id2 = askAsync()
        val id3 = askAsync()
        val id4 = askAsync()

        // Cancels two.
        post("cancel", "srvReqIds" -> Set(id1, id2).asJava)()

        // Checks states.
        post("check")(("$.states", (states: ResponseList) => {
            assertEquals(2, states.size())
            assertEquals(Set(id3, id4), states.asScala.map(p => p.get("srvReqId").asInstanceOf[String]).toSet)
        }))

        // Cancels others.
        post("cancel", "srvReqIds" -> Set(id3).asJava)()
        post("cancel", "srvReqIds" -> Set(id4).asJava)()

        // Checks empty states.
        post("check")(("$.states", (states: ResponseList) => assertTrue(states.isEmpty)))
    }
    /**
      *
      */
    private def askAsync(): String = {
        var id: String = null

        post(
            "ask",
            "txt" -> "a",
            "mdlId" -> "rest.test.model"
        )(
            ("$.srvReqId", (srvReqId: String) => id = srvReqId)
        )

        assertNotNull(id)

        id
    }

    @Test
    def testParameters(): Unit = {
        val m = Map("k1" -> "v1", "k2" -> 2).asJava

        testAsk(sync = true, enableLog = None, usrId = None, data = Some(m))
        testAsk(sync = true, enableLog = Some(true), usrId = None, data = Some(m))
        testAsk(sync = true, enableLog = Some(false), usrId = Some(usrId), data = None)
        testAsk(sync = true, enableLog = Some(true), usrId = Some(usrId), data = Some(m))

        testAsk(sync = false, enableLog = None, usrId = None, data = Some(m))
        testAsk(sync = false, enableLog = Some(true), usrId = None, data = Some(m))
        testAsk(sync = false, enableLog = Some(false), usrId = Some(usrId), data = None)
        testAsk(sync = false, enableLog = Some(true), usrId = Some(usrId), data = Some(m))
    }

    /**
      *
      * @param enableLog
      * @param usrId
      * @param data
      */
    def testAsk(
        sync: Boolean,
        enableLog: Option[java.lang.Boolean],
        usrId: Option[java.lang.Long],
        data: Option[java.util.Map[String, Any]]
    ): Unit = {
        post(
            if (sync) "ask/sync" else "ask",
            "txt" -> "a",
            "mdlId" -> "rest.test.model",
            "enableLog" -> enableLog.orNull,
            "usrId" -> usrId.orNull,
            "data" -> data.orNull
        )(
            ("$.status", (status: String) => assertEquals("API_OK", status))
        )

        post("cancel")()
    }

    @Test
    def testSyncMeta(): Unit = {
        post(
            "ask/sync",
            "txt" -> "meta",
            "mdlId" -> "rest.test.model"
        )(
            ("$.state.status", (status: String) => assertEquals("QRY_READY", status)),
            ("$.state.resMeta", (meta: java.util.Map[String, Object]) => {
                import RestTestModel._

                assertEquals(Map(K1 -> V1, K2 -> V2, K3 -> V3).asJava, meta)
            })
        )
    }
}
