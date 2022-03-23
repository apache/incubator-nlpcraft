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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Convenient adapter for {@link NCPropertyMap} interface.
 */
@SuppressWarnings("unchecked")
public class NCPropertyMapAdapter implements NCPropertyMap {
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    @Override
    public <T> T get(String key) {
        return (T)map.get(key);
    }

    @Override
    public <T> Optional<T> getOpt(String key) {
        return Optional.ofNullable((T)map.get(key));
    }

    @Override
    public <T> T put(String key, Object obj) {
        return (T)map.put(key, obj);
    }

    @Override
    public <T> T putIfAbsent(String key, T obj) {
        return (T)map.putIfAbsent(key, obj);
    }

    @Override
    public boolean contains(String key) {
        return map.containsKey(key);
    }

    @Override
    public <T> T remove(String key) {
        return (T)map.remove(key);
    }

    @Override
    public boolean remove(String key, Object obj) {
        return map.remove(key, obj);
    }

    @Override
    public Set<String> keysSet() {
        return map.keySet();
    }

    @Override
    public void clear() {
        map.clear();
    }
}
