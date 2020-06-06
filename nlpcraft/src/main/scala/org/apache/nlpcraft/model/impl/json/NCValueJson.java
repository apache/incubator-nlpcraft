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

/**
 * Parsing bean.
 */
public class NCValueJson {
    private String name;
    private String[] synonyms = new String[0];
    private String valueLoader;

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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
