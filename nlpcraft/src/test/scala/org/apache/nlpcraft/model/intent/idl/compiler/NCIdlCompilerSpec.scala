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

package org.apache.nlpcraft.model.intent.idl.compiler

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCModel
import org.apache.nlpcraft.model.intent.compiler.{NCIdlCompiler, NCIdlCompilerGlobal}
import org.junit.jupiter.api.Test

/**
 * Tests for IDL compiler.
 */
class NCIdlCompilerSpec {
    private final val MODEL_ID = "test.mdl.id"

    private final val MODEL: NCModel = new NCModel {
        override val getId: String = MODEL_ID
        override val getName: String = MODEL_ID
        override val getVersion: String = "1.0.0"
        override def getOrigin: String = "test"
    }
    
    /**
     *
     * @param idl
     */
    private def checkCompileOk(idl: String): Unit =
        try {
            NCIdlCompiler.compileIntents(idl, MODEL, MODEL_ID)

            assert(true)
        }
        catch {
            case e: Exception => assert(assertion = false, e)
        }

    /**
     *
     * @param txt
     */
    private def checkCompileError(txt: String): Unit =
        try {
            NCIdlCompiler.compileIntents(txt, MODEL, MODEL_ID)

            assert(false)
        } catch {
            case e: NCE =>
                println(e.getMessage)
                assert(true)
        }

    @Test
    @throws[NCException]
    def testInlineCompileOk(): Unit = {
        NCIdlCompilerGlobal.clearCache(MODEL_ID)
    
        checkCompileOk(
            """
              |import('org/apache/nlpcraft/model/intent/idl/compiler/test_ok.idl')
              |""".stripMargin
        )
        checkCompileOk(
            """
              |intent=i1
              |     flow="a[^0-9]b"
              |     meta={'a': true, 'b': {'Москва': [1, 2, 3]}}
              |     term(t1)={2 == 2 && size(tok_id()) != -25}
              |""".stripMargin
        )
        checkCompileOk(
            """
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), list("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |""".stripMargin
        )
        checkCompileOk(
            """
              |// Some comments.
              |fragment=f1
              |     term(ft1)={2==2} /* Term block comment. */
              |     term~/class#method/
              |/*
              | * +=====================+
              | * | block comments......|
              | * +=====================+
              | */
              |intent=i1
              |     flow="a[^0-9]b" // Flow comment.
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), list("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |     fragment(f1, {'a': true, 'b': ["s1", "s2"]}) /* Another fragment. */
              |""".stripMargin
        )
        checkCompileOk(
            """
              |fragment=f21
              |     term(f21_t1)={2==2}
              |     term~/class#method/
              |
              |fragment=f22
              |     term(f22_t1)={2==2_000_000.23}
              |     fragment(f21)
              |     term~/class#method/
              |
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), list("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |     fragment(f21, {'a': true, 'b': ["s1", "s2"]})
              |""".stripMargin
        )
    }

    @Test
    @throws[NCException]
    def testInlineCompileFail(): Unit = {
        NCIdlCompilerGlobal.clearCache(MODEL_ID)
        
        checkCompileError(
            """
              |intent=i1
              |/*
              | * +=====================+
              | * | block comments......|
              | * +=====================+
              | */
              |     flow="a[^0-9]b"
              |     meta={{'a': true, 'b': {'arr': [1, 2, 3]}}
              |     term(t1)={2 == 2 && size(tok_id()) != -25}
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     meta={'a': true, 'b': {'arr': [1, 2, 3]}}
              |     term(t1)={
              |         @x == 2 && size(tok_id()) != -25
              |     }
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     flow="a[^0-9b"
              |     term(t1)={true}
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     flow="a[^0-9b]"
              |     term(t1)={
              |         @x = 2
              |         @x = 2
              |
              |         true
              |     }
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     flow="a[^0-9b]"
              |     term(t1)={
              |         true
              |
              |         @x = 2
              |     }
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     term(t1)={true}[2,1]
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     flow="a[^0-9b]"
              |     term(t1)={true}
              |     term(t1)={true}
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     flow="a[^0-9b]"
              |     term(t1)={true}
              |intent=i1
              |     flow="a[^0-9b]"
              |     term(t1)={true}
              |""".stripMargin
        )
        checkCompileError(
            """
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), list("k1\"", 'v1\'v1', "k2", "v2"))}[1:2]
              |""".stripMargin
        )
        checkCompileError(
            """
              |fragment=f1
              |     term(t1)={2==2}
              |     term~/class#method/
              |
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), list("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |     fragment(f1, {'a': true, 'b': ["s1", "s2"]})
              |""".stripMargin
        )
        checkCompileError(
            """
              |fragment=f111
              |     term(t1)={2==2}
              |     term~/class#method/
              |
              |intent=i1
              |     flow="a[^0-9]b"
              |     term(t1)={has(json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"), list("موسكو\"", 'v1\'v1', "k2", "v2"))}
              |     fragment(f1_, {'a': true, 'b': ["s1", "s2"]})
              |""".stripMargin
        )
    }
}
