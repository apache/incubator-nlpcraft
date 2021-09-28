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

package org.apache.nlpcraft.examples.cargps

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

/**
  *
  */
@NCTestEnvironment(model = classOf[CarGpsModel], startClient = true)
class NCCarGpsModelSpec extends NCTestContext {
    @Test
    def testNavigate(): Unit = {
        checkIntent("hey truck, drive to 21 x x drive", "navigate")
        checkIntent("hey car, navigate to 21 table rock drive", "navigate")
        checkIntent("howdy, truck - drive to 2121 5th avenue please", "navigate")
    }

    @Test
    def testCancel(): Unit = {
        checkIntent("Hey truck - stop the navigation!", "cancel")
        checkIntent("Howdy, car, please cancel the routing now.", "cancel")
        checkIntent("Hi car - stop the route.", "cancel")
        checkIntent("Hi car - stop the navigation...", "cancel")
        checkIntent("Howdy truck - quit navigating.", "cancel")
        checkIntent("Hi car - finish off the driving.", "cancel")
        checkIntent("Hi car - cancel the journey.", "cancel")
    }
}
