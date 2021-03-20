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
class NCNestedTestModel5 extends NCModelAdapter(
    "nlpcraft.nested5.test.mdl", "Nested Data Test Model", "1.0"
) {
    override def getElements: util.Set[NCElement] =
        Set(
            NCTestElement("cityWrapper", "^^[cityAlias]{id() == 'nlpcraft:city'}^^"),
        )
    @NCIntent(
        "intent=bigCity " +
        "term(city)={" +
        "    id() == 'cityWrapper' && " +
        "    get(meta_part('cityAlias', 'nlpcraft:city:citymeta'), 'population') >= 10381222" +
        "}"
    )
    private def onBigCity(ctx: NCIntentMatch): NCResult = NCResult.text("OK")
}

/**
  *
  */
@NCTestEnvironment(model = classOf[NCNestedTestModel5], startClient = true)
class NCEnricherNestedModelSpec5 extends NCTestContext {
    @Test
    def test(): Unit = checkIntent("moscow", "bigCity")
}