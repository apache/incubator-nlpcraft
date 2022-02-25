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
import org.apache.nlpcraft.examples.lightswitch.ru.*
import org.apache.nlpcraft.nlp.entity.parser.nlp.NCNLPEntityParser
import org.apache.nlpcraft.nlp.entity.parser.semantic.NCSemanticEntityParser
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.en.NCEnSemanticPorterStemmer
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser
import org.apache.nlpcraft.nlp.token.enricher.en.NCStopWordsTokenEnricher

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

class LightSwitchModelRu extends NCModel:
    override val getConfig: NCModelConfig = new NCModelConfig("nlpcraft.lightswitch.ru.ex", "LightSwitch Example Model RU", "1.0")
    override val getPipeline: NCModelPipeline =
        val tp = new NCTokenParserRu
        new NCModelPipelineBuilder(
            tp,
            new NCSemanticEntityParser(new NCSemanticStemmerRu(), tp, "lightswitch_model_ru.yaml")
        ).
            withTokenEnricher(new NCStopWordsTokenEnricherRu()).
            build()

    /**
      * Intent and its on-match callback.
      *
      * @param actEnt Token from `act` term (guaranteed to be one).
      * @param locEnts Tokens from `loc` term (zero or more).
      * @return Query result to be sent to the REST caller.
      */
    @NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
    @NCIntentSample(Array(
        "Выключи свет по всем доме",
        "Выруби электричество!",
        "Включи свет в детской",
        "Включай повсюду освещение",
        "Включайте лампы в детской комнате",
        "Свет на кухне пожалуйста приглуши",
        "Нельзя ли повсюду выключить свет",
        "Пожалуйста без света",
        "Отключи электричесвто в ванной",
        "Выключи, пожалуйста, тут всюду свет",
        "Выключай все!",
        "Свет пожалуйсте везде включи"
    ))
    def onMatch(
        @NCIntentTerm("act") actEnt: NCEntity,
        @NCIntentTerm("loc") locEnts: List[NCEntity]
    ): NCResult =
        val status = if actEnt.getId == "ls:on" then "on" else "off"
        val locations = if locEnts.isEmpty then "entire house" else locEnts.map(_.mkText()).mkString(", ")

        // Add HomeKit, Arduino or other integration here.

        // By default - just return a descriptive action string.
        val res = new NCResult()

        res.setType(NCResultType.ASK_RESULT)
        res.setBody(s"Lights are [$status] in [${locations.toLowerCase}].")

        res