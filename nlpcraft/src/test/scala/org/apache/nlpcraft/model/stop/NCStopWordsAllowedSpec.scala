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

package org.apache.nlpcraft.model.stop

import org.apache.nlpcraft.model.{NCContext, NCElement, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.language.implicitConversions

/**
  *
  */
class NCStopWordsAllowedModelAdapter extends NCModelAdapter("nlpcraft.test", "Test Model", "1.0") {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("a", "the test"))

    override def onContext(ctx: NCContext): NCResult = {
        ctx.getVariants.asScala.forall(t => t.asScala.exists(_.isStopWord) == isStopWordsAllowed)

        NCResult.text("OK")
    }
}
/**
  *
  */
class NCStopWordsAllowedModel extends NCStopWordsAllowedModelAdapter {
    override def isStopWordsAllowed: Boolean = true
}

/**
  *
  */
class NCStopWordsNotAllowedModel extends NCStopWordsAllowedModelAdapter {
    override def isStopWordsAllowed: Boolean = false
}

/**
  *
  */
@NCTestEnvironment(model = classOf[NCStopWordsAllowedModel], startClient = true)
class NCStopWordsAllowedSpec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkResult("the", "OK")
        checkResult("the test", "OK")
        checkResult("the the test", "OK")
        checkResult("test the the test", "OK")
    }
}

/**
  *
  */
@NCTestEnvironment(model = classOf[NCStopWordsNotAllowedModel], startClient = true)
class NCStopWordsNotAllowedSpec extends NCStopWordsAllowedSpec