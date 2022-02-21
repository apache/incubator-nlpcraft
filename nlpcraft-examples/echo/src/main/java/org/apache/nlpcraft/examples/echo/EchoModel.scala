package org.apache.nlpcraft.examples.echo

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.Gson
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.entity.parser.nlp.NCNLPEntityParser
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser

import java.util

/**
  * Echo example data model.
  * <p>
  * For any user input this example returns JSON representation of the query context
  * corresponding to that input. This is a simple demonstration of the JSON output
  * and of most of the NLPCraft-provided data that a user defined model can operate on.
  * <p>
  * This example doesn't define a model and simply implements `onContext(...)` callback
  * method with a necessary logic.
  * <p>
  * See `README.md` file in the same folder for running instructions.
  */
class EchoModel(tokMdlSrc: String, posMdlSrc: String, lemmaDicSrc: String) extends NCModelAdapter(
    new NCModelConfig("nlpcraft.echo.ex", "Echo Example Model", "1.0"),
    new NCModelPipelineBuilder(
        new NCOpenNLPTokenParser(tokMdlSrc, posMdlSrc, lemmaDicSrc),
        new NCNLPEntityParser() // TODO: Required at least one parser.
    ).build()
):
    override def onContext(ctx: NCContext): NCResult =
        val req = ctx.getRequest

        val sm = new util.HashMap[String, Any]()
        sm.put("normalizedText", req.getText)
        sm.put("srvReqId", req.getRequestId)
        sm.put("receiveTimestamp", req.getReceiveTimestamp)
        sm.put("isUserAdmin", req.getUserId)
        sm.put("variants", ctx.getVariants)

        val map = new util.HashMap[String, Any]()
        map.put("srvReqId", ctx.getRequest.getRequestId)
        map.put("sentence", sm)

        val res = new NCResult()
        res.setType(NCResultType.ASK_RESULT)
        res.setBody(new Gson().toJson(map))

        res