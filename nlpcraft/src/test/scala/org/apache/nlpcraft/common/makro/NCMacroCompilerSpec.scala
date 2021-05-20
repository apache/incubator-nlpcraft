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

package org.apache.nlpcraft.common.makro

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.apache.nlpcraft.common._

/**
  * Unit tests for the macro compiler.
  */
class NCMacroCompilerSpec {
    /**
      *
      * @param txt
      * @param exp
      */
    private def checkEq(txt: String, exp: Seq[String]): Unit = {
        val res = NCMacroCompiler.compile(txt).toSeq.sorted
        val z = exp.sorted
        
        if (res != z)
            println(s"$res != $z")
        
        assertTrue(res == z)
    }

    /**
     *
     * @param txt
     */
    private def checkError(txt: String): Unit = {
        try {
            NCMacroCompiler.compile(txt)

            assert(false)
        } catch {
            case e: NCE =>
                println(e.getMessage)
                assert(true)
        }
    }
    
    @Test
    def testOkCompiler(): Unit = {
        checkEq("A", Seq("A"))
        checkEq("{_|A|_}", Seq("", "A"))
        checkEq("{_|A|_|_|_|{_|A}}", Seq("", "A"))
        checkEq("{A|_}", Seq("", "A"))
        checkEq("    A   ", Seq("A"))
        checkEq("A B", Seq("A B"))
        checkEq("""A {Москва|_|\|}""", Seq("A", "A Москва", """A \|"""))
        checkEq("A Moscó", Seq("A Moscó"))
        checkEq("""A B \[ \] \< \> \_ \{ \} \, \|""", Seq("""A B \[ \] \< \> \_ \{ \} \, \|"""))
        checkEq("A           B", Seq("A B"))
        checkEq("{A}", Seq("A"))
        checkEq("{{{A}}[1,1]}[1,1]", Seq("A"))
        checkEq("XX {A}", Seq("XX A"))
        checkEq("{A}[0,2]", Seq("", "A", "A A"))
        checkEq("{A  }   [0  ,2]", Seq("", "A", "A A"))
        checkEq("{A          }", Seq("A"))
        checkEq("      {      A          }", Seq("A"))
        checkEq("{A}{B}", Seq("A B"))
        checkEq("{A}[0,1]{  {    {B}}}", Seq("B", "A B"))
        checkEq("{A}[0,1]{  {    {xx B}}}", Seq("xx B", "A xx B"))
        checkEq(" {  A   }{B}", Seq("A B"))
        checkEq(" {  A   }      {B}", Seq("A B"))
        checkEq("A {B | C | _}", Seq("A", "A B", "A C"))
        checkEq("{A}[2,2]", Seq("A A"))
        checkEq("{A}[1,2]", Seq("A", "A A"))
        checkEq("{A|_|_|_|     _}[1,2]", Seq("", "A", "A A"))
        checkEq("{A}[1,2] {B}[2,2]", Seq("A B B", "A A B B"))
        checkEq("yy {xx A|_}[1,2] zz", Seq("yy zz", "yy xx A zz", "yy xx A xx A zz"))
        checkEq("yy {xx A|_}[0,2] zz", Seq("yy zz", "yy xx A zz", "yy xx A xx A zz"))
        checkEq("A {B| xxx {C|E}} D", Seq("A B D", "A xxx C D", "A xxx E D"))
        checkEq("x {<OF>| y <NO>}[1,2]", Seq("x <OF>", "x y <NO>", "x <OF> <OF>", "x y <NO> y <NO>"))
        checkEq("{{x  }} {<OF>| y <NO>}[1,2]", Seq("x <OF>", "x y <NO>", "x <OF> <OF>", "x y <NO> y <NO>"))
        checkEq("{tl;dr|j/k}", Seq("tl;dr", "j/k"))
        checkEq("a {b|_}. c", Seq( "a b . c", "a . c"))
        checkEq("""a {/abc.*/|\{\_\}} c""", Seq("a /abc.*/ c", """a \{\_\} c"""))
        checkEq("""{`a`   |\`a\`   }""", Seq("`a`", """\`a\`"""))
        checkEq("""a {/abc.\{\}*/     |/d/} c""", Seq("""a /abc.\{\}*/ c""", "a /d/ c"))
        checkEq("""a .{b\,  |_}. c""", Seq("""a . b\, . c""", "a . . c"))
        checkEq("a {        {b|c}|_}.", Seq("a .", "a b .", "a c ."))
        checkEq("°", Seq("°"))

    }

    @Test
    def testFailCompiler(): Unit = {
        checkError("{A")
        checkError("{A}[2,1]")
        checkError("{A}[1,0]")
        checkError("{A}[2,]")
        checkError("{A}[x,1]")
        checkError("{A}[2.33, 1]")
        checkError("{A}[1:1]")
        checkError("{A}[1 1]")
        checkError("A|__}")
        checkError("{A|__} }}")
        checkError("} {A|_}")
        checkError("{A|_} _")
        checkError("{A|")
    }
}
