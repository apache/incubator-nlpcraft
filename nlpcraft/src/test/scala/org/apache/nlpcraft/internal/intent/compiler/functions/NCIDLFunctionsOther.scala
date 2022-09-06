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
import scala.sys.SystemProperties

/**
  * Tests for 'other' functions.
  */
class NCIDLFunctionsOther extends NCIDLFunctions:
    test("test 1") {
        // If.
        test(
            TestDesc(truth = "if(true, 1, 0) == 1"),
            TestDesc(truth = "if(false, 1, 0) == 0"),
            TestDesc(truth = "or_else(null, false) == false"),
            TestDesc(truth = "or_else('s', list(1, 2, 3)) == 's'"),
            TestDesc(truth = "or_else(meta_cfg('unknown_prop'), list(1, 2, 3)) == list(1, 2, 3)")
        )
    }

    test("test 2") {
        val sys = new SystemProperties

        sys.put("k1", "v1")

        try
            val js = "{\"k1\": \"v1\"}"

            // JSON.
            test(
                s"has(keys(json('$js')), 'k1') == true",
                s"has(keys(json('$js')), 'k2') == false"
            )
        finally
            sys.remove("k1")
    }

    test("test 3") {
        test(
            "to_string(list(1, 2, 3)) == list('1', '2', '3')",
            "to_string(3.123) == '3.123'"
        )
    }

    test("test 4") {
        test(
            "true",
            "true == true",
            "false == false",
            "false != true",
            "true != false",
            "null == null"
        )
    }
