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

package org.apache.nlpcraft.nlp.util

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.token.parser.NCOpenNLPTokenParser
import org.apache.nlpcraft.nlp.util.NCTestPipeline.*

import java.util.{Optional, ArrayList as JList}

/**
  *
  * @param tokParser
  */
case class NCTestPipeline(tokParser: NCTokenParser) extends NCPropertyMapAdapter with NCModelPipeline with Cloneable:
    require(tokParser != null)

    private var variantFilter: Optional[NCVariantFilter] = Optional.empty()

    override val getTokenParser: NCTokenParser = tokParser
    override val getTokenEnrichers = new JList[NCTokenEnricher]()
    override val getEntityEnrichers = new JList[NCEntityEnricher]()
    override val getEntityParsers = new JList[NCEntityParser]()
    override val getTokenValidators = new JList[NCTokenValidator]()
    override val getEntityValidators = new JList[NCEntityValidator]()
    override def getVariantFilter: Optional[NCVariantFilter] = variantFilter

    override def clone(): NCTestPipeline =
        val copy = NCTestPipeline(this.tokParser)

        copy.getTokenEnrichers.addAll(this.getTokenEnrichers)
        copy.getEntityEnrichers.addAll(this.getEntityEnrichers)
        copy.getEntityParsers.addAll(this.getEntityParsers)
        copy.getTokenValidators.addAll(this.getTokenValidators)
        copy.getEntityValidators.addAll(this.getEntityValidators)
        copy.variantFilter = this.variantFilter

        copy