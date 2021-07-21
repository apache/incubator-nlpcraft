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
    placeFloor: Option[String] = None,
    placeConfidence: java.lang.Double = 0
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
        def getPart(id: String): NCToken =
            locTok.getPartTokens.asScala.find(_.getId == id).
            getOrElse(throw new AssertionError(s"Token not found: $id"))
        def getPartTextOpt(id: String): Option[String] = locTok.getPartTokens.asScala.find(_.getId == id) match {
            case Some(t) => Some(t.getOriginalText.toLowerCase)
            case None => None
        }

        val place = getPart("ls:part:place")
        val conf: Double = place.meta("ls:part:place:confidence")

        NCResult.json(
            MAPPER.writeValueAsString(
                NCContextWordSpecModel3Data(
                    action = if (actTok.getId == "ls:on") "on" else "off",
                    place = place.getOriginalText.toLowerCase,
                    placeConfidence = conf,
                    placeType = getPartTextOpt("ls:part:placeType"),
                    placeFloor = getPartTextOpt("ls:part:placeFloor")
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

    private def check(testsData: (String, NCContextWordSpecModel3Data)*): Unit = {
        val errs = collection.mutable.HashMap.empty[String, String]
        val okMsgs = collection.mutable.ArrayBuffer.empty[String]

        testsData.foreach { case (txt, expected) =>
            def addError(msg: String): Unit = errs += txt -> msg

            val res = getClient.ask(txt)

            if (!res.isOk)
                addError(res.getResultError.get())
            else {
                val actual = MAPPER.readValue(res.getResult.get(), classOf[R])

                def getMainData(d: NCContextWordSpecModel3Data): String =
                    s"Main [action=${d.action}, place=${d.place}, placeType=${d.placeType}, placeFloor=${d.placeFloor}]"

                val actualData = getMainData(actual)
                val expData = getMainData(expected)

                if (expData != actualData)
                    addError(s"Expected: $expData, actual: $actualData")
                else
                    okMsgs += s"`$txt` processed ok with detected place `${actual.place}` and confidence `${actual.placeConfidence}`."
            }
        }

        println(s"Test passed: ${okMsgs.size}")
        println(s"Test errors: ${errs.size}")

        okMsgs.foreach(println)

        if (errs.nonEmpty)
            throw new AssertionError(errs.mkString("\n"))
    }

    /**
      * `ls:part:place` has 2 values: room and bedroom.
      * Samples contains also: kitchen, washroom, garage, closet. These words detected with some confidence < 1.
      */
    @Test
    def testSamplesDetailed(): Unit =
        check(
            "Turn the lights off in the room." ->
                R(action = "off", place = "room"),
            "Set the lights on in in the room." ->
                R(action = "on", place = "room"),
            "Lights up in the kitchen." ->
                R(action = "on", place = "kitchen"),
            "Please, put the light out in the upstairs bedroom." ->
                R(action = "off", place = "bedroom", placeFloor = Some("upstairs")),
            "Turn the lights off in the guest bedroom." ->
                R(action = "off", place = "bedroom", placeType = Some("guest")),
            "No lights in the first floor guest washroom, please." ->
                R(action = "off", place = "washroom", placeType = Some("guest"), placeFloor = Some("first floor")),
            "Light up the garage, please!" ->
                R(action = "on", place = "garage"),
            "Kill the illumination now second floor kid closet!" ->
                R(action = "off", place = "closet",  placeType = Some("kid"), placeFloor = Some("second floor"))
        )

    /**
      * `ls:part:place` has 2 values: room and bedroom.
      * Samples contains also: loft, hallway, library, chamber, office.
      * Note, that These words are not provided as in samples.
      * These words detected with some confidence < 1.
      */
    @Test
    def testSynonymsSameCategory(): Unit =
        check(
            "Turn the lights off in the loft." ->
                R(action = "off", place = "loft"),
            "Set the lights on in in the loft." ->
                R(action = "on", place = "loft"),
            "Lights up in the hallway." ->
                R(action = "on", place = "hallway"),
            "Please, put the light out in the upstairs library." ->
                R(action = "off", place = "library", placeFloor = Some("upstairs")),
            "Turn the lights off in the guest office." ->
                R(action = "off", place = "office", placeType = Some("guest")),
            "No lights in the first floor guest chamber, please." ->
                R(action = "off", place = "chamber", placeType = Some("guest"), placeFloor = Some("first floor")),
            "Light up the office, please!" ->
                R(action = "on", place = "office"),
            "Kill the illumination now second floor kid chamber!" ->
                R(action = "off", place = "chamber",  placeType = Some("kid"), placeFloor = Some("second floor"))
        )
}