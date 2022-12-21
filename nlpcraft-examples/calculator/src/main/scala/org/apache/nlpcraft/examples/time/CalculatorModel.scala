/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nlpcraft.examples.time

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.nlpcraft.*
import org.apache.nlpcraft.annotations.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.*
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser
import java.util.Properties
import scala.annotation.*

/**
 *
 */
object CalculatorModel:
    private val OPS: Map[String, (Int, Int) => Int] = Map("+" -> (_ + _), "-" -> (_ - _), "*" -> (_ * _), "/" -> (_ / _))
    private val PIPELINE: NCPipeline =
        val props = new Properties()
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")

        val stanford = new StanfordCoreNLP(props)

        new NCPipelineBuilder().
            withTokenParser(new NCStanfordNLPTokenParser(stanford)).
            withEntityParser(new NCNLPEntityParser(t => OPS.contains(t.getText))). // For operations.
            withEntityParser(new NCStanfordNLPEntityParser(stanford, Set("number"))). // For numerics.
            build

    private def nne(e: NCEntity): Int = java.lang.Double.parseDouble(e[String]("stanford:number:nne")).intValue

import CalculatorModel.*

/**
 * 
 */
class CalculatorModel extends NCModel(NCModelConfig("nlpcraft.calculator.ex", "Calculator Example Model", "1.0"), PIPELINE) :
    private var mem: Option[Int] = None

    private def calc(x: Int, op: String, y: Int): NCResult =
        mem = Option(OPS.getOrElse(op, throw new IllegalStateException()).apply(x, y))
        NCResult(mem.get)

    /*
     * We use '@unused' marker annotation on the intent callbacks since these
     * callback are called by the system through reflection only and thus trigger
     * IDEA warning about being unused in the code.
     */

    @NCIntent(
        "intent=calc options={ 'ordered': true }" +
        "   term(x)={# == 'stanford:number'}" +
        "   term(op)={has(list('+', '-', '*', '/'), meta_ent('nlp:entity:text')) == true}" +
        "   term(y)={# == 'stanford:number'}"
    )
    @unused def onMatch(
        @unused ctx: NCContext,
        @unused im: NCIntentMatch,
        @NCIntentTerm("x") x: NCEntity,
        @NCIntentTerm("op") op: NCEntity,
        @NCIntentTerm("y") y: NCEntity
    ): NCResult = calc(nne(x), op.mkText, nne(y))

    @NCIntent(
        "intent=calcMem options={ 'ordered': true }" +
        "   term(op)={has(list('+', '-', '*', '/'), meta_ent('nlp:entity:text')) == true}" +
        "   term(y)={# == 'stanford:number'}"
    )
    @unused def onMatchMem(
        @unused ctx: NCContext,
        @unused im: NCIntentMatch,
        @NCIntentTerm("op") op: NCEntity,
        @NCIntentTerm("y") y: NCEntity
    ): NCResult = calc(mem.getOrElse(throw new NCRejection("Memory is empty.")), op.mkText, nne(y))
