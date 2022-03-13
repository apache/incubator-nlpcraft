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
import org.apache.nlpcraft.nlp.entity.parser.NCEnSemanticEntityParser
import java.util.*
import java.util.stream.Collectors


/**
 * This example provides very simple implementation for NLI-powered light switch.
 * You can say something like this:
 *
 *  - "Turn the lights off in the entire house."
 *  - "Switch on the illumination in the master bedroom closet."
 *
 * You can easily modify intent callbacks to perform the actual light switching using
 * HomeKit or Arduino-based controllers.
 *
 * See 'README.md' file in the same folder for running and testing instructions.
 */
class LightSwitchKotlinModel : NCModelAdapter(
    NCModelConfig("nlpcraft.lightswitch.kotlin.ex", "LightSwitch Example Model", "1.0"),
    NCModelPipelineBuilder().withLanguage("EN").withEntityParser(NCEnSemanticEntityParser("lightswitch_model.yaml")).build()
) {
    /**
     * Intent and its on-match callback.
     *
     * @param actEnt Token from 'act' term (guaranteed to be one).
     * @param locEnts Tokens from 'loc' term (zero or more).
     * @return Query result to be sent to the REST caller.
     */
    @NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
    @NCIntentSample(
        "Turn the lights off in the entire house.",
        "Turn off all lights now",
        "Switch on the illumination in the master bedroom closet.",
        "Get the lights on.",
        "Off the lights on the 1st floor",
        "Lights up in the kitchen.",
        "Please, put the light out in the upstairs bedroom.",
        "Set the lights on in the entire house.",
        "Turn the lights off in the guest bedroom.",
        "Could you please switch off all the lights?",
        "Dial off illumination on the 2nd floor.",
        "Turn down lights in 1st floor bedroom",
        "Lights on at second floor kitchen",
        "Please, no lights!",
        "Kill off all the lights now!",
        "Down the lights in the garage",
        "Lights down in the kitchen!",
        "Turn up the illumination in garage and master bedroom",
        "Turn down all the light now!",
        "No lights in the bedroom, please.",
        "Light up the garage, please!",
        "Kill the illumination now!"
    )
    fun onMatch(
        @NCIntentTerm("act") actEnt: NCEntity,
        @NCIntentTerm("loc") locEnts: List<NCEntity>
    ): NCResult {
        val status = if (actEnt.id == "ls:on") "on" else "off"
        val locations = if (locEnts.isEmpty()) "entire house" else locEnts.stream()
            .map { t: NCEntity -> t.mkText() }
            .collect(Collectors.joining(", "))

        // Add HomeKit, Arduino or other integration here.

        // By default - just return a descriptive action string.
        return NCResult(
            "Lights are [" + status + "] in [" + locations.lowercase(Locale.getDefault()) + "].",
            NCResultType.ASK_RESULT
        )
    }
}