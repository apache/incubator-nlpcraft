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
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.en.NCEnSemanticPorterStemmer
import org.apache.nlpcraft.NCResultType.*
import org.apache.nlpcraft.nlp.util.NCTestModelAdapter
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  *
  */
class NCModelPingPongSpec:
    private var client: NCModelClient = null

    case class R(resType: NCResultType, txt: String) extends NCResult:
        this.setType(resType)
        this.setBody(txt)
        override def toString: String = s"$resType ($txt)"

    private val MDL: NCTestModelAdapter =
        new NCTestModelAdapter():
            // TODO: how to reset it on any request?
            var state: String = null

            @NCIntent("intent=dialog term(dialog)={# == 'dialog'}")
            def onDialog(@NCIntentTerm("dialog") dialog: NCEntity): NCResult =
                state = "dialog"
                R(ASK_DIALOG, s"Confirm your request 'dialog' request: ${dialog.mkText()}")

            @NCIntent("intent=confirm term(confirm)={# == 'confirm'}")
            def onConfirm(@NCIntentTerm("confirm") confirm: NCEntity): NCResult =
                if state == null || state != "dialog" then throw new NCRejection("Nothing to confirm.")

                state = "confirm"
                R(ASK_RESULT, s"Confirmed by: ${confirm.mkText()}")

            @NCIntent("intent=other term(other)={# == 'other'}")
            def onOther(@NCIntentTerm("other") other: NCEntity): NCResult =
                state = "other"
                R(ASK_RESULT, s"Any by: ${other.mkText()}")

    MDL.getPipeline.getEntityParsers.add(
        new NCSemanticEntityParser(
            new NCEnSemanticPorterStemmer,
            EN_PIPELINE.getTokenParser,
            Seq(
                NCSemanticTestElement("dialog", "my command"),
                NCSemanticTestElement("confirm", "my confirm"),
                NCSemanticTestElement("other", "my other")
            ).asJava
        )
    )

    @BeforeEach
    def setUp(): Unit = client = new NCModelClient(MDL)

    @AfterEach
    def tearDown(): Unit = client.close()

    private def ask(txt: String, expType: NCResultType): Unit =
        val res = client.ask(txt, null, "userId")
        println(s"Request [text=$txt, result=$res]")
        require(res.getType == expType)

    /**
      *
      */
    @Test
    def test(): Unit =
        ask("my command", ASK_DIALOG)
        ask("my confirm", ASK_RESULT)

    /**
      *
      */
    @Test
    def test2(): Unit =
        // 1. Nothing to confirm. No history.
        try ask("my confirm", ASK_RESULT) catch case e: NCRejection => println("Expected reject.")

        // 2. Nothing to confirm. Last question is not `dialog`.
        ask("my other", ASK_RESULT)
        try ask("my confirm", ASK_RESULT) catch case e: NCRejection => println("Expected reject.")

        // 3. Last question is `dialog`. Can be confirmed.
        ask("my command", ASK_DIALOG)
        ask("my confirm", ASK_RESULT)
