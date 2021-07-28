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

package org.apache.nlpcraft.models.stm.indexes

import org.apache.nlpcraft.model.{NCIntent, NCIntentMatch, NCResult, _}
import org.apache.nlpcraft.models.stm.indexes.NCStmSpecModelAdapter.mapper
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util.{List => JList}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.language.implicitConversions

case class NCStmSortSpecModelData(
    subjnotes: Seq[String] = Seq.empty,
    subjindexes: Seq[Int] = Seq.empty,
    bynotes: Seq[String] = Seq.empty,
    byindexes: Seq[Int] = Seq.empty
)

class NCStmSortSpecModel extends NCStmSpecModelAdapter {
    @NCIntent("intent=onBySort term(sort)~{tok_id() == 'nlpcraft:sort'} term(elem)~{has(tok_groups(), 'G')}")
    private def onBySort(ctx: NCIntentMatch, @NCIntentTerm("sort") sort: NCToken): NCResult =
        NCResult.json(
            mapper.writeValueAsString(
                NCStmSortSpecModelData(
                    bynotes = sort.meta[JList[String]]("nlpcraft:sort:bynotes").asScala.toSeq,
                    byindexes = sort.meta[JList[Int]]("nlpcraft:sort:byindexes").asScala.toSeq
                )
            )
        )
}

@NCTestEnvironment(model = classOf[NCStmSortSpecModel], startClient = true)
class NCStmSortSpec extends NCTestContext {
    private def extract(s: String): NCStmSortSpecModelData = mapper.readValue(s, classOf[NCStmSortSpecModelData])

    @Test
    private[stm] def test(): Unit = {
        checkResult(
            "test test sort by a a",
            extract,
            NCStmSortSpecModelData(bynotes = Seq("A"), byindexes = Seq(3))
        )
        checkResult(
            "test b b",
            extract,
            NCStmSortSpecModelData(bynotes = Seq("B"), byindexes = Seq(1))
        )
    }
}