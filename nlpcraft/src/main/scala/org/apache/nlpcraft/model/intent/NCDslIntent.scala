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

package org.apache.nlpcraft.model.intent

import org.apache.nlpcraft.common.ScalaMeta

import java.util.regex.Pattern

/**
 * DSL intent.
 *
 * @param origin File path, file name, method name or URL.
 * @param dsl Original DSL of this intent.
 * @param id
 * @param ordered
 * @param meta
 * @param flow
 * @param terms
 */
case class NCDslIntent(
    origin: String,
    dsl: String,
    id: String,
    ordered: Boolean,
    meta: ScalaMeta,
    flow: Option[String],
    flowClsName: Option[String],
    flowMtdName: Option[String],
    terms: List[NCDslTerm]
) {
    require(id != null)
    require(terms.nonEmpty)
    require(meta != null)

    // Flow regex as a compiled pattern.
    // Regex validity check is already done during intent compilation.
    lazy val flowRegex = flow match {
        case Some(r) ⇒ Some(Pattern.compile(r))
        case None ⇒ None
    }

    lazy val isFlowDefined = flow.isDefined || flowMtdName.isDefined

    override def toString: String = dsl
}
