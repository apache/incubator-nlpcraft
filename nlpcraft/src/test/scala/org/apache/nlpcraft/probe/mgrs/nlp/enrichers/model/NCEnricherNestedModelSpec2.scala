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

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util

/**
  * Nested Elements test model.
  */
class NCNestedTestModel21 extends NCModelAdapter("nlpcraft.nested2.test.mdl", "Nested Test Model", "1.0") {
    override def getElements: util.Set[NCElement] = {
        // Note - it defines one simple and one DSL synonyms.
        // But it should be caught by long (IDL) variant (for `10 word`)
        Set(NCTestElement("e1", "{^^{tok_id() == 'nlpcraft:num'}^^|_} word"))
    }

    @NCIntent("intent=onE1 term(t1)={tok_id() == 'e1'}")
    def onAB(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    @NCIntent("intent=onNumAndE1 term(t1)={tok_id() == 'nlpcraft:num'} term(t2)={tok_id() == 'e1'}")
    def onNumAndE1(ctx: NCIntentMatch): NCResult = NCResult.text("OK")

    override def isPermutateSynonyms: Boolean = false
    override def isSparse: Boolean = false
}

/**
 * Nested elements model enricher test.
 */
@NCTestEnvironment(model = classOf[NCNestedTestModel21], startClient = true)
class NCEnricherNestedModelSpec21 extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("word", "onE1")
        checkIntent("10 word", "onE1")
        checkIntent("11 12 word", "onNumAndE1")
    }
}

/**
  * Nested Elements test model.
  */
class NCNestedTestModel22 extends NCNestedTestModel21 {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true
}

/**
  * Nested elements model enricher test.
  */
@NCTestEnvironment(model = classOf[NCNestedTestModel22], startClient = true)
class NCEnricherNestedModelSpec22 extends NCEnricherNestedModelSpec21

