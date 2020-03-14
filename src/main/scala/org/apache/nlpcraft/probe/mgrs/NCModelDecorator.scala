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

import org.apache.nlpcraft.model.{NCElement, NCModel}

/**
  *
  * @param model Decorated model.
  * @param synonyms Fast-access synonyms map for first phase.
  * @param synonymsDsl Fast-access synonyms map for second phase.
  * @param additionalStopWordsStems Stemmatized additional stopwords.
  * @param excludedStopWordsStems Stemmatized excluded stopwords.
  * @param suspiciousWordsStems Stemmatized suspicious stopwords.
  * @param elements Map of model elements.
  */
case class NCModelDecorator(
    model: NCModel,
    synonyms: Map[String/*Element ID*/, Map[Int/*Synonym length*/, Seq[NCSynonym]]], // Fast access map.
    synonymsDsl: Map[String/*Element ID*/, Map[Int/*Synonym length*/, Seq[NCSynonym]]], // Fast access map.
    additionalStopWordsStems: Set[String],
    excludedStopWordsStems: Set[String],
    suspiciousWordsStems: Set[String],
    elements: Map[String/*Element ID*/, NCElement]
) extends java.io.Serializable {
    override def toString: String = {
        s"Probe model decorator [" +
            s"id=${model.getId}, " +
            s"name=${model.getName}, " +
            s"version=${model.getVersion}" +
        s"]"
    }
}
