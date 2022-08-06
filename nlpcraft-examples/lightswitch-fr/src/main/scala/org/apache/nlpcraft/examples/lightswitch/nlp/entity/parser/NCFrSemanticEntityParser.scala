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

package org.apache.nlpcraft.examples.lightswitch.nlp.entity.parser

import opennlp.tools.stemmer.snowball.SnowballStemmer
import org.apache.nlpcraft.examples.lightswitch.nlp.token.parser.NCFrTokenParser
import org.apache.nlpcraft.nlp.entity.parser.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*

/**
  *
  * @param src
  */
class NCFrSemanticEntityParser(src: String) extends NCSemanticEntityParser(
    new NCSemanticStemmer:
        private val stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.FRENCH)
        override def stem(txt: String): String = stemmer.synchronized { stemmer.stem(txt.toLowerCase).toString }
    ,
    new NCFrTokenParser(),
    mdlSrcOpt = Option(src)
)
