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
import org.antlr.v4.runtime.{ParserRuleContext ⇒ PRC}
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.intent.compiler.antlr4.{NCIdlBaseListener, NCIdlLexer, NCIdlParser ⇒ IDP}
import org.apache.nlpcraft.model.intent.compiler.{NCIdlCompilerGlobal ⇒ Global}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlFunction, NCIdlIntent, NCIdlStack, NCIdlStackItem ⇒ Z, NCIdlSynonym, NCIdlTerm}

import java.io._
import java.net._
import scala.collection.JavaConverters._
import java.util.Optional
import java.util.regex.{Pattern, PatternSyntaxException}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object NCIdlCompiler extends LazyLogging {
    // Compiler caches.
    private val intentCache = new mutable.HashMap[String, Set[NCIdlIntent]]
    private val synCache = new mutable.HashMap[String, NCIdlSynonym]

    /**
      *
      * @param origin
      * @param idl
      * @param mdl
      */
    class FiniteStateMachine(origin: String, idl: String, mdl: NCModel) extends NCIdlBaseListener with NCIdlCompilerBase {
        // Actual value for '*' as in min/max shortcut.
        final private val MINMAX_MAX = 100

        // Accumulators for parsed objects.
        private val intents = ArrayBuffer.empty[NCIdlIntent]
        private var synonym: NCIdlSynonym = _

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
        private val terms = ArrayBuffer.empty[NCIdlTerm]

        // Currently term.
        private var vars = mutable.HashMap.empty[String, NCIdlFunction]
        private var termId: String = _
        private var termConv: Boolean = _
        private var min = 1
        private var max = 1

        // Current method reference.
        private var refClsName: Option[String] = None
        private var refMtdName: Option[String] = None

        // List of instructions for the current expression.
        private var expr = mutable.Buffer.empty[SI]


        /**
         *
         * @return
         */
        def getCompiledIntents: Set[NCIdlIntent] = intents.toSet

        /**
         *
         * @return
         */
        def getCompiledSynonym: NCIdlSynonym = synonym

        /*
         * Shared/common implementation.
         */
        override def exitUnaryExpr(ctx: IDP.UnaryExprContext): Unit = expr += parseUnaryExpr(ctx.MINUS(), ctx.NOT())(ctx)
        override def exitMultDivModExpr(ctx: IDP.MultDivModExprContext): Unit = expr += parseMultDivModExpr(ctx.MULT(), ctx.MOD(), ctx.DIV())(ctx)
        override def exitPlusMinusExpr(ctx: IDP.PlusMinusExprContext): Unit = expr += parsePlusMinusExpr(ctx.PLUS(), ctx.MINUS())(ctx)
        override def exitCompExpr(ctx: IDP.CompExprContext): Unit = expr += parseCompExpr(ctx.LT(), ctx.GT(), ctx.LTEQ(), ctx.GTEQ())(ctx)
        override def exitAndOrExpr(ctx: IDP.AndOrExprContext): Unit = expr += parseAndOrExpr(ctx.AND, ctx.OR())(ctx)
        override def exitEqNeqExpr(ctx: IDP.EqNeqExprContext): Unit = expr += parseEqNeqExpr(ctx.EQ, ctx.NEQ())(ctx)
        override def exitCallExpr(ctx: IDP.CallExprContext): Unit = expr += parseCallExpr(ctx.FUN_NAME())(ctx)
        override def exitAtom(ctx: IDP.AtomContext): Unit = expr += parseAtom(ctx.getText)(ctx)
        override def exitTermEq(ctx: IDP.TermEqContext): Unit = termConv = ctx.TILDA() != null
        override def exitFragMeta(ctx: IDP.FragMetaContext): Unit = fragMeta = U.jsonToScalaMap(ctx.jsonObj().getText)
        override def exitMetaDecl(ctx: IDP.MetaDeclContext): Unit = intentMeta = U.jsonToScalaMap(ctx.jsonObj().getText)
        override def exitOrderedDecl(ctx: IDP.OrderedDeclContext): Unit = ordered = ctx.BOOL().getText == "true"
        override def exitIntentId(ctx: IDP.IntentIdContext): Unit =  intentId = ctx.id().getText
        override def exitAlias(ctx: IDP.AliasContext): Unit = alias = ctx.id().getText

        override def enterCallExpr(ctx: IDP.CallExprContext): Unit =
            expr += ((_, stack: NCIdlStack, _) ⇒ stack.push(stack.PLIST_MARKER))

        /**
         *
         * @param min
         * @param max
         */
        private def setMinMax(min: Int, max: Int): Unit = {
            this.min = min
            this.max = max
        }

        override def exitVarRef(ctx: IDP.VarRefContext): Unit = {
            val varName = ctx.id().getText

            if (!vars.contains(varName))
                throw newSyntaxError(s"Unknown variable: @$varName")(ctx)

            val instr: SI = (tok: NCToken, stack: S, idlCtx: NCIdlContext) ⇒
                stack.push(() ⇒ idlCtx.vars(varName)(tok, idlCtx))

            expr += instr
        }

        override def exitVarDecl(ctx: IDP.VarDeclContext): Unit = {
            val varName = ctx.id().getText

            if (vars.contains(varName))
                throw newSyntaxError(s"Duplicate variable: @$varName")(ctx)

            vars += varName → exprToFunction("Variable declaration", _ ⇒ true)(ctx)

            expr.clear()
        }

        override def exitMinMaxShortcut(ctx: IDP.MinMaxShortcutContext): Unit = {
            if (ctx.PLUS() != null)
                setMinMax(1, MINMAX_MAX)
            else if (ctx.MULT() != null)
                setMinMax(0, MINMAX_MAX)
            else if (ctx.QUESTION() != null)
                setMinMax(0, 1)
            else
                assert(false)
        }

        override def exitMinMaxRange(ctx: IDP.MinMaxRangeContext): Unit = {
            val minStr = ctx.getChild(1).getText.trim
            val maxStr = ctx.getChild(3).getText.trim

            try {
                val min = java.lang.Integer.parseInt(minStr)
                val max = java.lang.Integer.parseInt(maxStr)

                if (min < 0)
                    throw newSyntaxError(s"Min value cannot be negative: $min")(ctx)
                if (min > max)
                    throw newSyntaxError(s"Min value '$min' cannot be greater than max value '$min'.")(ctx)
                if (max > MINMAX_MAX)
                    throw newSyntaxError(s"Max value '$max' cannot be greater than '$MINMAX_MAX'.")(ctx)

                setMinMax(min, max)
            }
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

        override def exitSynonym(ctx: IDP.SynonymContext): Unit = {
            implicit val evidence: PRC = ctx

            val pred = exprToFunction("Synonym", isBool)
            val capture = alias
            val wrapper: NCIdlFunction = (tok: NCToken, ctx: NCIdlContext) ⇒ {
                val Z(res, tokUses) = pred(tok, ctx)

                // Store predicate's alias, if any, in token metadata if this token satisfies this predicate.
                // NOTE: token can have multiple aliases associated with it.
                if (asBool(res) && capture != null) { // NOTE: we ignore 'tokUses' here on purpose.
                    val meta = tok.getMetadata

                    if (!meta.containsKey(TOK_META_ALIASES_KEY))
                        meta.put(TOK_META_ALIASES_KEY, new java.util.HashSet[String]())

                    val aliases = meta.get(TOK_META_ALIASES_KEY).asInstanceOf[java.util.Set[String]]

                    aliases.add(capture)
                }

                Z(res, tokUses)
            }

            synonym = NCIdlSynonym(origin, Option(alias), wrapper)

            alias = null
            expr.clear()
        }

        override def exitFragId(ctx: IDP.FragIdContext): Unit = {
            fragId = ctx.id().getText

            if (Global.getFragment(mdl.getId, fragId).isDefined)
                throw newSyntaxError(s"Duplicate fragment ID: $fragId")(ctx.id())
        }

        override def exitFragRef(ctx: IDP.FragRefContext): Unit = {
            val id = ctx.id().getText

            Global.getFragment(mdl.getId, id) match {
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
            if (ctx.qstring() != null) {
                val regex = U.trimQuotes(ctx.qstring().getText)

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
            if (min < 0 || min > max)
                throw newSyntaxError(s"Invalid intent term min quantifiers: $min (must be min >= 0 && min <= max).")(ctx.minMax())
            if (max < 1)
                throw newSyntaxError(s"Invalid intent term max quantifiers: $max (must be max >= 1).")(ctx.minMax())

            val pred: NCIdlFunction = if (refMtdName.isDefined) { // User-code defined term.
                // Closure copies.
                val clsName = refClsName.orNull
                val mtdName = refMtdName.orNull

                (tok: NCToken, termCtx: NCIdlContext) ⇒ {
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
                        val res = U.callMethod[NCTokenPredicateContext, NCTokenPredicateResult](
                            () ⇒ if (clsName == null) mdl else U.mkObject(clsName),
                            mtdName,
                            javaCtx
                        )

                        Z(res.getResult, res.getTokenUses)
                    }
                    catch {
                        case e: Exception ⇒
                            throw newRuntimeError(s"Failed to invoke custom intent term: $mdlCls.$mtdName", e)(ctx.mtdDecl())
                    }
                }
            }
            else  // IDL term.
                exprToFunction("Intent term", isBool)(ctx.expr())

            // Add term.
            terms += NCIdlTerm(
                ctx.getText,
                Option(termId),
                vars.values.toList,
                pred,
                min,
                max,
                termConv
            )

            // Reset term vars.
            setMinMax(1, 1)
            termId = null
            expr.clear()
            vars.clear()
            refClsName = None
            refMtdName = None
        }

        /**
         *
         * @param subj
         * @param check
         * @param ctx
         * @return
         */
        private def exprToFunction(
            subj: String,
            check: Object ⇒ Boolean
        )
        (
            implicit ctx: PRC
        ): NCIdlFunction = {
            val code = mutable.Buffer.empty[SI]

            code ++= expr

            (tok: NCToken, termCtx: NCIdlContext) ⇒ {
                val stack = new S()

                // Execute all instructions.
                code.foreach(_ (tok, stack, termCtx))

                // Pop final result from stack.
                val x = stack.pop()()
                val v = x.value

                // Check final value's type.
                if (!check(v))
                    throw newRuntimeError(s"$subj returned value of unexpected type '$v' in: ${ctx.getText}")

                Z(v, x.tokUse)
            }
        }

        override def exitFrag(ctx: IDP.FragContext): Unit = {
            Global.addFragment(mdl.getId, NCIdlFragment(fragId, terms.toList))

            terms.clear()
            fragId = null
        }

        /**
          *
          * @param intent
          * @param ctx
          */
        private def addIntent(intent: NCIdlIntent)(implicit ctx: ParserRuleContext): Unit = {
            val intentId = intent.id

            if (intents.exists(_.id == intentId))
                throw newSyntaxError(s"Duplicate intent ID: $intentId")

            intents += intent
        }

        override def exitImp(ctx: IDP.ImpContext): Unit = {
            val x = U.trimQuotes(ctx.qstring().getText)

            if (Global.hasImport(x))
                logger.warn(s"Ignoring already processed IDL import '$x' in: $origin")
            else {
                Global.addImport(x)

                var imports: Set[NCIdlIntent] = null

                val file = new File(x)

                // First, try absolute path.
                if (file.exists())
                    imports = NCIdlCompiler.compileIntents(
                        U.readFile(file).mkString("\n"),
                        mdl,
                        x
                    )

                // Second, try as a classloader resource.
                if (imports == null) {
                    val in = mdl.getClass.getClassLoader.getResourceAsStream(x)

                    if (in != null)
                        imports = NCIdlCompiler.compileIntents(
                            U.readStream(in).mkString("\n"),
                            mdl,
                            x
                        )
                }

                // Finally, try as URL resource.
                if (imports == null) {
                    try
                        imports = NCIdlCompiler.compileIntents(
                            U.readStream(new URL(x).openStream()).mkString("\n"),
                            mdl,
                            x
                        )
                    catch {
                        case _: Exception ⇒ throw newSyntaxError(s"Invalid or unknown import location: $x")(ctx.qstring())
                    }
                }

                require(imports != null)

                imports.foreach(addIntent(_)(ctx.qstring()))
            }
        }

        override def exitIntent(ctx: IDP.IntentContext): Unit = {
            addIntent(
                NCIdlIntent(
                    origin,
                    idl,
                    intentId,
                    ordered,
                    if (intentMeta == null) Map.empty else intentMeta,
                    flowRegex,
                    refClsName,
                    refMtdName,
                    terms.toList
                )
            )(ctx.intentId())

            refClsName = None
            refMtdName = None
            intentMeta = null
            terms.clear()
        }

        override def syntaxError(errMsg: String, srcName: String, line: Int, pos: Int): NCE =
            throw new NCE(mkSyntaxError(errMsg, srcName, line, pos, idl, origin, mdl))

        override def runtimeError(errMsg: String, srcName: String, line: Int, pos: Int, cause: Exception = null): NCE =
            throw new NCE(mkRuntimeError(errMsg, srcName, line, pos, idl, origin, mdl), cause)
    }

    /**
     *
     * @param msg
     * @param srcName
     * @param line
     * @param charPos
     * @param idl
     * @param origin IDL origin.
     * @param mdl
     * @return
     */
    private def mkSyntaxError(
        msg: String,
        srcName: String,
        line: Int, // 1, 2, ...
        charPos: Int, // 0, 1, 2, ...
        idl: String,
        origin: String,
        mdl: NCModel): String = mkError("syntax", msg, srcName, line, charPos, idl, origin, mdl)

    /**
     *
     * @param msg
     * @param srcName
     * @param line
     * @param charPos
     * @param idl
     * @param origin IDL origin.
     * @param mdl
     * @return
     */
    private def mkRuntimeError(
        msg: String,
        srcName: String,
        line: Int, // 1, 2, ...
        charPos: Int, // 0, 1, 2, ...
        idl: String,
        origin: String,
        mdl: NCModel): String = mkError("runtime", msg, srcName, line, charPos, idl, origin, mdl)

    /**
     *
     * @param kind
     * @param msg
     * @param srcName
     * @param line
     * @param charPos
     * @param idl
     * @param origin IDL origin.
     * @param mdl
     * @return
     */
    private def mkError(
        kind: String,
        msg: String,
        srcName: String,
        line: Int,
        charPos: Int,
        idl: String,
        origin: String,
        mdl: NCModel): String = {
        val idlLine = idl.split("\n")(line - 1)
        val dash = "-" * idlLine.length
        val pos = Math.max(0, charPos)
        val posPtr = dash.substring(0, pos) + r("^") + y(dash.substring(pos + 1))
        val idlPtr = idlLine.substring(0, pos) + r(idlLine.charAt(pos)) + y(idlLine.substring(pos + 1))
        val aMsg = U.decapitalize(msg) match {
            case s: String if s.last == '.' ⇒ s
            case s: String ⇒ s + '.'
        }

        s"IDL $kind error in '$srcName' at line $line:${charPos + 1} - $aMsg\n" +
            s"  |-- ${c("Model ID:")} ${mdl.getId}\n" +
            s"  |-- ${c("Model origin:")} ${mdl.getOrigin}\n" +
            s"  |-- ${c("Intent origin:")} $origin\n" +
            s"  |-- $RST$W--------------$RST\n" +
            s"  |-- ${c("Line:")}     $idlPtr\n" +
            s"  +-- ${c("Position:")} $posPtr"
    }

    /**
     * Custom error handler.
     *
     * @param dsl
     * @param mdl
     * @param origin IDL origin.
     */
    class CompilerErrorListener(dsl: String, mdl: NCModel, origin: String) extends BaseErrorListener {
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
            throw new NCE(mkSyntaxError(msg, recog.getInputStream.getSourceName, line, charPos - 1, dsl, origin, mdl))
    }

    /**
     *
     * @param idl
     * @param mdl
     * @param srcName
     * @return
     */
    private def parseIntents(
        idl: String,
        mdl: NCModel,
        srcName: String
    ): Set[NCIdlIntent] = {
        require(idl != null)
        require(mdl != null)
        require(srcName != null)

        val x = idl.strip()

        val intents: Set[NCIdlIntent] = intentCache.getOrElseUpdate(x, {
            val (fsm, parser) = antlr4Armature(x, mdl, srcName)

            // Parse the input IDL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.idl())

            // Return the compiled intents.
            fsm.getCompiledIntents
        })

        intents
    }

    /**
     *
     * @param idl
     * @param mdl
     * @return
     */
    private def parseSynonym(
        idl: String,
        mdl: NCModel,
        origin: String
    ): NCIdlSynonym = {
        require(idl != null)
        require(mdl != null)

        val x = idl.strip()

        val syn: NCIdlSynonym = synCache.getOrElseUpdate(x, {
            val (fsm, parser) = antlr4Armature(x, mdl, origin)

            // Parse the input IDL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.synonym())

            // Return the compiled synonym.
            fsm.getCompiledSynonym
        })

        syn
    }

    /**
     *
     * @param idl
     * @param mdl
     * @param origin
     * @return
     */
    private def antlr4Armature(
        idl: String,
        mdl: NCModel,
        origin: String
    ): (FiniteStateMachine, IDP) = {
        val lexer = new NCIdlLexer(CharStreams.fromString(idl, origin))
        val parser = new IDP(new CommonTokenStream(lexer))

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(idl, mdl, origin))
        parser.addErrorListener(new CompilerErrorListener(idl, mdl, origin))

        // State automata + it's parser.
        new FiniteStateMachine(origin, idl, mdl) → parser
    }

    /**
     * Compiles inline (supplied) fragments and/or intents. Note that fragments are accumulated in a static
     * map keyed by model ID. Only intents are returned, if any.
     *
     * @param idl Intent IDL to compile.
     * @param mdl Model IDL belongs to.
     * @param origin Optional source name.
     * @return
     */
    @throws[NCE]
    def compileIntents(
        idl: String,
        mdl: NCModel,
        origin: String
    ): Set[NCIdlIntent] = parseIntents(idl, mdl, origin)

    /**
      *
      * @param idl Synonym IDL to compile.
      * @param mdl Model IDL belongs to.*
      * @param origin Source name.
      * @return
      */
    @throws[NCE]
    def compileSynonym(
        idl: String,
        mdl: NCModel,
        origin: String,
    ): NCIdlSynonym = parseSynonym(idl, mdl, origin)
}
