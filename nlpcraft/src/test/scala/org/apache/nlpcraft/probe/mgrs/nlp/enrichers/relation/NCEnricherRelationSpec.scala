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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.relation

import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCEnricherBaseSpec, NCTestNlpToken ⇒ nlp, NCTestRelationToken ⇒ rel, NCTestUserToken ⇒ usr}
import org.junit.jupiter.api.Test

/**
  * Relation enricher test.
  */
class NCEnricherRelationSpec extends NCEnricherBaseSpec {
    /**
      *
      * @throws Exception
      */
    @Test
    def test(): Unit = {
        runBatch(
            _ ⇒ checkExists(
                "compare V1 and V2",
                rel(text = "compare", `type` = "compare", indexes = Seq(1, 3), note = "V"),
                usr(text = "V1", id = "V"),
                nlp(text = "and", isStop = true),
                usr(text = "V2", id = "V")
            )
        )
    }
}
