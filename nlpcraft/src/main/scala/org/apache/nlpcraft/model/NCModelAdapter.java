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

package org.apache.nlpcraft.model;

/**
 * Adapter for data models. In most cases new data models should extend either this adapter or
 * {@link NCModelFileAdapter}.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCModelFileAdapter
 */
abstract public class NCModelAdapter implements NCModel {
    private final String id;
    private final String name;
    private final String ver;
    
    /**
     * Creates new model with the given mandatory parameters.
     *
     * @param id Model {@link NCModel#getId() ID}.
     * @param name Model {@link NCModel#getName() name}.
     * @param ver Model {@link NCModel#getVersion() version}.
     */
    public NCModelAdapter(String id, String name, String ver) {
        assert id != null;
        assert name != null;
        assert ver != null;

        this.id = id;
        this.name = name;
        this.ver = ver;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return ver;
    }
}
