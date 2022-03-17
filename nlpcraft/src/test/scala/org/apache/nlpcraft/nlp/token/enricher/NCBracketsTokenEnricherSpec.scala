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

package org.apache.nlpcraft.nlp.token.enricher

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.token.enricher.*
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*

/**
  *
  */
class NCBracketsTokenEnricherSpec:
    private val bracketsEnricher = new NCEnBracketsTokenEnricher()

    /**
      *
      * @param txt
      * @param brackets
      */
    private def check(txt: String, brackets: Set[Integer]): Unit =
        val toks = EN_TOK_PARSER.tokenize(txt)
        bracketsEnricher.enrich(NCTestRequest(txt), CFG, toks)
        val seq = toks.asScala.toSeq
        
        NCTestUtils.printTokens(seq)
        
        seq.foreach (tok => require(!(tok.get[Boolean]("brackets") ^ brackets.contains(tok.getIndex))))

    @Test
    def test(): Unit =
        check("A [ B C ] D", Set(2, 3))
        check("A [ B { C } ] D", Set(2, 3, 4, 5))
        check("A [ B { C } ] [ [ D ] ] [ E ]", Set(2, 3, 4, 5, 8, 9, 10, 13))
