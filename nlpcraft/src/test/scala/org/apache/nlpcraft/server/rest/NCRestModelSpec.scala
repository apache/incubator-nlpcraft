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

package org.apache.nlpcraft.server.rest

import org.apache.nlpcraft.NCTestEnvironment
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import scala.jdk.CollectionConverters.ListHasAsScala

/**
  * Note that context word server should be started.
  */
@NCTestEnvironment(model = classOf[RestTestModel], startClient = false)
class NCRestModelSpec extends NCRestSpec {
    @Test
    def testSugsyn(): Unit = {
        def extract(data: JList[java.util.Map[String, Object]]): Seq[Double] =
            data.asScala.map(_.get("score").asInstanceOf[Number].doubleValue()).toSeq

        // Note that checked values are valid for current configuration of `RestTestModel` model.
        post("model/sugsyn", "mdlId" -> "rest.test.model")(
            ("$.status", (status: String) => assertEquals("API_OK", status)),
            ("$.result.suggestions[:1].a.*", (data: JList[java.util.Map[String, Object]]) => {
                val scores = extract(data)

                assertTrue(scores.nonEmpty)
                assertTrue(scores.forall(s => s >= 0 && s <= 1))
                assertTrue(scores.exists(_ >= 0.5))
                assertTrue(scores.exists(_ <= 0.5))
            })
        )
        post("model/sugsyn", "mdlId" -> "rest.test.model", "minScore" -> 0.5)(
            ("$.status", (status: String) => assertEquals("API_OK", status)),
            ("$.result.suggestions[:1].a.*", (data: JList[java.util.Map[String, Object]]) => {
                val scores = extract(data)

                assertTrue(scores.nonEmpty)
                assertTrue(scores.forall(s => s >= 0.5 && s <= 1))
            })
        )

        postError("model/sugsyn", 400, "NC_INVALID_FIELD", "mdlId" -> "UNKNOWN")
        postError("model/sugsyn", 400, "NC_INVALID_FIELD", "mdlId" -> "rest.test.model", "minScore" -> 2)
        postError("model/sugsyn", 400, "NC_ERROR")
    }

    @Test
    def testSyns(): Unit = {
        // Note that checked values are valid for current configuration of `RestTestModel` model.
        post("model/syns", "mdlId" -> "rest.test.model", "elmId" -> "x")(
            ("$.status", (status: String) => assertEquals("API_OK", status)),
            ("$.synonyms", (syns: ResponseList) => {
                println("synonyms="+syns)

                assertTrue(!syns.isEmpty)
            }),
            ("$.values", (vals: java.util.Map[Object, Object]) => {
                println("values="+vals)

                assertTrue(vals.isEmpty)
            })
        )
        post("model/syns", "mdlId" -> "rest.test.model", "elmId" -> "valElem")(
            ("$.status", (status: String) => assertEquals("API_OK", status)),
            ("$.synonyms", (syns: ResponseList) => {
                println("synonyms="+syns)

                assertTrue(!syns.isEmpty)
            }),
            ("$.values", (vals: java.util.Map[Object, Object]) => {
                println("values="+vals)

                assertTrue(!vals.isEmpty)
            })
        )

        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> "UNKNOWN", "elmId" -> "UNKNOWN")
        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> "rest.test.model", "elmId" -> "UNKNOWN")
        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> ("A" * 33), "elmId" -> "UNKNOWN")
        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> "rest.test.model", "elmId" -> ("A" * 65))
        postError("model/syns", 400, "NC_ERROR", "mdlId" -> "rest.test.model")
    }
}
