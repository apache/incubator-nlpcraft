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

package org.apache.nlpcraft.examples.lightswitch;

import org.apache.nlpcraft.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This example provides very simple implementation for NLI-powered light switch.
 * You can say something like this:
 * <ul>
 *     <li>"Turn the lights off in the entire house."</li>
 *     <li>"Switch on the illumination in the master bedroom closet."</li>
 * </ul>
 * You can easily modify intent callbacks to perform the actual light switching using
 * HomeKit or Arduino-based controllers.
 * <p>
 * See 'README.md' file in the same folder for running and testing instructions.
 */
public class LightSwitchJavaModel extends NCModelFileAdapter {
    public LightSwitchJavaModel() {
        // Loading the model from the file in the classpath.
        super("lightswitch_model.yaml");
    }

    /**
     * Intent and its on-match callback.
     *
     * @param actTok Token from 'act' term (guaranteed to be one).
     * @param locToks Tokens from 'loc' term (zero or more).
     * @return Query result to be sent to the REST caller.
     */
    @NCIntentRef("ls")
    @NCIntentSample({
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
    })
    NCResult onMatch(
        @NCIntentTerm("act") NCToken actTok,
        @NCIntentTerm("loc") List<NCToken> locToks) {
        String status = actTok.getId().equals("ls:on") ? "on" : "off";
        String locations = locToks.isEmpty() ?
            "entire house" :
            locToks.stream().map(NCToken::getOriginalText).collect(Collectors.joining(", "));

        // Add HomeKit, Arduino or other integration here.

        // By default - just return a descriptive action string.
        return NCResult.text("Lights are [" + status + "] in [" + locations.toLowerCase() + "].");
    }

    @Override
    public String getId() {
        return super.getId() + ".java";
    }
}
