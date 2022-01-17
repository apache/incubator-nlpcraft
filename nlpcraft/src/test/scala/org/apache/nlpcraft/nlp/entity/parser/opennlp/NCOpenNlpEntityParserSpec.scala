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

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.entity.parser.opennlp.NCOpenNlpEntityParser
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
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
    private val parser = new NCOpenNlpEntityParser(
        Seq(
            "opennlp/en-ner-location.bin",
            "opennlp/en-ner-money.bin",
            "opennlp/en-ner-person.bin",
            "opennlp/en-ner-organization.bin",
            "opennlp/en-ner-date.bin",
            "opennlp/en-ner-percentage.bin"
        ).asJava
    )

    /**
      *
      * @param txt
      * @param expected
      */
    private def check(txt: String, expected: String): Unit =
        val req = NCTestRequest(txt)
        val toks = EN_PIPELINE.getTokenParser.tokenize(txt)
        val ents = parser.parse(req, CFG, toks).asScala.toSeq

        NCTestUtils.printEntities(txt, ents)

        require(ents.sizeIs == 1)
        require(ents.exists(_.getOpt(s"opennlp:$expected:probability").isPresent))

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