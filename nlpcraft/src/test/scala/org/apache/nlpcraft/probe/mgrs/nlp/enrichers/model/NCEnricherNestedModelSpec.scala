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
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCDefaultTestModel, NCEnricherBaseSpec, NCTestUserToken => usr, NCTestNlpToken => nlp}
import org.apache.nlpcraft.{NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

/**
  * Nested Elements test model.
  */
class NCNestedTestModel1 extends NCDefaultTestModel {
    override def isPermutateSynonyms: Boolean = false
    override def isSparse: Boolean = false

    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("x1", "{test|_} ^^{tok_id() == 'nlpcraft:date'}^^"),
            NCTestElement("x2", "{test1|_} ^^{tok_id() == 'x1'}^^"),
            NCTestElement("x3", "{test2|_} ^^{tok_id() == 'x2'}^^"),
            NCTestElement("y1", "y"),
            NCTestElement("y2", "^^{tok_id() == 'y1'}^^"),
            NCTestElement("y3", "^^{tok_id() == 'y2'}^^ ^^{tok_id() == 'y2'}^^")
        )
}

/**
 * Nested elements model enricher test.
 */
@NCTestEnvironment(model = classOf[NCNestedTestModel1], startClient = true)
class NCEnricherNestedModelSpec1 extends NCEnricherBaseSpec {
    @Test
    def test(): Unit =
        runBatch(
            _ => checkExists(
                "tomorrow",
                usr(text = "tomorrow", id = "x3")
            ),
            _ => checkExists(
                "tomorrow yesterday",
                usr(text = "tomorrow", id = "x3"),
                usr(text = "yesterday", id = "x3")
            ),
            _ => checkExists(
                "y y",
                usr(text = "y y", id = "y3")
            )
        )
}

class NCNestedTestModel2 extends NCNestedTestModel1 {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true
}

/**
  * Nested elements model enricher test.
  */
@NCTestEnvironment(model = classOf[NCNestedTestModel2], startClient = true)
class NCEnricherNestedModelSpec2 extends NCEnricherNestedModelSpec1 {
    @Test
    def test2(): Unit =
        runBatch(
            _ => checkExists(
                "test tomorrow",
                usr(text = "test tomorrow", id = "x3")
            ),
            _ => checkExists(
                "tomorrow test",
                usr(text = "tomorrow test", id = "x3")
            ),
            _ => checkExists(
                "test xxx tomorrow",
                usr(text = "test tomorrow", id = "x3"),
                nlp(text = "xxx"),
            ),
            _ => checkExists(
                "y the y",
                usr(text = "y y", id = "y3"),
                nlp(text = "the", isStop = true)
            ),
            _ => checkExists(
                "y xxx y",
                usr(text = "y y", id = "y3"),
                nlp(text = "xxx")
            ),
            _ => checkExists(
                "aaa y xxx y",
                nlp(text = "aaa"),
                usr(text = "y y", id = "y3"),
                nlp(text = "xxx")
            )
        )
}