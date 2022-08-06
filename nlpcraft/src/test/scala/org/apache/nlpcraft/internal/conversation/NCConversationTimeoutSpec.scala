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
class NCConversationTimeoutSpec:
    private val TIMEOUT = 200
    private val VALUE = "value"
    private val EMPTY = "empty"

    /**
      *
      */
    @Test
    def test(): Unit =
        import NCSemanticTestElement as TE

        val mdl: NCModel =
            new NCTestModelAdapter:
                override val getConfig: NCModelConfig =
                    new NCModelConfig(CFG.id, CFG.name, CFG.version, CFG.description, CFG.origin, TIMEOUT, CFG.conversationDepth)

                override val getPipeline: NCPipeline =
                    val pl = mkEnPipeline
                    pl.entParsers += NCTestUtils.mkEnSemanticParser(TE("test"))
                    pl

                @NCIntent("intent=i term(e)~{# == 'test'}")
                def onMatch(ctx: NCContext, im: NCIntentMatch, @NCIntentTerm("e") e: NCEntity): NCResult =
                    val conv = ctx.getConversation
                    val res = NCResult(conv.getData.getOpt("key").getOrElse(EMPTY), NCResultType.ASK_RESULT)

                    // For next calls.
                    conv.getData.put("key", VALUE)

                    res

        Using.resource(new NCModelClient(mdl)) { cli =>
            def check(hasValue: Boolean): Unit =
                require(cli.ask("test", "userId").body.toString == (if hasValue then VALUE else EMPTY))

            check(false)
            check(true)

            Thread.sleep(TIMEOUT * 2)

            check(false)
            check(true)
        }
