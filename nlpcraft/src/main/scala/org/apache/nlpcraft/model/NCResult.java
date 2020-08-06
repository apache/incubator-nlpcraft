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

import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.common.util.NCUtils;

import java.io.Serializable;
import java.util.Collection;

/**
 * Data model result returned from model intent callbacks. Result consists of the
 * text body and the type. The type is similar in notion to MIME types. The following is the list of supported
 * result types:
 * <table summary="" class="dl-table">
 *     <tr>
 *         <th>Result Type</th>
 *         <th>Factory Method</th>
 *     </tr>
 *     <tr>
 *         <td><code>text</code></td>
 *         <td>{@link #text(String)}</td>
 *     </tr>
 *     <tr>
 *         <td><code>html</code></td>
 *         <td>{@link #html(String)}</td>
 *     </tr>
 *     <tr>
 *         <td><code>json</code></td>
 *         <td>{@link #json(String)}</td>
 *     </tr>
 *     <tr>
 *         <td><code>yaml</code></td>
 *         <td>{@link #yaml(String)}</td>
 *     </tr>
 * </table>
 * Note that all of these types have specific meaning <b>only</b> for REST applications that interpret them
 * accordingly. For example, the REST client interfacing between NLPCraft and Amazon Alexa or Apple HomeKit can only
 * accept {@code text} result type and ignore everything else.
 */
public class NCResult implements Serializable {
    private String body;
    private String type;
    private Collection<NCToken> tokens;
    private String intentId;
    
    /**
     * Creates new result with given body and type.
     *
     * @param body Result body.
     * @param type Result type.
     * @throws IllegalArgumentException Thrown if type is invalid.
     */
    public NCResult(String body, String type) {
        assert body != null;
        assert type != null;
        
        this.body = body;
        this.type = checkType(type);
    }
    
    /**
     * No-arg constructor.
     */
    public NCResult() {
        // No-op.
    }
    
    /**
     * Creates {@code text} result.
     *
     * @param txt Textual result. Text interpretation will be defined by the client receiving this result.
     * @return Newly created query result.
     */
    public static NCResult text(String txt) {
        return new NCResult(txt, "text");
    }
    
    /**
     * Creates {@code html} result.
     *
     * @param html HTML markup.
     * @return Newly created query result.
     */
    public static NCResult html(String html) {
        return new NCResult(html, "html");
    }
    
    /**
     * Creates {@code json} result. Note that this method will test given JSON string
     * for validness by using <code>com.google.gson.Gson</code> JSON utility. If JSON string is invalid
     * the {@link IllegalArgumentException} exception will be thrown.
     *
     * @param json Any JSON string to be rendered on the client.
     * @return Newly created query result.
     * @throws IllegalArgumentException Thrown if given JSON string is invalid.
     */
    public static NCResult json(String json) {
        // Validation.
        try {
            NCUtils.js2Obj(json);
        }
        catch (NCException e) {
            throw new IllegalArgumentException(String.format("Invalid JSON value: %s.", json), e.getCause());
        }
        
        return new NCResult(json, "json");
    }
    
    /**
     * Creates {@code yaml} result.
     *
     * @param yaml Any YAML string to be rendered on the client.
     * @return Newly created query result.
     */
    public static NCResult yaml(String yaml) {
        return new NCResult(yaml, "yaml");
    }
    
    /**
     *
     * @param type Type to check.
     * @throws IllegalArgumentException Thrown if type is invalid.
     */
    private String checkType(String type) {
        String typeLc = type.toLowerCase();
        
        if (!typeLc.equals("html") &&
            !typeLc.equals("json") &&
            !typeLc.equals("yaml") &&
            !typeLc.equals("text"))
            throw new IllegalArgumentException("Invalid result type: " + type);
        else
            return typeLc;
    }
    
    /**
     * Sets result body.
     *
     * @param body Result body.
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Set result type.
     *
     * @param type Result type.
     * @throws IllegalArgumentException Thrown if type is invalid.
     */
    public void setType(String type) {
        this.type = checkType(type);
    }

    /**
     * Gets tokens that were used to produce this query result. Note that the
     * returned tokens can come from the current request as well as from the conversation (i.e. from
     * previous requests). Order of tokens is not important.
     *
     * @return Gets tokens that were used to produce this query result.
     * @see #setTokens(Collection) 
     */
    public Collection<NCToken> getTokens() {
        return tokens;
    }

    /**
     * Sets a collection of tokens that was used to produce this query result. Note that the
     * returned tokens can come from the current request as well as from the conversation (i.e. from
     * previous requests). Order of tokens is not important.
     * <p>
     * Providing these tokens is necessary for proper STM operation. If conversational support isn't used
     * setting these tokens is not required. Note that built-in intent based matched automatically sets
     * these tokens. 
     *
     * @param tokens Collection of tokens that was used to produce this query result.
     * @see #getTokens()
     */
    public void setTokens(Collection<NCToken> tokens) {
        this.tokens = tokens;
    }
    
    /**
     * Gets result type.
     *
     * @return Result type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets result body.
     *
     * @return Result body.
     */
    public String getBody() {
        return body;
    }

    /**
     * Get optional intent ID. 
     *
     * @return Intent ID or {@code null} if intent ID was not provided.
     */
    public String getIntentId() {
        return intentId;
    }

    /**
     * Sets optional intent ID.
     *
     * @param intentId
     */
    public void setIntentId(String intentId) {
        this.intentId = intentId;
    }
}
