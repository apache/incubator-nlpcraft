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

package org.apache.nlpcraft;/*
   _________            ______________
   __  ____/_______________  __ \__  /_____ _____  __
   _  /    _  __ \_  ___/_  /_/ /_  /_  __ `/_  / / /
   / /___  / /_/ /(__  )_  ____/_  / / /_/ /_  /_/ /
   \____/  \____//____/ /_/     /_/  \__,_/ _\__, /
                                            /____/

          2D ASCII JVM GAME ENGINE FOR SCALA3
              (C) 2021 Rowan Games, Inc.
                ALl rights reserved.
*/

/**
 *
 */
public interface NCModelConfig {
    /**
     * Default value for {@link #getMinWords()} method.
     */
    int DFLT_MIN_WORDS = 1;

    /**
     * Default value for {@link #getMaxWords()} method.
     */
    int DFLT_MAX_WORDS = 50;

    /**
     * Default value for {@link #getMinTokens()} method.
     */
    int DFLT_MIN_TOKENS = 0;

    /**
     * Default value for {@link #getMaxTokens()} method.
     */
    int DFLT_MAX_TOKENS = 50;

    /**
     * Default value for {@link #getMinNonStopwords()} method.
     */
    int DFLT_MIN_NON_STOPWORDS = 0;

    /**
     * Default value for {@link #isNotLatinCharsetAllowed()} method.
     */
    boolean DFLT_IS_NOT_LATIN_CHARSET_ALLOWED = false;

    /**
     * Default value for {@link #isSwearWordsAllowed()} method.
     */
    boolean DFLT_IS_SWEAR_WORDS_ALLOWED = false;

    /**
     * Gets unique, <i>immutable</i> ID of this model.
     * <p>
     * Note that <b>model IDs are immutable</b> while name and version
     * can be changed freely. Changing model ID is equal to creating a completely new model.
     * Model IDs (unlike name and version) are not exposed to the end user and only serve a
     * technical purpose. ID's max length is 32 characters.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>id</code> property:
     * <pre class="brush: js">
     * {
     *      "id": "my.model.id"
     * }
     * </pre>
     *
     * @return Unique, <i>immutable</i> ID of this model.
     */
    String getId();

    /**
     * Gets descriptive name of this model. Name's max length is 64 characters.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>name</code> property:
     * <pre class="brush: js">
     * {
     *      "name": "My Model"
     * }
     * </pre>
     *
     * @return Descriptive name for this model.
     */
    String getName();

    /**
     * Gets the version of this model using semantic versioning. Version's max length is 16 characters.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>version</code> property:
     * <pre class="brush: js">
     * {
     *      "version": "1.0.0"
     * }
     * </pre>
     *
     * @return A version compatible with (<a href="http://www.semver.org">www.semver.org</a>) specification.
     */
    String getVersion();

    /**
     * Gets optional short model description. This can be displayed by the management tools.
     * Default implementation retusrns <code>null</code>.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>description</code> property:
     * <pre class="brush: js">
     * {
     *      "description": "Model description..."
     * }
     * </pre>
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
     * Gets minimum word count (<i>including</i> stopwords) below which user input will be automatically
     * rejected as too short. In almost all cases this value should be greater than or equal to one.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MIN_WORDS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>minWords</code> property:
     * <pre class="brush: js">
     * {
     *      "minWords": 2
     * }
     * </pre>
     *
     * @return Minimum word count (<i>including</i> stopwords) below which user input will be automatically
     * rejected as too short.
     */
    default int getMinWords() {
        return DFLT_MIN_WORDS;
    }

    /**
     * Gets maximum word count (<i>including</i> stopwords) above which user input will be automatically
     * rejected as too long. In almost all cases this value should be greater than or equal to one.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_WORDS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxWords</code> property:
     * <pre class="brush: js">
     * {
     *      "maxWords": 50
     * }
     * </pre>
     *
     * @return Maximum word count (<i>including</i> stopwords) above which user input will be automatically
     * rejected as too long.
     */
    default int getMaxWords() {
        return DFLT_MAX_WORDS;
    }

    /**
     * Gets minimum number of all tokens (system and user defined) below which user input will be
     * automatically rejected as too short. In almost all cases this value should be greater than or equal to one.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MIN_TOKENS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>minTokens</code> property:
     * <pre class="brush: js">
     * {
     *      "minTokens": 1
     * }
     * </pre>
     *
     * @return Minimum number of all tokens.
     */
    default int getMinTokens() {
        return DFLT_MIN_TOKENS;
    }

    /**
     * Gets maximum number of all tokens (system and user defined) above which user input will be
     * automatically rejected as too long. Note that sentences with large number of token can result
     * in significant processing delay and substantial memory consumption.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_TOKENS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxTokens</code> property:
     * <pre class="brush: js">
     * {
     *      "maxTokens": 100
     * }
     * </pre>
     *
     * @return Maximum number of all tokens.
     */
    default int getMaxTokens() {
        return DFLT_MAX_TOKENS;
    }

    /**
     * Gets minimum word count (<i>excluding</i> stopwords) below which user input will be automatically rejected
     * as ambiguous sentence.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MIN_NON_STOPWORDS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>minNonStopwords</code> property:
     * <pre class="brush: js">
     * {
     *      "minNonStopwords": 2
     * }
     * </pre>
     *
     * @return Minimum word count (<i>excluding</i> stopwords) below which user input will be automatically
     * rejected as too short.
     */
    default int getMinNonStopwords() {
        return DFLT_MIN_NON_STOPWORDS;
    }

    /**
     * Whether to allow non-Latin charset in user input. Currently, only
     * Latin charset is supported. However, model can choose whether to automatically reject user
     * input with characters outside of Latin charset. If {@code false} such user input will be automatically
     * rejected.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_NOT_LATIN_CHARSET_ALLOWED} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>nonLatinCharsetAllowed</code> property:
     * <pre class="brush: js">
     * {
     *      "nonLatinCharsetAllowed": false
     * }
     * </pre>
     *
     * @return Whether to allow non-Latin charset in user input.
     */
    default boolean isNotLatinCharsetAllowed() {
        return DFLT_IS_NOT_LATIN_CHARSET_ALLOWED;
    }

    /**
     * Whether to allow known swear words in user input. If {@code false} - user input with
     * detected known swear words will be automatically rejected.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_SWEAR_WORDS_ALLOWED} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>swearWordsAllowed</code> property:
     * <pre class="brush: js">
     * {
     *      "swearWordsAllowed": false
     * }
     * </pre>
     *
     * @return Whether to allow known swear words in user input.
     */
    default boolean isSwearWordsAllowed() {
        return DFLT_IS_SWEAR_WORDS_ALLOWED;
    }
}
