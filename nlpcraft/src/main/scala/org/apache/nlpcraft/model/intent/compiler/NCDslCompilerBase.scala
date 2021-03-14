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

import org.apache.commons.lang3.StringUtils
import org.apache.nlpcraft.common.{NCE, U}
import org.apache.nlpcraft.model.NCToken
import org.antlr.v4.runtime.{ParserRuleContext ⇒ PRC}
import org.antlr.v4.runtime.tree.{TerminalNode ⇒ TN}

import org.apache.nlpcraft.model.intent.NCDslContext
import org.apache.nlpcraft.model.intent.compiler.{NCDslStackItem ⇒ Z}

import java.lang.{Double ⇒ JDouble, Long ⇒ JLong}
import java.time.LocalDate
import java.util
import java.util.{Collections, List ⇒ JList, Map ⇒ JMap}

import scala.collection.JavaConverters._
import scala.collection.mutable

trait NCDslCompilerBase {
    type Stack = mutable.ArrayStack[() ⇒ Z]
    type Instr = (NCToken, Stack, NCDslContext) ⇒ Unit

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
     * @param cause
     * @param ctx
     * @return
     */
    def newRuntimeError(errMsg: String, cause: Exception = null)(implicit ctx: PRC): NCE = {
        val tok = ctx.start

        runtimeError(errMsg, tok.getTokenSource.getSourceName, tok.getLine, tok.getCharPositionInLine, cause)
    }

    //noinspection ComparingUnrelatedTypes
    def isJLong(v: Object): Boolean = v.isInstanceOf[JLong]
    //noinspection ComparingUnrelatedTypes
    def isJDouble(v: Object): Boolean = v.isInstanceOf[JDouble]
    //noinspection ComparingUnrelatedTypes
    def isBool(v: Object): Boolean = v.isInstanceOf[Boolean]
    def isJList(v: Object): Boolean = v.isInstanceOf[JList[_]]
    def isJMap(v: Object): Boolean = v.isInstanceOf[JMap[_, _]]
    def isStr(v: Object): Boolean = v.isInstanceOf[String]
    def isToken(v: Object): Boolean = v.isInstanceOf[NCToken]
    def asJLong(v: Object): Long = v.asInstanceOf[JLong].longValue()
    def asJList(v: Object): JList[_] = v.asInstanceOf[JList[_]]
    def asJMap(v: Object): JMap[_, _] = v.asInstanceOf[JMap[_, _]]
    def asJDouble(v: Object): Double = v.asInstanceOf[JDouble].doubleValue()
    def asStr(v: Object): String = v.asInstanceOf[String]
    def asToken(v: Object): NCToken = v.asInstanceOf[NCToken]
    def asBool(v: Object): Boolean = v.asInstanceOf[Boolean]

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
    def pop1()(implicit stack: Stack): () ⇒ Z = {
        require(stack.nonEmpty)

        stack.pop()
    }

    /**
     *
     * @param stack
     * @return
     */
    def pop2()(implicit stack: Stack): (() ⇒ Z, () ⇒ Z) = {
        require(stack.size >= 2)

        // Stack pops in reverse order of push...
        val v2 = stack.pop()
        val v1 = stack.pop()

        (v1, v2)
    }

    /**
     *
     * @param stack
     * @return
     */
    def pop3()(implicit stack: Stack): (() ⇒ Z, () ⇒ Z, () ⇒ Z) = {
        require(stack.size >= 3)

        // Stack pops in reverse order of push...
        val v3 = stack.pop()
        val v2 = stack.pop()
        val v1 = stack.pop()

        (v1, v2, v3)
    }

