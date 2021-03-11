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

package org.apache.nlpcraft.model.intent.compiler

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
 * Global DSL compiler state.
 */
object NCDslCompilerGlobal {
    private final val fragCache = TrieMap.empty[String /* Model ID. */ , mutable.Map[String, NCDslFragment]]

    /**
     *
     */
    def clearCache(): Unit = fragCache.clear()

    /**
     *
     * @param mdlId
     */
    def clearCache(mdlId: String): Unit = fragCache += mdlId → mutable.HashMap.empty[String, NCDslFragment]

    /**
     *
     * @param mdlId
     * @param frag
     */
    def addFragment(mdlId: String, frag: NCDslFragment): Unit =
        fragCache.getOrElse(mdlId, {
            val m = mutable.HashMap.empty[String, NCDslFragment]

            fragCache += mdlId → m

            m
        }) += (frag.id → frag)

    /**
     *
     * @param mdlId
     * @param fragId
     * @return
     */
    def getFragment(mdlId: String, fragId: String): Option[NCDslFragment] =
        fragCache.get(mdlId).flatMap(_.get(fragId))
}
