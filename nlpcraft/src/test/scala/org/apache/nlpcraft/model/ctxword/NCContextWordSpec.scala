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

import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentSample, NCIntentTerm, NCModel, NCResult, NCToken, NCValue}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.Test

import java.{lang, util}
import java.util.{Collections, Optional}
import scala.jdk.CollectionConverters.{ListHasAsScala, SeqHasAsJava, SetHasAsJava}

/**
  * Test model.
  */
class NCContextWordSpecModel extends NCModel {
    private final val LEVEL = 0.4

    case class Value(name: String, syns: String*) extends NCValue {
        override def getName: String = name
        override def getSynonyms: util.List[String] = (Seq(name) ++ syns).asJava
    }

    case class Elem(id: String, level: Double, values: NCValue*) extends NCElement {
        override def getId: String = id
        override def getValues: util.List[NCValue] = values.asJava
        override def getContextWordStrictLevel: Optional[lang.Double] = Optional.of(level)
        override def getGroups: util.List[String] = Collections.singletonList("testGroup")
    }

    object Elem {
        def apply(id: String, values: NCValue*): Elem = new Elem(id, LEVEL, values: _*)
    }

    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    override def getElements: util.Set[NCElement] =
        Set(
            Elem("class:cars", Value("BMW")),
            Elem("class:animal", Value("fox"), Value("cat", "tomcat")),
            Elem("class:weather", Value("temperature"), Value("rain"), Value("sun"))
        ).map(p => {
            val e: NCElement = p

            e
        }).asJava

    @NCIntentSample(
        Array(
            "I like drive my new BMW",
            "BMW has the best engine",
            "Luxury cars like Mercedes and BMW  are prime targets",
            "BMW will install side air bags up front",

            "A wild cat is very dangerous",
            "A fox eats hens",
            "The fox was already in your chicken house",

            "What is the local temperature ?",
            "This is the first day of heavy rain",
            "It is the beautiful day, the sun is shining ",
        )
    )
    @NCIntent("intent=classification term(cars)~{has(tok_groups(), 'testGroup')}*")
    def onMatch(@NCIntentTerm("classification") toks: List[NCToken]): NCResult = {
        val groups = toks.flatMap(_.getGroups.asScala).mkString(" ")
        val words = toks.map(_.getOriginalText).mkString(" ")

        NCResult.text(s"${groups.mkString(" ")} ${words.mkString(" ")}")
    }
}

/**
  * @see NCConversationSpecModel
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel], startClient = true)
class NCContextWordSpec extends NCTestContext {
    private def check(txt: String, group: String, words: String*): Unit =
        require(s"$group ${words.mkString(" ")}" == getClient.ask(txt).getText)

    @Test
    @throws[Exception]
    private[ctxword] def test(): Unit = {
        check("I want to have a dog and fox", "class:animal", "dog", "fox")
        check("I fed your fish", "class:animal", "fish")

        check("I like to drive my Porsche and Volkswagen", "class:cars", "Porsche", "Volkswagen")
        check("Peugeot added motorcycles to its range in 1901", "class:cars", "Peugeot", "motorcycles")

        check("The frost is possible today", "class:weather", "frost")
        check("There's a very strong wind from the east now", "class:weather", "wind")
    }
}