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

package org.apache.nlpcraft.nlp.entity.parser

import org.apache.nlpcraft.*

import java.util
import java.util.stream.Collectors

/**
  *
  */
object NCNLPEntityParser:
    private def id = "nlp:token"

import NCNLPEntityParser.*

/**
  *
  */
class NCNLPEntityParser extends NCEntityParser:
    override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
        toks.map(t =>
            new NCPropertyMapAdapter with NCEntity:
                put(s"$id:text", t.getText)
                put(s"$id:index", t.getIndex)
                put(s"$id:startCharIndex", t.getStartCharIndex)

                t.keysSet.foreach(key => put(s"$id:$key", t.get(key)))

                override val getTokens: List[NCToken] = List(t)
                override val getRequestId: String = req.getRequestId
                override val getId: String = id
        )
