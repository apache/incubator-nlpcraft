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

package org.apache.nlpcraft.model.impl.json;

import java.util.*;

/**
 * Parsing bean.
 */
public class NCElementJson {
    private String id;
    private String[] groups;
    private String parentId;
    private String desc;
    private String[] synonyms = new String[0];
    private Map<String, Object> metadata = new HashMap<>();
    private NCValueJson[] values = new NCValueJson[0];
    private String valueLoader;

    /**
     *
     * @return
     */
    public String getParentId() {
        return parentId;
    }

    /**
     *
     * @param parentId
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     *
     * @return
     */
    public NCValueJson[] getValues() {
        return values;
    }

    /**
     *
     * @param values
     */
    public void setValues(NCValueJson[] values) {
        this.values = values;
    }

    /**
     *
     * @return
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     *
     * @param metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String[] getGroups() {
        return groups;
    }

    /**
     *
     * @param groups
     */
    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return desc;
    }

    /**
     *
     * @param desc
     */
    public void setDescription(String desc) {
        this.desc = desc;
    }

    /**
     *
     * @return
     */
    public String[] getSynonyms() {
        return synonyms;
    }

    /**
     *
     * @param synonyms
     */
    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    /**
     *
     * @return
     */
    public String getValueLoader() {
        return valueLoader;
    }

    /**
     *
     * @param valueLoader
     */
    public void setValueLoader(String valueLoader) {
        this.valueLoader = valueLoader;
    }
}
