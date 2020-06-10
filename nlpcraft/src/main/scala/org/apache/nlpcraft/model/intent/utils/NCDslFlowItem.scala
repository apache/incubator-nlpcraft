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

/**
 * Flow item from intent DSL.
 */
case class NCDslFlowItem(intents: Seq[String], min: Int, max: Int) {
    if (intents == null || intents.isEmpty)
        throw new IllegalArgumentException(s"Intent DSL flow item must have at least one intent ID.")
    if (min < 0 || max < 1 || min > max)
        throw new IllegalArgumentException(s"Invalid intent DSL flow item quantifier: [$min,$max]")

    override def toString = {
        val minMax = if (min == 0 && max == 1) "?"
            else if (min == 1 && max == Integer.MAX_VALUE) "+"
            else if (min == 0 && max == Integer.MAX_VALUE) "*"
            else if (min == 1 && max == 1) ""
            else s"[$min,$max]"

        val ids = intents.size match {
            case 1 ⇒ intents.head
            case _ ⇒ intents.mkString("(", "|", ")")
        }
        
        ids + minMax
    }
}
