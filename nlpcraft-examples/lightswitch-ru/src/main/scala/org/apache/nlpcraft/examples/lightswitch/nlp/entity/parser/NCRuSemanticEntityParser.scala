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
import org.apache.nlpcraft.examples.lightswitch.nlp.token.parser.NCRuTokenParser
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.stemmer.NCStemmer

/**
  * Extension of [[NCSemanticEntityParser]] for RU language.
  *
  * @param mdlRes Relative path, absolute path, classpath resource or URL to YAML or JSON semantic model definition.
  */
class NCRuSemanticEntityParser(mdlRes: String) extends NCSemanticEntityParser(
    new NCStemmer:
        private val stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.RUSSIAN)
        override def stem(word: String): String = stemmer.synchronized { stemmer.stem(word.toLowerCase).toString }
    ,
    new NCRuTokenParser(),
    mdlRes
)
