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
import org.apache.nlpcraft.common.util.NCUtils._

import scala.collection.JavaConverters._
import scala.collection._

object NCMacroParser {
    private final val CHARS = "[A-Za-z0-9-_]+"
    private final val MACRO_REGEX = s"<$CHARS>".r
    private final val BROKEN_MACRO_REGEX1 = s"<$CHARS".r
    private final val BROKEN_MACRO_REGEX2 = s"$CHARS>".r
    
    /**
      * Constructor.
      *
      * @param macros Set of macros to add.
      */
    def apply(macros: List[(String, String)]): NCMacroParser = {
        apply(macros: _*)
    }
    
    /**
      * Constructor.
      *
      * @param macros Set of macros to add.
      */
    def apply(macros: Map[String, String]): NCMacroParser = {
        apply(macros.toSeq: _*)
    }
    
    /**
      * Constructor.
      *
      * @param macros Set of macros to add.
      */
    def apply(macros: (String, String)*): NCMacroParser = {
        val obj = new NCMacroParser
        
        macros.foreach(m ⇒ obj.addMacro(m._1, m._2))
        
        obj
    }
}

/**
  * Provides generic support for text expansion using macros and options groups.
  *
  * Syntax:
  * - all macros should start with '<' and end with '>'.
  * - '{A|B}' denotes either 'A' or 'B'.
  * - '{A|B|*}' denotes either 'A', or 'B' or nothing.
  * - '\' can be used only for escaping '{', '}', '|', and '*' special symbols.
  *
  * Examples:
  *      "A {B|C} D" ⇒ "A B D", "A C D"
  *      "A \{B\|C\} D" ⇒ "A {B|C} D"
  *      "A {B|*} D" ⇒ "A D", "A B D"
  *      "A {*|B|C} D" ⇒ "A D", "A B D", "A C D"
  *      "A <MACRO>" ⇒ "A ..." based on <MACRO> content.
  *      "A {<MACRO>|*}" ⇒ "A", "A ..." based on <MACRO> content.
  *
  * NOTE: Macros cannot be recursive.
  * NOTE: Macros and '{...}' options groups can be nested.
  */
class NCMacroParser {
    import NCMacroParser._
    
    // Maximum number of expanded strings.
    private final val MAX_LIMIT = 500000
    
    // Macros.
    private val macros = new java.util.concurrent.ConcurrentHashMap[String, String]().asScala
    
    /**
      * A token matched in the input string for LR parser.
      *
      * @param head Token text.
      * @param tail Remaining part of the input string, if any.
      */
    case class Token(head: String, tail: String)
    
    /**
      * Splits '{...}' option group into sequence of token. Note that
      * '|' separator will be excluded from tokens.
      *
      * @param txt Option group text to parse.
      */
    @throws[NCE]
    private[makro] def parseGroup(txt: String): Seq[Token] = {
        require(txt != null)
        
        var s = txt
        val last = s.length() - 1
        
        if (s.head != '{' || s.charAt(last) != '}')
            throw new NCE(s"Invalid option group in: $txt")
        
        s = s.substring(1, last) // Trim out opening '{' and closing '}'.
        
        /**
          *
          * @param s Text to parse to get next group item.
          */
        def nextGroupItem(s: String): Option[Token] = {
            if (s.isEmpty)
                None
            else {
                var i = 0
                val len = s.length()
                var found = false
                var isEscape = false
                var depth = 0
                
                while (i < len && !found) {
                    val ch = s.charAt(i)
    
                    if (ch == '\\' && !isEscape)
                        isEscape = true
                    else {
                        if (!isEscape)
                            ch match {
                                case '}' ⇒ depth -= 1
                                case '{' ⇒ depth += 1
                                case '|' ⇒ if (depth == 0) found = true
                                case _ ⇒
                            }
        
                        isEscape = false
                    }
    
                    i += 1
                }
                
                if (depth != 0)
                    throw new NCE(s"Uneven curly brackets in: $txt")
                if (isEscape)
                    throw new NCE(s"Incomplete '\\' escape usage in: $txt")
                
                if (!found)
                    Some(Token(s.substring(0), ""))
                else
                    Some(Token(s.substring(0, i - 1), s.substring(i)))
            }
        }
        
        var toks = Seq.empty[Token]
        
        var item = nextGroupItem(s)
        
        while (item.isDefined) {
            toks :+= item.get
            
            item = nextGroupItem(item.get.tail)
        }
        
        toks
    }
    
