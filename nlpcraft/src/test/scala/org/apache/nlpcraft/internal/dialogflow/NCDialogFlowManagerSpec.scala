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

package org.apache.nlpcraft.internal.dialogflow

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.util.NCTestRequest
import org.junit.jupiter.api.*

import java.util
import java.util.function.Predicate

/**
  *
  */
class NCDialogFlowManagerSpec:
    case class IntentMatchMock(intentId: String, ctx: NCContext) extends NCIntentMatch:
        override val getContext: NCContext = ctx
        override val getIntentId: String = intentId
        override val getIntentEntities: List[List[NCEntity]] = null
        override def getTermEntities(idx: Int): List[NCEntity] = null
        override def getTermEntities(termId: String): List[NCEntity] = null
        override val getVariant: NCVariant = null

    case class ContextMock(userId: String, reqTs: Long = NCUtils.now()) extends NCContext:
        override def isOwnerOf(ent: NCEntity): Boolean = false
        override def getModelConfig: NCModelConfig = null
        override def getRequest: NCRequest = NCTestRequest(txt = "Any", userId = userId, ts = reqTs)
        override def getConversation: NCConversation = null
        override def getVariants: List[NCVariant] = null
        override def getTokens: List[NCToken] = null

    case class ModelConfigMock(timeout: Long = Long.MaxValue) extends NCModelConfig("testId", "test", "1.0", "Test description", "Test origin"):
        override val conversationTimeout: Long = timeout

    private var mgr: NCDialogFlowManager = _

    /**
      *
      * @param expSizes
      */
    private def check(expSizes: (String, Int)*): Unit =
        for ((usrId, expSize) <- expSizes)
            val size = mgr.getDialogFlow(usrId).size
            require(size == expSize, s"Expected: $expSize for '$usrId', but found: $size")

    /**
      *
      * @param userIds
      */
    private def ask(userIds: String*): Unit = for (userId <- userIds) mgr.ack(userId)

    /**
      *
      * @param id
      * @param ctx
      */
    private def addMatchedIntent(id: String, ctx: NCContext): Unit = mgr.addMatchedIntent(IntentMatchMock(id, ctx), null, ctx)

    /**
      *
      */
    @AfterEach
    def cleanUp(): Unit = if mgr != null then mgr.close()

    @Test
    def test(): Unit =
        mgr = NCDialogFlowManager(ModelConfigMock())

        val now = NCUtils.now()

        addMatchedIntent("i11", ContextMock("user1"))
        addMatchedIntent("i12", ContextMock("user1"))
        addMatchedIntent("i21", ContextMock("user2"))
        addMatchedIntent("i22", ContextMock("user2"))
        addMatchedIntent("i31", ContextMock("user3"))

        // Initial.
        ask("user1", "user2", "user3", "user4")
        check("user1" -> 2, "user2" -> 2, "user3" -> 1, "user4" -> 0)

        mgr.clear(usrId = "user4")
        check("user1" -> 2, "user2" -> 2, "user3" -> 1, "user4" -> 0)

        mgr.clear(usrId = "user1")
        check("user1" -> 0, "user2" -> 2, "user3" -> 1, "user4" -> 0)

        mgr.clear(usrId = "user2", _.getIntentMatch.getIntentId == "i21")
        check("user1" -> 0, "user2" -> 1, "user3" -> 1, "user4" -> 0)

        mgr.clear(usrId = "user2")
        mgr.clear(usrId = "user3")
        check("user1" -> 0, "user2" -> 0, "user3" -> 0, "user4" -> 0)

    @Test
    def testTimeout(): Unit =
        val delay = 10
        val timeout = delay * 1000

        mgr = NCDialogFlowManager(ModelConfigMock(timeout))

        val now = NCUtils.now()

        addMatchedIntent("any", ContextMock("user1", now))
        addMatchedIntent("any", ContextMock("user1", now - timeout - delay))
        addMatchedIntent("any", ContextMock("user2", now - timeout))

        // Initial.
        ask("user1", "user2")
        check("user1" -> 2, "user2" -> 1)

        mgr.start()

        Thread.sleep(delay * 5)
        check("user1" -> 1, "user2" -> 0)

        mgr.close()
        check("user1" -> 0, "user2" -> 0)
