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
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlStack, NCIdlStackItem ⇒ Z, NCIdlStackType}

import java.lang.{Byte ⇒ JByte, Double ⇒ JDouble, Float ⇒ JFloat, Integer ⇒ JInt, Long ⇒ JLong, Short ⇒ JShort}
import java.time.temporal.IsoFields
import java.time.{LocalDate, LocalTime}
import java.util
import java.util.{Calendar, Collections, Collection ⇒ JColl, List ⇒ JList, Map ⇒ JMap}
import scala.collection.JavaConverters._

trait NCIdlCompilerBase {
    type S = NCIdlStack
    type ST = NCIdlStackType
    type SI = (NCToken, S, NCIdlContext) ⇒ Unit

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


    /**
     * Check if given object is mathematically an integer number.
     *
     * @param v
     * @return
     */
    //noinspection ComparingUnrelatedTypes
    def isInt(v: Object): Boolean = v.isInstanceOf[JLong] || v.isInstanceOf[JInt] || v.isInstanceOf[JByte] || v.isInstanceOf[JShort]

    /**
     * Check if given object is mathematically an real number.
     *
     * @param v
     * @return
     */
    //noinspection ComparingUnrelatedTypes
    def isReal(v: Object): Boolean = v.isInstanceOf[JDouble] || v.isInstanceOf[JFloat]

    /**
     *
     * @param v
     * @return
     */
    def asInt(v: Object): JLong = v match {
        case l: JLong ⇒ l
        case i: JInt ⇒ i.longValue()
        case b: JByte ⇒ b.longValue()
        case s: JShort ⇒ s.longValue()
        case _ ⇒ throw new AssertionError(s"Unexpected int value: $v")
    }

    /**
     *
     * @param v
     * @return
     */
    def asReal(v: Object): JDouble = v match {
        case d: JDouble ⇒ d
        case f: JFloat ⇒ f.doubleValue()
        case _ ⇒ throw new AssertionError(s"Unexpected real value: $v")
    }

    def box(v: Object): Object = {
        if (v == null)
            null
        else if (isInt(v))
            asInt(v)
        else if (isReal(v))
            asReal(v)
        else if (isJList(v) || isJMap(v))
            v
        else if (isJColl(v)) // Convert any other Java collections to ArrayList.
            new java.util.ArrayList(asJColl(v)).asInstanceOf[Object]
        else
            v
    }

    //noinspection ComparingUnrelatedTypes
    def isBool(v: Object): Boolean = v.isInstanceOf[Boolean]
    def isJList(v: Object): Boolean = v.isInstanceOf[JList[_]]
    def isJColl(v: Object): Boolean = v.isInstanceOf[JColl[_]]
    def isJMap(v: Object): Boolean = v.isInstanceOf[JMap[_, _]]
    def isStr(v: Object): Boolean = v.isInstanceOf[String]
    def isToken(v: Object): Boolean = v.isInstanceOf[NCToken]

    def asJList(v: Object): JList[_] = v.asInstanceOf[JList[_]]
    def asJColl(v: Object): JColl[_] = v.asInstanceOf[JColl[_]]
    def asJMap(v: Object): JMap[_, _] = v.asInstanceOf[JMap[_, _]]
    def asStr(v: Object): String = v.asInstanceOf[String]
    def asToken(v: Object): NCToken = v.asInstanceOf[NCToken]
    def asBool(v: Object): Boolean = v.asInstanceOf[Boolean]

