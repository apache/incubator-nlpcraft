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

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test

/**
 *
 */
@NCTestEnvironment(model = classOf[NCStmTestModel], startClient = true)
class NCStmTestModelSpec extends NCTestContext {
    /**
     * @param req
     * @param expResp
     */
    private def check(req: String, expResp: String): Unit = {
        val res = getClient.ask(req)

        assertTrue(res.isOk)
        assertTrue(res.getResult.isPresent)
        assertEquals(expResp, res.getResult.get)
    }

    /**
     * Checks behaviour. It is based on intents and elements groups.
     */
    @Test
    private[stm] def test(): Unit = for (i ← 0 until 3) {
        check("sale", "sale")
        check("best", "sale_best_employee")
        check("buy", "buy")
        check("best", "buy_best_employee")
        check("sale", "sale")
    }
}