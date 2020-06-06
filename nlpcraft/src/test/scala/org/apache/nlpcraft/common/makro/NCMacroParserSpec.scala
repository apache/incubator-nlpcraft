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
    parser.addMacro("<NUM>", "{overall|total|grand total|entire|complete|full|*} <QTY>")
    parser.addMacro("<WEBSITE>", "{html|*} {site|website|web site|web-site|web property}")
    parser.addMacro("<BY>", "{segmented|grouped|combined|arranged|organized|categorized|*} {for|by|over|along|over by}")
    parser.addMacro("<RATE>", "{rate|percentage|speed|momentum|frequency}")
    parser.addMacro("<AVG>", "{avg|average} <QTY>")
    parser.addMacro("<ID>", "{unique|*} {id|guid|identifier|identification} {number|*}")
    parser.addMacro("<USER>", "{{<WEBSITE>}|web|*} {user|visitor}")
    parser.addMacro("<SES>", "{{<WEBSITE>}|web|*} {session|visit}")
    parser.addMacro("<DCM>", "{double click|double-click|doubleclick|dcm} {manager|*}")
    parser.addMacro("<PAGE>", "{{<WEBSITE>}|*} {web page|web-page|webpage|page} {path|*}")
    parser.addMacro("<CTR>", "{ctr|click-through-rate|{click through|click-through} <RATE>}")
    parser.addMacro("<URL>", "{{uniform|*} resource {locator|identifier}|{{<PAGE>}|*} {link|*} {uri|url|address}}")
    parser.addMacro("<METRICS_A>", "{analytics|statistics|measurements|analysis|report|efficiency|performance}")
    parser.addMacro("<METRICS_B>", "{metrics|data|info|information|facts}")
    parser.addMacro("<METRICS>","{<METRICS_A>|<METRICS_B>|<METRICS_A> <METRICS_B>|<METRICS_B> <METRICS_A>}")
    
    private val ignoreNCE = ignoring(classOf[NCE])
    
    /**
      *
      * @param txt Text to find next token in.
      * @param tokHead Expected head value of the token.
      * @param tokTail Expected tail value of the token.
      */
    def testToken(txt: String, tokHead: String, tokTail: String): Unit = {
        val tok = parser.nextToken(txt)
        
        assertTrue(tok.get.head == tokHead)
        assertTrue(tok.get.tail == tokTail)
    }
    
    /**
      *
      * @param txt Text to expand.
      * @param exp Expected expansion strings.
      */
    def testParser(txt: String, exp: Seq[String]): Unit =
        assertTrue(parser.expand(txt).sorted == exp.sorted)
    
    /**
      *
      * @param txt Group text.
      * @param grps Sequence of group's elements.
      */
    def testGroup(txt: String, grps: Seq[String]): Unit = {
        val elms = parser.parseGroup(txt)
        
        assertTrue(grps == elms.map(_.head))
    }

    @Test
    def testPerformance() {
        val start = currentTime

        val N = 50000

        for (_ ← 0 to N)
            parser.expand("a {{{<C>}}|{*}} {c|d|e|f|g|h|j|k|l|n|m|p|r}")

        val duration = currentTime - start

        println(s"${N * 1000 / duration} ops/second.")
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

        testParser("<A> {b|*} c", Seq(
            "aaa b c",
            "aaa c"
        ))

        testParser("<B> {b|*} c", Seq(
            "aaa bbb b c",
            "aaa bbb c"
        ))

        testParser("{tl;dr|j/k}", Seq(
            "tl;dr",
            "j/k"
        ))

        testParser("a {b|*}. c", Seq(
            "a b. c",
            "a . c"
        ))

        testParser("""a {/abc.\*/|\{\*\}} c""", Seq(
            "a /abc.*/ c",
            "a {*} c"
        ))
    
        testParser("""{`a`|\`a\`}""", Seq(
            "`a`",
            "\\`a\\`"
        ))

        testParser("""a {/abc.\{\}\*/|/d/} c""", Seq(
            "a /abc.{}*/ c",
            "a /d/ c"
        ))

        testParser("a .{b,  |*}. c", Seq(
            "a .b, . c",
            "a .. c"
        ))

        testParser("a {{b|c}|*}.", Seq(
            "a .",
            "a b.",
            "a c."
        ))

        testParser("a {{{<C>}}|{*}} c", Seq(
            "a aaa bbb z c",
            "a aaa bbb w c",
            "a c"
        ))

        testParser("a {b|*}", Seq(
            "a b",
            "a"
        ))

        testParser("a {b|*}d", Seq(
            "a bd",
            "a d"
        ))

        testParser("a {b|*} d", Seq(
            "a b d",
            "a d"
        ))

        testParser("a {b|*}       d", Seq(
            "a b d",
            "a d"
        ))

        testParser("{{{a}}} {b||*|{{*}}||*}", Seq(
            "a b",
            "a"
        ))

        testParser("a {b}", Seq(
            "a b"
        ))

        testParser("a {b} {c|*}", Seq(
            "a b",
            "a b c"
        ))

        testParser("a {{b|c}}", Seq(
            "a b",
            "a c"
        ))

        ignoreNCE { testParser("a | b", Seq.empty); assertTrue(false) }
        ignoreNCE { testParser("a *", Seq.empty); assertTrue(false) }
        ignoreNCE { testParser("a}}", Seq.empty); assertTrue(false) }
        ignoreNCE { testParser("a {a|b} *", Seq.empty); assertTrue(false) }
    }

    @Test
    def testOptionGroup() {
        testGroup("{a {b|c} | d}", Seq("a {b|c} ", " d"))
        testGroup("{a|b}", Seq("a", "b"))
        testGroup("{a}", Seq("a"))
        testGroup("{{{a}}}", Seq("{{a}}"))
        testGroup("{{{a}}|{b}}", Seq("{{a}}", "{b}"))
        testGroup("{a {c}|b|*}", Seq("a {c}", "b", "*"))
        testGroup("""{/abc.\*/|\{\*\}}""", Seq("/abc.\\*/", "\\{\\*\\}"))

        ignoreNCE { parser.parseGroup("a"); assertTrue(false) }
        ignoreNCE { parser.parseGroup("{a"); assertTrue(false) }
        ignoreNCE { parser.parseGroup("a}"); assertTrue(false) }
    }

    @Test
    def testParseTokens() {
        testToken("""a \* b""", """a \* b""", "")
        testToken("""a \\\* b""", """a \\\* b""", "")
        testToken("""a \{\*\*\*\} b""", """a \{\*\*\*\} b""", "")
        testToken("""a{b\|\*\}|c}""", "a", """{b\|\*\}|c}""")
        testToken("""/\|\*\{\}/ a {bc|d}""", """/\|\*\{\}/ a """, """{bc|d}""")
        testToken("{a} b", "{a}", " b")
        testToken("{a|{c|d}}", "{a|{c|d}}", "")
        testToken("{a {c|d} xxx {f|g}} b", "{a {c|d} xxx {f|g}}", " b")
        testToken("c{a}     b", "c", "{a}     b")
        testToken("{{{a}}}", "{{{a}}}", "")
        
        ignoreNCE { parser.nextToken("a } b"); assertTrue(false) }
        ignoreNCE { parser.nextToken("{c b"); assertTrue(false) }
        ignoreNCE { parser.nextToken("a | b"); assertTrue(false) }
        ignoreNCE { parser.nextToken("a |*"); assertTrue(false) }
        
        assertTrue(parser.nextToken("").isEmpty)
        assertTrue(parser.nextToken("     ").isDefined)
    }

    @Test
    def testLimit() {
        ignoreNCE {
            parser.expand("<METRICS> <USER> <BY> <WEBSITE> <BY> <SES> <BY> <METRICS> <BY> <USER> <BY> <METRICS>")

            assertTrue(false)
        }
    }
}
