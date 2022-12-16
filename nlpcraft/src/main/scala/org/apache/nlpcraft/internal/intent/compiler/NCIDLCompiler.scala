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

package org.apache.nlpcraft.internal.intent.compiler

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.ParserRuleContext as PRC
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.antlr4.NCCompilerUtils
import org.apache.nlpcraft.internal.intent.compiler.antlr4.{NCIDLBaseListener, NCIDLLexer, NCIDLParser as IDP}
import org.apache.nlpcraft.internal.intent.*
import org.apache.nlpcraft.internal.intent.NCIDLStackItem as Z
import org.apache.nlpcraft.internal.util.NCUtils

import java.io.*
import java.net.*
import java.util.Optional
import java.util.regex.*
import scala.collection.mutable

/**
  * IDL fragment.
  *
  * @param id ID of this fragment (must be unique within a model).
  * @param terms List of terms this fragment defines.
  */
case class NCIDLFragment(id: String, terms: List[NCIDLTerm]):
    require(id != null)
    require(terms.nonEmpty)

/**
  *
  * @param cfg
  */
class NCIDLCompiler(cfg: NCModelConfig) extends LazyLogging with mutable.Cloneable[NCIDLCompiler]:
    private val intents = mutable.HashMap.empty[String, Set[NCIDLIntent]]

    // Compiler caches.
    private val fragCache = mutable.HashMap.empty[String, NCIDLFragment]
    private val importCache = mutable.HashSet.empty[String]

    /**
      *
      * @param origin
      * @param idl
      * @param isMethodLevel
      */
    class FiniteStateMachine(origin: String, idl: String, isMethodLevel: Boolean) extends NCIDLBaseListener with NCIDLCodeGenerator:
        // Actual value for '*' as in min/max shortcut.
        final private val MINMAX_MAX = 100

        // Accumulators for parsed objects.
        private val intents = mutable.ArrayBuffer.empty[NCIDLIntent]

        // Fragment components.
        private var fragId: String = _
        private var fragMeta: Map[String, Any] = _

        // Intent components.
        private var intentId: String = _
        private var flowRegex: Option[String] = None
        private var intentMeta: Map[String, Object] = _
        private var intentOpts: NCIDLIntentOptions = new NCIDLIntentOptions()

        // Accumulator for parsed terms.
        private val terms = mutable.ArrayBuffer.empty[NCIDLTerm]

        // Currently term.
        private val vars = mutable.HashMap.empty[String, NCIDLFunction]
        private var termId: String = _
        private var termConv: Boolean = _
        private var min = 1
        private var max = 1

        // List of instructions for the current expression.
        private val expr = mutable.Buffer.empty[SI]

        /**
          *
          */
        def getCompiledIntents: Set[NCIDLIntent] = intents.toSet

        /**
          *
          * @param json
          * @param ctx
          */
        private def json2Obj(json: String)(ctx: ParserRuleContext): Map[String, Object] =
            try NCUtils.jsonToScalaMap(json)
            catch case e: Exception => SE(s"Invalid JSON (${e.getMessage})")(ctx)

        override def exitUnaryExpr(ctx: IDP.UnaryExprContext): Unit = expr += parseUnaryExpr(ctx.MINUS(), ctx.NOT())(ctx)
        override def exitMultDivModExpr(ctx: IDP.MultDivModExprContext): Unit = expr += parseMultDivModExpr(ctx.MULT(), ctx.MOD(), ctx.DIV())(ctx)
        override def exitPlusMinusExpr(ctx: IDP.PlusMinusExprContext): Unit = expr += parsePlusMinusExpr(ctx.PLUS(), ctx.MINUS())(ctx)
        override def exitCompExpr(ctx: IDP.CompExprContext): Unit = expr += parseCompExpr(ctx.LT(), ctx.GT(), ctx.LTEQ(), ctx.GTEQ())(ctx)
        override def exitAndOrExpr(ctx: IDP.AndOrExprContext): Unit = expr += parseAndOrExpr(ctx.AND, ctx.OR())(ctx)
        override def exitEqNeqExpr(ctx: IDP.EqNeqExprContext): Unit = expr += parseEqNeqExpr(ctx.EQ, ctx.NEQ())(ctx)
        override def exitAtom(ctx: IDP.AtomContext): Unit = expr += parseAtom(ctx.getText)(ctx)
        override def exitTermEq(ctx: IDP.TermEqContext): Unit = termConv = ctx.TILDA() != null
        override def exitFragMeta(ctx: IDP.FragMetaContext): Unit = fragMeta = json2Obj(ctx.jsonObj().getText)(ctx)
        override def exitMetaDecl(ctx: IDP.MetaDeclContext): Unit = intentMeta = json2Obj(ctx.jsonObj().getText)(ctx)
        override def exitOptDecl (ctx: IDP.OptDeclContext): Unit = intentOpts = convertToOptions(json2Obj(ctx.jsonObj().getText)(ctx))(ctx)
        override def exitIntentId(ctx: IDP.IntentIdContext): Unit =  intentId = ctx.id().getText

        override def exitCallExpr(ctx: IDP.CallExprContext): Unit =
            val fun =
                if ctx.FUN_NAME() != null then ctx.FUN_NAME().getText
                else "ent_id"

            expr += parseCallExpr(fun)(ctx)

        private def convertToOptions(json: Map[String, Object])(ctx: IDP.OptDeclContext): NCIDLIntentOptions =
            val opts = new NCIDLIntentOptions()
            def boolVal(k: String, v: Object): Boolean =
                v match
                    case b: java.lang.Boolean if b != null => b
                    case _ => SE(s"Expecting boolean value for intent option: $k")(ctx)

            import NCIDLIntentOptions.*

            for ((k, v) <- json)
                if k == JSON_ORDERED then opts.ordered = boolVal(k, v)
                else if k == JSON_UNUSED_FREE_WORDS then opts.ignoreUnusedFreeWords = boolVal(k, v)
                else if k == JSON_UNUSED_ENTS then opts.ignoreUnusedEntities = boolVal(k, v)
                else if k == JSON_ALLOW_STM_ONLY then opts.allowStmEntityOnly = boolVal(k, v)
                else SE(s"Unknown intent option: $k")(ctx)

            opts

        override def enterCallExpr(ctx: IDP.CallExprContext): Unit =
            expr += ((_, stack: NCIDLStack, _) => stack.push(stack.PLIST_MARKER))

        /**
          *
          * @param min
          * @param max
          */
        private def setMinMax(min: Int, max: Int): Unit =
            this.min = min
            this.max = max

        override def exitVarRef(ctx: IDP.VarRefContext): Unit =
            val varName = ctx.id().getText
            if !vars.contains(varName) then SE(s"Undefined variable: @$varName")(ctx)
            val instr: SI = (ent: NCIDLEntity, stack: S, idlCtx: NCIDLContext) => stack.push(() => idlCtx.vars(varName)(ent, idlCtx))
            expr += instr

        override def exitVarDecl(ctx: IDP.VarDeclContext): Unit =
            val varName = ctx.id().getText
            if vars.contains(varName) then SE(s"Duplicate variable: @$varName")(ctx)
            vars += varName -> exprToFunction("Variable declaration", _ => true)(ctx)
            expr.clear()

        override def exitMinMaxShortcut(ctx: IDP.MinMaxShortcutContext): Unit =
            if ctx.PLUS() != null then setMinMax(1, MINMAX_MAX)
            else if ctx.MULT() != null then setMinMax(0, MINMAX_MAX)
            else if ctx.QUESTION() != null then setMinMax(0, 1)
            else assert(false)

        override def exitMinMaxRange(ctx: IDP.MinMaxRangeContext): Unit =
            val minStr = ctx.getChild(1).getText.trim
            val maxStr = ctx.getChild(3).getText.trim

            try
                val min = java.lang.Integer.parseInt(minStr)
                val max = java.lang.Integer.parseInt(maxStr)

                if min < 0 then SE(s"Min value cannot be negative: $min")(ctx)
                if min > max then SE(s"Min value '$min' cannot be greater than max value '$max'.")(ctx)
                if max > MINMAX_MAX then SE(s"Max value '$max' cannot be greater than '$MINMAX_MAX'.")(ctx)

                setMinMax(min, max)
            // Errors should be caught during compilation phase.
            catch case _: NumberFormatException => assert(false)

        override def exitTermId(ctx: IDP.TermIdContext): Unit =
            termId = ctx.id().getText
            if terms.exists(t => t.id === termId) then SE(s"Duplicate intent term ID: $termId")(ctx.id())

        override def exitFragId(ctx: IDP.FragIdContext): Unit =
            fragId = ctx.id().getText

        override def exitFragRef(ctx: IDP.FragRefContext): Unit =
            val id = ctx.id().getText

            fragCache.get(id) match
                case Some(frag) =>
                    val meta = if fragMeta == null then Map.empty[String, Any] else fragMeta
                    for (fragTerm <- frag.terms)
                        if terms.exists(t => t.id === fragTerm.id) then SE(s"Duplicate term ID '${fragTerm.id.get}' in fragment '$id'.")(ctx.id())
                        else terms += fragTerm.cloneWithFragMeta(meta)
                case None => SE(s"Unknown intent fragment ID: $id")(ctx.id())

            fragMeta = null

        override def exitFlowDecl(ctx: IDP.FlowDeclContext): Unit =
            val regex = NCUtils.trimQuotes(ctx.qstring().getText)

            if regex != null && regex.length > 2 then flowRegex = Option.when(regex.nonEmpty)(regex)
            if flowRegex.isDefined then // Pre-check.
                try Pattern.compile(flowRegex.get)
                catch case e: PatternSyntaxException => SE(s"${e.getDescription} in intent flow regex '${e.getPattern}' near index ${e.getIndex}.")(ctx.qstring())

        override def exitTerm(ctx: IDP.TermContext): Unit =
            if min < 0 || min > max then SE(s"Invalid intent term min quantifiers: $min (must be min >= 0 && min <= max).")(ctx.minMax())
            if max < 1 then SE(s"Invalid intent term max quantifiers: $max (must be max >= 1).")(ctx.minMax())

            val pred: NCIDLFunction = exprToFunction("Intent term", isBool)(ctx.expr())

            // Add term.
            terms += NCIDLTerm(
                ctx.getText,
                termId.?,
                vars.toMap,
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

        /**
          *
          * @param subj
          * @param check
          * @param ctx
          */
        private def exprToFunction(subj: String, check: Object => Boolean)(implicit ctx: PRC): NCIDLFunction =
            val code = mutable.Buffer.empty[SI]

            code ++= expr

            (ent: NCIDLEntity, termCtx: NCIDLContext) => {
                val stack = new S()

                // Execute all instructions.
                code.foreach(_ (ent, stack, termCtx))

                // Pop final result from stack.
                val x = stack.pop()()
                val v = x.value

                // Check final value's type.
                if !check(v) then RE(s"$subj returned value of unexpected type '$v' in: ${ctx.getText}")

                Z(v, x.entUse)
            }

        override def exitFrag(ctx: IDP.FragContext): Unit =
            val frag = NCIDLFragment(fragId, terms.toList)

            fragCache.get(frag.id) match
                case Some(_) =>
                    if isMethodLevel then
                        logger.warn(s"Fragment '${frag.id}' was overridden just for the scope of: '${this.origin}'")
                    else
                        logger.warn(s"Fragment '${frag.id}' was overridden in origin: '${this.origin}'")
                case None => // No-op.

            fragCache += frag.id -> frag

            terms.clear()
            fragId = null

        /**
          *
          * @param intent
          * @param ctx
          */
        private def addIntent(intent: NCIDLIntent)(implicit ctx: ParserRuleContext): Unit =
            val intentId = intent.id
            if intents.exists(_.id == intentId) then SE(s"Duplicate intent ID: $intentId")
            intents += intent

        override def exitIntent(ctx: IDP.IntentContext): Unit =
            addIntent(
                NCIDLIntent(
                    origin,
                    idl,
                    intentId,
                    intentOpts,
                    if intentMeta == null then Map.empty else intentMeta,
                    flowRegex,
                    terms.toList
                )
            )(ctx.intentId())

            intentMeta = null
            intentOpts = new NCIDLIntentOptions()
            terms.clear()

        override def exitImprt(ctx: IDP.ImprtContext): Unit =
                val x = NCUtils.trimQuotes(ctx.qstring().getText)

                if importCache.contains(x) then logger.warn(s"Ignoring already processed IDL import '$x' in: $origin")
                else
                   importCache += x

                    var imports: Set[NCIDLIntent] = null
                    val file = new File(x)

                    // First, try absolute path.
                    if file.exists() then
                        val idl = NCUtils.readLines(file).mkString("\n")
                        imports = compile(idl, x)

                    // Second, try as a classloader resource.
                    if imports == null then
                        val in = cfg.getClass.getClassLoader.getResourceAsStream(x)
                        if in != null then
                            val idl = NCUtils.readLines(in).mkString("\n")
                            imports = compile(idl, x)

                    // Finally, try as URL resource.
                    if imports == null then
                        try
                            val idl = NCUtils.readLines(new URL(x).openStream()).mkString("\n")
                            imports = compile(idl,  x)
                        catch case _: Exception => throw newRuntimeError(s"Invalid or unknown import location: $x")(ctx.qstring())

                    require(imports != null)
                    imports.foreach(addIntent(_)(ctx.qstring()))

        override def syntaxError(errMsg: String, srcName: String, line: Int, pos: Int): NCException =
            throw new NCException(mkSyntaxError(errMsg, srcName, line, pos, idl, origin, cfg))

        override def runtimeError(errMsg: String, srcName: String, line: Int, pos: Int, cause: Exception = null): NCException =
            throw new NCException(mkRuntimeError(errMsg, srcName, line, pos, idl, origin, cfg), cause)

    /**
      *
      * @param msg
      * @param srcName
      * @param line
      * @param charPos
      * @param idl
      * @param origin IDL origin.
      * @param mdlCfg
      */
    private def mkSyntaxError(
        msg: String,
        srcName: String,
        line: Int, // 1, 2, ...
        charPos: Int, // 0, 1, 2, ...
        idl: String,
        origin: String,
        mdlCfg: NCModelConfig): String = mkError("syntax", msg, srcName, line, charPos, idl, origin, mdlCfg)

    /**
      *
      * @param msg
      * @param srcName
      * @param line
      * @param charPos
      * @param idl
      * @param origin IDL origin.
      * @param mdlCfg
      */
    private def mkRuntimeError(
        msg: String,
        srcName: String,
        line: Int, // 1, 2, ...
        charPos: Int, // 0, 1, 2, ...
        idl: String,
        origin: String,
        mdlCfg: NCModelConfig): String = mkError("runtime", msg, srcName, line, charPos, idl, origin, mdlCfg)

    /**
      *
      * @param kind
      * @param msg
      * @param srcName
      * @param line
      * @param charPos
      * @param idl
      * @param origin IDL origin.
      * @param mdlCfg
      */
    private def mkError(
        kind: String,
        msg: String,
        srcName: String,
        line: Int,
        charPos: Int,
        idl: String,
        origin: String,
        mdlCfg: NCModelConfig): String = {
        val idlLine = idl.split("\n")(line - 1)
        val hold = NCCompilerUtils.mkErrorHolder(idlLine, charPos)
        val aMsg = NCUtils.decapitalize(msg) match
            case s: String if s.last == '.' => s
            case s: String => s"$s."

        s"""IDL $kind error in '$srcName' at line $line - $aMsg
          |-- Model ID: ${mdlCfg.getId}
          |-- Model origin: ${mdlCfg.getOrigin}
          |-- Intent origin: $origin
          |--
          |-- Line:  ${hold.origStr}
          +-- Error: ${hold.ptrStr}"""
    }

    /**
      * Custom error handler.
      *
      * @param dsl
      * @param mdlCfg
      * @param origin IDL origin.
      */
    class CompilerErrorListener(dsl: String, mdlCfg: NCModelConfig, origin: String) extends BaseErrorListener:
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
            val aMsg =
                if (msg.contains("'\"") && msg.contains("\"'")) || msg.contains("''") then
                    s"${if (msg.last == '.') msg.substring(0, msg.length - 1) else msg} - try removing quotes."
                else
                    msg

            throw new NCException(mkSyntaxError(aMsg, recog.getInputStream.getSourceName, line, charPos - 1, dsl, origin, mdlCfg))

    /**
      *
      * @param idl
      * @param origin
      * @param isMethodLevel
      */
    private def antlr4Armature(idl: String, origin: String, isMethodLevel: Boolean): (FiniteStateMachine, IDP) =
        val lexer = new NCIDLLexer(CharStreams.fromString(idl, origin))
        val parser = new IDP(new CommonTokenStream(lexer))

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(idl, cfg, origin))
        parser.addErrorListener(new CompilerErrorListener(idl, cfg, origin))

        // State automata + it's parser.
        new FiniteStateMachine(origin, idl, isMethodLevel) -> parser

    /**
      * Compiles inline (supplied) fragments and/or intents. Note that fragments are accumulated in a static
      * map keyed by model ID. Only intents are returned, if any.
      *
      * @param idl Intent IDL to compile.
      * @param origin Optional source name.
      * @param isMethodLevel Flag.
      */
    @throws[NCException]
    def compile(idl: String, origin: String, isMethodLevel: Boolean = false): Set[NCIDLIntent] =
        require(idl != null)
        require(origin != null)

        val x = idl.strip()

        intents.getOrElseUpdate(x, {
            val (fsm, parser) = antlr4Armature(x, origin, isMethodLevel)

            // Parse the input IDL and walk the built AST.
            (new ParseTreeWalker).walk(fsm, parser.idl())

            // Return the compiled intents.
            fsm.getCompiledIntents
        })

    def clone(cp: NCIDLCompiler): NCIDLCompiler =
        val cp = new NCIDLCompiler(cfg)

        cp.intents ++= cp.intents.clone()

        cp.importCache ++= this.importCache.clone()
        cp.fragCache ++= this.fragCache.clone()

        cp

    override def clone(): NCIDLCompiler = clone(this)