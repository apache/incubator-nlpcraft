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

import scala.language.implicitConversions

/**
  * Tests for 'strings' functions.
  */
class NCIDLFunctionsStrings extends NCIDLFunctions:
    test("test") {
        test(
            "trim(' a b  ') == 'a b'",
            "strip(' a b  ') == 'a b'",
            "uppercase('aB') == 'AB'",
            "lowercase('aB') == 'ab'",
            "is_alpha('aB') == true",
            "is_alpha('aB1') == false",
            "is_alphanum('aB1') == true",
            "is_whitespace('A') == false",
            "is_num('a') == false",
            "is_num('1') == true",
            "is_numspace('A') == false",
            "is_alphaspace('A') == true",
            "is_alphanumspace('A') == true",
            "is_alphanumspace('1 A') == true",
            "starts_with('ab', 'a') == true",
            "starts_with('ab', 'b') == false",
            "ends_with('ab', 'a') == false",
            "ends_with('ab', 'b') == true",
            "contains('ab', 'a') == true",
            "contains('ab', 'bc') == false",
            "index_of('ab', 'b') == 1",
            "index_of('ab', 'bc') == -1",
            "substr('abc', 0, 1) == 'a'",
            "regex('textabc', '^text.*$') == true",
            "regex('_textabc', '^text.*$') == false",
            "substr('abc', 0, 2) == 'ab'",
            "replace('abc', 'a', 'X') == 'Xbc'",
            "replace('abc', '0',  '0') == 'abc'",
            "split('1 A', ' ') == list('1', 'A')",
            "split_trim('1 A    ', ' ') == list('1', 'A')",
            "is_empty('a') == false",
            "non_empty('a') == true",
            "non_empty('a ') == true",
            "is_empty('') == true",
            "length(' ') == 1",
            "length('') == 0",
            "to_double('1.1') == 1.1",
            "to_double('1') == 1",
            "to_double('1') == 1.0",
            "to_int('1') == 1",
            "to_int('1') == 1.0",

            // Whitespaces.
            "replace('abc', 'ab',  '') == 'c'",
            "substr('abc', 1, 3) == 'bc'",
            "is_alphanumspace(' ') == true",
            "is_alphanumspace('  ') == true",
            "is_alphanumspace(' ') == true",
            "is_whitespace(' ') == true",
            "trim('   ') == ''"
        )
    }

    test("test errors") {
        expectError(
            "substr('abc', 10, 30) == 'bc'",
            "split('1 A') == true",
            "split_trim('1 A') == true",
            "to_double('1, 1') == true",
            "to_double('A') == true"
        )
    }