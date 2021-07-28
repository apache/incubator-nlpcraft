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
import org.apache.nlpcraft.models.stm.indexes.NCSpecModelAdapter.mapper
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util.{List => JList}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.language.implicitConversions

case class NCRelationSpecModelData(note: String, indexes: Seq[Int])

class NCRelationSpecModel extends NCSpecModelAdapter {
    @NCIntent("intent=rel term(rel)~{tok_id() == 'nlpcraft:relation'} term(elem)~{has(tok_groups(), 'G')}*")
    private def onRelation(ctx: NCIntentMatch, @NCIntentTerm("rel") rel: NCToken): NCResult =
        NCResult.json(
            mapper.writeValueAsString(
                NCRelationSpecModelData(
                    note = rel.meta[String]("nlpcraft:relation:note"),
                    indexes = rel.meta[JList[Int]]("nlpcraft:relation:indexes").asScala.toSeq
                )
            )
        )
}

@NCTestEnvironment(model = classOf[NCRelationSpecModel], startClient = true)
class NCRelationSpec extends NCTestContext {
    private def extract(s: String): NCRelationSpecModelData = mapper.readValue(s, classOf[NCRelationSpecModelData])

    @Test
    private[stm] def test(): Unit = {
        checkResult(
            "compare a a and a a",
            extract,
            NCRelationSpecModelData(note = "A", indexes = Seq(1, 3))
        )
        checkResult(
            "b b and b b",
            extract,
            NCRelationSpecModelData(note = "B", indexes = Seq(0, 2))
        )
    }
}