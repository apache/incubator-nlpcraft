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

package org.apache.nlpcraft.nlp.parsers

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.*
import org.apache.nlpcraft.nlp.parsers.NCOpenNLPEntityParser
import org.junit.jupiter.api.*

import java.util
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

/**
  *
  */
class NCOpenNLPEntityParserSpec:
    private val parser =
        val list = new java.util.concurrent.CopyOnWriteArrayList[String]()

        NCUtils.execPar(
            Seq(
                "opennlp/en-ner-location.bin",
                "opennlp/en-ner-money.bin",
                "opennlp/en-ner-person.bin",
                "opennlp/en-ner-organization.bin",
                "opennlp/en-ner-date.bin",
                "opennlp/en-ner-percentage.bin"
            ).map(p => () => list.add(NCResourceReader.getPath(p))))(ExecutionContext.Implicits.global)

        new NCOpenNLPEntityParser(list.asScala.toList)

    /**
      *
      * @param txt
      * @param expected
      */
    private def check(txt: String, expected: String): Unit =
        val req = NCTestRequest(txt)
        val toks = EN_TOK_PARSER.tokenize(txt)
        val ents = parser.parse(req, CFG, toks)

        NCTestUtils.printEntities(txt, ents)

        require(ents.sizeIs == 1)
        require(ents.exists(_.getOpt(s"opennlp:$expected:probability").isDefined))

    /**
      *
      */
    @Test
    def test(): Unit =
        check("today", "date")
        check("Moscow", "location")
        check("10 is 5 % from 200", "percentage")
        check("Tim Cook", "person")
        check("Microsoft", "organization")
        check("Current price is higher for 20 USA dollars", "money")