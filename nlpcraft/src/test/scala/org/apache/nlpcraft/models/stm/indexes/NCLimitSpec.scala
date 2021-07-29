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

case class NCLimitSpecModelData(intentId: String, note: String, indexes: Seq[Int])

class NCLimitSpecModel extends NCSpecModelAdapter {
    private def mkResult(intentId: String, limit: NCToken) =
        NCResult.json(
            mapper.writeValueAsString(
                NCLimitSpecModelData(
                    intentId = intentId,
                    note = limit.meta[String]("nlpcraft:limit:note"),
                    indexes = limit.meta[JList[Int]]("nlpcraft:limit:indexes").asScala.toSeq
                )
            )
        )

    @NCIntent(
        "intent=limit1 " +
        "term(limit)~{tok_id() == 'nlpcraft:limit'} " +
        "term(elem)~{has(tok_groups(), 'G1')}"
    )
    private def onLimit1(ctx: NCIntentMatch, @NCIntentTerm("limit") limit: NCToken): NCResult =
        mkResult(intentId = "limit1", limit = limit)

    // `x` is mandatory (difference with `limit3`)
    @NCIntent(
        "intent=limit2 " +
        "term(x)={tok_id() == 'X'} " +
        "term(limit)~{tok_id() == 'nlpcraft:limit'} " +
        "term(elem)~{has(tok_groups(), 'G1')}"
    )
    private def onLimit2(ctx: NCIntentMatch, @NCIntentTerm("limit") limit: NCToken): NCResult =
        mkResult(intentId = "limit2", limit = limit)

    // `y` is optional (difference with `limit2`)
    @NCIntent(
        "intent=limit3 " +
            "term(y)~{tok_id() == 'Y'} " +
            "term(limit)~{tok_id() == 'nlpcraft:limit'} " +
            "term(elem)~{has(tok_groups(), 'G1')}"
    )
    private def onLimit3(ctx: NCIntentMatch, @NCIntentTerm("limit") limit: NCToken): NCResult =
        mkResult(intentId = "limit3", limit = limit)
}

@NCTestEnvironment(model = classOf[NCLimitSpecModel], startClient = true)
class NCLimitSpec extends NCTestContext {
    private def extract(s: String): NCLimitSpecModelData = mapper.readValue(s, classOf[NCLimitSpecModelData])

    @Test
    private[stm] def test1(): Unit = {
        checkResult(
            "top 23 a a",
            extract,
            // Reference to variant.
            NCLimitSpecModelData(intentId = "limit1", note = "A2", indexes = Seq(1))
        )
        checkResult(
            "test test b b",
            extract,
            // Reference to recalculated variant (new changed indexes).
            NCLimitSpecModelData(intentId = "limit1", note = "B2", indexes = Seq(2))
        )
    }

    @Test
    private[stm] def test2(): Unit = {
        checkResult(
            "x test top 23 a a",
            extract,
            // Reference to variant.
            NCLimitSpecModelData(intentId = "limit2", note = "A2", indexes = Seq(3))
        )
        checkResult(
            "test x",
            extract,
            // Reference to conversation (tokens by these ID and indexes can be found in conversation).
            NCLimitSpecModelData(intentId = "limit2", note = "A2", indexes = Seq(3))
        )
    }

    @Test
    private[stm] def test3(): Unit = {
        checkResult(
            "y test top 23 a a",
            extract,
            // Reference to variant.
            NCLimitSpecModelData(intentId = "limit3", note = "A2", indexes = Seq(3))
        )
        checkResult(
            "test y",
            extract,
            // Reference to conversation (tokens by these ID and indexes can be found in conversation).
            NCLimitSpecModelData(intentId = "limit3", note = "A2", indexes = Seq(3))
        )
    }
}