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

package org.apache.nlpcraft.model

import org.apache.nlpcraft.NCTestElement
import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
import org.junit.jupiter.api.{Assertions, Test}

import java.util
import scala.language.implicitConversions

/**
  * Sample annotation test model.
  */
class NCIntentSampleSpecModel extends NCModelAdapter(
    "nlpcraft.sample.ann.model.test", "Sample annotation Test Model", "1.0"
) {
    private implicit def convert(s: String): NCResult = NCResult.text(s)

    override def getElements: util.Set[NCElement] = Set(NCTestElement("x1"), NCTestElement("x2"))

    @NCIntent("intent=intent1 term~{tok_id()=='x1'}")
    @NCIntentSample(Array("x1", "x1"))
    @NCIntentSample(Array("unknown", "unknown"))
    private def onX1(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntentSampleRef("samples.txt")
    @NCIntent("intent=intent2 term~{tok_id()=='x2'}")
    private def onX2(ctx: NCIntentMatch): NCResult = "OK"
}

/**
  * Sample annotation test.
  */
class NCIntentSampleSpec {
    @Test
    def test(): Unit = {
        System.setProperty("NLPCRAFT_TEST_MODELS", "org.apache.nlpcraft.model.NCIntentSampleSpecModel")

        // Note that this validation can print validation warnings for this 'NCIntentSampleSpecModel' model.
        // Its is expected behaviour because it is not the model that is tested, but validation itself.
        // Start model auto-validator.
        NCTestAutoModelValidator.isValid()
    }
}
