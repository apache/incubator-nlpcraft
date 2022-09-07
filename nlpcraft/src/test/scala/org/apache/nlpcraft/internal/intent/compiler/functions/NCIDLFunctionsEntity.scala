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

import org.apache.nlpcraft.internal.intent.compiler.functions.NCIDLFunctions.*
import org.apache.nlpcraft.nlp.util.NCTestToken

import java.lang.{Boolean as JBool, Integer as JInt}
import scala.language.implicitConversions

/**
  * Tests for 'entities' functions.
  */
class NCIDLFunctionsEntity extends NCIDLFunctions:
    test("test") {
        val t1 = NCTestToken(txt = "W1", idx = 0)
        val t2 = NCTestToken(txt = "w2", idx = 1)

        def mkMeta(truth: String): TestDesc = TestDesc(truth = truth, entity = mkEntity(tokens = t1, t2))

        test(
            TestDesc(
                truth = "# == 'a'",
                entity = mkEntity(id = "a", tokens = t1, t2)
            ),
            TestDesc(
                truth = "ent_id == 'a'",
                entity = mkEntity(id = "a", tokens = t1, t2)
            ),
            TestDesc(
                truth = "ent_index == 100",
                entity = mkEntity(id = "a", tokens = NCTestToken(txt = "w2", idx = 100))
            ),
            TestDesc(
                truth = "ent_text == 'W1 w2'",
                entity = mkEntity(id = "a", tokens = t1, t2)
            ),

            TestDesc(
                truth = "ent_count == 1",
                entity = mkEntity(id = "a", tokens = t1, t2)
            ),
            TestDesc(
                truth = "ent_this() == ent_this()",
                idlCtx = mkIdlContext()
            ),
            TestDesc(
                truth = "ent_groups() == list('g1', 'g2')",
                entity = mkEntity(id = "a", groups = Set("g1", "g2"), tokens = t1, t2)
            ),
            TestDesc(
                truth = "ent_groups() == list('a')",
                entity = mkEntity(id = "a", tokens = t1, t2)
            )
        )
    }

    test("test tokens first and last") {
        val e1 = mkEntity(id = "a", tokens = NCTestToken(idx = 0))
        val e2 = mkEntity(id = "b", tokens = NCTestToken(idx = 1))

        val ctx = mkIdlContext(entities = Seq(e1, e2))

        test(
            TestDesc(
                truth = "ent_is_first()",
                entity = e1,
                idlCtx = ctx
            ),
            TestDesc(
                truth = "ent_is_last()",
                entity = e2,
                idlCtx = ctx
            )
        )
    }

    test("test before and after") {
        val e1 = mkEntity(id = "1", tokens = NCTestToken(idx = 0))
        val e2 = mkEntity(id = "2", tokens = NCTestToken(idx = 1))

        val ctx = mkIdlContext(Seq(e1, e2))

        test(
            TestDesc(
                truth = "ent_is_before_id('2')",
                entity = e1,
                idlCtx = ctx
            ),
            TestDesc(
                truth = "ent_is_before_group('2')",
                entity = e1,
                idlCtx = ctx
            ),
            TestDesc(
                truth = "ent_is_after_id('1')",
                entity = e2,
                idlCtx = ctx
            ),
            TestDesc(
                truth = "ent_is_after_group('1')",
                entity = e2,
                idlCtx = ctx
            )
        )
    }

    test("test between") {
        val e1 = mkEntity(id = "1", groups = Set("grp1"), tokens = NCTestToken(idx = 0))
        val e2 = mkEntity(id = "2", groups = Set("grp2"), tokens = NCTestToken(idx = 1))
        val e3 = mkEntity(id = "3", groups = Set("grp3"), tokens = NCTestToken(idx = 2))

        val ctx = mkIdlContext(Seq(e1, e2, e3))

        test(
            TestDesc(
                truth = "ent_is_between_ids('1', '3')",
                entity = e2,
                idlCtx = ctx
            ),
            TestDesc(
                truth = "ent_is_between_groups('grp1', 'grp3')",
                entity = e2,
                idlCtx = ctx
            )
        )
    }
    
    test("test all methods") {
        val e1 = mkEntity(id = "1", tokens = NCTestToken())
        val e2 = mkEntity(id = "2", groups = Set("g", "z", "w"), tokens = NCTestToken())
        val e3 = mkEntity(id = "2", tokens = NCTestToken())

        val ctx = mkIdlContext(Seq(e1, e2, e3))

        test(
            TestDesc(
                truth = "size(ent_all) == 3",
                idlCtx = ctx
            ),
            TestDesc(
                truth = "size(ent_all_for_id('2')) == 2",
                idlCtx = ctx
            ),
            TestDesc(
                truth =
                    "size(ent_all_for_group('g')) == 1 && #(first(ent_all_for_group('w'))) == '2' && is_empty(ent_all_for_group('unknown'))",
                idlCtx = ctx
            )
        )
    }