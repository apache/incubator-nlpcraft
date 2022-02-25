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

package org.apache.nlpcraft.nlp.token.parser.opennlp

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.token.enricher.en.*
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.*

import java.util
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCOpenNLPTokenParserSpec:
    private val enricher = new NCStopWordsTokenEnricher(null, null)

    private def isStopWord(t: NCToken): Boolean = t.get[Boolean]("stopword")

    private def test(txt: String, validate: Seq[NCToken] => _): Unit =
        val toksList = EN_PIPELINE.getTokenParser.tokenize(txt)

        enricher.enrich(NCTestRequest(txt), CFG, toksList)

        val toks = toksList.asScala.toSeq
        assert(toks.nonEmpty)
        NCTestUtils.printTokens(toks)
        validate(toks)

    @Test
    def test(): Unit =
        test(
            "Test requests!",
            toks =>
                require(toks.sizeIs == 3)
                require(!isStopWord(toks.head))
                require(isStopWord(toks.last))
        )
        test(
            "Test requests !",
            toks => {
                require(toks.sizeIs == 3)
                require(!isStopWord(toks.head))
                require(isStopWord(toks.last))
            }
        )
        test(
            // First and last are stop words,
            // Third and fourth are not because quoted.
            // Note that "a ` a a` a" parsed as 5 tokens ("a", "`", ""a, "a`", "a") because OpenNLP tokenizer logic,
            // So we use spaces around quotes to simplify test.
            "a ` a a ` a",
            toks => {
                require(toks.sizeIs == 6)
                require(isStopWord(toks.head))
                require(isStopWord(toks.last))
                require(toks.drop(1).reverse.drop(1).forall(p => !isStopWord(p)))
            }
        )
        test(
            // First and last are stop words,
            // Third and fourth are not because brackets.
            "a ( a a ) a",
            toks => {
                require(toks.sizeIs == 6)
                require(isStopWord(toks.head))
                require(isStopWord(toks.last))
                require(toks.drop(1).reverse.drop(1).forall(p => !isStopWord(p)))
            }
        )
        test(
            // Invalid brackets.
            "a ( a a a",
            toks => toks.filter(_.getText != "(").forall(isStopWord)
        )
        test(
            // Nested brackets.
            "< < [ a ] > >",
            toks => require(!isStopWord(toks.find(_.getText == "a").get))
        )

    @Test
    def testNullable(): Unit =
        val reader = new NCResourceReader
        val txt = "parents had files"

        // 1. Nullable.
        var parser = new NCOpenNLPTokenParser(
            reader.getPath("opennlp/en-token.bin"),
            null,
            null
        )

        var tbl = NCAsciiTable("Text", "Lemma", "POS")

        for (t <- parser.tokenize(txt).asScala)
            tbl += (t.getText, t.getLemma, t.getPos)

            require(t.getPos.isEmpty)
            require(t.getText == t.getLemma)

        println(tbl.toString)

        // 2. Not nullable.
        parser = new NCOpenNLPTokenParser(
            reader.getPath("opennlp/en-token.bin"),
            reader.getPath("opennlp/en-pos-maxent.bin"),
            reader.getPath("opennlp/en-lemmatizer.dict")
        )

        tbl = NCAsciiTable("Text", "Lemma", "POS")

        for (t <- parser.tokenize(txt).asScala)
            tbl += (t.getText, t.getLemma, t.getPos)

            require(t.getPos.nonEmpty)
            require(t.getText != t.getLemma)

        println(tbl.toString)


