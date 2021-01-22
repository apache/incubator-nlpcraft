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

package org.apache.nlpcraft.server.nlp.core.opennlp

import io.opencensus.trace.Span
import opennlp.tools.lemmatizer.DictionaryLemmatizer
import opennlp.tools.postag.{POSModel, POSTagger, POSTaggerME}
import org.apache.ignite.IgniteCache
import org.apache.nlpcraft.common.extcfg.NCExternalConfigManager
import org.apache.nlpcraft.common.extcfg.NCExternalConfigType.OPENNLP
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.nlp.core.opennlp.NCOpenNlpTokenizer
import org.apache.nlpcraft.common.pool.NCThreadPoolContext
import org.apache.nlpcraft.common.{NCService, U}
import org.apache.nlpcraft.server.ignite.NCIgniteHelpers._
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.nlp.core.{NCNlpParser, NCNlpWord}
import resource.managed

import scala.util.control.Exception.catching

/**
  * OpenNLP parser implementation.
  */
object NCOpenNlpParser extends NCService with NCNlpParser with NCIgniteInstance with NCThreadPoolContext{
    @volatile private var tagger: POSTagger = _
    @volatile private var lemmatizer: DictionaryLemmatizer = _
    @volatile private var cache: IgniteCache[String, Array[String]] = _

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        require(NCOpenNlpTokenizer.isStarted)

        ackStarting()

        U.executeParallel(
            () ⇒ {
                tagger =
                    managed(NCExternalConfigManager.getStream(OPENNLP, "en-pos-maxent.bin", span)) acquireAndGet { in ⇒
                        new POSTaggerME(new POSModel(in))
                    }
            },
            () ⇒ {
                lemmatizer =
                    managed(NCExternalConfigManager.getStream(OPENNLP, "en-lemmatizer.dict", span)) acquireAndGet { in ⇒
                        new DictionaryLemmatizer(in)
                    }
            }
        )

        catching(wrapIE) {
            cache = ignite.cache[String, Array[String]]("opennlp-cache")
        }

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        ackStopping()

        cache = null

        ackStopped()
    }

    /**
     *
     * @param normTxt Normalized text.
     * @param parent Optional parent span.
     * @return Parsed words.
     */
    override def parse(normTxt: String, parent: Span = null): Seq[NCNlpWord] =
        startScopedSpan("parse", parent, "normTxt" → normTxt) { _ ⇒
            // Can be optimized.
            val (toks, words, poses, lemmas) =
                this.synchronized {
                    val toks = NCOpenNlpTokenizer.tokenize(normTxt).toArray
                    val words = toks.map(_.token)
                    val poses = tagger.tag(words)
    
                    require(toks.length == poses.length)
    
                    var lemmas = lemmatizer.lemmatize(words, poses).toSeq
    
                    require(toks.length == lemmas.length)
    
                    // Hack.
                    // For some reasons lemmatizer dictionary (en-lemmatizer.dict) marks some words with non-existent POS 'NNN'
                    // Valid POS list: https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
                    // Example of dictionary records:
                    // ...
                    // time	JJ	time
                    // time	NNN	time
                    // ...
                    // time-ball NN	time-ball
                    // ...
                    val suspIdxs: Seq[Int] =
                        lemmas.
                            zip(poses).
                            zipWithIndex.flatMap {
                                // "0" is flag that lemma cannot be obtained for some reasons.
                                case ((lemma, pos), i) ⇒ if (lemma == "O" && pos == "NN") Some(i) else None
                            }
    
                    if (suspIdxs.nonEmpty) {
                        val fixes: Map[Int, String] =
                            lemmatizer.
                                lemmatize(suspIdxs.map(i ⇒ words(i)).toArray, suspIdxs.map(_ ⇒ "NNN").toArray).
                                zipWithIndex.
                                flatMap { case (lemma, i) ⇒ if (lemma != "0") Some(suspIdxs(i) → lemma) else None }.toMap
    
                        lemmas = lemmas.zipWithIndex.map { case (lemma, idx) ⇒ fixes.getOrElse(idx, lemma) }
                    }
    
                    (toks, words, poses, lemmas)
                }
    
            cache += normTxt → words
    
            toks.zip(poses).zip(lemmas).map { case ((tok, pos), lemma) ⇒
                val normalWord = tok.token.toLowerCase
    
                NCNlpWord(
                    word = tok.token,
                    normalWord = normalWord,
                    // "0" is flag that lemma cannot be obtained for some reasons.
                    lemma = if (lemma == "O") normalWord else lemma.toLowerCase,
                    stem = NCNlpCoreManager.stemWord(normalWord),
                    pos = pos,
                    start = tok.from,
                    end = tok.to,
                    length = tok.length
                )
            }
        }
}
