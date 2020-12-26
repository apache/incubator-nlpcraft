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

package org.apache.nlpcraft.model.intent.utils

import java.util.regex.{Pattern, PatternSyntaxException}

/**
 * Intent from intent DSL.
 *
 * @param id Intent ID.
 * @param ordered Whether or not the order of terms is important for matching.
 * @param flow Optional flow matching regex.
 * @param terms Array of terms comprising this intent.
 */
case class NCDslIntent(id: String, ordered: Boolean, flow: Option[String], terms: Array[NCDslTerm]) {
    if (id == null)
        throw new IllegalArgumentException("Intent ID must be provided.")
    if (terms.length == 0)
        throw new IllegalArgumentException("Intent should have at least one term.")

    // Flow regex as a compiled pattern.
    val flowRegex = flow match {
        case Some(r) ⇒
            try
                Some(Pattern.compile(r))
            catch {
                case e: PatternSyntaxException ⇒ throw new IllegalArgumentException(s"Invalid flow regex: ${e.getLocalizedMessage}")
            }
        case None ⇒ None
    }

    override def toString: String =
        s"Intent: '$id'"

    /**
     * Gets full intent string representation in text DSL format.
     *
     * @return Full intent string representation in text DSL format.
     */
    def toDslString: String = {
        val orderedStr = if (!ordered) "" else " ordered=true"
        val flowStr = flow match {
            case Some(r) ⇒ s"flow='$r''"
            case None ⇒ ""
        }

        s"intent=$id$orderedStr$flowStr ${terms.mkString(" ")}"
    }
}
