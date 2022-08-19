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
import org.apache.nlpcraft.internal.util.NCUtils
import org.apache.nlpcraft.nlp.parsers.NCSemanticEntityParser
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
    private val semParser: NCSemanticEntityParser = NCTestUtils.mkEnSemanticParser("models/lightswitch_model.yaml")

    /**
      * 
      * @param txt
      * @param id
      */
    private def check(txt: String, id: String): Unit =
        val req = NCTestRequest(txt)
        val ents = semParser.parse(req, CFG, EN_TOK_PARSER.tokenize(req.txt))

        NCTestUtils.printEntities(txt, ents)

        val tok = ents.head
        
        require(tok.getId == id)

    /**
      * 
      */
    @Test
    def test(): Unit = check("Turn the lights off in the entire house.", "ls:off")