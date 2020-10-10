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

package org.apache.nlpcraft.probe.mgrs

import org.apache.nlpcraft.model.intent.impl.NCIntentSolver
import org.apache.nlpcraft.model.intent.utils.NCDslIntent
import org.apache.nlpcraft.model.{NCElement, NCModel}

import scala.collection.{Map, Seq}

/**
  *
  * @param model
  * @param solver
  * @param intents
  * @param synonyms
  * @param synonymsDsl
  * @param addStopWordsStems
  * @param exclStopWordsStems
  * @param suspWordsStems
  * @param elements
  */
case class NCProbeModel(
    model: NCModel,
    solver: NCIntentSolver,
    intents: Seq[NCDslIntent],
    synonyms: Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , Seq[NCProbeSynonym]]], // Fast access map.
    synonymsDsl: Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , Seq[NCProbeSynonym]]], // Fast access map.
    addStopWordsStems: Set[String],
    exclStopWordsStems: Set[String],
    suspWordsStems: Set[String],
    elements: Map[String /*Element ID*/ , NCElement],
    samples: Map[String, Seq[Seq[String]]]
)
