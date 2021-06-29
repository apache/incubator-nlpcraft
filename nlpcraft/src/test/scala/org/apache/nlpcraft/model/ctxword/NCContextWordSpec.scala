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

import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.model.{NCElement, NCIntent, NCIntentMatch, NCIntentSample, NCIntentTerm, NCModel, NCResult, NCToken, NCValue}
import org.apache.nlpcraft.{NCTestContext, NCTestEnvironment}
import org.junit.jupiter.api.{AfterEach, BeforeEach, Test}

import java.text.DecimalFormat
import java.util.{Collections, Optional}
import java.{lang, util}
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.{ListHasAsScala, SeqHasAsJava, SetHasAsJava}

object NCContextWordSpecModel {
    val tables: ArrayBuffer[(String, NCAsciiTable)] = ArrayBuffer.empty[(String, NCAsciiTable)]
}

import org.apache.nlpcraft.model.ctxword.NCContextWordSpecModel._

/**
  * Test model.
  */
class NCContextWordSpecModel extends NCModel {
    override def getId: String = this.getClass.getSimpleName
    override def getName: String = this.getClass.getSimpleName
    override def getVersion: String = "1.0.0"

    private final val FMT = new DecimalFormat("#0.00000")

    val level = 0.4

    case class Value(name: String, syns: String*) extends NCValue {
        override def getName: String = name
        override def getSynonyms: util.List[String] = (Seq(name) ++ syns).asJava
    }

    case class Element(id: String, level: Double, values: NCValue*) extends NCElement {
        override def getId: String = id
        override def getValues: util.List[NCValue] = values.asJava
        override def getContextWordStrictLevel: Optional[lang.Double] = Optional.of(level)
        override def getGroups: util.List[String] = Collections.singletonList("testGroup")
    }

    object Element {
        def apply(id: String, values: NCValue*): Element = new Element(id, level, values: _*)
    }

    override def getElements: util.Set[NCElement] =
        Set(
            Element("class:cars", Value("BMW")),
            Element("class:animal", Value("fox"), Value("cat", "tomcat")),
            Element("class:weather", Value("temperature"), Value("rain"), Value("sun"))
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

            "What is the local temperature?",
            "This is the first day of heavy rain",
            "It is the beautiful day, the sun is shining",
        )
    )
    @NCIntent("intent=classification term(toks)~{has(tok_groups(), 'testGroup')}*")
    def onMatch(ctx: NCIntentMatch, @NCIntentTerm("toks") toks: List[NCToken]): NCResult = {
        val table = NCAsciiTable()

        table #= ("Token text", "Element ID", "Scores")

        for (t <- toks)
            table += (
                t.getOriginalText,
                t.getId,
                t.getMetadata.
                    get(s"${t.getId}:scores").
                    asInstanceOf[java.util.List[Double]].
                    asScala.
                    sortBy(-_).
                    map(FMT.format).
                    mkString(", ")
            )

        tables += ctx.getContext.getRequest.getNormalizedText -> table

        val elemIds = toks.map(_.getId).distinct.mkString(" ")
        val words = toks.map(_.getOriginalText).mkString(" ")

        NCResult.text(s"$elemIds $words")
    }
}

/**
  * @see NCConversationSpecModel
  */
@NCTestEnvironment(model = classOf[NCContextWordSpecModel], startClient = true)
class NCContextWordSpec extends NCTestContext {
    private def check(txt: String, elemId: String, words: String*): Unit = {
        val res = getClient.ask(txt).getResult.get()
        val exp = s"$elemId ${words.mkString(" ")}"

        require(exp == res, s"Expected: $exp, result: $res")
    }

    @BeforeEach
    private[ctxword] def before(): Unit = tables.clear()

    @AfterEach
    private[ctxword] def after(): Unit = {
        println("MATCHED:")

        for ((txt, table) <- tables) {
            println(s"Text: $txt")
            table.render()
        }

        tables.clear()
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
