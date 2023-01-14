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

package org.apache.nlpcraft.nlp.stanford

import org.apache.nlpcraft.nlp.stanford.util.*
import org.apache.nlpcraft.nlp.util.{CFG, *}
import org.scalatest.funsuite.AnyFunSuite

/**
  *
  */
class NCStanfordNLPEntityParserSpec extends AnyFunSuite:
    private val parser = NCStanfordNLPEntityParser(STANFORD, Set("city", "date", "number", "email"))

    test("test") {
        val txt = "Los Angeles, 23 August, 23 and sergeykamov@apache.org, tomorrow"

        val toks = TOK_STANFORD_PARSER.tokenize(txt)
        NCTestUtils.printTokens(toks)

        val res = parser.parse(NCTestRequest(txt), CFG, toks)
        NCTestUtils.printEntities(txt, res)

        require(res.size == 5)
    }