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

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._
import org.apache.nlpcraft.common.NCE
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.intent.impl.antlr4.{NCIntentDslBaseListener, NCIntentDslLexer, NCIntentDslParser}
import org.apache.nlpcraft.model.intent.utils.{NCDslFlowItem, NCDslIntent, NCDslTerm, NCDslTokenPredicate}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Intent DSL compiler.
  */
object NCIntentDslCompiler extends LazyLogging {
    // Compiler cache.
    private val cache = new java.util.concurrent.ConcurrentHashMap[String, NCDslIntent]().asScala
    
    private var mdlId: String = _
    
    /**
      * 
      */
    class FiniteStateMachine extends NCIntentDslBaseListener {
        // Intent components.
        private var conv: Boolean = true
        private var ordered: Boolean = false
        private var id: String = _
        private val terms = ArrayBuffer.empty[NCDslTerm] // Accumulator for parsed terms.
        private val flow = ArrayBuffer.empty[NCDslFlowItem] // Accumulator for flow items.
        private val flowItemIds = mutable.HashSet.empty[String] // Accumulator for flow items IDs.
    
        // Currently parsed term.
        private var termId: String = _
        
        // Current min/max quantifier.
        private var min = 1
        private var max = 1
    
        private val predStack = new mutable.ArrayStack[NCToken ⇒ Boolean] // Stack of predicates.
        private val lvalParts = ArrayBuffer.empty[String] // lval parts collector.
        private val rvalList = ArrayBuffer.empty[String] // rval list collector.
        private var rval: String = _
    
        /**
          *
          * @return
          */
        def getBuiltIntent: NCDslIntent = {
            require(id != null)
            require(terms.nonEmpty)
            
            NCDslIntent(id, conv, ordered, flow.toArray, terms.toArray)
        }
    
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
    
        override def exitLvalPart(ctx: NCIntentDslParser.LvalPartContext): Unit = {
            lvalParts += ctx.ID().getText.trim()
        }
    
        override def exitTermId(ctx: NCIntentDslParser.TermIdContext): Unit = {
            termId = ctx.ID().getText
        }
    
        override def exitMinMaxRange(ctx: NCIntentDslParser.MinMaxRangeContext): Unit = {
            val minStr = ctx.getChild(1).getText
            val maxStr = ctx.getChild(3).getText
            
            try
                setMinMax(java.lang.Integer.parseInt(minStr), java.lang.Integer.parseInt(maxStr))
            catch {
                // Errors should be caught during compilation phase.
                case _: NumberFormatException ⇒ assert(false)
            }
        }
    
        override def exitIntentId(ctx: NCIntentDslParser.IntentIdContext): Unit = {
            id = ctx.ID().getText
        }
    
        override def exitConvDecl(ctx: NCIntentDslParser.ConvDeclContext): Unit = {
            conv = ctx.BOOL().getText == "true"
        }
    
        override def exitOrderedDecl(ctx: NCIntentDslParser.OrderedDeclContext): Unit = {
            ordered = ctx.BOOL().getText == "true"
        }
    
        override def exitTerm(ctx: NCIntentDslParser.TermContext): Unit = {
            require(predStack.size == 1)
            
            val p = predStack.pop
            
            terms += new NCDslTerm(termId, new java.util.function.Function[NCToken, java.lang.Boolean]() {
                override def apply(tok: NCToken): java.lang.Boolean = p.apply(tok)
                override def toString: String = p.toString() //ctx.item().getText
            }, min, max)
            
            // Reset.
            termId = null
            setMinMax(1, 1)
        }
    
        override def exitRvalSingle(ctx: NCIntentDslParser.RvalSingleContext): Unit = {
            rval = ctx.getText.trim()
        }
    
        override def exitRvalList(ctx: NCIntentDslParser.RvalListContext): Unit = {
            rvalList += rval
        }
    
        override def exitFlowItemIds(ctx: NCIntentDslParser.FlowItemIdsContext): Unit = {
            val id = ctx.ID()
            
            if (id != null)
                flowItemIds.add(ctx.ID().getText)
        }
    
        override def exitIdList(ctx: NCIntentDslParser.IdListContext): Unit = {
            flowItemIds.add(ctx.ID().getText)
        }
    
        override def exitFlowItem(ctx: NCIntentDslParser.FlowItemContext): Unit = {
            flow += NCDslFlowItem(Seq.empty ++ flowItemIds, min, max)
            
            // Reset
            setMinMax(1, 1)
            flowItemIds.clear()
        }
    
