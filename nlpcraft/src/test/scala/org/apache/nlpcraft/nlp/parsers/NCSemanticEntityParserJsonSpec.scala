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
import nlp.parsers.*
import nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

import java.util
import scala.collection.mutable
import scala.concurrent.ExecutionContext

/**
  *
  */
class NCSemanticEntityParserJsonSpec extends AnyFunSuite:
    private val semParser = NCTestUtils.mkEnSemanticParser("models/alarm_model.json")

    /**
      * 
      * @param txt
      * @param id
      * @param elemData
      */
    private def check(txt: String, id: String, elemData: Option[Map[String, Any]] = None): Unit =
        val req = NCTestRequest(txt)
        val ents = semParser.parse(req, CFG, EN_TOK_PARSER.tokenize(req.txt))

        NCTestUtils.printEntities(txt, ents)

        val tok = ents.head
        
        require(tok.getType == id)
        elemData match
            case Some(m) => m.foreach { (k, v) => require(tok[Any](s"$id:$k") == v) }
            case None => // No-op.

    /**
      * 
      */
    test("test") {
        check(
            "Ping me in 3 minutes tomorrow",
            "x:alarm",
            // File contains these data for element.
            elemData = Map("testKey" -> "testValue").?
        )
    }