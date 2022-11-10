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

package org.apache.nlpcraft.nlp

import org.apache.nlpcraft.*
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

/**
 *
 */
object NCEntityParserSpec:
    private val parser = new NCEntityParser :
        override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
            val txt = req.getText
            if txt.length > 2 && txt.startsWith("'") && txt.endsWith("'") && !txt.drop(1).reverse.drop(1).contains("'") then
                val ent: NCEntity = new NCPropertyMapAdapter with NCEntity :
                    override def getTokens: List[NCToken] = toks
                    override def getRequestId: String = req.getRequestId
                    override def getId: String = "quoted"
                List(ent)
            else
                List.empty

    private val mdl =
        new NCModel(CFG, new NCPipelineBuilder().withTokenParser(EN_TOK_PARSER).withEntityParser(parser).build):
            @NCIntent("intent=quoted term(any)={# == 'quoted'}")
            def onMatch(ctx: NCContext, im: NCIntentMatch): NCResult = TEST_RESULT

/**
  *
  */
class NCEntityParserSpec extends AnyFunSuite:
    test("test") {
        Using.resource(new NCModelClient(NCEntityParserSpec.mdl)) { client =>
            val intentId = client.ask( "'some quoted text'", "usrId").getIntentId.get

            require(intentId == "quoted", s"Unexpected intent:  $intentId")
        }
    }