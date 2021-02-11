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
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.intent.impl.antlr4._
import org.apache.nlpcraft.model.intent.utils.ver2._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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
            else if (ctx.STAR() != null)
                setMinMax(0, Integer.MAX_VALUE)
            else if (ctx.QUESTION() != null)
                setMinMax(0, 1)
            else
                assert(false)
        }

        //noinspection TypeCheckCanBeMatch
        override def exitExpr(ctx: NCIntentDslParser.ExprContext): Unit = {
            if (ctx.`val`() != null) {} // Just a val - no-op.
            else if (ctx.LPAREN() != null && ctx.RPAREN() != null) {} // Just a val in brackets - no-op.
            else if (ctx.COMMA() != null) { // Collection.
                termCode += ((_, stack: StackType, _) ⇒ {
                    require(stack.nonEmpty)

                    val NCDslTermRetVal(lastVal, usedTok) = stack.pop()

                    val newVal = lastVal match {
                        case list: List[_] ⇒ mkVal(ctx.`val`().getText) :: list
                        case _ ⇒ List(lastVal)
                    }

                    stack.push(NCDslTermRetVal(newVal, usedTok))
                })
            }
            else if (ctx.MINUS() != null || ctx.PLUS() != null || ctx.STAR() != null || ctx.FSLASH() != null || ctx.PERCENT() != null) {
                termCode += ((_, stack: StackType, _) ⇒ {
                    require(stack.size >= 2)

                    // Stack pop in reverse order of push...
                    val NCDslTermRetVal(val2, usedTok2) = stack.pop()
                    val NCDslTermRetVal(val1, usedTok1) = stack.pop()
                    
                    def push(any: Any): Unit = stack.push(NCDslTermRetVal(any, usedTok1 || usedTok2))
                    def isLong(v: Any): Boolean = v.isInstanceOf[java.lang.Long]
                    def isDouble(v: Any): Boolean = v.isInstanceOf[java.lang.Double]
                    def isString(v: Any): Boolean = v.isInstanceOf[String]
                    def asLong(v: Any): java.lang.Long = v.asInstanceOf[java.lang.Long]
                    def asDouble(v: Any): java.lang.Double = v.asInstanceOf[java.lang.Double]
                    def asString(v: Any): String = v.asInstanceOf[String]
                    def asList(v: Any): List[_] = v.asInstanceOf[List[_]]
                    def isList(v: Any): Boolean = v.isInstanceOf[List[_]]
                    
                    def error(op: String): Unit =
                        throw new IllegalArgumentException(s"Unexpected '$op' operation for values: $val1, $val2")
                    
                    if (ctx.PLUS() != null) { // '+'.
                        if (isList(val1) && isList(val2))
                            push(asList(val1) ::: asList(val2))
                        else if (isList(val1))
                            push(val2 :: asList(val1))
                        else if (isList(val2)) 
                            push(val1 :: asList(val2))
                        else if (isString(val1) && isString(val2))
                            push(asString(val1) + asString(val2))
                        else if (isLong(val1) && isLong(val2))
                            push(asLong(val1).longValue() + asLong(val2).longValue())
                        else if (isLong(val1) && isDouble(val2))
                            push(asLong(val1).longValue() + asDouble(val2).doubleValue())
                        else if (isDouble(val1) && isLong(val2))
                            push(asDouble(val1).doubleValue() + asLong(val2).longValue())
                        else if (isDouble(val1) && isDouble(val2))
                            push(asDouble(val1).doubleValue() + asDouble(val2).doubleValue())
                        else
                            error("+")
                    }
                    else if (ctx.MINUS() != null) { // '-'.
                        if (isList(val1) && isList(val2))
                            push(asList(val1).filterNot(asInstanceOf[List[_]].toSet))
                        else if (isList(val1))
                            push(asList(val1).filter(_ != val1))
                        else if (isLong(val1) && isLong(val2))
                            push(asLong(val1).longValue() - asLong(val2).longValue())
                        else if (isLong(val1) && isDouble(val2))
                            push(asLong(val1).longValue() - asDouble(val2).doubleValue())
                        else if (isDouble(val1) && isLong(val2))
                            push(asDouble(val1).doubleValue() - asLong(val2).longValue())
                        else if (isDouble(val1) && isDouble(val2))
                            push(asDouble(val1).doubleValue() - asDouble(val2).doubleValue())
                        else
                            error("-")
                    }
                    else if (ctx.STAR() != null) { // '*'.
                        if (isLong(val1) && isLong(val2))
                            push(asLong(val1).longValue() * asLong(val2).longValue())
                        else if (isLong(val1) && isDouble(val2))
                            push(asLong(val1).longValue() * asDouble(val2).doubleValue())
                        else if (isDouble(val1) && isLong(val2))
                            push(asDouble(val1).doubleValue() * asLong(val2).longValue())
                        else if (isDouble(val1) && isDouble(val2))
                            push(asDouble(val1).doubleValue() * asDouble(val2).doubleValue())
                        else
                            error("*")
                    }
                    else if (ctx.FSLASH() != null) { // '/'.
                        if (isLong(val1) && isLong(val2))
                            push(asLong(val1).longValue() / asLong(val2).longValue())
                        else if (isLong(val1) && isDouble(val2))
                            push(asLong(val1).longValue() / asDouble(val2).doubleValue())
                        else if (isDouble(val1) && isLong(val2))
                            push(asDouble(val1).doubleValue() / asLong(val2).longValue())
                        else if (isDouble(val1) && isDouble(val2))
                            push(asDouble(val1).doubleValue() / asDouble(val2).doubleValue())
                        else
                            error("/")
                    }
                    else if (ctx.PERCENT() != null) { // '%'.
                        if (isLong(val1) && isLong(val2))
                            push(asLong(val1).longValue() % asLong(val2).longValue())
                        else
                            error("%")
                    }
                    else
                        assert(false)
                })
            }
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
            meta = U.jsonToObject(ctx.jsonObj().getText, classOf[Map[String, Any]])
        }

        override def exitOrderedDecl(ctx: NCIntentDslParser.OrderedDeclContext): Unit = {
            ordered = ctx.BOOL().getText.strip == "true"
        }

        override def exitVal(ctx: NCIntentDslParser.ValContext): Unit = {
            termCode += ((_, stack, _) ⇒ stack.push(NCDslTermRetVal(mkVal(ctx.getText), usedTok = false)))
        }

        /**
         *
         * @param s
         * @return
         */
        private def mkVal(s: String): Any = {
            if (s == "null") null // Try 'null'.
            else if (s == "true") true // Try 'boolean'.
            else if (s == "false") false // Try 'boolean'.
            // Only numeric or string values below...
            else {
                // Strip '_' from numeric values.
                val num = s.replaceAll("_", "")

                try
                    java.lang.Long.parseLong(num) // Try 'long'.
                catch {
                    case _: NumberFormatException ⇒
                        try
                            java.lang.Double.parseDouble(num) // Try 'double'.
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
