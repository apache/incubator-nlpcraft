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

/**
  * IDL term.
  *
  * @param idl IDL expression for this term.
  * @param id Optional ID of this term.
  * @param decls Term optional declarations.
  * @param pred Term predicate.
  * @param min Min quantifier.
  * @param max Max quantifier.
  * @param conv Whether or not this term support conversation context.
  * @param fragMeta Fragment metadata, if any.
  */
case class NCIDLTerm(
    idl: String,
    id: Option[String],
    decls: Map[String, NCIDLFunction],
    pred: NCIDLFunction,
    min: Int,
    max: Int,
    conv: Boolean,
    fragMeta: Map[String, Any] = Map.empty
):
    require(pred != null)
    require(min >= 0 && max >= min)

    /**
      *
      * @param meta
      */
    def cloneWithFragMeta(meta: Map[String, Any]): NCIDLTerm =
        NCIDLTerm(
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