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

package org.apache.nlpcraft.probe.mgrs.model

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.tree._

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.intent.utils.NCDslTokenPredicate
import org.apache.nlpcraft.probe.mgrs.model.antlr4._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Compiler for model synonym DSL.
 */
object NCModelSynonymDslCompiler extends LazyLogging {
    private type Predicate = java.util.function.Function[NCToken, java.lang.Boolean]
    
    def toJavaFunc(alias: String, func: NCToken ⇒ Boolean): Predicate = (tok: NCToken) => {
        val res = func(tok)
    
        // Store predicate's alias, if any, in token metadata if this token satisfies this predicate.
        // NOTE: token can have multiple aliases associated with it.
        if (res && alias != null) {
            val meta = tok.getMetadata
            
            if (!meta.containsKey(TOK_META_ALIASES_KEY))
                meta.put(TOK_META_ALIASES_KEY, new java.util.HashSet[String]())
            
            val aliases = meta.get(TOK_META_ALIASES_KEY).asInstanceOf[java.util.Set[String]]
            
            aliases.add(alias)
        }
        
        res
    }
    
    /**
     *
     */
    class FiniteStateMachine extends NCSynonymDslBaseListener {
        private val predStack = new mutable.ArrayStack[NCToken ⇒ Boolean] // Stack of predicates.
        private val lvalParts = ArrayBuffer.empty[String] // lval parts collector.
        private val rvalList = ArrayBuffer.empty[String] // rval list collector.
        private var alias: String = _
        private var rval: String = _
    
        /**
         * Gets compiled synonym DSL.
         * 
         * @return
         */
        def getCompiledSynonymDsl: NCModelSynonymDsl = {
            NCModelSynonymDsl(alias, toJavaFunc(alias, predStack.pop()))
        }
    
        override def exitRvalSingle(ctx: NCSynonymDslParser.RvalSingleContext): Unit = {
            rval = ctx.getText.trim()
        }
    
        override def exitRvalList(ctx: NCSynonymDslParser.RvalListContext): Unit = {
            rvalList += rval
        }
    
        override def exitLvalPart(ctx: NCSynonymDslParser.LvalPartContext): Unit = {
            lvalParts += ctx.ID().getText.trim()
        }
    
        override def exitAlias(ctx: NCSynonymDslParser.AliasContext): Unit = {
            alias = ctx.ID().getText.trim()
        }
    
        override def exitItem(ctx: NCSynonymDslParser.ItemContext): Unit = {
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
    
        override def exitPredicate(ctx: NCSynonymDslParser.PredicateContext): Unit = {
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
            
            val errMsg = s"Synonym DSL syntax error at line $line:$charPos - $msg"
    
            logger.error(errMsg)
            logger.error(s"  |-- Expression: $dsl")
            logger.error(s"  +-- Error:      ${makeCharPosPointer(dsl.length, charPos)}")
            
            throw new NCE(errMsg)
        }
    }

    /**
     *
     * @param dsl Synonym DSL to parse.
     * @return
     */
    def parse(dsl: String): NCModelSynonymDsl = {
        require(dsl != null)
        
        // ANTLR4 armature.
        val lexer = new NCSynonymDslLexer(CharStreams.fromString(dsl))
        val tokens = new CommonTokenStream(lexer)
        val parser = new NCSynonymDslParser(tokens)
        
        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(dsl))
        parser.addErrorListener(new CompilerErrorListener(dsl))
        
        // State automata.
        val fsm = new FiniteStateMachine
        
        // Parse the input DSL and walk built AST.
        (new ParseTreeWalker).walk(fsm, parser.synonym())
        
        fsm.getCompiledSynonymDsl
    }
}
