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

package org.apache.nlpcraft.model.intent.compiler

import org.apache.commons.lang3.StringUtils
import org.apache.nlpcraft.common.{NCE, U}
import org.apache.nlpcraft.model.NCToken
import org.antlr.v4.runtime.{ParserRuleContext => PRC}
import org.antlr.v4.runtime.tree.{TerminalNode => TN}
import org.apache.commons.collections.CollectionUtils
import org.apache.nlpcraft.model.intent.{NCIdlContext, NCIdlStack, NCIdlStackType, NCIdlStackItem => Z}

import java.lang.{Byte => JByte, Double => JDouble, Float => JFloat, Integer => JInt, Long => JLong, Short => JShort}
import java.time.temporal.IsoFields
import java.time.{LocalDate, LocalTime}
import java.util
import java.util.{Calendar, Collections, Collection => JColl, List => JList, Map => JMap}

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.jdk.CollectionConverters.SeqHasAsJava

trait NCIdlCompilerBase {
    type S = NCIdlStack
    type ST = NCIdlStackType
    type SI = (NCToken, S, NCIdlContext) => Unit

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
        case l: JLong => l
        case i: JInt => i.longValue()
        case b: JByte => b.longValue()
        case s: JShort => s.longValue()
        case _ => throw new AssertionError(s"Unexpected int value: $v")
    }

    /**
     *
     * @param v
     * @return
     */
    def asReal(v: Object): JDouble = v match {
        case l: JLong => l.doubleValue()
        case i: JInt => i.doubleValue()
        case b: JByte => b.doubleValue()
        case s: JShort => s.doubleValue()
        case d: JDouble => d
        case f: JFloat => f.doubleValue()
        case _ => throw new AssertionError(s"Unexpected real value: $v")
    }

    def box(v: Object): Object = {
        if (v == null)
            null
        else if (isInt(v))
            asInt(v)
        else if (isReal(v))
            asReal(v)
        else if (isList(v) || isMap(v))
            v
        else if (isJColl(v)) // Convert any other Java collections to ArrayList.
            new java.util.ArrayList(asJColl(v)).asInstanceOf[Object]
        else
            v
    }

    //noinspection ComparingUnrelatedTypes
    def isBool(v: Object): Boolean = v.isInstanceOf[Boolean]
    def isList(v: Object): Boolean = v.isInstanceOf[JList[_]]
    def isJColl(v: Object): Boolean = v.isInstanceOf[JColl[_]]
    def isMap(v: Object): Boolean = v.isInstanceOf[JMap[_, _]]
    def isStr(v: Object): Boolean = v.isInstanceOf[String]
    def isToken(v: Object): Boolean = v.isInstanceOf[NCToken]

    def asList(v: Object): JList[_] = v.asInstanceOf[JList[_]]
    def asJColl(v: Object): JColl[_] = v.asInstanceOf[JColl[_]]
    def asMap(v: Object): JMap[_, _] = v.asInstanceOf[JMap[_, _]]
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
    def rtMissingParamError(argNum: Int, fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Missing parameters for IDL function ($argNum is required): $fun()")
    def rtTooManyParamsError(argNum: Int, fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Too many parameters for IDL function ($argNum is required): $fun()")
    def rtParamTypeError(fun: String, invalid: Object, expectType: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Expected '$expectType' type of parameter for IDL function '$fun()', found: $invalid")
    def rtParamNullError(fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Unexpected 'null' parameter for IDL function: $fun()")
    def rtListTypeError(fun: String, cause: Exception)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Expected uniform list type for IDL function '$fun()', found polymorphic list.", cause)
    def rtFunError(fun: String, cause: Exception)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Runtime error in IDL function: $fun()", cause)
    def rtUnavailFunError(fun: String)(implicit ctx: PRC): NCE =
        newRuntimeError(s"Function '$fun()' is unavailable in this IDL context.")

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
     * @param x1
     * @param x2
     * @return
     */
    def extract2(x1: ST, x2: ST): (Object, Object, Int) = {
        val Z(v1, n1) = x1()
        val Z(v2, n2) = x2()

        (v1, v2, n1 + n2)
    }

    /**
     *
     * @param x1
     * @param x2
     * @param x3
     * @return
     */
    def extract3(x1: ST, x2: ST, x3: ST): (Object, Object, Object, Int) = {
        val Z(v1, n1) = x1()
        val Z(v2, n2) = x2()
        val Z(v3, n3) = x3()

        (v1, v2, v3, n1 + n2 + n3)
    }

    /**
     *
     * @param lt
     * @param gt
     * @param lteq
     * @param gteq
     */
    def parseCompExpr(lt: TN, gt: TN, lteq: TN, gteq: TN)(implicit ctx: PRC): SI = (_, stack: S, _) => {
        val (x1, x2) = pop2()(stack, ctx)

        if (lt != null)
            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) < asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) < asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) < asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) < asReal(v2)
                    else
                        throw rtBinaryOpError("<", v1, v2)

                Z(f, n)
            })
        else if (gt != null)
            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) > asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) > asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) > asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) > asReal(v2)
                    else
                        throw rtBinaryOpError(">", v1, v2)

                Z(f, n)
            })
        else if (lteq != null)
            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) <= asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) <= asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) <= asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) <= asReal(v2)
                    else
                        throw rtBinaryOpError("<=", v1, v2)

                Z(f, n)
            })
        else {
            require(gteq != null)

            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

                val f =
                    if (isInt(v1) && isInt(v2)) asInt(v1) >= asInt(v2)
                    else if (isInt(v1) && isReal(v2)) asInt(v1) >= asReal(v2)
                    else if (isReal(v1) && isInt(v2)) asReal(v1) >= asInt(v2)
                    else if (isReal(v1) && isReal(v2)) asReal(v1) >= asReal(v2)
                    else
                        throw rtBinaryOpError(">=", v1, v2)

                Z(f, n)
            })
        }
    }

    /**
     *
     * @param mult
     * @param mod
     * @param div
     */
    def parseMultDivModExpr(mult: TN, mod: TN, div: TN)(implicit ctx: PRC): SI = (_, stack: S, _) => {
        val (x1, x2) = pop2()(stack, ctx)

        if (mult != null)
            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

                if (isInt(v1) && isInt(v2)) Z(asInt(v1) * asInt(v2), n)
                else if (isInt(v1) && isReal(v2)) Z(asInt(v1) * asReal(v2), n)
                else if (isReal(v1) && isInt(v2)) Z(asReal(v1) * asInt(v2), n)
                else if (isReal(v1) && isReal(v2)) Z(asReal(v1) * asReal(v2), n)
                else
                    throw rtBinaryOpError("*", v1, v2)
            })
        else if (mod != null)
            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

                if (isInt(v1) && isInt(v2)) Z(asInt(v1) % asInt(v2), n)
                else
                    throw rtBinaryOpError("%", v1, v2)
            })
        else {
            assert(div != null)

            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

                if (isInt(v1) && isInt(v2)) Z(asInt(v1) / asInt(v2), n)
                else if (isInt(v1) && isReal(v2)) Z(asInt(v1) / asReal(v2), n)
                else if (isReal(v1) && isInt(v2)) Z(asReal(v1) / asInt(v2), n)
                else if (isReal(v1) && isReal(v2)) Z(asReal(v1) / asReal(v2), n)
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
    def parseAndOrExpr(and: TN, or: TN)(implicit ctx: PRC): SI = (_, stack: S, _) => {
        val (x1, x2) = pop2()(stack, ctx)

        stack.push(() => {
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
    def parseEqNeqExpr(eq: TN, neq: TN)(implicit ctx: PRC): SI = (_, stack: S, _) => {
        val (x1, x2) = pop2()(stack, ctx)

        def doEq(v1: Object, v2: Object): Boolean = {
            //noinspection ComparingUnrelatedTypes
            if (v1 eq v2) true
            else if (v1 == null && v2 == null) true
            else if ((v1 == null && v2 != null) || (v1 != null && v2 == null)) false
            else if (isInt(v1) && isInt(v2)) asInt(v1) == asInt(v2)
            else if (isReal(v1) && isReal(v2)) asReal(v1) == asReal(v2)
            else if (isBool(v1) && isBool(v2)) asBool(v1) == asBool(v2)
            else if (isStr(v1) && isStr(v2)) asStr(v1) == asStr(v2)
            else if (isList(v1) && isList(v2)) CollectionUtils.isEqualCollection(asList(v1), asList(v2))
            else if ((isInt(v1) && isReal(v2)) || (isReal(v1) && isInt(v2))) asReal(v1) == asReal(v2)
            else
                v1.equals(v2)
        }

        stack.push(() => {
            val (v1, v2, n) = extract2(x1, x2)

            val f =
                if (eq != null)
                    doEq(v1, v2)
                else {
                    assert(neq != null)

                    !doEq(v1, v2)
                }

            Z(f, n)
        })
    }

    /**
     *
     * @param plus
     * @param minus
     */
    def parsePlusMinusExpr(plus: TN, minus: TN)(implicit ctx: PRC): SI = (_, stack: S, _) => {
        val (x1, x2) = pop2()(stack, ctx)

        if (plus != null)
            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

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

            stack.push(() => {
                val (v1, v2, n) = extract2(x1, x2)

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
    def parseUnaryExpr(minus: TN, not: TN)(implicit ctx: PRC): SI = (_, stack: S, _) => {
        val x = pop1()(stack, ctx)

        if (minus != null)
            stack.push(() => {
                val Z(v, n) = x()

                if (isReal(v)) Z(-asReal(v), n)
                else if (isInt(v)) Z(-asInt(v), n)
                else
                    throw rtUnaryOpError("-", v)
            })
        else {
            assert(not != null)

            stack.push(() => {
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
                    case _: NumberFormatException =>
                        try
                            Double.box(JDouble.parseDouble(num)) // Try 'double'.
                        catch {
                            case _: NumberFormatException => U.escapesQuotes(txt) // String in the end.
                        }
                }
            }

        (_, stack, _) => stack.push(() => Z(atom, 0))
    }

    /**
      * 
      * @param fun
      * @param ctx
      * @return
      */
    def parseCallExpr(fun: String)(implicit ctx: PRC): SI = (tok, stack: S, idlCtx) => {
        implicit val evidence: S = stack

        def popMarker(argNum: Int): Unit = if (pop1() != stack.PLIST_MARKER) throw rtTooManyParamsError(argNum, fun)
        def arg[X](argNum: Int, f: () => X): X = {
            if (stack.size < argNum + 1) // +1 for stack frame marker.
                throw rtMissingParamError(argNum, fun)

            val x = f()

            x match {
                case p: Product =>
                    for (e <- p.productIterator)
                        if (e == stack.PLIST_MARKER)
                            rtMissingParamError(argNum, fun)
                case _ =>
                    if (x.asInstanceOf[ST] == stack.PLIST_MARKER)
                        rtMissingParamError(argNum, fun)
            }

            // Make sure to pop up the parameter list stack frame marker.
            popMarker(argNum)

            x
        }
        def arg1(): ST = arg(1, pop1)
        def arg2(): (ST, ST) = arg(2, pop2)
        def arg3(): (ST, ST, ST) = arg(3, pop3)
        def arg1Tok(): ST =
            if (stack.nonEmpty && stack.top == stack.PLIST_MARKER) {
                popMarker(1)

                () => Z(tok, 1)
            }
            else
                arg1()

        def toX[T](typ: String, v: Object, is: Object => Boolean, as: Object => T): T = {
            if (v == null)
                throw rtParamNullError(fun)
            else if (!is(v))
                throw rtParamTypeError(fun, v, typ)

            as(v)
        }
        def toStr(v: Object): String = toX("string", v, isStr, asStr)
        def toInt(v: Object): JInt = toX("int", v, isInt, asInt).toInt
        def toList(v: Object): JList[_] = toX("list", v, isList, asList)
        def toMap(v: Object): JMap[_, _] = toX("map", v, isMap, asMap)
        def toToken(v: Object): NCToken = toX("token", v, isToken, asToken)
        def toBool(v: Object): Boolean = toX("boolean", v, isBool, asBool)
        def toDouble(v: Object): JDouble = toX("double or int", v, x => isInt(x) || isReal(x), asReal)

        def doSplit(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () => {
                    val (v1, v2, n) = extract2(x1, x2)

                    Z(util.Arrays.asList(toStr(v1).split(toStr(v2)):_*), n)
                }
            )
        }

        def doSplitTrim(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () => {
                    val (v1, v2, n) = extract2(x1, x2)

                    Z(util.Arrays.asList(toStr(v1).split(toStr(v2)).toList.map(_.strip):_*), n)
                }
            )
        }

        def doStartsWith(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () => {
                    val (v1, v2, n) = extract2(x1, x2)

                    Z(toStr(v1).startsWith(toStr(v2)), n)
                }
            )
        }

        def doEndsWith(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () => {
                    val (v1, v2, n) = extract2(x1, x2)

                    Z(toStr(v1).endsWith(toStr(v2)), n)
                }
            )
        }

        def doContains(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () => {
                    val (v1, v2, n) = extract2(x1, x2)

                    Z(toStr(v1).contains(toStr(v2)),n)
                }
            )
        }

        def doIndexOf(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () => {
                    val (v1, v2, n) = extract2(x1, x2)

                    Z(toStr(v1).indexOf(toStr(v2)), n)
                }
            )
        }

        def doSubstr(): Unit = {
            val (x1, x2, x3) = arg3()

            stack.push(
                () => {
                    val (v1, v2, v3, n) = extract3(x1, x2, x3)

                    Z(toStr(v1).substring(toInt(v2), toInt(v3)), n)
                }
            )
        }

        def doRegex(): Unit = {
            val (x1, x2) = arg2()

            stack.push(
                () => {
                    val (v1, v2, n) = extract2(x1, x2)

                    Z(toStr(v1).matches(toStr(v2)), n)
                }
            )
        }

        def doReplace(): Unit = {
            val (x1, x2, x3) = arg3()

            stack.push(
                () => {
                    val (v1, v2, v3, n) = extract3(x1, x2, x3)

                    Z(toStr(v1).replaceAll(toStr(v2), toStr(v3)), n)
                }
            )
        }

        def doList(): Unit = {
            val dump = new S() // Empty list is allowed.

            while (stack.nonEmpty && stack.top != stack.PLIST_MARKER)
                dump += stack.pop()

            require(stack.nonEmpty)

            // Pop frame marker.
            pop1()

            stack.push(() => {
                val jl = new util.ArrayList[Object]()
                var z = 0

                dump.toSeq.reverse.foreach { x =>
                    val Z(v, n) = x()

                    z += n

                    jl.add(v)
                }

                Z(jl, z)
            })
        }

        def doReverse(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                val jl = toList(v)

                Collections.reverse(jl)

                Z(jl, n)
            })
        }

        def doMin(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                val lst = toList(v).asInstanceOf[util.List[Object]]

                try
                    if (lst.isEmpty)
                        throw newRuntimeError(s"Unexpected empty list in IDL function: $fun()")
                    else
                        Z(Collections.min(lst, null), n)
                catch {
                    case e: Exception => throw rtListTypeError(fun, e)
                }
            })
        }

        def doAvg(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                val lst = toList(v).asInstanceOf[util.List[Object]]

                try
                    if (lst.isEmpty)
                        throw newRuntimeError(s"Unexpected empty list in IDL function: $fun()")
                    else {
                        val seq: Seq[Double] = lst.asScala.map(p => JDouble.valueOf(p.toString).doubleValue()).toSeq

                        Z(seq.sum / seq.length, n)
                    }
                catch {
                    case e: Exception => throw rtListTypeError(fun, e)
                }
            })
        }

        def doStdev(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                val lst = toList(v).asInstanceOf[util.List[Object]]

                try
                    if (lst.isEmpty)
                        throw newRuntimeError(s"Unexpected empty list in IDL function: $fun()")
                    else {
                        val seq: Seq[Double] = lst.asScala.map(p => JDouble.valueOf(p.toString).doubleValue()).toSeq

                        val mean = seq.sum / seq.length
                        val stdDev = Math.sqrt(seq.map( _ - mean).map(t => t * t).sum / seq.length)

                        Z(stdDev, n)
                    }
                catch {
                    case e: Exception => throw rtListTypeError(fun, e)
                }
            })
        }

        def doToString(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                if (isList(v)) {
                    val jl = new util.ArrayList[Object]()

                    for (d <- toList(v).asScala.map(_.toString))
                        jl.add(d)

                    Z(jl, n)
                }
                else
                    Z(v.toString, n)
            })
        }

        def doToDouble(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                if (isInt(v))
                    Z(asInt(v).toDouble, n)
                else if (isStr(v))
                    try
                        Z(toStr(v).toDouble, n)
                    catch {
                        case e: Exception => throw newRuntimeError(s"Invalid double value '$v' in IDL function: $fun()", e)
                    }
                else
                    throw rtParamTypeError(fun, v, "int or string")
            })
        }

        def doToInt(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                if (isReal(v))
                    Z(Math.round(asReal(v)), n)
                else if (isStr(v))
                    try
                        Z(toStr(v).toLong, n)
                    catch {
                        case e: Exception => throw newRuntimeError(s"Invalid int value '$v' in IDL function: $fun()", e)
                    }
                else
                    throw rtParamTypeError(fun, v, "double or string")
            })
        }

        def doMax(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                val lst = toList(v).asInstanceOf[util.List[Object]]

                try
                    if (lst.isEmpty)
                        throw newRuntimeError(s"Unexpected empty list in IDL function: $fun()")
                    else
                        Z(Collections.max(lst, null), n)
                catch {
                    case e: Exception => throw rtListTypeError(fun, e)
                }
            })
        }

        def doSort(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                val jl = toList(v)

                try
                    jl.sort(null) // Use natural order.
                catch {
                    case e: Exception => throw rtListTypeError(fun, e)
                }

                Z(jl, n)
            })
        }

        def doDistinct(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                val jl = new util.ArrayList[Object]()

                for (d <- toList(v).asScala.toSeq.distinct)
                    jl.add(d.asInstanceOf[Object])

                Z(jl, n)
            })
        }

        def doConcat(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (lst1, lst2, n) = extract2(x1, x2)

                val jl = new util.ArrayList[Object]()

                for (d <- toList(lst1).asScala ++ toList(lst2).asScala)
                    jl.add(d.asInstanceOf[Object])

                Z(jl, n)
            })
        }

        def doHas(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (lst, obj, n) = extract2(x1, x2)

                Z(toList(lst).contains(box(obj)), n)
            })
        }

        def doHasAll(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (lst1, lst2, n) = extract2(x1, x2)

                Z(toList(lst1).containsAll(toList(lst2)), n)
            })
        }

        def doHasAny(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (lst1, lst2, n) = extract2(x1, x2)

                Z(CollectionUtils.containsAny(toList(lst1), toList(lst2)), n)
            })
        }

        def doGet(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (col, key, n) = extract2(x1, x2)

                if (isList(col)) {
                    if (isInt(key))
                        Z(asList(col).get(asInt(key).intValue()).asInstanceOf[Object], n)
                    else
                        throw rtParamTypeError(fun, key, "numeric")
                }
                else if (isMap(col))
                    Z(asMap(col).get(box(key)).asInstanceOf[Object], n)
                else
                    throw rtParamTypeError(fun, col, "list or map")
            })
        }

        def doAbs(): Unit = arg1() match {
            case x => stack.push(() => {
                val Z(v, n) = x()

                v match {
                    case a: JLong => Z(Math.abs(a), n)
                    case a: JDouble => Z(Math.abs(a), n)
                    case _ => throw rtParamTypeError(fun, v, "numeric")
                }
            })
        }

        def doSquare(): Unit = arg1() match {
            case x => stack.push(() => {
                val Z(v, n) = x()

                v match {
                    case a: JLong => Z(a * a, n)
                    case a: JDouble => Z(a * a, n)
                    case _ => throw rtParamTypeError(fun, v, "numeric")
                }
            })
        }

        def doIf(): Unit = {
            val (x1, x2, x3) = arg3()

            stack.push(() => {
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

        def doOrElse(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val Z(v1, n1) = x1()

                if (v1 != null)
                    Z(v1, n1)
                else
                    x2()
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
                throw newRuntimeError(s"Too many parts found for token (use 'tok_find_parts' function instead) [" +
                    s"tokenId=${whole.getId}, " +
                    s"partId=$aliasId" +
                    s"]")
            else
                parts.get(0)
        }

        //noinspection DuplicatedCode
        def doFindPart(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (tok, aliasId, n) = extract2(x1, x2)

                Z(box(findPart(toToken(tok), toStr(aliasId))), n)
            })
        }

        def doPartMeta(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (aliasId, key, n) = extract2(x1, x2)

                Z(box(findPart(tok, toStr(aliasId)).meta[Object](toStr(key))), n)
            })
        }

        //noinspection DuplicatedCode
        def doFindParts(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (t, a, n) = extract2(x1, x2)

                Z(toToken(t).findPartTokens(toStr(a)), n)
            })
        }

        def doHasPart(): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (t, a, n) = extract2(x1, x2)

                Z(toToken(t).findPartTokens(toStr(a)).size() == 1, n)
            })
        }

        def doIsBefore(f: (NCToken, String) => Boolean): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(arg, n) = x()

                Z(idlCtx.toks.exists(t => t.getIndex > tok.getIndex && f(t, toStr(arg))), n)
            })
        }

        def doIsAfter(f: (NCToken, String) => Boolean): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(arg, n) = x()

                Z(idlCtx.toks.exists(t => t.getIndex < tok.getIndex && f(t, toStr(arg))), n)
            })
        }

        def doIsBetween(f: (NCToken, String) => Boolean): Unit = {
            val (x1, x2) = arg2()

            stack.push(() => {
                val (a1, a2, n) = extract2(x1, x2)

                Z(
                    idlCtx.toks.exists(t => t.getIndex < tok.getIndex && f(t, toStr(a1)))
                    &&
                    idlCtx.toks.exists(t => t.getIndex > tok.getIndex && f(t, toStr(a2)))
                    ,
                    n
                )
            })
        }

        def doForAll(f: (NCToken, String) => Boolean): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(arg, n) = x()

                Z(idlCtx.toks.filter(f(_, toStr(arg))).asJava, n)
            })
        }

        def doLength(): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                if (isList(v))
                    Z(asList(v).size(), n)
                else if (isMap(v))
                    Z(asMap(v).size(), n)
                else if (isStr(v))
                    Z(asStr(v).length, n)
                else
                    throw rtParamTypeError(fun, v, "string or list")
            })
        }

        def doIsEmpty(empty: Boolean): Unit = {
            val x = arg1()

            stack.push(() => {
                val Z(v, n) = x()

                if (isList(v))
                    Z(asList(v).isEmpty == empty, n)
                else if (isMap(v))
                    Z(asMap(v).isEmpty == empty, n)
                else if (isStr(v))
                    Z(asStr(v).isEmpty == empty, n)
                else
                    throw rtParamTypeError(fun, v, "string or list")
            })
        }

        def z[Y](args: () => Y, body: Y => Z): Unit = { val x = args(); stack.push(() => body(x)) }
        def z0(body: () => Z): Unit = { popMarker(0); stack.push(() => body()) }

        def checkAvail(): Unit =
            if (idlCtx.toks.isEmpty)
                throw rtUnavailFunError(fun)

        try
            fun match {
            // Metadata access.
            case "meta_part" => doPartMeta()
            case "meta_tok" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(tok.meta[Object](toStr(v))), 1) })
            case "meta_model" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(tok.getModel.meta[Object](toStr(v))), 0) })
            case "meta_req" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(idlCtx.req.getRequestData.get(toStr(v))), 0) })
            case "meta_user" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(idlCtx.req.getUser.meta(toStr(v))), 0) })
            case "meta_company" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(idlCtx.req.getCompany.meta(toStr(v))), 0) })
            case "meta_intent" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(idlCtx.intentMeta.get(toStr(v)).orNull), 0) })
            case "meta_conv" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(idlCtx.convMeta.get(toStr(v)).orNull), 0) })
            case "meta_frag" => z[ST](arg1, { x => val Z(v, f) = x(); Z(box(idlCtx.fragMeta.get(toStr(v)).orNull), f) })
            case "meta_sys" => z[ST](arg1, { x => val Z(v, _) = x(); Z(box(U.sysEnv(toStr(v)).orNull), 0) })

            // Converts JSON to map.
            case "json" => z[ST](arg1, { x => val Z(v, f) = x(); Z(U.jsonToJavaMap(asStr(v)), f) })

            // Inline if-statement.
            case "if" => doIf()

            case "or_else" => doOrElse()

            // Token functions.
            case "tok_id" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getId, 1) }) }
            case "tok_lemma" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getLemma, 1) }) }
            case "tok_stem" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getStem, 1) }) }
            case "tok_pos" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getPos, 1) }) }
            case "tok_txt" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getOriginalText, 1) }) }
            case "tok_norm_txt" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getNormalizedText, 1) }) }
            case "tok_req_id" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getServerRequestId, 1) }) }
            case "tok_sparsity" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getSparsity, 1) }) }
            case "tok_unid" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getUnid, 1) }) }

            case "tok_index" => checkAvail(); arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getIndex, 1) }) }
            case "tok_is_last" => checkAvail(); arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getIndex == idlCtx.toks.size - 1, 1) }) }
            case "tok_is_first" => checkAvail(); arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getIndex == 0, 1) }) }
            case "tok_is_before_id" => checkAvail(); doIsBefore((tok, id) => tok.getId == id)
            case "tok_is_before_group" => checkAvail(); doIsBefore((tok, grpId) => tok.getGroups.contains(grpId))
            case "tok_is_before_parent" => checkAvail(); doIsBefore((tok, id) => tok.getParentId == id)
            case "tok_is_after_id" => checkAvail(); doIsAfter((tok, id) => tok.getId == id)
            case "tok_is_after_group" => checkAvail(); doIsAfter((tok, grpId) => tok.getGroups.contains(grpId))
            case "tok_is_after_parent" => checkAvail(); doIsAfter((tok, id) => tok.getParentId == id)
            case "tok_is_between_ids" => checkAvail(); doIsBetween((tok, id) => tok.getId == id)
            case "tok_is_between_groups" => checkAvail(); doIsBetween((tok, grpId) => tok.getGroups.contains(grpId))
            case "tok_is_between_parents" => checkAvail(); doIsBetween((tok, id) => tok.getParentId == id)

            case "tok_is_abstract" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isAbstract, 1) }) }
            case "tok_is_bracketed" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isBracketed, 1) }) }
            case "tok_is_direct" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isDirect, 1) }) }
            case "tok_is_permutated" => arg1Tok() match { case x => stack.push(() => { Z(!toToken(x().value).isDirect, 1) }) }
            case "tok_is_english" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isEnglish, 1) }) }
            case "tok_is_freeword" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isFreeWord, 1) }) }
            case "tok_is_quoted" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isQuoted, 1) }) }
            case "tok_is_stopword" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isStopWord, 1) }) }
            case "tok_is_swear" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isSwear, 1) }) }
            case "tok_is_user" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isUserDefined, 1) }) }
            case "tok_is_wordnet" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).isWordnet, 1) }) }
            case "tok_ancestors" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getAncestors, 1) }) }
            case "tok_parent" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getParentId, 1) }) }
            case "tok_groups" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getGroups, 1) }) }
            case "tok_value" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getValue, 1) }) }
            case "tok_aliases" => arg1Tok() match { case x => stack.push(() => { Z(box(toToken(x().value).getAliases), 1) }) }
            case "tok_start_idx" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getStartCharIndex, 1) }) }
            case "tok_end_idx" => arg1Tok() match { case x => stack.push(() => { Z(toToken(x().value).getEndCharIndex, 1) }) }
            case "tok_this" => z0(() => Z(tok, 1))
            case "tok_has_part" => doHasPart()
            case "tok_find_part" => doFindPart()
            case "tok_find_parts" => doFindParts()

            case "tok_count" => checkAvail(); z0(() => Z(idlCtx.toks.size, 0))
            case "tok_all" => checkAvail(); z0(() => Z(idlCtx.toks.asJava, 0))
            case "tok_all_for_id" => checkAvail(); doForAll((tok, id) => tok.getId == id)
            case "tok_all_for_parent" => checkAvail(); doForAll((tok, id) => tok.getParentId == id)
            case "tok_all_for_group" => checkAvail(); doForAll((tok, grp) => tok.getGroups.contains(grp))

            // Request data.
            case "req_id" => z0(() => Z(idlCtx.req.getServerRequestId, 0))
            case "req_normtext" => z0(() => Z(idlCtx.req.getNormalizedText, 0))
            case "req_tstamp" => z0(() => Z(idlCtx.req.getReceiveTimestamp, 0))
            case "req_addr" => z0(() => Z(idlCtx.req.getRemoteAddress.orElse(null), 0))
            case "req_agent" => z0(() => Z(idlCtx.req.getClientAgent.orElse(null), 0))

            // User data.
            case "user_id" => z0(() => Z(idlCtx.req.getUser.getId, 0))
            case "user_fname" => z0(() => Z(idlCtx.req.getUser.getFirstName.orElse(null), 0))
            case "user_lname" => z0(() => Z(idlCtx.req.getUser.getLastName.orElse(null), 0))
            case "user_email" => z0(() => Z(idlCtx.req.getUser.getEmail.orElse(null), 0))
            case "user_admin" => z0(() => Z(idlCtx.req.getUser.isAdmin, 0))
            case "user_signup_tstamp" => z0(() => Z(idlCtx.req.getUser.getSignupTimestamp, 0))

            // Company data.
            case "comp_id" => z0(() => Z(idlCtx.req.getCompany.getId, 0))
            case "comp_name" => z0(() => Z(idlCtx.req.getCompany.getName, 0))
            case "comp_website" => z0(() => Z(idlCtx.req.getCompany.getWebsite.orElse(null), 0))
            case "comp_country" => z0(() => Z(idlCtx.req.getCompany.getCountry.orElse(null), 0))
            case "comp_region" => z0(() => Z(idlCtx.req.getCompany.getRegion.orElse(null), 0))
            case "comp_city" => z0(() => Z(idlCtx.req.getCompany.getCity.orElse(null), 0))
            case "comp_addr" => z0(() => Z(idlCtx.req.getCompany.getAddress.orElse(null), 0))
            case "comp_postcode" => z0(() => Z(idlCtx.req.getCompany.getPostalCode.orElse(null), 0))

            // String functions.
            case "trim" | "strip" => z[ST](arg1, { x => val Z(v, f) = x(); Z(toStr(v).trim, f) })
            case "uppercase" => z[ST](arg1, { x => val Z(v, f) = x(); Z(toStr(v).toUpperCase, f) })
            case "lowercase" => z[ST](arg1, { x => val Z(v, f) = x(); Z(toStr(v).toLowerCase, f) })
            case "is_alpha" => z[ST](arg1, { x => val Z(v, f) = x(); Z(StringUtils.isAlpha(toStr(v)), f) })
            case "is_alphanum" => z[ST](arg1, { x => val Z(v, f) = x(); Z(StringUtils.isAlphanumeric(toStr(v)), f) })
            case "is_whitespace" => z[ST](arg1, { x => val Z(v, f) = x(); Z(StringUtils.isWhitespace(toStr(v)), f) })
            case "is_num" => z[ST](arg1, { x => val Z(v, f) = x(); Z(StringUtils.isNumeric(toStr(v)), f) })
            case "is_numspace" => z[ST](arg1, { x => val Z(v, f) = x(); Z(StringUtils.isNumericSpace(toStr(v)), f) })
            case "is_alphaspace" => z[ST](arg1, { x => val Z(v, f) = x(); Z(StringUtils.isAlphaSpace(toStr(v)), f) })
            case "is_alphanumspace" => z[ST](arg1, { x => val Z(v, f) = x(); Z(StringUtils.isAlphanumericSpace(toStr(v)), f) })
            case "split" => doSplit()
            case "split_trim" => doSplitTrim()
            case "starts_with" => doStartsWith()
            case "ends_with" => doEndsWith()
            case "contains" => doContains()
            case "index_of" => doIndexOf()
            case "substr" => doSubstr()
            case "regex" => doRegex()
            case "replace" => doReplace()
            case "to_double" => doToDouble()
            case "to_int" => doToInt()

            // Math functions.
            case "abs" => doAbs()
            case "ceil" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.ceil(toDouble(v)), f) })
            case "floor" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.floor(toDouble(v)), f) })
            case "rint" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.rint(toDouble(v)), f) })
            case "round" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.round(toDouble(v)), f) })
            case "signum" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.signum(toDouble(v)), f) })
            case "sqrt" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.sqrt(toDouble(v)), f) })
            case "cbrt" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.cbrt(toDouble(v)), f) })
            case "acos" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.acos(toDouble(v)), f) })
            case "asin" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.asin(toDouble(v)), f) })
            case "atan" => z[ST](arg1, { x => val Z(v, f) = x(); Z( Math.atan(toDouble(v)), f) })
            case "cos" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.cos(toDouble(v)), f) })
            case "sin" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.sin(toDouble(v)), f) })
            case "tan" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.tan(toDouble(v)), f) })
            case "cosh" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.cosh(toDouble(v)), f) })
            case "sinh" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.sinh(toDouble(v)), f) })
            case "tanh" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.tanh(toDouble(v)), f) })
            case "atan2" => z[(ST, ST)](arg2, { x => val (v1, v2, n) = extract2(x._1, x._2); Z(Math.atan2(toDouble(v1), toDouble(v2)), n) })
            case "degrees" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.toDegrees(toDouble(v)), f) })
            case "radians" => z[ST](arg1, { x => val Z(v, f) = x(); Z( Math.toRadians(toDouble(v)), f) })
            case "exp" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.exp(toDouble(v)), f) })
            case "expm1" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.expm1(toDouble(v)), f) })
            case "hypot" => z[(ST, ST)](arg2, { x => val (v1, v2, n) = extract2(x._1, x._2); Z(Math.hypot(toDouble(v1), toDouble(v2)), n) })
            case "log" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.log(toDouble(v)), f) })
            case "log10" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.log10(toDouble(v)), f) })
            case "log1p" => z[ST](arg1, { x => val Z(v, f) = x(); Z(Math.log1p(toDouble(v)), f) })
            case "pow" => z[(ST, ST)](arg2, { x => val (v1, v2, n) = extract2(x._1, x._2); Z(Math.pow(toDouble(v1), toDouble(v2)), n) })
            case "square" => doSquare()
            case "pi" => z0(() => Z(Math.PI, 0))
            case "euler" => z0(() => Z(Math.E, 0))
            case "rand" => z0(() => Z(Math.random, 0))

            // Collection functions.
            case "list" => doList()
            case "get" => doGet() // Works for both lists (int index) and maps (object key).
            case "has" => doHas() // Only works for lists.
            case "has_any" => doHasAny()
            case "has_all" => doHasAll()
            case "first" => z[ST](arg1, { x => val Z(v, n) = x(); val lst = toList(v); Z(if (lst.isEmpty) null else lst.get(0).asInstanceOf[Object], n)})
            case "last" => z[ST](arg1, { x => val Z(v, n) = x(); val lst = toList(v); Z(if (lst.isEmpty) null else lst.get(lst.size() - 1).asInstanceOf[Object], n)})
            case "keys" => z[ST](arg1, { x => val Z(v, n) = x(); Z(new util.ArrayList(toMap(v).keySet()), n) })
            case "values" => z[ST](arg1, { x => val Z(v, n) = x(); Z(new util.ArrayList(toMap(v).values()), n) })
            case "reverse" => doReverse()
            case "sort" => doSort()
            case "is_empty" => doIsEmpty(true)
            case "non_empty" => doIsEmpty(false)
            case "distinct" => doDistinct()
            case "concat" => doConcat()

            // Applies to strings as well.
            case "size" | "count" | "length" => doLength()

            // Misc.
            case "to_string" => doToString()

            // Statistical operations on lists.
            case "max" => doMax()
            case "min" => doMin()
            case "avg" => doAvg()
            case "stdev" => doStdev()

            // Date-time functions.
            case "year" => z0(() => Z(LocalDate.now.getYear, 0)) // 2021.
            case "month" => z0(() => Z(LocalDate.now.getMonthValue, 0)) // 1 ... 12.
            case "day_of_month" => z0(() => Z(LocalDate.now.getDayOfMonth, 0)) // 1 ... 31.
            case "day_of_week" => z0(() => Z(LocalDate.now.getDayOfWeek.getValue, 0))
            case "day_of_year" => z0(() => Z(LocalDate.now.getDayOfYear, 0))
            case "hour" => z0(() => Z(LocalTime.now.getHour, 0))
            case "minute" => z0(() => Z(LocalTime.now.getMinute, 0))
            case "second" => z0(() => Z(LocalTime.now.getSecond, 0))
            case "week_of_month" => z0(() => Z(Calendar.getInstance().get(Calendar.WEEK_OF_MONTH), 0))
            case "week_of_year" => z0(() => Z(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), 0))
            case "quarter" => z0(() => Z(LocalDate.now().get(IsoFields.QUARTER_OF_YEAR), 0))
            case "now" => z0(() => Z(U.now(), 0)) // Epoc time.

            case _ => throw rtUnknownFunError(fun) // Assertion.
        }
        catch {
            case e: NCE => throw e // Rethrow.
            case e: Exception => throw rtFunError(fun, e)
        }
    }
}
