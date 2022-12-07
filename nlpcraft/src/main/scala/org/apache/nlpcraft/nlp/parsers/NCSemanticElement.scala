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

/*
   _________            ______________
   __  ____/_______________  __ \__  /_____ _____  __
   _  /    _  __ \_  ___/_  /_/ /_  /_  __ `/_  / / /
   / /___  / /_/ /(__  )_  ____/_  / / /_/ /_  /_/ /
   \____/  \____//____/ /_/     /_/  \__,_/ _\__, /
                                            /____/

          2D ASCII GAME ENGINE FOR SCALA3
            (C) 2021 Rowan Games, Inc.
               ALl rights reserved.
*/

// TODO: link on site?
/**
  *
  * Configuration which helps to detect [[org.apache.nlpcraft.NCEntity NCEntity]] for
  * <code>Semantic</code> implementation of [[org.apache.nlpcraft.NCEntityParser NCEntityParser]].
  *
  *  See detailed description [[https://nlpcraft.apache.org/built-in-entity-parser.html#parser-semantic Semantic Parser]].
  *
  * @see [[NCSemanticEntityParser]]
  * @see [[NCSemanticStemmer]]
  */
trait NCSemanticElement:
    /**
      * Gets <code>id<code> for created [[org.apache.nlpcraft.NCEntity NCEntity]] instance.
      * Representation of [[org.apache.nlpcraft.NCEntity.getId NCEntity.getId()]] method.
      *
      * @return Element ID.
      */
    def getId: String

    /**
      * Gets <code>groups<code> for created [[org.apache.nlpcraft.NCEntity NCEntity]] instance.
      * Representation of [[org.apache.nlpcraft.NCEntity.getGroups NCEntity.getGroups()]] method.
      *
      *  @return Groups.
      */
    def getGroups: Set[String] = Set(getId)

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
      * Gets optional <code>properties<code> map for created [[org.apache.nlpcraft.NCEntity NCEntity]] instance.
      * Representation of [[org.apache.nlpcraft.NCEntity NCEntity]] content.
      *
      * @return Groups.
      */
    def getProperties: Map[String, AnyRef] = Map.empty
