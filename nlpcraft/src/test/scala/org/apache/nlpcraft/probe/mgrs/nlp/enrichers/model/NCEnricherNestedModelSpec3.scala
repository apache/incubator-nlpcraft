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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.SetHasAsJava

/**
  * Nested Elements test model.
  */
class NCNestedTestModel3 extends NCModelAdapter("nlpcraft.nested3.test.mdl", "Nested Test Model", "1.0") {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("e1", "//[a-zA-Z0-9]+//"),
            NCTestElement("e2", "^^{# == 'e1'}^^")
        )

    override def getAbstractTokens: util.Set[String] = Set("e1").asJava
    override def getEnabledBuiltInTokens: util.Set[String] = Set.empty[String].asJava

    @NCIntent("intent=onE2 term(t1)={# == 'e2'}[12, 100]")
    def onAB(): NCResult = NCResult.text("OK")
}

/**
 * It shouldn't be too slow.
 */
@NCTestEnvironment(model = classOf[NCNestedTestModel3], startClient = true)
class NCEnricherNestedModelSpec3 extends NCTestContext {
    @Test
    def test(): Unit = checkIntent("a " * 15, "onE2")
}
