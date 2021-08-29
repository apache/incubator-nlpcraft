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

package org.apache.nlpcraft.model.synonyms

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

class NCSuspSynonymsSpecModel extends NCModelAdapter("nlpcraft.susp.syns.test.mdl", "Synonyms Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("a"),

            NCTestElement("e1", "a?"),
            NCTestElement("e2", "a*"),
            NCTestElement("e3", "a+"),
            NCTestElement("e4", "a? a+"),
            NCTestElement("e5", "a? a+ a*"),
        )

    @NCIntent("intent=onA term(t)={# == 'a'}")
    def onA(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

@NCTestEnvironment(model = classOf[NCSuspSynonymsSpecModel], startClient = true)
class NCSuspSynonymsSpec extends NCTestContext {
    @Test
    def test(): Unit = {
        // Look at the warnings. It checks that probe was successfully started.
        require(getClient.ask("a").isOk)
    }
}
