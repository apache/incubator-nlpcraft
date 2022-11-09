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
class NCEntityParserSpec extends AnyFunSuite:
    private val quoteEntityParser = new NCEntityParser:
        override def parse(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): List[NCEntity] =
            if
                req.getText.length > 2 &&
                req.getText.head == '\'' &&
                req.getText.last == '\'' &&
                !req.getText.drop(1).reverse.drop(1).exists(_ == '\'')
            then
                val e: NCEntity = new NCPropertyMapAdapter with NCEntity :
                    override def getTokens: List[NCToken] = toks
                    override def getRequestId: String = req.getRequestId
                    override def getId: String = "quoted"
                List(e)
            else
                List.empty

    private val mdl: NCModel =
        new NCModel(
            NCModelConfig("test.id", "Test model", "1.0"),
            new NCPipelineBuilder().
                withTokenParser(EN_TOK_PARSER).
                withEntityParser(quoteEntityParser).
                build
        ):
            @NCIntent("intent=i term(any)={# == 'quoted'}")
            def onMatch(ctx: NCContext, im: NCIntentMatch): NCResult = NCResult(ctx.getTokens.map(_.getText).mkString)

    test("test") {
        Using.resource(new NCModelClient(mdl)) { client =>
            val req = "'some quoted text'"
            val res = client.ask(req, "usrId")

            require(res.getIntentId.get == "i", s"Unexpected intent:  ${res.getIntentId.get}")
            require(res.getBody == req.replaceAll(" ", ""), s"Unexpected body:  ${res.getBody}")
        }
    }