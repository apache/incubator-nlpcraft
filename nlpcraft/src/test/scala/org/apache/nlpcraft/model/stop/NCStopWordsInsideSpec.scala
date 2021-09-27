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

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCModelAdapter, NCResult}
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.language.implicitConversions

/**
  *
  */
class NCStopWordsInsideModel extends NCModelAdapter("nlpcraft.test", "Test Model", "1.0") {
    override def getElements: util.Set[NCElement] = Set(NCTestElement("complex", "a b"))

    @NCIntent("intent=i term={# == 'complex'}")
    def onI(ctx: NCIntentMatch): NCResult = {
        require(ctx.getContext.getVariants.size() == 1)
        require(ctx.getContext.getVariants.asScala.head.asScala.size == 1)
        require(ctx.getContext.getVariants.asScala.head.asScala.head.getNormalizedText == ctx.getContext.getRequest.getNormalizedText)

        NCResult.text("OK")
    }
}

/**
  *
  */
@NCTestEnvironment(model = classOf[NCStopWordsInsideModel], startClient = true)
class NCStopWordsInsideSpec extends NCTestContext {
    @Test
    def test(): Unit = {
        checkIntent("a b", "i")
        checkIntent("a the b", "i")
        checkIntent("a , b", "i")
        checkIntent("a, b", "i")
        checkIntent("a, the b", "i")
        checkIntent("a, the, b", "i")
    }
}

class NCStopWordsInsideSparseModel extends NCStopWordsInsideModel {
    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true
}

@NCTestEnvironment(model = classOf[NCStopWordsInsideSparseModel], startClient = true)
class NCStopWordsInsideSparseSpec extends NCStopWordsInsideSpec {
    @Test
    def test2(): Unit = {
        // TODO: extend it.
    }
}

