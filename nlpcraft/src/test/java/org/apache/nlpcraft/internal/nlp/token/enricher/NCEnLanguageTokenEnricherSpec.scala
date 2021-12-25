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

package org.apache.nlpcraft.internal.nlp.token.enricher

import org.apache.nlpcraft.internal.nlp.token.parser.opennlp.NCEnOpenNlpTokenParser
import org.apache.nlpcraft.internal.nlp.util.{NCTestToken, NCTestUtils}
import org.junit.jupiter.api.{BeforeEach, Test}

import scala.jdk.CollectionConverters.SeqHasAsJava

/**
  *
  */
class NCEnLanguageTokenEnricherSpec:
    private var enricher: NCEnLanguageTokenEnricher = _

    @BeforeEach
    def start(): Unit = enricher = NCTestUtils.makeAndStart(new NCEnLanguageTokenEnricher)

    @Test
    def test(): Unit =
        val toks = Seq(
            NCTestToken(txt = "english", stem = "english"), // English word.
            NCTestToken(txt = "русский", stem = "русский") // Not english word.
        )

        require(toks.head.getOpt[Boolean]("lang:en").isEmpty)
        require(toks.last.getOpt[Boolean]("lang:en").isEmpty)

        enricher.enrich(null, null, toks.asJava)

        NCTestUtils.printTokens(toks, "lang:en")

        require(toks.head.get[Boolean]("lang:en"))
        require(!toks.last.get[Boolean]("lang:en"))