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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

class NCRestClearSpec extends NCRestSpec {
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
    def test(): Unit = {
        post("clear/conversation", "mdlId" → "nlpcraft.time.ex")()
        post("clear/conversation", "mdlId" → "nlpcraft.time.ex", "usrId" → usrId)()

        post("clear/dialog", "mdlId" → "nlpcraft.time.ex")()
        post("clear/dialog", "mdlId" → "nlpcraft.time.ex", "usrId" → usrId)()
    }
}
