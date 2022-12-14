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

package org.apache.nlpcraft.nlp.enrichers

import com.typesafe.scalalogging.LazyLogging
import opennlp.tools.lemmatizer.DictionaryLemmatizer
import opennlp.tools.postag.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.*

import java.io.*
import scala.concurrent.ExecutionContext

/**
  * [[https://opennlp.apache.org/ OpenNLP]] based language independent [[NCTokenEnricher token enricher]]. This
  * enricher adds `lemma` and `pos` (part-of-speech) string [[NCPropertyMap metadata]] property to the [[NCToken token]]
  * instance. Learn more about lemmas [[https://en.wikipedia.org/wiki/Lemma_(morphology) here]] and about part-of-speech
  * [[https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html here]].
  *
  * This OpenNLP enricher requires PoS and lemma models. Some of OpenNLP community models can be found
  * [[https://opennlp.sourceforge.net/models-1.5/ here]].
  *
  * @param posMdlRes Relative path, absolute path or URL to
  *         [[https://opennlp.apache.org/docs/2.0.0/apidocs/opennlp-tools/opennlp/tools/postag/POSTaggerME.html POSTaggerME]] model.
  * @param lemmaDicRes Relative path, absolute path or URL to
  *         [[https://opennlp.apache.org/docs/2.0.0/apidocs/opennlp-tools/opennlp/tools/lemmatizer/DictionaryLemmatizer.html DictionaryLemmatizer]] model.
  */
class NCOpenNLPTokenEnricher(posMdlRes: String = null, lemmaDicRes: String = null) extends NCTokenEnricher with LazyLogging:
    private var tagger: POSTaggerME = _
    private var lemmatizer: DictionaryLemmatizer = _

    init()

    private def init(): Unit =
        NCUtils.execPar(
            Seq(
                () => {
                    if posMdlRes != null then
                        tagger = new POSTaggerME(new POSModel(NCUtils.getStream(posMdlRes)))
                        logger.trace(s"Loaded resource: $posMdlRes")
                    else logger.warn("POS tagger is not configured.")
                },
                () => {
                    if lemmaDicRes != null then
                        lemmatizer = new DictionaryLemmatizer(NCUtils.getStream(lemmaDicRes))
                        logger.trace(s"Loaded resource: $lemmaDicRes")
                    else logger.warn("Lemmatizer is not configured.")
                }
            )
        )(ExecutionContext.Implicits.global)

    /** @inheritdoc */
    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        val txts = toks.map(_.getText).toArray

        this.synchronized {
            val poses = if tagger != null then tagger.tag(txts) else txts.map(_ => "")
            var lemmas = if lemmatizer != null then lemmatizer.lemmatize(txts, poses) else txts

            require(toks.sizeIs == poses.length && toks.sizeIs == lemmas.length)

            // For some reasons lemmatizer (en-lemmatizer.dict) marks some words with non-existent POS 'NNN'
            // Valid POS list: https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
            val suspIdxs = lemmas.zip(poses).zipWithIndex.flatMap {
                // "0" is flag that lemma cannot be obtained for some reasons.
                case ((lemma, pos), i) => Option.when(lemma == "O" && pos == "NN")(i)
            }

            if suspIdxs.nonEmpty && lemmatizer != null then
                val fixes = lemmatizer.
                    lemmatize(suspIdxs.map(i => txts(i)), suspIdxs.map(_ => "NNN")).
                    zipWithIndex.
                    flatMap { (lemma, i) => Option.when(lemma != "0")(suspIdxs(i) -> lemma) }.toMap
                lemmas = lemmas.zipWithIndex.map {
                    (lemma, idx) => fixes.getOrElse(idx, lemma)
                }

            toks.zip(poses).zip(lemmas).foreach { case ((t, pos), lemma) =>
                t.put("pos", pos)
                t.put("lemma", lemma)
                () // Otherwise - NPE.
            }
        }
