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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
import org.apache.nlpcraft.model.{NCIntentRef, NCIntentSample, NCIntentTerm, NCModelFileAdapter, NCResult, NCToken}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.{Assertions, Test}

import scala.jdk.CollectionConverters.ListHasAsScala

object NCContextWordSpecModel3Data {
    final val MAPPER = new ObjectMapper()

    MAPPER.registerModule(DefaultScalaModule)
}

case class NCContextWordSpecModel3Data(
    action: String,
    place: String,
    placeType: Option[String] = None,
    placeFloor: Option[String] = None
)

import org.apache.nlpcraft.model.ctxword.NCContextWordSpecModel3Data._

/**
  * Test model.
  */
class NCLightSwitchScalaModel2 extends NCModelFileAdapter("org/apache/nlpcraft/model/ctxword/lightswitch_model2.yaml") {
    @NCIntentRef("ls")
    @NCIntentSample(Array(
        "Turn the lights off in the room.",
        "Set the lights on in in the room.",
        "Lights up in the kitchen.",
        "Please, put the light out in the upstairs bedroom.",
        "Turn the lights off in the guest bedroom.",
        "No lights in the first floor guest washroom, please.",
        "Light up the garage, please!",
        "Kill the illumination now second floor kid closet!"
    ))
    def onMatch(@NCIntentTerm("act") actTok: NCToken, @NCIntentTerm("loc") locTok: NCToken): NCResult = {
        def getPart(id: String): Option[String] = locTok.getPartTokens.asScala.find(_.getId == id) match {
            case Some(t) => Some(t.getOriginalText.toLowerCase)
            case None => None
        }

        NCResult.json(
            MAPPER.writeValueAsString(
                NCContextWordSpecModel3Data(
                    action = if (actTok.getId == "ls:on") "on" else "off",
                    place = getPart("ls:part:place").get,
                    placeType = getPart("ls:part:placeType"),
                    placeFloor = getPart("ls:part:placeFloor")
                )
            )
        )
    }
}

/**
  * Verifies samples set.
  */
class NCLightSwitchScalaModel2SpecSamples {
    @Test
    private[ctxword] def testSamplesStandard(): Unit = {
        System.setProperty("NLPCRAFT_TEST_MODELS", classOf[NCLightSwitchScalaModel2].getName)

        Assertions.assertTrue(NCTestAutoModelValidator.isValid(),"See error logs above.")
    }
}

/**
  *  Extra values set.
  */
@NCTestEnvironment(model = classOf[NCLightSwitchScalaModel2], startClient = true)
class NCLightSwitchScalaModel2Spec extends NCTestContext {
    import org.apache.nlpcraft.model.ctxword.{NCContextWordSpecModel3Data => R}

    private def test0(txt: String, expected: NCContextWordSpecModel3Data): Unit = {
        val res = getClient.ask(txt)

        assertTrue(res.isOk, s"Checked: $txt")
        assertTrue(res.getResult.isPresent, s"Checked: $txt")

        val actual = MAPPER.readValue(res.getResult.get(), classOf[NCContextWordSpecModel3Data])

        assertEquals(expected, actual, s"Expected: $expected, actual: $actual")
    }

    @Test
    def testSamplesDetailed(): Unit = {
        test0(
            "Turn the lights off in the room.",
            R(action = "off", place = "room")
        )
        test0(
            "Set the lights on in in the room.",
            R(action = "on", place = "room")
        )
        test0(
            "Lights up in the kitchen.",
            R(action = "on", place = "kitchen")
        )
        test0(
            "Please, put the light out in the upstairs bedroom.",
            R(action = "off", place = "bedroom", placeFloor = Some("upstairs"))
        )
        test0(
            "Turn the lights off in the guest bedroom.",
            R(action = "off", place = "bedroom", placeType = Some("guest"))
        )
        test0(
            "No lights in the first floor guest washroom, please.",
            R(action = "off", place = "washroom", placeType = Some("guest"), placeFloor = Some("first floor"))
        )
        test0(
            "Light up the garage, please!",
            R(action = "on", place = "garage")
        )
        test0(
            "Kill the illumination now second floor kid closet!",
            R(action = "off", place = "closet",  placeType = Some("kid"), placeFloor = Some("second floor"))
        )
    }

    @Test
    def testSynonymsSameCategory(): Unit = {
        // Word `loft` is not defined as place.
        test0(
            "Turn the lights off in the loft.",
            R(action = "off", place = "loft")
        )
        // Word `loft` is not defined as place.
        test0(
            "Set the lights on in in the loft.",
            R(action = "on", place = "loft")
        )
        // Word `office` is not defined as place.
        test0(
            "Lights up in the office.",
            R(action = "on", place = "office")
        )
        // Word `library` is not defined as place.
        test0(
            "Please, put the light out in the upstairs library.",
            R(action = "off", place = "library", placeFloor = Some("upstairs"))
        )
        // Word `office` is not defined as place.
        test0(
            "Turn the lights off in the guest office.",
            R(action = "off", place = "office", placeType = Some("guest"))
        )
        // Word `chamber` is not defined as place.
        test0(
            "No lights in the first floor guest chamber, please.",
            R(action = "off", place = "chamber", placeType = Some("guest"), placeFloor = Some("first floor"))
        )
        // Word `office` is not defined as place.
        test0(
            "Light up the office, please!",
            R(action = "on", place = "office")
        )
        // Word `chamber` is not defined as place.
        test0(
            "Kill the illumination now second floor kid chamber!",
            R(action = "off", place = "chamber",  placeType = Some("kid"), placeFloor = Some("second floor"))
        )
    }
}