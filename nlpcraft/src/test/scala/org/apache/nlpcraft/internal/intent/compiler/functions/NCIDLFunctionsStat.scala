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
  * Tests for 'stat' functions.
  */
class NCIDLFunctionsStat extends NCIDLFunctions:
    test("test") {
        test(
            "max(list(1, 2, 3)) == 3",
            "max(list(1.0, 2.0, 3.0)) == 3.0",
            "min(list(1, 2, 3)) == 1",
            "min(list(1.0, 2.0, 3.0)) == 1.0",
            "avg(list(1.0, 2.0, 3.0)) == 2.0",
            "avg(list(1, 2, 3)) == 2.0",
            "avg(list(1.2, 2.2, 3.2)) == 2.2",
            "avg(list(1, 2.2, 3.1)) == 2.1",
            "stdev(list(1, 2.2, 3.1)) > 0",
            "stdev(list(1, 2, 3)) > 0",
            "stdev(list(0.0, 0.0, 0.0)) == 0.0",
            "stdev(list(0, 0, 0)) == 0.0"
        )
    }

    test("test errors") {
        expectError(
            "avg(list()) == 2",
            "avg(list('A')) == 2",
            "stdev(list()) == 2",
            "stdev(list('A')) == 2"
        )
    }    