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

package org.apache.nlpcraft.nlp.parsers

import org.apache.nlpcraft.nlp.stemmer.NCStemmer
import org.apache.nlpcraft.*
/**
  * This trait defines configuration which helps to detect [[NCEntity]] of
  * **Semantic** entity parser [[NCSemanticEntityParser]].
  * It denotes a **named entity** [[NCEntity]] which is created by [[NCSemanticEntityParser]].
  *
  * Each trait contains a set of synonyms to match on named entity.
  * A synonym can have one or more individual words.
  * Note that element's type is its implicit synonym so that even if no additional synonyms are defined at least one synonym always exists.
  * Note also that synonym matching is performed on normalized and stemmatized forms of both a synonym and user input on first phase and if first attempt is not successful, it tries to match stemmatized forms of synonyms with stemmatized forms of user input which were lemmatized preliminarily.
  * This approach allows to provide more accurate matching and doesn't force users to prepare synonyms in initial words form.
  *
  * See detailed description on the website [[https://nlpcraft.apache.org/built-in-entity-parser.html#parser-semantic Semantic Parser]].
  *
  * @see [[NCSemanticEntityParser]]
  */
trait NCSemanticElement:
    /**
      * Gets **type** for created [[NCEntity]] instance.
      * Representation of [[NCEntity.getType]] method.
      *
      * @return Element type.
      */
    def getType: String

    /**
      * Gets **groups** for created [[NCEntity]] instance.
      * Representation of [[NCEntity.getGroups]] method.
      *
      * @return Groups.
      */
    def getGroups: Set[String] = Set(getType)

    /**
      * Gets values map. Each element can contain multiple value,
      * each value is described as name and list of its synonyms.
      * They allows to find element's value in text.
      *
      * @return Values.
      */
    def getValues: Map[String, Set[String]] = Map.empty

    /**
      * Gets elements synonyms list. They allows to find element in text.
      *
      * @return Synonyms.
      */
    def getSynonyms: Set[String] = Set.empty

    /**
      * Gets [[NCPropertyMap metadata]] property for created [[NCEntity]] instance.
      *
      * @return Groups.
      */
    def getProperties: Map[String, AnyRef] = Map.empty
