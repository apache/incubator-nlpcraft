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

package org.apache.nlpcraft.model.intent.impl

import org.antlr.v4.runtime.tree.{TerminalNode ⇒ TN}
import org.antlr.v4.runtime.{ParserRuleContext ⇒ PRC}
import org.apache.commons.lang3.StringUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.intent.utils.{NCDslTermContext, NCDslTermRetVal}

import java.lang.{Double ⇒ JDouble, Long ⇒ JLong}
import java.time.LocalDate
import java.util.{Collections, ArrayList ⇒ JArrayList, HashMap ⇒ JHashMap}
import scala.collection.mutable

//noinspection DuplicatedCode
trait NCIntentDslBaselCompiler {
    def syntaxError(errMsg: String, srcName: String, line: Int, pos: Int): NCE
    def runtimeError(errMsg: String, srcName: String, line: Int, pos: Int, cause: Exception = null): NCE

    /**
     *
     * @param errMsg
     * @param ctx
     * @return
     */
    def newSyntaxError(errMsg: String)(implicit ctx: PRC): NCE = {
        val tok = ctx.start

        syntaxError(errMsg, tok.getTokenSource.getSourceName, tok.getLine, tok.getCharPositionInLine)
    }
    /**
      *
      * @param errMsg
      * @param ctx
      * @return
      */
    def newRuntimeError(errMsg: String, cause: Exception = null)(implicit ctx: PRC): NCE = {
        val tok = ctx.start

        runtimeError(errMsg, tok.getTokenSource.getSourceName, tok.getLine, tok.getCharPositionInLine, cause)
    }

    type StackType = mutable.ArrayStack[NCDslTermRetVal]
    type Instr = (NCToken, StackType,  NCDslTermContext) ⇒ Unit

    //noinspection ComparingUnrelatedTypes
    def isJLong(v: Object): Boolean = v.isInstanceOf[JLong]
    //noinspection ComparingUnrelatedTypes
    def isJDouble(v: Object): Boolean = v.isInstanceOf[JDouble]
    //noinspection ComparingUnrelatedTypes
    def isBoolean(v: Object): Boolean = v.isInstanceOf[Boolean]
    def isString(v: Object): Boolean = v.isInstanceOf[String]
    def isToken(v: Object): Boolean = v.isInstanceOf[NCToken]
    def asJLong(v: Object): Long = v.asInstanceOf[JLong].longValue()
    def asJDouble(v: Object): Double = v.asInstanceOf[JDouble].doubleValue()
    def asString(v: Object): String = v.asInstanceOf[String]
    def asBool(v: Object): Boolean = v.asInstanceOf[Boolean]

    def pushAny(any: Object, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(any, usedTok))
    def pushLong(any: Long, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(Long.box(any), usedTok))
    def pushDouble(any: Double, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(Double.box(any), usedTok))
    def pushBool(any: Boolean, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(Boolean.box(any), usedTok))

