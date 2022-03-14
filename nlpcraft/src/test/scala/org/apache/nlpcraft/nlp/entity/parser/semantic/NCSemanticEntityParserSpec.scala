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

package org.apache.nlpcraft.nlp.entity.parser.semantic

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.*
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.token.enricher.*
import org.apache.nlpcraft.nlp.token.enricher.en.*
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.*

import java.util
import java.util.{List as JList, Map as JMap, Set as JSet}
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

/**
  *
  * @param id
  * @param synonyms
  * @param values
  * @param groups
  */
case class NCSemanticTestElement(
    id: String,
    synonyms: Set[String] = Set.empty,
    values: Map[String, Set[String]] = Map.empty,
    groups: Seq[String] = Seq.empty,
    props: Map[String, AnyRef] = Map.empty
) extends NCSemanticElement:
    override def getId: String = id
    override def getGroups: JSet[String] = groups.toSet.asJava
    override def getValues: JMap[String, JSet[String]] = values.map { (k, v) => k -> v.asJava}.asJava
    override def getSynonyms: JSet[String] = synonyms.asJava
    override def getProperties: JMap[String, Object] = props.asJava

/**
  *
  */
object NCSemanticTestElement:
    def apply(id: String, synonyms: String*) = new NCSemanticTestElement(id, synonyms = synonyms.toSet)

/**
  *
  */
class NCSemanticEntityParserSpec:
    private val parser =
        new NCEnSemanticEntityParser(
            Seq(
                // Standard.
                NCSemanticTestElement("t1", synonyms = Set("t1")),
                // No extra synonyms.
                NCSemanticTestElement("t2"),
                // Multiple words.
                NCSemanticTestElement("t3", synonyms = Set("t3 t3")),
                // Value. No extra synonyms.
                NCSemanticTestElement("t4", values = Map("value4" -> Set.empty)),
                // Value. Multiple words.
                NCSemanticTestElement("t5", values = Map("value5" -> Set("value 5"))),
                // Elements data.
                NCSemanticTestElement("t6", props = Map("testKey" -> "testValue")),
                // Regex.
                NCSemanticTestElement("t7", synonyms = Set("x //[a-d]+//"))

            ).asJava
        )

    private val stopWordsEnricher = new NCEnStopWordsTokenEnricher()

    private val lemmaPosEnricher = new NCEnOpenNLPLemmaPosTokenEnricher()

    /**
      *
      * @param txt
      * @param id
      * @param value
      * @param elemData
      */
    private def check(txt: String, id: String, value: Option[String] = None, elemData: Option[Map[String, Any]] = None): Unit =
        val req = NCTestRequest(txt)
        val toks = EN_PIPELINE.getTokenParser.tokenize(txt)

        lemmaPosEnricher.enrich(req, CFG, toks)
        stopWordsEnricher.enrich(req, CFG, toks)

        NCTestUtils.printTokens(toks.asScala.toSeq)

        val ents = parser.parse(req, CFG, toks).asScala.toSeq

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
        val toks = EN_PIPELINE.getTokenParser.tokenize(txt)

        lemmaPosEnricher.enrich(req, CFG, toks)
        stopWordsEnricher.enrich(req, CFG, toks)

        NCTestUtils.printTokens(toks.asScala.toSeq)

        val ents = parser.parse(req, CFG, toks).asScala.toSeq

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

        checkMultiple("t1 the x abc the x the abc", "t1", "t7", "t7")