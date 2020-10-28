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

package org.apache.nlpcraft.model.conversation

import java.io.IOException
import java.util
import java.util.Collections

import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModel, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

object NCTimeoutSpecModel {
    final val TIMEOUT = 2000
}

import org.apache.nlpcraft.model.conversation.NCTimeoutSpecModel._

class NCTimeoutSpecModel extends NCModel {
    private var step = 0
    private var saveData: util.Map[String, AnyRef] = _

    override def getId: String = this.getClass.getSimpleName

    override def getName: String = this.getClass.getSimpleName

    override def getVersion: String = "1.0.0"

    override def getConversationTimeout: Long = TIMEOUT

    override def getElements: util.Set[NCElement] =
        Collections.singleton(
            new NCElement {
                override def getId: String = "test"

                override def getSynonyms: util.List[String] = Collections.singletonList("test")
            }
        )

    @NCIntent("intent=req term~{id == 'test'}")
    def onMatch(ctx: NCIntentMatch): NCResult = {
        val conv = ctx.getContext.getConversation

        step match {
            case 0 ⇒
                assertTrue(conv.getUserData.isEmpty)
                assertTrue(conv.getDialogFlow.isEmpty)

                conv.getUserData.put("key", "value")

                saveData = conv.getUserData

            case 1 ⇒
                assertFalse(conv.getUserData.isEmpty)
                assertEquals(saveData, conv.getUserData)
                assertEquals("value", conv.getUserData.getOrDefault("key", "-"))
                assertFalse(conv.getDialogFlow.isEmpty)

            case 2 ⇒
                assertTrue(conv.getUserData.isEmpty)
                assertTrue(saveData.isEmpty)
                assertTrue(conv.getDialogFlow.isEmpty)

            case 3 ⇒
                assertTrue(conv.getUserData.isEmpty)
                assertFalse(conv.getDialogFlow.isEmpty)

            case _ ⇒ require(false)
        }

        val msg = s"Step-$step"

        step = step + 1

        NCResult.json(msg)
    }
}

/**
  * @see NCTimeoutSpecModel
  */
@NCTestEnvironment(model = classOf[NCTimeoutSpecModel], startClient = true)
class NCConversationTimeoutSpec extends NCTestContext {
    @Test
    @throws[NCException]
    @throws[IOException]
    private[conversation] def test(): Unit = {
        def ask(): Unit = assertTrue(getClient.ask("test").isOk)

        ask()
        ask()

        Thread.sleep(TIMEOUT + 1000)

        ask()
        ask()
    }
}
