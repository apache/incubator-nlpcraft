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

package org.apache.nlpcraft.model.`abstract`

import org.apache.nlpcraft.model.{NCContext, NCResult, NCToken}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.collection.JavaConverters._

class NCAbstractTokensModelVariants extends NCAbstractTokensModel {
    private def checkId(t: NCToken, id: String): Unit =
        require(t.getId == id, s"Expected ID: $id, token: $t")
    private def checkText(t: NCToken, txt: String): Unit =
        require(t.getOriginalText == txt, s"Expected text: $txt, token: $t")

    private def checkToken(t: NCToken, id: String, txt: String): Unit = {
        checkId(t, id)
        checkText(t, txt)
    }

    override def onContext(ctx: NCContext): NCResult = {
        val variants = ctx.getVariants.asScala

        def checkLimit(limitPart: NCToken): Unit = {
            require(limitPart.getIndex == -1, s"Unexpected limit token index: ${limitPart.getIndex}, token: $limitPart, meta: ${limitPart.getMetadata}")
            checkId(limitPart, "nlpcraft:limit")

            val limNote = limitPart.getMetadata.get("nlpcraft:limit:note").asInstanceOf[String]

            require(limNote == "wrapAnyWord", s"Unexpected limit token note: '$limNote', token: $limitPart, meta: ${limitPart.getMetadata}")

            val limIdxs = limitPart.getMetadata.get("nlpcraft:limit:indexes").asInstanceOf[util.List[Integer]].asScala

            require(
                limIdxs.size == 1 && limIdxs.head == -1,
                s"Unexpected limit token ref indexes: [${limIdxs.mkString(",")}], token: $limitPart, meta: ${limitPart.getMetadata}"
            )
        }

        def checkWrapAnyWord(t: NCToken, any: String): Unit = {
            val parts = t.getPartTokens.asScala

            require(parts.size == 2)

            checkToken(parts.head, "nlpcraft:nlp", "the")
            checkToken(parts.last, "anyWord", any)

            require(parts.last.isAbstract, s"Unexpected abstract token: ${parts.last}")

        }

        ctx.getRequest.getNormalizedText match {
            case "word the word" =>
                require(variants.size == 1)

                val toks = variants.head.asScala

                require(toks.size == 2)

                checkToken(toks.head, "nlpcraft:nlp", "word")
                checkToken(toks.last, "wrapAnyWord", "the word")

                checkWrapAnyWord(toks.last, "word")

            case "10 w1 10 w2" =>
                require(variants.nonEmpty)

                val vars = variants.
                    map(p => p.asScala).
                    filter(v => v.size == 2 && v.head.getId == "nlpcraft:nlp" && v.last.getId == "wrapNum")

                require(vars.size == 1)

                val toks = vars.head

                require(toks.size == 2)

                checkToken(toks.head, "nlpcraft:nlp", "10")
                checkToken(toks.last,"wrapNum", "w1 10 w2")

                val t2Parts = toks.last.getPartTokens.asScala

                require(t2Parts.size == 3)

                checkToken(t2Parts.head,"nlpcraft:nlp", "w1")
                checkToken(t2Parts(1),"nlpcraft:num", "10")
                checkToken(t2Parts.last,"nlpcraft:nlp", "w2")

            case "before limit top 6 the any" =>
                require(variants.nonEmpty)

                val vars = variants.
                    map(p => p.asScala).
                    filter(v => v.size == 2 && v.head.getId == "wrapLimit" && v.last.getId == "wrapAnyWord")

                require(vars.size == 1)

                val toks = vars.head

                require(toks.size == 2)

                checkToken(toks.head, "wrapLimit", "before limit top 6")
                checkToken(toks.last, "wrapAnyWord", "the any")

                val wrap = toks.head.getPartTokens.asScala

                require(wrap.size == 3)

                checkLimit(wrap.last)

                checkWrapAnyWord(toks.last, "any")
            case "a wrap before limit top 6 the any" =>
                require(variants.nonEmpty)

                val vars = variants.
                    map(p => p.asScala).
                    filter(v => v.size == 3 && v(1).getId == "wrapWrapLimit" && v.last.getId == "wrapAnyWord")

                require(vars.size == 1)

                val toks = vars.head

                require(toks.size == 3)

                checkToken(toks.head, "nlpcraft:nlp", "a")
                checkToken(toks(1), "wrapWrapLimit", "wrap before limit top 6")
                checkToken(toks.last, "wrapAnyWord", "the any")

                val wrap = toks(1).getPartTokens.asScala

                require(wrap.size == 2)

                val wrapLimit = wrap.last

                require(wrapLimit.getIndex == -1, s"Unexpected limit token: $wrapLimit, meta: ${wrapLimit.getMetadata}")
                checkId(wrapLimit,"wrapLimit")

                require(wrapLimit.getPartTokens.size == 3, s"Parts count: ${wrapLimit.getPartTokens.size()}")

                checkLimit(wrapLimit.getPartTokens.asScala.last)

                checkWrapAnyWord(toks.last, "any")
            case _ => throw new AssertionError(s"Unexpected request: ${ctx.getRequest.getNormalizedText}")
        }

        NCResult.text("OK")
    }
}

@NCTestEnvironment(model = classOf[NCAbstractTokensModelVariants], startClient = true)
class NCAbstractTokensVariantsSpec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkResult("word the word", "OK")
        checkResult("10 w1 10 w2", "OK")
        checkResult("before limit top 6 the any", "OK")
        checkResult("a wrap before limit top 6 the any", "OK")
    }
}