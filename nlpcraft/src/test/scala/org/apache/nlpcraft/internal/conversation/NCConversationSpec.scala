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
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticTestElement
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import scala.jdk.CollectionConverters.*
import scala.util.Using

/**
  *
  */
class NCConversationSpec:
    private val usrId = "userId"

    import NCSemanticTestElement as TE

    /**
      *
      */
    @Test
    def test(): Unit =
        val mdl: NCModel =
            new NCTestModelAdapter:
                override val getPipeline: NCPipeline =
                    val pl = mkEnPipeline
                    pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("e1"), TE("e2"))
                    pl

                @NCIntent("intent=i1 term(t1)~{# == 'e1'} term(t2)~{# == 'e2'}?")
                def onMatch(@NCIntentTerm("t1") t1: NCEntity, @NCIntentTerm("t2") t2: Option[NCEntity]): NCResult = new NCResult()

        Using.resource(new NCModelClient(mdl)) { cli =>
            def execOk(txt: String): Unit = cli.ask(txt, null, usrId)
            def execReject(txt: String): Unit =
                try
                    cli.ask(txt, null, usrId)
                    require(false)
                catch
                    case e: NCRejection => // OK.
                    case e: Throwable => throw e

            // missed 'e1'
            execReject("e2")
            execOk("e1 e2")

            // 'e1' received from conversation.
            execOk("e2")

            cli.clearStm(usrId)
            cli.clearDialog(usrId)

            // missed 'e1' again.
            execReject("e2")
            execOk("e1 e2")

            // 'e1' received from conversation.
            execOk("e2")
        }

    @Test
    def testClearEmptyData(): Unit =
        val mdl: NCModel =
            new NCTestModelAdapter:
                override val getPipeline: NCPipeline =
                    val pl = mkEnPipeline
                    pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("e1"))
                    pl

                @NCIntent("intent=i1 term(t1)~{# == 'e1'}")
                def onMatch(im: NCIntentMatch): NCResult =
                    val conv = im.getContext.getConversation
                    conv.clearStm(_ => true)
                    conv.clearDialog(_ => true)
                    new NCResult()

        Using.resource(new NCModelClient(mdl)) { client =>
            client.ask("e1", null, "userId")
            client.clearDialog("userId1", _ => true)
            client.clearDialog("userId2")
            client.clearStm("userId3", _ => true)
            client.clearStm("user4")
        }
