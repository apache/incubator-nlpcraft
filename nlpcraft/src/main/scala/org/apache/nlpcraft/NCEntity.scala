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

package org.apache.nlpcraft

/**
  * An entity is a collection if one or more [[NCToken tokens]]. An entity typically has a
  * consistent semantic meaning and usually denotes a real-world object, such as persons, locations, number,
  * date and time, organizations, products, etc. - where such objects can be abstract or have a physical existence.
  * Entities are produced by [[NCEntityParser]]. See [[NCPipeline]] for documentation on the entities in the
  * overall processing pipeline.
  *
  * Note that both [[NCToken]] and [[NCEntity]] interfaces extend [[NCPropertyMap]] trait
  * that allows them to store custom metadata properties. Parser, enrichers and validators for tokens
  * and entities use this capability to store and check their properties in tokens and entities.
  *
  * @see [[NCEntity]]
  * @see [[NCToken]]
  * @see [[NCTokenParser]]
  * @see [[NCTokenEnricher]]
  * @see [[NCTokenValidator]]
  * @see [[NCEntityParser]]
  * @see [[NCEntityEnricher]]
  * @see [[NCEntityValidator]]
  * @see [[NCEntityMapper]]
  */
trait NCEntity extends NCPropertyMap:

    /**
      * Gets the list of tokens this entity is comprised of. Returned list is never empty or `null`.
      *
      * @return List of tokens that are part of this entity.
      */
    def getTokens: List[NCToken]

    /**
      * Joins all tokens' text with trimming using space as a delimiter. This function does not cache the
      * result and performs text construction on each call. Make sure to cache the result to avoid
      * unnecessary parasitic workload if and when method [[getTokens]] does not change.
      *
      * @return Constructed text value representation of this entity. Note that returned value is not
      *     cached and created anew every time this method is called.
      */
    def mkText: String = getTokens.map(_.getText.trim).mkString(" ").trim

    /**
      * Gets ID of the request this entity is part of.
      *
      * @return ID of the request this entity is part of.
      */
    def getRequestId: String

    /**
      * Gets optional set of groups this entity belongs to.
      *
      * @return Optional set of groups this entity belongs to. Returned set can be empty but never `null`. By
      *     default the entity belongs to a group of its own [[getType type]].
      */
    def getGroups: Set[String] = Set(getType)

    /**
      * Gets entity type.
      *
      * @return Entity type.
      */
    def getType: String
