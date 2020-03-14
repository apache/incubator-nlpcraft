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

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model.NCToken

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * DSL token qualifier.
 */
class NCDslTokenQualifier(parts: java.util.List[String]) extends java.util.function.Function[NCToken, NCToken] {
    private final val func: NCToken ⇒ NCToken =
        if (parts.isEmpty)
            t ⇒ t // Identity function.
        else
            t ⇒ {
                var tok = t
                
                for (part ← parts.asScala) {
                    val partToks = tok.getPartTokens.asScala
                    
                    val qualToks: mutable.Buffer[(NCToken, Set[String]/*ID or predicate alias*/)] =
                        (
                            partToks.zip(partToks.map(tok ⇒ Set(tok.getId))) ++
                            partToks.zip(partToks.map(tok ⇒ tok.meta(TOK_META_ALIASES_KEY).asInstanceOf[java.util.Set[String]] match {
                                case null ⇒ Set.empty[String]
                                case x ⇒ x.asScala.toSet
                            }))
                        )
                        .filter(_._2.contains(part))
                    
                    if (qualToks.isEmpty)
                        throw new IllegalArgumentException(s"Unknown part token qualifier: $part for token: ${t.getId}")
                    if (qualToks.size > 1)
                        throw new IllegalArgumentException(s"Multiple part tokens found for: $part for token: ${t.getId}")
                    
                    tok = qualToks.head._1
                }
                
                tok
            }
        
    override def apply(tok: NCToken): NCToken = func(tok)
}
