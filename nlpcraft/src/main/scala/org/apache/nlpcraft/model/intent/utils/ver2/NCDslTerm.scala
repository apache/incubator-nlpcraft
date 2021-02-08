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

import org.apache.nlpcraft.model.NCToken

/**
 * DSL term.
 */
case class NCDslTerm(
    id: String,
    pred: NCToken ⇒ Boolean,
    min: Int,
    max: Int,
    conv: Boolean
) {
    if (pred == null)
        throw new IllegalArgumentException("Intent DSL term must be defined.")
    if (min < 0 || min > max)
        throw new IllegalArgumentException(s"Invalid intent DSL term min quantifiers: $min (must be min >= 0 && min <= max).")
    if (max < 1)
        throw new IllegalArgumentException(s"Invalid intent DSL term max quantifiers: $max (must be max >= 1).")
}
