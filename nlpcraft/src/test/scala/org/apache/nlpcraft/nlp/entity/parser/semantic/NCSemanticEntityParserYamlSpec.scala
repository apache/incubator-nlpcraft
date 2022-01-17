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

package org.apache.nlpcraft.nlp.entity.parser.semantic

import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.entity.parser.opennlp.NCOpenNlpEntityParser
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.en.NCEnPorterStemmer
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
class NCSemanticEntityParserYamlSpec:
    private val parser = new NCSemanticEntityParser(
        new NCEnPorterStemmer,
        EN_PIPELINE.getTokenParser,
        "models/lightswitch_model.yaml"
    )

    /**
      * 
      * @param txt
      * @param id
      */
    private def check(txt: String, id: String): Unit =
        val req = NCTestRequest(txt)
        val ents = parser.parse(
            req,
            CFG,
            EN_PIPELINE.getTokenParser.tokenize(req.txt)
        ).asScala.toSeq

        NCTestUtils.printEntities(txt, ents)

        val tok = ents.head
        
        require(tok.getId == id)

    /**
      * 
      */
    @Test
    def test(): Unit =
        check("Turn the lights off in the entire house.", "ls:off")