    /**
      * Gets the next lexical token.
      *
      * Special symbols are: ' ', '{', '}', '*' and '|'. Use `\` for escaping.
      *
      * @param s Input string to get the next lexical token from.
      */
    @throws[NCE]
    private[makro] def nextToken(s: String): Option[Token] = {
        require(s != null)
        
        def procMarker(fix: String): Option[Token] = {
            if (s.startsWith(fix))
                s.substring(fix.length).indexOf(fix) match {
                    case -1 ⇒ throw new NCE(s"Uneven '$fix' marker in: $s")
                    case i ⇒
                        val tail = i + 2 * fix.length
    
                        Some(Token(s.substring(0, tail), s.substring(tail)))
                }
            else
                None
        }
        
        if (s.isEmpty)
            None
        else {
            // Check prefixes first.
            val tok = procMarker(DSL_FIX) match {
                case t: Some[Token] ⇒ t
                case None ⇒ procMarker(REGEX_FIX) match {
                    case t: Some[Token] ⇒ t
                    case None ⇒ None
                }
            }
            
            if (tok.isDefined)
                tok
            else {
                val len = s.length
                var found = false
                var isEscape = false
                
                if (s.startsWith(DSL_FIX)) {
                    val i = s.substring(DSL_FIX.length).indexOf(DSL_FIX)
                    
                    if (i == -1)
                        throw new NCE(s"Uneven '$DSL_FIX' marker in: $s")
                    
                    val tail = i + 2 * DSL_FIX.length
        
                    Some(Token(s.substring(0, tail), s.substring(tail)))
                }
                else if (s.charAt(0) == '{') { // Option group.
                    var depth = 0
                    var i = 1
                    
                    while (i < len && !found) {
                        val ch = s.charAt(i)
        
                        if (ch == '\\' && !isEscape)
                            isEscape = true
                        else {
                            if (!isEscape)
                                ch match {
                                    case '}' ⇒ if (depth == 0) found = true else depth -= 1
                                    case '{' ⇒ depth += 1
                                    case _ ⇒
                                }
            
                            isEscape = false
                        }
        
                        i += 1
                    }
                    
                    if (depth != 0 || !found)
                        throw new NCE(s"Uneven curly brackets in: $s")
                    if (isEscape)
                        throw new NCE(s"Incomplete '\\' escape usage in: $s")
                    
                    Some(Token(s.substring(0, i), s.substring(i)))
                }
                else { // Not an option group.
                    var i = 0
                    
                    while (i < len && !found) {
                        val ch = s.charAt(i)
                        
                        if (ch == '\\' && !isEscape)
                            isEscape = true
                        else {
                            if (!isEscape)
                                ch match {
                                    case '|' | '*' | '}' ⇒ throw new NCE(s"Suspicious '$ch' at pos $i in: $s")
                                    case '{' ⇒ found = true // Found start of the option group.
                                    case _ ⇒
                                }
                            
                            isEscape = false
                        }
        
                        i += 1
                    }
                    
                    if (isEscape)
                        throw new NCE(s"Incomplete '\\' escape usage in: $s")
    
                    if (!found)
                        Some(Token(s.substring(0), ""))
                    else
                        Some(Token(s.substring(0, i - 1), s.substring(i - 1)))
                }
            }
        }
    }

    // Trims all duplicate spaces.
    private def trimDupSpaces(s: String) =
        s.split(" ").map(_.trim).filter(_.nonEmpty).mkString(" ")
    
