/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.model.intent.idl.compiler.functions

import org.apache.nlpcraft.common.U
import org.apache.nlpcraft.model.{NCTokenPredicateContext, NCTokenPredicateResult}
import org.junit.jupiter.api.Test

class NCIdlTokensTestWrapper {
    def trueOn123(ctx: NCTokenPredicateContext): NCTokenPredicateResult =
        new NCTokenPredicateResult(ctx.getToken.getOriginalText == "123", 1)

    def wrongParams1(): NCTokenPredicateResult = new NCTokenPredicateResult(true, 1)
    def wrongParams2(s: String): NCTokenPredicateResult = new NCTokenPredicateResult(true, 1)
    def wrongParams3(s: String, ctx: NCTokenPredicateContext): NCTokenPredicateResult =
        new NCTokenPredicateResult(true, 1)
    def wrongRet1(ctx: NCTokenPredicateContext): String = ""
    def wrongRet2(ctx: NCTokenPredicateContext): Unit = {}
    def wrongExec1(ctx: NCTokenPredicateContext): NCTokenPredicateResult = null
    def wrongExec2(ctx: NCTokenPredicateContext): NCTokenPredicateResult = throw new NullPointerException
}

/**
  * Tests for 'custom' functions.
  */
class NCIdlFunctionsCustom extends NCIdlFunctions {
    private final val C = U.cleanClassName(classOf[NCIdlTokensTestWrapper], simpleName = false)

    @Test
    def testErrors(): Unit = {
        def test(truth: String*): Unit =
            for (t <- truth)
                expectError(TestDesc(truth = t, isCustom = true))

        test(
            "invalid",
            s"$C#missed",
            s"$C#wrongParams1",
            s"$C#wrongParams2",
            s"$C#wrongParams3",
            s"$C#wrongRet1",
            s"$C#wrongRet2",
            s"$C#wrongExec1",
            s"$C#wrongExec2"
        )
    }

    @Test
    def test(): Unit =
        test(
            TestDesc(
                truth = s"$C#trueOn123",
                isCustom = true,
                token = Some(tkn(txt = "123")),
                tokensUsed = Some(1)
            ),
            TestDesc(
                truth = s"$C#trueOn123",
                isCustom = true,
                token = Some(tkn(txt = "456")),
                expectedRes = false,
                tokensUsed = Some(1)
            ),
            TestDesc(
                // Method defined in model.
                truth = s"#trueAlwaysCustomToken",
                isCustom = true,
                token = Some(tkn(txt = "any"))
            )
        )
}
