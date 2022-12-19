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

package org.apache.nlpcraft.nlp.parsers

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.namefind.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.parsers.NCOpenNLPEntityParser

import java.io.*
import java.util
import java.util.Objects
import scala.Option.*
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.Using

/**
  * [[NCOpenNLPEntityParser]] helper.
  */
object NCOpenNLPEntityParser:
    /**
      * Creates [[NCOpenNLPEntityParser]] instance.
      *
      * @param mdl Relative path, absolute path, classpath resource or URL to [[https://opennlp.apache.org/docs/2.0.0/apidocs/opennlp-tools/opennlp/tools/namefind/TokenNameFinderModel.html model]].
      * @return [[NCOpenNLPEntityParser]] instance.
      */
    def apply(mdl: String): NCOpenNLPEntityParser =
        require(mdl != null, "Model source cannot be null.")
        new NCOpenNLPEntityParser(List(mdl))

/**
  *  [[https://opennlp.apache.org/ OpenNLP]] based language independent [[NCEntityParser entity parser]] configured by
  * paths to [[https://opennlp.apache.org/ OpenNLP]] **name finders** models.
  *
  * This parser prepares [[NCEntity]] instances which are detected by given models.
  * These entities are created with ID `opennlp:modelId`, where `modelId` is [[https://opennlp.apache.org/ OpenNLP]] model ID.
  * Also this parser adds `opennlp:modelId:probability` double [[NCPropertyMap metadata]] property to the
  * created entities extracted from related [[https://opennlp.apache.org/ OpenNLP]] model.
  *
  * Some of OpenNLP prepared models can be found [[https://opennlp.sourceforge.net/models-1.5/ here]].
  *
  * **NOTE:** that each input [[NCToken]] can be included into several output [[NCEntity]] instances.
  *
  * @param findersMdlsRes Relative paths, absolute paths, resources or URLs to [[https://opennlp.apache.org/docs/2.0.0/apidocs/opennlp-tools/opennlp/tools/namefind/TokenNameFinderModel.html models]].
  */
class NCOpenNLPEntityParser(findersMdlsRes: List[String]) extends NCEntityParser with LazyLogging:
    require(findersMdlsRes != null && findersMdlsRes.nonEmpty, "Models resources cannot be null or empty.")

    private var finders: Seq[NameFinderME] = _
    private case class Holder(start: Int, end: Int, name: String, probability: Double)

    init()

    /**
      *
      */
    private def init(): Unit =
        val finders = mutable.ArrayBuffer.empty[NameFinderME]
        NCUtils.execPar(
            findersMdlsRes.map(res => () => {
                val f = new NameFinderME(new TokenNameFinderModel(NCUtils.getStream(res)))
                logger.trace(s"Loaded resource: $res")
                finders.synchronized { finders += f }
            }))(ExecutionContext.Implicits.global)

        this.finders = finders.toSeq

    /**
      *
      * @param finder
      * @param words
      */
    private def find(finder: NameFinderME, words: Array[String]): Array[Holder] = finder.synchronized {
        try finder.find(words).map(p => Holder(p.getStart, p.getEnd - 1, p.getType, p.getProb))
        finally finder.clearAdaptiveData()
    }

    /** @inheritdoc */
    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        val txtArr = toks.map(_.getText).toArray

        finders.flatMap(find(_, txtArr)).flatMap(h => {
            def calcIndex(getHolderIndex: Holder => Int): Int =
                toks.find(_.getIndex == getHolderIndex(h)) match
                    case Some(t) => t.getIndex
                    case None => -1

            val i1 = calcIndex(_.start)
            lazy val i2 = calcIndex(_.end)

            Option.when(i1 != -1 && i2 != -1)(
                new NCPropertyMapAdapter with NCEntity:
                    put(s"opennlp:${h.name}:probability", h.probability)

                    override val getTokens: List[NCToken] = toks.flatMap(t => Option.when(t.getIndex >= i1 && t.getIndex <= i2)(t))
                    override val getRequestId: String = req.getRequestId
                    override val getId: String = s"opennlp:${h.name}"
            )
        }).toList