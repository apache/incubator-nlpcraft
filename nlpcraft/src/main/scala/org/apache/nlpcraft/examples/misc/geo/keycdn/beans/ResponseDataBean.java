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
 * Service https://tools.keycdn.com/geo response bean.
 */
public class ResponseDataBean {
    private GeoDataBean geo;

    /**
     * Gets geo data holder.
     *
     * @return Geo data holder.
     */
    public GeoDataBean getGeo() {
        return geo;
    }

    /**
     * Sets get data holder.
     *
     * @param geo Geo data holder to set.
     */
    public void setGeo(GeoDataBean geo) {
        this.geo = geo;
    }
}
