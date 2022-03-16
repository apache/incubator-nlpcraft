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
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.*

import java.util
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

/**
  *
  */
class NCSemanticEntityParserJsonSpec:
    private val parser = NCTestUtils.mkENSemanticParser("models/alarm_model.json")

    /**
      * 
      * @param txt
      * @param id
      * @param elemData
      */
    private def check(txt: String, id: String, elemData: Option[Map[String, Any]] = None): Unit =
        val req = NCTestRequest(txt)
        val ents = parser.parse(
            req,
            CFG,
            EN_PIPELINE.getTokenParser.tokenize(req.txt)
        ).asScala.toSeq

        NCTestUtils.printEntities(txt, ents)

        val tok = ents.head
        
        require(tok.getId == id)
        elemData match
            case Some(m) => m.foreach { (k, v) => require(tok.get[Any](s"$id:$k") == v) }
            case None => // No-op.

    /**
      * 
      */
    @Test
    def test(): Unit =
        check(
            "Ping me in 3 minutes tomorrow",
            "x:alarm",
            // File contains these data for element.
            elemData = Option(Map("testKey" -> "testValue"))
        )