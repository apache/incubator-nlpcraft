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

import org.apache.nlpcraft.model.impl.NCTokenPimp
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.apache.nlpcraft.model.{NCIntentMatch, _}
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.{ListHasAsScala, SeqHasAsJava, SetHasAsJava}
import scala.language.implicitConversions
import java.util
import java.util.Collections

class NCStmIndexesTestModel extends NCModelAdapter("nlpcraft.stm.idxs.test", "STM Indexes Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            mkElement("A", "G"),
            mkElement("B", "G")
        ).asJava

    private def mkElement(id: String, group: String): NCElement =
        new NCElement {
            override def getId: String = id
            override def getSynonyms: util.List[String] = Collections.singletonList(id)
            override def getGroups: util.List[String] = Collections.singletonList(group)
        }

    @NCIntent("intent=i term(sort)~{tok_id() == 'nlpcraft:sort'} term(elem)={has(tok_groups(), 'G')}")
    private def onI(ctx: NCIntentMatch, @NCIntentTerm("sort") sort: NCToken): NCResult = {
        val bynotes = sort.meta[java.util.List[String]]("nlpcraft:sort:bynotes")
        val byindexes = sort.meta[java.util.List[String]]("nlpcraft:sort:byindexes")

        def str(t: NCToken): String = s"${t.origText}(${t.index})"

        println(s"variant: ${ctx.getVariant.asScala.map(str).mkString("|")}")
        println(s"variant freeTokens: ${ctx.getVariant.getFreeTokens.asScala.map(str).mkString("|")}")
        println(s"variant matchedTokens: ${ctx.getVariant.getMatchedTokens.asScala.map(str).mkString("|")}")
        println(s"variant userDefinedTokens: ${ctx.getVariant.getUserDefinedTokens.asScala.map(str).mkString("|")}")
        println(s"variant conversation: ${ctx.getContext.getConversation.getTokens.asScala.map(str).mkString("|")}")
        println(s"bynotes: $bynotes")
        println(s"byindexes: $byindexes")

        NCResult.text("OK")
    }
}

@NCTestEnvironment(model = classOf[NCStmIndexesTestModel], startClient = true)
class NCStmIndexesTestModelSpec extends NCTestContext {
    @Test
    private[stm] def test(): Unit = {
        checkResult("test test sort by A", "OK")
        checkResult("B", "OK")
    }
}