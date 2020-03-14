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

package org.apache.nlpcraft.model.impl

import org.apache.nlpcraft.model.NCToken
import scala.language.implicitConversions
import scala.collection.JavaConverters._

/**
  *
  */
object NCTokenPimp {
    implicit  def x(tok: NCToken): NCTokenPimp = new NCTokenPimp(tok)

    private final val SYS = Set("nlpcraft", "google", "opennlp", "spacy", "stanford")
}

import NCTokenPimp._

/**
 *
 */
class NCTokenPimp(impl: NCToken) {
    def isStopWord: Boolean = impl.meta("nlpcraft:nlp:stopword")
    def isFreeWord: Boolean = impl.meta("nlpcraft:nlp:freeword")
    def isQuoted: Boolean = impl.meta("nlpcraft:nlp:quoted")
    def isDirect: Boolean = impl.meta("nlpcraft:nlp:direct")
    def isBracketed: Boolean = impl.meta("nlpcraft:nlp:bracketed")
    def wordIndexes: List[Int] = impl.meta[java.util.List[Int]]("nlpcraft:nlp:wordindexes").asScala.toList
    def origText: String = impl.meta("nlpcraft:nlp:origtext")
    def normText: String = impl.meta("nlpcraft:nlp:normtext")
    def stem: String = impl.meta("nlpcraft:nlp:stem")
    def lemma: String = impl.meta("nlpcraft:nlp:lemma")
    def unid: String = impl.meta("nlpcraft:nlp:unid")
    def pos: String = impl.meta("nlpcraft:nlp:pos")
    def posDesc: String = impl.meta("nlpcraft:nlp:posdesc")
    def index: Int = impl.meta("nlpcraft:nlp:index")
    def wordLength: Int = impl.meta("nlpcraft:nlp:wordlength")
    def sparsity: Int = impl.meta("nlpcraft:nlp:sparsity")
    def minIndex: Int = impl.meta("nlpcraft:nlp:minindex")
    def maxIndex: Int = impl.meta("nlpcraft:nlp:maxindex")
    def isUserDefined: Boolean = {
        val id = impl.getId
        val i = id.indexOf(':')

        i <= 0 || !SYS.contains(id.take(i))
    }
}

