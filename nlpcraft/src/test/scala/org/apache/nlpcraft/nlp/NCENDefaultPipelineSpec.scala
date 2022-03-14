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
import org.apache.nlpcraft.nlp.entity.parser.NCEnSemanticEntityParser
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser
import org.apache.nlpcraft.nlp.util.NCTestModelAdapter
import org.junit.jupiter.api.Test

import scala.util.Using

class NCENDefaultPipelineSpec:
    /**
      *
      * @param cfg
      * @param pipeline
      * @return
      */
    private def mkModel(cfg: NCModelConfig, pipeline: NCModelPipeline): NCModel =
        new NCModelAdapter(cfg, pipeline):
            @NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
            @NCIntentSample(Array(
                "Please, put the light out in the upstairs bedroom.",
            ))
            def onMatch(
                @NCIntentTerm("act") actEnt: NCEntity,
                @NCIntentTerm("loc") locEnts: List[NCEntity]
            ): NCResult =
                val status = if actEnt.getId == "ls:on" then "on" else "off"
                val locations = if locEnts.isEmpty then "entire house" else locEnts.map(_.mkText()).mkString(", ")
                new NCResult(
                    s"Lights are [$status] in [${locations.toLowerCase}].",
                    NCResultType.ASK_RESULT
                )

    @Test
    def test(): Unit =
        val cfg = new NCModelConfig("test.id", "Test model", "1.0")
        // Default EN pipeline with default EN semantic parser.

        val pipeline = new NCModelPipelineBuilder().withLanguage("EN").withEntityParser(new NCEnSemanticEntityParser("models/lightswitch_model.yaml")).build()

        Using.resource(new NCModelClient(mkModel(cfg, pipeline))) { client =>
            println(client.ask("Please, put the light out in the upstairs bedroom.", null, "userId").getBody)
        }