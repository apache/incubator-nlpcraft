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

package org.apache.nlpcraft.probe.embedded;

import org.apache.nlpcraft.model.*;
import java.util.function.*;

/**
 * Result container for the embedded probe. When an embedded probe completes query processing for one of its
 * deployed models it calls registered callbacks, if any, with this container before the results are sent back
 * to the REST server.
 *
 * @see NCEmbeddedProbe#registerCallback(Consumer)
 * @see NCEmbeddedProbe#unregisterCallback(Consumer)
 * @see NCResult
 * @see NCEmbeddedProbe
 */
public interface NCEmbeddedResult {
    /**
     * Gets the ID of the model that produced this result. Note that embedded probe can host more than one
     * data model hence this parameter is important to distinguish to which model this result belongs.
     *
     * @return ID of the model produced this result.
     */
    String getModelId();

    /**
     * Gets ID of the request that produced this result.
     *
     * @return ID of the request that produced this result.
     */
    String getServerRequestId();

    /**
     * Gets original text of the request that produced this result.
     *
     * @return Original text of the request that produced this result.
     */
    String getOriginalText();

    /**
     * Gets ID of the user that made the request this result is for.
     * 
     * @return ID of the user that made the request this result is for.
     */
    long getUserId();

    /**
     * Gets optional result body. Note that either both result body and type are set or
     * error message and error code are set, but not both pairs.
     *
     * @return Result body or {@code null} if error occurred.
     */
    String getBody();

    /**
     * Gets optional result type. Note that either both result body and type are set or
     * error message and error code are set, but not both pairs.
     *
     * @return Result type or {@code null} if error occurred.
     */
    String getType();

    /**
     * Gets optional error message.
     *
     * @return Error message or {@code null} if no errors occurred.
     */
    String getErrorMessage();

    /**
     * Gets optional error code. One of the following codes:
     * <table class="dl-table" summary="">
     *     <thead>
     *         <tr>
     *             <th>Code</th>
     *             <th>Description</th>
     *         </tr>
     *     </thead>
     * <tbody>
     * <tr>
     *     <td><code>1</code></td>
     *     <td>Rejected by the model.</td>
     * </tr>
     * <tr>
     *     <td><code>100</code></td>
     *     <td>Unexpected system error.</td>
     * </tr>
     * <tr>
     *     <td><code>101</code></td>
     *     <td>Model's result is too big.</td>
     * </tr>
     * <tr>
     *     <td><code>102</code></td>
     *     <td>Recoverable system error.</td>
     * </tr>
     * <tr>
     *     <td><code>10001</code></td>
     *     <td>Too many unknown words.</td>
     * </tr>
     * <tr>
     *     <td><code>10002</code></td>
     *     <td>Sentence is too complex (too many free words).</td>
     * </tr>
     * <tr>
     *     <td><code>10003</code></td>
     *     <td>Too many suspicious or unrelated words.</td>
     * </tr>
     * <tr>
     *     <td><code>10004</code></td>
     *     <td>Swear words found and are not allowed.</td>
     * </tr>
     * <tr>
     *     <td><code>10005</code></td>
     *     <td>Sentence contains no nouns.</td>
     * </tr>
     * <tr>
     *     <td><code>10006</code></td>
     *     <td>Only latin charset is supported.</td>
     * </tr>
     * <tr>
     *     <td><code>10007</code></td>
     *     <td>Only english language is supported.</td>
     * </tr>
     * <tr>
     *     <td><code>10008</code></td>
     *     <td>Sentence seems unrelated to data model.</td>
     * </tr>
     * <tr>
     *     <td><code>10009</code></td>
     *     <td>Sentence is too short (before processing).</td>
     * </tr>
     * <tr>
     *     <td><code>10010</code></td>
     *     <td>Sentence is ambiguous.</td>
     * </tr>
     * <tr>
     *     <td><code>10011</code></td>
     *     <td>Sentence is too short (after processing).</td>
     * </tr>
     * <tr>
     *     <td><code>10012</code></td>
     *     <td>Sentence is too long.</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @return Error code if error message is not {@code null}.
     */
    int getErrorCode();

    /**
     * Gets ID of the probe this result was generated by.
     *
     * @return ID of the probe this result was generated by.
     */
    String getProbeId();

    /**
     * Gets request processing log holder as JSON string.
     * 
     * @return Request processing log holder as JSON string.
     */
    String getLogHolder();

    // TODO:
    String getIntentId();
}
