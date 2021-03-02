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

package org.apache.nlpcraft.model.intent.dsl

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.intent.impl.ver2.NCIntentDslCompiler
import org.junit.jupiter.api.Test

/**
 * Tests for DSL compiler.
 */
class NCIntentDslCompilerSpec {
    /**
     *
     * @param dsl
     */
    private def checkOk(dsl: String): Unit =
        try {
            NCIntentDslCompiler.compile(dsl, "mdl.id")

            assert(true)
        }
        catch {
            case e: Exception ⇒ assert(false, e)
        }

    /**
     *
     * @param txt
     */
    private def checkError(txt: String): Unit =
        try {
            NCIntentDslCompiler.compile(txt, "mdl.id")

            assert(false)
        } catch {
            case e: NCE ⇒
                println(e.getMessage)
                assert(true)
        }

    @Test
    @throws[NCException]
    def testOk(): Unit = {
        checkOk(
            """
              |intent=i1
              |     flow="a[^0-9]b"
              |     meta={'a': true, 'b': {'Москва': [1, 2, 3]}}
              |     term(t1)={2 == 2 && size(id()) != -25}
              |""".stripMargin
        )
        checkOk(
            """
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), map("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |""".stripMargin
        )
        checkOk(
            """
              |fragment=f1
              |     term(ft1)={2==2}
              |     term~/class#method/
              |
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), map("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |     fragment(f1, {'a': true, 'b': ["s1", "s2"]})
              |""".stripMargin
        )
        checkOk(
            """
              |fragment=f21
              |     term(f21_t1)={2==2}
              |     term~/class#method/
              |
              |fragment=f22
              |     term(f22_t1)={2==2}
              |     fragment(f21)
              |     term~/class#method/
              |
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), map("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |     fragment(f21, {'a': true, 'b': ["s1", "s2"]})
              |""".stripMargin
        )
    }

    @Test
    @throws[NCException]
    def testFail(): Unit = {
        checkError(
            """
              |intent=i1
              |     flow="a[^0-9]b"
              |     meta={{'a': true, 'b': {'arr': [1, 2, 3]}}
              |     term(t1)={2 == 2 && size(id()) != -25}
              |""".stripMargin
        )
        checkError(
            """
              |intent=i1
              |     flow="a[^0-9b"
              |     term(t1)={true}
              |""".stripMargin
        )
        checkError(
            """
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), map("k1\"", 'v1\'v1', "k2", "v2"))}[1:2]
              |""".stripMargin
        )
    }
}
