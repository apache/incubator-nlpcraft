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

package org.apache.nlpcraft.model.synonyms

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult, NCValue}
import org.apache.nlpcraft.server.rest.NCRestSpec
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava}

class NCSynonymsValuesSpecModel extends NCModelAdapter("nlpcraft.syns.vals.test.mdl", "Synonyms Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            new NCElement {
                override def getId: String = "elemId"
                override def getSynonyms: util.List[String] = Seq("eSynonym").asJava
                override def getValues: util.List[NCValue] = Seq(
                    new NCValue {
                        override def getName: String = "v1"
                        override def getSynonyms: util.List[String] = Seq("v1Synonym").asJava
                    },
                    new NCValue {
                        override def getName: String = "v2"
                        override def getSynonyms: util.List[String] = Seq.empty.asJava
                    }
                ).asJava
            },
            new NCElement {
                override def getId: String = "elemEmpty"
                override def getSynonyms: util.List[String] = Seq.empty.asJava
            }

        ).asJava

    @NCIntent("intent=i term(t)={# == 'elemId'}")
    def callback(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

// Checks that elementID and value name added as synonyms.
@NCTestEnvironment(model = classOf[NCSynonymsValuesSpecModel], startClient = true)
class NCSynonymsValuesSpec1 extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("elemId", "i")
        checkIntent("eSynonym", "i")
        checkIntent("v1", "i")
        checkIntent("v1Synonym", "i")
        checkIntent("v2", "i")
    }
}

// Checks valida synonyms representation.
@NCTestEnvironment(model = classOf[NCSynonymsValuesSpecModel], startClient = false)
class NCSynonymsValuesSpec2 extends NCRestSpec {
    @Test
    def test(): Unit = {
        post("model/syns", "mdlId" -> "nlpcraft.syns.vals.test.mdl", "elmId" -> "elemId")(
            ("$.status", (status: String) => assertEquals("API_OK", status)),
            ("$.synonyms", (data: ResponseList) => {
                println(s"Synonyms: $data")

                assertTrue(data.size() == 1)
            }),
            ("$.values", (data: java.util.Map[Object, Object]) => {
                println(s"Values: $data")

                val v1 = data.get("v1").asInstanceOf[java.util.List[Map[String, String]]]

                println(s"Value 1 data : $v1")

                assertTrue(v1.size == 1)

                val v2 = data.get("v2").asInstanceOf[java.util.List[Map[String, String]]]

                println(s"Value 2 data : $v2")

                assertTrue(v2.isEmpty)
            })
        )

        post("model/syns", "mdlId" -> "nlpcraft.syns.vals.test.mdl", "elmId" -> "elemEmpty")(
            ("$.status", (status: String) => assertEquals("API_OK", status)),
            ("$.synonyms", (data: ResponseList) => {
                println(s"Synonyms: $data")

                assertTrue(data.isEmpty)
            }),
            ("$.values", (data: java.util.Map[Object, Object]) => {
                println(s"Values: $data")

                assertTrue(data.isEmpty)
            })
        )
    }
}

