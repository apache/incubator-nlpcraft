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
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils as U

/**
  * Dictionary-based "known-word" [[NCTokenEnricher token enricher]].
  *
  * This enricher adds `dict` boolean [[NCPropertyMap metadata]] property to the [[NCToken token]]
  * instance if word it represents is a known dictionary word, i.e. the configured dictionary contains this word's
  * lemma. The value `true` of the metadata property indicates that this word's lemma is found in the dictionary,
  * `false` value indicates otherwise.
  *
  * **NOTE:** this implementation requires `lemma` string [[NCPropertyMap metadata]] property that contains
  * token's lemma. You can configure [[NCOpenNLPTokenEnricher]] for required language that provides this
  * metadata property before this enricher in your [[NCPipeline pipeline]].
  *
  * @param dictRes Relative path, absolute path, classpath resource or URL to the dictionary.
  *         The dictionary should have a simple plain text format with *one lemma per line*, empty lines are skipped,
  *         duplicates ignored, lines starting with **#** symbol will be treated as comments and ignored.
  *         Note that the search in the dictionary is implemented using words' **lemma** and case is ignored.
  */
//noinspection DuplicatedCode,ScalaWeakerAccess
class NCDictionaryTokenEnricher(dictRes: String) extends NCTokenEnricher with LazyLogging:
    require(dictRes != null, "Dictionary resource cannot be null.")

    private var dict: Set[String] = _

    init()

    private def init(): Unit = dict = U.readLines(res = dictRes, filterText = true, convert = _.toLowerCase, log = logger).toSet

    /** @inheritdoc */
    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        toks.foreach(t => t.put("dict", dict.contains(U.getProperty(t, "lemma"))))
