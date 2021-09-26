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

package org.apache.nlpcraft.probe.mgrs

import org.apache.nlpcraft.common.nlp.NCNlpSentenceToken
import org.apache.nlpcraft.model.{NCToken, _}

/**
  *
  * @param token
  * @param word
  */
case class NCProbeIdlToken(token: NCToken, word: NCNlpSentenceToken) {
    val (origText: String, wordIndexes: Set[Int], minIndex: Int, maxIndex: Int, isToken: Boolean, isWord: Boolean) =
        if (token != null)
            (token.origText, token.wordIndexes.toSet, token.wordIndexes.head, token.wordIndexes.last, true, false)
        else
            (word.origText, word.wordIndexes.toSet, word.wordIndexes.head, word.wordIndexes.last, false, true)

    private lazy val hash = if (isToken) Seq(wordIndexes, token.getId).hashCode() else wordIndexes.hashCode()

    override def hashCode(): Int = hash

    def isSubsetOf(minIndex: Int, maxIndex: Int, indexes: Set[Int]): Boolean =
        if (this.minIndex > maxIndex || this.maxIndex < minIndex)
            false
        else
            wordIndexes.subsetOf(indexes)

    override def equals(obj: Any): Boolean = obj match {
        case x: NCProbeIdlToken =>
            hash == x.hash && (isToken && x.isToken && token == x.token || isWord && x.isWord && word == x.word)
        case _ => false
    }

    // Added for debug reasons.
    override def toString: String = {
        val idxs = wordIndexes.mkString(",")

        if (isToken && token.getId != "nlpcraft:nlp") s"'$origText' (${token.getId}) [$idxs]]" else s"'$origText' [$idxs]"
    }
}

/**
  *
  */
object NCProbeIdlToken {
    def apply(t: NCToken): NCProbeIdlToken = NCProbeIdlToken(token = t, word = null)
    def apply(t: NCNlpSentenceToken): NCProbeIdlToken = NCProbeIdlToken(token = null, word = t)
}