    // Runtime errors.
    def rtUnaryOpError(op: String, v: Object)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Unexpected '$op' IDL operation for value: $v")
    def rtBinaryOpError(op: String, v1: Object, v2: Object)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Unexpected '$op' IDL operation for values: $v1, $v2")
    def rtUnknownFunError(fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Unknown IDL function: $fun()")
    def rtMinParamNumError(min: Int, fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Invalid number of parameters for function ($min is required): $fun()")
    def rtParamNumError(fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Invalid number of parameters for IDL function: $fun()")
    def rtParamTypeError(fun: String, invalid: Object, expectType: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Expected '$expectType' type of parameter for IDL function '$fun()', found: $invalid")
    def rtListTypeError(fun: String, cause: Exception)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Expected uniform list type for IDL function '$fun()', found polymorphic list.", cause)

    /**
     *
     * @param stack
     * @return
     */
    def pop1()(implicit stack: S, ctx: PRC): ST = {
        require(stack.nonEmpty, ctx.getText)

        stack.pop()
    }

    /**
     *
     * @param stack
     * @return
     */
    def pop2()(implicit stack: S, ctx: PRC): (ST, ST) = {
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
    def pop3()(implicit stack: S, ctx: PRC): (ST, ST, ST) = {
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
    def parseCompExpr(lt: TN, gt: TN, lteq: TN, gteq: TN)(implicit ctx: PRC): SI = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        if (lt != null)
            stack.push(() ⇒ {
                val Z(v1, n1) = x1()
                val Z(v2, n2) = x2()

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) < asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) < asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) < asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) < asReal(v2)
                    else
                        throw rtBinaryOpError("<", v1, v2)

                Z(f, n1 + n2)
            })
        else if (gt != null)
            stack.push(() ⇒ {
                val Z(v1, n1) = x1()
                val Z(v2, n2) = x2()

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) > asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) > asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) > asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) > asReal(v2)
                    else
                        throw rtBinaryOpError(">", v1, v2)

