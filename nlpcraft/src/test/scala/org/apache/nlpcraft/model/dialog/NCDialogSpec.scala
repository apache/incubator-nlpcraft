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

package org.apache.nlpcraft.model.dialog

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCModel, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test

import java.util
import scala.collection.JavaConverters._

/**
  * Test model.
  */
class NCDialogSpecModel extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    override def getElements: util.Set[NCElement] =
        (for (ch ← 'a' to 'y'; i ← 1 to 9) yield new NCElement { override def getId: String = s"$ch$i" }).toSet.asJava

    @NCIntent("intent=onA1 term~{id == 'a1'}")
    def onA1(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA2 flow='^(?:onA1)(^:onA1)*$' term~{id == 'a2'}")
    def onA2(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA3 flow='onA1' term~{id == 'a3'}")
    def onA3(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA4 flow='onA1 onA1' term~{id == 'a4'}")
    def onA4(): NCResult = NCResult.text("ok")
}

/**
  * @see NCDialogSpecModel
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel], startClient = true)
class NCDialogSpec extends NCTestContext {
    private def f(pairs: (String, String)*): Unit = {
        val cli = getClient

        def go(): Unit =
            pairs.zipWithIndex.foreach { case ((txt, intentId), idx) ⇒
                val res = cli.ask(txt)

                if (intentId == null)
                    assertTrue(
                        res.isFailed,
                        s"Unexpected success [request=$txt, resultIntent=${res.getIntentId}, idx=$idx]"
                    )
                else {
                    assertTrue(
                        res.isOk,
                        s"Unexpected error [request=$txt, expectedIntent=$intentId, idx=$idx]"
                    )
                    assertEquals(
                        intentId, res.getIntentId,
                        s"Expected: $intentId, but got: ${res.getIntentId}, idx=$idx"
                    )
                }
            }

        go()

        cli.clearConversation()
        cli.clearDialog()
    }

    @Test
    @throws[Exception]
    private[dialog] def test1(): Unit =
        f(
            "a2" → null,
            "a1" → "onA1",
            "a2" → "onA2",
            "a1" → "onA1",
            "a1" → "onA1",
            "a2" -> null
        )

    @Test
    @throws[Exception]
    private[dialog] def test2(): Unit =
        f(
            "a3" → null,
            "a1" → "onA1",
            "a3" → "onA3",
            "a1" → "onA1",
            "a1" → "onA1",
            "a3" → "onA3"
        )

    @Test
    @throws[Exception]
    private[dialog] def test3(): Unit =
        f(
            "a4" → null,
            "a1" → "onA1",
            "a1" → "onA1",
            "a4" → "onA4",
            "a4" → "onA4"
        )
}
