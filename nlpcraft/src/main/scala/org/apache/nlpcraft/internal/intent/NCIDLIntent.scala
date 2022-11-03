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

import java.util.regex.Pattern

/**
  * IDL intent.
  *
  * @param origin File path, file name, method name or URL.
  * @param idl Original IDL of this intent.
  * @param id
  * @param options
  * @param meta
  * @param flow
  * @param terms
  */
case class NCIDLIntent(
    origin: String,
    idl: String,
    id: String,
    options: NCIDLIntentOptions,
    meta: Map[String, Object],
    flow: Option[String],
    terms: List[NCIDLTerm]
):
    require(id != null)
    require(terms.nonEmpty)
    require(meta != null)
    require(options != null)

    // Flow regex as a compiled pattern.
    // Regex validity check is already done during intent compilation.
    lazy val flowRegex: Option[Pattern] = flow match
        case Some(r) => Pattern.compile(r).?
        case None => None

    override def toString: String = idl
