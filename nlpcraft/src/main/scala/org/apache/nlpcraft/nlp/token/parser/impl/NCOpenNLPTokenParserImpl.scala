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

package org.apache.nlpcraft.nlp.token.parser.impl

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.tokenize.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils

import java.io.*
import java.util
import java.util.List as JList
import scala.jdk.CollectionConverters.*

/**
  *
  * @param tokMdl
  */
class NCOpenNLPTokenParserImpl(tokMdl: String) extends NCTokenParser with LazyLogging:
    require(tokMdl != null)

    @volatile private var tokenizer: TokenizerME = _

    init()

    private def init(): Unit =
        tokenizer = new TokenizerME(new TokenizerModel(NCUtils.getStream(tokMdl)))

        logger.trace(s"Loaded resource: $tokMdl")

    override def tokenize(text: String): JList[NCToken] =
        this.synchronized {
            tokenizer.tokenizePos(text).zipWithIndex.map { (p, idx) =>
                new NCPropertyMapAdapter with NCToken :
                    override val getText: String = p.getCoveredText(text).toString
                    override val getIndex: Int = idx
                    override val getStartCharIndex: Int = p.getStart
                    override val getEndCharIndex: Int = p.getEnd
            }.toSeq.asJava
        }