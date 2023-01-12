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
  * This trait defines a named [[NCEntity entity]] that is used by  [[NCSemanticEntityParser]].
  *
  * THe main purpose of this trait is to provide a set of synonyms by which this named entity can be matched
  * in the input text. Each synonym consists of one or more individual words. Synonym matching is performed on the
  * normalized and stemmatized forms of both a synonym and a user input on first phase and if the first attempt was not
  * successful, it tries to match stemmatized forms of synonyms with lemmatized and the stemmatized forms of user input.
  * This approach provides more accurate matching and doesn't force users to provide synonyms in their initial words form.
  *
  * Note that element's type is its implicit synonym so that even if no additional synonyms are defined at least one synonym
  * always exists.
  *
  * **1st Phase**: on the 1st phase [[NCSemanticEntityParser]] will use stemmatized forms of both the synonym and the user input.
  * For example, aa single synonyms **argue** will match all following words **argued**, **argues** and **arguing** by utilizing
  * the same stem **argu**.* Note that you can control stemmatization level by choosing preferable algorithm,
  * look at the following article [[https://www.baeldung.com/cs/porter-vs-lancaster-stemming-algorithms Differences Between Porter and Lancaster Stemming Algorithms]].
  * Also note that stemmatization approach effectiveness varies depending on the chosen languages.
  *
  * **2ng Phase**: at the second phase, if the 1st phase didn't produce a match, [[NCSemanticEntityParser]] will try to use lemmatized and
  * then stemmatized version of the user input against stemmatized form of the synonym. For example, if an element is defined via
  * synonym **go**, all following user input texts will be matched: **go**, **gone**, **goes**, **went**. Note that it is enough to
  * define just initial word's form for the synonym.
  *
  * Beside described above synonyms, semantic element can also have an optional set of special synonyms called values or
  * "proper nouns" for this element. Unlike basic synonyms, each value is a pair of a name and a set of standard synonyms
  * by which that value, and ultimately its element, can be recognized in the user input. Note that the value name itself
  * acts as an implicit synonym even when no additional synonyms added for that value.
  *
  * Example 1.
  * <pre>
  * - id: "ord:menu"
  *   description: "Order menu."
  *   synonyms:
  *     - "{menu|carte|card}"
  *     - "{products|goods|food|item|_} list"
  * </pre>
  * This YAML representation describes semantic entity **ord:menu** that can be detected via synonyms: *menu*, *products*, *products list* etc.
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
  * This YAML definition describes semantic entity **ord&#58;pizza&#58;size** that can be detected via values synonyms: *small*, *medium size*,
  * *big piece* etc. Note that **value** (*small*, *medium* or *large* in this example) is passed in created [[NCEntity]] as
  * a property with a key *element-type:value* (`ord:pizza:size:value` in this example).
  *
  * **NOTE**: these examples show how semantic elements can be defined via YAML format
  * when these elements passed in [[NCSemanticEntityParser]] via resource definition,
  * but there aren't any differences when semantic elements defined via JSON/YAML files or prepared programmatically.
  *
  * See detailed description on the website [[https://nlpcraft.apache.org/built-in-entity-parser.html#parser-semantic Semantic Parser]].
  *
  * @see [[NCSemanticEntityParser]]
  */
trait NCSemanticElement:
    /**
      * Gets type of this element which will become type of the entity if this element is detected.
      *
      * @return Element type.
      * @see [[NCEntity.getType]]
      */
    def getType: String

    /**
      * Gets groups this element is member of. By default, this element belongs to at least
      * one group with the name of its [[getType type]].
      *
      * @return Groups.
      * @see [[NCEntity.getGroups]]
      */
    def getGroups: Set[String] = Set(getType)

    /**
      * Gets values map. Each element can have zero or more values,
      * each value is described as name and list of its synonyms.
      * Note that macros can be used for synonyms definition, i.e. returned synonyms can contain references to macros.
      *
      * @return Values.
      */
    def getValues: Map[String, Set[String]] = Map.empty

    /**
      * Gets element's synonyms.
      * Note that macros can be used for synonyms definition, i.e. returned synonyms can contain references to macros.
      *
      * @return Synonyms.
      */
    def getSynonyms: Set[String] = Set.empty

    /**
      * Gets [[NCPropertyMap metadata]] that will be passed to [[NCEntity]] instance if this element is detected.
      *
      * @return Metadata.
      */
    def getProperties: Map[String, AnyRef] = Map.empty
