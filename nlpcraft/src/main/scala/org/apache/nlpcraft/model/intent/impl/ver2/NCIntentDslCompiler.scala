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

        type Instr = (NCToken, NCDslTermContext) ⇒ NCDslTermRetVal

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
            termCode += ((_, _) ⇒ NCDslTermRetVal(mkVal(ctx.getText), usedTok = false))
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
            // Only numeric values below...
            else {
                // Strip '_' from numeric values.
                val num = s.replaceAll("_", "")

                try
                    java.lang.Integer.parseInt(num) // Try 'int'.
                catch {
                    case _: NumberFormatException ⇒
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
