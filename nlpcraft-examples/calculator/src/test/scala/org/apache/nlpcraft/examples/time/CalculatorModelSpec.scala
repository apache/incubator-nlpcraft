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

package org.apache.nlpcraft.examples.time

import org.apache.nlpcraft.*
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

/**
  * Model validation.
  */
class CalculatorModelSpec extends AnyFunSuite:
    test("test") {
        Using.resource(new NCModelClient(new CalculatorModel())) { client =>
            def check(txt: String, v: Int): Unit = require(v == client.ask(txt, "userId").getBody)

            check("2 + 2", 4)
            check("3 * 4", 12)
            check("/ two", 6)
            check("+ twenty two", 28)
            check("7 + 2", 9)
        }
    }
