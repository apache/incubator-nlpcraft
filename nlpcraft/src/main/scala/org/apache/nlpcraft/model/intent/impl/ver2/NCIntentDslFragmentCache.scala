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

package org.apache.nlpcraft.model.intent.impl.ver2

import org.apache.nlpcraft.model.intent.utils.ver2.NCDslFragment

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
  * Global intent DSL fragment cache.
  */
object NCIntentDslFragmentCache {
    private final val cache = TrieMap.empty[String /* Model ID. */, mutable.Map[String, NCDslFragment]]
    
    /**
      *
      * @param mdlId
      * @param frag
      */
    def add(mdlId: String, frag: NCDslFragment): Unit =
        cache.getOrElse(mdlId, {
            val m = mutable.HashMap.empty[String, NCDslFragment]
            
            cache += mdlId → m
            
            m
        }) += (frag.id → frag)
    
    /**
      *
      * @param mdlId
      * @param fragId
      * @return
      */
    def get(mdlId: String, fragId: String): Option[NCDslFragment] =
        cache.get(mdlId).flatMap(_.get(fragId))
}
