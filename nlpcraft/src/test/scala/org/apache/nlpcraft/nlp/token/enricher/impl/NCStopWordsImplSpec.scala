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

package org.apache.nlpcraft.nlp.token.enricher.impl

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.token.enricher.NCEnStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*


/**
  *
  */
class NCStopWordsImplSpec:
    case class W(text: String, stop: Boolean)

    /**
      *
      * @param words
      * @param expected
      */
    private def test0(words: Seq[W], expected: Seq[String]): Unit =
        val toks = EN_TOK_PARSER.tokenize(words.map(_.text).mkString(" "))
        require(toks.size == words.size)

        toks.zip(words).foreach { (t, w) => t.put("stopword", w.stop) }

        val mix = NCEnStopWordsTokenEnricher.tokenMixWithStopWords(toks)

        val resSorted = mix.map(_.map(_.getText).mkString).sorted
        val expectedSorted = expected.sorted

        require(expectedSorted == resSorted, s"Expected=$expectedSorted, result=$resSorted")

    @Test
    def testPermute(): Unit =
        test0(
            Seq(W("A", false), W("B", true), W("C", true)),
            Seq("ABC", "AB", "AC", "BC", "A", "B", "C")
        )
        test0(
            Seq(W("A", false), W("B", false), W("C", false)),
            Seq("ABC", "AB", "BC", "A", "B", "C")
        )
