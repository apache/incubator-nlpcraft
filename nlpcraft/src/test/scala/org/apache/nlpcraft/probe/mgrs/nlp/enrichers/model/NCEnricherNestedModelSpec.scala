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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model

import org.apache.nlpcraft.model.NCElement
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCDefaultTestModel, NCEnricherBaseSpec, NCTestUserToken ⇒ usr}
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

/**
  * Nested Elements test model.
  */
class NCNestedTestModel extends NCDefaultTestModel {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("x1", "{test|_} ^^{id() == 'nlpcraft:date'}^^"),
            NCTestElement("x2", "{test1|_} ^^{id() == 'x1'}^^"),
            NCTestElement("x3", "{test2|_} ^^{id() == 'x2'}^^"),
            NCTestElement("y1", "y"),
            NCTestElement("y2", "^^id == 'y1'^^"),
            NCTestElement("y3", "^^id == 'y2'^^ ^^id == 'y2'^^")
        )
}

/**
 * Nested elements model enricher test.
 */
@NCTestEnvironment(model = classOf[NCNestedTestModel], startClient = true)
class NCEnricherNestedModelSpec extends NCEnricherBaseSpec {
    @Test
    def test(): Unit =
        runBatch(
            _ ⇒ checkExists(
                "tomorrow",
                usr(text = "tomorrow", id = "x3")
            ),
            _ ⇒ checkExists(
                "tomorrow yesterday",
                usr(text = "tomorrow", id = "x3"),
                usr(text = "yesterday", id = "x3")
            ),
            _ ⇒ checkExists(
                "y y",
                usr(text = "y y", id = "y3")
            )
        )
}
