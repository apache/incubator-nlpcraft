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

package org.apache.nlpcraft.nlp.token.enricher.en

import org.apache.nlpcraft.nlp.token.enricher.en.NCEnSwearWordsTokenEnricher
import org.apache.nlpcraft.nlp.token.parser.opennlp.en.NCEnOpenNlpTokenParser
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.SeqHasAsJava

/**
  *
  */
class NCEnSwearWordsTokenEnricherSpec:
    private var enricher: NCEnSwearWordsTokenEnricher = _

    @BeforeEach
    def start(): Unit = enricher = NCTestUtils.makeAndStart(new NCEnSwearWordsTokenEnricher("badfilter/swear_words.txt"))

    @Test
    def test(): Unit =
        val toks = Seq(
            NCTestToken(txt = "english", stem = "english", idx = 0), // English word.
            NCTestToken(txt = "ass", stem = "ass", idx = 1) // Swear english word.
        )

        require(toks.head.getOpt[Boolean]("swear:en").isEmpty)
        require(toks.last.getOpt[Boolean]("swear:en").isEmpty)

        enricher.enrich(null, null, toks.asJava)

        NCTestUtils.printTokens(toks)

        require(!toks.head.get[Boolean]("swear:en"))
        require(toks.last.get[Boolean]("swear:en"))