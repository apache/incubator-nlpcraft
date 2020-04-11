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

package org.apache.nlpcraft.models.stm

import java.io.IOException

import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.model.tools.test.{NCTestClient, NCTestClientBuilder}
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

/**
 *
 */
class NCStmTestModelSpec {
    private var cli: NCTestClient = _

    @BeforeEach
    @throws[NCException]
    @throws[IOException]
    private[stm] def setUp(): Unit = {
        NCEmbeddedProbe.start(classOf[NCStmTestModel])

        cli = new NCTestClientBuilder().newBuilder.build

        cli.open("nlpcraft.stm.test") // See phone_model.json
    }

    @AfterEach
    @throws[NCException]
    @throws[IOException]
    private[stm] def tearDown(): Unit = {
        if (cli != null)
            cli.close()

        NCEmbeddedProbe.stop()
    }

    /**
     * @param req
     * @param expResp
     * @throws IOException
     */
    @throws[IOException]
    private def check(req: String, expResp: String): Unit = {
        val res = cli.ask(req)

        assertTrue(res.isOk)
        assertTrue(res.getResult.isPresent)
        assertEquals(expResp, res.getResult.get)
    }

    /**
     * Checks behaviour. It is based on intents and elements groups.
     *
     * @throws Exception
     */
    @Test
    @throws[Exception]
    private[stm] def test(): Unit = for (i <- 0 until 3) {
        check("sale", "sale")
        check("best", "sale_best_employee")
        check("buy", "buy")
        check("best", "buy_best_employee")
        check("sale", "sale")
    }
}