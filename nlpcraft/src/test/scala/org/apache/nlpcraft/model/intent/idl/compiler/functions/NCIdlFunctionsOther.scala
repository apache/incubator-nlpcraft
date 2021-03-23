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

import scala.sys.SystemProperties

/**
  * Tests for 'other' functions.
  */
class NCIdlFunctionsOther extends NCIdlFunctions {
    @Test
    def test1(): Unit =
        // If.
        test(
            TestDesc(truth = "if(true, 1, 0) == 1"),
            TestDesc(truth = "if(false, 1, 0) == 0")
        )

    @Test
    def test2(): Unit = {
        val sys = new SystemProperties

        sys.put("k1", "v1")

        val js = "{\"k1\": \"v1\"}"

        // JSON.
        test(
            s"has(keys(json('$js')), 'k1') == true",
            s"has(keys(json('$js')), 'k2') == false"
        )
    }

    @Test
    def test3(): Unit = {
        test(
            s"to_string(list(1, 2, 3)) == list('1', '2', '3')",
            "to_string(3.123) == '3.123'"
        )
    }

    @Test
    def test4(): Unit = {
        test(
            s"to_double(25) == 25.0",
            s"to_double(25) != 25", // double != int without rounding.
            s"round(to_double(25)) == 25"
        )
    }
}
