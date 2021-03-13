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

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCModel, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import java.util

/**
  * Test model.
  */
class NCConversationSpecModel extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    override def getElements: util.Set[NCElement] = Set(NCTestElement("test1"), NCTestElement("test2"))

    // 'test1' is mandatory, 'test2' is optional.
    @NCIntent("intent=testIntentId term~{id() == 'test1'} term~{id() == 'test2'}?")
    def onMatch(): NCResult = NCResult.text("ok")
}

/**
  * @see NCConversationSpecModel
  */
@NCTestEnvironment(model = classOf[NCConversationSpecModel], startClient = true)
class NCConversationSpec extends NCTestContext {
    @Test
    @throws[Exception]
    private[conversation] def test(): Unit = {
        val cli = getClient

        // missed 'test1'
        assertFalse(cli.ask("test2").isOk)
        assertTrue(cli.ask("test1 test2").isOk)

        // 'test1' received from conversation.
        assertTrue(cli.ask("test2").isOk)

        cli.clearConversation()

        // missed 'test1' again.
        assertFalse(cli.ask("test2").isOk)
        assertTrue(cli.ask("test1 test2").isOk)

        // 'test1' received from conversation.
        assertTrue(cli.ask("test2").isOk)
    }
}
