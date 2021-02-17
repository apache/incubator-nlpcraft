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

import org.antlr.v4.runtime.tree.{TerminalNode ⇒ TN}
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.intent.utils.ver2.{NCDslTermContext, NCDslTermRetVal}

import java.lang.{Double ⇒ JDouble, IllegalArgumentException ⇒ IAE, Long ⇒ JLong}
import scala.collection.mutable

trait NCBaseDslCompiler {
    type StackType = mutable.ArrayStack[NCDslTermRetVal]
    type Instr = (NCToken, StackType,  NCDslTermContext) ⇒ Unit

    def isJLong(v: AnyRef): Boolean = v.isInstanceOf[JLong]
    def isJDouble(v: AnyRef): Boolean = v.isInstanceOf[JDouble]
    def isString(v: AnyRef): Boolean = v.isInstanceOf[String]
    def isBoolean(v: AnyRef): Boolean = v.isInstanceOf[Boolean]
    def asJLong(v: AnyRef): Long = v.asInstanceOf[JLong].longValue()
    def asJDouble(v: AnyRef): Double = v.asInstanceOf[JDouble].doubleValue()
    def asString(v: AnyRef): String = v.asInstanceOf[String]
    def asBoolean(v: AnyRef): Boolean = v.asInstanceOf[Boolean]

    def pushAny(any: AnyRef, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(any, usedTok))
    def pushLong(any: Long, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(Long.box(any), usedTok))
    def pushDouble(any: Double, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(Double.box(any), usedTok))
    def pushBoolean(any: Boolean, usedTok: Boolean)(implicit stack: StackType): Unit =
        stack.push(NCDslTermRetVal(Boolean.box(any), usedTok))

    def errUnaryOp(op: String, v: AnyRef): IAE =
        new IAE(s"Unexpected '$op' DSL operation for value: $v")
    def errBinaryOp(op: String, v1: AnyRef, v2: AnyRef): IAE =
        new IAE(s"Unexpected '$op' DSL operation for values: $v1, $v2")
    def errUnknownFun(fun: String): IAE =
        new IAE(s"Unknown DSL function: $fun()")
    def errMinParamNum(min: Int, fun: String): IAE =
        new IAE(s"Invalid number of parameters for DSL function ($min is required): $fun()")
    def errParamNum(fun: String): IAE =
        new IAE(s"Invalid number of parameters for DSL function: $fun()")
    def errParamType(fun: String, param: AnyRef, expectType: String): IAE =
        new IAE(s"Expecting '$expectType' type of parameter for DSL function '$fun()', found: $param")

    /**
     *
     * @param stack
     * @return
     */
    def pop2()(implicit stack: StackType): (AnyRef, AnyRef, Boolean, Boolean) = {
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
    def pop3()(implicit stack: StackType): (AnyRef, AnyRef, AnyRef, Boolean, Boolean, Boolean) = {
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
    def pop1()(implicit stack: StackType): (AnyRef, Boolean) = {
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
    def parseCompExpr(lt: TN, gt: TN, lteq: TN, gteq: TN): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s = stack

        val (v1, v2, f1, f2) = pop2()
        val usedTok = f1 || f2

        if (lt != null) {
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
        else if (gt != null) {
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
        else if (lteq != null) {
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
            assert(gteq != null)

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
    }

    /**
     *
     * @param mult
     * @param mod
     * @param div
     */
    def parseMultExpr(mult: TN, mod: TN, div: TN): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s = stack

        val (v1, v2, f1, f2) = pop2()
        val usedTok = f1 || f2

        if (mult != null) {
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
        else if (mod != null) {
            if (isJLong(v1) && isJLong(v2))
                pushLong(asJLong(v1) % asJLong(v2), usedTok)
            else
                throw errBinaryOp("%", v1, v2)
        }
        else {
            assert(div != null)

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
    }

    /**
     *
     * @param plus
     * @param minus
     */
    def parsePlusExpr(plus: TN, minus: TN): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s = stack

        val (v1, v2, f1, f2) = pop2()
        val usedTok = f1 || f2

        if (plus != null) {
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
            assert(minus != null)

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
    }


    /**
     * @param minus
     * @param not
     * @return
     */
    def parseUnaryExpr(minus: TN, not: TN): Instr = (_, stack: StackType, _) ⇒ {
        implicit val s = stack

        val (v, usedTok) = pop1()

        if (minus != null) {
            if (isJDouble(v))
                pushDouble(-asJDouble(v), usedTok)
            else if (isJLong(v))
                pushLong(-asJLong(v), usedTok)
            else
                throw errUnaryOp("-", v)
        }
        else {
            assert(not != null)

            if (isBoolean(v))
                pushBoolean(!asBoolean(v), usedTok)
            else
                throw errUnaryOp("!", v)
        }
    }
}
