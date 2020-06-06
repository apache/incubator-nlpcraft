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
 * Intent from intent DSL.
 */
case class NCDslIntent(id: String, conv: Boolean, ordered: Boolean, flow: Array[NCDslFlowItem], terms: Array[NCDslTerm]) {
    if (id == null)
        throw new IllegalArgumentException("Intent ID must be provided.")
    if (terms.length == 0)
        throw new IllegalArgumentException("Intent should have at least one term.")
    
    override def toString: String =
        s"Intent: '$id'"
    
    /**
     * Gets full intent string representation in text DSL format.
     *
     * @return Full intent string representation in text DSL format.
     */
    def toDslString: String =
        s"intent=$id conv=$conv ordered=$ordered flow='${flow.mkString(" >> ")}' ${terms.mkString(" ")}"
}
