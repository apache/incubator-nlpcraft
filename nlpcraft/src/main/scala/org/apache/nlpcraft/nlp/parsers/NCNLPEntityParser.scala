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
object NCNLPEntityParser:
    private val id: String = "nlp:entity"

import org.apache.nlpcraft.nlp.parsers.NCNLPEntityParser.*

/**
  *  NLP data [[NCEntityParser entity parser]].
  *
  * This parser converts list of input [[NCToken]] instances to list of [[NCEntity]] instances with ID **nlp:entity**.
  * All [[NCEntity]] instances contain following mandatory [[NCPropertyMap metadata]] properties:
  *  - nlp:entity:text
  *  - nlp:entity:index
  *  - nlp:entity:startCharIndex
  *  - nlp:entity:endCharIndex
  *
  *  Also created [[NCEntity]] instances receive all another [[NCPropertyMap metadata]] properties
  *  which were added by configured in [[NCPipeline pipeline]] token [[org.apache.nlpcraft.NCTokenEnricher enrichers]].
  *  These properties identifiers will be prefixed by **nlp:entity:**.
  *
  *  @param predicate Predicate which allows to filter list of converted [[NCToken]] instances.
  *  By default all [[NCToken]] instances converted.
  */
class NCNLPEntityParser(predicate: NCToken => Boolean = _ => true) extends NCEntityParser:
    require(predicate != null)

    /** @inheritdoc */
    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        toks.filter(predicate).map(t =>
            new NCPropertyMapAdapter with NCEntity:
                put(s"$id:text", t.getText)
                put(s"$id:index", t.getIndex)
                put(s"$id:startCharIndex", t.getStartCharIndex)
                put(s"$id:endCharIndex", t.getEndCharIndex)

                t.keysSet.foreach(key => put(s"$id:$key", t(key)))

                override val getTokens: List[NCToken] = List(t)
                override val getRequestId: String = req.getRequestId
                override val getId: String = id
        )
