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

import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCDefaultTestModel, NCEnricherBaseSpec, NCTestUserToken ⇒ usr}
import org.junit.jupiter.api.Test

/**
 * Nested elements model enricher test.
 */
class NCEnricherNestedModelSpec extends NCEnricherBaseSpec {
    override def getModelClass: Option[Class[_ <: NCDefaultTestModel]] = Some(classOf[NCNestedTestModel])

    @Test
    def test(): Unit = {
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
}
