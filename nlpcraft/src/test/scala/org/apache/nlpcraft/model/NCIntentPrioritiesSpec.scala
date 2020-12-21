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

package org.apache.nlpcraft.model

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.util
import java.util.Collections

/**
  * Intents priorities test model.
  */
class NCIntentPrioritiesSpecModel extends NCModelAdapter(
    "nlpcraft.priorities.model.test", "Priorities Test Model", "1.0"
) {
    override def getElements: util.Set[NCElement] =
        Collections.singleton(new NCElement { override def getId: String = "x" } )

    @NCIntent("intent=low term(x)={id == 'x'} term(city)={id == 'nlpcraft:city'}?")
    private def onLow(ctx: NCIntentMatch): NCResult = NCResult.text("low")

    @NCIntent("intent=high term(x)={id == 'x'} term(city)={id == 'nlpcraft:city'}")
    private def onHigh(ctx: NCIntentMatch): NCResult = NCResult.text("high")
}

/**
  * Intents priorities test.
  *
  * It checks that intent2 priority higher that intent1
  * Note that element `x` added because `city` is optional and at least one element should be in text for matching.
  */
@NCTestEnvironment(model = classOf[NCIntentPrioritiesSpecModel], startClient = true)
class NCIntentPrioritiesSpec extends NCTestContext {
    private def checkHigh(txts: String*): Unit =
        txts.foreach(txt ⇒ assertEquals("high", getClient.ask(txt).getIntentId, s"Error on: $txt"))

    @Test
    def test(): Unit =
        checkHigh(
            "Moscow x",
            "London x",
            "x berlin",
            "x Berlin, Germany"
        )
}
