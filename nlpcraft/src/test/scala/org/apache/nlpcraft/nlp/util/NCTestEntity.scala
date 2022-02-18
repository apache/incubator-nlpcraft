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

import java.util.{List as JList, Set as JSet}
import scala.jdk.CollectionConverters.*

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
    tokens: NCTestToken*
) extends NCPropertyMapAdapter with NCEntity:
    if meta != null then meta.foreach { (k, v) => put(k, v) }

    override def getTokens: JList[NCToken] = tokens.asJava
    override def getRequestId: String = reqId
    override def getGroups: JSet[String] = (if groups != null then groups else Set(id)).asJava
    override def getId: String = id