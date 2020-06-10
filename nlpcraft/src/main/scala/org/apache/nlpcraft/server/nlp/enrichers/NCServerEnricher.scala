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

package org.apache.nlpcraft.server.nlp.enrichers

import com.typesafe.scalalogging.LazyLogging
import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.common.nlp._

import scala.collection._

/**
  * Base trait for all server enricher.
  */
abstract class NCServerEnricher extends NCService with LazyLogging {
    /**
      * Attempts to enrich given NLP sentence in an isolation.
      *
      * @param ns NLP sentence to enrich.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def enrich(ns: NCNlpSentence, parent: Span = null): Unit
    
    // Utility functions.
    final protected def toStemKey(toks: Seq[NCNlpSentenceToken]): String = toks.map(_.stem).mkString(" ")
    final protected def toLemmaKey(toks: Seq[NCNlpSentenceToken]): String = toks.map(_.lemma).mkString(" ")
    final protected def toValueKey(toks: Seq[NCNlpSentenceToken]): String = toks.map(_.origText.toLowerCase).mkString(" ")
    final protected def toOriginalKey(toks: Seq[NCNlpSentenceToken]): String = toks.map(_.origText).mkString(" ")
}

object NCServerEnricher {
    // Penn Treebank POS tags for opening & closing quotes.
    private val Q_POS = Set("``", "''")
    
    // Stanford POS for opening & closing brackets.
    // NOTE: it's different from standard Penn Treebank.
    private val LB_POS = "-LRB-"
    private val RB_POS = "-RRB-"
    
    def isQuote(t: NCNlpSentenceToken): Boolean = Q_POS.contains(t.pos)
    
    def isLBR(t: NCNlpSentenceToken): Boolean = t.pos == LB_POS
    def isRBR(t: NCNlpSentenceToken): Boolean = t.pos == RB_POS
    def isBR(t: NCNlpSentenceToken): Boolean = isLBR(t) || isRBR(t)
    
    /**
      * Skips spaces after left and before right brackets.
      *
      * @param toks
      * @param get
      */
    def mkSumString(toks: Seq[NCNlpSentenceToken], get: NCNlpSentenceToken ⇒ String): String = {
        val buf = mutable.Buffer.empty[String]
        
        val n = toks.size
        
        toks.zipWithIndex.foreach(p ⇒ {
            val t = p._1
            val idx = p._2
            
            buf += get(t)
            
            def isNextTokenRB: Boolean = idx <= n - 2 && isRBR(toks(idx + 1))
            def isLast: Boolean = idx == n - 1
            def isSolidWithNext: Boolean = idx <= n - 2 && t.endCharIndex == toks(idx + 1).startCharIndex
            
            if (
                !isLBR(t) &&
                !isNextTokenRB &&
                !isLast &&
                !isSolidWithNext
            )
                buf += " "
        })
        
        buf.mkString
    }
}
