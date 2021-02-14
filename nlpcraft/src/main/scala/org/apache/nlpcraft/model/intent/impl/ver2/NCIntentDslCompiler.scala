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
import org.apache.commons.lang3.StringUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.intent.impl.antlr4._
import org.apache.nlpcraft.model.intent.utils.ver2._

import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import java.lang.{Double ⇒ JDouble, IllegalArgumentException ⇒ IAE, Long ⇒ JLong}
import java.util
import java.util.{List ⇒ JList, Map ⇒ JMap}
import scala.language.implicitConversions

object NCIntentDslCompiler extends LazyLogging {
    // Compiler cache.
    private val cache = new mutable.HashMap[String, NCDslIntent]

    private var mdlId: String = _

    /**
     *
     */
    class FiniteStateMachine(dsl: String) extends NCIntentDslBaseListener {
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

        type StackType = mutable.ArrayStack[NCDslTermRetVal]
        type Instr = (NCToken, StackType,  NCDslTermContext) ⇒ Unit

        // Term's code, i.e. list of instructions.
        private var termCode = mutable.Buffer.empty[Instr]

        private def isJLong(v: AnyRef): Boolean = v.isInstanceOf[JLong]
        private def isJDouble(v: AnyRef): Boolean = v.isInstanceOf[JDouble]
        private def isString(v: AnyRef): Boolean = v.isInstanceOf[String]
        private def asJLong(v: AnyRef): JLong = v.asInstanceOf[JLong]
        private def asJDouble(v: AnyRef): JDouble = v.asInstanceOf[JDouble]
        private def asString(v: AnyRef): String = v.asInstanceOf[String]
        private def asJList(v: AnyRef): JList[AnyRef] = v.asInstanceOf[JList[AnyRef]]
        private def isJList(v: AnyRef): Boolean = v.isInstanceOf[JList[AnyRef]]
        private def asJMap(v: AnyRef): JMap[AnyRef, AnyRef] = v.asInstanceOf[JMap[AnyRef, AnyRef]]
        private def isJMap(v: AnyRef): Boolean = v.isInstanceOf[JMap[AnyRef, AnyRef]]

        private def pushAny(any: AnyRef, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(any, usedTok))
        private def pushLong(any: Long, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(Long.box(any), usedTok))
        private def pushDouble(any: Double, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(Double.box(any), usedTok))
        private def pushBoolean(any: Boolean, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(Boolean.box(any), usedTok))

        private def errBinaryOp(op: String, val1: AnyRef, val2: AnyRef): Unit =
            throw new IAE(s"Unexpected '$op' operation for values: $val1, $val2")
        private def errUnknownFun(fun: String): Unit =
            throw new IAE(s"Unknown built-in function: $fun")
        private def errParamNum(fun: String): Unit =
            throw new IAE(s"Invalid number of parameters for built-in function: $fun")
        private def errParamType(fun: String): Unit =
            throw new IAE(s"Invalid parameter type for built-in function: $fun")


        /**
         *
         * @param min
         * @param max
         */
        private def setMinMax(min: Int, max: Int): Unit = {
            this.min = min
            this.max = max
        }

        override def exitMinMaxShortcut(ctx: NCIntentDslParser.MinMaxShortcutContext): Unit = {
            if (ctx.PLUS() != null)
                setMinMax(1, Integer.MAX_VALUE)
            else if (ctx.MULT() != null)
                setMinMax(0, Integer.MAX_VALUE)
            else if (ctx.QUESTION() != null)
                setMinMax(0, 1)
            else
                assert(false)
        }

        /**
         *
         * @param stack
         * @return
         */
        private def pop2()(implicit stack: StackType): (AnyRef, AnyRef, Boolean) = {
            // Stack pop in reverse order of push...
            val NCDslTermRetVal(val2, f1) = stack.pop()
            val NCDslTermRetVal(val1, f2) = stack.pop()

            (val1, val2, f1 || f2)
        }

