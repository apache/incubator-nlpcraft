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

package org.apache.nlpcraft.server.ignite

import java.util.UUID

import javax.cache.Cache
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.query.{QueryCursor, SqlQuery}
import org.apache.ignite.events.CacheEvent
import org.apache.ignite.lang.{IgniteBiPredicate, IgnitePredicate}

import scala.collection.JavaConverters._

/*
 * Helpers for working with Ignite.
 */
object NCIgniteHelpers extends NCIgniteInstance {
    /**
     * Helper for work with ignite cache.
     *
     * @param ic Ignite cache.
     */
    implicit class NCCacheHelper[K, V](ic: IgniteCache[K, V]) {
        /**
          * Adds cache events remote listener.
          *
          * @param onEvent On event processor.
          * @param types Cache event types.
          * @return Listener ID.
          */
        def addListener(onEvent: CacheEvent ⇒ Unit, types: Int*): UUID = {
            val locNodeId = ignite.cluster().localNode().id()
            val cacheName = ic.getName

            ignite.events(ignite.cluster().forCacheNodes(cacheName)).remoteListen(
                new IgniteBiPredicate[UUID, CacheEvent]() {
                    override def apply(evtUuid: UUID, evt: CacheEvent): Boolean = {
                        if (evt.cacheName() == cacheName && evt.node().id() == locNodeId && evt.hasNewValue)
                            onEvent(evt)

                        true
                    }
                },
                new IgnitePredicate[CacheEvent]() {
                    override def apply(evt: CacheEvent): Boolean = evt.cacheName() == cacheName
                },
                types:_*
            )
        }

        /**
          * Removes previously added remote events listener.
          *
          * @param id Listener ID.
          */
        def removeListener(id: UUID): Unit = ignite.events().stopRemoteListen(id)

        /**
         * Gets an entry from the cache.
         *
         * @param key The key whose associated value is to be returned.
         * @return Return some value, or none if it does't exist.
         */
        def apply(key: K): Option[V] =
            ic.get(key) match {
                case null ⇒ None
                case value ⇒ Some(value)
            }

        /**
          * Put map to the cache.
          *
          * @param m Map.
          */
        def ++=(m: Map[K, V]): Unit = ic.putAll(m.asJava)

        /**
         * Put key-value pair to the cache.
         *
         * @param entry Key-value pair.
         */
        def +=(entry: (K, V)): Unit = ic.put(entry._1, entry._2)

        /**
         * Returns all values from cache.
         */
        def values: Iterable[V] = ic.asScala.map(_.getValue)

        /**
         * Returns all keys from cache.
         */
        def keys: Iterable[K] = ic.asScala.map(_.getKey)

        /**
         * Remove cache entry from cache by key.
         *
         * @param key Key to be removed from the cache.
         */
        def -=(key: K): Boolean = ic.remove(key)

        /**
          * Remove cache entry from cache by keys.
          *
          * @param keys Keys to be removed from the cache.
          */
        def --=(keys: Set[K]): Unit = ic.removeAll(keys.asJava)

        /**
          *
          * @param key Key to be gotten and removed from the cache.
          */
        def -==(key: K): Option[V] = ic.getAndRemove(key) match {
            case v if v != null ⇒ Some(v)
            case null ⇒ None
        }

        /**
         * SQL request to the cache.
         *
         * @param cls Type.
         * @param clause SQL request.
         * @param args Arguments for SQL request.
         * @return
         */
        def select(cls: Class[_ <: V], clause: String, args: Any*): QueryCursor[Cache.Entry[K, V]] = {
            assert(cls != null)
            assert(clause != null)
            assert(args != null)

            val qry = new SqlQuery[K, V](cls, clause)

            if (args != null && args.nonEmpty)
                qry.setArgs(args.map(_.asInstanceOf[AnyRef]): _*)

            ic.query(qry)
        }

        /**
         * SQL request to the cache.
         *
         * @param clause SQL request.
         * @param args Arguments for SQL request.
         **/
        def select(clause: String, args: Any*)(implicit m: Manifest[V]): QueryCursor[Cache.Entry[K, V]] = {
            assert(clause != null)
            assert(args != null)

            select(m.runtimeClass.asInstanceOf[Class[V]], clause, args: _*)
        }
    }
}
