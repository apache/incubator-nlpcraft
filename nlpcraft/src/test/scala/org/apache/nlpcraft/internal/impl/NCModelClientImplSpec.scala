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
import org.apache.nlpcraft.internal.impl.scan.NCTestModelScala.NCTestModelScalaObj
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.en.NCEnSemanticPorterStemmer
import org.apache.nlpcraft.nlp.util.NCTestModelAdapter
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.Test

import scala.jdk.CollectionConverters.*

class NCModelClientImplSpec:
    /**
      *
      */
    @Test
    def test(): Unit =
        val mdl =
            new NCTestModelAdapter():
                //@NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
                @NCIntent("intent=ls term(act)={# == 'ls:on'} term(loc)={# == 'ls:loc'}*")
                def onMatch(@NCIntentTerm("act") act: NCEntity, @NCIntentTerm("loc") locs: List[NCEntity]): NCResult =
                    val ncRes = new NCResult()
                    ncRes.setType(NCResultType.ASK_RESULT)
                    ncRes.setBody(if locs.isEmpty then "entire house" else locs.map(_.mkText()).mkString(","))

                    ncRes

        mdl.getPipeline.getEntityParsers.add(
            new NCSemanticEntityParser(
                new NCEnSemanticPorterStemmer,
                EN_PIPELINE.getTokenParser,
                "models/lightswitch_model.yaml"
            )
        )

        val client = new NCModelClientImpl(mdl)

        val res = client.askSync("Lights on at second floor kitchen", null, "userId")

        println(res.getIntentId)
        println(res.getBody)



