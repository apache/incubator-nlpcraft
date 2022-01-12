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

package org.apache.nlpcraft.nlp.entity.parser.stanford

import org.apache.nlpcraft.nlp.entity.parser.stanford.NCStanfordEntityParser
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordTokenParser
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.stanford.*
import org.junit.jupiter.api.Test

import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCStanfordEntityParserSpec:
    private val parser = NCStanfordEntityParser(STANFORD, Set("city", "date", "number", "email").asJava)

    @Test
    def test(): Unit =
        val txt = "Los Angeles, 23 August, 23 and sergeykamov@apache.org"

        val toks = EN_STANFORD_PIPELINE.getTokenParser.tokenize(txt)
        NCTestUtils.printTokens(toks.asScala.toSeq)

        val res = parser.parse(NCTestRequest(txt), CFG, toks)
        NCTestUtils.printEntities(txt, res.asScala.toSeq)

        require(res.size() == 4)