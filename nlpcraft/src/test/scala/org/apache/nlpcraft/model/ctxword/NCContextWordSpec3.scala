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

package org.apache.nlpcraft.model.ctxword

import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentSample, NCIntentTerm, NCModel, NCResult, NCToken}
import org.junit.jupiter.api.{Assertions, Test}

import java.util
import java.util.Collections
import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava, SetHasAsJava}

/**
  * Test model.
  */
class NCContextWordSpecModel3 extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    override def isPermutateSynonyms: Boolean = true
    override def isSparse: Boolean = true

    override def getMacros: util.Map[String, String] =
        Map(
            "<ACTION>" -> "{turn|switch|dial|let|set|get|put}",
            "<KILL>" -> "{shut|kill|stop|eliminate}",
            "<ENTIRE_OPT>" -> "{entire|full|whole|total|_}",
            "<FLOOR_OPT>" -> "{upstairs|downstairs|{1st|2nd|3rd|4th|5th|top|ground} floor|_}",
            "<TYPE>" -> "{room|closet|attic|loft|{store|storage} {room|_}}",
            "<LIGHT>" -> "{all|_} {it|them|light|illumination|lamp|lamplight}"
        ).asJava

    private def mkElement(id: String, group: Option[String], syns: String*): NCElement =
        new NCElement {
            override def getId: String = id
            override def getSynonyms: util.List[String] = syns.asJava
            override def getGroups: util.List[String] =
                group match {
                    case Some(g) => Collections.singletonList(g)
                    case None => super.getGroups
                }
        }

    override def getElements: util.Set[NCElement] =
        Set(
            mkElement(
                "ls:loc",
                None,
                "<ENTIRE_OPT> <FLOOR_OPT> {kitchen|library|closet|garage|office|playroom|{dinning|laundry|play} <TYPE>}",
                "<ENTIRE_OPT> <FLOOR_OPT> {master|kid|children|child|guest|_} {bedroom|bathroom|washroom|storage} {<TYPE>|_}",
                "<ENTIRE_OPT> {house|home|building|{1st|first} floor|{2nd|second} floor}"
            ),
            mkElement(
                "ls:on",
                Some("act"),
                "<ACTION> {on|up|_} <LIGHT> {on|up|_}",
                "<LIGHT> {on|up}"
            ),
            mkElement(
                "ls:off",
                Some("act"),
                "<ACTION> <LIGHT> {off|out}",
                "{<ACTION>|<KILL>} {off|out} <LIGHT>",
                "<KILL> <LIGHT>",
                "<LIGHT> <KILL>",
                "no <LIGHT>"
            )
        ).asJava

    @NCIntent("intent=ls term(act)={has(tok_groups(), 'act')} term(loc)={tok_id() == 'ls:loc'}*")
    @NCIntentSample(Array(
        "Turn the lights off in the entire house.",
        "Turn off all lights now",
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
    ))
    def onMatch(
        @NCIntentTerm("act") actTok: NCToken,
        @NCIntentTerm("loc") locToks: List[NCToken]
    ): NCResult = {
        val status = if (actTok.getId == "ls:on") "on" else "off"
        val locations =
            if (locToks.isEmpty)
                "entire house"
            else
                locToks.map(_.meta[String]("nlpcraft:nlp:origtext")).mkString(", ")

        // Add HomeKit, Arduino or other integration here.

        // By default - just return a descriptive action string.
        NCResult.text(s"Lights are [$status] in [${locations.toLowerCase}].")
    }
}

/**
  *
  */
class NCContextWordSpec3 {
    @Test
    private[ctxword] def test(): Unit = {
        System.setProperty("NLPCRAFT_TEST_MODELS", classOf[NCContextWordSpecModel3].getName)

        Assertions.assertTrue(NCTestAutoModelValidator.isValid(),"See error logs above.")
    }
}
