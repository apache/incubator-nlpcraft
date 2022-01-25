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

/**
  * Tests for 'tokens used' result.
  */
class NCIdlFunctionsTokensUsed extends NCIdlFunctions:
    @Test
    def test(): Unit =
        val entityIdA = mkEntity(id = "a")
        val entityAb = mkEntity(id = "a", parentId = "b")
        test(
            TestDesc(
                truth = "1 == 1",
                idlCtx = mkIdlContext(),
                tokensUsed = Option(0)
            ),
            TestDesc(
                truth = "# == 'a'",
                entity = Option(entityIdA),
                idlCtx = mkIdlContext(Seq(entityIdA)),
                tokensUsed = Option(1)
            ),
            TestDesc(
                truth = "# == 'a' && # == 'a'",
                entity = Option(entityIdA),
                idlCtx = mkIdlContext(Seq(entityIdA)),
                tokensUsed = Option(2)
            ),
            TestDesc(
                truth = "# == 'a' && tok_parent == 'b'",
                entity = Option(entityAb),
                idlCtx = mkIdlContext(Seq(entityAb)),
                tokensUsed = Option(2)
            ),
            TestDesc(
                truth = "# == 'a' && # == 'a' && tok_parent == 'b'",
                entity = Option(entityAb),
                idlCtx = mkIdlContext(Seq(entityAb)),
                tokensUsed = Option(3)
            )
        )
