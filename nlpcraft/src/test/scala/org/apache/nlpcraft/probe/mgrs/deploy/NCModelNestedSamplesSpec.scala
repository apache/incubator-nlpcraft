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

package org.apache.nlpcraft.probe.mgrs.deploy

import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe
import org.apache.nlpcraft.model.{NCIntent, NCIntentSample, NCModelAdapter, NCResult}
import org.apache.nlpcraft.probe.mgrs.model.NCModelManager
import org.junit.jupiter.api.Test

/**
 *
 */
class NCModelNested extends NCModelAdapter("nlpcraft.samples.test.mdl", "Test Model", "1.0") {
    /**
     *
     * @return
     */
    @NCIntentSample(Array("a"))
    @NCIntent("intent=nested term={tok_id() == 'a'}*")
    def m(): NCResult = NCResult.text("OK")
}

/**
 *
 */
class NCModelWrapper extends NCModelNested

/**
 *
 */
class NCModelNestedSamplesSpec {
    /**
     *
     */
    @Test
    def test(): Unit = {
        try {
            NCEmbeddedProbe.start("nlpcraft.conf", java.util.Collections.singleton(classOf[NCModelWrapper].getName))

            val mdls = NCModelManager.getAllModels()

            require(mdls.size == 1)

            require(mdls.head.samples.nonEmpty)
        }
        finally
            NCEmbeddedProbe.stop()
    }
}