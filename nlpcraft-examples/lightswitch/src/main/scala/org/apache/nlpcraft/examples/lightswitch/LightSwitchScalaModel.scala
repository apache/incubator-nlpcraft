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
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.*
import org.apache.nlpcraft.nlp.enrichers.NCEnStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.parsers.{NCOpenNLPTokenParser, NCSemanticEntityParser}

/**
  * This example provides very simple implementation for NLI-powered light switch.
  * You can say something like this:
  * <ul>
  *     <li>"Turn the lights off in the entire house."</li>
  *     <li>"Switch on the illumination in the master bedroom closet."</li>
  * </ul>
  * You can easily modify intent callbacks to perform the actual light switching using
  * HomeKit or Arduino-based controllers.
  *
  * See 'README.md' file in the same folder for running and testing instructions.
  */
class LightSwitchScalaModel extends NCModelAdapter(
    NCModelConfig("nlpcraft.lightswitch.java.ex", "LightSwitch Example Model", "1.0"),
    new NCPipelineBuilder().withSemantic("en", "lightswitch_model.yaml").build
):
    /**
      * Intent and its on-match callback.
      *
      * @param actEnt Token from `act` term (guaranteed to be one).
      * @param locEnts Tokens from `loc` term (zero or more).
      * @return Query result to be sent to the REST caller.
      */
    @NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
    def onMatch(
        ctx: NCContext,
        im: NCIntentMatch,
        @NCIntentTerm("act") actEnt: NCEntity,
        @NCIntentTerm("loc") locEnts: List[NCEntity]
    ): NCResult =
        val status = if actEnt.getId == "ls:on" then "on" else "off"
        val locations = if locEnts.isEmpty then "entire house" else locEnts.map(_.mkText).mkString(", ")

        // Add HomeKit, Arduino or other integration here.=
        // By default - just return a descriptive action string.
        NCResult(s"Lights are [$status] in [${locations.toLowerCase}].")