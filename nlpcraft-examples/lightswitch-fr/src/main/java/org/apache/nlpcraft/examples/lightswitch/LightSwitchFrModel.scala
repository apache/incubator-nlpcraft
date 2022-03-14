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

import com.google.gson.Gson
import org.apache.nlpcraft.*
import org.apache.nlpcraft.examples.lightswitch.nlp.entity.parser.NCFrSemanticEntityParser
import org.apache.nlpcraft.examples.lightswitch.nlp.token.enricher.{NCFrLemmaPosTokenEnricher, NCFrStopWordsTokenEnricher}
import org.apache.nlpcraft.examples.lightswitch.nlp.token.parser.NCFrTokenParser
import org.apache.nlpcraft.nlp.entity.parser.{NCNLPEntityParser, NCSemanticEntityParser}
import org.apache.nlpcraft.nlp.token.enricher.NCENStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser

import java.util
import scala.jdk.CollectionConverters.*

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
class LightSwitchFrModel extends NCModelAdapter(
    new NCModelConfig("nlpcraft.lightswitch.fr.ex", "LightSwitch Example Model FR", "1.0"),
    new NCModelPipelineBuilder().
        withTokenParser(new NCFrTokenParser()).
        withTokenEnricher(new NCFrLemmaPosTokenEnricher()).
        withTokenEnricher(new NCFrStopWordsTokenEnricher()).
        withEntityParser(new NCFrSemanticEntityParser("lightswitch_model_fr.yaml")).
        build()
):
    /**
      * Intent and its on-match callback.
      *
      * @param actEnt Token from `act` term (guaranteed to be one).
      * @param locEnts Tokens from `loc` term (zero or more).
      * @return Query result to be sent to the REST caller.
      */
    @NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
    @NCIntentSample(Array(
        "Éteins l“allumier dans toute la maison",
        "Coupez l'électricité!",
        "Allume la lumière dans la chambre des enfants",
        "Allume l'éclairage partout",
        "Allumer les lampes dans la chambre des enfants",
        "Éteins la lumière dans la cuisine, s'il te plaît",
        "Est-il possible d'éteindre les lumières partout?",
        "S'il vous plaît sans lumière",
        "Couper l'électricité dans la salle de bain",
        "Éteins la lumière, s'il te plaît.",
        "Éteignez tout!",
        "Allume la lumière partout s'il te plaît ",
        "Allume la lampe dans la cuisine"
    ))
    def onMatch(
        @NCIntentTerm("act") actEnt: NCEntity,
        @NCIntentTerm("loc") locEnts: List[NCEntity]
    ): NCResult =
        val action = if actEnt.getId == "ls:on" then "allumer" else "éteindre"
        val locations = if locEnts.isEmpty then "toute la maison" else locEnts.map(_.mkText()).mkString(", ")

        // Add HomeKit, Arduino or other integration here.

        // By default - just return a descriptive action string.

        new NCResult(
            new Gson().toJson(Map("locations" -> locations, "action" -> action).asJava),
            NCResultType.ASK_RESULT
        )