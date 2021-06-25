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

import java.util.HashMap;
import java.util.Map;

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
    // Can be null.
    private Boolean isPermutateSynonyms;
    // Can be null.
    private Boolean isSparse;
    // Can be null.
    private Double contextWordStrictLevel;

    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    public NCValueJson[] getValues() {
        return values;
    }
    public void setValues(NCValueJson[] values) {
        this.values = values;
    }
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String[] getGroups() {
        return groups;
    }
    public void setGroups(String[] groups) {
        this.groups = groups;
    }
    public String getDescription() {
        return desc;
    }
    public void setDescription(String desc) {
        this.desc = desc;
    }
    public String[] getSynonyms() {
        return synonyms;
    }
    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }
    public String getValueLoader() {
        return valueLoader;
    }
    public void setValueLoader(String valueLoader) {
        this.valueLoader = valueLoader;
    }
    public Boolean isPermutateSynonyms() {
        return isPermutateSynonyms;
    }
    public void setPermutateSynonyms(Boolean permutateSynonyms) {
        isPermutateSynonyms = permutateSynonyms;
    }
    public Boolean isSparse() {
        return isSparse;
    }
    public void setSparse(Boolean sparse) {
        isSparse = sparse;
    }
    public Double getContextWordStrictLevel() {
        return contextWordStrictLevel;
    }
    public void setContextWordStrictLevel(Double contextWordStrictLevel) {
        this.contextWordStrictLevel = contextWordStrictLevel;
    }
}
