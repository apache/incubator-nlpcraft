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
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils

import java.io.*
import java.util.Objects

/**
  * [[NCTokenEnricher]] built-in English language implementation.
  *
  * It adds <code>swear</code> boolean property to [[NCToken]] instance if word which it represents is swear word.
  *
  * @param res Path to swear words list text resource.
  * Note that [[NCPipelineBuilder.withSemantic()]] methods use for English language
  * [[https://raw.githubusercontent.com/apache/incubator-nlpcraft/external_config/external/badfilter/swear_words.txt NlpCraft Swearword Dictionary]]
  */
//noinspection ScalaWeakerAccess
class NCEnSwearWordsTokenEnricher(res: String) extends NCTokenEnricher with LazyLogging:
    require(res != null, "Swear words model file cannot be null.")

    private final val stemmer = new PorterStemmer
    private var swearWords: Set[String] = _

    init()

    private def init(): Unit =
        swearWords = NCUtils.readTextStream(NCUtils.getStream(res), "UTF-8").
            map(p => stemmer.stem(p.toLowerCase)).toSet
        logger.trace(s"Loaded resource: $res")

    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        toks.foreach(t => t.put("swear", swearWords.contains(stemmer.stem(t.getText.toLowerCase))))

