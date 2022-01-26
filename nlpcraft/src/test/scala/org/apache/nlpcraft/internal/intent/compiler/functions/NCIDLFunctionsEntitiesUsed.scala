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
import org.junit.jupiter.api.Test

/**
  * Tests for 'entities used' result.
  */
class NCIDLFunctionsEntitiesUsed extends NCIDLFunctions:
    @Test
    def test(): Unit =
        val e1 = mkEntity(id = "a", tokens = NCTestToken())
        val e2 = mkEntity(id = "b", tokens = NCTestToken())

        test(
            TestDesc(
                truth = "1 == 1",
                idlCtx = mkIdlContext(),
                entitiesUsed = Option(0)
            ),
            TestDesc(
                truth = "# == 'a'",
                entity = Option(e1),
                idlCtx = mkIdlContext(Seq(e1)),
                entitiesUsed = Option(1)
            ),
            TestDesc(
                truth = "# == 'a' && # == 'a'",
                entity = Option(e1),
                idlCtx = mkIdlContext(Seq(e1)),
                entitiesUsed = Option(2)
            ),
            TestDesc(
                truth = "ent_text == '*' || # == 'a'",
                entity = Option(e1),
                idlCtx = mkIdlContext(Seq(e1, e2)),
                entitiesUsed = Option(2)
            ),
            TestDesc(
                truth = "# == 'a' && # == 'a' && ent_text != '*'",
                entity = Option(e1),
                idlCtx = mkIdlContext(Seq(e1)),
                entitiesUsed = Option(3)
            )
        )
