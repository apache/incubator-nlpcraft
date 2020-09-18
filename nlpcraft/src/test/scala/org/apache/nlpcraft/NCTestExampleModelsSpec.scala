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

package org.apache.nlpcraft

import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
import org.junit.jupiter.api.{Assertions, Test}

/**
  * JUnit example models validation.
  */
class NCTestExampleModelsSpec {
    @Test
    def test(): Unit = {
        val models = "" +
            "org.apache.nlpcraft.examples.alarm.AlarmModel," +
            "org.apache.nlpcraft.examples.time.TimeModel," +
            "org.apache.nlpcraft.examples.lightswitch.LightSwitchModel," +
            "org.apache.nlpcraft.examples.echo.EchoModel"

        // Instruct auto-validator what models to test.
        System.setProperty("NLPCRAFT_TEST_MODELS", models)

        // Start model auto-validator.
        Assertions.assertTrue(NCTestAutoModelValidator.isValid(),"See error logs above.")
    }
}
