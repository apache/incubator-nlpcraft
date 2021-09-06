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

package org.apache.nlpcraft.model.dialog

import org.apache.nlpcraft.model.{NCDialogFlowItem, NCElement, NCIntent, NCModel, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test

import java.util
import java.util.{List => JList}

import scala.jdk.CollectionConverters.CollectionHasAsScala

/**
  * Test model.
  */
class NCDialogSpecModelFlow {
    def trueAlways(flow: JList[NCDialogFlowItem]): Boolean = true
    def falseAlways(flow: JList[NCDialogFlowItem]): Boolean = false
    def trueAfterOnA7(flow: JList[NCDialogFlowItem]): Boolean = flow.asScala.exists(_.getIntentId == "onA7")
    def trueAfterOnA7AndA8(flow: JList[NCDialogFlowItem]): Boolean = {
        val seq = flow.asScala
        seq.exists(_.getIntentId == "onA7") && seq.exists(_.getIntentId == "onA8")
    }
}

class NCDialogSpecModel extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    override def getElements: util.Set[NCElement] = Set((for (i <- 1 to 100) yield NCTestElement(s"a$i")):_*)

    @NCIntent("intent=onA1 term~{# == 'a1'}")
    def onA1(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA2 flow='^(?:onA1)(^:onA1)*$' term~{# == 'a2'}")
    def onA2(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA3 flow='onA1' term~{# == 'a3'}")
    def onA3(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA4 flow='onA1 onA1' term~{# == 'a4'}")
    def onA4(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA5 flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow#trueAlways/ term~{# == 'a5'}")
    def onA5(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA6 flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow#falseAlways/ term~{# == 'a6'}")
    def onA6(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA7 term~{# == 'a7'}")
    def onA7(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA8 flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow#trueAfterOnA7/ term~{# == 'a8'}")
    def onA8(): NCResult = NCResult.text("ok")

    @NCIntent("intent=onA9 flow=/org.apache.nlpcraft.model.dialog.NCDialogSpecModelFlow#trueAfterOnA7AndA8/ term~{# == 'a9'}")
    def onA9(): NCResult = NCResult.text("ok")

    def trueAlwaysInternal(flow: JList[NCDialogFlowItem]): Boolean = true

    @NCIntent("intent=onA10 flow=/#trueAlwaysInternal/ term~{# == 'a10'}")
    def onA10(): NCResult = NCResult.text("ok")

    def falseAlwaysInternal(flow: JList[NCDialogFlowItem]): Boolean = false

    @NCIntent("intent=onA11 flow=/#falseAlwaysInternal/ term~{# == 'a11'}")
    def onA11(): NCResult = NCResult.text("ok")
}

/**
  * @see NCDialogSpecModel
  */
@NCTestEnvironment(model = classOf[NCDialogSpecModel], startClient = true)
class NCDialogSpec extends NCTestContext {
    private def f(pairs: (String, String)*): Unit = {
        def go(): Unit = {
            val cli = getClient

            cli.clearConversation()
            cli.clearDialog()

            pairs.zipWithIndex.foreach { case ((txt, intentId), idx) =>
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
        }

        go()
        go()
    }

    @Test
    private[dialog] def test1(): Unit =
        f(
            "a2" -> null,
            "a1" -> "onA1",
            "a2" -> "onA2",
            "a1" -> "onA1",
            "a1" -> "onA1",
            "a2" -> null
        )

    @Test
    private[dialog] def test2(): Unit =
        f(
            "a3" -> null,
            "a1" -> "onA1",
            "a3" -> "onA3",
            "a1" -> "onA1",
            "a1" -> "onA1",
            "a3" -> "onA3"
        )

    @Test
    private[dialog] def test3(): Unit =
        f(
            "a4" -> null,
            "a1" -> "onA1",
            "a1" -> "onA1",
            "a4" -> "onA4",
            "a4" -> "onA4"
        )

    @Test
    private[dialog] def test4(): Unit = {
        // Always true.
        checkIntent("a5", "onA5")
        checkIntent("a5", "onA5")

        // Always false.
        require(getClient.ask("a6").isFailed)
        require(getClient.ask("a6").isFailed)
    }

    @Test
    private[dialog] def test5(): Unit =
        f(
            "a8" -> null,
            "a9" -> null,
            "a7" -> "onA7",
            "a9" -> null,
            "a8" -> "onA8",
            "a9" -> "onA9"
        )

    @Test
    private[dialog] def test6(): Unit = {
        // Always 'true'.
        require(getClient.ask("a10").isOk)
        // Always 'false'.
        require(getClient.ask("a11").isFailed)
    }
}
