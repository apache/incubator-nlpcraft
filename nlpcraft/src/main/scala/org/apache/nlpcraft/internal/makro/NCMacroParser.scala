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

package org.apache.nlpcraft.internal.makro

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils


object NCMacroParser:
    private final val ESC_CHARS = """{}\<>_[]|,"""
    private final val MACRO_REGEX = s"<[A-Za-z0-9-_]+>".r
    private final val BROKEN_MACRO_REGEX1 = s"<[A-Za-z0-9-_]+".r
    private final val BROKEN_MACRO_REGEX2 = s"[A-Za-z0-9-_]+>".r

    /**
      * Constructor.
      *
      * @param macros Set of macros to add.
      */
    def apply(macros: List[(String, String)]): NCMacroParser =
        apply(macros: _*)

    /**
      * Constructor.
      *
      * @param macros Set of macros to add.
      */
    def apply(macros: Map[String, String]): NCMacroParser =
        apply(macros.toSeq: _*)

    /**
      * Constructor.
      *
      * @param macros Set of macros to add.
      */
    def apply(macros: (String, String)*): NCMacroParser =
        macros.foldLeft(new NCMacroParser)((parser, tup) => parser.addMacro(tup._1, tup._2))

/**
  * Provides generic support for text expansion using macros and options groups.
  *
  * Syntax:
  * - all macros should start with '<' and end with '>'.
  * - '{A|{B}}' denotes either 'A' or 'B'.
  * - '{A|B|_}' denotes either 'A', or 'B' or nothing ('_').
  * - '{A}[1,2]' denotes 'A' or 'A A'.
  * - '{A}[0,1]' denotes 'A' or nothing (just like '{A|_}').
  * - '\' should be used for escaping any of '{}\_[]|,' special symbols.
  * - Excessive pairs'{' and '}' are ignored
  *
  * Examples:
  *      "A {B|C}[1,2] D" => "A B D", "A C D", "A B B D", "A C C D"
  *      "A \{B\|C\} D" => "A {B|C} D"
  *      "A {B|_} D" => "A D", "A B D"
  *      "A {_|B|C} {D}[1,2]" => "A D", "A B D", "A C D", "A D D", "A B D D", "A C D D"
  *      "A <MACRO>" => "A ..." based on <MACRO> content.
  *      "A {<MACRO>|_}" => "A", "A ..." based on <MACRO> content.
  *
  * NOTE: Macros cannot be recursive.
  * NOTE: Macros and '{...}' options groups can be nested.
  */
class NCMacroParser:
    import NCMacroParser.*

    private val macros = scala.collection.mutable.HashMap.empty[String, String]

    /**
      * Trims all duplicate spaces.
      *
      * @param s
      * @return
      */
    private def trimDupSpaces(s: String) = NCUtils.splitTrimFilter(s, " ").mkString(" ")

    /**
      *
      * @param s
      * @return
      */
    private def processEscapes(s: String): String =
        val len = s.length()
        var i = 0
        var isEscape = false

        val buf = new StringBuilder()
        while (i < len)
            val ch = s.charAt(i)
            if ch == '\\' && !isEscape then
                isEscape = true
            else
                if isEscape && !ESC_CHARS.contains(ch) then buf += '\\'
                buf += ch
                isEscape = false
            i += 1
        buf.toString

    /**
      * Expand given string.
      *
      * @param txt Text to expand.
      */
    def expand(txt: String): Seq[String] =
        require(txt != null)

        var s = txt

        // Grab 1st macro match, if any.
        var m = MACRO_REGEX.findFirstMatchIn(s)

        // Expand macros including nested ones.
        while (m.isDefined)
            val ms = m.get.toString()
            if !macros.keySet.contains(ms) then E(s"Unknown macro [macro=$ms, txt=$txt]")
            // Expand all registered macros.
            for ((k, v) <- macros) s = s.replace(k, v)
            // Grab another macro match, if any.
            m = MACRO_REGEX.findFirstMatchIn(s)

        // Check for potentially invalid macros syntax.
        if BROKEN_MACRO_REGEX1.findFirstIn(s).isDefined || BROKEN_MACRO_REGEX2.findFirstIn(s).isDefined then
            E(s"Suspicious or invalid macro in: $txt")

        NCUtils.distinct(NCMacroCompiler.compile(s).toList map trimDupSpaces map processEscapes)


    /**
      * Checks macro name.
      *
      * @param name Macro name.
      */
    private def checkName(name: String): Unit =
        if name.head != '<' then E(s"Missing macro '<' opening: $name")
        if name.last != '>' then E(s"Missing macro '>' closing: $name")

    /**
      * Adds or overrides given macro.
      *
      * @param name Macro name (typically an upper case string).
      *     It must start with '&lt;' and end with '&gt;'.
      * @param str Value of the macro (any arbitrary string).
      */
    def addMacro(name: String, str: String): NCMacroParser =
        require(name != null)
        require(str != null)

        checkName(name)
        // Check for recursion.
        if str.contains(name) then E(s"Recursion is not supported, macro: $name")
        macros += name -> str
        this

    /**
      * Removes macro.
      *
      * @param name Macro name (typically an upper case string).
      *      It must start with '<' and end with '>'.
      */
    def removeMacro(name: String): NCMacroParser =
        require(name != null)
        macros -= name
        this

    /**
      * Checks whether or not macro with given name exists or not.
      *
      * @param name Name.
      */
    def hasMacro(name: String): Boolean = macros.contains(name)

