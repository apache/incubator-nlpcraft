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

package org.apache.nlpcraft.internal.intent.compiler.functions

import org.apache.nlpcraft.internal.intent.compiler.functions.NCIdlFunctions.*
import org.junit.jupiter.api.Test

import java.lang.{Boolean as JBool, Integer as JInt}
import scala.language.implicitConversions

/**
  * Tests for 'entities' functions.
  */
class NCIdlFunctionsEntity extends NCIdlFunctions:
    private final val meta: Map[String, AnyRef] = Map(
        "nlpcraft:nlp:stopword" -> JBool.TRUE,
        "nlpcraft:nlp:freeword" -> JBool.TRUE,
        "nlpcraft:nlp:origtext" -> "orig text",
        "nlpcraft:nlp:index" -> JInt.valueOf(11),
        "nlpcraft:nlp:normtext" -> "norm text",
        "nlpcraft:nlp:direct" -> JBool.TRUE,
        "nlpcraft:nlp:english" -> JBool.TRUE,
        "nlpcraft:nlp:swear" -> JBool.TRUE,
        "nlpcraft:nlp:quoted" -> JBool.TRUE,
        "nlpcraft:nlp:bracketed" -> JBool.TRUE,
        "nlpcraft:nlp:dict" -> JBool.TRUE,
        "nlpcraft:nlp:lemma" -> "lemma",
        "nlpcraft:nlp:stem" -> "stem",
        "nlpcraft:nlp:sparsity" -> JInt.valueOf(112),
        "nlpcraft:nlp:pos" -> "pos",
        "nlpcraft:nlp:unid" -> "21421"
    )

    private def mkMeta(truth: String): TestDesc = TestDesc(truth = truth, entity = mkEntity(meta = meta))

    @Test
    def testMainTokenProperties(): Unit =
        test(
            TestDesc(
                truth = "# == 'a'",
                entity = mkEntity(id = "a")
            ),
            mkMeta(truth = s"tok_lemma == '${meta("nlpcraft:nlp:lemma")}'"),
            mkMeta(truth = s"tok_stem == '${meta("nlpcraft:nlp:stem")}'"),
            mkMeta(truth = s"tok_pos == '${meta("nlpcraft:nlp:pos")}'"),
            mkMeta(truth = s"tok_sparsity == ${meta("nlpcraft:nlp:sparsity")}"),
            mkMeta(truth = s"tok_unid == '${meta("nlpcraft:nlp:unid")}'"),
            TestDesc(
                truth = s"tok_is_abstract()",
                entity = mkEntity(`abstract` = true)
            ),
            mkMeta(truth = s"tok_is_abstract == false"),
            mkMeta(truth = s"tok_is_bracketed == ${meta("nlpcraft:nlp:bracketed")}"),
            mkMeta(truth = s"tok_is_direct == ${meta("nlpcraft:nlp:direct")}"),
            mkMeta(truth = s"tok_is_permutated != ${meta("nlpcraft:nlp:direct")}"),
            mkMeta(truth = s"tok_is_english == ${meta("nlpcraft:nlp:english")}"),
            mkMeta(truth = s"tok_is_freeword == ${meta("nlpcraft:nlp:freeword")}"),
            mkMeta(truth = s"tok_is_quoted == ${meta("nlpcraft:nlp:quoted")}"),
            mkMeta(truth = s"tok_is_stopword == ${meta("nlpcraft:nlp:stopword")}"),
            mkMeta(truth = s"tok_is_swear == ${meta("nlpcraft:nlp:swear")}"),
            TestDesc(
                truth = s"tok_is_user()",
                entity = mkEntity(id = "aa")
            ),
            TestDesc(
                truth = s"!tok_is_user()",
                entity = mkEntity(id = "nlpcraft:nlp")
            ),
            mkMeta(truth = s"tok_is_wordnet() == ${meta("nlpcraft:nlp:dict")}"),
            TestDesc(
                truth = s"tok_ancestors() == list('1', '2')",
                entity = mkEntity(ancestors = Seq("1", "2"))
            ),
            TestDesc(
                truth = s"tok_parent() == 'parentId'",
                entity = mkEntity(parentId = "parentId")
            ),
            TestDesc(
                truth = "tok_groups() == list('1', '2')",
                entity = mkEntity(groups = Seq("1", "2"))
            ),
            TestDesc(
                truth = "tok_value() == 'value'",
                entity = mkEntity(value = "value")
            ),
            TestDesc(
                truth = "tok_value() == null",
                entity = mkEntity()
            ),
            TestDesc(
                truth = "tok_start_idx() == 123",
                entity = mkEntity(start = 123)
            ),
            TestDesc(
                truth = "tok_end_idx() == 123",
                entity = mkEntity(end = 123)
            ),
            TestDesc(truth = "tok_this() == tok_this()", idlCtx = mkIdlContext())
        )

    @Test
    def testTokenFirstLast(): Unit =
        val e = mkEntity(id = "a")

        // TODO:
        //tok.getMetadata.put("nlpcraft:nlp:index", 0)

        test(
            TestDesc(
                truth = "tok_is_first()",
                entity = e,
                idlCtx = mkIdlContext(entities = Seq(e))
            ),
            TestDesc(
                truth = "tok_is_last()",
                entity = e,
                idlCtx = mkIdlContext(entities = Seq(e))
            )
        )

    @Test
    def testTokenBeforeId(): Unit =
        val e1 = mkEntity(id = "1")
        val e2 = mkEntity(id = "2")

        // TODO:
