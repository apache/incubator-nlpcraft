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

import org.apache.nlpcraft.model.{NCContext, NCElement, NCIntent, NCIntentSample, NCModel, NCResult, NCValue}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util.{Collections, Optional}
import java.{lang, util}
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.{CollectionHasAsScala, SeqHasAsJava, SetHasAsJava}

object NCContextWordSpecModel {
    case class Value(name: String, syns: String*) extends NCValue {
        override def getName: String = name
        override def getSynonyms: util.List[String] = (Seq(name) ++ syns).asJava
    }

    case class Element(id: String, level: Double, values: NCValue*) extends NCElement {
        override def getId: String = id
        override def getValues: util.List[NCValue] = values.asJava
        override def getGroups: util.List[String] = Collections.singletonList("testGroup")
        override def getCategoryConfidence: Optional[lang.Double] = Optional.of(level)
    }

    var expected: String = _
}

import org.apache.nlpcraft.model.ctxword.NCContextWordSpecModel._

class NCContextWordSpecModel extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    // Empirical detected confidence for given model and requests.
    val MDL_LEVEL: java.lang.Double = 0.68

    @NCIntentSample(
        Array(
            "I like drive my new BMW",
            "BMW has the best engine",
            "Luxury cars like Mercedes and BMW  are prime targets",
            "BMW will install side air bags up front",
            "I want to change BMW engine",
            "I want to try BMW driver dynamics",
            "BMW has excellent driver protection",
            "BMW pricing are going up",
            "BMW drivers have the highest loyalty",

            "A wild cat is very dangerous",
            "A fox eat hens",
            "The fox was already in your chicken house",

            "What is the local temperature?",
            "This is the first day of heavy rain",
            "It is the beautiful day, the sun is shining"
        )
    )
    @NCIntent("intent=i term(t)={false}")
    def x(): NCResult = NCResult.text("OK")

    override def getElements: util.Set[NCElement] =
        Set(
            Element("class:cars", MDL_LEVEL, Value("BMW")),
            Element("class:animal", MDL_LEVEL, Value("fox"), Value("cat", "tomcat")),
            Element("class:weather", MDL_LEVEL, Value("temperature"), Value("rain"), Value("sun"))
        ).map(p => {
            val e: NCElement = p

            e
        }).asJava

    override def onContext(ctx: NCContext): NCResult = {
        val varRes = ArrayBuffer.empty[String]

        require(ctx.getVariants.size() == 1)

        val v = ctx.getVariants.asScala.head

        val testGroupToks = v.asScala.toSeq.filter(_.getGroups.contains("testGroup"))

        val elemIds = testGroupToks.map(_.getId).distinct.mkString(" ")
        val words = testGroupToks.map(_.getOriginalText).mkString(" ")

        val res =
            if (NCContextWordSpecModel.expected == s"$elemIds $words")
                "OK"
            else
                s"ERROR: variant '${NCContextWordSpecModel.expected}' not found. Found: ${varRes.mkString(", ")}"

        NCResult.text(res)
    }

    override def getEnabledBuiltInTokens: util.Set[String] = Collections.emptySet()
}

/**
  * @see NCConversationSpecModel
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel], startClient = true)
class NCContextWordSpec extends NCTestContext {
    private def checkSingleVariant(txt: String, elemId: String, words: String*): Unit = {
        NCContextWordSpecModel.expected = s"$elemId ${words.mkString(" ")}"

        val res = getClient.ask(txt).getResult.get()

        require(res == "OK", s"Unexpected: $res")
    }

    @Test
    private[ctxword] def test(): Unit = {
        checkSingleVariant("I want to have dogs and foxes", "class:animal", "dogs", "foxes")
        checkSingleVariant("I bought dog's meat", "class:animal", "dog")
        checkSingleVariant("I bought meat dog's", "class:animal", "dog")

        checkSingleVariant("I want to have a dog and fox", "class:animal", "dog", "fox")
        checkSingleVariant("I fed your fish", "class:animal", "fish")

        checkSingleVariant("I like to drive my Porsche and Volkswagen", "class:cars", "Porsche", "Volkswagen")
        checkSingleVariant("Peugeot added motorcycles to its range year ago", "class:cars", "Peugeot")

        checkSingleVariant("The frost is possible today", "class:weather", "frost")
        checkSingleVariant("There's a very strong wind from the east now", "class:weather", "wind")
    }
}
