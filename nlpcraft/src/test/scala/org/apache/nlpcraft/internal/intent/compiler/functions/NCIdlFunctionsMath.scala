
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

import scala.language.implicitConversions

/**
  * Tests for 'math' functions.
  */
class NCIdlFunctionsMath extends NCIdlFunctions:
    @Test
    def testError(): Unit =
        expectError(
            // Invalid name.
            "xxx(1, 1)",
            "xxx()",
            // Invalid arguments count.
            "atan(1, 1) == 1",
            "pi(1)"
        )

    @Test
    def test(): Unit =
        test(
            "abs(2) == 2",
            "abs(-2.2) == 2.2",
            "ceil(1.8) == 2.0",
            "floor(1.1) == 1.0",
            "rint(1.8) == 2.0",
            "round(1.8) == 2",
            "to_double(25) == 25.0",
            "to_double(25) == 25",
            "to_double('25.25') == 25.25",
            "to_int(25.02) == 25",
            "to_int('101') == 101",
            "round(to_double(25)) == 25",
            "signum(-1.8) == -1.0",
            "sqrt(4) - 2 < 0.001",
            "cbrt(8) - 2 < 0.001",
            "acos(8.1) != acos(9.1)",
            "asin(8.1) != asin(9)",
            "atan(8) != atan(9.1)",
            "cos(1.5708) < 0.001",
            "cos(1.5708) < 0.001",
            "sin(1.5708) > 0.999",
            "sin(1.5708) > 0.999",
            "tan(8) != tan(9)",
            "cosh(8) != cosh(9)",
            "sinh(8) != sinh(9)",
            "tanh(8) != tanh(9)",
            "atan2(8, 2) != atan2(9, 2)",
            "atan2(8.1, 2.1) != atan2(9.1, 2.1)",
            "degrees(1.5708) - 90 < 0.001",
            "radians(90) - 1.5708 < 0.001",
            "exp(2) != exp(3)",
            "expm1(8) != expm1(9)",
            "hypot(2, 3) - 3.606  < 0.001",
            "log(8) != log(9)",
            "log10(8) != log10(9)",
            "log1p(8) != log1p(9)",
            "pow(2, 2) - 4 < 0.001",
            "pow(2.0, 2.0) - 4 < 0.001",
            "pow(2, 2.0) - 4 < 0.001",
            "pow(2.0, 2) - 4 < 0.001",
            "pow(2.0, 2) - 4 < 0.001",
            "rand() < 1",
            "pi() - 3.142 < 0.01",
            "euler() - 2.718 < 0.01",
            "to_double(2) - 2.0 < 0.01",
            "1.1 == 1.1",
            "1.0 == 1",
            "1 == 1.0"
        )
