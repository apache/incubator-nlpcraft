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

package org.apache.nlpcraft.models.stm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult, _}
import org.apache.nlpcraft.models.stm.NCStmIndexesTestModelData.mapper
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import java.util.Collections
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsScala, SeqHasAsJava, SetHasAsJava}
import scala.language.implicitConversions
import java.util.{List => JList}

object NCStmIndexesTestModelData {
    val mapper = new ObjectMapper()

    mapper.registerModule(new DefaultScalaModule())
}

case class NCStmIndexesTestModelData(
    subjnotes: Seq[String] = Seq.empty,
    subjindexes: Seq[Int] = Seq.empty,
    bynotes: Seq[String] = Seq.empty,
    byindexes: Seq[Int] = Seq.empty
)

class NCStmIndexesTestModel extends NCModelAdapter("nlpcraft.stm.idxs.test", "STM Indexes Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            mkElement("A", "G", "a a"),
            mkElement("B", "G", "b b")
        ).asJava

    private def mkElement(id: String, group: String, syns: String*): NCElement =
        new NCElement {
            override def getId: String = id
            override def getSynonyms: util.List[String] = {
                val seq: Seq[String] = syns

                seq.asJava
            }
            override def getGroups: util.List[String] = Collections.singletonList(group)
        }

    @NCIntent("intent=onBySort term(sort)~{tok_id() == 'nlpcraft:sort'} term(elem)={has(tok_groups(), 'G')}")
    private def onBySort(ctx: NCIntentMatch, @NCIntentTerm("sort") sort: NCToken): NCResult = {
        def toStr(t: NCToken): String = s"${t.origText}(${t.index})"

        println(s"sort: $sort")
        println(s"sort-nlp-meta: ${sort.getMetadata.asScala.filter(_._1.startsWith("nlpcraft:nlp:")).mkString(", ")}")
        println(s"sort-not-nlp-meta: ${sort.getMetadata.asScala.filter(!_._1.startsWith("nlpcraft:nlp:")).mkString(", ")}")

        println(s"variant: ${ctx.getVariant.asScala.map(toStr).mkString("|")}")
        println(s"variant freeTokens: ${ctx.getVariant.getFreeTokens.asScala.map(toStr).mkString("|")}")
        println(s"variant matchedTokens: ${ctx.getVariant.getMatchedTokens.asScala.map(toStr).mkString("|")}")
        println(s"variant userDefinedTokens: ${ctx.getVariant.getUserDefinedTokens.asScala.map(toStr).mkString("|")}")
        println(s"variant conversation: ${ctx.getContext.getConversation.getTokens.asScala.map(toStr).mkString("|")}")

        NCResult.json(
            mapper.writeValueAsString(
                NCStmIndexesTestModelData(
                    bynotes = sort.meta[JList[String]]("nlpcraft:sort:bynotes").asScala.toSeq,
                    byindexes = sort.meta[JList[Int]]("nlpcraft:sort:byindexes").asScala.toSeq
                )
            )
        )
    }
}

@NCTestEnvironment(model = classOf[NCStmIndexesTestModel], startClient = true)
class NCStmIndexesTestModelSpec extends NCTestContext {
    private def extract(s: String): NCStmIndexesTestModelData = mapper.readValue(s, classOf[NCStmIndexesTestModelData])

    @Test
    private[stm] def test(): Unit = {
        checkResult(
            "test test sort by a a",
            extract,
            NCStmIndexesTestModelData(bynotes = Seq("A"), byindexes = Seq(3))
        )
        checkResult(
            "b b",
            extract,
            NCStmIndexesTestModelData(bynotes = Seq("B"), byindexes = Seq(0))
        )
    }
}