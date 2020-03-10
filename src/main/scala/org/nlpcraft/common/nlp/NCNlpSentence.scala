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

package org.nlpcraft.common.nlp

import scala.collection._
import scala.collection.mutable.ArrayBuffer
import scala.language.implicitConversions

/**
  * Parsed NLP sentence is a collection of tokens. Each token is a collection of notes and
  * each note is a collection of KV pairs.
  *
  * @param srvReqId Server request ID.
  * @param text Normalized text.
  * @param weight Weight.
  * @param enabledBuiltInToks Enabled built-in tokens.
  * @param tokens Initial buffer.
  */
class NCNlpSentence(
    val srvReqId: String,
    val text: String,
    val weight: Double,
    val enabledBuiltInToks: Set[String],
    override val tokens: ArrayBuffer[NCNlpSentenceToken] = new ArrayBuffer[NCNlpSentenceToken](32)
) extends NCNlpSentenceTokenBuffer(tokens) with java.io.Serializable {
    private lazy val hash =
        Seq(srvReqId, text, enabledBuiltInToks, tokens).map(_.hashCode()).foldLeft(0)((a, b) ⇒ 31 * a + b)

    // Deep copy.
    override def clone(): NCNlpSentence = new NCNlpSentence(srvReqId, text, weight, enabledBuiltInToks, tokens.map(_.clone()))

    /**
      * Utility method that gets set of notes for given note type collected from
      * tokens in this sentence. Notes are sorted in the same order they appear
      * in this sentence.
      *
      * @param noteType Note type.
      */
    def getNotes(noteType: String): Seq[NCNlpSentenceNote] = this.flatMap(_.getNotes(noteType)).distinct
    
    /**
      * Utility method that removes note with given ID from all tokens in this sentence.
      * No-op if such note wasn't found.
      *
      * @param id Note ID.
      */
    def removeNote(id: String): Unit = this.foreach(_.remove(id))

    override def hashCode(): Int = hash

    override def equals(obj: Any): Boolean = obj match {
        case x: NCNlpSentence ⇒
            tokens == x.tokens &&
            srvReqId == x.srvReqId &&
            text == x.text &&
            enabledBuiltInToks == x.enabledBuiltInToks
        case _ ⇒ false
    }
}

object NCNlpSentence {
    implicit def toTokens(x: NCNlpSentence): ArrayBuffer[NCNlpSentenceToken] = x.tokens
}