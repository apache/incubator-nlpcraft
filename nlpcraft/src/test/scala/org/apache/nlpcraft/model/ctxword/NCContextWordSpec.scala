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

import org.apache.nlpcraft.model.NCContextWordElementConfig.NCContextWordElementPolicy
import org.apache.nlpcraft.model.NCContextWordElementConfig.NCContextWordElementPolicy._
import org.apache.nlpcraft.model.{NCContext, NCContextWordElementConfig, NCContextWordModelConfig, NCElement, NCModel, NCResult, NCValue}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.util
import java.util.{Collections, Optional}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsJava, SeqHasAsJava, SetHasAsJava}

object NCContextWordSpecModel {
    case class Value(name: String, syns: String*) extends NCValue {
        override def getName: String = name
        override def getSynonyms: util.List[String] = (Seq(name) ++ syns).asJava
    }

    case class Element(id: String, level: Double, values: NCValue*) extends NCElement {
        override def getId: String = id
        override def getValues: util.List[NCValue] = values.asJava
        override def getGroups: util.List[String] = Collections.singletonList("testGroup")
    }

    var expected: String = _
}

import org.apache.nlpcraft.model.ctxword.NCContextWordSpecModel._

class NCContextWordSpecModel extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    val MDL_LEVEL = 0.4
    val MDL_POLICY = MIN

    override def getContextWordModelConfig: Optional[NCContextWordModelConfig] = {
        Optional.of(
            new NCContextWordModelConfig() {
                override def getSupportedElements: util.Map[String, NCContextWordElementConfig] =
                    getElements.asScala.map(e =>
                        e.getId ->
                        new NCContextWordElementConfig() {
                            override def getPolicy: NCContextWordElementPolicy = MDL_POLICY
                            override def getScore: Double = MDL_LEVEL
                        }
                    ).toMap.asJava

                override def useIntentsSamples(): Boolean = false

                override def getSamples: util.List[String] =
                    Seq(
                        "I like drive my new BMW",
                        "BMW has the best engine",
                        "Luxury cars like Mercedes and BMW  are prime targets",
                        "BMW will install side air bags up front",

                        "A wild cat is very dangerous",
                        "A fox eats hens",
                        "The fox was already in your chicken house",

                        "What is the local temperature?",
                        "This is the first day of heavy rain",
                        "It is the beautiful day, the sun is shining"
                    ).asJava
            }
        )
    }

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
        val ok =
            ctx.getVariants.asScala.exists(v => {
                val testGroupToks = v.asScala.toSeq.filter(_.getGroups.contains("testGroup"))

                val elemIds = testGroupToks.map(_.getId).distinct.mkString(" ")
                val words = testGroupToks.map(_.getOriginalText).mkString(" ")

                NCContextWordSpecModel.expected == s"$elemIds $words"
            })

        NCResult.text(if (ok) "OK" else "ERROR")
    }
}

/**
  * @see NCConversationSpecModel
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel], startClient = true)
class NCContextWordSpec extends NCTestContext {
    private def check(txt: String, elemId: String, words: String*): Unit = {
        NCContextWordSpecModel.expected = s"$elemId ${words.mkString(" ")}"

        require(getClient.ask(txt).getResult.get() == "OK")
    }

    @Test
    private[ctxword] def test(): Unit = {
        check("I want to have dogs and foxes", "class:animal", "dogs", "foxes")
        check("I bought dog's meat", "class:animal", "dog")
        check("I bought meat dog's", "class:animal", "dog")

        check("I want to have a dog and fox", "class:animal", "dog", "fox")
        check("I fed your fish", "class:animal", "fish")

        check("I like to drive my Porsche and Volkswagen", "class:cars", "Porsche", "Volkswagen")
        check("Peugeot added motorcycles to its range in 1901", "class:cars", "Peugeot", "motorcycles")

        check("The frost is possible today", "class:weather", "frost")
        check("There's a very strong wind from the east now", "class:weather", "wind")
    }
}
