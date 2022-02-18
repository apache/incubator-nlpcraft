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

package org.apache.nlpcraft.nlp.token.parser.opennlp.impl

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.lemmatizer.*
import opennlp.tools.postag.*
import opennlp.tools.stemmer.*
import opennlp.tools.tokenize.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils

import java.io.*
import java.util
import java.util.stream.Collectors
import java.util.{Collections, List as JList, Set as JSet}
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*

/**
  *
  * @param tokMdl
  * @param posMdlSrc
  * @param lemmaDicSrc
  */
class NCOpenNLPTokenParserImpl(tokMdl: String,  posMdlSrc: String, lemmaDicSrc: String) extends NCTokenParser with LazyLogging:
    require(tokMdl != null)
    require(posMdlSrc != null)
    require(lemmaDicSrc != null)

    private var tagger: POSTaggerME = _
    private var lemmatizer: DictionaryLemmatizer = _
    private var tokenizer: TokenizerME = _

    init()

    private def init(): Unit =
        NCUtils.execPar(
            () => {
                tagger = new POSTaggerME(new POSModel(NCUtils.getStream(posMdlSrc)))
                logger.trace(s"Loaded resource: $posMdlSrc")
            },
            () => {
                lemmatizer = new DictionaryLemmatizer(NCUtils.getStream(lemmaDicSrc))
                logger.trace(s"Loaded resource: $lemmaDicSrc")
            },
            () => {
                tokenizer = new TokenizerME(new TokenizerModel(NCUtils.getStream(tokMdl)))
                logger.trace(s"Loaded resource: $tokMdl")
            }
        )(ExecutionContext.Implicits.global)

    override def tokenize(text: String): JList[NCToken] =
        case class Holder(text: String, start: Int, end: Int)

        this.synchronized {
            val hs = tokenizer.tokenizePos(text).map(p => Holder(p.getCoveredText(text).toString, p.getStart, p.getEnd))
            val toks = hs.map(_.text)
            val poses = tagger.tag(toks)
            var lemmas = lemmatizer.lemmatize(toks, poses)

            require(toks.length == poses.length && toks.length == lemmas.length)

            // For some reasons lemmatizer (en-lemmatizer.dict) marks some words with non-existent POS 'NNN'
            // Valid POS list: https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
            val suspIdxs = lemmas.zip(poses).zipWithIndex.flatMap {
                // "0" is flag that lemma cannot be obtained for some reasons.
                case ((lemma, pos), i) => Option.when(lemma == "O" && pos == "NN")(i)
            }

            if suspIdxs.nonEmpty then
                val fixes: Map[Int, String] = lemmatizer.
                    lemmatize(suspIdxs.map(i => toks(i)), suspIdxs.map(_ => "NNN")).
                    zipWithIndex.
                    flatMap { (lemma, i) => Option.when(lemma != "0")(suspIdxs(i) -> lemma) }.toMap
                lemmas = lemmas.zipWithIndex.map {
                    (lemma, idx) => fixes.getOrElse(idx, lemma)
                }

            hs.zip(poses).zip(lemmas).zipWithIndex.map { case (((h, pos), lemma), idx) =>
                new NCPropertyMapAdapter with NCToken:
                    override inline def getText: String = h.text
                    override val getLemma: String = lemma
                    override val getPos: String = pos
                    override val getIndex: Int = idx
                    override val getStartCharIndex: Int = h.start
                    override val getEndCharIndex: Int = h.end
            }.toSeq.asJava
        }
