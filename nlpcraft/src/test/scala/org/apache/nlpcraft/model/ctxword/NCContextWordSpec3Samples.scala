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

import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentSample, NCIntentTerm, NCModel, NCResult, NCToken, NCValue}
import org.junit.jupiter.api.{Assertions, Test}

import java.util.{Collections, Optional}
import java.{lang, util}
import scala.jdk.CollectionConverters.{MapHasAsJava, SeqHasAsJava, SetHasAsJava}

object NCContextWordSpecModel3 {
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
    private def mkValuesElement(id: String, conf: Double, valSyns: String*): NCElement =
        new NCElement {
            override def getId: String = id
            override def getCategoryConfidence: Optional[lang.Double] = Optional.of(conf)
            override def getValues: util.List[NCValue] = valSyns.map(p => new NCValue {
                override def getName: String = p
                override def getSynonyms: util.List[String] = Collections.singletonList(p)
            }).asJava
        }
}

import NCContextWordSpecModel3._

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
            "<ENTIRE_OPT>" -> "{entire|full|whole|total|_}"
        ).asJava

    override def getAbstractTokens: util.Set[String] = Set("ls:part:place", "ls:part:floor", "ls:part:placeType", "ls:part:light").asJava

    override def getElements: util.Set[NCElement] =
        Set(
            // Abstract element. Used for top level element `ls:loc`. Note, that this element is defined via context word categories.
            mkValuesElement(
                id = "ls:part:place",
                conf = 0.7,
                valSyns = "room", "closet", "attic", "loft", "kitchen", "library", "closet", "garage", "office", "playroom", "bedroom", "washroom"
            ),
            // Abstract element. Used for top level element `ls:loc`.
            mkElement(
                id =  "ls:part:floor",
                group = None,
                syns = "{house|home|building|_} {upstairs|downstairs|{1st|2nd|3rd|4th|5th|top|ground} floor|_}"
            ),
            // Abstract element. Used for top level element `ls:loc`.
            mkElement(
                id =  "ls:part:placeType",
                group = None,
                syns = "{dinning|laundry|play|master|kid|children|child|guest|_}"
            ),
            // Abstract element. Used for top level elements `ls:on` and `ls:of`.
            mkElement(
                id =  "ls:part:light",
                group = None,
                syns = "{all|_} {light|illumination|lamp|lamplight|it|them}"
            ),

            // Top level element. Used in intents.
            // Part `ls:part:place` is mandatory.
            // Parts `ls:part:floor` and `ls:part:placeType` are optional.
            // Parts can be extracted from `ls:loc` to specify certain location point.
            mkElement(
                id =  "ls:loc",
                group = None,
                syns =
                    "<ENTIRE_OPT> ^^{tok_id() == 'ls:part:floor'}^^? ^^{tok_id() == 'ls:part:place'}^^ ^^{tok_id() == 'ls:part:placeType'}^^?",
                    "<ENTIRE_OPT> ^^{tok_id() == 'ls:part:floor'}^^? ^^{tok_id() == 'ls:part:placeType'}^^? ^^{tok_id() == 'ls:part:place'}^^",
            ),
            // Top level element. Used in intents. It's parts help to catch this element, after they can be ignored.
            mkElement(
                id = "ls:on",
                group = Some("act"),
                syns =
                    "<ACTION> {on|up|_} ^^{tok_id() == 'ls:part:light'}^^ {on|up|_}",
                    "^^{tok_id() == 'ls:part:light'}^^ {on|up}"
            ),
            // Top level element. Used in intents. It's parts help to catch this element, after they can be ignored.
            mkElement(
                id = "ls:off",
                group = Some("act"),
                syns =
                    "<ACTION> ^^{tok_id() == 'ls:part:light'}^^ {off|out}",
                    "{<ACTION>|<KILL>} {off|out} ^^{tok_id() == 'ls:part:light'}^^",
                    "<KILL> ^^{tok_id() == 'ls:part:light'}^^",
                    "^^{tok_id() == 'ls:part:light'}^^ <KILL>",
                    "no ^^{tok_id() == 'ls:part:light'}^^"
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

        // By default - just return a descriptive action string.
        NCResult.text(s"Lights are [$status] in [${locations.toLowerCase}].")
    }
}

/**
  * Verifies samples set.
  */
class NCContextWordSpec3Samples {
    @Test
    private[ctxword] def testSamples(): Unit = {
        System.setProperty("NLPCRAFT_TEST_MODELS", classOf[NCContextWordSpecModel3].getName)

        Assertions.assertTrue(NCTestAutoModelValidator.isValid(),"See error logs above.")
    }
}

/**
  *  Extra values set.
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel3], startClient = true)
class NCContextWordSpec3Extra extends NCTestContext {
    @Test
    private[ctxword] def testValues(): Unit = {
        // Look at `ls:type3` element definition.
        // `bedroom` is defined, but 'bathroom' and 'hallway' are not
        // (detected as `ls:type3` by context word category enricher.)
        checkIntent("Switch on the illumination in the master bathroom closet.", "ls")
        checkIntent("Switch on the illumination in the master hallway closet.", "ls")
    }
}