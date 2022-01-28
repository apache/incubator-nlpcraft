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

package org.apache.nlpcraft.internal.intent.compiler

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
  * Global IDL compiler state.
  */
object NCIDLGlobal:
    private final val fragCache = TrieMap.empty[String /* Model ID. */ , mutable.Map[String, NCIDLFragment]]
    private final val importCache = mutable.HashSet.empty[String]

    /**
      *
      */
    def clearAllCaches(): Unit =
        fragCache.clear()
        clearImportCache()

    /**
      *
      */
    private def clearImportCache(): Unit = importCache.synchronized { importCache.clear() }

    /**
      *
      * @param mdlId
      */
    def clearCache(mdlId: String): Unit = fragCache += mdlId -> mutable.HashMap.empty[String, NCIDLFragment]

    /**
      *
      * @param imp
      */
    def addImport(imp: String): Unit = importCache.synchronized { importCache += imp }

    /**
      *
      * @param imp
      * @return
      */
    def hasImport(imp: String): Boolean = importCache.synchronized { importCache.contains(imp) }

    /**
      *
      * @param mdlId
      * @param frag
      */
    def addFragment(mdlId: String, frag: NCIDLFragment): Unit =
        fragCache.getOrElse(mdlId, {
            val m = mutable.HashMap.empty[String, NCIDLFragment]

            fragCache += mdlId -> m

            m
        }) += (frag.id -> frag)

    /**
      *
      * @param mdlId
      * @param fragId
      * @return
      */
    def getFragment(mdlId: String, fragId: String): Option[NCIDLFragment] =
        fragCache.get(mdlId).flatMap(_.get(fragId))

