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

import org.apache.nlpcraft.model.NCModelFileAdapter
import org.apache.nlpcraft.model.NCIntentRef
import org.apache.nlpcraft.model.NCIntentSample
import org.apache.nlpcraft.model.NCIntentTerm
import org.apache.nlpcraft.model.NCToken
import org.apache.nlpcraft.model.NCResult
import java.util.stream.Collectors

/**
 * This example provides very simple implementation for NLI-powered light switch.
 * You can say something like this:
 *
 *  * "Turn the lights off in the entire house."
 *  * "Switch on the illumination in the master bedroom closet."
 *
 * You can easily modify intent callbacks to perform the actual light switching using
 * HomeKit or Arduino-based controllers.
 *
 *
 * See 'README.md' file in the same folder for running and testing instructions.
 */
class LightSwitchKotlinModel : NCModelFileAdapter("org/apache/nlpcraft/examples/lightswitch/lightswitch_model.yaml") {
    /**
     * Intent and its on-match callback.
     *
     * @param actTok Token from 'act' term (guaranteed to be one).
     * @param locToks Tokens from 'loc' term (zero or more).
     * @return Query result to be sent to the REST caller.
     */
    @NCIntentRef("ls")
    @NCIntentSample(
        "Turn the lights off in the entire house.",
        "Switch on the illumination in the master bedroom closet.",
        "Get the lights on.",
        "Lights up in the kitchen.",
        "Please, put the light out in the upstairs bedroom.",
        "Set the lights on in the entire house.",
        "Turn the lights off in the guest bedroom.",
        "Could you please switch off all the lights?",
        "Dial off illumination on the 2nd floor.",
        "Please, no lights!",
        "Kill off all the lights now!",
        "No lights in the bedroom, please.",
        "Light up the garage, please!",
        "Kill the illumination now!"
    )
    fun onMatch(
        @NCIntentTerm("act") actTok: NCToken,
        @NCIntentTerm("loc") locToks: List<NCToken>
    ): NCResult {
        val status = if (actTok.id == "ls:on") "on" else "off"
        val locations = if (locToks.isEmpty()) "entire house" else locToks.stream()
            .map { t: NCToken -> t.meta<Any>("nlpcraft:nlp:origtext") as String }
            .collect(Collectors.joining(", "))

        // Add HomeKit, Arduino or other integration here.

        // By default - just return a descriptive action string.
        return NCResult.text("Lights are [" + status + "] in [" + locations.toLowerCase() + "].")
    }
}