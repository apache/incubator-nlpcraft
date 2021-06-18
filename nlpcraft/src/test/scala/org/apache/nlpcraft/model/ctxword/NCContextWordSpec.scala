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
import org.apache.nlpcraft.{NCTestContext, NCTestElement, NCTestEnvironment}
import org.junit.jupiter.api.Assertions.{assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import java.util
import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava}

/**
  * Test model.
  */
class NCContextWordSpecModel extends NCModel {
    case class Value(name: String, syns: String*) extends NCValue {
        override def getName: String = name
        override def getSynonyms: util.List[String] = (Seq(name) ++ syns).asJava
    }

    case class Elem(id: String, values: NCValue*) extends NCElement {
        override def getId: String = id
        override def getValues: util.List[NCValue] = values.asJava
        override def isContextWordSupport: Boolean = true
    }

    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    override def getElements: util.Set[NCElement] =
        Set(
            Elem("class:carBrand", Value("BMW")),
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
            "What is the local temperature",
            "This is the first day of heavy rain"
        )
    )
    @NCIntent(
        "intent=classification " +
        "term(carBrands)~{tok_id() == 'class:carBrand'}* " +
        "term(animals)~{tok_id() == 'class:animal'}* " +
        "term(weathers)~{tok_id() == 'class:weather'}* "
    )
    def onMatch(
        @NCIntentTerm("carBrands") carBrands: List[NCToken],
        @NCIntentTerm("animals") animals: List[NCToken],
        @NCIntentTerm("weathers") weathers: List[NCToken]
    ): NCResult = {
        println("carBrands=" + carBrands)
        println("animals=" + animals)
        println("weathers=" + weathers)

        NCResult.text("ok")
    }
}

/**
  * @see NCConversationSpecModel
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel], startClient = true)
class NCContextWordSpec extends NCTestContext {
    @Test
    @throws[Exception]
    private[ctxword] def test(): Unit = {
        val cli = getClient

        //cli.ask("I want to have a dog and fox")
        cli.ask("I like to drive my Porsche")
    }
}
