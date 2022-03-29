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

package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser
import org.apache.nlpcraft.nlp.util.*
import org.junit.jupiter.api.Test

import scala.jdk.CollectionConverters.*
import scala.util.Using

class NCModelClientSpec:
    private def s(e: NCEntity): String =
        s"Entity [id=${e.getId}, text=${e.mkText()}, properties={${e.keysSet().asScala.map(k => s"$k=${e.get(k)}")}}]"

    private def test0(mdl: NCTestModelAdapter): Unit =
        mdl.getPipeline.getEntityParsers.add(NCTestUtils.mkENSemanticParser("models/lightswitch_model.yaml"))

        Using.resource(new NCModelClient(mdl)) { client =>
            val res = client.ask("Lights on at second floor kitchen", null, "userId")

            println(s"Intent: ${res.getIntentId}")
            println(s"Body: ${res.getBody}")

            client.validateSamples()

            val winner = client.getWinnerIntent("Lights on at second floor kitchen", null, "userId")
            println(s"Winner intent: ${winner.getIntentId}")
            println("Entities: \n" + winner.getArguments.asScala.map(p => p.asScala.map(s).mkString(", ")).mkString("\n"))
        }
    /**
      *
      */
    @Test
    def test(): Unit =
        test0(
            new NCTestModelAdapter():
                @NCIntentSample(Array("Lights on at second floor kitchen"))
                @NCIntent("intent=ls term(act)={# == 'ls:on'} term(loc)={# == 'ls:loc'}*")
                def onMatch(@NCIntentTerm("act") act: NCEntity, @NCIntentTerm("loc") locs: List[NCEntity]): NCResult = new NCResult()
        )

    @Test
    def test2(): Unit =
        test0(
            new NCTestModelAdapter():
                @NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
                @NCIntentSample(Array("Lights on at second floor kitchen"))
                def onMatch(@NCIntentTerm("act") act: NCEntity, @NCIntentTerm("loc") locs: List[NCEntity]): NCResult = new NCResult()
        )

