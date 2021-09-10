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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model.anyword

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

/**
  *
  */
class NCNestedAnySpec extends NCTestContext {
    private def test(): Unit = {
        // 1, 2 and 3 any words should be suitable.
        checkIntent("a t1 t2 t3 b", "compose")
        checkIntent("a t1 t2 b", "compose")
        checkIntent("a t1 b", "compose")

        // Too many 'any words'.
        checkFail("a t1 t2 t3 t4 b")

        // Missed 'any words'.
        checkFail("a b")
    }

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyRegex1], startClient = true)
    def testRegex1(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyRegex2], startClient = true)
    def testRegex2(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyRegex3], startClient = true)
    def testRegex3(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyRegex5], startClient = true)
    def testRegex4(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyRegex5], startClient = true)
    def testRegex5(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyRegex6], startClient = true)
    def testRegex6(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyRegex7], startClient = true)
    def testRegex7(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyAlphaNum1], startClient = true)
    def testAlphaNum1(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyAlphaNum2], startClient = true)
    def testAlphaNum2(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyAlphaNum3], startClient = true)
    def testAlphaNum3(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyAlphaNum4], startClient = true)
    def testAlphaNum4(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyAlphaNum5], startClient = true)
    def testAlphaNum5(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyAlphaNum6], startClient = true)
    def testAlphaNum6(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyAlphaNum7], startClient = true)
    def testAlphaNum7(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyNotSpace1], startClient = true)
    def testNotSpaceNum1(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyNotSpace2], startClient = true)
    def testNotSpaceNum2(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyNotSpace3], startClient = true)
    def testNotSpaceNum3(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyNotSpace4], startClient = true)
    def testNotSpaceNum4(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyNotSpace5], startClient = true)
    def testNotSpaceNum5(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyNotSpace6], startClient = true)
    def testNotSpaceNum6(): Unit = test()

    @Test @NCTestEnvironment(model = classOf[NCNestedTestModelAnyNotSpace7], startClient = true)
    def testNotSpaceNum7(): Unit = test()
}
