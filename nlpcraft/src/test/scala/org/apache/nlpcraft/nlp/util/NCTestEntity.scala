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
import org.apache.nlpcraft.nlp.util.NCTestPipeline.*

/**
  *
  */
object NCTestEntity:
    def apply(id: String, token: NCTestToken) = new NCTestEntity(id, tokens = Seq(token))
    def apply(id: String, reqId: String, token: NCTestToken) = new NCTestEntity(id, reqId, tokens = Seq(token))

/**
  * Entity test implementation.
  *
  * @param id
  * @param reqId
  * @param groups
  * @param meta
  * @param tokens
  */
case class NCTestEntity(
    id: String,
    reqId: String = null,
    groups: Set[String] = null,
    meta: Map[String, AnyRef] = null,
    tokens: Seq[NCTestToken]
) extends NCPropertyMapAdapter with NCEntity:
    if meta != null then meta.foreach { (k, v) => put(k, v) }

    override def getTokens: List[NCToken] = tokens.toList
    override def getRequestId: String = reqId
    override def getGroups: Set[String] = if groups != null then groups else Set(id)
    override def getId: String = id