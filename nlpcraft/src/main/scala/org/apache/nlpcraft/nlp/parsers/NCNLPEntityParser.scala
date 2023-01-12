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

import org.apache.nlpcraft.*

import java.util
import java.util.stream.Collectors

/**
  * [[NCNLPEntityParser]] helper.
  */
private object NCNLPEntityParser:
    private val entityType: String = "nlp:entity"

import NCNLPEntityParser.*

/**
  * Common NLP data [[NCEntityParser entity parser]].
  *
  * This parser converts list of input [[NCToken]] instances **one-to-one** to the list of [[NCEntity]] instances with
  * type **nlp:entity**. All [[NCEntity]] instances in the result list will contain the following
  * [[NCPropertyMap metadata]] properties:
  *  - `nlp:entity:text` - token's text.
  *  - `nlp:entity:index` - token's index in the input sentence.
  *  - `nlp:entity:startCharIndex` - token text's first character index in the input sentence.
  *  - `nlp:entity:endCharIndex` - token text 's last character index in the input sentence.
  *
  * Note that [[NCEntity]] instances inherit all [[NCToken]] [[NCPropertyMap metadata]] properties from its
  * corresponding token with new name that is prefixed with **'nlp:entity:'**. For example, for token property **prop**
  * the corresponding inherited entity property name will be **nlp:entity:prop**.
  *
  * @param predicate Predicate which allows to filter list of converted [[NCToken]] instances. Only tokens that
  *     satisfy given predicate will convert to entity by this parser. By default all [[NCToken]] instances are
  *     converted.
  */
class NCNLPEntityParser(predicate: NCToken => Boolean = _ => true) extends NCEntityParser:
    require(predicate != null, "Predicate cannot be null.")

    /** @inheritdoc */
    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        toks.filter(predicate).map(t =>
            new NCPropertyMapAdapter with NCEntity:
                put(s"$entityType:text", t.getText)
                put(s"$entityType:index", t.getIndex)
                put(s"$entityType:startCharIndex", t.getStartCharIndex)
                put(s"$entityType:endCharIndex", t.getEndCharIndex)

                t.keysSet.foreach(key => put(s"$entityType:$key", t(key)))

                override val getTokens: List[NCToken] = List(t)
                override val getRequestId: String = req.getRequestId
                override val getType: String = entityType
        )
