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
import java.util.{List ⇒ JList, Map ⇒ JMap}

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

        //noinspection TypeCheckCanBeMatch
        override def exitExpr(ctx: NCIntentDslParser.ExprContext): Unit = {
            if (ctx.atom() != null) {} // Just a val - no-op.
            else if (ctx.LPAR() != null && ctx.RPAR() != null) {} // Just a val in brackets - no-op.
            else if (ctx.COMMA() != null) { // Collection.
                termCode += ((_, stack: StackType, _) ⇒ {
                    require(stack.nonEmpty)

                    val NCDslTermRetVal(lastVal, usedTok) = stack.pop()

                    // Only use Java collections.
                    val newVal: AnyRef =
                        if (lastVal.isInstanceOf[JList[Object]]) {
                            val x = lastVal.asInstanceOf[JList[Object]]

                            x.add(mkVal(ctx.atom().getText))

                            x
                        }
                        else
                            java.util.Collections.singletonList(lastVal)

                    stack.push(NCDslTermRetVal(newVal, usedTok))
                })
            }
            else if (ctx.MINUS() != null || ctx.PLUS() != null || ctx.MULT() != null || ctx.DIV() != null || ctx.MOD() != null) {
                termCode += ((_, stack: StackType, _) ⇒ {
                    require(stack.size >= 2)

                    // Stack pop in reverse order of push...
                    val NCDslTermRetVal(val2, f1) = stack.pop()
                    val NCDslTermRetVal(val1, f2) = stack.pop()

                    val usedTok = f1 || f2
                    
                    def push(any: AnyRef): Unit = stack.push(NCDslTermRetVal(any, usedTok))
                    def pushLong(any: Long): Unit = stack.push(NCDslTermRetVal(Long.box(any), usedTok))
                    def pushDouble(any: Double): Unit = stack.push(NCDslTermRetVal(Double.box(any), usedTok))

                    def error(op: String): Unit = throw new IAE(s"Unexpected '$op' operation for values: $val1, $val2")
                    
                    if (ctx.PLUS() != null) { // '+'.
                        if (isJList(val1) && isJList(val2)) {
                            val lst1 = asJList(val1)
                            val lst2 = asJList(val2)
    
                            lst1.addAll(lst2)
                            
                            push(lst1)
                        }
                        else if (isJList(val1)) {
                            val lst1 = asJList(val1)
                            
                            lst1.add(val2)
                            
                            push(lst1)
                        }
                        else if (isJList(val2)) {
                            val lst2 = asJList(val2)
    
                            lst2.add(val1)
    
                            push(lst2)
                        }
                        else if (isString(val1) && isString(val2))
                            push(asString(val1) + asString(val2))
                        else if (isJLong(val1) && isJLong(val2))
                            pushLong(asJLong(val1).longValue() + asJLong(val2).longValue())
                        else if (isJLong(val1) && isJDouble(val2))
                            pushDouble(asJLong(val1).longValue() + asJDouble(val2).doubleValue())
                        else if (isJDouble(val1) && isJLong(val2))
                            pushDouble(asJDouble(val1).doubleValue() + asJLong(val2).longValue())
                        else if (isJDouble(val1) && isJDouble(val2))
                            pushDouble(asJDouble(val1).doubleValue() + asJDouble(val2).doubleValue())
                        else
                            error("+")
                    }
                    else if (ctx.MINUS() != null) { // '-'.
                        if (isJList(val1) && isJList(val2)) {
                            val lst1 = asJList(val1)
                            val lst2 = asJList(val2)
                            
                            lst1.removeAll(lst2)

                            push(lst1)
                        }
                        else if (isJList(val1)) {
                            val lst1 = asJList(val1)
                            
                            lst1.remove(val2)
                            
                            push(lst1)
                        }
                        else if (isJLong(val1) && isJLong(val2))
                            pushLong(asJLong(val1).longValue() - asJLong(val2).longValue())
                        else if (isJLong(val1) && isJDouble(val2))
                            pushDouble(asJLong(val1).longValue() - asJDouble(val2).doubleValue())
                        else if (isJDouble(val1) && isJLong(val2))
                            pushDouble(asJDouble(val1).doubleValue() - asJLong(val2).longValue())
                        else if (isJDouble(val1) && isJDouble(val2))
                            pushDouble(asJDouble(val1).doubleValue() - asJDouble(val2).doubleValue())
                        else
                            error("-")
                    }
                    else if (ctx.MULT() != null) { // '*'.
                        if (isJLong(val1) && isJLong(val2))
                            pushLong(asJLong(val1).longValue() * asJLong(val2).longValue())
                        else if (isJLong(val1) && isJDouble(val2))
                            pushDouble(asJLong(val1).longValue() * asJDouble(val2).doubleValue())
                        else if (isJDouble(val1) && isJLong(val2))
                            pushDouble(asJDouble(val1).doubleValue() * asJLong(val2).longValue())
                        else if (isJDouble(val1) && isJDouble(val2))
                            pushDouble(asJDouble(val1).doubleValue() * asJDouble(val2).doubleValue())
                        else
                            error("*")
                    }
                    else if (ctx.DIV() != null) { // '/'.
                        if (isJLong(val1) && isJLong(val2))
                            pushLong(asJLong(val1).longValue() / asJLong(val2).longValue())
                        else if (isJLong(val1) && isJDouble(val2))
                            pushDouble(asJLong(val1).longValue() / asJDouble(val2).doubleValue())
                        else if (isJDouble(val1) && isJLong(val2))
                            pushDouble(asJDouble(val1).doubleValue() / asJLong(val2).longValue())
                        else if (isJDouble(val1) && isJDouble(val2))
                            pushDouble(asJDouble(val1).doubleValue() / asJDouble(val2).doubleValue())
                        else
                            error("/")
                    }
                    else if (ctx.MOD() != null) { // '%'.
                        if (isJLong(val1) && isJLong(val2))
                            pushLong(asJLong(val1).longValue() % asJLong(val2).longValue())
                        else
                            error("%")
                    }
                    else
                        assert(false)
                })
            }
        }

        override def exitCall(ctx: NCIntentDslParser.CallContext): Unit = {
            val fun = ctx.ID().getText

            termCode += ((tok: NCToken, stack: StackType, ctx: NCDslTermContext) ⇒ {
                val NCDslTermRetVal(param, usedTok) = if (stack.nonEmpty) stack.pop else (null, false)

                def push(any: AnyRef, f: Boolean): Unit = stack.push(NCDslTermRetVal(any, f))
                def pushLong(any: Long, f: Boolean): Unit = stack.push(NCDslTermRetVal(Long.box(any), f))
                def pushDouble(any: Double, f: Boolean): Unit = stack.push(NCDslTermRetVal(Double.box(any), f))
                def pushBoolean(any: Boolean, f: Boolean): Unit = stack.push(NCDslTermRetVal(Boolean.box(any), f))

                def unknownFun(): Unit = throw new IAE(s"Unknown built-in function: $fun")
                def errParamNum(): Unit = throw new IAE(s"Invalid number of parameters for built-in function: $fun")
                def errParamType(): Unit = throw new IAE(s"Invalid parameter type for built-in function: $fun")

                def check1String(): Unit = if (param == null) errParamNum() else if (!isString(param)) errParamType()
                def check1Long(): Unit = if (param == null) errParamNum() else if (!isJLong(param)) errParamType()
                def check1Double(): Unit = if (param == null) errParamNum() else if (!isJDouble(param)) errParamType()

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
                    case "meta" ⇒

                    // Converts JSON to map.
                    case "json" ⇒

                    // Inline if-statement.
                    case "if" ⇒

                    // Token functions.
                    case "id" ⇒ push(tok.getId, true)
                    case "ancestors" ⇒ push(tok.getAncestors, true)
                    case "parent" ⇒ push(tok.getParentId, true)
                    case "groups" ⇒ push(tok.getGroups, true)
                    case "value" ⇒ push(tok.getValue, true)
                    case "aliases" ⇒ push(tok.getAliases, true)
                    case "start_idx" ⇒ pushLong(tok.getStartCharIndex, true)
                    case "end_idx" ⇒ pushLong(tok.getEndCharIndex, true)

                    // String functions.
                    case "trim" ⇒ push(doTrim(), usedTok)
                    case "strip" ⇒ push(doTrim(), usedTok)
                    case "uppercase" ⇒ push(doUppercase(), usedTok)
                    case "lowercase" ⇒ push(doLowercase(), usedTok)
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

                    case _ ⇒ unknownFun()
                }
            })
        }

        override def exitClsNer(ctx: NCIntentDslParser.ClsNerContext): Unit = {
            if (ctx.javaFqn() != null)
                termClsName = ctx.javaFqn().getText.strip()

            termMtdName = ctx.ID().getText.strip()
        }

        override def exitTermId(ctx: NCIntentDslParser.TermIdContext): Unit = {
            termId = ctx.ID().getText.trim
        }

        override def exitTermEq(ctx: NCIntentDslParser.TermEqContext): Unit = {
            termConv = ctx.TILDA() != null
        }

        override def exitFlowDecl(ctx: NCIntentDslParser.FlowDeclContext): Unit = {
            val qRegex = ctx.qstring().getText.trim

            if (qRegex != null && qRegex.length > 2) {
                val regex = qRegex.substring(1, qRegex.length - 1).strip // Remove single quotes.

                flowRegex = if (regex.nonEmpty) Some(regex) else None
            }
        }

        override def exitIntentId(ctx: NCIntentDslParser.IntentIdContext): Unit = {
            id = ctx.ID().getText.trim
        }

        override def exitMetaDecl(ctx: NCIntentDslParser.MetaDeclContext): Unit = {
            meta = U.jsonToObject(ctx.jsonObj().getText, classOf[HashMap[String, Any]])
        }

        override def exitOrderedDecl(ctx: NCIntentDslParser.OrderedDeclContext): Unit = {
            ordered = ctx.BOOL().getText.strip == "true"
        }

        override def exitAtom(ctx: NCIntentDslParser.AtomContext): Unit = {
            termCode += ((_, stack, _) ⇒ stack.push(NCDslTermRetVal(mkVal(ctx.getText), usedTok = false)))
        }

        /**
         *
         * @param s
         * @return
         */
        private def mkVal(s: String): Object = {
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
