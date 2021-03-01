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
import org.apache.nlpcraft.model.intent.impl.antlr4.{NCIntentDslParser ⇒ Parser, _}
import org.apache.nlpcraft.model.intent.utils.ver2._
import org.apache.nlpcraft.model.{NCMetadata, NCRequest, NCToken, NCTokenPredicateContext, NCTokenPredicateResult}

import java.util.Optional
import java.util.regex.{Pattern, PatternSyntaxException}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

object NCIntentDslCompiler extends LazyLogging {
    // Compiler cache.
    private val cache = new mutable.HashMap[String, NCDslIntent]

    /**
     *
     * @param dsl
     * @param mdlId
     */
    class FiniteStateMachine(dsl: String, mdlId: String) extends NCIntentDslBaseListener with NCIntentDslBaselCompiler {
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
        override def exitUnaryExpr(ctx: Parser.UnaryExprContext): Unit = termInstrs += parseUnaryExpr(ctx.MINUS(), ctx.NOT())(ctx)
        override def exitMultExpr(ctx: Parser.MultExprContext): Unit = termInstrs += parseMultExpr(ctx.MULT(), ctx.MOD(), ctx.DIV())(ctx)
        override def exitPlusExpr(ctx: Parser.PlusExprContext): Unit = termInstrs += parsePlusExpr(ctx.PLUS(), ctx.MINUS())(ctx)
        override def exitCompExpr(ctx: Parser.CompExprContext): Unit = termInstrs += parseCompExpr(ctx.LT(), ctx.GT(), ctx.LTEQ(), ctx.GTEQ())(ctx)
        override def exitLogExpr(ctx: Parser.LogExprContext): Unit = termInstrs += parseLogExpr(ctx.AND, ctx.OR())(ctx)
        override def exitEqExpr(ctx: Parser.EqExprContext): Unit = termInstrs += parseEqExpr(ctx.EQ, ctx.NEQ())(ctx)
        override def exitCallExpr(ctx: Parser.CallExprContext): Unit = termInstrs += parseCallExpr(ctx.ID())(ctx)
        override def exitAtom(ctx: Parser.AtomContext): Unit = termInstrs += parseAtom(ctx.getText)(ctx)

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
            implicit val evidence: ParserRuleContext = ctx

            val regex = ctx.qstring().getText

            if (regex != null && regex.length > 2) 
                flowRegex = if (regex.nonEmpty) Some(regex) else None

            if (flowRegex.isDefined) // Pre-check.
                try
                    Pattern.compile(flowRegex.get)
                catch {
                    case e: PatternSyntaxException ⇒
                        newSyntaxError(s"${e.getDescription} in intent flow regex '${e.getPattern}' near index ${e.getIndex}.")
                }
        }

