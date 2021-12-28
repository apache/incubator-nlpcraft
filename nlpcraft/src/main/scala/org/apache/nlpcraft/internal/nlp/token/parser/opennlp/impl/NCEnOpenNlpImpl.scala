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

package org.apache.nlpcraft.internal.nlp.token.parser.opennlp.impl

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

object NCEnOpenNlpImpl:
    /**
      *
      * @param tokMdlSrc Local filesystem path, resources file path or URL for OpenNLP tokenizer model.
      * @param posMdlSrc Local filesystem path, resources file path or URL for OpenNLP tagger model.
      * @param lemmaDicSrc Local filesystem path, resources file path or URL for OpenNLP lemmatizer dictionary.
      * @return
      */
    def apply(tokMdlSrc: String, posMdlSrc: String, lemmaDicSrc: String): NCEnOpenNlpImpl =
        new NCEnOpenNlpImpl(NCUtils.getStream(tokMdlSrc), NCUtils.getStream(posMdlSrc), NCUtils.getStream(lemmaDicSrc))

    /**
      *
      * @param tokMdlFile Local file for OpenNLP tokenizer model.
      * @param posMdlFile Local file for OpenNLP tagger model.
      * @param lemmaDicFile Local file for OpenNLP lemmatizer dictionary.
      * @return
      */
    def apply(tokMdlFile: File, posMdlFile: File, lemmaDicFile: File): NCEnOpenNlpImpl =
        def toStream(f: File) = new BufferedInputStream(new FileInputStream(f))

        new NCEnOpenNlpImpl(toStream(tokMdlFile), toStream(posMdlFile), toStream(lemmaDicFile))

/**
  *
  * @param tokMdlIn
  * @param posMdlIn
  * @param lemmaDicIn
  */
class NCEnOpenNlpImpl(
    tokMdlIn: InputStream,
    posMdlIn: InputStream,
    lemmaDicIn: InputStream
) extends NCTokenParser :
    private val stemmer = new PorterStemmer

    @volatile var tokenizer: TokenizerME = _
    @volatile var tagger: POSTaggerME = _
    @volatile var lemmatizer: DictionaryLemmatizer = _
    @volatile var swFinder: NCEnStopWordsFinder = _

    private var addStopWords: JSet[String] = _
    private var exclStopWords: JSet[String] = _

    override def start(cfg: NCModelConfig): Unit =
        NCUtils.execPar(
            () => tokenizer = new TokenizerME(new TokenizerModel(tokMdlIn)),
            () => tagger = new POSTaggerME(new POSModel(posMdlIn)),
            () => lemmatizer = new DictionaryLemmatizer(lemmaDicIn),
            () => swFinder = new NCEnStopWordsFinder(stem(addStopWords), stem(exclStopWords))
        )(ExecutionContext.Implicits.global)

    override def stop(): Unit =
        swFinder = null
        lemmatizer = null
        tagger = null
        lemmatizer = null

    /**
      *
      * @param addStopWords
      */
    def setAdditionalStopWords(addStopWords: JSet[String]): Unit = this.addStopWords = addStopWords

    /**
      *
      * @return
      */
    def getAdditionalStopWords: JSet[String] = addStopWords

    /**
      *
      * @param exclStopWords
      */
    def setExcludedStopWords(exclStopWords: JSet[String]): Unit = this.exclStopWords = exclStopWords

    /**
      *
      * @return
      */
    def getExcludedStopWords: JSet[String] = exclStopWords

    /**
      *
      * @param set
      */
    private def stem(set: JSet[String]): Set[String] =
        if set == null then Set.empty else set.asScala.toSet.map(stemmer.stem)

    override def parse(req: NCRequest, cfg: NCModelConfig): JList[NCToken] =
        // OpenNLP classes are not thread-safe.
        this.synchronized {
            val sen = req.getText

            case class TokenHolder(origin: String, normalized: String, start: Int, end: Int, length: Int)

            val holders = tokenizer.tokenizePos(sen).map( t => {
                val txt = t.getCoveredText(sen).toString
                TokenHolder(txt, txt.toLowerCase, t.getStart, t.getEnd, t.length)
            })

            val words = holders.map(_.origin)
            val posTags = tagger.tag(words)
            var lemmas = lemmatizer.lemmatize(words, posTags).toSeq

            require(holders.length == posTags.length)

            // For some reasons lemmatizer (en-lemmatizer.dict) marks some words with non-existent POS 'NNN'
            // Valid POS list: https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
            val suspIdxs = lemmas.zip(posTags).zipWithIndex.flatMap {
                // "0" is flag that lemma cannot be obtained for some reasons.
                case ((lemma, pos), i) => if lemma == "O" && pos == "NN" then Some(i) else None
            }

            if suspIdxs.nonEmpty then
                val fixes: Map[Int, String] = lemmatizer.
                    lemmatize(suspIdxs.map(i => words(i)).toArray, suspIdxs.map(_ => "NNN").toArray).
                    zipWithIndex.
                    flatMap {
                        (lemma, i) => if lemma != "0" then Some(suspIdxs(i) -> lemma) else None
                    }.toMap
                lemmas = lemmas.zipWithIndex.map {
                    (lemma, idx) => fixes.getOrElse(idx, lemma)
                }

            val res: Seq[NCToken] = holders.zip(posTags).zip(lemmas).toIndexedSeq.map { case ((h, pos), lemma) =>
                new NCPropertyMapAdapter with NCToken:
                    override def getText: String = h.origin
                    override def getLemma: String = lemma
                    override def getStem: String = stemmer.stem(h.normalized)
                    override def getPos: String = pos
                    override def isStopWord: Boolean = false
                    override def getStartCharIndex: Int = h.start
                    override def getEndCharIndex: Int = h.end
                    override def getLength: Int = h.length
            }

            val stops = swFinder.find(res)

            res.map(tok =>
                if stops.contains(tok) then
                    new NCPropertyMapAdapter with NCToken:
                        override def getText: String = tok.getText
                        override def getLemma: String = tok.getLemma
                        override def getStem: String = tok.getStem
                        override def getPos: String = tok.getPos
                        override def isStopWord: Boolean = true
                        override def getStartCharIndex: Int = tok.getStartCharIndex
                        override def getEndCharIndex: Int = tok.getEndCharIndex
                        override def getLength: Int = tok.getLength
                else
                    tok
            ).asJava
        }