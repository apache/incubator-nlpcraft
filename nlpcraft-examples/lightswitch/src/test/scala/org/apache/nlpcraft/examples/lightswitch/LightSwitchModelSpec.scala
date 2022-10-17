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

package org.apache.nlpcraft.examples.lightswitch

import org.apache.nlpcraft.*
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

/**
  * JUnit models validation.
  */
class LightSwitchModelSpec extends AnyFunSuite:
    test("test") {
        Using.resource(new NCModelClient(new LightSwitchModel())) { client =>
            def check(txt: String): Unit =
                require(client.debugAsk(txt, "userId", true).getIntentId == "ls")

            check("Turn the lights off in the entire house.")
            check("Turn off all lights now")
            check("Switch on the illumination in the master bedroom closet.")
            check("Get the lights on.")
            check("Off the lights on the 1st floor")
            check("Lights up in the kitchen.")
            check("Please, put the light out in the upstairs bedroom.")
            check("Set the lights on in the entire house.")
            check("Turn the lights off in the guest bedroom.")
            check("Could you please switch off all the lights?")
            check("Dial off illumination on the 2nd floor.")
            check("Turn down lights in 1st floor bedroom")
            check("Lights on at second floor kitchen")
            check("Please, no lights!")
            check("Kill off all the lights now!")
            check("Down the lights in the garage")
            check("Lights down in the kitchen!")
            check("Turn up the illumination in garage and master bedroom")
            check("Turn down all the light now!")
            check("No lights in the bedroom, please.")
            check("Light up the garage, please!")
            check("Kill the illumination now!")
        }
    }