        override def exitItem(ctx: NCIntentDslParser.ItemContext): Unit = {
            if (ctx.EXCL() != null) {
                val p = predStack.pop
    
                predStack.push(new Function[NCToken, Boolean] {
                    override def apply(tok: NCToken): Boolean = !p.apply(tok)
                    override def toString: String = s"!$p"
                })
            }
            else if (ctx.AND() != null) {
                // Note that stack is LIFO so order is flipped.
                val p2 = predStack.pop
                val p1 = predStack.pop
        
                predStack.push(new Function[NCToken, Boolean] {
                    override def apply(tok: NCToken): Boolean = {
                        // To bypass any possible compiler optimizations.
                        if (!p1.apply(tok))
                            false
                        else if (!p2.apply(tok))
                            false
                        else
                            true
                    }
                    override def toString: String = s"$p1 && $p2"
                })
            }
            else if (ctx.OR() != null) {
                // Note that stack is LIFO so order is flipped.
                val p2 = predStack.pop
                val p1 = predStack.pop
    
                predStack.push(new Function[NCToken, Boolean] {
                    override def apply(tok: NCToken): Boolean = {
                        // To bypass any possible compiler optimizations.
                        if (p1.apply(tok))
                            true
                        else if (p2.apply(tok))
                            true
                        else
                            false
                    }
                    override def toString: String = s"$p1 || $p2"
                })
            }
            else if (ctx.RPAREN() != null && ctx.LPAREN() != null) {
                val p = predStack.pop
    
                predStack.push(new Function[NCToken, Boolean] {
                    override def apply(tok: NCToken): Boolean = p.apply(tok)
                    override def toString: String = s"($p)"
                })
            }
    
            // In all other cases the current predicate is already on the top of the stack.
        }
    
        /**
         *
         * @param rv
         * @return
         */
        private def mkRvalObject(rv: String): Any = {
            if (rv == "null") null // Try 'null'.
            else if (rv == "true") true // Try 'boolean'.
            else if (rv == "false") false // Try 'boolean'.
            // Only numeric values below...
            else {
                // Strip '_' from numeric values.
                val rvalNum = rv.replaceAll("_", "")
            
                try
                    java.lang.Integer.parseInt(rvalNum) // Try 'int'.
                catch {
                    case _: NumberFormatException ⇒
                        try
                            java.lang.Long.parseLong(rvalNum) // Try 'long'.
                        catch {
                            case _: NumberFormatException ⇒
                                try
                                    java.lang.Double.parseDouble(rvalNum) // Try 'double'.
                                catch {
                                    case _: NumberFormatException ⇒ rv // String by default.
                                }
                        }
                }
            }
        }
    
        override def exitPredicate(ctx: NCIntentDslParser.PredicateContext): Unit = {
            var lval: String = null
            var lvalFunc: String = null
            var op: String = null
        
            def getLvalNode(tree: ParseTree): String =
                tree.getChild(if (tree.getChildCount == 1) 0 else 1).getText.trim
        
            if (ctx.children.size() == 3) {
                lval = getLvalNode(ctx.getChild(0))
                op = ctx.getChild(1).getText.trim
            }
            else {
                lvalFunc = ctx.getChild(0).getText.trim
                lval = getLvalNode(ctx.getChild(2))
                op = ctx.getChild(4).getText.trim
            }
        
            val pred = new NCDslTokenPredicate(
                lvalParts.asJava,
                lvalFunc,
                lval,
                op,
                if (rvalList.isEmpty) mkRvalObject(rval) else rvalList.map(mkRvalObject).asJava
            )
        
            predStack.push(new Function[NCToken, Boolean] {
                override def apply(tok: NCToken): Boolean = pred.apply(tok)
                override def toString: String = pred.toString
            })
        
            // Reset.
            lvalParts.clear()
            rvalList.clear()
            rval = null
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
            
            val errMsg = s"Intent DSL syntax error at line $line:$charPos - $msg"
            
            logger.error(errMsg)
            logger.error(s"  |-- Model:  $mdlId")
            logger.error(s"  |-- Intent: $dsl")
            logger.error(s"  +-- Error:  ${makeCharPosPointer(dsl.length, charPos)}")
            
            throw new NCE(errMsg)
        }
    }

    /**
      *
      * @param dsl Intent DSL to parse.
      * @param mdlId ID of the model the intent belongs to.
      * @return
      */
    def compile(dsl: String, mdlId: String): NCDslIntent = {
        require(dsl != null)
        
        this.mdlId = mdlId
        
        val intent: NCDslIntent = cache.getOrElseUpdate(dsl, {
            // ANTLR4 armature.
            val lexer = new NCIntentDslLexer(CharStreams.fromString(dsl))
            val tokens = new CommonTokenStream(lexer)
            val parser = new NCIntentDslParser(tokens)
    
            // Set custom error handlers.
            lexer.removeErrorListeners()
            parser.removeErrorListeners()
            lexer.addErrorListener(new CompilerErrorListener(dsl))
            parser.addErrorListener(new CompilerErrorListener(dsl))
            
            // State automata.
            val fsm = new FiniteStateMachine
    
            // Parse the input DSL and walk built AST.
            (new ParseTreeWalker).walk(fsm, parser.intent())
            
            // Return the built intent.
            val newIntent = fsm.getBuiltIntent
    
            // Log for visual verification.
            logger.debug(s"Intent compiler:")
            logger.debug(s"  |-- IN  $dsl")
            logger.debug(s"  |-- OUT ${newIntent.toDslString}")
    
            newIntent
        })
        
        intent
    }
}
