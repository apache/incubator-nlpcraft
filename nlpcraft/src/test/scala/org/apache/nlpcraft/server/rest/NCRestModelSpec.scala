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

import org.apache.nlpcraft.model.NCElement
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, SetHasAsJava, SetHasAsScala}

/**
  * Note that context word server should be started.
  */
@NCTestEnvironment(model = classOf[RestTestModel], startClient = false)
class NCRestModelSpec1 extends NCRestSpec {
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
}

class RestTestModelExt extends RestTestModel {
    override def getMacros: util.Map[String, String] = {
        Map(
            "<M1>" -> "mtest1 {x|_}",
            "<M2>" -> "<M1> mtest2 {mtest3|_}"
        ).asJava
    }

    override def getElements: util.Set[NCElement] = {
        (
            super.getElements.asScala ++
            Set(
                NCTestElement("eExt1", "<M1>", "<M1> more"),
                NCTestElement("eExt2", Seq("<M1>", "<M1> more"), Map("v1"-> Seq("<M2>", "<M2> more"), "v2" -> Seq("<M2>")))
            )
        ).asJava
    }
}
/**
  *
  */
@NCTestEnvironment(model = classOf[RestTestModelExt], startClient = false)
class NCRestModelSpec2 extends NCRestSpec {
    @Test
    def testSyns(): Unit = {
        // Note that checked values are valid for current configuration of `RestTestModelExt` model.
        def post0(elemId: String, valsShouldBe: Boolean): Unit =
            post("model/syns", "mdlId" -> "rest.test.model", "elmId" -> elemId)(
                ("$.status", (status: String) => assertEquals("API_OK", status)),
                ("$.synonyms", (data: ResponseList) => assertTrue(!data.isEmpty)),
                ("$.synonymsExp", (data: ResponseList) => assertTrue(!data.isEmpty)),
                ("$.values", (data: java.util.Map[Object, Object]) =>
                    if (valsShouldBe) assertTrue(!data.isEmpty) else assertTrue(data.isEmpty)),
                ("$.valuesExp", (data: java.util.Map[Object, Object]) =>
                    if (valsShouldBe) assertTrue(!data.isEmpty) else assertTrue(data.isEmpty)
                )
            )

        post0("eExt1", valsShouldBe = false)
        post0("eExt2", valsShouldBe = true)

        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> "UNKNOWN", "elmId" -> "UNKNOWN")
        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> "rest.test.model", "elmId" -> "UNKNOWN")
        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> ("A" * 33), "elmId" -> "UNKNOWN")
        postError("model/syns", 400, "NC_INVALID_FIELD", "mdlId" -> "rest.test.model", "elmId" -> ("A" * 65))
        postError("model/syns", 400, "NC_ERROR", "mdlId" -> "rest.test.model")
    }

    @Test
    def testModelInfo(): Unit = {
        post("model/info", "mdlId" -> "rest.test.model")(
            ("$.status", (status: String) => assertEquals("API_OK", status)),
            ("$.model", (data: java.util.Map[Object, Object]) => assertTrue(!data.isEmpty))
        )

        postError("model/info", 400, "NC_INVALID_FIELD", "mdlId" -> "UNKNOWN")
        postError("model/info", 400, "NC_ERROR")
    }
}
