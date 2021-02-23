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

import org.apache.nlpcraft.common._
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import scala.compat.Platform._
import scala.util.control.Exception._

/**
  * Tests for text parser.
  */
class NCMacroParserSpec  {
    private val parser = NCMacroParser(
        "<A>" → "aaa",
        "<B>" → "<A> bbb",
        "<C>" → "<A> bbb {z|w}"
    )
    
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
    
    private val ignoreNCE = ignoring(classOf[NCE])
    
    /**
      *
      * @param txt Text to expand.
      * @param exp Expected expansion strings.
      */
    def testParser(txt: String, exp: Seq[String]): Unit = {
        val z = parser.expand(txt).sorted
        val w = exp.sorted
        
        if (z != w)
            println(s"$z != $w")
            
        assertTrue(z == w)
    }
    
    def testPerformance() {
        val start = currentTime

        val N = 50000

        for (_ ← 0 to N)
            parser.expand("a {{{<C>}}} {c|d|e|f|g|h|j|k|l|n|m|p|r}")

        val duration = currentTime - start

        println(s"${N * 1000 / duration} expansions/sec.")
    }

    @Test
    def testExpand() {
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

        testParser("<A> {b|_} c", Seq(
            "aaa b c",
            "aaa c"
        ))

        testParser("<B> {b|_} c", Seq(
            "aaa bbb b c",
            "aaa bbb c"
        ))

        testParser("{tl;dr|j/k}", Seq(
            "tl;dr",
            "j/k"
        ))

        testParser("a {b|_}. c", Seq(
            "a b . c",
            "a . c"
        ))

        testParser("""a {/abc.*/|\{\_\}} c""", Seq(
            "a /abc.*/ c",
            "a {_} c"
        ))
    
        testParser("""{`a`|\`a\`}""", Seq(
            "`a`",
            """\`a\`"""
        ))

        testParser("""a {/abc.\{\}*/|/d/} c""", Seq(
            "a /abc.{}*/ c",
            "a /d/ c"
        ))

        testParser("a .{b,  |_}. c", Seq(
            "a .b, . c",
            "a .. c"
        ))

        testParser("a {{b|c}|_}.", Seq(
            "a .",
            "a b.",
            "a c."
        ))

        testParser("a {{{<C>}}|{_}} c", Seq(
            "a aaa bbb z c",
            "a aaa bbb w c",
            "a c"
        ))

        testParser("a {b|_}", Seq(
            "a b",
            "a"
        ))

        testParser("a {b|_}d", Seq(
            "a bd",
            "a d"
        ))

        testParser("a {b|_} d", Seq(
            "a b d",
            "a d"
        ))

        testParser("a {b|_}       d", Seq(
            "a b d",
            "a d"
        ))

        testParser("{{{a}}} {b||_|{{_}}||_}", Seq(
            "a b",
            "a"
        ))

        testParser("a {b}", Seq(
            "a b"
        ))

        testParser("a {b} {c|_}", Seq(
            "a b",
            "a b c"
        ))

        testParser("a {{b|c}}", Seq(
            "a b",
            "a c"
        ))

        ignoreNCE { testParser("a | b", Seq.empty); assertTrue(false) }
        ignoreNCE { testParser("a _", Seq.empty); assertTrue(false) }
        ignoreNCE { testParser("a}}", Seq.empty); assertTrue(false) }
        ignoreNCE { testParser("a {a|b} _", Seq.empty); assertTrue(false) }
    }

    @Test
    def testLimit() {
        ignoreNCE {
            parser.expand("<METRICS> <USER> <BY> <WEBSITE> <BY> <SES> <BY> <METRICS> <BY> <USER> <BY> <METRICS>")

            assertTrue(false)
        }
    }
}
