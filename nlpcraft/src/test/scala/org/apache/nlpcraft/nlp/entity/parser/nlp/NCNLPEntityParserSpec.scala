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

package org.apache.nlpcraft.nlp.entity.parser.nlp

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.entity.parser.opennlp.NCOpenNLPEntityParser
import org.apache.nlpcraft.nlp.util.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.*

import java.util
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

/**
  *
  */
class NCNLPEntityParserSpec:
    private val parser = new NCNLPEntityParser()

    /**
      *
      */
    @Test
    def test(): Unit =
        val req = NCTestRequest("I had the lunch")
        val toks = EN_PIPELINE.getTokenParser.tokenize(req.txt)
        val entities = parser.parse(req, CFG, toks).asScala.toSeq

        NCTestUtils.printEntities(req.txt, entities)

        require(entities.sizeIs == toks.size())
        entities.zipWithIndex.foreach { (ent, idx) =>
            require(ent.getTokens.size() == 1)
            require(ent.getTokens.get(0) == toks.get(idx))
        }
