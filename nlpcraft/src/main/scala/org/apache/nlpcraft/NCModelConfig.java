/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft;

import java.util.*;

/**
 *
 */
public interface NCModelConfig extends NCParameterized {
    /**
     * Default value for {@link #getMinTokens()} method.
     */
    int DFLT_MIN_TOKENS = 0;

    /**
     * Default value for {@link #getMaxTokens()} method.
     */
    int DFLT_MAX_TOKENS = 50;

    /**
     * Default value for {@link #getMinNonStopWords()} method.
     */
    int DFLT_MIN_NON_STOPWORDS = 0;

    /**
     * Default value for {@link #getMaxStopWords()} method.
     */
    int DFLT_MAX_STOPWORDS = 15;

    /**
     * Default value for {@link #isNotLatinCharsetAllowed()} method.
     */
    boolean DFLT_IS_NOT_LATIN_CHARSET_ALLOWED = false;

    /**
     *
     * @return
     */
    NCTokenParser getTokenParser();

    /**
     *
     * @return
     */
    List<NCTokenEnricher> getTokenEnrichers();

    /**
     *
     * @return
     */
    List<NCEntityEnricher> getEntityEnrichers();

    /**
     *
     * @return
     */
    List<NCEntityParser> getEntityParsers();

    /**
     * Gets unique, <i>immutable</i> ID of this model.
     *
     * @return Unique, <i>immutable</i> ID of this model.
     */
    String getId();

    /**
     * Gets descriptive name of this model.
     *
     * @return Descriptive name for this model.
     */
    String getName();

    /**
     * Gets the version of this model using semantic versioning.
     *
     * @return A version compatible with (<a href="http://www.semver.org">www.semver.org</a>) specification.
     */
    String getVersion();

    /**
     * Gets optional short model description. This can be displayed by the management tools.
     * Default implementation retusrns <code>null</code>.
     *
     * @return Optional short model description. Can return <code>null</code>.
     */
    default String getDescription() {
        return null;
    }

    /**
     * Gets the origin of this model like name of the class, file path or URL.
     * Default implementation return current class name.
     *
     * @return Origin of this model like name of the class, file path or URL.
     */
    default String getOrigin() {
        return getClass().getCanonicalName();
    }

    /**
     *
     * @return
     */
    default int getMinTokens() {
        return DFLT_MIN_TOKENS;
    }

    /**
     *
     * @return
     */
    default int getMaxTokens() {
        return DFLT_MAX_TOKENS;
    }

    /**
     *
     * @return
     */
    default int getMaxStopWords() {
        return DFLT_MAX_STOPWORDS;
    }

    /**
     *
     * @return
     */
    default int getMinNonStopWords() {
        return DFLT_MIN_NON_STOPWORDS;
    }

    /**
     *
     * @return
     */
    default boolean isNotLatinCharsetAllowed() {
        return DFLT_IS_NOT_LATIN_CHARSET_ALLOWED;
    }
}
