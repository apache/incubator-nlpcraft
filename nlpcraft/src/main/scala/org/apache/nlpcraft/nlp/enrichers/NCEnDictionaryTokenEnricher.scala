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

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils

/**
  * "Known-word" [[NCTokenEnricher enricher]] for English language.
  *
  * This enricher adds `dict` boolean [[NCPropertyMap metadata]] property to the [[NCToken token]]
  * instance if word it represents is a known English word, i.e. the English dictionary contains this word's
  * lemma. The value `true` of the metadata property indicates that this word's lemma is found in the dictionary,
  * `false` value indicates otherwise.
  *
  * Implementation uses the [[https://en.wikipedia.org/wiki/Moby_Project Moby Project]] English dictionary.
  *
  * **NOTE:** this implementation requires `lemma` string [[NCPropertyMap metadata]] property that contains
  * token's lemma. You can configure [[NCOpenNLPTokenEnricher]] that provides this metadata property before
  * this enricher in your [[NCPipeline pipeline]].
  */
//noinspection DuplicatedCode,ScalaWeakerAccess
class NCEnDictionaryTokenEnricher extends NCTokenEnricher:
    private var dict: Set[String] = _

    init()

    private def init(): Unit = dict = NCUtils.readResource("moby/354984si.ngl", "iso-8859-1").toSet
    private def getLemma(t: NCToken): String = t.get("lemma").getOrElse(throw new NCException("Lemma not found in token."))

    override def enrich(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit =
        toks.foreach(t => t.put("dict", dict.contains(getLemma(t))))
