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
import java.time.LocalDate
import java.util.{Collections, ArrayList ⇒ JArrayList, HashMap ⇒ JHashMap}
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
        private def isBoolean(v: AnyRef): Boolean = v.isInstanceOf[Boolean]
        private def asJLong(v: AnyRef): Long = v.asInstanceOf[JLong].longValue()
        private def asJDouble(v: AnyRef): Double = v.asInstanceOf[JDouble].doubleValue()
        private def asString(v: AnyRef): String = v.asInstanceOf[String]
        private def asBoolean(v: AnyRef): Boolean = v.asInstanceOf[Boolean]

        private def pushAny(any: AnyRef, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(any, usedTok))
        private def pushLong(any: Long, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(Long.box(any), usedTok))
        private def pushDouble(any: Double, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(Double.box(any), usedTok))
        private def pushBoolean(any: Boolean, usedTok: Boolean)(implicit stack: StackType): Unit =
            stack.push(NCDslTermRetVal(Boolean.box(any), usedTok))

        private def errUnaryOp(op: String, v: AnyRef): IAE =
            new IAE(s"Unexpected '$op' DSL operation for value: $v")
        private def errBinaryOp(op: String, v1: AnyRef, v2: AnyRef): IAE =
            new IAE(s"Unexpected '$op' DSL operation for values: $v1, $v2")
        private def errUnknownFun(fun: String): IAE =
            new IAE(s"Unknown DSL function: $fun")
        private def errParamNum(fun: String): IAE =
            new IAE(s"Invalid number of parameters for DSL function: $fun")
        private def errParamType(fun: String, param: AnyRef): IAE =
            new IAE(s"Invalid type of parameter for DSL function '$fun': $param")

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
        private def pop2()(implicit stack: StackType): (AnyRef, AnyRef, Boolean, Boolean) = {
            // Stack pops in reverse order of push...
            val NCDslTermRetVal(val2, f1) = stack.pop()
            val NCDslTermRetVal(val1, f2) = stack.pop()

            (val1, val2, f1, f2)
        }

        /**
         *
         * @param stack
         * @return
         */
        private def pop1()(implicit stack: StackType): (AnyRef, Boolean) = {
            val NCDslTermRetVal(v, f) = stack.pop()

            (v, f)
        }

        override def exitUnaryExpr(ctx: NCIntentDslParser.UnaryExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.nonEmpty)

                implicit val s = stack

                val (v, usedTok) = pop1()

                if (ctx.MINUS() != null) {
                    if (isJDouble(v))
                        pushDouble(-asJDouble(v), usedTok)
                    else if (isJLong(v))
                        pushLong(-asJLong(v), usedTok)
                    else
                        throw errUnaryOp("-", v)
                }
                else {
                    assert(ctx.NOT() != null)

                    if (isBoolean(v))
                        pushBoolean(!asBoolean(v), usedTok)
                    else
                        throw errUnaryOp("!", v)
                }
            })
        }

        override def exitMultExpr(ctx: NCIntentDslParser.MultExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (v1, v2, f1, f2) = pop2()
                val usedTok = f1 || f2

                if (ctx.MULT() != null) {
                    if (isJLong(v1) && isJLong(v2))
                        pushLong(asJLong(v1) * asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushDouble(asJLong(v1) * asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushDouble(asJDouble(v1) * asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushDouble(asJDouble(v1) * asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp("*", v1, v2)
                }
                else if (ctx.MOD() != null) {
                    if (isJLong(v1) && isJLong(v2))
                        pushLong(asJLong(v1) % asJLong(v2), usedTok)
                    else
                        throw errBinaryOp("%", v1, v2)
                }
                else {
                    assert(ctx.DIV() != null)

                    if (isJLong(v1) && isJLong(v2))
                        pushLong(asJLong(v1) / asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushDouble(asJLong(v1) / asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushDouble(asJDouble(v1) / asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushDouble(asJDouble(v1) / asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp("/", v1, v2)
                }
            })
        }

        override def exitPlusExpr(ctx: NCIntentDslParser.PlusExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                require(stack.size >= 2)

                implicit val s = stack

                val (v1, v2, f1, f2) = pop2()
                val usedTok = f1 || f2

                if (ctx.PLUS != null) {
                    if (isString(v1) && isString(v2))
                        pushAny(asString(v1) + asString(v2), usedTok)
                    else if (isJLong(v1) && isJLong(v2))
                        pushLong(asJLong(v1) + asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushDouble(asJLong(v1) + asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushDouble(asJDouble(v1) + asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushDouble(asJDouble(v1) + asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp("+", v1, v2)
                }
                else {
                    assert(ctx.MINUS != null)

                    if (isJLong(v1) && isJLong(v2))
                        pushLong(asJLong(v1) - asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushDouble(asJLong(v1) - asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushDouble(asJDouble(v1) - asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushDouble(asJDouble(v1) - asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp("-", v1, v2)
                }
            })
        }

        override def exitCompExpr(ctx: NCIntentDslParser.CompExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                implicit val s = stack

                require(stack.size >= 2)

                val (v1, v2, f1, f2) = pop2()
                val usedTok = f1 || f2

                if (ctx.LT() != null) {
                    if (isJLong(v1) && isJLong(v2))
                        pushBoolean(asJLong(v1) < asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushBoolean(asJLong(v1) < asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushBoolean(asJDouble(v1) < asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushBoolean(asJDouble(v1) < asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp("<", v1, v2)
                }
                else if (ctx.GT() != null) {
                    if (isJLong(v1) && isJLong(v2))
                        pushBoolean(asJLong(v1) > asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushBoolean(asJLong(v1) > asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushBoolean(asJDouble(v1) > asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushBoolean(asJDouble(v1) > asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp(">", v1, v2)
                }
                else if (ctx.LTEQ() != null) {
                    if (isJLong(v1) && isJLong(v2))
                        pushBoolean(asJLong(v1) <= asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushBoolean(asJLong(v1) <= asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushBoolean(asJDouble(v1) <= asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushBoolean(asJDouble(v1) <= asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp("<=", v1, v2)
                }
                else {
                    assert(ctx.GTEQ() != null)

                    if (isJLong(v1) && isJLong(v2))
                        pushBoolean(asJLong(v1) >= asJLong(v2), usedTok)
                    else if (isJLong(v1) && isJDouble(v2))
                        pushBoolean(asJLong(v1) >= asJDouble(v2), usedTok)
                    else if (isJDouble(v1) && isJLong(v2))
                        pushBoolean(asJDouble(v1) >= asJLong(v2), usedTok)
                    else if (isJDouble(v1) && isJDouble(v2))
                        pushBoolean(asJDouble(v1) >= asJDouble(v2), usedTok)
                    else
                        throw errBinaryOp(">=", v1, v2)
                }
            })
        }

        override def exitLogExpr(ctx: NCIntentDslParser.LogExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                implicit val s = stack

                require(stack.size >= 2)

                val (v1, v2, f1, f2) = pop2()

                if (!isBoolean(v1) || !isBoolean(v2))
                    throw errBinaryOp(if (ctx.AND() != null) "&&" else "||", v1, v2)

                if (ctx.AND() != null)
                    pushBoolean(asBoolean(v1) && asBoolean(v2), f1 || f2) // Note logical OR for used token flag.
                else {
                    assert(ctx.OR() != null)

                    pushBoolean(asBoolean(v1) || asBoolean(v2), f1 && f2) // Note local AND for used token flag.
                }
            })
        }

        override def exitEqExpr(ctx: NCIntentDslParser.EqExprContext): Unit = {
            termCode += ((_, stack: StackType, _) ⇒ {
                implicit val s = stack

                require(stack.size >= 2)

                val (v1, v2, f1, f2) = pop2()
                val usedTok = f1 || f2

                def doEq(op: String): Boolean = {
                    if (isJLong(v1) && isJLong(v2))
                        asJLong(v1) == asJLong(v2)
                    if (isJLong(v1) && isJLong(v2))
                        asJLong(v1) == asJLong(v2)
                    else
                        throw errBinaryOp(op, v1, v2)

                }

                if (ctx.EQ() != null)
                    pushBoolean(doEq("=="), usedTok)
                else {
                    assert(ctx.NEQ() != null)

                    pushBoolean(!doEq("!='"), usedTok)
                }
            })
        }

        override def exitCallExpr(ctx: NCIntentDslParser.CallExprContext): Unit = {
            val fun = ctx.ID().getText

            termCode += ((tok: NCToken, stack: StackType, ctx: NCDslTermContext) ⇒ {
                implicit val evidence = stack

                def ensureStack(min: Int): Unit =
                    if (stack.size < min)
                        throw errParamNum(fun)

                def get1Str(): (String, Boolean) = {
                    ensureStack(1)

                    val (v, f) = pop1()

                    if (!isString(v))
                        throw errParamType(fun, v)

                    (asString(v), f)
                }
                def get1Any(): (AnyRef, Boolean) = {
                    ensureStack(1)

                    pop1()
                }

                /*
                 * String operations.
                 */
                def doTrim(): Unit = get1Str() match { case (s, f) ⇒ pushAny(s.trim, f) }
                def doUppercase(): Unit = get1Str() match { case (s, f) ⇒ pushAny(s.toUpperCase, f) }
                def doLowercase(): Unit = get1Str() match { case (s, f) ⇒ pushAny(s.toLowerCase, f) }
                def doIsAlpha(): Unit = get1Str() match { case (s, f) ⇒ pushBoolean(StringUtils.isAlpha(asString(s)), f) }
                def doIsNum(): Unit = get1Str() match { case (s, f) ⇒ pushBoolean(StringUtils.isNumeric(asString(s)), f) }
                def doIsAlphaNum(): Unit = get1Str() match { case (s, f) ⇒ pushBoolean(StringUtils.isAlphanumeric(asString(s)), f) }
                def doIsWhitespace(): Unit = get1Str() match { case (s, f) ⇒ pushBoolean(StringUtils.isWhitespace(asString(s)), f) }
                def doIsAlphaSpace(): Unit = get1Str() match { case (s, f) ⇒ pushBoolean(StringUtils.isAlphaSpace(asString(s)), f) }
                def doIsAlphaNumSpace(): Unit = get1Str() match { case (s, f) ⇒ pushBoolean(StringUtils.isAlphanumericSpace(asString(s)), f) }
                def doIsNumSpace(): Unit = get1Str() match { case (s, f) ⇒ pushBoolean(StringUtils.isNumericSpace(asString(s)), f) }

                def doSplit(): Unit = {
                    ensureStack(2)

                    val (v1, v2, f1, f2) = pop2()
                    val usedTok = f1 || f2

                    if (!isString(v1))
                        errParamType(fun, v1)
                    if (!isString(v2))
                        errParamType(fun, v2)

                    asString(v1).split(asString(v2)).foreach { pushAny(_, usedTok) }
                }

                /*
                 * Math operations.
                 */
                def doAbs(): Unit = get1Any() match {
                    case (a: JLong, f) ⇒ pushLong(Math.abs(a), f)
                    case (a: JDouble, f) ⇒ pushDouble(Math.abs(a), f)
                    case x ⇒ errParamType(fun, x)
                }

                /*
                 * Collection, statistical operations.
                 */
                def doList(): Unit = {
                    val jl = new JArrayList[Object]() // Empty list is allowed.
                    var f = false

                    stack.drain { x ⇒
                        jl.add(x.retVal)
                        f = f || x.usedTok
                    }

                    Collections.reverse(jl)

                    pushAny(jl, f)
                }
                def doMap(): Unit = {
                    if (stack.size % 2 != 0)
                        errParamNum(fun)

                    val jm = new JHashMap[Object, Object]()
                    var f = false

                    val keys = mutable.Buffer.empty[AnyRef]
                    val vals = mutable.Buffer.empty[AnyRef]

                    var idx = 0

                    stack.drain { x ⇒
                        if (idx % 2 == 0) keys += x.retVal else vals += x.retVal
                        f = f || x.usedTok

                        idx += 1
                    }

                    for ((k, v) ← keys zip vals)
                        jm.put(k, v)

                    pushAny(jm, f)
                }

                /*
                 * Metadata operations.
                 */
                def doTokenMeta(): Unit = get1Str() match {
                    case (s, _) ⇒ pushAny(tok.meta(s), true)
                }
                def doModelMeta(): Unit = get1Str() match {
                    case (s, _) ⇒ pushAny(tok.getModel.meta(s), false)
                }
                def doReqMeta(): Unit = get1Str() match {
                    case (s, _) ⇒ pushAny(ctx.reqMeta.get(s).orNull, false)
                }
                def doUserMeta(): Unit = get1Str() match {
                    case (s, _) ⇒ pushAny(ctx.usrMeta.get(s).orNull, false)
                }
                def doCompMeta(): Unit = get1Str() match {
                    case (s, _) ⇒ pushAny(ctx.compMeta.get(s).orNull, false)
                }
                def doIntentMeta(): Unit = get1Str() match {
                    case (s, _) ⇒ pushAny(ctx.intentMeta.get(s).orNull, false)
                }

                /*
                 * Date-time operations.
                 */
                def doYear(): Unit = pushLong(LocalDate.now.getYear,false)
                def doMonth(): Unit = pushLong(LocalDate.now.getMonthValue,false)
                def doDayOfMonth(): Unit = pushLong(LocalDate.now.getDayOfMonth,false)
                def doDayOfWeek(): Unit = pushLong(LocalDate.now.getDayOfWeek.getValue,false)
                def doDayOfYear(): Unit = pushLong(LocalDate.now.getDayOfYear,false)

                fun match {
                    // Metadata access.
                    case "token_meta" ⇒ doTokenMeta()
                    case "model_meta" ⇒ doModelMeta()
                    case "intent_meta" ⇒ doIntentMeta()
                    case "req_meta" ⇒ doReqMeta()
                    case "user_meta" ⇒ doUserMeta()
                    case "company_meta" ⇒ doCompMeta()

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
                    case "trim" ⇒ doTrim()
                    case "strip" ⇒ doTrim()
                    case "uppercase" ⇒ doUppercase()
                    case "lowercase" ⇒ doLowercase()
                    case "is_alpha" ⇒ doIsAlpha()
                    case "is_alphanum" ⇒ doIsAlphaNum()
                    case "is_whitespace" ⇒ doIsWhitespace()
                    case "is_numeric" ⇒ doIsNum()
                    case "is_numeric_space" ⇒ doIsNumSpace()
                    case "is_alpha_space" ⇒ doIsAlphaSpace()
                    case "is_alphanum_space" ⇒ doIsAlphaNumSpace()
                    case "substring" ⇒
                    case "index" ⇒
                    case "regex" ⇒
                    case "soundex" ⇒
                    case "split" ⇒ doSplit()
                    case "replace" ⇒

                    // Math functions.
                    case "abs" ⇒ doAbs()
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
                    case "list" ⇒ doList()
                    case "map" ⇒ doMap()
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
                    case "year" ⇒ doYear()
                    case "month" ⇒ doMonth()
                    case "day_of_month" ⇒ doDayOfMonth()
                    case "day_of_week" ⇒ doDayOfWeek()
                    case "day_of_year" ⇒ doDayOfYear()
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
