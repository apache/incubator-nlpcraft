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

import org.apache.nlpcraft.common.*;
import java.util.*;

/**
 * Provides support for map-based metadata.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 */
public interface NCMetadata {
    /**
     * Gets metadata.
     *
     * @return Metadata.
     * @see #meta(String)
     * @see #metaOpt(String)
     * @see #meta(String, Object)
     */
    Map<String, Object> getMetadata();

    /**
     * Shortcut method to get given optional metadata property. Equivalent to:
     * <pre class="brush: java">
     *      Optional.ofNullable((T)getMetadata().get(prop));
     * </pre>
     *
     * @param prop Metadata property name.
     * @param <T> Type of the metadata property.
     * @return Metadata optional property value.
     */
    @SuppressWarnings("unchecked")
    default <T> Optional<T> metaOpt(String prop) {
        return Optional.ofNullable((T)getMetadata().get(prop));
    }

    /**
     * Shortcut method to get given metadata property. Equivalent to:
     * <pre class="brush: java">
     *      (T)getMetadata().get(prop);
     * </pre>
     *
     * @param prop Metadata property name.
     * @param <T> Type of the metadata property.
     * @return Metadata property value or {@code null} if given metadata property not found.
     */
    @SuppressWarnings("unchecked")
    default <T> T meta(String prop) {
        return (T)getMetadata().get(prop);
    }

    /**
     * Shortcut method to get given mandatory metadata property. Equivalent to:
     * <pre class="brush: java">
     *     T t = (T)getMetadata().get(prop);
     *     if (t == null)
     *         throw new NCException("Mandatory metadata property not found: " + prop);
     *     else
     *         return t;
     * </pre>
     *
     * @param prop Metadata property name.
     * @param <T> Type of the metadata property.
     * @return Metadata property value or throws an exception if given metadata property not found.
     * @throws NCException Thrown if given metadata property not found.
     */
    default <T> T metax(String prop) throws NCException {
        T t = meta(prop);

        if (t == null)
            throw new NCException("Mandatory metadata property not found: " + prop);
        else
            return t;
    }

    /**
     * Shortcut method to get given metadata property. Equivalent to:
     * <pre class="brush: java">
     *      getMetadata().get(tokId, prop, dflt);
     * </pre>
     *
     * @param prop Metadata property name.
     * @param dflt Default value to return if specified one isn't set.
     * @param <T> Type of the metadata property.
     * @return Metadata property value or default value if one isn't set.
     */
    @SuppressWarnings("unchecked")
    default <T> T meta(String prop, T dflt) {
        return (T)getMetadata().getOrDefault(prop, dflt);
    }
}
