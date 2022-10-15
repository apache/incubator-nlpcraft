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
package org.apache.nlpcraft.models

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.*
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.*
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser
import org.apache.nlpcraft.nlp.util.*
import org.scalatest.funsuite.AnyFunSuite

import java.util.Properties
import scala.util.Using

object NCIntCalcModelSpec:
    val MDL: NCModel = new NCTestModelAdapter:
        private var mem: Option[Int] = None

        override val getPipeline: NCPipeline =
            val stanford =
                val props = new Properties()
                props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
                new StanfordCoreNLP(props)

            new NCPipelineBuilder().
                withTokenParser(new NCStanfordNLPTokenParser(stanford)).
                // For operations.
                withEntityParser(new NCNLPEntityParser).
                // For numerics.
                withEntityParser(new NCStanfordNLPEntityParser(stanford, Set("number"))).
                build

        private def nne(e: NCEntity): Int = java.lang.Double.parseDouble(e[String]("stanford:number:nne")).intValue

        private def calc(x: Int, op: String, y: Int): NCResult =
            mem =
                Some(op match
                    case "+" => x + y
                    case "-" => x - y
                    case "*" => x * y
                    case "/" => x / y
                    case _ => throw new IllegalStateException()
                )

            NCResult(mem.get)

        @NCIntent(
            "intent=calc options={ 'ordered': false }" +
            "  term(x)={# == 'stanford:number'} " +
            "  term(op)={# == 'nlp:token' && has(list('+', '-', '*', '/'), meta_ent('nlp:token:text')) == true} " +
            "  term(y)={# == 'stanford:number'}"
        )
        def onMatch(
            ctx: NCContext,
            im: NCIntentMatch,
            @NCIntentTerm("x") x: NCEntity,
            @NCIntentTerm("op") op: NCEntity,
            @NCIntentTerm("y") y: NCEntity
        ): NCResult = calc(nne(x), op.mkText, nne(y))

        @NCIntent(
            "intent=calcMem options={ 'ordered': false }" +
            "  term(op)={# == 'nlp:token' && has(list('+', '-', '*', '/'), meta_ent('nlp:token:text')) == true} " +
            "  term(y)={# == 'stanford:number'}"
        )
        def onMatchMem(
            ctx: NCContext,
            im: NCIntentMatch,
            @NCIntentTerm("op") op: NCEntity,
            @NCIntentTerm("y") y: NCEntity
        ): NCResult =
            mem match
                case Some(x) => calc(x, op.mkText, nne(y))
                case None => throw new NCRejection("Memory is empty.")

class NCIntCalcModelSpec extends AnyFunSuite:
    test("test") {
        Using.resource(new NCModelClient(NCIntCalcModelSpec.MDL)) { client =>
            def check(txt: String, v: Int): Unit =
                require(v == client.ask(txt, "userId").getBody)

            check("2 + 2", 4)
            check("3 * 4", 12)
            check("/ two", 6)
            check("+ twenty two", 28)
            check("7 + 2", 9)
        }
    }

