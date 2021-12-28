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

package org.apache.nlpcraft.internal.nlp.token.parser.opennlp

import org.apache.nlpcraft.internal.ascii.NCAsciiTable
import org.apache.nlpcraft.internal.nlp
import org.apache.nlpcraft.internal.nlp.util.*
import org.apache.nlpcraft.*
import org.junit.jupiter.api.*

import java.util
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCEnOpenNlpTokenParserSpec:
    private var parser: NCEnOpenNlpTokenParser = _

    @BeforeEach
    def start(): Unit =
        parser = NCTestUtils.makeAndStart(
            new NCEnOpenNlpTokenParser(
                "opennlp/en-token.bin",
                "opennlp/en-pos-maxent.bin",
                "opennlp/en-lemmatizer.dict"
            )
        )

    private def test(txt: String, validate: Seq[NCToken] => _): Unit =
        val toks = parser.parse(nlp.util.NCTestRequest(txt), null).asScala.toSeq
        assert(toks.nonEmpty)
        NCTestUtils.printTokens(toks)
        validate(toks)

    @Test
    def test(): Unit =
        test(
            "Test requests!",
            toks =>
                require(toks.length == 3);
                require(!toks.head.isStopWord);
                require(toks.last.isStopWord)
        )
        test(
            "Test requests !",
            toks =>
                require(toks.length == 3);
                require(!toks.head.isStopWord);
                require(toks.last.isStopWord)
        )
        test(
            // First and last are stop words,
            // Third and fourth are not because quoted.
            // Note that "A ` A A` A" parsed as 5 tokens ("A", "`", ""A, "A`", "A") because OpenNLP tokenizer logic,
            // So we use spaces around quotes to simplify test.
            "A ` A A ` A",
            toks =>
                require(toks.length == 6);
                require(toks.head.isStopWord);
                require(toks.last.isStopWord);
                require(toks.drop(1).reverse.drop(1).forall(!_.isStopWord))
        )
        test(
            // First and last are stop words,
            // Third and fourth are not because brackets.
            "A ( A A ) A",
            toks =>
                require(toks.length == 6);
                require(toks.head.isStopWord);
                require(toks.last.isStopWord);
                require(toks.drop(1).reverse.drop(1).forall(!_.isStopWord))
        )
        test(
            // Invalid brackets.
            "A ( A A A",
            toks => toks.filter(_.getNormalizedText != "(").forall(_.isStopWord)
        )
        test(
            // Nested brackets.
            "< < [ A ] > >",
            toks => require(!toks.find(_.getNormalizedText == "a").get.isStopWord)
        )