    // Processes '\' escapes for '{', '}', '|', and '*'.
    private def processEscapes(s: String): String = {
        val len = s.length()
        val buf = new StringBuilder()
        var i = 0
        var isEscape = false
        
        while (i < len) {
            val ch = s.charAt(i)
            
            if (ch == '\\' && !isEscape)
                isEscape = true
            else {
                if (isEscape && ch != '|' && ch != '}' && ch != '{' && ch != '*')
                    buf += '\\'
                
                buf += ch
    
                isEscape = false
            }
            
            i += 1
        }
        
        buf.toString
    }
    
    /**
      * LR-parser.
      *
      * @param s Text to expand.
      */
    @throws[NCE]
    private def expand0(s: String): Seq[String] = {
        require(s != null)
        
        if (s.isEmpty)
            Seq.empty
        else if (s.head == '/' && s.last == '/') // Don't macro-process regex.
            Seq(s)
        else {
    
            /**
              * Mixes (multiplies) given string with tails.
              *
              * @param s     String to mix in.
              * @param tails Sequence of tail strings (potentially empty).
              */
            def mixTails(s: String, tails: Seq[String]): Seq[String] =
                if (tails.isEmpty) Seq(s)
                else tails.map(t ⇒ s + t)
    
            val res = nextToken(s) match {
                case None ⇒ Seq.empty
                case Some(tok) ⇒
                    val tails = expand0(tok.tail)
                    if (tok.head.head == '{') // Option group.
                        parseGroup(tok.head).flatMap(x ⇒
                            if (x.head == "*")
                                mixTails("", tails)
                            else
                                expand0(x.head).flatMap(z ⇒ mixTails(z, tails))
                        )
                    else // Plain text.
                        mixTails(tok.head, tails)
            }
            if (res.lengthCompare(MAX_LIMIT) > 0)
                throw new NCE(s"Maximum expansion length reached: $MAX_LIMIT")
            res
        }
    }
    
    /**
      * Expand given string.
      *
      * @param txt Text to expand.
      */
    @throws[NCE]
    def expand(txt: String): Seq[String] = {
        require(txt != null)
        
        var s = txt
        
        // Grab 1st macro match, if any.
        var m = MACRO_REGEX.findFirstMatchIn(s)
        
        // Expand macros (supporting nesting).
        while (m.isDefined) {
            val ms = m.get.toString()
            
            if (!macros.keySet.contains(ms))
                throw new NCE(s"Unknown macro [match=$ms, txt=$txt]")
            
            // Expand all registered macros.
            for ((k, v) ← macros) s = s.replace(k, v)
            
            // Grab another macro match, if any.
            m = MACRO_REGEX.findFirstMatchIn(s)
        }
        
        // Check for potentially invalid macros syntax.
        if (BROKEN_MACRO_REGEX1.findFirstIn(s).isDefined || BROKEN_MACRO_REGEX2.findFirstIn(s).isDefined)
            throw new NCE(s"Likely invalid macro in: $txt")
        
        U.distinct(expand0(s).toList map trimDupSpaces map processEscapes)
    }
    
    /**
      * Checks macro name.
      *
      * @param name Macro name.
      */
    private def checkName(name: String): Unit = {
        if (name.head != '<')
            throw new NCE(s"Missing macro '<' opening in: $name")
        if (name.reverse.head != '>')
            throw new NCE(s"Missing macro '>' closing in: $name")
    }
    
    /**
      * Adds or overrides given macro.
      *
      * @param name Macro name (typically an upper case string).
      *     It must start with '<' and end with '>'.
      * @param str Value of the macro (any arbitrary string).
      */
    @throws[NCE]
    def addMacro(name: String, str: String): Unit = {
        require(name != null)
        require(str != null)
        
        checkName(name)
    
        // Check for recursion.
        if (str.contains(name))
           throw new NCE(s"Recursion is not supported, macro: $name")
    
        macros += name → str
    }
    
    /**
      * Removes macro.
      *
      * @param name Macro name (typically an upper case string).
      *      It must start with '<' and end with '>'.
      */
    @throws[NCE]
    def removeMacro(name: String): Unit = {
        require(name != null)
        
        macros -= name
    }
    
    /**
      * Checks whether or not macro with given name exists or not.
      *
      * @param name Name.
      */
    def hasMacro(name: String): Boolean = {
        macros.contains(name)
    }
}
