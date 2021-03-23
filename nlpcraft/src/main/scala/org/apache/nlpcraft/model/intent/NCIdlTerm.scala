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

import org.apache.nlpcraft.common._

/**
 * IDL term.
 *
 * @param idl
 * @param id Optional ID of this term.
 * @param decls Term optional declarations.
 * @param pred Term predicate.
 * @param min
 * @param max
 * @param conv
 * @param fragMeta
 */
case class NCIdlTerm(
    idl: String,
    id: Option[String],
    decls: List[NCIdlFunction[Object]],
    pred: NCIdlFunction[Boolean],
    min: Int,
    max: Int,
    conv: Boolean,
    fragMeta: Map[String, Any] = Map.empty
) {
    require(pred != null)
    require(min >= 0 && max >= min)

    /**
     *
     * @param meta
     * @return
     */
    def cloneWithFragMeta(meta: Map[String, Any]): NCIdlTerm =
        NCIdlTerm(
            idl,
            id,
            decls,
            pred,
            min,
            max,
            conv,
            meta
        )

    override def toString: String = idl

    def toAnsiString: String = g(idl)
}
