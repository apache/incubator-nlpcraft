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
  * Tests for 'stat' functions.
  */
class NCIdlFunctionsStat extends NCIdlFunctions {
    @Test
    def testError(): Unit = {
        expectError("avg(list()) == 2")
        expectError("avg(list('A')) == 2")
        expectError("stdev(list()) == 2")
        expectError("stdev(list('A')) == 2")
    }

    @Test
    def test(): Unit =
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
            "stdev(list(0, 0, 0)) == 0.0",
        )
}
