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

package org.apache.nlpcraft.examples.lightswitch.nlp.token.parser

import org.apache.nlpcraft.*
import org.languagetool.tokenizers.fr.FrenchWordTokenizer
import scala.jdk.CollectionConverters.*

/**
  * [[https://languagetool.org/ Language Tool]] based FR language [[NCTokenParser entity parser]].
  */
class NCFrTokenParser extends NCTokenParser:
    private val tokenizer = new FrenchWordTokenizer

    /** @inheritdoc */
    override def tokenize(text: String): List[NCToken] =
        val toks = collection.mutable.ArrayBuffer.empty[NCToken]
        var sumLen = 0

        for ((word, idx) <- tokenizer.tokenize(text).asScala.zipWithIndex)
            val start = sumLen
            val end = sumLen + word.length

            if word.strip.nonEmpty then
                toks += new NCPropertyMapAdapter with NCToken:
                    override def getText: String = word
                    override def getIndex: Int = idx
                    override def getStartCharIndex: Int = start
                    override def getEndCharIndex: Int = end

            sumLen = end

        toks.toList
