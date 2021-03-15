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

trait NCDslCompilerBase {
    type S = NCDslStack
    type T = NCDslStackType
    type I = (NCToken, S, NCDslContext) ⇒ Unit

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
    def pop1()(implicit stack: S, ctx: PRC): T = {
        require(stack.nonEmpty, ctx.getText)

        stack.pop()
    }

    /**
     *
     * @param stack
     * @return
     */
    def pop2()(implicit stack: S, ctx: PRC): (T, T) = {
        require(stack.size >= 2, ctx.getText)

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
    def pop3()(implicit stack: S, ctx: PRC): (T, T, T) = {
        require(stack.size >= 3, ctx.getText)

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
    def parseCompExpr(lt: TN, gt: TN, lteq: TN, gteq: TN)(implicit ctx: PRC): I = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        if (lt != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

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
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

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
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

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
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

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
    def parseMultDivModExpr(mult: TN, mod: TN, div: TN)(implicit ctx: PRC): I = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        if (mult != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

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
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

                val f =
                    if (isJLong(v1) && isJLong(v2)) asJLong(v1) % asJLong(v2)
                    else
                        throw rtBinaryOpError("%", v1, v2)

                Z(f, f1 || f2)
            })
        else {
            assert(div != null)

            stack.push(() ⇒ {
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

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
    def parseAndOrExpr(and: TN, or: TN)(implicit ctx: PRC): I = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        stack.push(() ⇒ {
            val (op, flag) = if (and != null) ("&&", false) else ("||", true)

            val Z(v1, f1) = x1()

            if (!isBool(v1))
                throw rtBinaryOpError(op, v1, x2().value)

            // NOTE: check v1 first and only if it is {true|false} check the v2.
            if (asBool(v1) == flag)
                Z(flag, f1)
            else {
                val Z(v2, f2) = x2()

                if (!isBool(v2))
                    throw rtBinaryOpError(op, v2, v1)

                Z(asBool(v2), if (and != null) f1 || f2 else f1 && f2)
            }
        })
    }

    /**
     *
     * @param eq
     * @param neq
     * @return
     */
    def parseEqNeqExpr(eq: TN, neq: TN)(implicit ctx: PRC): I = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

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
            val Z(v1, f1) = x1()
            val Z(v2, f2) = x2()

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
    def parsePlusMinusExpr(plus: TN, minus: TN)(implicit ctx: PRC): I = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        if (plus != null)
            stack.push(() ⇒ {
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()
                val f = f1 || f2

                if (isStr(v1) && isStr(v2)) Z(asStr(v1) + asStr(v2), f)
                else if (isJLong(v1) && isJLong(v2))  Z(asJLong(v1) + asJLong(v2), f)
                else if (isJLong(v1) && isJDouble(v2))  Z(asJLong(v1) + asJDouble(v2), f)
                else if (isJDouble(v1) && isJLong(v2))  Z(asJDouble(v1) + asJLong(v2), f)
                else if (isJDouble(v1) && isJDouble(v2))  Z(asJDouble(v1) + asJDouble(v2), f)
                else
                    throw rtBinaryOpError("+", v1, v2)
            })
        else {
            assert(minus != null)

            stack.push(() ⇒ {
                val v1 = x1()
                val v2 = x2()

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
    }

    /**
     * @param minus
     * @param not
     * @return
     */
    def parseUnaryExpr(minus: TN, not: TN)(implicit ctx: PRC): I = (_, stack: S, _) ⇒ {
        val x = pop1()(stack, ctx)

        if (minus != null)
            stack.push(() ⇒ {
                val Z(v, f) = x()

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
                val Z(v, f) = x()

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
    def parseAtom(txt: String)(implicit ctx: PRC): I = {
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
    def parseCallExpr(id: TN)(implicit ctx: PRC): I = (tok, stack: S, termCtx) ⇒ {
        implicit val evidence: S = stack
    
        val fun = id.getText
    
        def ensureStack(min: Int): Unit = if (stack.size < min) throw rtMinParamNumError(min, fun)
        def delMarker(): Unit = require(pop1() == stack.MARKER)
        def arg[X](min: Int, f: () ⇒ X): X = {
            ensureStack(min + 1) // +1 for the frame marker.
            
            val x = f()
        
            // Make sure to pop up the parameter list stack frame marker.
            delMarker()
            
            x
        }
        def arg1(): T = arg(1, pop1)
        def arg2(): (T, T) = arg(1, pop2)
        def arg3(): (T, T, T) = arg(1, pop3)
        def arg1Tok(): T =
            if (stack.nonEmpty && stack.top == stack.MARKER) {
                delMarker()
            
                () ⇒ Z(tok, true)
            }
            else
                arg1()
        def toX[T](typ: String, v: Object, is: Object ⇒ Boolean, as: Object ⇒ T): T = {
            if (!is(v))
                throw rtParamTypeError(fun, v, typ)

            as(v)
        }
        def toStr(v: Object): String = toX("string", v, isStr, asStr)
        def toJDouble(v: Object): JDouble = toX("double", v, isJDouble, asJDouble)
        def toJList(v: Object): JList[_] = toX("list", v, isJList, asJList)
        def toJMap(v: Object): JMap[_, _] = toX("map", v, isJMap, asJMap)
        def toToken(v: Object): NCToken = toX("token", v, isToken, asToken)
        def toBool(v: Object): Boolean = toX("boolean", v, isBool, asBool)
    
        def doSplit(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () ⇒ {
                    val Z(v1, f1) = x1()
                    val Z(v2, f2) = x2()

                   Z(util.Arrays.asList(toStr(v1).split(toStr(v2))), f1 || f2)
                }
            )
        }

        def doSplitTrim(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () ⇒ {
                    val Z(v1, f1) = x1()
                    val Z(v2, f2) = x2()

                    Z(util.Arrays.asList(toStr(v1).split(toStr(v2)).toList.map(_.strip)), f1 || f2)
                }
            )
        }

        def doList(): Unit = {
            val dump = new S() // Empty list is allowed.

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

        def doHas(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(v1, f1) = x1()
                val Z(v2, f2) = x2()

                Z(toJList(v1).contains(v2), f1 || f2)
            })
        }

        def doGet(): Unit = {
            val (x1, x2) = arg2() // NOTE: get2() corrects for stack's LIFO order.

            stack.push(() ⇒ {
                val Z(col, f1) = x1()
                val Z(key, f2) = x2()
                val f = f1 || f2

                if (isJList(col)) {
                    if (isJLong(key))
                        Z(asJList(col).get(asJLong(key).intValue()).asInstanceOf[Object], f)
                    else
                        throw rtParamTypeError(fun, key, "numeric")
                }
                else if (isJMap(col))
                    Z(asJMap(col).get(key).asInstanceOf[Object], f)
                else
                    throw rtParamTypeError(fun, col, "list or map")
            })
        }

        def doAbs(): Unit = arg1() match {
            case x ⇒ stack.push(() ⇒ {
                val Z(v, f) = x()

                v match {
                    case a: JLong ⇒ Z(Math.abs(a), f)
                    case a: JDouble ⇒ Z(Math.abs(a), f)
                    case _ ⇒ throw rtParamTypeError(fun, v, "numeric")
                }
            })
        }

        def doSquare(): Unit = arg1() match {
            case x ⇒ stack.push(() ⇒ {
                val Z(v, f) = x()

                v match {
                    case a: JLong ⇒ Z(a * a, f)
                    case a: JDouble ⇒ Z(a * a, f)
                    case _ ⇒ throw rtParamTypeError(fun, v, "numeric")
                }
            })
        }

        def doIf(): Unit = {
            val (x1, x2, x3) = arg3()

            stack.push(() ⇒ {
                val Z(v1, f1) = x1()

                if (toBool(v1)) {
                    val Z(v2, f2) = x2()

                    Z(v2, f1 || f2)
                }
                else {
                    val Z(v3, f3) = x3()

                    Z(v3, f1 || f3)
                }
            })
        }

        //noinspection DuplicatedCode
        def doPart(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(t, f1) = x1()
                val Z(a, f2) = x2()

                val tok = toToken(t)
                val aliasId = toStr(a)

                val parts = tok.findPartTokens(aliasId)

                if (parts.isEmpty)
                    throw newRuntimeError(s"Cannot find part for token (try 'parts' function instead) [" +
                        s"id=${tok.getId}, " +
                        s"aliasId=$aliasId" +
                    s"]")
                else if (parts.size() > 1)
                    throw newRuntimeError(s"Too many parts found for token (use 'parts' function instead) [" +
                        s"id=${tok.getId}, " +
                        s"aliasId=$aliasId" +
                    s"]")

                Z(parts.get(0), f1 || f2)
            })
        }

        //noinspection DuplicatedCode
        def doParts(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(t, f1) = x1()
                val Z(a, f2) = x2()

                val tok = toToken(t)
                val aliasId = toStr(a)

                Z(tok.findPartTokens(aliasId), f1 || f2)
            })
        }

        fun match {
            // Metadata access.
            case "meta_part" ⇒ arg2() match { case (x1, x2) ⇒ stack.push(() ⇒ { val Z(v1, f1) = x1(); val Z(v2, f2) = x2(); Z(toToken(v1).meta[Object](toStr(v2)), f1 || f2) }) }
            case "meta_token" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(tok.meta[Object](toStr(v)), true) }) }
            case "meta_model" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(tok.getModel.meta[Object](toStr(v)), false) }) }
            case "meta_intent" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(termCtx.intentMeta.get(toStr(v)).orNull, false) }) }
            case "meta_req" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(termCtx.req.getRequestData.get(toStr(v)), false) }) }
            case "meta_user" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(termCtx.req.getUser.getMetadata.get(toStr(v)), false) }) }
            case "meta_company" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(termCtx.req.getCompany.getMetadata.get(v), false) }) }
            case "meta_sys" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(U.sysEnv(toStr(v)).orNull, false) }) }
            case "meta_conv" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, _) = x(); Z(termCtx.convMeta.get(toStr(v)).orNull, false) }) }
            case "meta_frag" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(termCtx.fragMeta.get(toStr(v)).orNull, f) }) }

            // Converts JSON to map.
            case "json" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(U.jsonToJavaMap(asStr(v)), f) }) }

            // Inline if-statement.
            case "if" ⇒ doIf()

            // Token functions.
            case "id" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getId, true) }) }
            case "ancestors" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getAncestors, true) }) }
            case "parent" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getParentId, true) }) }
            case "groups" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getGroups, true) }) }
            case "value" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getValue, true) }) }
            case "aliases" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getAliases, true) }) }
            case "start_idx" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getStartCharIndex, true) }) }
            case "end_idx" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getEndCharIndex, true) }) }
            case "this" ⇒ delMarker(); stack.push(() ⇒ Z(tok, true))
            case "part" ⇒ doPart()
            case "parts" ⇒ doParts()

            // Request data.
            case "req_id" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getServerRequestId, false))
            case "req_normtext" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getNormalizedText, false))
            case "req_tstamp" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getReceiveTimestamp, false))
            case "req_addr" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getRemoteAddress.orElse(null), false))
            case "req_agent" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getClientAgent.orElse(null), false))

            // User data.
            case "user_id" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getUser.getId, false))
            case "user_fname" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getUser.getFirstName, false))
            case "user_lname" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getUser.getLastName, false))
            case "user_email" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getUser.getEmail, false))
            case "user_admin" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getUser.isAdmin, false))
            case "user_signup_tstamp" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getUser.getSignupTimestamp, false))

            // Company data.
            case "comp_id" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getId, false))
            case "comp_name" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getName, false))
            case "comp_website" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getWebsite, false))
            case "comp_country" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getCountry, false))
            case "comp_region" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getRegion, false))
            case "comp_city" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getCity, false))
            case "comp_addr" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getAddress, false))
            case "comp_postcode" ⇒ delMarker(); stack.push(() ⇒ Z(termCtx.req.getCompany.getPostalCode, false))

            // String functions.
            case "trim" | "strip" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(toStr(v).trim, f) }) }
            case "uppercase" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(toStr(v).toUpperCase, f) }) }
            case "lowercase" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(toStr(v).toLowerCase, f) }) }
            case "is_alpha" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(StringUtils.isAlpha(toStr(v)), f) }) }
            case "is_alphanum" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(StringUtils.isAlphanumeric(toStr(v)), f) }) }
            case "is_whitespace" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(StringUtils.isWhitespace(toStr(v)), f) }) }
            case "is_num" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(StringUtils.isNumeric(toStr(v)), f) }) }
            case "is_numspace" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(StringUtils.isNumericSpace(toStr(v)), f) }) }
            case "is_alphaspace" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(StringUtils.isAlphaSpace(toStr(v)), f) }) }
            case "is_alphanumspace" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(StringUtils.isAlphanumericSpace(toStr(v)), f) }) }
            case "substring" ⇒
            case "charAt" ⇒
            case "regex" ⇒
            case "soundex" ⇒
            case "split" ⇒ doSplit()
            case "split_trim" ⇒ doSplitTrim()
            case "replace" ⇒

            // Math functions.
            case "abs" ⇒ doAbs()
            case "ceil" ⇒ arg1() match { case item ⇒ stack.push(() ⇒ {
                val Z(v, f) = item()

                Z(Math.ceil(toJDouble(v)), f)
            }) }
            case "floor" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.floor(toJDouble(v)), f) }) }
            case "rint" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.rint(toJDouble(v)), f) }) }
            case "round" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.round(toJDouble(v)), f) }) }
            case "signum" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.signum(toJDouble(v)), f) }) }
            case "sqrt" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.sqrt(toJDouble(v)), f) }) }
            case "cbrt" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.cbrt(toJDouble(v)), f) }) }
            case "acos" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.acos(toJDouble(v)), f) }) }
            case "asin" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.asin(toJDouble(v)), f) }) }
            case "atan" ⇒ arg1() match { case x ⇒ stack.push(() ⇒{ val Z(v, f) = x(); Z( Math.atan(toJDouble(v)), f) }) }
            case "cos" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.cos(toJDouble(v)), f) }) }
            case "sin" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.sin(toJDouble(v)), f) }) }
            case "tan" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.tan(toJDouble(v)), f) }) }
            case "cosh" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.cosh(toJDouble(v)), f) }) }
            case "sinh" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.sinh(toJDouble(v)), f) }) }
            case "tanh" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.tanh(toJDouble(v)), f) }) }
            case "atn2" ⇒ arg2() match { case (x1, x2) ⇒ stack.push(() ⇒ { val Z(v1, f1) = x1(); val Z(v2, f2) = x2(); Z(Math.atan2(toJDouble(v1), toJDouble(v2)), f1 || f2) }) }
            case "degrees" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.toDegrees(toJDouble(v)), f) }) }
            case "radians" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z( Math.toRadians(toJDouble(v)), f) }) }
            case "exp" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.exp(toJDouble(v)), f) }) }
            case "expm1" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.expm1(toJDouble(v)), f) }) }
            case "hypot" ⇒ arg2() match { case (x1, x2) ⇒ stack.push(() ⇒ { val Z(v1, f1) = x1(); val Z(v2, f2) = x2(); Z(Math.hypot(toJDouble(v1), toJDouble(v2)), f1 || f2) }) }
            case "log" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.log(toJDouble(v)), f) }) }
            case "log10" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.log10(toJDouble(v)), f) }) }
            case "log1p" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(Math.log1p(toJDouble(v)), f) }) }
            case "pow" ⇒ arg2() match { case (x1, x2) ⇒ stack.push(() ⇒ { val Z(v1, f1) = x1(); val Z(v2, f2) = x2(); Z(Math.pow(toJDouble(v1), toJDouble(v2)), f1 || f2) }) }
            case "square" ⇒ doSquare()
            case "pi" ⇒ delMarker(); stack.push(() ⇒ Z(Math.PI, false))
            case "euler" ⇒ delMarker(); stack.push(() ⇒ Z(Math.E, false))
            case "rand" ⇒ delMarker(); stack.push(() ⇒ Z(Math.random, false))

            // Collection functions.
            case "list" ⇒ doList()
            case "map" ⇒
            case "get" ⇒ doGet()
            case "index" ⇒
            case "has" ⇒ doHas()
            case "tail" ⇒
            case "add" ⇒
            case "remove" ⇒
            case "first" ⇒
            case "last" ⇒
            case "keys" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(new util.ArrayList(toJMap(v).keySet()), f) }) }
            case "values" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(new util.ArrayList(toJMap(v).values()), f) }) }
            case "take" ⇒
            case "drop" ⇒
            case "size" | "count" | "length" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(toJList(v).size(), f)}) }
            case "reverse" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ {
                val Z(v, f) = x()

                val jl = toJList(v)

                Collections.reverse(jl)

                Z(jl, f)
            }) }
            case "is_empty" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(toJList(v).isEmpty, f) }) }
            case "non_empty" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(!toJList(v).isEmpty, f) }) }
            case "to_string" ⇒ arg1() match { case x ⇒ stack.push(() ⇒ { val Z(v, f) = x(); Z(toJList(v).asScala.map(_.toString).asJava, f) }) }

            // Statistical operations.
            case "avg" ⇒
            case "max" ⇒ // Works for numerics as well.
            case "min" ⇒ // Works for numerics as well.
            case "stdev" ⇒
            case "sum" ⇒

            // Date-time functions.
            case "year" ⇒ delMarker(); stack.push(() ⇒ Z(LocalDate.now.getYear, false)) // 2021.
            case "month" ⇒ delMarker(); stack.push(() ⇒ Z(LocalDate.now.getMonthValue, false)) // 1 ... 12.
            case "day_of_month" ⇒ delMarker(); stack.push(() ⇒ Z(LocalDate.now.getDayOfMonth, false)) // 1 ... 31.
            case "day_of_week" ⇒ delMarker(); stack.push(() ⇒ Z(LocalDate.now.getDayOfWeek.getValue, false))
            case "day_of_year" ⇒ delMarker(); stack.push(() ⇒ Z(LocalDate.now.getDayOfYear, false))
            case "hour" ⇒
            case "minute" ⇒
            case "second" ⇒
            case "week_of_month" ⇒
            case "week_of_year" ⇒
            case "quarter" ⇒
            case "now" ⇒ delMarker(); stack.push(() ⇒ Z(System.currentTimeMillis(), false)) // Epoc time.

            case _ ⇒ throw rtUnknownFunError(fun) // Assertion.
        }
    }
}
