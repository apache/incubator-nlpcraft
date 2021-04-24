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
  * Tests for 'collections' functions.
  */
class NCIdlFunctionsCollections extends NCIdlFunctions {
    private final val js = "{\"k1\": \"v1\"}"

    @Test
    def test(): Unit =
        test(
            "list(1, 2, 3) == list(1, 2, 3)",
            "list(1.0, 2, 3) == list(1.0, 2, 3)",
            "list(1, 2, 3) != list(1.0, 2, 3)",
            "get(list(1, 2, 3), 1) == 2",
            "has(list(1, 2, 3), 1) == true",
            "has(list(1.1, 2.1, 3.1), 1.1) == true",
            "has(list(1.0, 2.0, 3.0), 1.0) == true",
            "has(list(1.0, 2.0, 3.0), 1) == false", // Different types.
            "has(list('1', '2', '3'), '1') == true",
            "has(list(1, 2, 3), 5) == false",
            "has(list(1.1, 2.1, 3.1), 5.1) == false",
            "has(list('1', '2', '3'), '5') == false",
            "has_any(list('1', '2', '3'), list('1', '20', '30')) == true",
            "has_any(list('1', '2', '3'), list('10', '20', '30')) == false",
            "has_all(list('1', '2', '3'), list('1', '20', '30')) == false",
            "has_all(list('1', '2', '3'), list('1', '2', '3')) == true",
            "first(list(1, 2, 3)) == 1",
            "@lst = list(1, 2, 3) first(reverse(@lst)) == last(@lst)",
            "last(list(1, 2, 3)) == 3",
            "is_empty(list()) == true",
            "is_empty(list(1)) == false",
            "non_empty(list()) == false",
            "non_empty(list(1)) == true",
            "reverse(list(1.0, 2, 3)) == list(3, 2, 1.0)",
            "sort(list(2, 1, 3)) == list(1, 2, 3)",
            "sort(list('c', 'a', 'b')) == list('a', 'b', 'c')",
            "size(list(2.0, 1, 3)) == 3",
            "length(list(2.0, 1, 3)) == 3",
            "count(list(2.0, 1, 3)) == 3",
            "size(list()) == 0",
            "length(list()) == 0",
            "count(list()) == 0",
            s"keys(json('$js')) == list('k1')",
            s"values(json('$js')) == list('v1')",
            s"distinct(list(1, 2, 3, 3, 2)) == list(1, 2, 3)",
            s"distinct(list(1.0, 2.0, 3.0, 3.0, 2.0)) == list(1.0, 2.0, 3.0)",
            s"distinct(list('1', '2', '3', '3', '2')) == list('1', '2', '3')",
            s"concat(list(1, 2, 3), list(1, 2, 3)) == list(1, 2, 3, 1, 2, 3)",
            s"concat(list(1, 2, 3), list()) == list(1, 2, 3)",
            s"concat(list(), list()) == list()",
            s"concat(list(1, 2), list(3.0)) == list(1, 2, 3.0)"
        )
}
