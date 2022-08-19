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
import org.apache.nlpcraft.nlp.parsers.NCSemanticEntityParser
import org.junit.jupiter.api.*

import java.util
import scala.collection.mutable
import scala.concurrent.ExecutionContext

/**
  *
  */
class NCSemanticEntityParserSpec:
    private val semParser: NCSemanticEntityParser =
        NCTestUtils.mkEnSemanticParser(
            List(
                // Standard.
                E("t1", synonyms = Set("t1")),
                // No extra synonyms.
                E("t2"),
                // Multiple words.
                E("t3", synonyms = Set("t3 t3")),
                // Value. No extra synonyms.
                E("t4", values = Map("value4" -> Set.empty)),
                // Value. Multiple words.
                E("t5", values = Map("value5" -> Set("value 5"))),
                // Elements data.
                E("t6", props = Map("testKey" -> "testValue")),
                // Regex.
                E("t7", synonyms = Set("x //[a-d]+//")),
                // Empty synonyms.
                E("t8", synonyms = Set("{A|_} {B|_}"))
            )
        )

    /**
      *
      * @param txt
      * @param id
      * @param value
      * @param elemData
      */
    private def check(txt: String, id: String, value: Option[String] = None, elemData: Option[Map[String, Any]] = None): Unit =
        val req = NCTestRequest(txt)
        val toks = EN_TOK_PARSER.tokenize(txt)

        EN_TOK_LEMMA_POS_ENRICHER.enrich(req, CFG, toks)
        EN_TOK_STOP_ENRICHER.enrich(req, CFG, toks)

        NCTestUtils.printTokens(toks)

        val ents = semParser.parse(req, CFG, toks)

        NCTestUtils.printEntities(txt, ents)
        require(ents.sizeIs == 1)

        val e = ents.head
        require(e.getId == id)

        value match
            case Some(v) => require(e.get[Any](s"$id:value") == v)
            case None => // No-op.
        elemData match
            case Some(m) => m.foreach { (k, v) => require(e.get[Any](s"$id:$k") == v) }
            case None => // No-op.

    /**
      *
      * @param txt
      * @param ids
      */
    private def checkMultiple(txt: String, ids: String*): Unit =
        val req = NCTestRequest(txt)
        val toks = EN_TOK_PARSER.tokenize(txt)

        EN_TOK_LEMMA_POS_ENRICHER.enrich(req, CFG, toks)
        EN_TOK_STOP_ENRICHER.enrich(req, CFG, toks)

        NCTestUtils.printTokens(toks)

        val ents = semParser.parse(req, CFG, toks)

        NCTestUtils.printEntities(txt, ents)
        require(ents.sizeIs == ids.size)
        ents.map(_.getId).sorted.zip(ids.sorted).foreach { case (eId, id) => require(eId == id) }

    /**
      *
      */
    @Test
    def test(): Unit =
        check("t1", "t1")
        check("the t1", "t1")
        check("t2", "t2")
        check("the t2", "t2")
        check("t3 t3", "t3")
        check("t3 the t3", "t3") // With stopword inside.
        check("value4", "t4", value = Option("value4"))
        check("value the 5", "t5", value = Option("value5")) // With stopword inside.
        check("t6", "t6", elemData = Option(Map("testKey" -> "testValue")))
        check("the x abc x abe", "t7") // `x abc` should be matched, `x abe` shouldn't.
        check("A B", "t8")
        check("A", "t8")
        check("B", "t8")

        checkMultiple("t1 the x abc the x the abc", "t1", "t7", "t7")