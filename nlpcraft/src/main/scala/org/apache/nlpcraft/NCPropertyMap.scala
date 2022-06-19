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

/**
  * Map-like container that provides support for mutable runtime-only propertes or metadata.
  *
  * @see NCPropertyMapAdapter
  * @see NCToken
  * @see NCEntity */
trait NCPropertyMap:
    /**
      * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
      *
      * @param key The key whose associated value is to be returned.
      * @param <T> Type of the returned value.
      * @return The value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key. */
    def get[T](key: String): T

    /**
      * Returns the value to which the specified key is mapped as an optional. This method is equivalent to:
      * <pre class="brush: java">
      * return Optional.ofNullable((T)map.get(key));
      * </pre>
      *
      * @param key The key whose associated value is to be returned.
      * @param <T> Type of the returned value.
      * @return The value to which the specified key is mapped as an optional. */
    def getOpt[T](key: String): Option[T]

    /**
      * Associates the specified value with the specified key in this map. If the map previously contained a mapping
      * for the key, the old value is replaced by the specified value.
      *
      * @param key Key with which the specified value is to be associated.
      * @param obj Value to be associated with the specified key.
      * @param <T> Type of the value.
      * @return The previous value associated with key, or {@code null} if there was no mapping for key. */
    def put[T](key: String, obj: Any): T

    /**
      * If the specified key is not already associated with a value (or is mapped to {@code null}) associates it with
      * the given value and returns {@code null}, else returns the current value.
      *
      * @param key Key with which the specified value is to be associate
      * @param obj Value to be associated with the specified key
      * @param <T> Type of the value.
      * @return The previous value associated with the specified key, or {@code null} if there was no mapping for the key. */
    def putIfAbsent[T](key: String, obj: T): T

    /**
      * Returns {@code true} if this map contains a mapping for the specified key.
      *
      * @return {@code true} if this map contains a mapping for the specified key. */
    def contains(key: String): Boolean

    /**
      * Removes the mapping for a key from this map if it is present.
      *
      * @param key Key whose mapping is to be removed from the map.
      * @param <T> Type of the value.
      * @return The previous value associated with key, or {@code null} if there was no mapping for key. */
    def remove[T](key: String): T

    /**
      * Removes the entry for the specified key only if it is currently mapped to the specified value.
      *
      * @param key Key with which the specified value is associated value.
      * @param obj Value expected to be associated with the specified key.
      * @return {@code true} if the value was removed */
    def remove(key: String, obj: Any): Boolean

    /**
      * Returns a set view of the keys contained in this map.
      *
      * @return A set view of the keys contained in this map */
    def keysSet: Set[String]

    /**
      * Removes all of the mappings from this map. The map will be empty after this call returns. */
    def clear(): Unit
