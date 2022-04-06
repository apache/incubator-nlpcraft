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

package org.apache.nlpcraft.examples.order.components

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.NCStanfordNLPEntityParser
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser
import scala.jdk.CollectionConverters.*
import java.util.Properties

/**
  *
  */
object StanfordPipeline:
    val PIPELINE: NCPipeline =
        val stanford =
            val props = new Properties()
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
            new StanfordCoreNLP(props)
        val tokParser = new NCStanfordNLPTokenParser(stanford)
        val stemmer = new NCSemanticStemmer():
            private val ps = new PorterStemmer
            override def stem(txt: String): String = ps.synchronized { ps.stem(txt) }

        new NCPipelineBuilder().
            withTokenParser(tokParser).
            withEntityParser(new NCStanfordNLPEntityParser(stanford, "number")).
            withEntityParser(new NCSemanticEntityParser(stemmer, tokParser, "order_model.yaml")).
            withEntityMappers(Seq(new PizzaSizeExtender, new PizzaQtyExtender, new DrinkQtyExtender).asJava).
            withEntityValidator(new OrderValidator).
            build()