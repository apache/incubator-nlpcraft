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
import org.apache.commons.collections.CollectionUtils
import org.apache.nlpcraft.model.intent.NCDslContext
import org.apache.nlpcraft.model.intent.compiler.{NCDslStackItem ⇒ Z}

import java.lang.{Double ⇒ JDouble, Long ⇒ JLong}
import java.time.temporal.IsoFields
import java.time.{LocalDate, LocalTime}
import java.util
import java.util.{Calendar, Collections, List ⇒ JList, Map ⇒ JMap}
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
    def rtParamTypeError(fun: String, invalid: Object, expectType: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Expected '$expectType' type of parameter for DSL function '$fun()', found: $invalid")
    def rtListTypeError(fun: String, cause: Exception)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Expected uniform list type for DSL function '$fun()', found polymorphic list.", cause)

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
            else if (isJList(v1) && isJList(v2)) CollectionUtils.isEqualCollection(asJList(v1), asJList(v2))
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
        def delMarker(): Unit = require(pop1() == stack.PLIST_MARKER)
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
            if (stack.nonEmpty && stack.top == stack.PLIST_MARKER) {
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

            while (stack.nonEmpty && stack.top != stack.PLIST_MARKER)
                dump += stack.pop()

            delMarker()

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
        
        def doReverse(): Unit = {
            val x = arg1()
            
            stack.push(() ⇒ {
                val Z(v, f) = x()
        
                val jl = toJList(v)
        
                Collections.reverse(jl)
        
                Z(jl, f)
            })
        }
        
        def doMin(): Unit = {
            val x = arg1()
    
            stack.push(() ⇒ {
                val Z(v, f) = x()
                
                val lst = toJList(v).asInstanceOf[util.List[Object]]
                
                try
                    if (lst.isEmpty)
                        Z(0, f)
                    else
                        Z(Collections.min(lst, null), f)
                catch {
                    case e: Exception ⇒ throw rtListTypeError(fun, e)
                }
            })
        }
    
        def doMax(): Unit = {
            val x = arg1()
        
            stack.push(() ⇒ {
                val Z(v, f) = x()
            
                val lst = toJList(v).asInstanceOf[util.List[Object]]
            
                try
                    if (lst.isEmpty)
                        Z(0, f)
                    else
                        Z(Collections.max(lst, null), f)
                catch {
                    case e: Exception ⇒ throw rtListTypeError(fun, e)
                }
            })
        }

        def doSort(): Unit = {
            val x = arg1()
        
            stack.push(() ⇒ {
                val Z(v, f) = x()
            
                val jl = toJList(v)
                
                jl.sort(null) // Use natural order.
            
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
            val (x1, x2) = arg2()

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

        def z[Y](args: () ⇒ Y, body: Y ⇒ Z): Unit = { val x = args(); stack.push(() ⇒ body(x)) }
        def z0(body: () ⇒ Z): Unit = { delMarker(); stack.push(() ⇒ body()) } 

        fun match {
            // Metadata access.
            case "meta_part" ⇒ z[(T, T)](arg2, { x ⇒ val Z(v1, f1) = x._1(); val Z(v2, f2) = x._2(); Z(toToken(v1).meta[Object](toStr(v2)), f1 || f2) })
            case "meta_token" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(tok.meta[Object](toStr(v)), true) })
            case "meta_model" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(tok.getModel.meta[Object](toStr(v)), false) })
            case "meta_intent" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(termCtx.intentMeta.get(toStr(v)).orNull, false) })
            case "meta_req" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(termCtx.req.getRequestData.get(toStr(v)), false) })
            case "meta_user" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(termCtx.req.getUser.getMetadata.get(toStr(v)), false) })
            case "meta_company" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(termCtx.req.getCompany.getMetadata.get(v), false) })
            case "meta_sys" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(U.sysEnv(toStr(v)).orNull, false) })
            case "meta_conv" ⇒ z[T](arg1, { x ⇒ val Z(v, _) = x(); Z(termCtx.convMeta.get(toStr(v)).orNull, false) })
            case "meta_frag" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(termCtx.fragMeta.get(toStr(v)).orNull, f) })

            // Converts JSON to map.
            case "json" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(U.jsonToJavaMap(asStr(v)), f) })

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
            case "this" ⇒ z0(() ⇒ Z(tok, true))
            case "part" ⇒ doPart()
            case "parts" ⇒ doParts()

            // Request data.
            case "req_id" ⇒ z0(() ⇒ Z(termCtx.req.getServerRequestId, false))
            case "req_normtext" ⇒ z0(() ⇒ Z(termCtx.req.getNormalizedText, false))
            case "req_tstamp" ⇒ z0(() ⇒ Z(termCtx.req.getReceiveTimestamp, false))
            case "req_addr" ⇒ z0(() ⇒ Z(termCtx.req.getRemoteAddress.orElse(null), false))
            case "req_agent" ⇒ z0(() ⇒ Z(termCtx.req.getClientAgent.orElse(null), false))

            // User data.
            case "user_id" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getId, false))
            case "user_fname" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getFirstName, false))
            case "user_lname" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getLastName, false))
            case "user_email" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getEmail, false))
            case "user_admin" ⇒ z0(() ⇒ Z(termCtx.req.getUser.isAdmin, false))
            case "user_signup_tstamp" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getSignupTimestamp, false))

            // Company data.
            case "comp_id" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getId, false))
            case "comp_name" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getName, false))
            case "comp_website" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getWebsite, false))
            case "comp_country" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getCountry, false))
            case "comp_region" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getRegion, false))
            case "comp_city" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getCity, false))
            case "comp_addr" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getAddress, false))
            case "comp_postcode" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getPostalCode, false))

            // String functions.
            case "trim" | "strip" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(toStr(v).trim, f) })
            case "uppercase" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(toStr(v).toUpperCase, f) })
            case "lowercase" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(toStr(v).toLowerCase, f) })
            case "is_alpha" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlpha(toStr(v)), f) })
            case "is_alphanum" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlphanumeric(toStr(v)), f) })
            case "is_whitespace" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isWhitespace(toStr(v)), f) })
            case "is_num" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isNumeric(toStr(v)), f) })
            case "is_numspace" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isNumericSpace(toStr(v)), f) })
            case "is_alphaspace" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlphaSpace(toStr(v)), f) })
            case "is_alphanumspace" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlphanumericSpace(toStr(v)), f) }) 
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
            case "floor" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.floor(toJDouble(v)), f) }) 
            case "rint" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.rint(toJDouble(v)), f) }) 
            case "round" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.round(toJDouble(v)), f) }) 
            case "signum" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.signum(toJDouble(v)), f) }) 
            case "sqrt" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.sqrt(toJDouble(v)), f) }) 
            case "cbrt" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.cbrt(toJDouble(v)), f) }) 
            case "acos" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.acos(toJDouble(v)), f) }) 
            case "asin" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.asin(toJDouble(v)), f) }) 
            case "atan" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z( Math.atan(toJDouble(v)), f) }) 
            case "cos" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.cos(toJDouble(v)), f) }) 
            case "sin" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.sin(toJDouble(v)), f) }) 
            case "tan" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.tan(toJDouble(v)), f) }) 
            case "cosh" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.cosh(toJDouble(v)), f) }) 
            case "sinh" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.sinh(toJDouble(v)), f) }) 
            case "tanh" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.tanh(toJDouble(v)), f) }) 
            case "atn2" ⇒ z[(T, T)](arg2, { x ⇒ val Z(v1, f1) = x._1(); val Z(v2, f2) = x._2(); Z(Math.atan2(toJDouble(v1), toJDouble(v2)), f1 || f2) }) 
            case "degrees" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.toDegrees(toJDouble(v)), f) }) 
            case "radians" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z( Math.toRadians(toJDouble(v)), f) }) 
            case "exp" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.exp(toJDouble(v)), f) }) 
            case "expm1" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.expm1(toJDouble(v)), f) }) 
            case "hypot" ⇒ z[(T, T)](arg2, { x ⇒ val Z(v1, f1) = x._1(); val Z(v2, f2) = x._2(); Z(Math.hypot(toJDouble(v1), toJDouble(v2)), f1 || f2) }) 
            case "log" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.log(toJDouble(v)), f) }) 
            case "log10" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.log10(toJDouble(v)), f) }) 
            case "log1p" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.log1p(toJDouble(v)), f) }) 
            case "pow" ⇒ z[(T, T)](arg2, { x ⇒ val Z(v1, f1) = x._1(); val Z(v2, f2) = x._2(); Z(Math.pow(toJDouble(v1), toJDouble(v2)), f1 || f2) }) 
            case "square" ⇒ doSquare()
            case "pi" ⇒ z0(() ⇒ Z(Math.PI, false))
            case "euler" ⇒ z0(() ⇒ Z(Math.E, false))
            case "rand" ⇒ z0(() ⇒ Z(Math.random, false))

            // Collection functions.
            case "list" ⇒ doList()
            case "get" ⇒ doGet()
            case "has" ⇒ doHas()
            case "first" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); val lst = toJList(v); Z(if (lst.isEmpty) null else lst.get(0).asInstanceOf[Object], f)})
            case "last" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); val lst = toJList(v); Z(if (lst.isEmpty) null else lst.get(lst.size() - 1).asInstanceOf[Object], f)}) 
            case "keys" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(new util.ArrayList(toJMap(v).keySet()), f) }) 
            case "values" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(new util.ArrayList(toJMap(v).values()), f) }) 
            case "size" | "count" | "length" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(toJList(v).size(), f)})
            case "reverse" ⇒ doReverse()
            case "sort" ⇒ doSort()
            case "is_empty" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(toJList(v).isEmpty, f) }) 
            case "non_empty" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(!toJList(v).isEmpty, f) }) 
            case "to_string" ⇒ z[T](arg1, { x ⇒ val Z(v, f) = x(); Z(toJList(v).asScala.map(_.toString).asJava, f) }) 

            // Statistical operations on lists.
            case "avg" ⇒
            case "max" ⇒ doMin()
            case "min" ⇒ doMax()
            case "stdev" ⇒
            case "sum" ⇒

            // Date-time functions.
            case "year" ⇒ z0(() ⇒ Z(LocalDate.now.getYear, false)) // 2021.
            case "month" ⇒ z0(() ⇒ Z(LocalDate.now.getMonthValue, false)) // 1 ... 12.
            case "day_of_month" ⇒ z0(() ⇒ Z(LocalDate.now.getDayOfMonth, false)) // 1 ... 31.
            case "day_of_week" ⇒ z0(() ⇒ Z(LocalDate.now.getDayOfWeek.getValue, false))
            case "day_of_year" ⇒ z0(() ⇒ Z(LocalDate.now.getDayOfYear, false))
            case "hour" ⇒ z0(() ⇒ Z(LocalTime.now.getHour, false))
            case "minute" ⇒ z0(() ⇒ Z(LocalTime.now.getMinute, false))
            case "second" ⇒ z0(() ⇒ Z(LocalTime.now.getSecond, false))
            case "week_of_month" ⇒ z0(() ⇒ Z(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH), false))
            case "week_of_year" ⇒ z0(() ⇒ Z(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), false))
            case "quarter" ⇒ z0(() ⇒ Z(LocalDate.now().get(IsoFields.QUARTER_OF_YEAR), false))
            case "now" ⇒ z0(() ⇒ Z(System.currentTimeMillis(), false)) // Epoc time.

            case _ ⇒ throw rtUnknownFunError(fun) // Assertion.
        }
    }
}
