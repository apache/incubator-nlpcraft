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

package org.apache.nlpcraft.model.intent.impl.ver2

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.intent.impl.antlr4.{NCIntentDslParser ⇒ Parser, _}
import org.apache.nlpcraft.model.intent.utils.ver2._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import java.lang.{IllegalArgumentException ⇒ IAE}
import java.util.regex.{Pattern, PatternSyntaxException}

object NCIntentDslCompiler extends LazyLogging {
    // Compiler cache.
    private val cache = new mutable.HashMap[String, NCDslIntent]

    private var mdlId: String = _

    /**
     *
     */
    class FiniteStateMachine(dsl: String) extends NCIntentDslBaseListener with NCBaseDslCompiler {
        // Intent components.
        private var id: String = _
        private var ordered: Boolean = false
        private var meta: Map[String, Any] = _
        private val terms = ArrayBuffer.empty[NCDslTerm] // Accumulator for parsed terms.
        private var flowRegex: Option[String] = None

        // Currently term.
        private var termId: String = _
        private var termConv: Boolean = _
        private var min = 1
        private var max = 1
        private var termClsName: String = _
        private var termMtdName: String = _

        // Term's code, i.e. list of instructions.
        private var termInstrs = mutable.Buffer.empty[Instr]

        /*
         * Shared/common implementation.
         */
        override def exitUnaryExpr(ctx: Parser.UnaryExprContext): Unit = termInstrs += parseUnaryExpr(ctx.MINUS(), ctx.NOT())
        override def exitMultExpr(ctx: Parser.MultExprContext): Unit = termInstrs += parseMultExpr(ctx.MULT(), ctx.MOD(), ctx.DIV())
        override def exitPlusExpr(ctx: Parser.PlusExprContext): Unit = termInstrs += parsePlusExpr(ctx.PLUS(), ctx.MINUS())
        override def exitCompExpr(ctx: Parser.CompExprContext): Unit = termInstrs += parseCompExpr(ctx.LT(), ctx.GT(), ctx.LTEQ(), ctx.GTEQ())
        override def exitLogExpr(ctx: Parser.LogExprContext): Unit = termInstrs += parseLogExpr(ctx.AND, ctx.OR())
        override def exitEqExpr(ctx: Parser.EqExprContext): Unit = termInstrs += parseEqExpr(ctx.EQ, ctx.NEQ())
        override def exitCallExpr(ctx: Parser.CallExprContext): Unit = termInstrs += parseCallExpr(ctx.ID())
        override def exitAtom(ctx: Parser.AtomContext): Unit = termInstrs += parseAtom(ctx.getText)

        /**
         *
         * @param min
         * @param max
         */
        private def setMinMax(min: Int, max: Int): Unit = {
            this.min = min
            this.max = max
        }

        override def exitMinMaxShortcut(ctx: Parser.MinMaxShortcutContext): Unit = {
            if (ctx.PLUS() != null)
                setMinMax(1, Integer.MAX_VALUE)
            else if (ctx.MULT() != null)
                setMinMax(0, Integer.MAX_VALUE)
            else if (ctx.QUESTION() != null)
                setMinMax(0, 1)
            else
                assert(false)
        }

        override def exitClsNer(ctx: Parser.ClsNerContext): Unit = {
            if (ctx.javaFqn() != null)
                termClsName = ctx.javaFqn().getText

            termMtdName = ctx.ID().getText
        }

        override def exitTermId(ctx: Parser.TermIdContext): Unit = termId = ctx.ID().getText
        override def exitTermEq(ctx: Parser.TermEqContext): Unit =  termConv = ctx.TILDA() != null
        override def exitIntentId(ctx: Parser.IntentIdContext): Unit = id = ctx.ID().getText
        override def exitMetaDecl(ctx: Parser.MetaDeclContext): Unit = meta = U.jsonToScalaMap(ctx.jsonObj().getText)
        override def exitOrderedDecl(ctx: Parser.OrderedDeclContext): Unit = ordered = ctx.BOOL().getText == "true"