    // Runtime errors.
    def rtUnaryOpError(op: String, v: Object)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Unexpected '$op' DSL operation for value: $v")
    def rtBinaryOpError(op: String, v1: Object, v2: Object)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Unexpected '$op' DSL operation for values: $v1, $v2")
    def rtUnknownFunError(fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Unknown DSL function: $fun()")
    def rtMinParamNumError(min: Int, fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Invalid number of parameters for DSL function ($min is required): $fun()")
    def rtParamNumError(fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Invalid number of parameters for DSL function: $fun()")
    def rtParamTypeError(fun: String, param: Object, expectType: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Expecting '$expectType' type of parameter for DSL function '$fun()', found: $param")

    /**
     *
     * @param stack
     * @return
     */
    def pop2()(implicit stack: StackType): (Object, Object, Boolean, Boolean) = {
        require(stack.size >= 2)

        // Stack pops in reverse order of push...
        val NCDslTermRetVal(val2, f2) = stack.pop()
        val NCDslTermRetVal(val1, f1) = stack.pop()

        (val1, val2, f1, f2)
    }
    /**
     *
     * @param stack
     * @return
     */
    def pop3()(implicit stack: StackType): (Object, Object, Object, Boolean, Boolean, Boolean) = {
        require(stack.size >= 3)

        // Stack pops in reverse order of push...
        val NCDslTermRetVal(val3, f3) = stack.pop()
        val NCDslTermRetVal(val2, f2) = stack.pop()
        val NCDslTermRetVal(val1, f1) = stack.pop()

        (val1, val2, val3, f1, f2, f3)
    }

    /**
     *
     * @param stack
     * @return
     */
    def pop1()(implicit stack: StackType): (Object, Boolean) = {
        require(stack.nonEmpty)

        val NCDslTermRetVal(v, f) = stack.pop()

        (v, f)
    }

    /**
     *
     * @param lt
     * @param gt
     * @param lteq
     * @param gteq
     */
    def parseCompExpr(lt: TN, gt: TN, lteq: TN, gteq: TN)(implicit ctx: PRC): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s: StackType = stack

        val (v1, v2, f1, f2) = pop2()
        val usedTok = f1 || f2

        if (lt != null) {
            if (isJLong(v1) && isJLong(v2)) pushBool(asJLong(v1) < asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushBool(asJLong(v1) < asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushBool(asJDouble(v1) < asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushBool(asJDouble(v1) < asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError("<", v1, v2)
        }
        else if (gt != null) {
            if (isJLong(v1) && isJLong(v2)) pushBool(asJLong(v1) > asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushBool(asJLong(v1) > asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushBool(asJDouble(v1) > asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushBool(asJDouble(v1) > asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError(">", v1, v2)
        }
        else if (lteq != null) {
            if (isJLong(v1) && isJLong(v2)) pushBool(asJLong(v1) <= asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushBool(asJLong(v1) <= asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushBool(asJDouble(v1) <= asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushBool(asJDouble(v1) <= asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError("<=", v1, v2)
        }
        else {
            assert(gteq != null)

            if (isJLong(v1) && isJLong(v2)) pushBool(asJLong(v1) >= asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushBool(asJLong(v1) >= asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushBool(asJDouble(v1) >= asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushBool(asJDouble(v1) >= asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError(">=", v1, v2)
        }
    }

    /**
     *
     * @param mult
     * @param mod
     * @param div
     */
    def parseMultExpr(mult: TN, mod: TN, div: TN)(implicit ctx: PRC): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s: StackType = stack

        val (v1, v2, f1, f2) = pop2()
        val usedTok = f1 || f2

        if (mult != null) {
            if (isJLong(v1) && isJLong(v2)) pushLong(asJLong(v1) * asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushDouble(asJLong(v1) * asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushDouble(asJDouble(v1) * asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushDouble(asJDouble(v1) * asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError("*", v1, v2)
        }
        else if (mod != null) {
            if (isJLong(v1) && isJLong(v2)) pushLong(asJLong(v1) % asJLong(v2), usedTok)
            else
                throw rtBinaryOpError("%", v1, v2)
        }
        else {
            assert(div != null)

            if (isJLong(v1) && isJLong(v2)) pushLong(asJLong(v1) / asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushDouble(asJLong(v1) / asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushDouble(asJDouble(v1) / asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushDouble(asJDouble(v1) / asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError("/", v1, v2)
        }
    }

    /**
     *
     * @param and
     * @param or
     * @return
     */
    def parseLogExpr(and: TN, or : TN)(implicit ctx: PRC): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s: StackType = stack

        val (v1, v2, f1, f2) = pop2()

        if (!isBoolean(v1) || !isBoolean(v2))
            throw rtBinaryOpError(if (and != null) "&&" else "||", v1, v2)

        if (and != null)
            pushBool(asBool(v1) && asBool(v2), f1 || f2) // Note logical OR for used token flag.
        else {
            assert(or != null)

            pushBool(asBool(v1) || asBool(v2), f1 && f2) // Note local AND for used token flag.
        }
    }

    /**
     *
     * @param eq
     * @param neq
     * @return
     */
    def parseEqExpr(eq: TN, neq: TN)(implicit ctx: PRC): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s: StackType = stack

        val (v1, v2, f1, f2) = pop2()
        val usedTok = f1 || f2

        def doEq(op: String): Boolean = {
            if (isJLong(v1) && isJLong(v2))
                asJLong(v1) == asJLong(v2)
            if (isJLong(v1) && isJLong(v2))
                asJLong(v1) == asJLong(v2)
            else
                throw rtBinaryOpError(op, v1, v2)

        }

        if (eq != null)
            pushBool(doEq("=="), usedTok)
        else {
            assert(neq != null)

            pushBool(!doEq("!='"), usedTok)
        }
    }

    /**
     *
     * @param plus
     * @param minus
     */
    def parsePlusExpr(plus: TN, minus: TN)(implicit ctx: PRC): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s: StackType = stack

        val (v1, v2, f1, f2) = pop2()
        val usedTok = f1 || f2

        if (plus != null) {
            if (isString(v1) && isString(v2)) pushAny(asString(v1) + asString(v2), usedTok)
            else if (isJLong(v1) && isJLong(v2)) pushLong(asJLong(v1) + asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushDouble(asJLong(v1) + asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushDouble(asJDouble(v1) + asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushDouble(asJDouble(v1) + asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError("+", v1, v2)
        }
        else {
            assert(minus != null)

            if (isJLong(v1) && isJLong(v2)) pushLong(asJLong(v1) - asJLong(v2), usedTok)
            else if (isJLong(v1) && isJDouble(v2)) pushDouble(asJLong(v1) - asJDouble(v2), usedTok)
            else if (isJDouble(v1) && isJLong(v2)) pushDouble(asJDouble(v1) - asJLong(v2), usedTok)
            else if (isJDouble(v1) && isJDouble(v2)) pushDouble(asJDouble(v1) - asJDouble(v2), usedTok)
            else
                throw rtBinaryOpError("-", v1, v2)
        }
    }


    /**
     * @param minus
     * @param not
     * @return
     */
    def parseUnaryExpr(minus: TN, not: TN)(implicit ctx: PRC): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s: StackType = stack

        val (v, usedTok) = pop1()

        if (minus != null) {
            if (isJDouble(v)) pushDouble(-asJDouble(v), usedTok)
            else if (isJLong(v)) pushLong(-asJLong(v), usedTok)
            else
                throw rtUnaryOpError("-", v)
        }
        else {
            assert(not != null)

            if (isBoolean(v)) pushBool(!asBool(v), usedTok)
            else
                throw rtUnaryOpError("!", v)
        }
    }

    /**
     *
     * @param txt
     * @return
     */
    def parseAtom(txt: String)(implicit ctx: PRC): Instr = {
        val atom =
            if (txt == "null") null // Try 'null'.
            else if (txt == "true") Boolean.box(true) // Try 'boolean'.
            else if (txt == "false") Boolean.box(false) // Try 'boolean'.
            // Only numeric or string values below...
            else {
                // Strip '_' from numeric values.
                val num = txt.replaceAll("_", "")

                try
                    Long.box(JLong.parseLong(num)) // Try 'long'.
                catch {
                    case _: NumberFormatException ⇒
                        try
                            Double.box(JDouble.parseDouble(num)) // Try 'double'.
                        catch {
                            case _: NumberFormatException ⇒ U.trimEscapesQuotes(txt) // String in the end.
                        }
                }
            }

        (_, stack, _) ⇒ pushAny(atom, false)(stack)
    }

    /**
     *
     * @param id
     * @return
     */
    def parseCallExpr(id: TN)(implicit ctx: PRC): Instr = (tok, stack: StackType, termCtx) ⇒ {
        val fun = id.getText

        implicit val s2: StackType = stack

        def ensureStack(min: Int): Unit =
            if (stack.size < min)
                throw rtMinParamNumError(min, fun)

        def get1Str(): (String, Boolean) = {
            ensureStack(1)

            val (v, f) = pop1()

            if (!isString(v))
                throw rtParamTypeError(fun, v, "string")

            (asString(v), f)
        }
        def get1Double(): (JDouble, Boolean) = {
            ensureStack(1)

            val (v, f) = pop1()

            if (!isJDouble(v))
                throw rtParamTypeError(fun, v, "double")

            (asJDouble(v), f)
        }
        def get2Doubles(): (JDouble, JDouble, Boolean) = {
            ensureStack(2)
            
            val (v1, v2, f1, f2) = pop2()

            if (!isJDouble(v1))
                throw rtParamTypeError(fun, v1, "double")
            if (!isJDouble(v2))
                throw rtParamTypeError(fun, v2, "double")

            (asJDouble(v1), asJDouble(v2), f1 || f2)
        }
        def get1Any(): (Any, Boolean) = {
            ensureStack(1)

            pop1()
        }

        def doSplit(): Unit = {
            ensureStack(2)

            val (v1, v2, f1, f2) = pop2()

            if (!isString(v1))
                throw rtParamTypeError(fun, v1, "string")
            if (!isString(v2))
                throw rtParamTypeError(fun, v2, "string")

            asString(v1).split(asString(v2)).foreach { pushAny(_, f1 || f2) }
        }
        def doSplitTrim(): Unit = {
            ensureStack(2)

            val (v1, v2, f1, f2) = pop2()

            if (!isString(v1))
                throw rtParamTypeError(fun, v1, "string")
            if (!isString(v2))
                throw rtParamTypeError(fun, v2, "string")

            asString(v1).split(asString(v2)).foreach { s ⇒ pushAny(s.strip, f1 || f2) }
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
                throw rtParamNumError(fun)

            val jm = new JHashMap[Object, Object]()
            var f = false

            val keys = mutable.Buffer.empty[Object]
            val vals = mutable.Buffer.empty[Object]

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
        def doPartMeta(): Unit = {
            ensureStack(2)

            val (v1, v2, f1, f2) = pop2()

            if (!isToken(v1))
                throw rtParamTypeError(fun, v1, "token")
            if (!isString(v2))
                throw rtParamTypeError(fun, v2, "string")

            pushAny(v1.asInstanceOf[NCToken].meta(v2.asInstanceOf[String]), f1 || f2)
        }

        /*
         * Math operations.
         */
        def doAbs(): Unit = get1Any() match {
            case (a: JLong, f) ⇒ pushLong(Math.abs(a), f)
            case (a: JDouble, f) ⇒ pushDouble(Math.abs(a), f)
            case x ⇒ throw rtParamTypeError(fun, x, "numeric")
        }
        def doSquare(): Unit = get1Any() match {
            case (a: JLong, f) ⇒ pushLong(a * a, f)
            case (a: JDouble, f) ⇒ pushDouble(a * a, f)
            case x ⇒ throw rtParamTypeError(fun, x, "numeric")
        }

        def doJson(): Unit = get1Str() match { case (s, f) ⇒ pushAny(U.jsonToJavaMap(asString(s)), f) }
        def doIf(): Unit = {
            ensureStack(3)

            val (v1, v2, v3, f1, f2, f3) = pop3()

            if (!isBoolean(v1))
                throw rtParamTypeError(fun, v1, "boolean")

            if (asBool(v1))
                pushAny(v2, f1 || f2)
            else
                pushAny(v3, f1 || f3)
        }

        def token(): NCToken =
            if (stack.nonEmpty && stack.top.isInstanceOf[NCToken]) stack.top.asInstanceOf[NCToken] else tok

        def doPart(): Unit = {
            ensureStack(2)

            val (v1, v2, f1, f2) = pop2()

            if (!isToken(v1))
                throw rtParamTypeError(fun, v1, "token")
            if (!isString(v2))
                throw rtParamTypeError(fun, v2, "string")

            val t = v1.asInstanceOf[NCToken]
            val aliasId = v2.asInstanceOf[String]

            val parts = t.findPartTokens(aliasId)

            if (parts.isEmpty)
                throw newRuntimeError(s"Cannot find part for token (use 'parts' function instead) [" +
                    s"id=${t.getId}, " +
                    s"aliasId=$aliasId" +
                s"]")
            else if (parts.size() > 1)
                throw newRuntimeError(s"Too many parts found for token (use 'parts' function instead) [" +
                    s"id=${t.getId}, " +
                    s"aliasId=$aliasId" +
                s"]")

            pushAny(parts.get(0), f1 || f2)
        }

        def doParts(): Unit = {
            ensureStack(2)

            val (v1, v2, f1, f2) = pop2()

            if (!isToken(v1))
                throw rtParamTypeError(fun, v1, "token")
            if (!isString(v2))
                throw rtParamTypeError(fun, v2, "string")

            pushAny(v1.asInstanceOf[NCToken].findPartTokens(v2.asInstanceOf[String]), f1 || f2)
        }

        fun match {
            // Metadata access.
            case "meta_part" ⇒ doPartMeta()
            case "meta_token" ⇒ get1Str() match { case (s, _) ⇒ pushAny(tok.meta(s), true) }
            case "meta_model" ⇒ get1Str() match { case (s, _) ⇒ pushAny(tok.getModel.meta(s), false) }
            case "meta_intent" ⇒ get1Str() match { case (s, _) ⇒ pushAny(termCtx.intentMeta.get(s).orNull, false) }
            case "meta_req" ⇒ get1Str() match { case (s, _) ⇒ pushAny(termCtx.reqMeta.get(s).orNull, false) }
            case "meta_user" ⇒ get1Str() match { case (s, _) ⇒ pushAny(termCtx.usrMeta.get(s).orNull, false) }
            case "meta_company" ⇒ get1Str() match { case (s, _) ⇒ pushAny(termCtx.compMeta.get(s).orNull, false) }
            case "meta_sys" ⇒ get1Str() match { case (s, _) ⇒ pushAny(U.sysEnv(s).orNull, false) }
            case "meta_conv" ⇒ get1Str() match { case (s, _) ⇒ pushAny(termCtx.convMeta.get(s).orNull, false) }
            case "meta_frag" ⇒ get1Str() match { case (s, _) ⇒ pushAny(termCtx.fragMeta.get(s).orNull, false) }

            // Converts JSON to map.
            case "json" ⇒ doJson()

            // Inline if-statement.
            case "if" ⇒ doIf()

            // Token functions.
            case "id" ⇒ pushAny(token().getId, true)
            case "ancestors" ⇒ pushAny(token().getAncestors, true)
            case "parent" ⇒ pushAny(token().getParentId, true)
            case "groups" ⇒ pushAny(token().getGroups, true)
            case "value" ⇒ pushAny(token().getValue, true)
            case "aliases" ⇒ pushAny(token().getAliases, true)
            case "start_idx" ⇒ pushLong(token().getStartCharIndex, true)
            case "end_idx" ⇒ pushLong(token().getEndCharIndex, true)
            case "this" ⇒ pushAny(tok, true)
            case "part" ⇒ doPart()
            case "parts" ⇒ doParts()

            // Request data.
            case "req_id" ⇒ pushAny(termCtx.req.getServerRequestId, false)
            case "req_normtext" ⇒
            case "req_tstamp" ⇒
            case "req_addr" ⇒
            case "req_agent" ⇒

            // User data.
            case "user_id" ⇒ pushLong(termCtx.req.getUser.getId, false)
            case "user_fname" ⇒
            case "user_lname" ⇒
            case "user_email" ⇒
            case "user_admin" ⇒
            case "user_signup_tstamp" ⇒

            // Company data.
            case "comp_id" ⇒ pushLong(termCtx.req.getCompany.getId, false)
            case "comp_name" ⇒
            case "comp_website" ⇒
            case "comp_country" ⇒
            case "comp_region" ⇒
            case "comp_city" ⇒
            case "comp_addr" ⇒
            case "comp_postcode" ⇒

            // String functions.
            case "trim" ⇒ get1Str() match { case (s, f) ⇒ pushAny(s.trim, f) }
            case "strip" ⇒ get1Str() match { case (s, f) ⇒ pushAny(s.trim, f) }
            case "uppercase" ⇒ get1Str() match { case (s, f) ⇒ pushAny(s.toUpperCase, f) }
            case "lowercase" ⇒ get1Str() match { case (s, f) ⇒ pushAny(s.toLowerCase, f) }
            case "is_alpha" ⇒ get1Str() match { case (s, f) ⇒ pushBool(StringUtils.isAlpha(asString(s)), f) }
            case "is_alphanum" ⇒ get1Str() match { case (s, f) ⇒ pushBool(StringUtils.isAlphanumeric(asString(s)), f) }
            case "is_whitespace" ⇒ get1Str() match { case (s, f) ⇒ pushBool(StringUtils.isWhitespace(asString(s)), f) }
            case "is_num" ⇒ get1Str() match { case (s, f) ⇒ pushBool(StringUtils.isNumeric(asString(s)), f) }
            case "is_numspace" ⇒ get1Str() match { case (s, f) ⇒ pushBool(StringUtils.isNumericSpace(asString(s)), f) }
            case "is_alphaspace" ⇒ get1Str() match { case (s, f) ⇒ pushBool(StringUtils.isAlphaSpace(asString(s)), f) }
            case "is_alphanumspace" ⇒ get1Str() match { case (s, f) ⇒ pushBool(StringUtils.isAlphanumericSpace(asString(s)), f) }
            case "substring" ⇒
            case "charAt" ⇒
            case "regex" ⇒
            case "soundex" ⇒
            case "split" ⇒ doSplit()
            case "split_trim" ⇒ doSplitTrim()
            case "replace" ⇒

            // Math functions.
            case "abs" ⇒ doAbs()
            case "ceil" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.ceil(a), f) }
            case "floor" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.floor(a), f) }
            case "rint" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.rint(a), f) }
            case "round" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushLong(Math.round(a), f) }
            case "signum" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.signum(a), f) }
            case "sqrt" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.sqrt(a), f) }
            case "cbrt" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.cbrt(a), f) }
            case "pi" ⇒ pushDouble(Math.PI, false)
            case "euler" ⇒ pushDouble(Math.E, false)
            case "acos" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.acos(a), f) }
            case "asin" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.asin(a), f) }
            case "atan" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.atan(a), f) }
            case "cos" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.cos(a), f) }
            case "sin" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.sin(a), f) }
            case "tan" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.tan(a), f) }
            case "cosh" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.cosh(a), f) }
            case "sinh" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.sinh(a), f) }
            case "tanh" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.tanh(a), f) }
            case "atn2" ⇒ get2Doubles() match { case (a1: JDouble, a2: JDouble, f) ⇒ pushDouble(Math.atan2(a1, a2), f) }
            case "degrees" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.toDegrees(a), f) }
            case "radians" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.toRadians(a), f) }
            case "exp" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.exp(a), f) }
            case "expm1" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.expm1(a), f) }
            case "hypot" ⇒ get2Doubles() match { case (a1: JDouble, a2: JDouble, f) ⇒ pushDouble(Math.hypot(a1, a2), f) }
            case "log" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.log(a), f) }
            case "log10" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.log10(a), f) }
            case "log1p" ⇒ get1Double() match { case (a: JDouble, f) ⇒ pushDouble(Math.log1p(a), f) }
            case "pow" ⇒ get2Doubles() match { case (a1: JDouble, a2: JDouble, f) ⇒ pushDouble(Math.pow(a1, a2), f) }
            case "rand" ⇒ pushDouble(Math.random, false)
            case "square" ⇒ doSquare()

            // Collection functions.
            case "list" ⇒ doList()
            case "map" ⇒ doMap()
            case "get" ⇒
            case "index" ⇒
            case "has" ⇒
            case "tail" ⇒
            case "add" ⇒
            case "remove" ⇒
            case "first" ⇒
            case "last" ⇒
            case "keys" ⇒
            case "values" ⇒
            case "count" ⇒
            case "take" ⇒
            case "drop" ⇒
            case "size" ⇒
            case "length" ⇒
            case "reverse" ⇒
            case "is_empty" ⇒
            case "non_empty" ⇒
            case "to_string" ⇒

            // Statistical operations.
            case "avg" ⇒
            case "max" ⇒ // Works for numerics as well.
            case "min" ⇒ // Works for numerics as well.
            case "stdev" ⇒
            case "sum" ⇒

            // Date-time functions.
            case "year" ⇒ pushLong(LocalDate.now.getYear,false) // 2021.
            case "month" ⇒ pushLong(LocalDate.now.getMonthValue,false) // 1 ... 12.
            case "day_of_month" ⇒ pushLong(LocalDate.now.getDayOfMonth,false) // 1 ... 31.
            case "day_of_week" ⇒ pushLong(LocalDate.now.getDayOfWeek.getValue,false)
            case "day_of_year" ⇒ pushLong(LocalDate.now.getDayOfYear,false)
            case "hour" ⇒
            case "minute" ⇒
            case "second" ⇒
            case "week_of_month" ⇒
            case "week_of_year" ⇒
            case "quarter" ⇒
            case "now" ⇒ pushLong(System.currentTimeMillis(),false) // Epoc time.

            case _ ⇒ throw rtUnknownFunError(fun) // Assertion.
        }
    }
}
