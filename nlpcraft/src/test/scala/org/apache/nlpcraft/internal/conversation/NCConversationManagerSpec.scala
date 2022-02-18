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

package org.apache.nlpcraft.internal.conversation

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Test

import java.util.function.Predicate

/**
  *
  */
class NCConversationManagerSpec:
    case class ModelConfigMock(timeout: Long = Long.MaxValue) extends NCModelConfig("testId", "test", "1.0", "Test description", "Test origin"):
        override def getConversationTimeout: Long = timeout

    @Test
    def test(): Unit =
        val mgr = NCConversationManager(ModelConfigMock())
        val t = NCTestToken()
        val reqId = "req1"

        val conv = mgr.getConversation("user1")

        def checkSize(size: Int): Unit =
            require(conv.getEntities.size() == size, s"Unexpected entities size: ${conv.getEntities.size()}, expected: $size")

        // Initial empty.
        checkSize(0)

        // Added. Still empty.
        conv.addEntities(reqId, Seq(NCTestEntity("e1", reqId, tokens = t), NCTestEntity("e2", reqId, tokens = t)))
        checkSize(0)

        // Updated. Not empty.
        conv.updateEntities()
        checkSize(2)

        // Partially cleared.
        conv.clearEntities(new Predicate[NCEntity]:
            override def test(t: NCEntity): Boolean = t.getId == "e1"
        )
        checkSize(1)
        require(conv.getEntities.get(0).getId == "e2")

    @Test
    def testTimeout(): Unit =
        val timeout = 1000

        val mgr = NCConversationManager(ModelConfigMock(timeout))
        val t = NCTestToken()
        val reqId = "req1"

        // TODO: Drop method and use saved conversation instead - error is thrown
        def getConversation: NCConversationHolder = mgr.getConversation("user1")

        def checkSize(size: Int): Unit =
            val conv = getConversation
            require(conv.getEntities.size() == size, s"Unexpected entities size: ${conv.getEntities.size()}, expected: $size")

        // Initial empty.
        checkSize(0)

        // Added. Still empty.
        getConversation.addEntities(reqId, Seq(NCTestEntity("e1", reqId, tokens = t), NCTestEntity("e2", reqId, tokens = t)))
        checkSize(0)

        // Updated. Not empty.
        getConversation.updateEntities()
        checkSize(2)

        // Cleared by timeout.
        try
            mgr.start()
            Thread.sleep(timeout * 2)
            checkSize(0)
        finally
            mgr.close()