//        e1.getMetadata.put("nlpcraft:nlp:index", 0)
//        e2.getMetadata.put("nlpcraft:nlp:index", 1)

        test(
            TestDesc(
                truth = "tok_is_before_id('2')",
                entity = e1,
                idlCtx = mkIdlContext(Seq(e1, e2))
            )
        )

    @Test
    def testTokenAfterId(): Unit =
        val e1 = mkEntity(id = "1")
        val e2 = mkEntity(id = "2")

        // TODO:
//        e1.getMetadata.put("nlpcraft:nlp:index", 0)
//        e2.getMetadata.put("nlpcraft:nlp:index", 1)

        test(
            TestDesc(
                truth = "tok_is_after_id('1')",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2))
            )
        )

    @Test
    def testTokenBetweenIds(): Unit =
        val e1 = mkEntity(id = "1", groups = Seq("grp1"))
        val e2 = mkEntity(id = "2", groups = Seq("grp2"))
        val e3 = mkEntity(id = "3", groups = Seq("grp3"))

        // TODO:
//        e1.getMetadata.put("nlpcraft:nlp:index", 0)
//        e2.getMetadata.put("nlpcraft:nlp:index", 1)
//        e3.getMetadata.put("nlpcraft:nlp:index", 2)

        test(
            TestDesc(
                truth = "tok_is_between_ids('1', '3')",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2, e3))
            ),
            TestDesc(
                truth = "tok_is_between_groups('grp1', 'grp3')",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2, e3))
            )
        )

    @Test
    def testTokenCount(): Unit =
        val e1 = mkEntity(id = "1")
        val e2 = mkEntity(id = "2")

        test(
            TestDesc(
                truth = "tok_count() == 2",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2))
            )
        )

    @Test
    def testTokenText(): Unit =
        val e = mkEntity(id = "1", txt="txt", normTxt = "normTxt")

        test(
            TestDesc(
                truth = "tok_txt() == 'txt'",
                entity = e
            ),
            TestDesc(
                truth = "tok_norm_txt() == 'normTxt'",
                entity = e
            )
        )

    @Test
    def testTokenForAll(): Unit =
        val e1 = mkEntity(id = "1", parentId = "x")
        val e2 = mkEntity(id = "2", groups = Seq("g", "z", "w"))
        val e3 = mkEntity(id = "2")

        test(
            TestDesc(
                truth = "size(tok_all_for_id('1')) == 1",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2, e3))
            ),
            TestDesc(
                truth = "size(tok_all_for_parent('x')) == 1",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2, e3))
            ),
            TestDesc(
                truth = "size(tok_all()) == 3",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2, e3))
            ),
            TestDesc(
                truth = "tok_count == size(tok_all())",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2, e3))
            ),
            TestDesc(
                truth =
                    "size(tok_all_for_group('g')) == 1 && #(first(tok_all_for_group('w'))) == '2' && is_empty(tok_all_for_group('unknown'))",
                entity = e2,
                idlCtx = mkIdlContext(Seq(e1, e2, e3))
            )
        )
