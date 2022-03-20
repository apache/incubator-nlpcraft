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

package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.{NCSemanticTestElement as STE, *}
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  *
  */
class NCModelPingPongSpec:
    private var client: NCModelClient = _

    private case class R(resType: NCResultType, txt: String) extends NCResult(txt, resType):
        override def toString: String = s"$resType ($txt)"

    private val MDL: NCModel =
        new NCTestModelAdapter():
            @NCIntent("intent=command term(command)={# == 'command'}")
            def onCommand(im: NCIntentMatch, @NCIntentTerm("command") command: NCEntity): NCResult =
                R(ASK_DIALOG, s"Confirm your request 'command'")

            @NCIntent("intent=confirmCommand term(confirm)={# == 'confirm'}")
            def onConfirmCommand(im: NCIntentMatch, @NCIntentTerm("confirm") confirm: NCEntity): NCResult =
                val lastIntentId =
                    im.getContext.
                        getConversation.
                        getDialogFlow.asScala.lastOption.
                        flatMap(p => Option(p.getIntentMatch.getIntentId)).orNull

                if lastIntentId != "command" then
                    throw new NCRejection("Nothing to confirm.")

                println("'Command' confirmed and can be be executed here.")

                R(ASK_RESULT, s"'dialog' confirmed.")

            @NCIntent("intent=other term(other)={# == 'other'}")
            def onOther(im: NCIntentMatch, @NCIntentTerm("other") other: NCEntity): NCResult =
                R(ASK_RESULT, s"Some request by: ${other.mkText()}")

    MDL.getPipeline.getEntityParsers.add(NCTestUtils.mkENSemanticParser(Seq(STE("command"), STE("confirm"), STE("other")).asJava))

    @BeforeEach
    def setUp(): Unit = client = new NCModelClient(MDL)

    @AfterEach
    def tearDown(): Unit = client.close()

    private def ask(txt: String, typ: NCResultType): Unit =
        val res = client.ask(txt, null, "userId")
        println(s"Request [text=$txt, result=$res]")
        require(res.getType == typ)

    private def askForDialog(txt: String): Unit = ask(txt, ASK_DIALOG)
    private def askForResult(txt: String): Unit = ask(txt, ASK_RESULT)
    private def askForReject(txt: String): Unit =
        try ask(txt, ASK_RESULT) catch case e: NCRejection => println(s"Expected reject on: $txt")

    /**
      *
      */
    @Test
    def test(): Unit =
        askForDialog("command")
        askForResult("confirm")

    /**
      *
      */
    @Test
    def test2(): Unit =
        // 1. Nothing to confirm. No history.
        askForReject("confirm")

        // 2. Nothing to confirm. Last question is not `command`.
        askForResult("other")
        askForReject("confirm")

        // 3. Last question is `command`. Can be confirmed.
        askForDialog("command")
        askForResult("confirm")