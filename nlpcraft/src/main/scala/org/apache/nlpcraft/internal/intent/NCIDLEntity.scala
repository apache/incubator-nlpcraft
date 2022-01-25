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

package org.apache.nlpcraft.internal.intent

import org.apache.nlpcraft.*
import scala.jdk.CollectionConverters.*
import java.util
//import java.util.{Collections, List, Set}

/**
  *
  * @param ent
  * @param idx
  */
class NCIDLEntity(ent: NCEntity, idx: Int) extends NCPropertyMapAdapter with NCEntity:
    private lazy val txt = ent.getTokens.asScala.map(_.getText).mkString(" ")

    override def getTokens: util.List[NCToken] = ent.getTokens
    override def getRequestId: String = ent.getRequestId
    override def getGroups: util.Set[String] = ent.getGroups
    override def getId: String = ent.getId

    def getText: String = txt
    def getIndex: Int = idx
