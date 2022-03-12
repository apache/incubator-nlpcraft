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

package org.apache.nlpcraft;

import java.util.Optional;
import java.util.Set;

/**
 *
 */
public interface NCPropertyMap {
    /**
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T get(String key);

    /**
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> Optional<T> getOpt(String key);

    /**
     *
     * @param key
     * @param obj
     */
    <T> T put(String key, Object obj);

    /**
     *
     * @param key
     * @param obj
     * @param <T>
     * @return
     */
    <T> T putIfAbsent(String key, T obj);

    /**
     *
     * @param key
     * @return
     */
    boolean contains(String key);

    /**
     *
     * @param key
     * @return
     */
    <T> T remove(String key);

    /**
     *
     * @param key
     * @param obj
     * @return
     */
    boolean remove(String key, Object obj);

    /**
     *
     * @return
     */
    Set<String> keysSet();

    /**
     *
     */
    void clear();
}
