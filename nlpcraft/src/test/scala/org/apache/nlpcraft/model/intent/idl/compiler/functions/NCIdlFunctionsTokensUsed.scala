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
  * Tests for 'tokens used' result.
  */
class NCIdlFunctionsTokensUsed extends NCIdlFunctions {
    @Test
    def test(): Unit = {
        val tokIdA = mkToken(id = "a")
        val tokAb = mkToken(id = "a", parentId = "b")
        test(
            TestDesc(
                truth = "1 == 1",
                idlCtx = mkIdlContext(),
                tokensUsed = Some(0)
            ),
            TestDesc(
                truth = "# == 'a'",
                token = Some(tokIdA),
                idlCtx = mkIdlContext(Seq(tokIdA)),
                tokensUsed = Some(1)
            ),
            TestDesc(
                truth = "# == 'a' && # == 'a'",
                token = Some(tokIdA),
                idlCtx = mkIdlContext(Seq(tokIdA)),
                tokensUsed = Some(2)
            ),
            TestDesc(
                truth = "# == 'a' && tok_parent == 'b'",
                token = Some(tokAb),
                idlCtx = mkIdlContext(Seq(tokAb)),
                tokensUsed = Some(2)
            ),
            TestDesc(
                truth = "# == 'a' && # == 'a' && tok_parent == 'b'",
                token = Some(tokAb),
                idlCtx = mkIdlContext(Seq(tokAb)),
                tokensUsed = Some(3)
            )
        )
    }
}