        override def exitTerm(ctx: Parser.TermContext): Unit = {
            implicit val c: ParserRuleContext = ctx

            if (min < 0 || min > max)
                throw newSyntaxError(s"Invalid intent term min quantifiers: $min (must be min >= 0 && min <= max).")
            if (max < 1)
                throw newSyntaxError(s"Invalid intent term max quantifiers: $max (must be max >= 1).")

            val pred =
                if (termMtdName != null) { // User-code defined term.
                    (tok: NCToken, termCtx: NCDslTermContext) ⇒ {
                        val javaCtx: NCTokenPredicateContext = new NCTokenPredicateContext {
                            override lazy val getRequest: NCRequest = termCtx.req
                            override lazy val getToken: NCToken = tok
                            override lazy val getIntentMeta: Optional[NCMetadata] =
                                if (termCtx.intentMeta != null)
                                    Optional.of(NCMetadata.apply(termCtx.intentMeta.asJava))
                                else
                                    Optional.empty()
                        }

                        val mdl = tok.getModel
                        val mdlCls = if (termClsName == null) mdl.meta[String](MDL_META_MODEL_CLASS_KEY) else termClsName

                        try {
                            val obj = if (termClsName == null) mdl else U.mkObject(termClsName)
                            val mtd = Thread.currentThread().getContextClassLoader.loadClass(mdlCls).getMethod(termMtdName, classOf[NCTokenPredicateContext])

                            val res = mtd.invoke(obj, javaCtx).asInstanceOf[NCTokenPredicateResult]

                            (res.getResult, res.wasTokenUsed())
                        }
                        catch {
                            case e: Exception ⇒
                                throw newRuntimeError(s"Failed to invoke custom intent term: $mdlCls.$termMtdName", e)
                        }
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
                            throw newRuntimeError(s"Intent term does not return boolean value: ${ctx.getText}")

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

            NCDslIntent(dsl, id, ordered, if (meta == null) Map.empty else meta, flowRegex, terms.toList)
        }
        
        override def syntaxError(errMsg: String, srcName: String, line: Int, pos: Int): NCE =
            throw new NCE(mkSyntaxError(errMsg, srcName, line, pos, dsl, mdlId))
        override def runtimeError(errMsg: String, srcName: String, line: Int, pos: Int, cause: Exception = null): NCE =
            throw new NCE(mkRuntimeError(errMsg, srcName, line, pos, dsl, mdlId), cause)
    }

    /**
     *
     * @param msg
     * @param line
     * @param charPos
     * @param dsl Original DSL text (input).
     * @param mdlId
     * @return
     */
    private def mkSyntaxError(
        msg: String,
        srcName: String,
        line: Int, // 1, 2, ...
        charPos: Int, // 0, 1, 2, ...
        dsl: String,
        mdlId: String): String = mkError("syntax", msg, srcName, line, charPos, dsl, mdlId)

    /**
      *
      * @param msg
      * @param dsl
      * @param mdlId
      * @param srcName
      * @param line
      * @param charPos
      * @return
      */
    private def mkRuntimeError(
        msg: String,
        srcName: String,
        line: Int,
        charPos: Int,
        dsl: String,
        mdlId: String): String = mkError("runtime", msg, srcName, line, charPos, dsl, mdlId)

    private def mkError(
        kind: String,
        msg: String,
        srcName: String,
        line: Int,
        charPos: Int,
        dsl: String,
        mdlId: String): String = {
        val dslLine = dsl.split("\n")(line - 1)
        val dash = "-" * dslLine.length
        val pos = Math.max(0, charPos)
        val posPtr = dash.substring(0, pos) + r("^") + y(dash.substring(pos + 1))
        val dslPtr = dslLine.substring(0, pos) + r(dslLine.charAt(pos)) + y(dslLine.substring(pos + 1))
        val src = if (srcName == "<unknown>") "<inline>"else srcName
        val aMsg = U.decapitalize(msg) match {
            case s: String if s.last == '.' ⇒ s
            case s: String ⇒ s + '.'
        }
        
        s"Intent DSL $kind error in '$src' at line $line:${charPos + 1} - $aMsg\n" +
        s"  |-- ${c("Model:")}    $mdlId\n" +
        s"  |-- ${c("Line:")}     $dslPtr\n" +
        s"  +-- ${c("Position:")} $posPtr"
    }
    
    /**
     * Custom error handler.
     *
     * @param dsl
     * @param mdlId
     */
    class CompilerErrorListener(dsl: String, mdlId: String) extends BaseErrorListener {
        /**
         *
         * @param recog
         * @param badSymbol
         * @param line
         * @param charPos
         * @param msg
         * @param e
         */
        override def syntaxError(
            recog: Recognizer[_, _],
            badSymbol: scala.Any,
            line: Int, // 1, 2, ...
            charPos: Int, // 1, 2, ...
            msg: String,
            e: RecognitionException): Unit =
            throw new NCE(mkSyntaxError(msg, recog.getInputStream.getSourceName, line, charPos - 1, dsl, mdlId))
    }
    
    /**
      * Compile individual fragment or intent. Note that fragments are accumulated in a static
      * map keyed by model ID. Only intents are returned, if any.
      *
      * @param filePath *.nc DSL file to compile.
      * @param mdlId ID of the model *.nc file belongs to.
      * @return
      */
    def compileFile(
        filePath: String,
        mdlId: String
    ): Set[NCDslIntent] = ???
    
    /**
      * Compile individual fragment or intent. Note that fragments are accumulated in a static
      * map keyed by model ID. Only intents are returned, if any.
      *
      * @param dsl DSL to compile.
      * @param mdlId ID of the model DSL belongs to.
      * @return
      */
    def compile(
        dsl: String,
        mdlId: String
    ): Set[NCDslIntent] = {
        require(dsl != null)

        val src = dsl.strip()

        val intent: NCDslIntent = cache.getOrElseUpdate(src, {
            // ANTLR4 armature.
            val lexer = new NCIntentDslLexer(CharStreams.fromString(src))
            val tokens = new CommonTokenStream(lexer)
            val parser = new Parser(tokens)

            // Set custom error handlers.
            lexer.removeErrorListeners()
            parser.removeErrorListeners()
            lexer.addErrorListener(new CompilerErrorListener(src, mdlId))
            parser.addErrorListener(new CompilerErrorListener(src, mdlId))

            // State automata.
            val fsm = new FiniteStateMachine(src, mdlId)

            // Parse the input DSL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.intent())

            // Return the built intent.
            fsm.getBuiltIntent
        })

        Set(intent) // TODO
    }
}
