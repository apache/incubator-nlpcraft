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

package org.apache.nlpcraft.model.intent.utils.ver2

import java.util.regex.{Pattern, PatternSyntaxException}

/**
 * DSL intent.
 */
case class NCDslIntent(
    orig: String,
    id: String,
    ordered: Boolean,
    meta: Map[String, Any],
    flow: Option[String],
    terms: Array[NCDslTerm]
) {
    require(id != null)
    require(terms.nonEmpty)
    require(meta != null)

    // Flow regex as a compiled pattern.
    val flowRegex = flow match {
        case Some(r) ⇒
            try
                Some(Pattern.compile(r))
            catch {
                case e: PatternSyntaxException ⇒
                    throw new IllegalArgumentException(s"${e.getDescription} in flow regex '${e.getPattern}' near index ${e.getIndex}.")
            }

        case None ⇒ None
    }

    override def toString: String =
        s"intent=$id"
}
