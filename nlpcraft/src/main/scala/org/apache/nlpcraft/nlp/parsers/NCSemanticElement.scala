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
  * This trait is used in [[NCSemanticEntityParser]] and defines configuration
  * which contains synonyms for [[NCEntity]] detection and properties which are used for new [[NCEntity]] instances creation.
  *
  * In other words it denotes a [[NCEntity named entity]] which can be created by [[NCSemanticEntityParser]].
  *
  * The trait contains a set of synonyms to match on named entity.
  * A synonym can have one or more individual words.
  * Note that element's type is its implicit synonym so that even if no additional synonyms are defined at least one synonym
  * always exists.
  * Note also that synonym matching is performed on normalized and stemmatized forms of both a synonym and user input on
  * first phase and if first attempt is not successful, it tries to match stemmatized forms of synonyms
  * with stemmatized forms of user input which were lemmatized preliminarily.
  * This approach allows to provide more accurate matching and doesn't force users to prepare synonyms in initial words form.
  *
  * Stemmetization.
  * Via one synonyms **argue** all following words *argued*, *argues* and *arguing* are matched
  * by the same stem **argu**.
  * Note that you can control stemmatization aggression level by choosing preferable algorithm,
  * look at the following article [[https://www.baeldung.com/cs/porter-vs-lancaster-stemming-algorithms Differences Between Porter and Lancaster Stemming Algorithms]].
  * Also note please that stemmatization approach can be less or more useful for different languages.
  *
  * Lemmatization.
  * If an element defined via synonym **go**, all following user input texts are matched:
  * *go*, *gone*, *goes*, *went*. So, it is enough to define just synonym initial word's forms.
  *
  * Beside described above synonyms, semantic element can also have an optional set of special synonyms called values or "proper nouns" for this element.
  * Unlike basic synonyms, each value is a pair of a name and a set of standard synonyms by which that value,
  * and ultimately its element, can be recognized in the user input.
  * Note that the value name itself acts as an implicit synonym even when no additional synonyms added for that value.
  *
  * So [[NCEntity named entity]] can be found via [[NCSemanticElement.getSynonyms element synonyms]] or
  * [[NCSemanticElement.getValues element values]].
  * Other [[NCSemanticElement]] properties are passed into created corresponded  [[NCEntity]] instance.
  *
  * Example 1.
  * <pre>
  * - id: "ord:menu"
  *   description: "Order menu."
  *   synonyms:
  *     - "{menu|carte|card}"
  *     - "{products|goods|food|item|_} list"
  * </pre>
  * Described above element **ord&#58;menu** can be detected via synonyms: *menu*, *products*, *products list* etc.
  *
  * Example 2.
  * <pre>
  * - id: "ord:pizza:size"
  *   description: "Size of pizza."
  *   values:
  *     "small": [ "{small|smallest|min|minimal|tiny} {size|piece|_}" ]
  *     "medium": [ "{medium|intermediate|normal|regular} {size|piece|_}" ]
  *     "large": [ "{big|biggest|large|max|maximum|huge|enormous} {size|piece|_}" ]
  * </pre>
  * Described above element **ord&#58;pizza&#58;size** can be detected via values synonyms: *small*, *medium size*, *big piece* etc.
  * Note that **value** (*small*, *medium* or *large* in this example) is passed in created [[NCEntity]] as property with key
  * **element-type&#58;value** (*ord&#58;pizza&#58;size&#58;value* in this example).
  *
  * **NOTE** that given examples show how semantic elements synonyms and values are represented via YAML format
  * when these elements passed in [[NCSemanticEntityParser]] via semantic model resource definition,
  * but there aren't differences when semantic elements are defined via JSON files or prepared programmatically.
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
      * Note that macros can be used for synonyms definition.
      *
      * @return Values.
      */
    def getValues: Map[String, Set[String]] = Map.empty

    /**
      * Gets elements synonyms list. They allows to find element in text.
      * Note that macros can be used for synonyms definition, so returned synonyms can contain references to macroses.
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