        override def exitListExpr(ctx: NCIntentDslParser.ListExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (val1, val2, usedTok) = pop2()

                if (isJList(val1) && isJList(val2)) {
                    val lst1 = asJList(val1)
                    val lst2 = asJList(val2)

                    lst1.addAll(lst2)

                    pushAny(lst1, usedTok)

                }
                else if (isJList(val1)) {
                    val lst = asJList(val1)

                    lst.add(val2)

                    pushAny(lst, usedTok)
                }
                else if (isJList(val2)) {
                    val lst = asJList(val2)

                    lst.add(val1)

                    pushAny(lst, usedTok)

                }
                else {
                    pushAny(util.Arrays.asList(val1, val2), usedTok)
                }
            })
        }


        override def exitUnaryExpr(ctx: NCIntentDslParser.UnaryExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {

            })
        }

        override def exitMultExpr(ctx: NCIntentDslParser.MultExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (val1, val2, usedTok) = pop2()

                if (ctx.MULT() != null) {
                    if (isJLong(val1) && isJLong(val2))
                        pushLong(asJLong(val1).longValue() * asJLong(val2).longValue(), usedTok)
                    else if (isJLong(val1) && isJDouble(val2))
                        pushDouble(asJLong(val1).longValue() * asJDouble(val2).doubleValue(), usedTok)
                    else if (isJDouble(val1) && isJLong(val2))
                        pushDouble(asJDouble(val1).doubleValue() * asJLong(val2).longValue(), usedTok)
                    else if (isJDouble(val1) && isJDouble(val2))
                        pushDouble(asJDouble(val1).doubleValue() * asJDouble(val2).doubleValue(), usedTok)
                    else
                        errBinaryOp("*", val1, val2)
                }
                else if (ctx.MOD() != null) {
                    if (isJLong(val1) && isJLong(val2))
                        pushLong(asJLong(val1).longValue() % asJLong(val2).longValue(), usedTok)
                    else
                        errBinaryOp("%", val1, val2)
                }
                else {
                    assert(ctx.DIV() != null)

                    if (isJLong(val1) && isJLong(val2))
                        pushLong(asJLong(val1).longValue() / asJLong(val2).longValue(), usedTok)
                    else if (isJLong(val1) && isJDouble(val2))
                        pushDouble(asJLong(val1).longValue() / asJDouble(val2).doubleValue(), usedTok)
                    else if (isJDouble(val1) && isJLong(val2))
                        pushDouble(asJDouble(val1).doubleValue() / asJLong(val2).longValue(), usedTok)
                    else if (isJDouble(val1) && isJDouble(val2))
                        pushDouble(asJDouble(val1).doubleValue() / asJDouble(val2).doubleValue(), usedTok)
                    else
                        errBinaryOp("/", val1, val2)
                }
            })
        }

        override def exitPlusExpr(ctx: NCIntentDslParser.PlusExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (val1, val2, usedTok) = pop2()

                if (ctx.PLUS() != null) {
                    if (isString(val1) && isString(val2))
                        pushAny(asString(val1) + asString(val2), usedTok)
                    else if (isJLong(val1) && isJLong(val2))
                        pushLong(asJLong(val1).longValue() + asJLong(val2).longValue(), usedTok)
                    else if (isJLong(val1) && isJDouble(val2))
                        pushDouble(asJLong(val1).longValue() + asJDouble(val2).doubleValue(), usedTok)
                    else if (isJDouble(val1) && isJLong(val2))
                        pushDouble(asJDouble(val1).doubleValue() + asJLong(val2).longValue(), usedTok)
                    else if (isJDouble(val1) && isJDouble(val2))
                        pushDouble(asJDouble(val1).doubleValue() + asJDouble(val2).doubleValue(), usedTok)
                    else
                        errBinaryOp("+", val1, val2)
                }
                else {
                    assert(ctx.MINUS() != null)

                    if (isJLong(val1) && isJLong(val2))
                        pushLong(asJLong(val1).longValue() - asJLong(val2).longValue(), usedTok)
                    else if (isJLong(val1) && isJDouble(val2))
                        pushDouble(asJLong(val1).longValue() - asJDouble(val2).doubleValue(), usedTok)
                    else if (isJDouble(val1) && isJLong(val2))
                        pushDouble(asJDouble(val1).doubleValue() - asJLong(val2).longValue(), usedTok)
                    else if (isJDouble(val1) && isJDouble(val2))
                        pushDouble(asJDouble(val1).doubleValue() - asJDouble(val2).doubleValue(), usedTok)
                    else
                        errBinaryOp("-", val1, val2)
                }
            })
        }

        override def exitCompExpr(ctx: NCIntentDslParser.CompExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (val1, val2, usedTok) = pop2()

                if (ctx.LT() != null) {

                }
                else if (ctx.GT() != null) {

                }
                else if (ctx.LTEQ() != null) {

                }
                else {
                    assert(ctx.GT() != null)
                }
            })
        }

        override def exitLogExpr(ctx: NCIntentDslParser.LogExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (val1, val2, usedTok) = pop2()

                if (ctx.AND() != null) {

                }
                else {
                    assert(ctx.OR() != null)
                }
            })
        }

        override def exitEqExpr(ctx: NCIntentDslParser.EqExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (val1, val2, usedTok) = pop2()

                if (ctx.EQ() != null) {

                }
                else {
                    assert(ctx.NEQ() != null)
                }
            })
        }

        override def exitCallExpr(ctx: NCIntentDslParser.CallExprContext): Unit = {
            val fun = ctx.ID().getText

            termCode += ((tok: NCToken, stack: StackType, ctx: NCDslTermContext) ⇒ {
                implicit val s = stack

                val NCDslTermRetVal(param, usedTok) = if (stack.nonEmpty) stack.pop else (null, false)

                def check1String(): Unit = if (param == null) errParamNum(fun) else if (!isString(param)) errParamType(fun)
                def check1Long(): Unit = if (param == null) errParamNum(fun) else if (!isJLong(param)) errParamType(fun)
                def check1Double(): Unit = if (param == null) errParamNum(fun) else if (!isJDouble(param)) errParamType(fun)

                def doTrim(): String = { check1String(); asString(param).strip() }
                def doUppercase(): String = { check1String(); asString(param).toUpperCase() }
                def doLowercase(): String = { check1String(); asString(param).toLowerCase() }
                def doIsAlpha(): Boolean = { check1String(); StringUtils.isAlpha(asString(param)) }
                def doIsNum(): Boolean = { check1String(); StringUtils.isNumeric(asString(param)) }
                def doIsAlphaNum(): Boolean = { check1String(); StringUtils.isAlphanumeric(asString(param)) }
                def doIsWhitespace(): Boolean = { check1String(); StringUtils.isWhitespace(asString(param)) }
                def doIsAlphaSpace(): Boolean = { check1String(); StringUtils.isAlphaSpace(asString(param)) }
                def doIsAlphaNumSpace(): Boolean = { check1String(); StringUtils.isAlphanumericSpace(asString(param)) }
                def doIsNumSpace(): Boolean = { check1String(); StringUtils.isNumericSpace(asString(param)) }

                fun match {
                    // Metadata access.
                    case "token_meta" ⇒
                    case "model_meta" ⇒
                    case "intent_meta" ⇒
                    case "data_meta" ⇒
                    case "user_meta" ⇒
                    case "company_meta" ⇒

                    // Converts JSON to map.
                    case "json" ⇒

                    // Inline if-statement.
                    case "if" ⇒

                    // Token functions.
                    case "id" ⇒ pushAny(tok.getId, true)
                    case "ancestors" ⇒ pushAny(tok.getAncestors, true)
                    case "parent" ⇒ pushAny(tok.getParentId, true)
                    case "groups" ⇒ pushAny(tok.getGroups, true)
                    case "value" ⇒ pushAny(tok.getValue, true)
                    case "aliases" ⇒ pushAny(tok.getAliases, true)
                    case "start_idx" ⇒ pushLong(tok.getStartCharIndex, true)
                    case "end_idx" ⇒ pushLong(tok.getEndCharIndex, true)

                    // String functions.
                    case "trim" ⇒ pushAny(doTrim(), usedTok)
                    case "strip" ⇒ pushAny(doTrim(), usedTok)
                    case "uppercase" ⇒ pushAny(doUppercase(), usedTok)
                    case "lowercase" ⇒ pushAny(doLowercase(), usedTok)
                    case "is_alpha" ⇒ pushBoolean(doIsAlpha(), usedTok)
                    case "is_alphanum" ⇒ pushBoolean(doIsAlphaNum(), usedTok)
                    case "is_whitespace" ⇒ pushBoolean(doIsWhitespace(), usedTok)
                    case "is_numeric" ⇒ pushBoolean(doIsNum(), usedTok)
                    case "is_numeric_space" ⇒ pushBoolean(doIsNumSpace(), usedTok)
                    case "is_alpha_space" ⇒ pushBoolean(doIsAlphaSpace(), usedTok)
                    case "is_alphanum_space" ⇒ pushBoolean(doIsAlphaNumSpace(), usedTok)
                    case "substring" ⇒
                    case "index" ⇒
                    case "regex" ⇒
                    case "soundex" ⇒
                    case "split" ⇒
                    case "replace" ⇒

                    // Math functions.
                    case "abs" ⇒
                    case "ceil" ⇒
                    case "floor" ⇒
                    case "rint" ⇒
                    case "round" ⇒
                    case "signum" ⇒
                    case "sqrt" ⇒
                    case "pi" ⇒
                    case "acos" ⇒
                    case "asin" ⇒
                    case "atan" ⇒
                    case "atn2" ⇒
                    case "cos" ⇒
                    case "cot" ⇒
                    case "degrees" ⇒
                    case "exp" ⇒
                    case "log" ⇒
                    case "log10" ⇒
                    case "power" ⇒
                    case "radians" ⇒
                    case "rand" ⇒
                    case "sin" ⇒
                    case "square" ⇒
                    case "tan" ⇒

                    // Collection, statistical (incl. string) functions.
                    case "avg" ⇒
                    case "max" ⇒
                    case "min" ⇒
                    case "stdev" ⇒
                    case "sum" ⇒
                    case "get" ⇒
                    case "index" ⇒
                    case "contains" ⇒
                    case "tail" ⇒
                    case "add" ⇒
                    case "remove" ⇒
                    case "first" ⇒
                    case "last" ⇒
                    case "keys" ⇒
                    case "values" ⇒
                    case "length" ⇒
                    case "count" ⇒
                    case "take" ⇒
                    case "drop" ⇒
                    case "size" ⇒
                    case "reverse" ⇒
                    case "is_empty" ⇒
                    case "non_empty" ⇒
                    case "to_string" ⇒

                    // Date-time functions.
                    case "year" ⇒
                    case "month" ⇒
                    case "day_of_month" ⇒
                    case "day_of_week" ⇒
                    case "hour" ⇒
                    case "min" ⇒
                    case "sec" ⇒
                    case "week" ⇒
                    case "quarter" ⇒
                    case "msec" ⇒
                    case "now" ⇒

                    case _ ⇒ errUnknownFun(fun)
                }
            })
        }

        override def exitClsNer(ctx: NCIntentDslParser.ClsNerContext): Unit = {
            if (ctx.javaFqn() != null)
                termClsName = ctx.javaFqn().getText

            termMtdName = ctx.ID().getText
        }

        override def exitTermId(ctx: NCIntentDslParser.TermIdContext): Unit = {
            termId = ctx.ID().getText
        }

        override def exitTermEq(ctx: NCIntentDslParser.TermEqContext): Unit = {
            termConv = ctx.TILDA() != null
        }

        override def exitFlowDecl(ctx: NCIntentDslParser.FlowDeclContext): Unit = {
            val qRegex = ctx.qstring().getText

            if (qRegex != null && qRegex.length > 2) {
                val regex = qRegex.substring(1, qRegex.length - 1).strip // Remove quotes.

                flowRegex = if (regex.nonEmpty) Some(regex) else None
            }
        }

        override def exitIntentId(ctx: NCIntentDslParser.IntentIdContext): Unit = {
            id = ctx.ID().getText
        }

        override def exitMetaDecl(ctx: NCIntentDslParser.MetaDeclContext): Unit = {
            meta = U.jsonToObject(ctx.jsonObj().getText, classOf[HashMap[String, Any]])
        }

        override def exitOrderedDecl(ctx: NCIntentDslParser.OrderedDeclContext): Unit = {
            ordered = ctx.BOOL().getText == "true"
        }

        override def exitAtom(ctx: NCIntentDslParser.AtomContext): Unit = {
            val s = ctx.getText

            val atom =
                if (s == "null") null // Try 'null'.
                else if (s == "true") Boolean.box(true) // Try 'boolean'.
                else if (s == "false") Boolean.box(false) // Try 'boolean'.
                // Only numeric or string values below...
                else {
                    // Strip '_' from numeric values.
                    val num = s.replaceAll("_", "")

                    try
                        Long.box(JLong.parseLong(num)) // Try 'long'.
                    catch {
                        case _: NumberFormatException ⇒
                            try
                                Double.box(JDouble.parseDouble(num)) // Try 'double'.
                            catch {
                                case _: NumberFormatException ⇒ s // String by default (incl. quotes).
                            }
                    }
                }

            termCode += ((_, stack, _) ⇒ pushAny(atom, false)(stack))
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
        mdlId: String,
        reqMeta: ScalaMeta,
        usrMeta: ScalaMeta,
        compMeta: ScalaMeta
    ): NCDslIntent = {
        require(dsl != null)

        val src = dsl.strip()

        this.mdlId = mdlId

        val intent: NCDslIntent = cache.getOrElseUpdate(src, {
            // ANTLR4 armature.
            val lexer = new NCIntentDslLexer(CharStreams.fromString(src))
            val tokens = new CommonTokenStream(lexer)
            val parser = new NCIntentDslParser(tokens)

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
