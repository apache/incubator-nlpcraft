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

package org.apache.nlpcraft.common.makro

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCMacroProcessor
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import scala.jdk.CollectionConverters.CollectionHasAsScala

/**
  * Tests for text parser.
  */
class NCMacroParserSpec  {
    private val parser = new NCMacroProcessor()

    parser.addMacro("<A>", "aaa")
    parser.addMacro("<B>", "<A> bbb")
    parser.addMacro("<C>", "<A> bbb {z|w}")

    
    // Add macros for testing...
    parser.addMacro("<OF>", "{of|for|per}")
    parser.addMacro("<QTY>", "{number|tally|count|quantity|amount}")
    parser.addMacro("<NUM>", "{overall|total|grand total|entire|complete|full|_} <QTY>")
    parser.addMacro("<WEBSITE>", "{html|_} {site|website|web site|web-site|web property}")
    parser.addMacro("<BY>", "{segmented|grouped|combined|arranged|organized|categorized|_} {for|by|over|along|over by}")
    parser.addMacro("<RATE>", "{rate|percentage|speed|momentum|frequency}")
    parser.addMacro("<AVG>", "{avg|average} <QTY>")
    parser.addMacro("<ID>", "{unique|_} {id|guid|identifier|identification} {number|_}")
    parser.addMacro("<USER>", "{{<WEBSITE>}|web|_} {user|visitor}")
    parser.addMacro("<SES>", "{{<WEBSITE>}|web|_} {session|visit}")
    parser.addMacro("<DCM>", "{double click|double-click|doubleclick|dcm} {manager|_}")
    parser.addMacro("<PAGE>", "{{<WEBSITE>}|_} {web page|web-page|webpage|page} {path|_}")
    parser.addMacro("<CTR>", "{ctr|click-through-rate|{click through|click-through} <RATE>}")
    parser.addMacro("<URL>", "{{uniform|_} resource {locator|identifier}|{{<PAGE>}|_} {link|_} {uri|url|address}}")
    parser.addMacro("<METRICS_A>", "{analytics|statistics|measurements|analysis|report|efficiency|performance}")
    parser.addMacro("<METRICS_B>", "{metrics|data|info|information|facts}")
    parser.addMacro("<METRICS>","{<METRICS_A>|<METRICS_B>|<METRICS_A> <METRICS_B>|<METRICS_B> <METRICS_A>}")

    /**
      *
      * @param txt Text to expand.
      * @param exp Expected expansion strings.
      */
    def checkEq(txt: String, exp: Seq[String]): Unit = {
        val z = parser.expand(txt).asScala.toSeq.sorted
        val w = exp.sorted

        if (z != w)
            println(s"$z != $w")

        assertTrue(z == w)
    }

    // @Test
    def testPerformance(): Unit = {
        val start = U.now()

        val N = 50000

        for (_ <- 0 to N)
            parser.expand("a {{{<C>}}} {c|d|e|f|g|h|j|k|l|n|m|p|r}")

        val duration = U.now() - start

        println(s"${N * 1000 / duration} expansions/sec.")
    }


    /**
     *
     * @param txt
     */
    private def checkError(txt: String): Unit = {
        try {
            parser.expand(txt)

            assert(false)
        } catch {
            case e: NCE =>
                println("Expected error below:")
                println("^^^^^^^^^^^^^^^^^^^^^")
                println(e.getMessage)
                assert(true)
        }
    }

    @Test
    def testExpand(): Unit = {
        // Make sure we can parse these.
        parser.expand("<OF>")
        parser.expand("<QTY>")
        parser.expand("<NUM>")
        parser.expand("<WEBSITE>")
        parser.expand("<BY>")
        parser.expand("<RATE>")
        parser.expand("<AVG>")
        parser.expand("<ID>")
        parser.expand("<USER>")
        parser.expand("<SES>")
        parser.expand("<DCM>")
        parser.expand("<PAGE>")
        parser.expand("<CTR>")
        parser.expand("<URL>")
        parser.expand("<METRICS_A>")
        parser.expand("<METRICS_B>")
        parser.expand("<METRICS>")

        checkEq("<A> {b|_} c", Seq("aaa b c", "aaa c"))
        checkEq("<B> {b|_} c", Seq("aaa bbb b c", "aaa bbb c"))
        checkEq("{tl;dr|j/k}", Seq("tl;dr", "j/k"))
        checkEq("a {b|_}. c", Seq("a b . c", "a . c"))
        checkEq("""a {/abc.*/|\{\_\}} c""", Seq("a /abc.*/ c", "a {_} c"))
        checkEq("""{`a`|\`a\`}""", Seq("`a`", """\`a\`"""))
        checkEq("""a {/abc.\{\}*/|/d/} c""", Seq("a /abc.{}*/ c", "a /d/ c"))
        checkEq("""a .{b\,  |_}. c""", Seq("a . b, . c", "a . . c"))
        checkEq("a {{b|c}|_}.", Seq("a .", "a b .", "a c ."))
        checkEq("a {{{<C>}}|_} c", Seq("a aaa bbb z c", "a aaa bbb w c", "a c"))
        checkEq("a {b|_}", Seq("a b", "a"))
        checkEq("a {b|_}d", Seq("a b d", "a d"))
        checkEq("a {b|_} d", Seq("a b d", "a d"))
        checkEq("a {b|_}       d", Seq("a b d", "a d"))
        checkEq("a {b}", Seq("a b"))
        checkEq("a {b} {c|_}", Seq("a b", "a b c"))
        checkEq("a {{b|c}}", Seq("a b", "a c"))
        checkEq("a {b|_|{g\\}}[1,2]}", Seq("a", "a b", "a g}", "a g} g}"))
        checkEq("a {b|_|{//[]{}//}[1,2]}", Seq("a", "a b", "a //[]{}//", "a //[]{}// //[]{}//"))
        checkEq("a {b|_|{//[]^^// ^^{_}^^}[1,2]}", Seq("a", "a b", "a //[]^^// ^^{_}^^", "a //[]^^// ^^{_}^^ //[]^^// ^^{_}^^"))
        checkEq("//[a-zA-Z0-9]+//", Seq("//[a-zA-Z0-9]+//"))
        checkEq("the ^^[internal]{# == 'anyWord'}^^", Seq("the ^^[internal]{# == 'anyWord'}^^"))
        checkEq("{A}[0,1] ^^[internal]{# == 'anyWord'}^^", Seq("^^[internal]{# == 'anyWord'}^^", "A ^^[internal]{# == 'anyWord'}^^"))
        checkEq("w1 ^^{# == 'nlpcraft:num'}^^ w2", Seq("w1 ^^{# == 'nlpcraft:num'}^^ w2"))
        checkEq("before limit ^^[limitAlias]{# == 'nlpcraft:limit'}^^", Seq("before limit ^^[limitAlias]{# == 'nlpcraft:limit'}^^"))
        checkEq("wrap ^^[wrapLimitAlias]{# == 'wrapLimit'}^^", Seq("wrap ^^[wrapLimitAlias]{# == 'wrapLimit'}^^"))

        checkError("a {| b")
        checkError("{a}}")
    }

    @Test
    def testLimit(): Unit = {
        checkError("<METRICS> <USER> <BY> <WEBSITE> <BY> <SES> <BY> <METRICS> <BY> <USER> <BY> <METRICS>")
    }
}
