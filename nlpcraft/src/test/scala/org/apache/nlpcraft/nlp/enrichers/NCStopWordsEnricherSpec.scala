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

package org.apache.nlpcraft.nlp.enrichers

import org.apache.nlpcraft.*
import internal.util.NCResourceReader
import nlp.enrichers.NCEnStopWordsTokenEnricher
import org.junit.jupiter.api.*

import java.util
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCStopWordsEnricherSpec:
    /**
      *
      * @param stopEnricher
      * @param txt
      * @param boolVals
      */
    private def test(stopEnricher: NCEnStopWordsTokenEnricher, txt: String, boolVals: Boolean*): Unit =
        val toks = EN_TOK_PARSER.tokenize(txt)
        require(toks.size == boolVals.size)

        toks.foreach(tok => require(tok.getOpt[Boolean]("stopword").isEmpty))

        val req = NCTestRequest(txt)

        EN_TOK_LEMMA_POS_ENRICHER.enrich(req, CFG, toks)
        stopEnricher.enrich(req, CFG, toks)

        NCTestUtils.printTokens(toks)
        toks.zip(boolVals).foreach { (tok, boolVal) => require(tok.get[Boolean]("stopword") == boolVal) }

    @Test
    def test(): Unit =
        test(
            EN_TOK_STOP_ENRICHER,
            "the test",
            true,
            false
        )
        test(
            new NCEnStopWordsTokenEnricher(Set("test"), Set("the")),
            "the test",
            false,
            true
        )