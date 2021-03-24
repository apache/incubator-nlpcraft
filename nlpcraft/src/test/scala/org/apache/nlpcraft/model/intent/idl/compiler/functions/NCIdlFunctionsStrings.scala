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

import org.junit.jupiter.api.Test

/**
  * Tests for 'strings' functions.
  */
class NCIdlFunctionsStrings extends NCIdlFunctions {
    @Test
    def test(): Unit =
        test(
            "trim(' a b  ') == 'a b'",
            "strip(' a b  ') == 'a b'",
            "uppercase('aB') == 'AB'",
            "lowercase('aB') == 'ab'",
            "is_alpha('aB') == true",
            "is_alpha('aB1') == false",
            "is_alphanum('aB1') == true",
            "is_whitespace(' ') == true",
            "is_whitespace('A') == false",
            "is_num('a') == false",
            "is_num('1') == true",
            "is_numspace('A') == false",
            "is_alphaspace('A') == true",
            "is_alphanumspace('A') == true",
            "is_alphanumspace(' ') == true",
            "is_alphanumspace('1 A') == true",
            "is_alphanumspace(' ') == true",
            "is_alphanumspace('  ') == true",
            "split('1 A') == list(1, 2)"
        )

    @Test
    def testError(): Unit = {
        // TODO: add correct test for `split` and `split_trim`
        testExpectedError("split('1 A') == true")
        testExpectedError("split_trim('1 A') == true")
    }
}