                Z(f, n1 + n2)
            })
        else if (lteq != null)
            stack.push(() ⇒ {
                val Z(v1, n1) = x1()
                val Z(v2, n2) = x2()

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) <= asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) <= asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) <= asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) <= asReal(v2)
                    else
                        throw rtBinaryOpError("<=", v1, v2)

                Z(f, n1 + n2)
            })
        else {
            require(gteq != null)

            stack.push(() ⇒ {
                val Z(v1, n1) = x1()
                val Z(v2, n2) = x2()

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) >= asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) >= asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) >= asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) >= asReal(v2)
                    else
                        throw rtBinaryOpError(">=", v1, v2)

                Z(f, n1 + n2)
            })
        }
    }

    /**
     *
     * @param mult
     * @param mod
     * @param div
     */
    def parseMultDivModExpr(mult: TN, mod: TN, div: TN)(implicit ctx: PRC): SI = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        if (mult != null)
            stack.push(() ⇒ {
                val Z(v1, n1) = x1()
                val Z(v2, n2) = x2()

                if (isInt(v1) && isInt(v2)) Z(asInt(v1) * asInt(v2), n1 + n2)
                else if (isInt(v1) && isReal(v2)) Z(asInt(v1) * asReal(v2), n1 + n2)
                else if (isReal(v1) && isInt(v2)) Z(asReal(v1) * asInt(v2), n1 + n2)
                else if (isReal(v1) && isReal(v2)) Z(asReal(v1) * asReal(v2), n1 + n2)
                else
                    throw rtBinaryOpError("*", v1, v2)
            })
        else if (mod != null)
            stack.push(() ⇒ {
                val Z(v1, n1) = x1()
                val Z(v2, n2) = x2()

                if (isInt(v1) && isInt(v2)) Z(asInt(v1) % asInt(v2), n1 + n2)
                else
                    throw rtBinaryOpError("%", v1, v2)
            })
        else {
            assert(div != null)

            stack.push(() ⇒ {
                val Z(v1, n1) = x1()
                val Z(v2, n2) = x2()

                if (isInt(v1) && isInt(v2)) Z(asInt(v1) / asInt(v2), n1 + n2)
                else if (isInt(v1) && isReal(v2)) Z(asInt(v1) / asReal(v2), n1 + n2)
                else if (isReal(v1) && isInt(v2)) Z(asReal(v1) / asInt(v2), n1 + n2)
                else if (isReal(v1) && isReal(v2)) Z(asReal(v1) / asReal(v2), n1 + n2)
                else
                    throw rtBinaryOpError("/", v1, v2)
            })
        }
    }

    /**
     *
     * @param and
     * @param or
     * @return
     */
    def parseAndOrExpr(and: TN, or: TN)(implicit ctx: PRC): SI = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        stack.push(() ⇒ {
            val (op, flag) = if (and != null) ("&&", false) else ("||", true)

            val Z(v1, n1) = x1()

            if (!isBool(v1))
                throw rtBinaryOpError(op, v1, x2().value)

            // NOTE: check v1 first and only if it is {true|false} check the v2.
            if (asBool(v1) == flag)
                Z(flag, n1)
            else {
                val Z(v2, n2) = x2()

                if (!isBool(v2))
                    throw rtBinaryOpError(op, v2, v1)

                Z(asBool(v2), n1 + n2)
            }
        })
    }

    /**
     *
     * @param eq
     * @param neq
     * @return
     */
    def parseEqNeqExpr(eq: TN, neq: TN)(implicit ctx: PRC): SI = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        def doEq(op: String, v1: Object, v2: Object): Boolean = {
            if (v1 == null && v2 == null) true
            else if ((v1 == null && v2 != null) || (v1 != null && v2 == null)) false
            else if (isInt(v1) && isInt(v2)) asInt(v1) == asInt(v2)
            else if (isReal(v1) && isReal(v2)) asReal(v1) == asReal(v2)
            else if (isBool(v1) && isBool(v2)) asBool(v1) == asBool(v2)
            else if (isStr(v1) && isStr(v2)) asStr(v1) == asStr(v2)
            else if (isJList(v1) && isJList(v2)) CollectionUtils.isEqualCollection(asJList(v1), asJList(v2))
            else if ((isInt(v1) && isReal(v2)) || (isReal(v1) && isInt(v2))) false
            else
                throw rtBinaryOpError(op, v1, v2)
        }

        stack.push(() ⇒ {
            val Z(v1, n1) = x1()
            val Z(v2, n2) = x2()

            val f =
                if (eq != null)
                    doEq("==", v1, v2)
                else {
                    assert(neq != null)

                    !doEq("!='", v1, v2)
                }

            Z(f, n1 + n2)
        })
    }

    /**
     *
     * @param plus
     * @param minus
     */
    def parsePlusMinusExpr(plus: TN, minus: TN)(implicit ctx: PRC): SI = (_, stack: S, _) ⇒ {
        val (x1, x2) = pop2()(stack, ctx)

        def extract(): (Object, Object, Int) = {
            val Z(v1, n1) = x1()
            val Z(v2, n2) = x2()

            (v1, v2, n1 + n2)
        }

        if (plus != null)
            stack.push(() ⇒ {
                val (v1, v2, n) = extract()

                if (isStr(v1) && isStr(v2)) Z(asStr(v1) + asStr(v2), n)
                else if (isInt(v1) && isInt(v2)) Z(asInt(v1) + asInt(v2), n)
                else if (isInt(v1) && isReal(v2)) Z(asInt(v1) + asReal(v2), n)
                else if (isReal(v1) && isInt(v2)) Z(asReal(v1) + asInt(v2), n)
                else if (isReal(v1) && isReal(v2)) Z(asReal(v1) + asReal(v2), n)
                else
                    throw rtBinaryOpError("+", v1, v2)
            })
        else {
            assert(minus != null)

            stack.push(() ⇒ {
                val (v1, v2, n) = extract()

                if (isInt(v1) && isInt(v2)) Z(asInt(v1) - asInt(v2), n)
                else if (isInt(v1) && isReal(v2)) Z(asInt(v1) - asReal(v2), n)
                else if (isReal(v1) && isInt(v2)) Z(asReal(v1) - asInt(v2), n)
                else if (isReal(v1) && isReal(v2)) Z(asReal(v1) - asReal(v2), n)
                else
                    throw rtBinaryOpError("-", v1, v2)
            })
        }
    }

    /**
     * @param minus
     * @param not
     * @return
     */
    def parseUnaryExpr(minus: TN, not: TN)(implicit ctx: PRC): SI = (_, stack: S, _) ⇒ {
        val x = pop1()(stack, ctx)

        if (minus != null)
            stack.push(() ⇒ {
                val Z(v, n) = x()

                if (isReal(v)) Z(-asReal(v), n)
                else if (isInt(v)) Z(-asInt(v), n)
                else
                    throw rtUnaryOpError("-", v)
            })
        else {
            assert(not != null)

            stack.push(() ⇒ {
                val Z(v, n) = x()

                if (isBool(v)) Z(!asBool(v), n)
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
    def parseAtom(txt: String)(implicit ctx: PRC): SI = {
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

        (_, stack, _) ⇒ stack.push(() ⇒ Z(atom, 0))
    }

    /**
     *
     * @param id
     * @return
     */
    def parseCallExpr(id: TN)(implicit ctx: PRC): SI = (tok, stack: S, termCtx) ⇒ {
        implicit val evidence: S = stack
    
        val fun = id.getText
    
        def ensureStack(min: Int): Unit = if (stack.size < min) throw rtMinParamNumError(min, fun)
        def popMarker(): Unit = require(pop1() == stack.PLIST_MARKER)
        def arg[X](min: Int, f: () ⇒ X): X = {
            ensureStack(min + 1) // +1 for the frame marker.
            
            val x = f()
        
            // Make sure to pop up the parameter list stack frame marker.
            popMarker()
            
            x
        }
        def arg1(): ST = arg(1, pop1)
        def arg2(): (ST, ST) = arg(2, pop2)
        def arg3(): (ST, ST, ST) = arg(3, pop3)
        def arg1Tok(): ST =
            if (stack.nonEmpty && stack.top == stack.PLIST_MARKER) {
                popMarker()
            
                () ⇒ Z(tok, 1)
            }
            else
                arg1()
        def toX[T](typ: String, v: Object, is: Object ⇒ Boolean, as: Object ⇒ T): T = {
            if (!is(v))
                throw rtParamTypeError(fun, v, typ)

            as(v)
        }
        def toStr(v: Object): String = toX("string", v, isStr, asStr)
        def toJDouble(v: Object): JDouble = toX("double", v, isReal, asReal)
        def toJList(v: Object): JList[_] = toX("list", v, isJList, asJList)
        def toJMap(v: Object): JMap[_, _] = toX("map", v, isJMap, asJMap)
        def toToken(v: Object): NCToken = toX("token", v, isToken, asToken)
        def toBool(v: Object): Boolean = toX("boolean", v, isBool, asBool)
        def toJDoubleSafe(v: Object): JDouble = {
            if (isReal(v))
                asReal(v)
            else if (isInt(v))
                asInt(v).toDouble
            else
                throw rtParamTypeError(fun, v, "double")
        }
    
        def doSplit(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () ⇒ {
                    val Z(v1, n1) = x1()
                    val Z(v2, n2) = x2()

                   Z(util.Arrays.asList(toStr(v1).split(toStr(v2))), n1 + n2)
                }
            )
        }

        def doSplitTrim(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () ⇒ {
                    val Z(v1, n1) = x1()
                    val Z(v2, n2) = x2()

                    Z(util.Arrays.asList(toStr(v1).split(toStr(v2)).toList.map(_.strip)), n1 + n2)
                }
            )
        }

        def doList(): Unit = {
            val dump = new S() // Empty list is allowed.

            while (stack.nonEmpty && stack.top != stack.PLIST_MARKER)
                dump += stack.pop()

            popMarker()

            stack.push(() ⇒ {
                val jl = new util.ArrayList[Object]()
                var z = 0

                dump.foreach { x ⇒
                    val Z(v, n) = x()

                    z += n

                    jl.add(v)
                }

                Z(jl, z)
            })
        }
        
        def doReverse(): Unit = {
            val x = arg1()
            
            stack.push(() ⇒ {
                val Z(v, n) = x()
        
                val jl = toJList(v)
        
                Collections.reverse(jl)
        
                Z(jl, n)
            })
        }
        
        def doMin(): Unit = {
            val x = arg1()
    
            stack.push(() ⇒ {
                val Z(v, n) = x()
                
                val lst = toJList(v).asInstanceOf[util.List[Object]]
                
                try
                    if (lst.isEmpty)
                        throw newRuntimeError(s"Unexpected empty list in IDL function '$fun()'.")
                    else 
                        Z(Collections.min(lst, null), n)
                catch {
                    case e: Exception ⇒ throw rtListTypeError(fun, e)
                }
            })
        }
    
        def doMax(): Unit = {
            val x = arg1()
        
            stack.push(() ⇒ {
                val Z(v, n) = x()
            
                val lst = toJList(v).asInstanceOf[util.List[Object]]
            
                try
                    if (lst.isEmpty)
                        throw newRuntimeError(s"Unexpected empty list in IDL function '$fun()'.")
                    else
                        Z(Collections.max(lst, null), n)
                catch {
                    case e: Exception ⇒ throw rtListTypeError(fun, e)
                }
            })
        }

        def doSort(): Unit = {
            val x = arg1()
        
            stack.push(() ⇒ {
                val Z(v, n) = x()
            
                val jl = toJList(v)
                
                jl.sort(null) // Use natural order.
            
                Z(jl, n)
            })
        }

        def doHas(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(lst, n1) = x1()
                val Z(obj, n2) = x2()

                Z(toJList(lst).contains(box(obj)), n1 + n2)
            })
        }

        def doHasAll(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(lst1, n1) = x1()
                val Z(lst2, n2) = x2()

                Z(toJList(lst1).containsAll(toJList(lst2)), n1 + n2)
            })
        }

        def doHasAny(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(lst1, n1) = x1()
                val Z(lst2, n2) = x2()

                Z(CollectionUtils.containsAny(toJList(lst1), toJList(lst2)), n1 + n2)
            })
        }

        def doGet(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(col, n1) = x1()
                val Z(key, n2) = x2()
                val n = n1 + n2

                if (isJList(col)) {
                    if (isInt(key))
                        Z(asJList(col).get(asInt(key).intValue()).asInstanceOf[Object], n)
                    else
                        throw rtParamTypeError(fun, key, "numeric")
                }
                else if (isJMap(col))
                    Z(asJMap(col).get(box(key)).asInstanceOf[Object], n)
                else
                    throw rtParamTypeError(fun, col, "list or map")
            })
        }

        def doAbs(): Unit = arg1() match {
            case x ⇒ stack.push(() ⇒ {
                val Z(v, n) = x()

                v match {
                    case a: JLong ⇒ Z(Math.abs(a), n)
                    case a: JDouble ⇒ Z(Math.abs(a), n)
                    case _ ⇒ throw rtParamTypeError(fun, v, "numeric")
                }
            })
        }

        def doSquare(): Unit = arg1() match {
            case x ⇒ stack.push(() ⇒ {
                val Z(v, n) = x()

                v match {
                    case a: JLong ⇒ Z(a * a, n)
                    case a: JDouble ⇒ Z(a * a, n)
                    case _ ⇒ throw rtParamTypeError(fun, v, "numeric")
                }
            })
        }

        def doIf(): Unit = {
            val (x1, x2, x3) = arg3()

            stack.push(() ⇒ {
                val Z(v1, n1) = x1()

                if (toBool(v1)) {
                    val Z(v2, n2) = x2()

                    Z(v2, n1 + n2)
                }
                else {
                    val Z(v3, n3) = x3()

                    Z(v3, n1 + n3)
                }
            })
        }

        /**
         *
         * @param whole
         * @param aliasId
         * @return
         */
        def findPart(whole: NCToken, aliasId: String): NCToken = {
            val parts = whole.findPartTokens(aliasId)

            if (parts.isEmpty)
                throw newRuntimeError(s"Cannot find part for token [" +
                    s"tokenId=${whole.getId}, " +
                    s"partId=$aliasId" +
                    s"]")
            else if (parts.size() > 1)
                throw newRuntimeError(s"Too many parts found for token (use 'parts' function instead) [" +
                    s"tokenId=${whole.getId}, " +
                    s"partId=$aliasId" +
                    s"]")
            else
                parts.get(0)

        }

        //noinspection DuplicatedCode
        def doFindPart(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(tok, n1) = x1()
                val Z(aliasId, n2) = x2() // Token alias or token ID.

                Z(box(findPart(toToken(tok), toStr(aliasId))), n1 + n2)
            })
        }
        
        def doPartMeta(): Unit = {
            val (x1, x2) = arg2()
    
            stack.push(() ⇒ {
                val Z(aliasId, n1) = x1() // Token alias or token ID.
                val Z(key, n2) = x2()

                Z(box(findPart(tok, toStr(aliasId)).meta[Object](toStr(key))), n1 + n2)
            })
        }

        //noinspection DuplicatedCode
        def doFindParts(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() ⇒ {
                val Z(t, n1) = x1()
                val Z(a, n2) = x2()

                val tok = toToken(t)
                val aliasId = toStr(a)

                Z(tok.findPartTokens(aliasId), n1 + n2)
            })
        }

        def z[Y](args: () ⇒ Y, body: Y ⇒ Z): Unit = { val x = args(); stack.push(() ⇒ body(x)) }
        def z0(body: () ⇒ Z): Unit = { popMarker(); stack.push(() ⇒ body()) }

        fun match {
            // Metadata access.
            case "meta_part" ⇒ doPartMeta()
            case "meta_token" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(tok.meta[Object](toStr(v))), 1) })
            case "meta_model" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(tok.getModel.meta[Object](toStr(v))), 0) })
            case "meta_req" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(termCtx.req.getRequestData.get(toStr(v))), 0) })
            case "meta_user" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(termCtx.req.getUser.meta(toStr(v))), 0) })
            case "meta_company" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(termCtx.req.getCompany.meta(toStr(v))), 0) })
            case "meta_intent" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(termCtx.intentMeta.get(toStr(v)).orNull), 0) })
            case "meta_conv" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(termCtx.convMeta.get(toStr(v)).orNull), 0) })
            case "meta_frag" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(box(termCtx.fragMeta.get(toStr(v)).orNull), f) })
            case "meta_sys" ⇒ z[ST](arg1, { x ⇒ val Z(v, _) = x(); Z(box(U.sysEnv(toStr(v)).orNull), 0) })

            // Converts JSON to map.
            case "json" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(U.jsonToJavaMap(asStr(v)), f) })

            // Inline if-statement.
            case "if" ⇒ doIf()

            // Token functions.
            case "id" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getId, 1) }) }
            case "ancestors" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getAncestors, 1) }) }
            case "parent" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getParentId, 1) }) }
            case "groups" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getGroups, 1) }) }
            case "value" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getValue, 1) }) }
            case "aliases" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getAliases, 1) }) }
            case "start_idx" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getStartCharIndex, 1) }) }
            case "end_idx" ⇒ arg1Tok() match { case x ⇒ stack.push(() ⇒ { Z(toToken(x().value).getEndCharIndex, 1) }) }
            case "token" ⇒ z0(() ⇒ Z(tok, 1))
            case "find_part" ⇒ doFindPart()
            case "find_parts" ⇒ doFindParts()

            // Request data.
            case "req_id" ⇒ z0(() ⇒ Z(termCtx.req.getServerRequestId, 0))
            case "req_normtext" ⇒ z0(() ⇒ Z(termCtx.req.getNormalizedText, 0))
            case "req_tstamp" ⇒ z0(() ⇒ Z(termCtx.req.getReceiveTimestamp, 0))
            case "req_addr" ⇒ z0(() ⇒ Z(termCtx.req.getRemoteAddress.orElse(null), 0))
            case "req_agent" ⇒ z0(() ⇒ Z(termCtx.req.getClientAgent.orElse(null), 0))

            // User data.
            case "user_id" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getId, 0))
            case "user_fname" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getFirstName.orElse(null), 0))
            case "user_lname" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getLastName.orElse(null), 0))
            case "user_email" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getEmail.orElse(null), 0))
            case "user_admin" ⇒ z0(() ⇒ Z(termCtx.req.getUser.isAdmin, 0))
            case "user_signup_tstamp" ⇒ z0(() ⇒ Z(termCtx.req.getUser.getSignupTimestamp, 0))

            // Company data.
            case "comp_id" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getId, 0))
            case "comp_name" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getName, 0))
            case "comp_website" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getWebsite.orElse(null), 0))
            case "comp_country" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getCountry.orElse(null), 0))
            case "comp_region" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getRegion.orElse(null), 0))
            case "comp_city" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getCity.orElse(null), 0))
            case "comp_addr" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getAddress.orElse(null), 0))
            case "comp_postcode" ⇒ z0(() ⇒ Z(termCtx.req.getCompany.getPostalCode.orElse(null), 0))

            // String functions.
            case "trim" | "strip" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(toStr(v).trim, f) })
            case "uppercase" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(toStr(v).toUpperCase, f) })
            case "lowercase" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(toStr(v).toLowerCase, f) })
            case "is_alpha" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlpha(toStr(v)), f) })
            case "is_alphanum" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlphanumeric(toStr(v)), f) })
            case "is_whitespace" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isWhitespace(toStr(v)), f) })
            case "is_num" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isNumeric(toStr(v)), f) })
            case "is_numspace" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isNumericSpace(toStr(v)), f) })
            case "is_alphaspace" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlphaSpace(toStr(v)), f) })
            case "is_alphanumspace" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(StringUtils.isAlphanumericSpace(toStr(v)), f) })
            case "split" ⇒ doSplit()
            case "split_trim" ⇒ doSplitTrim()

            // Math functions.
            case "abs" ⇒ doAbs()
            case "ceil" ⇒ arg1() match { case item ⇒ stack.push(() ⇒ {
                val Z(v, f) = item()

                Z(Math.ceil(toJDouble(v)), f)
            }) }
            case "floor" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.floor(toJDoubleSafe(v)), f) })
            case "rint" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.rint(toJDoubleSafe(v)), f) })
            case "round" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.round(toJDoubleSafe(v)), f) })
            case "signum" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.signum(toJDoubleSafe(v)), f) })
            case "sqrt" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.sqrt(toJDoubleSafe(v)), f) })
            case "cbrt" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.cbrt(toJDoubleSafe(v)), f) })
            case "acos" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.acos(toJDoubleSafe(v)), f) })
            case "asin" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.asin(toJDoubleSafe(v)), f) })
            case "atan" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z( Math.atan(toJDoubleSafe(v)), f) })
            case "cos" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.cos(toJDoubleSafe(v)), f) })
            case "sin" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.sin(toJDoubleSafe(v)), f) })
            case "tan" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.tan(toJDoubleSafe(v)), f) })
            case "cosh" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.cosh(toJDoubleSafe(v)), f) })
            case "sinh" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.sinh(toJDoubleSafe(v)), f) })
            case "tanh" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.tanh(toJDoubleSafe(v)), f) })
            case "atn2" ⇒ z[(ST, ST)](arg2, { x ⇒ val Z(v1, n1) = x._1(); val Z(v2, n2) = x._2(); Z(Math.atan2(toJDoubleSafe(v1), toJDoubleSafe(v2)), n1 + n2) })
            case "degrees" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.toDegrees(toJDoubleSafe(v)), f) })
            case "radians" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z( Math.toRadians(toJDoubleSafe(v)), f) })
            case "exp" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.exp(toJDoubleSafe(v)), f) })
            case "expm1" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.expm1(toJDoubleSafe(v)), f) })
            case "hypot" ⇒ z[(ST, ST)](arg2, { x ⇒ val Z(v1, n1) = x._1(); val Z(v2, n2) = x._2(); Z(Math.hypot(toJDoubleSafe(v1), toJDoubleSafe(v2)), n1 + n2) })
            case "log" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.log(toJDoubleSafe(v)), f) })
            case "log10" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.log10(toJDoubleSafe(v)), f) })
            case "log1p" ⇒ z[ST](arg1, { x ⇒ val Z(v, f) = x(); Z(Math.log1p(toJDoubleSafe(v)), f) })
            case "pow" ⇒ z[(ST, ST)](arg2, { x ⇒ val Z(v1, f1) = x._1(); val Z(v2, f2) = x._2(); Z(Math.pow(toJDoubleSafe(v1), toJDoubleSafe(v2)), f1 + f2 + 1) })
            case "square" ⇒ doSquare()
            case "pi" ⇒ z0(() ⇒ Z(Math.PI, 0))
            case "euler" ⇒ z0(() ⇒ Z(Math.E, 0))
            case "rand" ⇒ z0(() ⇒ Z(Math.random, 0))

            // Collection functions.
            case "list" ⇒ doList()
            case "get" ⇒ doGet()
            case "has" ⇒ doHas()
            case "has_any" ⇒ doHasAny()
            case "has_all" ⇒ doHasAll()
            case "first" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); val lst = toJList(v); Z(if (lst.isEmpty) null else lst.get(0).asInstanceOf[Object], n)})
            case "last" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); val lst = toJList(v); Z(if (lst.isEmpty) null else lst.get(lst.size() - 1).asInstanceOf[Object], n)})
            case "keys" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); Z(new util.ArrayList(toJMap(v).keySet()), n) })
            case "values" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); Z(new util.ArrayList(toJMap(v).values()), n) })
            case "size" | "count" | "length" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); Z(toJList(v).size(), n)})
            case "reverse" ⇒ doReverse()
            case "sort" ⇒ doSort()
            case "is_empty" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); Z(toJList(v).isEmpty, n) })
            case "non_empty" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); Z(!toJList(v).isEmpty, n) })
            case "to_string" ⇒ z[ST](arg1, { x ⇒ val Z(v, n) = x(); Z(toJList(v).asScala.map(_.toString).asJava, n) })

            // Statistical operations on lists.
            case "max" ⇒ doMax()
            case "min" ⇒ doMin()

            // Date-time functions.
            case "year" ⇒ z0(() ⇒ Z(LocalDate.now.getYear, 0)) // 2021.
            case "month" ⇒ z0(() ⇒ Z(LocalDate.now.getMonthValue, 0)) // 1 ... 12.
            case "day_of_month" ⇒ z0(() ⇒ Z(LocalDate.now.getDayOfMonth, 0)) // 1 ... 31.
            case "day_of_week" ⇒ z0(() ⇒ Z(LocalDate.now.getDayOfWeek.getValue, 0))
            case "day_of_year" ⇒ z0(() ⇒ Z(LocalDate.now.getDayOfYear, 0))
            case "hour" ⇒ z0(() ⇒ Z(LocalTime.now.getHour, 0))
            case "minute" ⇒ z0(() ⇒ Z(LocalTime.now.getMinute, 0))
            case "second" ⇒ z0(() ⇒ Z(LocalTime.now.getSecond, 0))
            case "week_of_month" ⇒ z0(() ⇒ Z(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH), 0))
            case "week_of_year" ⇒ z0(() ⇒ Z(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), 0))
            case "quarter" ⇒ z0(() ⇒ Z(LocalDate.now().get(IsoFields.QUARTER_OF_YEAR), 0))
            case "now" ⇒ z0(() ⇒ Z(System.currentTimeMillis(), 0)) // Epoc time.

            case _ ⇒ throw rtUnknownFunError(fun) // Assertion.
        }
    }
}
