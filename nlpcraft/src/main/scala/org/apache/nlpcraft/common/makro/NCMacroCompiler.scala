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

package org.apache.nlpcraft.common.makro

import com.typesafe.scalalogging.LazyLogging
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{BaseErrorListener, CharStreams, CommonTokenStream, RecognitionException, Recognizer}
import org.apache.nlpcraft.common.makro.antlr4._
import org.apache.nlpcraft.common._

/**
 *
 */
object NCMacroCompiler extends LazyLogging {
    /**
     *
     */
    class FiniteStateMachine extends NCMacroDslBaseListener {
        /**
         *
         * @return
         */
        def getExpandedMacro(): Set[String] = ???
    }

    /**
     * Custom error handler.
     */
    class CompilerErrorListener(in: String) extends BaseErrorListener {
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

            val errMsg = s"Macro syntax error at line $line:$charPos - $msg\n" +
                s"  |-- ${c("Macro:")} $in\n" +
                s"  +-- ${c("Error:")}  ${makeCharPosPointer(in.length, charPos)}"

            throw new NCE(errMsg)
        }
    }

    /**
     *
     * @param in Macro to expand.
     * @return Expanded macro as a set of finite strings.
     */
    def compile(in: String): Set[String] = {
        // ANTLR4 armature.
        val lexer = new NCMacroDslLexer(CharStreams.fromString(in))
        val tokens = new CommonTokenStream(lexer)
        val parser = new NCMacroDslParser(tokens)

        // Set custom error handlers.
        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        lexer.addErrorListener(new CompilerErrorListener(in))
        parser.addErrorListener(new CompilerErrorListener(in))

        // State automata.
        val fsm = new FiniteStateMachine

        // Parse the input DSL and walk built AST.
        (new ParseTreeWalker).walk(fsm, parser.line())

        // Return the expanded macro.
        fsm.getExpandedMacro()
    }
}
