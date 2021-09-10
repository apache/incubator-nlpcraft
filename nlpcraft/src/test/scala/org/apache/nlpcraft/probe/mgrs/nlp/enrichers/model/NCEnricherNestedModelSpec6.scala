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

import org.apache.nlpcraft.model.{NCContext, NCElement, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.{IterableHasAsScala, SetHasAsJava}

/**
  * Nested Elements test model.
  */
class NCNestedTestModel6 extends NCModelAdapter("nlpcraft.nested6.test.mdl", "Nested Test Model", "1.0") {
    override def getAbstractTokens: util.Set[String] = Set("nlpcraft:date").asJava

    override def getElements: util.Set[NCElement] =
        Set(NCTestElement("dateWrapper", "^^{# == 'nlpcraft:date'}^^"))

    override def onContext(ctx: NCContext): NCResult = {
        require(ctx.getRequest.getNormalizedText == "today")

        println(s"Variants:\n${ctx.getVariants.asScala.mkString("\n")}")

        // `nlpcraft:date` will be deleted.
        require(ctx.getVariants.size() == 1)

        NCResult.text("OK")
    }
}
/**
  *
  */
@NCTestEnvironment(model = classOf[NCNestedTestModel6], startClient = true)
class NCEnricherNestedModelSpec6 extends NCTestContext {
    @Test
    def test(): Unit = checkResult("today", "OK")
}