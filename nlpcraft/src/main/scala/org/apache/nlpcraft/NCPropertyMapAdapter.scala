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

package org.apache.nlpcraft

import scala.jdk.CollectionConverters.*
import java.util.concurrent.ConcurrentHashMap

/**
  * Convenient adapter for {@link NCPropertyMap} interface. */
class NCPropertyMapAdapter extends NCPropertyMap:
    private val map = new ConcurrentHashMap[String, Any]

    // TODO: or error?

    override def get[T](key: String): T = getOpt(key).orNull.asInstanceOf[T]

    override def getOpt[T](key: String): Option[T] =
        map.get(key) match
            case null => None
            case x => Some(x.asInstanceOf[T])

    override def put[T](key: String, obj: Any): T = map.put(key, obj).asInstanceOf[T]

    override def putIfAbsent[T](key: String, obj: T): T = map.putIfAbsent(key, obj).asInstanceOf[T]

    override def contains(key: String): Boolean = map.containsKey(key)

    override def remove[T](key: String): T = map.remove(key).asInstanceOf[T]

    override def remove(key: String, obj: Any): Boolean = map.remove(key, obj)

    override def keysSet: Set[String] = map.keys().asScala.toSet

    override def clear(): Unit = map.clear()