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

package org.apache.nlpcraft.common.nlp

import scala.collection.mutable.ArrayBuffer
import scala.collection.{Seq, IndexedSeq ⇒ IdxSeq}
import scala.language.implicitConversions

/**
  *
  * @param tokens Initial buffer.
  */
class NCNlpSentenceTokenBuffer(val tokens: ArrayBuffer[NCNlpSentenceToken] = new ArrayBuffer[NCNlpSentenceToken](16)) extends java.io.Serializable {
    /** Stringified stems. */
    lazy val stems: String = tokens.map(_.stem).mkString(" ")

    /** Stem-based hashcode. */
    lazy val stemsHash: Int = stems.hashCode()

    type SSOT = IdxSeq[IdxSeq[Option[NCNlpSentenceToken]]]
    type SST = IdxSeq[IdxSeq[NCNlpSentenceToken]]

    /**
      * Gets all sequential permutations of tokens in this NLP sentence.
      *
      * For example, if NLP sentence contains "a, b, c, d" tokens, then
      * this function will return the sequence of following token sequences in this order:
      * "a b c d"
      * "a b c"
      * "b c d"
      * "a b"
      * "b c"
      * "c d"
      * "a"
      * "b"
      * "c"
      * "d"
      *
      * NOTE: this method will not return any permutations with a quoted token.
      *
      * @param stopWords Whether or not include tokens marked as stop words.
      * @param maxLen Maximum number of tokens in the sequence.
      * @param withQuoted Whether or not to include quoted tokens.
      */
    def tokenMix(
        stopWords: Boolean = false,
        maxLen: Int = Integer.MAX_VALUE,
        withQuoted: Boolean = false
    ): SST = {
        val toks = tokens.filter(t ⇒ stopWords || (!stopWords && !t.isStopWord))

        val res = (for (n ← toks.length until 0 by -1 if n <= maxLen) yield toks.sliding(n)).flatten

        if (withQuoted) res else res.filter(!_.exists(_.isQuoted))
    }

    /**
      * Gets all sequential permutations of tokens in this NLP sentence.
      * This method is like a 'tokenMix', but with all combinations of stop-words (with and without)
      *
      * @param maxLen Maximum number of tokens in the sequence.
      * @param withQuoted Whether or not to include quoted tokens.
      */
    def tokenMixWithStopWords(maxLen: Int = Integer.MAX_VALUE, withQuoted: Boolean = false): SST = {
        /**
          * Gets all combinations for sequence of mandatory tokens with stop-words and without.
          *
          * Example:
          * 'A (stop), B, C(stop) → [A, B, C]; [A, B]; [B, C], [B]
          * 'A, B(stop), C(stop) → [A, B, C]; [A, B]; [A, C], [A].
          *
          * @param toks Tokens.
          */
        def permutations(toks: Seq[NCNlpSentenceToken]): SST = {
            def multiple(seq: SSOT, t: NCNlpSentenceToken): SSOT =
                if (seq.isEmpty)
                    if (t.isStopWord) IdxSeq(IdxSeq(Some(t)), IdxSeq(None)) else IdxSeq(IdxSeq(Some(t)))
                else {
                    (for (subSeq ← seq) yield subSeq :+ Some(t)) ++
                        (if (t.isStopWord) for (subSeq ← seq) yield subSeq :+ None else Seq.empty)
                }

            var res: SSOT = IdxSeq.empty

            for (t ← toks)
                res = multiple(res, t)

            res.map(_.flatten).filter(_.nonEmpty)
        }

        tokenMix(stopWords = true, maxLen, withQuoted).
            flatMap(permutations).
            filter(_.nonEmpty).
            distinct.
            sortBy(seq ⇒ (-seq.length, seq.head.index))
    }
}

object NCNlpSentenceTokenBuffer {
    implicit def toTokens(x: NCNlpSentenceTokenBuffer): ArrayBuffer[NCNlpSentenceToken] = x.tokens
    implicit def toBuf( toks: Iterable[NCNlpSentenceToken]): NCNlpSentenceTokenBuffer = apply(toks)

    def apply(toks: Iterable[NCNlpSentenceToken]): NCNlpSentenceTokenBuffer =
        new NCNlpSentenceTokenBuffer(new ArrayBuffer[NCNlpSentenceToken](toks.size) ++ toks)
}
