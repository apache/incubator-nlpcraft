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

package org.apache.nlpcraft;

import org.apache.nlpcraft.internal.util.*;

import java.io.Serializable;
import java.util.Collection;

/**
 *
 */
public class NCResult implements Serializable {
    /** Data Model result text. */
    private Object body;

    /** Data Model result type. */
    private NCResultType type;

    /** ID of the intent. */
    private String intentId;

    /**
     * Creates new result with given body and type.
     *
     * @param body Result body.
     * @param type Result type.
     */
    public NCResult(Object body, NCResultType type) {
        assert body != null;
        assert type != null;

        this.body = body;
        this.type = type;
    }

    /**
     * No-arg constructor.
     */
    public NCResult() {
        // No-op.
    }

    /**
     * Sets result body.
     *
     * @param body Result body.
     */
    public void setBody(Object body) {
        this.body = body;
    }

    /**
     * Set result type.
     *
     * @param type Result type.
     */
    public void setType(NCResultType type) {
        this.type = type;
    }

    /**
     * Gets result type.
     *
     * @return Result type.
     */
    public NCResultType getType() {
        return type;
    }

    /**
     * Gets result body.
     *
     * @return Result body.
     */
    public Object getBody() {
        return body;
    }

    /**
     * Get optional intent ID.
     *
     * @return Intent ID or {@code null} if intent ID is not available.
     */
    public String getIntentId() {
        return intentId;
    }

    /**
     * Sets optional intent ID.
     *
     * @param intentId Intent ID to set for this result.
     */
    public void setIntentId(String intentId) {
        this.intentId = intentId;
    }
}
