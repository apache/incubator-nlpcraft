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

package org.apache.nlpcraft.model.intent.compiler

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime._
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.intent.compiler.antlr4.{NCIntentDslBaseListener, NCIntentDslLexer, NCIntentDslParser ⇒ IDP}
import org.apache.nlpcraft.model.intent.utils._
import org.apache.nlpcraft.model.intent.compiler.{NCIntentDslFragmentCache ⇒ FragCache}
import org.apache.nlpcraft.model._
import scala.collection.JavaConverters._

import java.nio.file.Path
import java.util.Optional
import java.util.regex.{Pattern, PatternSyntaxException}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object NCIntentDslCompiler extends LazyLogging {
    // Compiler caches.
    private val intentCache = new mutable.HashMap[String, Set[NCDslIntent]]
    private val synCache = new mutable.HashMap[String, NCDslSynonym]

    /**
     *
     * @param dsl
     * @param mdlId
     */
    class FiniteStateMachine(dsl: String, mdlId: String) extends NCIntentDslBaseListener with NCIntentDslBaselCompiler {
        // Accumulators for parsed objects.
        private val intents = ArrayBuffer.empty[NCDslIntent]
        private var synonym: NCDslSynonym = _

        // Synonym.
        private var alias: String = _

        // Fragment components.
        private var fragId: String = _
        private var fragMeta: Map[String, Any] = _

        // Intent components.
        private var intentId: String = _
        private var ordered: Boolean = false
        private var flowRegex: Option[String] = None
        private var intentMeta: ScalaMeta = _

        // Accumulator for parsed terms.
        private val terms = ArrayBuffer.empty[NCDslTerm]

        // Currently term.
        private var termId: String = _
        private var termConv: Boolean = _
        private var min = 1
        private var max = 1

        // Current method reference.
        private var refClsName: Option[String] = None
        private var refMtdName: Option[String] = None

        // Current expression code, i.e. list of instructions.
        private var instrs = mutable.Buffer.empty[Instr]

        /*
         * Shared/common implementation.
         */
        override def exitUnaryExpr(ctx: IDP.UnaryExprContext): Unit = instrs += parseUnaryExpr(ctx.MINUS(), ctx.NOT())(ctx)

        override def exitMultExpr(ctx: IDP.MultExprContext): Unit = instrs += parseMultExpr(ctx.MULT(), ctx.MOD(), ctx.DIV())(ctx)

        override def exitPlusExpr(ctx: IDP.PlusExprContext): Unit = instrs += parsePlusExpr(ctx.PLUS(), ctx.MINUS())(ctx)

        override def exitCompExpr(ctx: IDP.CompExprContext): Unit = instrs += parseCompExpr(ctx.LT(), ctx.GT(), ctx.LTEQ(), ctx.GTEQ())(ctx)

        override def exitLogExpr(ctx: IDP.LogExprContext): Unit = instrs += parseLogExpr(ctx.AND, ctx.OR())(ctx)

        override def exitEqExpr(ctx: IDP.EqExprContext): Unit = instrs += parseEqExpr(ctx.EQ, ctx.NEQ())(ctx)

        override def exitCallExpr(ctx: IDP.CallExprContext): Unit = instrs += parseCallExpr(ctx.FUN_NAME())(ctx)

        override def exitAtom(ctx: IDP.AtomContext): Unit = instrs += parseAtom(ctx.getText)(ctx)

        /**
         *
         * @param min
         * @param max
         */
        private def setMinMax(min: Int, max: Int): Unit = {
            this.min = min
            this.max = max
        }

        override def exitMinMaxShortcut(ctx: IDP.MinMaxShortcutContext): Unit = {
            if (ctx.PLUS() != null)
                setMinMax(1, Integer.MAX_VALUE)
            else if (ctx.MULT() != null)
                setMinMax(0, Integer.MAX_VALUE)
            else if (ctx.QUESTION() != null)
                setMinMax(0, 1)
            else
                assert(false)
        }

        override def exitMinMaxRange(ctx: IDP.MinMaxRangeContext): Unit = {
            val minStr = ctx.getChild(1).getText.trim
            val maxStr = ctx.getChild(3).getText.trim

            try
                setMinMax(java.lang.Integer.parseInt(minStr), java.lang.Integer.parseInt(maxStr))
            catch {
                // Errors should be caught during compilation phase.
                case _: NumberFormatException ⇒ assert(false)
            }
        }

        override def exitMtdRef(ctx: IDP.MtdRefContext): Unit = {
            if (ctx.javaFqn() != null)
                refClsName = Some(ctx.javaFqn().getText)

            refMtdName = Some(ctx.id().getText)
        }

        override def exitTermId(ctx: IDP.TermIdContext): Unit = {
            termId = ctx.id().getText

            if (terms.exists(t ⇒ t.id === termId))
                throw newSyntaxError(s"Duplicate intent term ID: $termId")(ctx.id())
        }

        override def exitIntentId(ctx: IDP.IntentIdContext): Unit = {
            intentId = ctx.id().getText

            if (intents.exists(i ⇒ i.id != null && i.id == intentId))
                throw newSyntaxError(s"Duplicate intent ID: $intentId")(ctx.id())
        }


        override def exitAlias(ctx: IDP.AliasContext): Unit = {
            alias = ctx.id().getText
        }

        override def exitSynonym(ctx: IDP.SynonymContext): Unit = {
            implicit val evidence: ParserRuleContext = ctx

            val code = mutable.Buffer.empty[Instr] ++ instrs // Local copy.

            synonym = NCDslSynonym(
                Option(alias),
                (tok: NCToken, termCtx: NCDslTermContext) ⇒ {
                    val stack = new mutable.ArrayStack[NCDslTermRetVal]()

                    // Execute all instructions.
                    code.foreach(_ (tok, stack, termCtx))

                    // Pop final result from stack.
                    val x = stack.pop()

                    if (!isBool(x.retVal))
                        throw newRuntimeError(s"Synonym does not return boolean value: ${ctx.getText}")

                    asBool(x.retVal)
                }
            )
        }

        override def exitFragId(ctx: IDP.FragIdContext): Unit = {
            fragId = ctx.id().getText

            if (FragCache.get(mdlId, fragId).isDefined)
                throw newSyntaxError(s"Duplicate fragment ID: $fragId")(ctx.id())
        }

        override def exitTermEq(ctx: IDP.TermEqContext): Unit = termConv = ctx.TILDA() != null

        override def exitFragMeta(ctx: IDP.FragMetaContext): Unit = fragMeta = U.jsonToScalaMap(ctx.jsonObj().getText)

        override def exitMetaDecl(ctx: IDP.MetaDeclContext): Unit = intentMeta = U.jsonToScalaMap(ctx.jsonObj().getText)

        override def exitOrderedDecl(ctx: IDP.OrderedDeclContext): Unit = ordered = ctx.BOOL().getText == "true"

        override def exitFragRef(ctx: IDP.FragRefContext): Unit = {
            val id = ctx.id().getText

            FragCache.get(mdlId, id) match {
                case Some(frag) ⇒
                    val meta = if (fragMeta == null) Map.empty[String, Any] else fragMeta

                    for (fragTerm ← frag.terms)
                        if (terms.exists(t ⇒ t.id === fragTerm.id))
                            throw newSyntaxError(s"Duplicate term ID '${fragTerm.id.get}' in fragment '$id'.")(ctx.id())
                        else
                            terms += fragTerm.cloneWithFragMeta(meta)

                case None ⇒ throw newSyntaxError(s"Unknown intent fragment ID: $id")(ctx.id())
            }

            fragMeta = null
        }

        override def exitFlowDecl(ctx: IDP.FlowDeclContext): Unit = {
            implicit val evidence: ParserRuleContext = ctx

            if (ctx.qstring() != null) {
                val regex = ctx.qstring().getText

                if (regex != null && regex.length > 2)
                    flowRegex = if (regex.nonEmpty) Some(regex) else None

                if (flowRegex.isDefined) // Pre-check.
                    try
                        Pattern.compile(flowRegex.get)
                    catch {
                        case e: PatternSyntaxException ⇒
                            newSyntaxError(s"${e.getDescription} in intent flow regex '${e.getPattern}' near index ${e.getIndex}.")(ctx.qstring())
                    }
            }
        }

        override def exitTerm(ctx: IDP.TermContext): Unit = {
            implicit val c: ParserRuleContext = ctx

            if (min < 0 || min > max)
                throw newSyntaxError(s"Invalid intent term min quantifiers: $min (must be min >= 0 && min <= max).")(ctx.minMax())
            if (max < 1)
                throw newSyntaxError(s"Invalid intent term max quantifiers: $max (must be max >= 1).")(ctx.minMax())

            val pred =
                if (refMtdName != null) { // User-code defined term.
                    // Closure copies.
                    val clsName = refClsName.orNull
                    val mtdName = refMtdName.orNull

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
                        val mdlCls = if (clsName == null) mdl.meta[String](MDL_META_MODEL_CLASS_KEY) else clsName

                        try {
                            val obj = if (clsName == null) mdl else U.mkObject(clsName)
                            val mtd = Thread.currentThread().getContextClassLoader.loadClass(mdlCls)
                                .getMethod(mtdName, classOf[NCTokenPredicateContext])

                            var flag = mtd.canAccess(mdl)

                            val res = try {
                                if (!flag) {
                                    mtd.setAccessible(true)

                                    flag = true
                                }
                                else
                                    flag = false

                                mtd.invoke(obj, javaCtx).asInstanceOf[NCTokenPredicateResult]
                            }
                            finally {
                                if (flag)
                                    try
                                        mtd.setAccessible(false)
                                    catch {
                                        case e: SecurityException ⇒
                                            throw new NCE(s"Access or security error in custom intent term: $mdlCls.$mtdName", e)
                                    }
                            }

                            (res.getResult, res.wasTokenUsed())
                        }
                        catch {
                            case e: Exception ⇒
                                throw newRuntimeError(s"Failed to invoke custom intent term: $mdlCls.$mtdName", e)
                        }
                    }
                }
                else { // DSL-defined term.
                    val code = mutable.Buffer.empty[Instr]

                    code ++= instrs

                    (tok: NCToken, termCtx: NCDslTermContext) ⇒ {
                        val stack = new mutable.ArrayStack[NCDslTermRetVal]()

                        // Execute all instructions.
                        code.foreach(_ (tok, stack, termCtx))

                        // Pop final result from stack.
                        val x = stack.pop()

                        if (!isBool(x.retVal))
                            throw newRuntimeError(s"Intent term does not return boolean value: ${ctx.getText}")

                        (asBool(x.retVal), x.usedTok)
                    }
                }

            // Add term.
            terms += NCDslTerm(
                Option(termId),
                pred,
                min,
                max,
                termConv
            )

            // Reset term vars.
            setMinMax(1, 1)
            termId = null
            instrs.clear()
            refClsName = None
            refMtdName = None
        }

        override def exitFrag(ctx: IDP.FragContext): Unit = {
            FragCache.add(mdlId, NCDslFragment(fragId, terms.toList))

            terms.clear()
        }

        override def exitIntent(ctx: IDP.IntentContext): Unit = {
            intents += NCDslIntent(
                dsl,
                intentId,
                ordered,
                if (intentMeta == null) Map.empty else intentMeta,
                flowRegex,
                refClsName,
                refMtdName,
                terms.toList
            )

            refClsName = None
            refMtdName = None
            intentMeta = null
            terms.clear()
        }

        /**
         *
         * @return
         */
        def getCompiledIntents: Set[NCDslIntent] = intents.toSet

        /**
         *
         * @return
         */
        def getCompiledSynonym: NCDslSynonym = synonym

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
        line: Int, // 1, 2, ...
        charPos: Int, // 0, 1, 2, ...
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
        val aMsg = U.decapitalize(msg) match {
            case s: String if s.last == '.' ⇒ s
            case s: String ⇒ s + '.'
        }

        s"DSL $kind error in '$srcName' at line $line:${charPos + 1} - $aMsg\n" +
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
     *
     * @param dsl
     * @param mdlId
     * @param srcName
     * @return
     */
    private def parseIntents(
        dsl: String,
        mdlId: String,
        srcName: String
    ): Set[NCDslIntent] = {
        require(dsl != null)
        require(mdlId != null)
        require(srcName != null)

        val x = dsl.strip()

        val intents: Set[NCDslIntent] = intentCache.getOrElseUpdate(x, {
            val (fsm, parser) = antlr4Armature(x, mdlId)

            // Parse the input DSL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.dsl())

            // Return the compiled intents.
            fsm.getCompiledIntents
        })

        intents
    }

    /**
     *
     * @param dsl
     * @param mdlId
     * @return
     */
    private def parseSynonym(
        dsl: String,
        mdlId: String
    ): NCDslSynonym = {
        require(dsl != null)
        require(mdlId != null)

        val x = dsl.strip()

        val syn: NCDslSynonym = synCache.getOrElseUpdate(x, {
            val (fsm, parser) = antlr4Armature(x, mdlId)

            // Parse the input DSL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.synonym())

            // Return the compiled synonym.
            fsm.getCompiledSynonym
        })

        syn
    }

    /**
     *
     * @param dsl
     * @param mdlId
     * @param srcName
     * @return
     */
    private def antlr4Armature(
        dsl: String,
        mdlId: String,
        srcName: String = "<inline>"
    ): (FiniteStateMachine, IDP) = {
        val lexer = new NCIntentDslLexer(CharStreams.fromString(dsl, srcName))
        val tokens = new CommonTokenStream(lexer)
        val parser = new IDP(tokens)

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(dsl, mdlId))
        parser.addErrorListener(new CompilerErrorListener(dsl, mdlId))

        // State automata + it's parser.
        new FiniteStateMachine(dsl, mdlId) → parser
    }

    /**
     * Compiles inline (supplied) fragments and/or intents from given file. Note that fragments are
     * accumulated in a static map keyed by model ID. Only intents are returned, if any.
     *
     * @param filePath *.nc intent DSL file to compile.
     * @param mdlId    ID of the model *.nc file belongs to.
     * @return
     */
    @throws[NCE]
    def compileIntents(
        filePath: Path,
        mdlId: String
    ): Set[NCDslIntent] = parseIntents(U.readFile(filePath.toFile).mkString("\n"), mdlId, filePath.getFileName.toString)

    /**
     * Compiles inline (supplied) fragments and/or intents. Note that fragments are accumulated in a static
     * map keyed by model ID. Only intents are returned, if any.
     *
     * @param dsl     Intent DSL to compile.
     * @param mdlId   ID of the model DSL belongs to.
     * @param srcName Optional source name.
     * @return
     */
    @throws[NCE]
    def compileIntents(
        dsl: String,
        mdlId: String,
        srcName: String = "<inline>"
    ): Set[NCDslIntent] = parseIntents(dsl, mdlId, srcName)

    /**
     *
     * @param dsl   Synonym DSL to compile.
     * @param mdlId ID of the model DSL belongs to.
     * @return
     */
    @throws[NCE]
    def compileSynonym(
        dsl: String,
        mdlId: String
    ): NCDslSynonym = parseSynonym(dsl, mdlId)
}
