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

package org.apache.nlpcraft.model.intent.idl.compiler.functions

import org.junit.jupiter.api.Test

/**
  * Tests for 'tokens' functions.
  */
class NCIdlFunctionsToken extends NCIdlFunctions {
    private final val meta: Map[String, AnyRef] = Map(
        "nlpcraft:nlp:stopword" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:freeword" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:origtext" -> "orig text",
        "nlpcraft:nlp:index" -> java.lang.Integer.valueOf(11),
        "nlpcraft:nlp:normtext" -> "norm text",
        "nlpcraft:nlp:direct" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:english" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:swear" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:quoted" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:bracketed" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:dict" -> java.lang.Boolean.TRUE,
        "nlpcraft:nlp:lemma" -> "lemma",
        "nlpcraft:nlp:stem" -> "stem",
        "nlpcraft:nlp:sparsity" -> java.lang.Integer.valueOf(112),
        "nlpcraft:nlp:pos" -> "pos",
        "nlpcraft:nlp:unid" -> "21421"
    )

    private def mkMeta(truth: String):TestDesc = TestDesc(truth = truth, token = mkToken(meta = meta))

    @Test
    def testMainTokenProperties(): Unit =
        test(
            TestDesc(
                truth = "tok_id() == 'a'",
                token = mkToken(id = "a")
            ),
            mkMeta(truth = s"tok_lemma() == '${meta("nlpcraft:nlp:lemma")}'"),
            mkMeta(truth = s"tok_stem() == '${meta("nlpcraft:nlp:stem")}'"),
            mkMeta(truth = s"tok_pos() == '${meta("nlpcraft:nlp:pos")}'"),
            mkMeta(truth = s"tok_sparsity() == ${meta("nlpcraft:nlp:sparsity")}"),
            mkMeta(truth = s"tok_unid() == '${meta("nlpcraft:nlp:unid")}'"),
            TestDesc(
                truth = s"tok_is_abstract()",
                token = mkToken(`abstract` = true)
            ),
            mkMeta(truth = s"tok_is_abstract() == false"),
            mkMeta(truth = s"tok_is_bracketed() == ${meta("nlpcraft:nlp:bracketed")}"),
            mkMeta(truth = s"tok_is_direct() == ${meta("nlpcraft:nlp:direct")}"),
            mkMeta(truth = s"tok_is_permutated() != ${meta("nlpcraft:nlp:direct")}"),
            mkMeta(truth = s"tok_is_english() == ${meta("nlpcraft:nlp:english")}"),
            mkMeta(truth = s"tok_is_freeword() == ${meta("nlpcraft:nlp:freeword")}"),
            mkMeta(truth = s"tok_is_quoted() == ${meta("nlpcraft:nlp:quoted")}"),
            mkMeta(truth = s"tok_is_stopword() == ${meta("nlpcraft:nlp:stopword")}"),
            mkMeta(truth = s"tok_is_swear() == ${meta("nlpcraft:nlp:swear")}"),
            TestDesc(
                truth = s"tok_is_user()",
                token = mkToken(id = "aa")
            ),
            TestDesc(
                truth = s"!tok_is_user()",
                token = mkToken(id = "nlpcraft:nlp")
            ),
            mkMeta(truth = s"tok_is_wordnet() == ${meta("nlpcraft:nlp:dict")}"),
            TestDesc(
                truth = s"tok_ancestors() == list('1', '2')",
                token = mkToken(ancestors = Seq("1", "2"))
            ),
            TestDesc(
                truth = s"tok_parent() == 'parentId'",
                token = mkToken(parentId = "parentId")
            ),
            TestDesc(
                truth = "tok_groups() == list('1', '2')",
                token = mkToken(groups = Seq("1", "2"))
            ),
            TestDesc(
                truth = "tok_value() == 'value'",
                token = mkToken(value = "value")
            ),
            TestDesc(
                truth = "tok_value() == null",
                token = mkToken()
            ),
            TestDesc(
                truth = "tok_start_idx() == 123",
                token = mkToken(start = 123)
            ),
            TestDesc(
                truth = "tok_end_idx() == 123",
                token = mkToken(end = 123)
            ),
            TestDesc(truth = "tok_this() == tok_this()", idlCtx = mkIdlContext())
        )

    @Test
    def testTokenFirstLast(): Unit = {
        val tok = mkToken(id = "a")

        tok.getMetadata.put("nlpcraft:nlp:index", 0)

        test(
            TestDesc(
                truth = "tok_is_first()",
                token = tok,
                idlCtx = mkIdlContext(toks = Seq(tok))
            ),
            TestDesc(
                truth = "tok_is_last()",
                token = tok,
                idlCtx = mkIdlContext(toks = Seq(tok))
            )
        )
    }

    @Test
    def testTokenBeforeId(): Unit = {
        val tok1 = mkToken(id = "1")
        val tok2 = mkToken(id = "2")

        tok1.getMetadata.put("nlpcraft:nlp:index", 0)
        tok2.getMetadata.put("nlpcraft:nlp:index", 1)

        test(
            TestDesc(
                truth = "tok_is_before_id('2')",
                token = tok1,
                idlCtx = mkIdlContext(Seq(tok1, tok2))
            )
        )
    }

    @Test
    def testTokenAfterId(): Unit = {
        val tok1 = mkToken(id = "1")
        val tok2 = mkToken(id = "2")

        tok1.getMetadata.put("nlpcraft:nlp:index", 0)
        tok2.getMetadata.put("nlpcraft:nlp:index", 1)

        test(
            TestDesc(
                truth = "tok_is_after_id('1')",
                token = tok2,
                idlCtx = mkIdlContext(Seq(tok1, tok2))
            )
        )
    }
}
