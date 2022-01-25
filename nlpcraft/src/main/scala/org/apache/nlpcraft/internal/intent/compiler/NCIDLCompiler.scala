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
import org.antlr.v4.runtime.{ParserRuleContext => PRC}
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.antlr4.NCCompilerUtils
import org.apache.nlpcraft.internal.intent.compiler.antlr4.{NCIDLBaseListener, NCIDLLexer, NCIDLParser => IDP}
import org.apache.nlpcraft.internal.intent.*
import org.apache.nlpcraft.internal.intent.{NCIDLStackItem => Z}

import java.io.*
import java.net.*
import java.util.Optional
import java.util.regex.*
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object NCIDLCompiler extends LazyLogging:
    // Compiler caches.
    private val cache = new mutable.HashMap[String, Set[NCIDLIntent]]

    /**
      *
      * @param origin
      * @param idl
      * @param mdlCfg
      */
    class FiniteStateMachine(origin: String, idl: String, mdlCfg: NCModelConfig) extends NCIDLBaseListener with NCIDLCompilerBase 
        // Actual value for '*' as in min/max shortcut.
        final private val MINMAX_MAX = 100

        // Accumulators for parsed objects.
        private val intents = mutable.ArrayBuffer.empty[NCIDLIntent]

        // Synonym.
        private var alias: String = _

        // Fragment components.
        private var fragId: String = _
        private var fragMeta: Map[String, Any] = _

        // Intent components.
        private var intentId: String = _
        private var flowRegex: Option[String] = None
        private var intentMeta: ScalaMeta = _
        private var intentOpts: NCIDLIntentOptions = new NCIDLIntentOptions()

        // Accumulator for parsed terms.
        private val terms = mutable.ArrayBuffer.empty[NCIDLTerm]

        // Currently term.
        private val vars = mutable.HashMap.empty[String, NCIDLFunction]
        private var termId: String = _
        private var termConv: Boolean = _
        private var min = 1
        private var max = 1

        // Class & method reference.
        private var clsName: Option[String] = None
        private var mtdName: Option[String] = None
        private var flowClsName: Option[String] = None
        private var flowMtdName: Option[String] = None

        // List of instructions for the current expression.
        private val expr = mutable.Buffer.empty[SI]

        /**
          *
          * @return
          */
        def getCompiledIntents: Set[NCIDLIntent] = intents.toSet

        /**
          *
          * @param json
          * @param ctx
          * @return
          */
        private def json2Obj(json: String)(ctx: ParserRuleContext): Map[String, Object] =
            try U.jsonToScalaMap(json)
            catch case e: Exception => throw newSyntaxError(s"Invalid JSON (${e.getMessage})")(ctx)

        /*
         * Shared/common implementation.
         */
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
        override def exitAlias(ctx: IDP.AliasContext): Unit = alias = ctx.id().getText

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
                    case _ => throw newSyntaxError(s"Expecting boolean value for intent option: $k")(ctx)

            import NCIDLIntentOptions._

            for ((k, v) <- json)
                if k == JSON_ORDERED then opts.ordered = boolVal(k, v)
                else if k == JSON_UNUSED_FREE_WORDS then opts.ignoreUnusedFreeWords = boolVal(k, v)
                else if k == JSON_UNUSED_ENTS then opts.ignoreUnusedEntities = boolVal(k, v)
                else if k == JSON_ALLOW_STM_ONLY then opts.allowStmTokenOnly = boolVal(k, v)
                else
                    throw newSyntaxError(s"Unknown intent option: $k")(ctx)

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
            if !vars.contains(varName) then throw newSyntaxError(s"Undefined variable: @$varName")(ctx)
            val instr: SI = (tok: NCToken, stack: S, idlCtx: NCIDLContext) => stack.push(() => idlCtx.vars(varName)(tok, idlCtx))
            expr += instr

        override def exitVarDecl(ctx: IDP.VarDeclContext): Unit =
            val varName = ctx.id().getText
            if vars.contains(varName) then throw newSyntaxError(s"Duplicate variable: @$varName")(ctx)
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

                if min < 0 then throw newSyntaxError(s"Min value cannot be negative: $min")(ctx)
                if min > max then throw newSyntaxError(s"Min value '$min' cannot be greater than max value '$max'.")(ctx)
                if max > MINMAX_MAX then throw newSyntaxError(s"Max value '$max' cannot be greater than '$MINMAX_MAX'.")(ctx)

                setMinMax(min, max)
            // Errors should be caught during compilation phase.
            catch case _: NumberFormatException => assert(false)

        override def exitMtdRef(ctx: IDP.MtdRefContext): Unit =
            clsName = if (ctx.javaFqn() != null) Some(ctx.javaFqn().getText) else None
            mtdName = Some(ctx.id().getText)

        override def exitTermId(ctx: IDP.TermIdContext): Unit =
            termId = ctx.id().getText
            if terms.exists(t => t.id === termId) then throw newSyntaxError(s"Duplicate intent term ID: $termId")(ctx.id())

        override def exitFragId(ctx: IDP.FragIdContext): Unit =
            fragId = ctx.id().getText
            if NCIDLCompilerGlobal.getFragment(mdl.getId, fragId).isDefined then throw newSyntaxError(s"Duplicate fragment ID: $fragId")(ctx.id())

        override def exitFragRef(ctx: IDP.FragRefContext): Unit =
            val id = ctx.id().getText

            NCIDLCompilerGlobal.getFragment(mdl.getId, id) match
                case Some(frag) =>
                    val meta = if fragMeta == null then Map.empty[String, Any] else fragMeta
                    for (fragTerm <- frag.terms)
                        if terms.exists(t => t.id === fragTerm.id) then throw newSyntaxError(s"Duplicate term ID '${fragTerm.id.get}' in fragment '$id'.")(ctx.id())
                        else terms += fragTerm.cloneWithFragMeta(meta)
                case None => throw newSyntaxError(s"Unknown intent fragment ID: $id")(ctx.id())

            fragMeta = null

        override def exitFlowDecl(ctx: IDP.FlowDeclContext): Unit =
            if ctx.qstring() != null then
                flowClsName = None
                flowMtdName = None

                val regex = U.trimQuotes(ctx.qstring().getText)

                if regex != null && regex.length > 2 then flowRegex = if (regex.nonEmpty) Some(regex) else None
                if flowRegex.isDefined then // Pre-check.
                    try Pattern.compile(flowRegex.get)
                    catch case e: PatternSyntaxException => throw newSyntaxError(s"${e.getDescription} in intent flow regex '${e.getPattern}' near index ${e.getIndex}.")(ctx.qstring())
            else
                flowClsName = clsName
                flowMtdName = mtdName

            clsName = None
            mtdName = None

        override def exitTerm(ctx: IDP.TermContext): Unit =
            if min < 0 || min > max then throw newSyntaxError(s"Invalid intent term min quantifiers: $min (must be min >= 0 && min <= max).")(ctx.minMax())
            if max < 1 then throw newSyntaxError(s"Invalid intent term max quantifiers: $max (must be max >= 1).")(ctx.minMax())

            val pred: NCIDLFunction = if mtdName.isDefined then // User-code defined term.
                // Closure copies.
                val cls = clsName.orNull
                val mtd = mtdName.orNull

                (tok: NCToken, termCtx: NCIDLContext) => {
                    val javaCtx: NCTokenPredicateContext = new NCTokenPredicateContext:
                        override lazy val getRequest: NCRequest = termCtx.req
                        override lazy val getToken: NCToken = tok
                        override lazy val getIntentMeta: Optional[NCMetadata] =
                            if termCtx.intentMeta != null then Optional.of(NCMetadata.apply(termCtx.intentMeta.asJava))
                            else Optional.empty()

                    val mdl = tok.getModel
                    val mdlCls = if (cls == null) mdl.meta[String](MDL_META_MODEL_CLASS_KEY) else cls

                    try
                        val res = U.callMethod[NCTokenPredicateContext, NCTokenPredicateResult](
                            () => if cls == null then mdl else U.mkObject(cls),
                            mtd,
                            javaCtx
                        )

                        Z(res.getResult, res.getTokenUses)
                    catch case e: Exception => throw newRuntimeError(s"Failed to invoke custom intent term: $mdlCls.$mtd(...)", e)(ctx.mtdDecl())
                }
            // IDL term.
            else exprToFunction("Intent term", isBool)(ctx.expr())

            // Add term.
            terms += NCIDLTerm(
                ctx.getText,
                Option(termId),
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
            clsName = None
            mtdName = None

        /**
          *
          * @param subj
          * @param check
          * @param ctx
          * @return
          */
        private def exprToFunction(subj: String, check: Object => Boolean)(implicit ctx: PRC): NCIDLFunction =
            val code = mutable.Buffer.empty[SI]

            code ++= expr

            (tok: NCToken, termCtx: NCIDLContext) => {
                val stack = new S()

                // Execute all instructions.
                code.foreach(_ (tok, stack, termCtx))

                // Pop final result from stack.
                val x = stack.pop()()
                val v = x.value

                // Check final value's type.
                if !check(v) then throw newRuntimeError(s"$subj returned value of unexpected type '$v' in: ${ctx.getText}")

                Z(v, x.tokUse)
            }

        override def exitFrag(ctx: IDP.FragContext): Unit =
            NCIDLCompilerGlobal.addFragment(mdl.getId, NCIDLFragment(fragId, terms.toList))
            terms.clear()
            fragId = null

        /**
          *
          * @param intent
          * @param ctx
          */
        private def addIntent(intent: NCIDLIntent)(implicit ctx: ParserRuleContext): Unit =
            val intentId = intent.id
            if intents.exists(_.id == intentId) then throw newSyntaxError(s"Duplicate intent ID: $intentId")
            intents += intent

        override def exitImp(ctx: IDP.ImpContext): Unit =
            val x = U.trimQuotes(ctx.qstring().getText)

            if NCIDLCompilerGlobal.hasImport(x) then logger.warn(s"Ignoring already processed IDL import '$x' in: $origin")
            else
                NCIDLCompilerGlobal.addImport(x)

                var imports: Set[NCIDLIntent] = null
                val file = new File(x)

                // First, try absolute path.
                if file.exists() then
                    imports = NCIDLCompiler.compileIntents(
                        U.readFile(file).mkString("\n"),
                        mdl,
                        x
                    )

                // Second, try as a classloader resource.
                if imports == null then
                    val in = mdl.getClass.getClassLoader.getResourceAsStream(x)
                    if in != null then
                        imports = NCIDLCompiler.compileIntents(
                            U.readStream(in).mkString("\n"),
                            mdl,
                            x
                        )


                // Finally, try as URL resource.
                if imports == null then
                    try
                        imports = NCIDLCompiler.compileIntents(
                            U.readStream(new URL(x).openStream()).mkString("\n"),
                            mdl,
                            x
                        )
                    catch case _: Exception => throw newSyntaxError(s"Invalid or unknown import location: $x")(ctx.qstring())

                require(imports != null)
                imports.foreach(addIntent(_)(ctx.qstring()))

        override def exitIntent(ctx: IDP.IntentContext): Unit =
            addIntent(
                NCIDLIntent(
                    origin,
                    idl,
                    intentId,
                    intentOpts,
                    if (intentMeta == null) Map.empty else intentMeta,
                    flowRegex,
                    flowClsName,
                    flowMtdName,
                    terms.toList
                )
            )(ctx.intentId())

            flowClsName = None
            flowMtdName = None
            intentMeta = null
            intentOpts = new NCIDLIntentOptions()
            terms.clear()

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
        val hold = NCCompilerUtils.mkErrorHolder(idlLine, charPos)
        val aMsg = U.decapitalize(msg) match
            case s: String if s.last == '.' => s
            case s: String => s + '.'

        s"IDL $kind error in '$srcName' at line $line - $aMsg\n" +
            s"  |-- Model ID: ${mdl.getId}\n" +
            s"  |-- Model origin: ${mdl.getOrigin}\n" +
            s"  |-- Intent origin: $origin\n" +
            s"  |--<\n" +
            s"  |-- Line:  ${hold.origStr}\n" +
            s"  +-- Error: ${hold.ptrStr}"
    }

    /**
      * Custom error handler.
      *
      * @param dsl
      * @param mdlCfg
      * @param origin IDL origin.
      */
    class CompilerErrorListener(dsl: String, mdlCfg: NCModelConfig, origin: String) extends BaseErrorListener
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
            e: RecognitionException): Unit = {
            val aMsg = if (msg.contains("'\"") && msg.contains("\"'")) || msg.contains("''") then
                s"${if (msg.last == '.') msg.substring(0, msg.length - 1) else msg} - try removing quotes."
            else
                msg

            throw new NCE(mkSyntaxError(aMsg, recog.getInputStream.getSourceName, line, charPos - 1, dsl, origin, mdl))
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
    ): Set[NCIDLIntent] =
        require(idl != null)
        require(mdl != null)
        require(srcName != null)

        val x = idl.strip()
        val intents: Set[NCIDLIntent] = intentCache.getOrElseUpdate(x, {
            val (fsm, parser) = antlr4Armature(x, mdl, srcName)

            // Parse the input IDL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.idl())

            // Return the compiled intents.
            fsm.getCompiledIntents
        })

        intents

    /**
      *
      * @param idl
      * @param mdlCfg
      * @param origin
      * @return
      */
    private def antlr4Armature(idl: String, mdlCfg: NCModelConfig, origin: String): (FiniteStateMachine, IDP) =
        val lexer = new NCIDLLexer(CharStreams.fromString(idl, origin))
        val parser = new IDP(new CommonTokenStream(lexer))

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(idl, mdlCfg, origin))
        parser.addErrorListener(new CompilerErrorListener(idl, mdlCfg, origin))

        // State automata + it's parser.
        new FiniteStateMachine(origin, idlCfg, mdl) -> parser
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
    def compile(idl: String, mdlCfg: NCModelConfig, origin: String): Set[NCIDLIntent] =
        parseIntents(idl, mdlCfg, origin)
