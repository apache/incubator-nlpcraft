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

package org.apache.nlpcraft.examples.pizzeria.components

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.stemmer.*
import org.apache.nlpcraft.nlp.enrichers.NCEnStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.parsers.NCSemanticEntityParser
import org.apache.nlpcraft.nlp.stanford.*

import java.util.Properties

/**
  * PizzeriaModel pipeline, based on Stanford NLP engine, including model custom components.
  */
private [pizzeria] object PizzeriaModelPipeline:
    /** Prepared pipeline instance.*/
    val PIPELINE: NCPipeline =
        val stanford =
            val props = new Properties()
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
            new StanfordCoreNLP(props)
        val tokParser = new NCStanfordNLPTokenParser(stanford)

        import org.apache.nlpcraft.examples.pizzeria.components.PizzeriaOrderMapperDesc as D

        new NCPipelineBuilder().
            withTokenParser(tokParser).
            withTokenEnricher(new NCEnStopWordsTokenEnricher()).
            withEntityParser(new NCStanfordNLPEntityParser(stanford, Set("number"))).
            withEntityParser(new NCSemanticEntityParser(new NCEnStemmer, tokParser, "pizzeria_model.yaml")).
            withEntityMapper(PizzeriaOrderMapper(
                extra = D("ord:pizza:size", "ord:pizza:size:value"),
                descr = D("ord:pizza", "ord:pizza:size"))
            ).
            withEntityMapper(PizzeriaOrderMapper(
                extra = D("stanford:number", "stanford:number:nne"),
                descr = D("ord:pizza", "ord:pizza:qty"), D("ord:drink", "ord:drink:qty"))
            ).
            withEntityValidator(new PizzeriaOrderValidator()).
            build