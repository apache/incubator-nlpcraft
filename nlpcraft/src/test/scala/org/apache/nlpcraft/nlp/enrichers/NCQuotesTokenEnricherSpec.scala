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
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.nlp.enrichers.*
import org.apache.nlpcraft.nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

/**
  *
  */
class NCQuotesTokenEnricherSpec extends AnyFunSuite:
    private val quoteEnricher = new NCQuotesTokenEnricher

    /**
      *
      * @param txt
      * @param quotes
      */
    private def check(txt: String, quotes: Integer*): Unit =
        val toks = EN_TOK_PARSER.tokenize(txt)

        val req = NCTestRequest(txt)
        EN_TOK_LEMMA_POS_ENRICHER.enrich(req, CFG, toks)
        quoteEnricher.enrich(req, CFG, toks)
        
        NCTestUtils.printTokens(toks)
        toks.foreach (tok => require(!(tok[Boolean]("quoted") ^ quotes.contains(tok.getIndex))))

    test("test") {
        check("It called ' test data '", 3, 4)
        check("It called \" test data \"", 3, 4)
        check("It called « test data »", 3, 4)
        check("It called ' test data ' , ' test data '", 3, 4, 8, 9)

        // Invalid.
        check("It called ' test data ' '")
        check("It called ' test data `")
        check("It called ' test data ' `")
        check("It called « test data '")
        check("It called « test data ' »")
        check("It called « test data «")
        check("It called » test data »")
        check("'")
        check("\"a\"\"")

        // Empty.
        check("It called ' ' test data ' '")

        // Nested.
        check("It called \" ' test data ' \"", 3, 4, 5, 6)
        check("It called ' \" test data \" '", 3, 4, 5, 6)
        check("It called « \" test data \" »", 3, 4, 5, 6)
        check("It called « \" ' test data ' \" »", 3, 4, 5, 6, 7, 8)
    }