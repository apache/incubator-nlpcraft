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

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime._
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.makro.antlr4._
import org.apache.nlpcraft.common.makro.antlr4.{NCMacroDslParser ⇒ P}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  *
  */
object NCMacroCompiler extends LazyLogging {
    
    /**
      *
      * @param buffer
      * @param inGroup
      */
    case class StackItem (
        buffer: mutable.Buffer[String],
        inGroup: Boolean
    )
    
    /**
      *
      * @param parser
      */
    class FiniteStateMachine(parser: P) extends NCMacroDslBaseListener {
        private val stack = new mutable.ArrayStack[StackItem]

        // Current min/max quantifier.
        private var min = 1
        private var max = 1
        
        private var expandedSyns: Set[String] = _
        
        def pushNew(inGroup: Boolean): Unit = stack.push(StackItem(new ListBuffer[String](), inGroup))
    
        /**
          *
          * @param errMsg
          * @param ctx
          * @return
          */
        def error(errMsg: String)(implicit ctx: ParserRuleContext): RecognitionException =
            new RecognitionException(errMsg, parser, parser.getInputStream, ctx)
        
        // Push new container at the beginning and at each new group.
        override def enterLine(ctx: P.LineContext): Unit = pushNew(false)
        override def enterGroup(ctx: P.GroupContext): Unit = pushNew(true)
        
        override def exitGroup(ctx: NCMacroDslParser.GroupContext): Unit = {
            val top = stack.pop()
            
            require(top.inGroup)
            
            val arg = stack.top
            
            arg.buffer.flatMap { s ⇒
                (
                    for (z ← top.buffer) yield
                        for (i ← min to max) yield
                            s + " " + (s"$z " * i)
                )
                .flatten.toSet
            }

            // Reset min max.
            min = 1
            max = 1
        }
    
        override def exitSyn(ctx: P.SynContext): Unit = {
            val syn = if (ctx.TXT() != null) ctx.TXT().getText else ctx.INT().getText
            val top = stack.top
            val buf = top.buffer
            
            if (top.inGroup)
                buf += syn
            else {
                if (buf.isEmpty) buf += syn else buf.map(_ + " " + syn)
            }
        }
    
        override def exitList(ctx: P.ListContext): Unit = if (ctx.UNDERSCORE() != null) stack.top.buffer += ""
        override def exitLine(ctx: P.LineContext): Unit = expandedSyns = stack.pop().buffer.map(_.trim).toSet
    
        override def exitMinMax(ctx: P.MinMaxContext): Unit = {
            implicit val evidence: ParserRuleContext = ctx
            
            val minStr = ctx.getChild(1).getText.trim
            val maxStr = ctx.getChild(3).getText.trim
    
            try
                min = java.lang.Integer.parseInt(minStr)
            catch {
                case _: NumberFormatException ⇒ throw error(s"Invalid min quantifier: $minStr")
            }
            try
                max = java.lang.Integer.parseInt(maxStr)
            catch {
                case _: NumberFormatException ⇒ throw error(s"Invalid max quantifier: $maxStr")
            }
            
            if (min < 0 || max < 0 || min > max || max == 0)
                throw error(s"Min/max quantifiers should satisfy 'max >= min, min >= 0, max > 0': [$min, $max]")
        }
    
        /**
         *
         * @return
         */
        def getExpandedMacro: Set[String] = {
            require(expandedSyns != null)
    
            expandedSyns
        }
    }

    /**
     * Custom error handler.
     */
    class CompilerErrorListener(in: String) extends BaseErrorListener {
        /**
         *
         * @param len
         * @param pos
         * @return
         */
        private def makeCharPosPointer(len: Int, pos: Int): String = {
            val s = (for (_ ← 1 to len) yield '-').mkString("")

            s.substring(0, pos - 1) + '^' + s.substring(pos)
        }

        /**
         *
         * @param recognizer
         * @param offendingSymbol
         * @param line
         * @param charPos
         * @param msg
         * @param e
         */
        override def syntaxError(
            recognizer: Recognizer[_, _],
            offendingSymbol: scala.Any,
            line: Int,
            charPos: Int,
            msg: String,
            e: RecognitionException): Unit = {

            val errMsg = s"Macro syntax error at line $line:$charPos - $msg\n" +
                s"  |-- ${c("Macro:")} $in\n" +
                s"  +-- ${c("Error:")}  ${makeCharPosPointer(in.length, charPos)}"

            throw new NCE(errMsg)
        }
    }

    /**
     *
     * @param in Macro to expand.
     * @return Expanded macro as a set of finite strings.
     */
    def compile(in: String): Set[String] = {
        // ANTLR4 armature.
        val lexer = new NCMacroDslLexer(CharStreams.fromString(in))
        val tokens = new CommonTokenStream(lexer)
        val parser = new NCMacroDslParser(tokens)

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(in))
        parser.addErrorListener(new CompilerErrorListener(in))

        // State automata.
        val fsm = new FiniteStateMachine(parser)

        // Parse the input DSL and walk built AST.
        (new ParseTreeWalker).walk(fsm, parser.line())

        // Return the expanded macro.
        fsm.getExpandedMacro
    }
}
