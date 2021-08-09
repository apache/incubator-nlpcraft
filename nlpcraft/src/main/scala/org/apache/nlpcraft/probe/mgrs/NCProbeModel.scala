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

package org.apache.nlpcraft.probe.mgrs

import org.apache.nlpcraft.model.intent.NCIdlIntent
import org.apache.nlpcraft.model.intent.solver.NCIntentSolver
import org.apache.nlpcraft.model.{NCElement, NCModel}

case class NCProbeModelCallback(
    origin: String,
    className: String,
    methodName: String
)

/**
  *
  * @param model
  * @param solver
  * @param intents
  * @param callbacks
  * @param continuousSynonyms
  * @param sparseSynonyms
  * @param idlSynonyms
  * @param exclStopWordsStems
  * @param suspWordsStems
  * @param elements
  */
case class NCProbeModel(
    model: NCModel,
    solver: NCIntentSolver,
    intents: Seq[NCIdlIntent],
    callbacks: Map[String /* Intent ID */, NCProbeModelCallback],
    continuousSynonyms: Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , NCProbeSynonymsWrapper]], // Fast access map.
    sparseSynonyms: Map[String /*Element ID*/, Seq[NCProbeSynonym]],
    idlSynonyms: Map[String /*Element ID*/ , Seq[NCProbeSynonym]], // Fast access map.
    addStopWordsStems: Set[String],
    exclStopWordsStems: Set[String],
    suspWordsStems: Set[String],
    elements: Map[String /*Element ID*/ , NCElement],
    samples: Set[(String, Seq[Seq[String]])]
) {
    lazy val hasIdlSynonyms: Boolean = idlSynonyms.nonEmpty
    lazy val hasNoIdlSynonyms: Boolean = continuousSynonyms.nonEmpty || sparseSynonyms.nonEmpty
    lazy val hasSparseSynonyms: Boolean = sparseSynonyms.nonEmpty || idlSynonyms.exists(_._2.exists(_.sparse))
    lazy val hasContinuousSynonyms: Boolean = continuousSynonyms.nonEmpty || idlSynonyms.exists(_._2.exists(!_.sparse))

    def hasIdlSynonyms(elemId: String): Boolean = idlSynonyms.contains(elemId)
}
