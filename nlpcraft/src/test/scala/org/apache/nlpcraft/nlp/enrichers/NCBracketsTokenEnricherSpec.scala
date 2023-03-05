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
import nlp.enrichers.NCBracketsTokenEnricher
import nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

/**
  *
  */
class NCBracketsTokenEnricherSpec extends AnyFunSuite:
    private val bracketsEnricher = new NCBracketsTokenEnricher()

    /**
      *
      * @param txt
      * @param brackets
      */
    private def check(txt: String, brackets: Integer*): Unit =
        val toks = EN_TOK_PARSER.tokenize(txt)
        bracketsEnricher.enrich(NCTestRequest(txt), CFG, toks)

        NCTestUtils.printTokens(toks)

        if brackets.isEmpty then require(toks.forall(p => !p[Boolean]("brackets")))
        else toks.foreach (tok => require(!(tok[Boolean]("brackets") ^ brackets.contains(tok.getIndex))))

    test("test") {
        check("A [ B C ] D", 2, 3)
        check("A [ B { C } ] D", 2, 3, 4, 5)
        check("A [ B { C } ] [ [ D ] ] [ E ]", 2, 3, 4, 5, 8, 9, 10, 13)

        // Invalid.
        check("[[a]")
        check("[a[")
        check("{[a[}")
        check("[")
        check("}")
    }
