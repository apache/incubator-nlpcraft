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

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.token.enricher.en.*
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.*

import java.util
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCStopWordsEnricherSpec:
    /**
      *
      * @param enricher
      * @param txt
      * @param boolVals
      */
    private def test(enricher: NCStopWordsTokenEnricher, txt: String, boolVals: Boolean*): Unit =
        val toksList = EN_PIPELINE.getTokenParser.tokenize(txt)
        require(toksList.size == boolVals.size)
        val toks = toksList.asScala.toSeq

        toks.foreach(tok => require(tok.getOpt[Boolean]("stopword").isEmpty))

        enricher.enrich(NCTestRequest(txt), CFG, toksList)

        NCTestUtils.printTokens(toks)
        toks.zip(boolVals).foreach { (tok, boolVal) => require(tok.get[Boolean]("stopword") == boolVal) }

    @Test
    def test(): Unit =
        test(
            new NCStopWordsTokenEnricher(),
            "the test",
            true,
            false
        )
        test(
            new NCStopWordsTokenEnricher(Set("test").asJava, Set("the").asJava),
            "the test",
            false,
            true
        )