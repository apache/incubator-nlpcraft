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

package org.apache.nlpcraft.nlp.entity.parser.opennlp

import org.apache.nlpcraft.nlp.entity.parser.opennlp.NCOpenNlpEntityParser
import org.apache.nlpcraft.nlp.token.parser.opennlp.en.NCEnOpenNlpTokenParser
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.junit.jupiter.api.*

import java.util
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

/**
  *
  */
class NCOpenNlpEntityParserSpec:
    private val eParsers = scala.collection.mutable.ArrayBuffer.empty[NCOpenNlpEntityParser]
    private var tParser: NCEnOpenNlpTokenParser = _

    @BeforeEach
    def start(): Unit =
        tParser = NCTestUtils.makeAndStart(NCTestUtils.mkEnParser)

        def add(res: String): Unit =
            eParsers += NCTestUtils.makeAndStart(new NCOpenNlpEntityParser(s"opennlp/$res"))

        NCUtils.execPar(
            // en-ner-time.bin is skipped. I can't find any working example.
            () => add("en-ner-location.bin"),
            () => add("en-ner-money.bin"),
            () => add("en-ner-person.bin"),
            () => add("en-ner-organization.bin"),
            () => add("en-ner-date.bin"),
            () => add("en-ner-percentage.bin")
        )(ExecutionContext.Implicits.global)

    private def checkSingleEntity(txt: String, expected: String): Unit =
        val req = NCTestRequest(txt)
        val toks = tParser.parse(req, null)
        val resSeq = eParsers.map(_.parse(req, null, toks).asScala.toSeq).filter(_.size == 1)

        require(resSeq.size == 1)

        val res = resSeq.head
        NCTestUtils.printEntities(txt, res)
        require(res.exists(_.getOpt(s"opennlp:$expected:probability").isPresent))

    @Test
    def test(): Unit =
        checkSingleEntity("today", "date")
        checkSingleEntity("Moscow", "location")
        checkSingleEntity("10 is 5 % from 200", "percentage")
        checkSingleEntity("Tim Cook", "person")
        checkSingleEntity("Microsoft", "organization")
        checkSingleEntity("Current price is higher for 20 USA dollars", "money")