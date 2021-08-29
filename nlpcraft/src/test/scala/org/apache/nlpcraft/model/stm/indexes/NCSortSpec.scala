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

object NCSortSpecModelData {
    private def nvl[T](list: JList[T]): Seq[T] = if (list == null) Seq.empty else list.asScala.toSeq

    def apply(
        intentId: String,
        subjnotes: JList[String],
        subjindexes: JList[Int],
        bynotes: JList[String],
        byindexes: JList[Int]
    ): NCSortSpecModelData =
        new NCSortSpecModelData(
            intentId = intentId,
            subjnotes = nvl(subjnotes),
            subjindexes = nvl(subjindexes),
            bynotes = nvl(bynotes),
            byindexes = nvl(byindexes)
        )
}

case class NCSortSpecModelData(
    intentId: String,
    subjnotes: Seq[String] = Seq.empty,
    subjindexes: Seq[Int] = Seq.empty,
    bynotes: Seq[String] = Seq.empty,
    byindexes: Seq[Int] = Seq.empty
)

class NCSortSpecModel extends NCSpecModelAdapter {
    private def mkResult(intentId: String, sort: NCToken) =
        NCResult.json(
            mapper.writeValueAsString(
                NCSortSpecModelData(
                    intentId = intentId,
                    subjnotes = sort.meta[JList[String]]("nlpcraft:sort:subjnotes"),
                    subjindexes = sort.meta[JList[Int]]("nlpcraft:sort:subjindexes"),
                    bynotes = sort.meta[JList[String]]("nlpcraft:sort:bynotes"),
                    byindexes = sort.meta[JList[Int]]("nlpcraft:sort:byindexes")
                )
            )
        )

    @NCIntent(
        "intent=onSort1 " +
        "term(sort)~{# == 'nlpcraft:sort'} " +
        "term(elem)~{has(tok_groups, 'G1')}*"
    )
    private def onSort1(ctx: NCIntentMatch, @NCIntentTerm("sort") sort: NCToken): NCResult =
        mkResult(intentId = "onSort1", sort = sort)

    // `x` is mandatory (difference with `onSort3`)
    @NCIntent(
        "intent=onSort2 " +
        "term(x)={# == 'X'} " +
        "term(sort)~{# == 'nlpcraft:sort'} " +
        "term(elem)~{has(tok_groups, 'G1')}*"
    )
    private def onSort2(ctx: NCIntentMatch, @NCIntentTerm("sort") sort: NCToken): NCResult =
        mkResult(intentId = "onSort2", sort = sort)

    // `y` is optional (difference with `onSort2`)
    @NCIntent(
        "intent=onSort3 " +
        "term(y)~{# == 'Y'} " +
        "term(sort)~{# == 'nlpcraft:sort'} " +
        "term(elem)~{has(tok_groups, 'G1')}*"
    )
    private def onSort3(ctx: NCIntentMatch, @NCIntentTerm("sort") sort: NCToken): NCResult =
        mkResult(intentId = "onSort3", sort = sort)

    @NCIntent(
        "intent=onSort4 " +
        "term(z)~{# == 'Z'} " +
        "term(elem1)~{has(tok_groups, 'G1')}+ " +
        "term(elem2)~{has(tok_groups, 'G2')}+ " +
        "term(sort)~{# == 'nlpcraft:sort'}"
    )
    private def onSort4(ctx: NCIntentMatch, @NCIntentTerm("sort") sort: NCToken): NCResult =
        mkResult(intentId = "onSort4", sort = sort)
}

@NCTestEnvironment(model = classOf[NCSortSpecModel], startClient = true)
class NCSortSpec extends NCTestContext {
    private def extract(s: String): NCSortSpecModelData = mapper.readValue(s, classOf[NCSortSpecModelData])

    @Test
    private[stm] def testOnSort11(): Unit = {
        checkResult(
            "test test sort by a a",
            extract,
            // Reference to variant.
            NCSortSpecModelData(intentId = "onSort1", bynotes = Seq("A2"), byindexes = Seq(3))
        )
        checkResult(
            "test b b",
            extract,
            // Reference to variant.
            NCSortSpecModelData(intentId = "onSort1", bynotes = Seq("B2"), byindexes = Seq(1))
        )
        checkResult(
            "test test sort a a by a a",
            extract,
            // Reference to variant.
            NCSortSpecModelData(
                intentId = "onSort1",
                subjnotes = Seq("A2"),
                subjindexes = Seq(3),
                bynotes = Seq("A2"),
                byindexes = Seq(5)
            )
        )

        checkResult(
            "test test sort a a, a a by a a, a a",
            extract,
            // Reference to variant.
            NCSortSpecModelData(
                intentId = "onSort1",
                subjnotes = Seq("A2", "A2"),
                subjindexes = Seq(3, 5),
                bynotes = Seq("A2", "A2"),
                byindexes = Seq(7, 9)
            )
        )
    }

    @Test
    private[stm] def testOnSort12(): Unit = {
        checkResult(
            "test test sort by a a",
            extract,
            // Reference to variant.
            NCSortSpecModelData(intentId = "onSort1", bynotes = Seq("A2"), byindexes = Seq(3))
        )

        checkResult(
            "test b b",
            extract,
            // Reference to recalculated variant (new changed indexes).
            NCSortSpecModelData(intentId = "onSort1", bynotes = Seq("B2"), byindexes = Seq(1))
        )
    }

    @Test
    private[stm] def testOnSort2(): Unit = {
        checkResult(
            "test test x sort by a a",
            extract,
            // Reference to variant.
            NCSortSpecModelData(intentId = "onSort2", bynotes = Seq("A2"), byindexes = Seq(4))
        )

        checkResult(
            "test x",
            extract,
            // Reference to conversation (tokens by these ID and indexes can be found in conversation).
            NCSortSpecModelData(intentId = "onSort2", bynotes = Seq("A2"), byindexes = Seq(4))
        )
    }

    @Test
    private[stm] def testOnSort3(): Unit = {
        checkResult(
            "test test y sort by a a",
            extract,
            // Reference to variant.
            NCSortSpecModelData(intentId = "onSort3", bynotes = Seq("A2"), byindexes = Seq(4))
        )

        checkResult(
            "test y",
            extract,
            // Reference to conversation (tokens by these ID and indexes can be found in conversation).
            NCSortSpecModelData(intentId = "onSort3", bynotes = Seq("A2"), byindexes = Seq(4))
        )
    }

    // Like `testOnSort11` and `testOnSort12`, but more complex.
    @Test
    private[stm] def testOnSort4(): Unit = {
        checkResult(
            "test z test sort x by a a",
            extract,
            // Reference to variant.
            NCSortSpecModelData(
                intentId = "onSort4",
                subjnotes = Seq("X"),
                subjindexes = Seq(4),
                bynotes = Seq("A2"),
                byindexes = Seq(6)
            )
        )

        checkResult(
            "test z y b b",
            extract,
            // Reference to recalculated variant (new changed indexes).
            NCSortSpecModelData(
                intentId = "onSort4",
                subjnotes = Seq("Y"),
                subjindexes = Seq(2),
                bynotes = Seq("B2"),
                byindexes = Seq(3)
            )
        )
    }
}