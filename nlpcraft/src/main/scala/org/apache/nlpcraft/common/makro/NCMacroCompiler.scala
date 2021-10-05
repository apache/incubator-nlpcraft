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

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.*
import org.apache.nlpcraft.common.*
import org.apache.nlpcraft.common.ansi.NCAnsi.*
import org.apache.nlpcraft.common.antlr4.*
import org.apache.nlpcraft.common.makro.NCMacroCompiler.FiniteStateMachine
import org.apache.nlpcraft.common.makro.antlr4.*
import org.apache.nlpcraft.common.util.NCUtils

import scala.collection.mutable

/**
  *
  */
object NCMacroCompiler extends LazyLogging:
    private final val MAX_SYN = 10000
    private final val MAX_QTY = 100

    /**
      *
      * @param buffer
      * @param isGroup
      */
    case class StackItem (
        var buffer: mutable.Buffer[String],
        isGroup: Boolean
    )

    /**
      *
      * @param parser
      * @param in
      */
    class FiniteStateMachine(parser: NCMacroDslParser, in: String) extends NCMacroDslBaseListener:
        private val stack = new mutable.Stack[StackItem]
        private var expandedSyns: Set[String] = _
        private var min = 1
        private var max = 1

        /**
          *
          * @param optS
          * @param s
          * @return
          */
        private def concat(optS: String, s: String): String = if (optS.isEmpty) s else optS + " " + s

        /**
          *
          * @param errMsg
          * @param ctx
          * @return
          */
        private def compilerError(errMsg: String)(implicit ctx: ParserRuleContext): NCException =
            val tok = ctx.stop
            new NCException(mkCompilerError(errMsg, tok.getLine, tok.getCharPositionInLine, in))

        /**
          *
          * @param buf
          * @param ctx
          */
        private def checkMaxSyn(buf: mutable.Buffer[String])(implicit ctx: ParserRuleContext): Unit =
            if buf.size > MAX_SYN then
                throw compilerError(s"Exceeded max number ($MAX_SYN) of macro expansions: ${buf.size}")

        override def enterExpr(ctx: NCMacroDslParser.ExprContext): Unit =
            val buf = mutable.Buffer.empty[String]
            // NOTE: do not allow expression's buffer to be empty.
            // Add harmless empty string.
            buf += ""
            stack.push(StackItem(buf, isGroup = false))

        override def enterGroup(ctx: NCMacroDslParser.GroupContext): Unit =
            // NOTE: group cannot be empty based on the BNF grammar.
            stack.push(StackItem(mutable.Buffer.empty[String], isGroup = true))

        override def exitExpr(ctx: NCMacroDslParser.ExprContext): Unit =
            given ParserRuleContext = ctx

            if stack.size > 1 then
                val expr = stack.pop()
                val prn = stack.top
                checkMaxSyn(expr.buffer)
                require(expr.buffer.nonEmpty)
                if prn.isGroup then
                    prn.buffer ++= expr.buffer
                else
                    prn.buffer = for (z <- expr.buffer; i <- prn.buffer.indices) yield concat(prn.buffer(i), z)

        override def exitMinMax(ctx: NCMacroDslParser.MinMaxContext): Unit =
            given ParserRuleContext = ctx

            if ctx.minMaxShortcut() != null then
                ctx.minMaxShortcut().getText match
                    case "?" => min = 0; max = 1
                    case c => throw compilerError(s"Invalid min/max shortcut '$c' in: ${ctx.getText}")
            else if ctx.MINMAX() != null then
                var s = ctx.MINMAX().getText
                val orig = s

                s = s.substring(1, s.length - 1)
                val comma = s.indexOf(',')
                if comma == -1 || comma == 0 || comma == s.length - 1 then
                    throw compilerError(s"Invalid min/max quantifier: $orig")

                try
                    min = java.lang.Integer.parseInt(s.substring(0, comma).trim)
                catch
                    case _: NumberFormatException => throw compilerError(s"Invalid min quantifier: $orig")

                try
                    max = java.lang.Integer.parseInt(s.substring(comma + 1).trim)
                catch
                    case _: NumberFormatException => throw compilerError(s"Invalid max quantifier: $orig")

            if min < 0 || max < 0 || min > max || max == 0 || max > MAX_QTY then
                throw compilerError(s"[$min,$max] quantifiers should be 'max >= min, min >= 0, max > 0, max <= $MAX_QTY'.")

        override def exitGroup(ctx: NCMacroDslParser.GroupContext): Unit =
            given ParserRuleContext = ctx
            val grp = stack.pop()
            // Remove dups.
            grp.buffer = grp.buffer.distinct
            checkMaxSyn(grp.buffer)
            require(grp.isGroup)
            val prn = stack.top
            prn.buffer = prn.buffer.flatMap {
                s => (for (z <- grp.buffer; i <- min to max) yield concat(s, s"$z " * i).trim).toSet
            }
            // Reset.
            min = 1
            max = 1

        override def exitSyn(ctx: NCMacroDslParser.SynContext): Unit =
            val syn = (
                if ctx.TXT() != null then ctx.TXT()
                else if ctx.REGEX_TXT() != null then ctx.REGEX_TXT()
                else ctx.IDL_TXT()
            ).getText

            val buf = stack.top.buffer
            require(buf.nonEmpty)
            for (i <- buf.indices) buf.update(i, concat(buf(i), syn))

        override def exitList(ctx: NCMacroDslParser.ListContext): Unit = if ctx.UNDERSCORE() != null then stack.top.buffer += ""
        override def exitMakro(ctx: NCMacroDslParser.MakroContext): Unit = expandedSyns = stack.pop().buffer.map(_.trim).toSet

        /**
          *
          * @return
          */
        def getExpandedMacro: Set[String] =
            require(expandedSyns != null)
            expandedSyns

    /**
      * Custom error handler.
      */
    class CompilerErrorListener(in: String) extends BaseErrorListener:
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
            line: Int, // 1, 2, ...
            charPos: Int, // 1, 2, ...
            msg: String,
            e: RecognitionException): Unit = throw new NCException(mkCompilerError(msg, line, charPos - 1, in))

    /**
      *
      * @param line
      * @param charPos
      * @param in
      * @param msg
      */
    private def mkCompilerError(
        msg: String,
        line: Int, // 1, 2, ...
        charPos: Int, // 0, 1, 2, ...
        in: String
    ): String =
        val hldr = NCCompilerUtils.mkErrorHolder(in, charPos)
        val aMsg = NCUtils.decapitalize(msg) match
            case s: String if s.last == '.' => s
            case s: String => s + '.'
        s"Macro compiler error at line $line - $aMsg\n" +
            s"  |-- ${c("Macro:")} ${hldr.origStr}\n" +
            s"  +-- ${c("Error:")} ${hldr.ptrStr}"

    /**
      *
      * @param in Macro to expand.
      * @return Expanded macro as a set of finite strings.
      */
    def compile(in: String): Set[String] =
        // ANTLR4 armature.
        val lexer = new NCMacroDslLexer(CharStreams.fromString(in))
        val stream = new CommonTokenStream(lexer)
        val parser = new NCMacroDslParser(stream)

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(in))
        parser.addErrorListener(new CompilerErrorListener(in))

        // State automata.
        val fsm = new FiniteStateMachine(parser, in)
        // Parse the input DSL and walk built AST.
        (new ParseTreeWalker).walk(fsm, parser.makro())
        // Return the expanded macro.
        fsm.getExpandedMacro
