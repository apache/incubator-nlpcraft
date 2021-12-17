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

import org.apache.nlpcraft.*

import java.io.*
import java.util.List as JList
import opennlp.tools.lemmatizer.*
import opennlp.tools.postag.*
import opennlp.tools.stemmer.*
import opennlp.tools.tokenize.*
import org.apache.nlpcraft.internal.util.NCUtils

import scala.jdk.CollectionConverters.*

object NCOpenNlpImpl:
    /**
      *
      * @param tokMdlSrc Local filesystem path, resources file path or URL for OpenNLP tokenizer model.
      * @param posMdlSrc Local filesystem path, resources file path or URL for OpenNLP tagger model.
      * @param lemmaDicSrc Local filesystem path, resources file path or URL for OpenNLP lemmatizer dictionary.
      * @return
      */
    def apply(tokMdlSrc: String, posMdlSrc: String, lemmaDicSrc: String): NCOpenNlpImpl =
        new NCOpenNlpImpl(NCUtils.getStream(tokMdlSrc), NCUtils.getStream(posMdlSrc), NCUtils.getStream(lemmaDicSrc))

/**
  *
  * @param tokMdlIn
  * @param posMdlIn
  * @param lemmaDicIn
  */
class NCOpenNlpImpl(tokMdlIn: InputStream, posMdlIn: InputStream, lemmaDicIn: InputStream):
    private val tokenizer = new TokenizerME(new TokenizerModel(tokMdlIn))
    private val tagger = new POSTaggerME(new POSModel(posMdlIn))
    private val lemmatizer = new DictionaryLemmatizer(lemmaDicIn)
    private val stemmer = new PorterStemmer
    private var addStopWords = List.empty[String]
    private var exclStopWords = List.empty[String]

    /**
      *
      * @return
      */
    def getAdditionalStopWords: JList[String] = addStopWords.asJava

    /**
      *
      * @return
      */
    def getExcludedStopWords: JList[String] = exclStopWords.asJava

    /**
      *
      * @param addStopWords
      */
    def setAdditionalStopWords(addStopWords: JList[String]): Unit = this.addStopWords = addStopWords.asScala.toList

    /**
      *
      * @param exclStopWords
      */
    def setExcludedStopWords(exclStopWords: JList[String]): Unit = this.exclStopWords = exclStopWords.asScala.toList

    /**
      *
      * @param req
      * @return
      */
    def parse(req: NCRequest): JList[NCToken] =
        val sen = req.getNormalizedText

        case class TokenHolder(origin: String, normalized: String, start: Int, end: Int, length: Int)

        val holders = tokenizer.tokenizePos(sen).map( t => {
            val txt = t.getCoveredText(sen).toString
            TokenHolder(txt, txt.toLowerCase, t.getStart, t.getEnd, t.length())
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
                    case (lemma, i) => if lemma != "0" then Some(suspIdxs(i) -> lemma) else None
                }.toMap
            lemmas = lemmas.zipWithIndex.map {
                case (lemma, idx) => fixes.getOrElse(idx, lemma)
            }

        holders.zip(posTags).zip(lemmas).toIndexedSeq.map { case ((h, pos), lemma) =>
            new NCParameterizedAdapter with NCToken:
                override def getOriginalText: String = h.origin
                override def getNormalizedText: String = h.normalized
                override def getLemma: String = lemma
                override def getStem: String = stemmer.stem(h.normalized)
                override def getPos: String = pos
                override def isStopWord: Boolean = true // TODO: implement
                override def getStartCharIndex: Int = h.start
                override def getEndCharIndex: Int = h.end
                override def getLength: Int = h.length
        }.asJava
