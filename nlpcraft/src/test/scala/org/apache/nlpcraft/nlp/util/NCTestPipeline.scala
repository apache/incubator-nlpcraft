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

/**
  *
  * @param tokParser
  */
case class NCTestPipeline(tokParser: NCTokenParser) extends NCPropertyMapAdapter with NCPipeline:
    require(tokParser != null)

    import scala.collection.mutable.ArrayBuffer as Buf

    val tokEnrichers: Buf[NCTokenEnricher] = Buf.empty
    val entEnrichers: Buf[NCEntityEnricher] = Buf.empty
    val entParsers: Buf[NCEntityParser] = Buf.empty
    val tokVals: Buf[NCTokenValidator] = Buf.empty
    val entVals: Buf[NCEntityValidator] = Buf.empty
    val entMappers: Buf[NCEntityMapper] = Buf.empty
    var varFilter: Option[NCVariantFilter] = None

    override def getTokenParser: NCTokenParser = tokParser
    override def getTokenEnrichers: List[NCTokenEnricher] = tokEnrichers.toList
    override def getEntityEnrichers: List[NCEntityEnricher] = entEnrichers.toList
    override def getEntityParsers: List[NCEntityParser] = entParsers.toList
    override def getTokenValidators: List[NCTokenValidator] = tokVals.toList
    override def getEntityValidators: List[NCEntityValidator] = entVals.toList
    override def getEntityMappers: List[NCEntityMapper] = entMappers.toList
    override def getVariantFilter: Option[NCVariantFilter] = varFilter