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

package org.apache.nlpcraft.model.stm.indexes

import org.apache.nlpcraft.model.{NCIntent, NCIntentMatch, NCResult, _}
import NCSpecModelAdapter.mapper
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util.{List => JList}
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.language.implicitConversions

case class NCRelationSpecModelData(intentId: String, note: String, indexes: Seq[Int])

class NCRelationSpecModel extends NCSpecModelAdapter {
    private def mkResult(intentId: String, rel: NCToken) =
        NCResult.json(
            mapper.writeValueAsString(
                NCRelationSpecModelData(
                    intentId = intentId,
                    note = rel.meta[String]("nlpcraft:relation:note"),
                    indexes = rel.meta[JList[Int]]("nlpcraft:relation:indexes").asScala.toSeq
                )
            )
        )

    @NCIntent(
        "intent=rel1 " +
        "term(rel)~{# == 'nlpcraft:relation'} " +
        "term(elem)~{has(tok_groups, 'G1')}*"
    )
    private def onRelation1(ctx: NCIntentMatch, @NCIntentTerm("rel") rel: NCToken): NCResult =
        mkResult(intentId = "rel1", rel = rel)

    // `x` is mandatory (difference with `rel3`)
    @NCIntent(
        "intent=rel2 " +
            "term(x)={# == 'X'} " +
            "term(rel)~{# == 'nlpcraft:relation'} " +
            "term(elem)~{has(tok_groups, 'G1')}*"
    )
    private def onRelation2(ctx: NCIntentMatch, @NCIntentTerm("rel") rel: NCToken): NCResult =
        mkResult(intentId = "rel2", rel = rel)

    // `y` is optional (difference with `rel2`)
    @NCIntent(
        "intent=rel3 " +
        "term(y)~{# == 'Y'} " +
        "term(rel)~{# == 'nlpcraft:relation'} " +
        "term(elem)~{has(tok_groups, 'G1')}*"
    )
    private def onRelation3(ctx: NCIntentMatch, @NCIntentTerm("rel") rel: NCToken): NCResult =
        mkResult(intentId = "rel3", rel = rel)
}

@NCTestEnvironment(model = classOf[NCRelationSpecModel], startClient = true)
class NCRelationSpec extends NCTestContext {
    private def extract(s: String): NCRelationSpecModelData = mapper.readValue(s, classOf[NCRelationSpecModelData])

    @Test
    private[stm] def test1(): Unit = {
        checkResult(
            "compare a a and a a",
            extract,
            // Reference to variant.
            NCRelationSpecModelData(intentId = "rel1", note = "A2", indexes = Seq(1, 3))
        )
        checkResult(
            "b b and b b",
            extract,
            // Reference to recalculated variant (new changed indexes).
            NCRelationSpecModelData(intentId = "rel1", note = "B2", indexes = Seq(0, 2))
        )
    }

    @Test
    private[stm] def test2(): Unit = {
        checkResult(
            "x compare a a and a a",
            extract,
            // Reference to variant.
            NCRelationSpecModelData(intentId = "rel2", note = "A2", indexes = Seq(2, 4))
        )
        checkResult(
            "test x",
            extract,
            // Reference to conversation (tokens by these ID and indexes can be found in conversation).
            NCRelationSpecModelData(intentId = "rel2", note = "A2", indexes = Seq(2, 4))
        )
    }

    @Test
    private[stm] def test3(): Unit = {
        checkResult(
            "y compare a a and a a",
            extract,
            // Reference to variant.
            NCRelationSpecModelData(intentId = "rel3", note = "A2", indexes = Seq(2, 4))
        )
        checkResult(
            "test y",
            extract,
            // Reference to conversation (tokens by these ID and indexes can be found in conversation).
            NCRelationSpecModelData(intentId = "rel3", note = "A2", indexes = Seq(2, 4))
        )
    }
}