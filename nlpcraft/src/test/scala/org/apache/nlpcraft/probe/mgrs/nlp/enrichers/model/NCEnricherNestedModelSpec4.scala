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
import scala.collection.JavaConverters._

/**
  * Nested Elements test model.
  */
class NCNestedTestModel4 extends NCModelAdapter(
    "nlpcraft.nested3.test.mdl", "Nested Data Test Model", "1.0"
) {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("e1", "//[a-zA-Z0-9]+//"),
            NCTestElement("e2", "the ^^(id == 'e1')^^")
        )

    override def getAbstractTokens: util.Set[String] = Set("e1").asJava
    override def getEnabledBuiltInTokens: util.Set[String] = Set.empty[String].asJava

    @NCIntent("intent=onE2 term(t1)={id == 'e2'}[8, 100]")
    def onAB(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

/**
  * It shouldn't be too slow.
  */
@NCTestEnvironment(model = classOf[NCNestedTestModel4], startClient = true)
class NCEnricherNestedModelSpec4 extends NCTestContext {
    @Test
    def test(): Unit = checkIntent("the a " * 11, "onE2")
}