    /**
     *
     * @param lt
     * @param gt
     * @param lteq
     * @param gteq
     */
    def parseCompExpr(lt: TN, gt: TN, lteq: TN, gteq: TN)(implicit ctx: PRC): Instr = (_, stack: Stack, _) ⇒ {
        val (item1, item2) = pop2()(stack)

        if (lt != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) < asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) < asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) < asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) < asJDouble(v2)
                    else
                        throw rtBinaryOpError("<", v1, v2)

                Z(f, f1 || f2)
            })
        else if (gt != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) > asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) > asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) > asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) > asJDouble(v2)
                    else
                        throw rtBinaryOpError(">", v1, v2)

                Z(f, f1 || f2)
            })
        else if (lteq != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) <= asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) <= asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) <= asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) <= asJDouble(v2)
                    else
                        throw rtBinaryOpError("<=", v1, v2)

                Z(f, f1 || f2)
            })
        else {
            require(gteq != null)

            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) >= asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) >= asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) >= asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) >= asJDouble(v2)
                    else
                        throw rtBinaryOpError(">=", v1, v2)

                Z(f, f1 || f2)
            })
        }
    }

    /**
     *
     * @param mult
     * @param mod
     * @param div
     */
    def parseMultDivModExpr(mult: TN, mod: TN, div: TN)(implicit ctx: PRC): Instr = (_, stack: Stack, _) ⇒ {
        val (item1, item2) = pop2()(stack)

        if (mult != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) * asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) * asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) * asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) * asJDouble(v2)
                    else
                        throw rtBinaryOpError("*", v1, v2)

                Z(f, f1 || f2)
            })
        else if (mod != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) % asJLong(v2)
                    else
                        throw rtBinaryOpError("%", v1, v2)

                Z(f, f1 || f2)
            })
        else {
            assert(div != null)

            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) / asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) / asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) / asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) / asJDouble(v2)
                    else
                        throw rtBinaryOpError("/", v1, v2)

                Z(f, f1 || f2)
            })
        }
    }

    /**
     *
     * @param and
     * @param or
     * @return
     */
    def parseAndOrExpr(and: TN, or: TN)(implicit ctx: PRC): Instr = (_, stack: Stack, _) ⇒ {
        val (item1, item2) = pop2()(stack)

        stack.push(() ⇒ {
            val (op, flag) = if (and != null) ("&&", false) else ("||", true)

            val v1 = item1()

            if (!isBool(v1))
                throw rtBinaryOpError(op, v1, item2())

            // NOTE: check v1 first and only if it is {true|false} check the v2.
            if (asBool(v1) == flag)
                Z(flag, v1.usedTok)
            else {
                val v2 = item2()

                if (!isBool(v2))
                    throw rtBinaryOpError(op, v2, v1)

                Z(asBool(v2), if (and != null) v1.usedTok || v2.usedTok else v1.usedTok && v2.usedTok)
            }
        })
    }

    /**
     *
     * @param eq
     * @param neq
     * @return
     */
    def parseEqNeqExpr(eq: TN, neq: TN)(implicit ctx: PRC): Instr = (_, stack: Stack, _) ⇒ {
        val (item1, item2) = pop2()(stack)

        def doEq(op: String, v1: Object, v2: Object): Boolean = {
            if (v1 == null && v2 == null) true
            else if ((v1 == null && v2 != null) || (v1 != null && v2 == null)) false
            else if (isJLong(v1) && isJLong(v2)) asJLong(v1) == asJLong(v2)
            else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) == asJDouble(v2)
            else if (isBool(v1) && isBool(v2)) asBool(v1) == asBool(v2)
            else if (isStr(v1) && isStr(v2)) asStr(v1) == asStr(v2)
            else if (isJList(v1) && isJList(v2)) asJList(v1).equals(asJList(v2))
            else {
                throw rtBinaryOpError(op, v1, v2)
            }}

        stack.push(() ⇒ {
            val Z(v1, f1) = item1()
            val Z(v2, f2) = item2()

            val f =
                if (eq != null)
                    doEq("==", v1, v2)
                else {
                    assert(neq != null)

                    !doEq("!='", v1, v2)
                }

            Z(f, f1 || f2)
        })
    }

    /**
     *
     * @param plus
     * @param minus
     */
    def parsePlusMinusExpr(plus: TN, minus: TN)(implicit ctx: PRC): Instr = (_, stack: Stack, _) ⇒ {
        val (item1, item2) = pop2()(stack)

        if (plus != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = item1()
                val Z(v2, f2) = item2()

                val f: Object =
                    if (isStr(v1) && isStr(v2)) asStr(v1) + asStr(v2)
                    else if (isJLong(v1) && isJLong(v2)) asJLong(v1) + asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) + asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) + asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) + asJDouble(v2)
                    else
                        throw rtBinaryOpError("+", v1, v2)

                Z(f, f1 || f2)
            })
        else {
            assert(minus != null)

            stack.push(() ⇒ {
                val v1 = item1()
                val v2 = item2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) - asJLong(v2)
                    else if (isJLong(v1) && isJDouble(v2)) asJLong(v1) - asJDouble(v2)
                    else if (isJDouble(v1) && isJLong(v2)) asJDouble(v1) - asJLong(v2)
                    else if (isJDouble(v1) && isJDouble(v2)) asJDouble(v1) - asJDouble(v2)
                    else
                        throw rtBinaryOpError("-", v1, v2)

                Z(f, v1.usedTok || v2.usedTok)
            })
    }


    /**
     * @param minus
     * @param not
     * @return
     */
    def parseUnaryExpr(minus: TN, not: TN)(implicit ctx: PRC): Instr = (_, stack: Stack, _) ⇒ {
        val item = pop1()(stack)

        if (minus != null)
            stack.push(() ⇒ {
                val Z(v, f) = item()

                val z =
                    if (isJDouble(v)) -asJDouble(v)
                    else if (isJLong(v)) -asJLong(v)
                    else
                        throw rtUnaryOpError("-", v)

                Z(z, f)
            })
        else {
            assert(not != null)

            stack.push(() ⇒ {
                val Z(v, f) = item()

                if (isBool(v)) Z(!asBool(v), f)
                else
                    throw rtUnaryOpError("!", v)
            })
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

        (_, stack, _) ⇒ stack.push(() ⇒ Z(atom, false))
    }

    /**
     *
     * @param id
     * @return
     */
    def parseCallExpr(id: TN)(implicit ctx: PRC): Instr = (tok, stack: Stack, termCtx) ⇒ {
        val fun = id.getText

        implicit val evidence: Stack = stack

        def ensureStack(min: Int): Unit =
            if (stack.size < min)
                throw rtMinParamNumError(min, fun)

        def toX[T](typ: String, v: Object, is: Object ⇒ Boolean, as: Object ⇒ T): T = {
            if (!is(v))
                throw rtParamTypeError(fun, v, typ)

            as(v)
        }

        def toStr(v: Object): String = toX("string", v, isStr, asStr)
        def toJDouble(v: Object): JDouble = toX("double", v, isJDouble, asJDouble)
        def toJLong(v: Object): JDouble = toX("long", v, isJLong, asJLong)
        def toJList(v: Object): JList[_] = toX("list", v, isJList, asJList)
        def toJMap(v: Object): JMap[_, _] = toX("map", v, isJMap, asJMap)
        def toToken(v: Object): NCToken = toX("token", v, isToken, asToken)

        def optToken(): NCToken =
            if (stack.nonEmpty && stack.top.isInstanceOf[NCToken]) stack.pop().asInstanceOf[NCToken] else tok

        def doSplit(): Unit = {
            val (item1, item2) = pop2()

            stack.push(
                () ⇒ {
                    val Z(v1, f1) = item1()
                    val Z(v2, f2) = item2()

                   Z(util.Arrays.asList(toStr(v1).split(toStr(v2))), f1 || f2)
                }
            )
        }

        def doSplitTrim(): Unit = {
            val (item1, item2) = pop2()

            stack.push(
                () ⇒ {
                    val Z(v1, f1) = item1()
                    val Z(v2, f2) = item2()

                    Z(util.Arrays.asList(toStr(v1).split(toStr(v2)).toList.map(_.strip)), f1 || f2)
                }
            )
        }










        // ----------------------------------------------------












        /*
         * Collection, statistical operations.
         */
        def doList(): Unit = {
            val dump = new Stack() // Empty list is allowed.

            stack.drain { dump += _ }

            stack.push(() ⇒ {
                val jl = new util.ArrayList[Object]()
                var f: Boolean = true

                dump.reverse.foreach { x ⇒
                    val v = x()

                    f = f || v.usedTok

                    jl.add(v.value)
                }

                Z(jl, f)
            })
        }
        
        def doSize(): Unit = get1List() match { case (list, f) ⇒ pushLong(() ⇒ list().size(), f) }
        
        def doHas(): Unit = {
            val (vf1, vf2, f1, f2) = pop2()

            pushBool(() ⇒ {
                val v1 = vf1()
                val v2 = vf2()

                if (!isJList(v1))
                    throw rtParamTypeError(fun, v1, "list")

                asJList(v1).contains(v2)
            }, f1 || f2)
        }

        def doGet(): Unit = {
            ensureStack(2)

            val (col, key, f1, f2) = pop2() // NOTE: pop2() corrects for stack's LIFO order.
            val f = f1 || f2

            if (isJList(col)) {
                if (isJLong(key))
                    pushAny(asJList(col).get(asJLong(key).intValue()).asInstanceOf[Object], f)
                else
                    rtParamTypeError(fun, key, "numeric")
            }
            else if (isJMap(col))
                pushAny(asJMap(col).get(key).asInstanceOf[Object], f)
            else
                rtParamTypeError(fun, col, "list or map")
        }

        def doMap(): Unit = {
            if (stack.size % 2 != 0)
                throw rtParamNumError(fun)

            val jm = new util.HashMap[Object, Object]()
            var f = false

            val keys = mutable.Buffer.empty[Object]
            val vals = mutable.Buffer.empty[Object]

            var idx = 0

            stack.drain { x ⇒
                if (idx % 2 == 0) keys += x.valFun else vals += x.valFun
                f = f || x.usedTok

                idx += 1
            }

            for ((k, v) ← keys zip vals)
                jm.put(k, v)

            pushAny(jm, f)
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

        def doJson(): Unit = get1Str() match {
            case (s, f) ⇒ pushAny(U.jsonToJavaMap(asStr(s)), f)
        }

        def doIf(): Unit = {
            ensureStack(3)

            val (v1, v2, v3, f1, f2, f3) = pop3()

            if (!isBool(v1))
                throw rtParamTypeError(fun, v1, "boolean")

            if (asBool(v1))
                pushAny(v2, f1 || f2)
            else
                pushAny(v3, f1 || f3)
        }

        def doPart(): Unit = {
            val (t, aliasId, f) = get1Tok1Str()

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

            pushAny(parts.get(0), f)
        }

        def doParts(): Unit = get1Tok1Str() match { case (t, aliasId, f) ⇒ pushAny(t.findPartTokens(aliasId), f) }

        fun match {
            // Metadata access.
            case "meta_part" ⇒ pop2() match { case (t, s, f1, f2) ⇒  push(() ⇒ toToken(t).meta(toStr(s)), f1 || f2) }
            case "meta_token" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ tok.meta(toStr(s)), true) }
            case "meta_model" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ tok.getModel.meta(toStr(s)), false) }
            case "meta_intent" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ termCtx.intentMeta.get(toStr(s)).orNull, false) }
            case "meta_req" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ termCtx.req.getRequestData.get(toStr(s)), false) }
            case "meta_user" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ termCtx.req.getUser.getMetadata.get(toStr(s)), false) }
            case "meta_company" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ termCtx.req.getCompany.getMetadata.get(s), false) }
            case "meta_sys" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ U.sysEnv(toStr(s)).orNull, false) }
            case "meta_conv" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ termCtx.convMeta.get(toStr(s)).orNull, false) }
            case "meta_frag" ⇒ pop1() match { case (s, _) ⇒ push(() ⇒ termCtx.fragMeta.get(toStr(s)).orNull, false) }

            // Converts JSON to map.
            case "json" ⇒ doJson()

            // Inline if-statement.
            case "if" ⇒ doIf()

            // Token functions.
            case "id" ⇒ push(() ⇒ optToken().getId, true)
            case "ancestors" ⇒ push(() ⇒ optToken().getAncestors, true)
            case "parent" ⇒ push(() ⇒ optToken().getParentId, true)
            case "groups" ⇒ push(() ⇒ optToken().getGroups, true)
            case "value" ⇒ push(() ⇒ optToken().getValue, true)
            case "aliases" ⇒ push(() ⇒ optToken().getAliases, true)
            case "start_idx" ⇒ push(() ⇒ optToken().getStartCharIndex, true)
            case "end_idx" ⇒ push(() ⇒ optToken().getEndCharIndex, true)
            case "this" ⇒ push(() ⇒ tok, true)
            case "part" ⇒ doPart()
            case "parts" ⇒ doParts()

            // Request data.
            case "req_id" ⇒ push(() ⇒ termCtx.req.getServerRequestId, false)
            case "req_normtext" ⇒ push(() ⇒ termCtx.req.getNormalizedText, false)
            case "req_tstamp" ⇒ push(() ⇒ termCtx.req.getReceiveTimestamp, false)
            case "req_addr" ⇒ push(() ⇒ termCtx.req.getRemoteAddress.orElse(null), false)
            case "req_agent" ⇒ push(() ⇒ termCtx.req.getClientAgent.orElse(null), false)

            // User data.
            case "user_id" ⇒ push(() ⇒ termCtx.req.getUser.getId, false)
            case "user_fname" ⇒ push(() ⇒ termCtx.req.getUser.getFirstName, false)
            case "user_lname" ⇒ push(() ⇒ termCtx.req.getUser.getLastName, false)
            case "user_email" ⇒ push(() ⇒ termCtx.req.getUser.getEmail, false)
            case "user_admin" ⇒ push(() ⇒ termCtx.req.getUser.isAdmin, false)
            case "user_signup_tstamp" ⇒ push(() ⇒ termCtx.req.getUser.getSignupTimestamp, false)

            // Company data.
            case "comp_id" ⇒ push(() ⇒ termCtx.req.getCompany.getId, false)
            case "comp_name" ⇒ push(() ⇒ termCtx.req.getCompany.getName, false)
            case "comp_website" ⇒ push(() ⇒ termCtx.req.getCompany.getWebsite, false)
            case "comp_country" ⇒ push(() ⇒ termCtx.req.getCompany.getCountry, false)
            case "comp_region" ⇒ push(() ⇒ termCtx.req.getCompany.getRegion, false)
            case "comp_city" ⇒ push(() ⇒ termCtx.req.getCompany.getCity, false)
            case "comp_addr" ⇒ push(() ⇒ termCtx.req.getCompany.getAddress, false)
            case "comp_postcode" ⇒ push(() ⇒ termCtx.req.getCompany.getPostalCode, false)

            // String functions.
            case "trim" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ toStr(s).trim, f) }
            case "strip" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ toStr(s).trim, f) }
            case "uppercase" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ toStr(s).toUpperCase, f) }
            case "lowercase" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ toStr(s).toLowerCase, f) }
            case "is_alpha" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ StringUtils.isAlpha(toStr(s)), f) }
            case "is_alphanum" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ StringUtils.isAlphanumeric(toStr(s)), f) }
            case "is_whitespace" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ StringUtils.isWhitespace(toStr(s)), f) }
            case "is_num" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ StringUtils.isNumeric(toStr(s)), f) }
            case "is_numspace" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ StringUtils.isNumericSpace(toStr(s)), f) }
            case "is_alphaspace" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ StringUtils.isAlphaSpace(toStr(s)), f) }
            case "is_alphanumspace" ⇒ pop1() match { case (s, f) ⇒ push(() ⇒ StringUtils.isAlphanumericSpace(toStr(s)), f) }
            case "substring" ⇒
            case "charAt" ⇒
            case "regex" ⇒
            case "soundex" ⇒
            case "split" ⇒ doSplit()
            case "split_trim" ⇒ doSplitTrim()
            case "replace" ⇒

            // Math functions.
            case "abs" ⇒ doAbs()
            case "ceil" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.ceil(toJDouble(a)), f) }
            case "floor" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.floor(toJDouble(a)), f) }
            case "rint" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.rint(toJDouble(a)), f) }
            case "round" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.round(toJDouble(a)), f) }
            case "signum" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.signum(toJDouble(a)), f) }
            case "sqrt" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.sqrt(toJDouble(a)), f) }
            case "cbrt" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.cbrt(toJDouble(a)), f) }
            case "pi" ⇒ push(() ⇒ Math.PI, false)
            case "euler" ⇒ push(() ⇒ Math.E, false)
            case "acos" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.acos(toJDouble(a)), f) }
            case "asin" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.asin(toJDouble(a)), f) }
            case "atan" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.atan(toJDouble(a)), f) }
            case "cos" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.cos(toJDouble(a)), f) }
            case "sin" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.sin(toJDouble(a)), f) }
            case "tan" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.tan(toJDouble(a)), f) }
            case "cosh" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.cosh(toJDouble(a)), f) }
            case "sinh" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.sinh(toJDouble(a)), f) }
            case "tanh" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.tanh(toJDouble(a)), f) }
            case "atn2" ⇒ pop2() match { case (a1, a2, f1, f2) ⇒ push(() ⇒ Math.atan2(toJDouble(a1), toJDouble(a2)), f1 || f2) }
            case "degrees" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.toDegrees(toJDouble(a)), f) }
            case "radians" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.toRadians(toJDouble(a)), f) }
            case "exp" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.exp(toJDouble(a)), f) }
            case "expm1" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.expm1(toJDouble(a)), f) }
            case "hypot" ⇒ pop2() match { case (a1, a2, f1, f2) ⇒ push(() ⇒ Math.hypot(toJDouble(a1), toJDouble(a2)), f1 || f2) }
            case "log" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.log(toJDouble(a)), f) }
            case "log10" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.log10(toJDouble(a)), f) }
            case "log1p" ⇒ pop1() match { case (a, f) ⇒ push(() ⇒ Math.log1p(toJDouble(a)), f) }
            case "pow" ⇒ pop2() match { case (a1, a2, f1, f2) ⇒ push(() ⇒ Math.pow(toJDouble(a1), toJDouble(a2)), f1 || f2) }
            case "rand" ⇒ push(() ⇒ Math.random, false)
            case "square" ⇒ doSquare()

            // Collection functions.
            case "list" ⇒ doList()
            case "map" ⇒ doMap()
            case "get" ⇒ doGet()
            case "index" ⇒
            case "has" ⇒ doHas()
            case "tail" ⇒
            case "add" ⇒
            case "remove" ⇒
            case "first" ⇒
            case "last" ⇒
            case "keys" ⇒ pop1() match { case (map, f) ⇒ push(() ⇒ new util.ArrayList(toJMap(map).keySet()), f) }
            case "values" ⇒ pop1() match { case (map, f) ⇒ push(() ⇒ new util.ArrayList(toJMap(map).values()), f) }
            case "take" ⇒
            case "drop" ⇒
            case "size" ⇒ doSize()
            case "count" ⇒ doSize()
            case "length" ⇒ doSize()
            case "reverse" ⇒ pop1() match { case (list, f) ⇒ push(() ⇒ {
                val jl = toJList(list)

                Collections.reverse(jl)

                jl
            }, f) }
            case "is_empty" ⇒ pop1() match { case (list, f) ⇒ push(() ⇒ toJList(list).isEmpty, f) }
            case "non_empty" ⇒ pop1() match { case (list, f) ⇒ push(() ⇒ !toJList(list).isEmpty, f) }
            case "to_string" ⇒ pop1() match { case (list, f) ⇒ push(() ⇒ toJList(list).asScala.map(_.toString).asJava, f) }

            // Statistical operations.
            case "avg" ⇒
            case "max" ⇒ // Works for numerics as well.
            case "min" ⇒ // Works for numerics as well.
            case "stdev" ⇒
            case "sum" ⇒

            // Date-time functions.
            case "year" ⇒ push(() ⇒ LocalDate.now.getYear, false) // 2021.
            case "month" ⇒ push(() ⇒ LocalDate.now.getMonthValue, false) // 1 ... 12.
            case "day_of_month" ⇒ push(() ⇒ LocalDate.now.getDayOfMonth, false) // 1 ... 31.
            case "day_of_week" ⇒ push(() ⇒ LocalDate.now.getDayOfWeek.getValue, false)
            case "day_of_year" ⇒ push(() ⇒ LocalDate.now.getDayOfYear, false)
            case "hour" ⇒
            case "minute" ⇒
            case "second" ⇒
            case "week_of_month" ⇒
            case "week_of_year" ⇒
            case "quarter" ⇒
            case "now" ⇒ push(() ⇒ System.currentTimeMillis(), false) // Epoc time.

            case _ ⇒ throw rtUnknownFunError(fun) // Assertion.
        }
    }
}
