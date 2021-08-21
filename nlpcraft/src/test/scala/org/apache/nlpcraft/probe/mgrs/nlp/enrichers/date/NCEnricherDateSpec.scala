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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.date

import org.apache.nlpcraft.NCTestEnvironment
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{NCDefaultTestModel, NCEnricherBaseSpec, NCTestDateToken => dte}
import org.junit.jupiter.api.Test

/**
  * Date enricher test.
  */
@NCTestEnvironment(model = classOf[NCDefaultTestModel], startClient = true)
class NCEnricherDateSpec extends NCEnricherBaseSpec {
    /**
      *
      * @throws Exception
      */
    @Test
    def test(): Unit =
        runBatch(
            Seq("today",
                "1900",
                "1900 year",
                "from 1900 year",
                "between 1900 and 1905",
                "between 1900 and 1905 years"
            ).map(txt => {
                val f: Unit => Unit = _ => checkExists(txt, dte(text = txt))

                f
            }):_*)
}