        override def exitFlowDecl(ctx: Parser.FlowDeclContext): Unit = {
            val qRegex = ctx.qstring().getText

            if (qRegex != null && qRegex.length > 2) {
                val regex = qRegex.substring(1, qRegex.length - 1).strip // Remove quotes.

                flowRegex = if (regex.nonEmpty) Some(regex) else None
            }

            if (flowRegex.isDefined) // Pre-check.
                try
                    Pattern.compile(flowRegex.get)
                catch {
                    case e: PatternSyntaxException ⇒
                        throw new IAE(s"${e.getDescription} in DSL intent flow regex '${e.getPattern}' near index ${e.getIndex}.")
                }
        }

        override def exitTerm(ctx: Parser.TermContext): Unit = {
            if (min < 0 || min > max)
                throw new IAE(s"Invalid DSL intent term min quantifiers: $min (must be min >= 0 && min <= max).")
            if (max < 1)
                throw new IAE(s"Invalid DSL intent term max quantifiers: $max (must be max >= 1).")

            val pred =
                if (termMtdName != null) { // User-code defined term.
                    (tok: NCToken, termCtx: NCDslTermContext) ⇒ {
                        // TODO.

                        (true, true)
                    }
                }
                else { // DSL-defined term.
                    val instrs = mutable.Buffer.empty[Instr]

                    instrs ++= termInstrs

                    (tok: NCToken, termCtx: NCDslTermContext) ⇒ {
                        val stack = new mutable.ArrayStack[NCDslTermRetVal]()

                        instrs.foreach(_(tok, stack, termCtx))

                        val x = stack.pop()

                        if (!isBoolean(x.retVal))
                            throw new IAE(s"DSL intent term does not return boolean value: ${ctx.getText}")

                        (asBool(x.retVal), x.usedTok)
                    }

                }

            // Add term.
            terms += NCDslTerm(
                termId,
                pred,
                min,
                max,
                termConv
            )

            // Reset term vars.
            setMinMax(1, 1)
            termInstrs.clear()
            termClsName = null
            termMtdName = null
        }

        /**
         *
         * @return
         */
        def getBuiltIntent: NCDslIntent = {
            require(id != null)
            require(terms.nonEmpty)

            NCDslIntent(dsl, id, ordered, if (meta == null) Map.empty else meta, flowRegex, terms.toArray)
        }
    }

    /**
     * Custom error handler.
     */
    class CompilerErrorListener(dsl: String) extends BaseErrorListener {
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

            val errMsg = s"Intent DSL syntax error at line $line:$charPos - $msg\n" +
                s"  |-- ${c("Model:")}  $mdlId\n" +
                s"  |-- ${c("Intent:")} $dsl\n" +
                s"  +-- ${c("Error:")}  ${makeCharPosPointer(dsl.length, charPos)}"

            throw new NCE(errMsg)
        }
    }

    /**
     *
     * @param dsl Intent DSL to parse.
     * @param mdlId ID of the model the intent belongs to.
     * @return
     */
    def compile(
        dsl: String,
        mdlId: String
    ): NCDslIntent = {
        require(dsl != null)

        val src = dsl.strip()

        this.mdlId = mdlId

        val intent: NCDslIntent = cache.getOrElseUpdate(src, {
            // ANTLR4 armature.
            val lexer = new NCIntentDslLexer(CharStreams.fromString(src))
            val tokens = new CommonTokenStream(lexer)
            val parser = new Parser(tokens)

            // Set custom error handlers.
            lexer.removeErrorListeners()
            parser.removeErrorListeners()
            lexer.addErrorListener(new CompilerErrorListener(src))
            parser.addErrorListener(new CompilerErrorListener(src))

            // State automata.
            val fsm = new FiniteStateMachine(src)

            // Parse the input DSL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.intent())

            // Return the built intent.
            fsm.getBuiltIntent
        })

        intent
    }
}
