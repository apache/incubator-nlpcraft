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

package org.apache.nlpcraft.model.intent

import org.apache.nlpcraft.common.ScalaMeta
import org.apache.nlpcraft.model.{NCRequest, NCToken}

import scala.collection.mutable

/**
 *
 * @param toks User input tokens.
 * @param intentMeta Intent metadata.
 * @param convMeta Conversation metadata.
 * @param fragMeta Fragment (argument) metadata passed during intent fragment reference.
 * @param req Server request holder.
 * @param vars Intent variable storage.
 */
case class NCIdlContext(
    toks: Seq[NCToken] = Seq.empty,
    intentMeta: ScalaMeta = Map.empty,
    convMeta: ScalaMeta = Map.empty,
    fragMeta: ScalaMeta = Map.empty,
    req: NCRequest,
    vars: mutable.Map[String, NCIdlFunction] = mutable.HashMap.empty
)

