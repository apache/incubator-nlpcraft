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

import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.token.enricher.*
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*


/**
  *
  */
class NCSwearWordsTokenEnricherSpec:
    private val swEnricher = new NCEnSwearWordsTokenEnricher(NCResourceReader.getPath("badfilter/swear_words.txt"))

    @Test
    def test(): Unit =
        val toks = EN_TOK_PARSER.tokenize("english ass")

        require(toks.head.getOpt[Boolean]("swear:en").isEmpty)
        require(toks.last.getOpt[Boolean]("swear:en").isEmpty)

        swEnricher.enrich(null, null, toks)

        NCTestUtils.printTokens(toks)

        require(!toks.head.get[Boolean]("swear"))
        require(toks.last.get[Boolean]("swear"))