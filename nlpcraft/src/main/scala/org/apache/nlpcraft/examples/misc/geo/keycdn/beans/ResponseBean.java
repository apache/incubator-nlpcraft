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

package org.apache.nlpcraft.examples.misc.geo.keycdn.beans;

/**
 * Service https://tools.keycdn.com/geo response part bean.
 */
public class ResponseBean {
    private String status;
    private String description;
    private ResponseDataBean data;

    /**
     * Gets response status.
     *
     * @return Response status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets response status.
     *
     * @param status Response status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets response data.
     *
     * @return Response data.
     */
    public ResponseDataBean getData() {
        return data;
    }

    /**
     * Sets response data.
     *
     * @param data Response data to set.
     */
    public void setData(ResponseDataBean data) {
        this.data = data;
    }

    /**
     * Gets response description.
     *
     * @return Response description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets response description.
     *
     * @param description Response description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
