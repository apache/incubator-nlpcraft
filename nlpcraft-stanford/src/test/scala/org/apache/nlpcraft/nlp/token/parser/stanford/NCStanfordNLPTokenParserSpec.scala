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

package org.apache.nlpcraft.nlp.token.parser.stanford

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.stanford.*
import org.junit.jupiter.api.*

import java.util.Properties
import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCStanfordNLPTokenParserSpec:
    @Test
    def test(): Unit =
        val toks = TOK_STANFORD_PARSER.tokenize("I had a lunch with brand names 'AAA'").asScala.toSeq

        require(toks.sizeIs > 1)
        NCTestUtils.printTokens(toks)

        val words = toks.map(_.getText)
        require(toks.map(_.get["String"]("pos")).filter(_ != null).distinct.sizeIs > 1)
        require(toks.map(_.get[String]("lemma")).filter(_ != null).zip(words).exists {_ != _})
