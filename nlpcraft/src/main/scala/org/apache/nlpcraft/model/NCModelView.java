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

import java.time.Duration;
import java.util.*;

/**
 * Read-only view on data model. Model view defines a declarative, or configurable, part of the model.
 * All properties in this interface can be defined or overridden in JSON/YAML external
 * presentation when used with {@link NCModelFileAdapter} adapter.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples">examples</a>.
 *
 * @see NCModel
 * @see NCModelAdapter
 * @see NCModelFileAdapter
 */
public interface NCModelView extends NCMetadata {
    /**
     * Min value for {@link #getConversationTimeout()} method.
     */
    long CONV_TIMEOUT_MIN = 0L;

    /**
     * Max value for {@link #getConversationTimeout()} method.
     */
    long CONV_TIMEOUT_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getMaxUnknownWords()} method.
     */
    long MAX_UNKNOWN_WORDS_MIN = 0L;

    /**
     * Max value for {@link #getMaxUnknownWords()} method.
     */
    long MAX_UNKNOWN_WORDS_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getMaxFreeWords()} method.
     */
    long MAX_FREE_WORDS_MIN = 0L;

    /**
     * Max value for {@link #getMaxFreeWords()} method.
     */
    long MAX_FREE_WORDS_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getMaxSuspiciousWords()} method.
     */
    long MAX_SUSPICIOUS_WORDS_MIN = 0L;

    /**
     * Max value for {@link #getMaxSuspiciousWords()} method.
     */
    long MAX_SUSPICIOUS_WORDS_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getMinWords()} method.
     */
    long MIN_WORDS_MIN = 1L;

    /**
     * Max value for {@link #getMinWords()} method.
     */
    long MIN_WORDS_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getMinNonStopwords()} method.
     */
    long MIN_NON_STOPWORDS_MIN = 0L;

    /**
     * Max value for {@link #getMinNonStopwords()} method.
     */
    long MIN_NON_STOPWORDS_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getMinTokens()} method.
     */
    long MIN_TOKENS_MIN = 0L;

    /**
     * Max value for {@link #getMinTokens()} method.
     */
    long MIN_TOKENS_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getMaxTokens()} method.
     */
    long MAX_TOKENS_MIN = 0L;

    /**
     * Max value for {@link #getMaxTokens()} method.
     */
    long MAX_TOKENS_MAX = 100L;

    /**
     * Min value for {@link #getMaxWords()} method.
     */
    long MAX_WORDS_MIN = 1L;

    /**
     * Max value for {@link #getMaxWords()} method.
     */
    long MAX_WORDS_MAX = 100L;

    /**
     * Min value for {@link #getMaxElementSynonyms()} method.
     */
    long MAX_SYN_MIN = 1L;

    /**
     * Max value for {@link #getMaxElementSynonyms()} method.
     */
    long MAX_SYN_MAX = Long.MAX_VALUE;

    /**
     * Min value for {@link #getConversationDepth()} method.
     */
    long CONV_DEPTH_MIN = 1L;

    /**
     * Max value for {@link #getConversationDepth()} method.
     */
    long CONV_DEPTH_MAX = Long.MAX_VALUE;

    /**
     * Max length for {@link #getId()} method.
     */
    int MODEL_ID_MAXLEN = 32;

    /**
     * Max length for {@link #getName()} method.
     */
    int MODEL_NAME_MAXLEN = 64;

    /**
     * Max length for {@link #getVersion()} method.
     */
    int MODEL_VERSION_MAXLEN = 16;

    /**
     * Default value for {@link #isSparse()} method.
     */
    boolean DFLT_IS_SPARSE = false;

    /**
     * Default value for {@link #getMaxElementSynonyms()} method.
     */
    int DFLT_MAX_ELEMENT_SYNONYMS = 1000;

    /**
     * Default value for {@link #getMaxTotalSynonyms()} method.
     */
    int DFLT_MAX_TOTAL_SYNONYMS = Integer.MAX_VALUE;

    /**
     * Default value for {@link #isMaxSynonymsThresholdError()} method.
     */
    boolean DFLT_MAX_SYNONYMS_THRESHOLD_ERROR = false;

    /**
     * Default value for {@link #getConversationTimeout()} method.
     */
    long DFLT_CONV_TIMEOUT_MS = Duration.ofMinutes(60).toMillis();

    /**
     * Default value for {@link #getConversationDepth()} method.
     */
    int DFLT_CONV_DEPTH = 3;

    /**
     * Default value fof {@link #getMetadata()} method.
     */
    Map<String, Object> DFLT_METADATA = new HashMap<>();

    /**
     * Default value for {@link #getMaxUnknownWords()} method.
     */
    int DFLT_MAX_UNKNOWN_WORDS = Integer.MAX_VALUE;

    /**
     * Default value for {@link #getMaxFreeWords()} method.
     */
    int DFLT_MAX_FREE_WORDS = Integer.MAX_VALUE;

    /**
     * Default value for {@link #getMaxSuspiciousWords()} method.
     */
    int DFLT_MAX_SUSPICIOUS_WORDS = 0;

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
     * Default value for {@link #isNonEnglishAllowed()} method.
     */
    boolean DFLT_IS_NON_ENGLISH_ALLOWED = true;

    /**
     * Default value for {@link #isNotLatinCharsetAllowed()} method.
     */
    boolean DFLT_IS_NOT_LATIN_CHARSET_ALLOWED = false;

    /**
     * Default value for {@link #isSwearWordsAllowed()} method.
     */
    boolean DFLT_IS_SWEAR_WORDS_ALLOWED = false;

    /**
     * Default value for {@link #isNoNounsAllowed()} method.
     */
    boolean DFLT_IS_NO_NOUNS_ALLOWED = true;

    /**
     * Default value for {@link #isPermutateSynonyms()} method.
     */
    boolean DFLT_IS_PERMUTATE_SYNONYMS = false;

    /**
     * Default value for {@link #isDupSynonymsAllowed()} method.
     */
    boolean DFLT_IS_DUP_SYNONYMS_ALLOWED = true;

    /**
     * Default value for {@link #isNoUserTokensAllowed()} method.
     */
    boolean DFLT_IS_NO_USER_TOKENS_ALLOWED = true;

    /**
     * Default set of enabled built-in tokens. The following built-in tokens are enabled by default:
     * <ul>
     * <li><code>nlpcraft:date</code></li>
     * <li><code>nlpcraft:continent</code></li>
     * <li><code>nlpcraft:subcontinent</code></li>
     * <li><code>nlpcraft:country</code></li>
     * <li><code>nlpcraft:metro</code></li>
     * <li><code>nlpcraft:region</code></li>
     * <li><code>nlpcraft:city</code></li>
     * <li><code>nlpcraft:num</code></li>
     * <li><code>nlpcraft:coordinate</code></li>
     * <li><code>nlpcraft:relation</code></li>
     * <li><code>nlpcraft:sort</code></li>
     * <li><code>nlpcraft:limit</code></li>
     * </ul>
     */
    Set<String> DFLT_ENABLED_BUILTIN_TOKENS =
        new HashSet<>(
            Arrays.asList(
                "nlpcraft:date",
                "nlpcraft:continent",
                "nlpcraft:subcontinent",
                "nlpcraft:country",
                "nlpcraft:metro",
                "nlpcraft:region",
                "nlpcraft:city",
                "nlpcraft:num",
                "nlpcraft:coordinate",
                "nlpcraft:relation",
                "nlpcraft:sort",
                "nlpcraft:limit"
            )
        );

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
     * @return Optional short model description.
     */
    default String getDescription() {
        return null;
    }

    /**
     * Gets the origin of this model like name of the class, file path or URL.
     *
     * @return Origin of this model like name of the class, file path or URL.
     */
    default String getOrigin() {
        return getClass().getCanonicalName();
    }

    /**
     * Gets maximum number of unknown words until automatic rejection. An unknown word is a word
     * that is not part of Princeton WordNet database. If you expect a very formalized and well
     * defined input without uncommon slang and abbreviations you can set this to a small number
     * like one or two. However, in most cases we recommend to leave it as default or set it to a larger
     * number like five or more.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_UNKNOWN_WORDS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxUnknownWords</code> property:
     * <pre class="brush: js">
     * {
     *      "maxUnknownWords": 2
     * }
     * </pre>
     *
     * @return Maximum number of unknown words until automatic rejection.
     */
    default int getMaxUnknownWords() {
        return DFLT_MAX_UNKNOWN_WORDS;
    }

    /**
     * Gets maximum number of free words until automatic rejection. A free word is a known word that is
     * not part of any recognized token. In other words, a word that is present in the user input
     * but won't be used to understand its meaning. Setting it to a non-zero risks the misunderstanding
     * of the user input, while setting it to zero often makes understanding logic too rigid. In most
     * cases we recommend setting to between one and three. If you expect the user input to contain
     * many <i>noisy</i> idioms, slang or colloquials - you can set it to a larger number.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_FREE_WORDS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxFreeWords</code> property:
     * <pre class="brush: js">
     * {
     *      "maxFreeWords": 2
     * }
     * </pre>
     *
     * @return Maximum number of free words until automatic rejection.
     */
    default int getMaxFreeWords() {
        return DFLT_MAX_FREE_WORDS;
    }

    /**
     * Gets maximum number of suspicious words until automatic rejection. A suspicious word is a word
     * that is defined by the model that should not appear in a valid user input under no circumstances.
     * A typical example of suspicious words would be words "sex" or "porn" when processing
     * queries about children books. In most cases this should be set to zero (default) to automatically
     * reject any such suspicious words in the user input.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_SUSPICIOUS_WORDS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxSuspiciousWords</code> property:
     * <pre class="brush: js">
     * {
     *      "maxSuspiciousWords": 2
     * }
     * </pre>
     *
     * @return Maximum number of suspicious words until automatic rejection.
     */
    default int getMaxSuspiciousWords() {
        return DFLT_MAX_SUSPICIOUS_WORDS;
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
     * Whether or not to allow non-English language in user input.
     * Currently, only English language is supported. However, model can choose whether or not
     * to automatically reject user input that is detected to be a non-English. Note that current
     * algorithm only works reliably on longer user input (10+ words). On short sentences it will
     * often produce an incorrect result.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_NON_ENGLISH_ALLOWED} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>nonEnglishAllowed</code> property:
     * <pre class="brush: js">
     * {
     *      "nonEnglishAllowed": false
     * }
     * </pre>
     *
     * @return Whether or not to allow non-English language in user input.
     */
    default boolean isNonEnglishAllowed() {
        return DFLT_IS_NON_ENGLISH_ALLOWED;
    }

    /**
     * Whether or not to allow non-Latin charset in user input. Currently, only
     * Latin charset is supported. However, model can choose whether or not to automatically reject user
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
     * @return Whether or not to allow non-Latin charset in user input.
     */
    default boolean isNotLatinCharsetAllowed() {
        return DFLT_IS_NOT_LATIN_CHARSET_ALLOWED;
    }

    /**
     * Whether or not to allow known English swear words in user input. If {@code false} - user input with
     * detected known English swear words will be automatically rejected.
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
     * @return Whether or not to allow known swear words in user input.
     */
    default boolean isSwearWordsAllowed() {
        return DFLT_IS_SWEAR_WORDS_ALLOWED;
    }

    /**
     * Whether or not to allow user input without a single noun. If {@code false} such user input
     * will be automatically rejected. Typically for strict command or query-oriented models this should be set to
     * {@code false} as any command or query should have at least one noun subject. However, for conversational
     * models this can be set to {@code false} to allow for a smalltalk and one-liners.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_NO_NOUNS_ALLOWED} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>noNounsAllowed</code> property:
     * <pre class="brush: js">
     * {
     *      "noNounsAllowed": false
     * }
     * </pre>
     *
     * @return Whether or not to allow user input without a single noun.
     */
    default boolean isNoNounsAllowed() {
        return DFLT_IS_NO_NOUNS_ALLOWED;
    }

    /**
     * Whether or not to permutate multi-word synonyms. Automatic multi-word synonyms permutations greatly
     * increase the total number of synonyms in the system and allows for better multi-word synonym detection.
     * For example, if permutation is allowed the synonym "a b c" will be automatically converted into a
     * sequence of synonyms of "a b c", "b a c", "a c b".
     * <p>
     * Note that individual model elements can override this property using {@link NCElement#isPermutateSynonyms()}
     * method.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_PERMUTATE_SYNONYMS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>permutateSynonyms</code> property:
     * <pre class="brush: js">
     * {
     *      "permutateSynonyms": true
     * }
     * </pre>
     *
     * @return Whether or not to permutate multi-word synonyms.
     * @see NCElement#isPermutateSynonyms()
     */
    default boolean isPermutateSynonyms() {
        return DFLT_IS_PERMUTATE_SYNONYMS;
    }

    /**
     * Whether or not duplicate synonyms are allowed. If {@code true} - the model will pick the random
     * model element when multiple elements found due to duplicate synonyms. If {@code false} - model
     * will print error message and will not deploy.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_DUP_SYNONYMS_ALLOWED} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>dupSynonymsAllowed</code> property:
     * <pre class="brush: js">
     * {
     *      "dupSynonymsAllowed": true
     * }
     * </pre>
     *
     * @return Whether or not to allow duplicate synonyms.
     */
    default boolean isDupSynonymsAllowed() {
        return DFLT_IS_DUP_SYNONYMS_ALLOWED;
    }

    /**
     * Total number of synonyms allowed per model. Model won't deploy if total number of synonyms exceeds this
     * number.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_TOTAL_SYNONYMS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxTotalSynonyms</code> property:
     * <pre class="brush: js">
     * {
     *      "maxTotalSynonyms": true
     * }
     * </pre>
     *
     * @return Total number of synonyms allowed per model.
     * @see #getMaxElementSynonyms()
     */
    default int getMaxTotalSynonyms() {
        return DFLT_MAX_TOTAL_SYNONYMS;
    }

    /**
     * Whether or not to allow the user input with no user token detected. If {@code false} such user
     * input will be automatically rejected. Note that this property only applies to user-defined
     * token (i.e. model element). Even if there are no user defined tokens, the user input may still
     * contain system token like <code>nlpcraft:city</code> or <code>nlpcraft:date</code>. In many cases models
     * should be build to allow user input without user tokens. However, set it to {@code false} if presence
     * of at least one user token is mandatory.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_NO_USER_TOKENS_ALLOWED} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>noUserTokensAllowed</code> property:
     * <pre class="brush: js">
     * {
     *      "noUserTokensAllowed": false
     * }
     * </pre>
     *
     * @return Whether or not to allow the user input with no user token detected.
     */
    default boolean isNoUserTokensAllowed() {
        return DFLT_IS_NO_USER_TOKENS_ALLOWED;
    }

    /**
     * Whether or not this model elements allows non-stop words gaps in their multi-word synonyms.
     * <p>
     * Note that individual model elements can override this property using {@link NCElement#isSparse()}
     * method.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_IS_SPARSE} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>sparse</code>:
     * <pre class="brush: js, highlight: [2]">
     * {
     *      "sparse": true
     * }
     * </pre>
     *
     * @return Optional multi-word synonym sparsity model property.
     * @see NCElement#isSparse()
     */
    default boolean isSparse() {
        return DFLT_IS_SPARSE;
    }

    /**
     * Gets optional user defined model metadata that can be set by the developer and accessed later.
     * By default, it returns an empty map. Note that this metadata is mutable and can be
     * changed at runtime by the model's code.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>metadata</code> property:
     * <pre class="brush: js">
     * {
     *      "metadata": {
     *          "str": "val1",
     *          "num": 100,
     *          "bool": false
     *      }
     * }
     * </pre>
     *
     * @return Optional user defined model metadata. By default, returns an empty map. Never returns {@code null}.
     */
    default Map<String, Object> getMetadata() {
        return DFLT_METADATA;
    }

    /**
     * Gets an optional list of stopwords to add to the built-in ones.
     * <p>
     * Stopword is an individual word (i.e. sequence of characters excluding whitespaces) that contribute no
     * semantic meaning to the sentence. For example, 'the', 'wow', or 'hm' provide no semantic meaning to the
     * sentence and can be safely excluded from semantic analysis.
     * <p>
     * NLPCraft comes with a carefully selected list of English stopwords which should be sufficient
     * for a majority of use cases. However, you can add additional stopwords to this list. The typical
     * use for user-defined stopwords are jargon parasite words that are specific to the model's domain.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>additionalStopwords</code> property:
     * <pre class="brush: js">
     * {
     *      "additionalStopwords": [
     *          "stopword1",
     *          "stopword2"
     *      ]
     * }
     * </pre>
     *
     * @return Potentially empty list of additional stopwords.
     */
    default Set<String> getAdditionalStopWords() {
        return Collections.emptySet();
    }

    /**
     * Gets an optional list of stopwords to exclude from the built-in list of stopwords.
     * <p>
     * Just like you can add additional stopwords via {@link #getAdditionalStopWords()} you can exclude
     * certain words from the list of stopwords. This can be useful in rare cases when default built-in
     * stopword has specific meaning of your model. In order to process them you need to exclude them
     * from the list of stopwords.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>excludedStopwords</code> property:
     * <pre class="brush: js">
     * {
     *      "excludedStopwords": [
     *          "excludedStopword1",
     *          "excludedStopword2"
     *      ]
     * }
     * </pre>
     *
     * @return Potentially empty list of excluded stopwords.
     */
    default Set<String> getExcludedStopWords() {
        return Collections.emptySet();
    }

    /**
     * Gets an optional list of suspicious words. A suspicious word is a word that generally should not appear in user
     * sentence when used with this model. For example, if a particular model is for children oriented book search,
     * the words "sex" and "porn" should probably NOT appear in the user input and can be automatically rejected
     * when added here and model's metadata {@code MAX_SUSPICIOUS_WORDS} property set to zero.
     * <p>
     * Note that by setting model's metadata {@code MAX_SUSPICIOUS_WORDS} property to non-zero value you can
     * adjust the sensitivity of suspicious words auto-rejection logic.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>suspiciousWords</code> property:
     * <pre class="brush: js">
     * {
     *      "suspiciousWords": [
     *          "sex",
     *          "porn"
     *      ]
     * }
     * </pre>
     *
     * @return Potentially empty list of suspicious words in their lemma form.
     */
    default Set<String> getSuspiciousWords() {
        return Collections.emptySet();
    }

    /**
     * Gets an optional map of macros to be used in this model. Macros and option groups are instrumental
     * in defining model's elements. See {@link NCElement} for documentation on macros.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>macros</code> property:
     * <pre class="brush: js">
     * {
     *      "macros": [
     *          {
     *              "name": "&lt;OF&gt;",
     *              "macro": "{of|for|per}"
     *          },
     *          {
     *              "name": "&lt;CUR&gt;",
     *              "macro": "{current|present|moment|now}"
     *          }
     *      ]
     * }
     * </pre>
     *
     * @return Potentially empty map of macros.
     */
    default Map<String, String> getMacros() {
        return Collections.emptyMap();
    }
    
    /**
     * Gets optional user-defined model element parsers for custom NER implementations. Note that order of the parsers
     * is important as they will be invoked in the same order they are returned.
     * <p>
     * By default, the data model detects its elements by their synonyms, regexp or IDL expressions. However,
     * in some cases these methods are not expressive enough. In such cases, a user-defined parser can be defined
     * for the model that would allow the user to define its own NER logic to detect the model elements in the user
     * input programmatically. Note that a single parser can detect any number of model elements.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>parser</code> property which is an array
     * with every element being a fully qualified class name implementing {@link NCCustomParser} interface:
     * <pre class="brush: js">
     * {
     *      "parsers": [
     *          "my.package.Parser1",
     *          "my.package.Parser2"
     *      ]
     * }
     * </pre>
     *
     * @return Custom user parsers for model elements or empty list if not used (default). Never returns {@code null}.
     */
    default List<NCCustomParser> getParsers() {
        return Collections.emptyList();
    }

    /**
     * Gets a set of model elements or named entities. Model can have zero or more user defined elements.
     * <p>
     * An element is the main building block of the model. Data model element defines a named entity
     * that will be automatically recognized in the user input. See also {@link NCModel#getParsers()} method on how
     * to provide programmatic named entity recognizer (NER) implementations.
     * <p>
     * Note that unless model elements are loaded dynamically it is highly recommended to declare model
     * elements in the external JSON/YAML model configuration (under <code>elements</code> property):
     * <pre class="brush: js">
     * {
     *      "elements": [
     *         {
     *             "id": "wt:hist",
     *             "synonyms": [
     *                 "{&lt;WEATHER&gt;|_} &lt;HISTORY&gt;",
     *                 "&lt;HISTORY&gt; {&lt;OF&gt;|_} &lt;WEATHER&gt;"
     *             ],
     *             "description": "Past weather conditions."
     *         }
     *      ]
     * }
     * </pre>
     *
     * @return Set of model elements, potentially empty.
     * @see NCModel#getParsers() 
     */
    default Set<NCElement> getElements() {
        return Collections.emptySet();
    }

    /**
     * Gets a set of IDs for built-in named entities (tokens) that should be enabled and detected for this model.
     * Unless model requests (i.e. enables) the built-in tokens in this method the NLP subsystem will not attempt
     * to detect them. Explicit enablement of the token significantly improves the overall performance by avoiding
     * unnecessary token detection. Note that you don't have to specify your own user elements here as they are
     * always enabled.
     * <p>
     * <b>Default</b>
     * <br>
     * The following built-in tokens are enabled by default implementation of this method:
     * <ul>
     * <li><code>nlpcraft:date</code></li>
     * <li><code>nlpcraft:continent</code></li>
     * <li><code>nlpcraft:subcontinent</code></li>
     * <li><code>nlpcraft:country</code></li>
     * <li><code>nlpcraft:metro</code></li>
     * <li><code>nlpcraft:region</code></li>
     * <li><code>nlpcraft:city</code></li>
     * <li><code>nlpcraft:num</code></li>
     * <li><code>nlpcraft:coordinate</code></li>
     * <li><code>nlpcraft:relation</code></li>
     * <li><code>nlpcraft:sort</code></li>
     * <li><code>nlpcraft:limit</code></li>
     * </ul>
     * Note that this method can return an empty list if the data model doesn't need any built-in tokens
     * for its logic. See {@link NCToken} for the list of all supported built-in tokens.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>enabledBuiltInTokens</code> property:
     * <pre class="brush: js">
     * {
     *      "enabledBuiltInTokens": [
     *          "google:person",
     *          "google:location",
     *          "stanford:money"
     *      ]
     * }
     * </pre>
     *
     * @return Set of built-in tokens, potentially empty but never {@code null}, that should be enabled
     *      and detected for this model.
     */
    default Set<String> getEnabledBuiltInTokens() {
        return DFLT_ENABLED_BUILTIN_TOKENS;
    }

    /**
     * Gets s set of named entities (token) IDs that will be considered as abstract tokens.
     * An abstract token is only detected when it is either a constituent part of some other non-abstract token
     * or referenced by built-in tokens. In other words, an abstract token will not be detected in a standalone
     * unreferenced position. By default (unless returned by this method), all named entities considered to be
     * non-abstract.
     * <p>
     * Declaring tokens as abstract is important to minimize number of parsing variants automatically
     * generated as permutation of all possible parsing compositions. For example, if it is known that a particular
     * named entity will only be used as a constituent part of some other token - declaring such named entity as
     * abstract can significantly reduce the number of parsing variants leading to a better performance,
     * and often simpler corresponding intent definition and callback logic.
     *
     * @return Set of abstract token IDs. Can be empty but never {@code null}.
     */
    default Set<String> getAbstractTokens() {
        return Collections.emptySet();
    }

    /**
     * Gets maximum number of unique synonyms per model element after which either warning or error will be
     * triggered. Note that there is no technical limit on how many synonyms a model element can have apart
     * from memory consumption and performance considerations. However, in cases where synonyms are auto-generated
     * (i.e. from database) this property can serve as a courtesy notification that a model element has too many
     * synonyms. Also, in general, too many synonyms can potentially lead to a performance degradation.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_ELEMENT_SYNONYMS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxSynonymThreshold</code> property:
     * <pre class="brush: js">
     * {
     *      "maxSynonymThreshold": 1000
     * }
     * </pre>
     *
     * @return Maximum number of unique synonyms per model element after which either warning or
     *      error will be triggered.
     * @see #isMaxSynonymsThresholdError()
     * @see #getMaxTotalSynonyms()
     */
    default int getMaxElementSynonyms() { return DFLT_MAX_ELEMENT_SYNONYMS; }

    /**
     * Whether or not exceeding {@link #getMaxElementSynonyms()} will trigger a warning log or throwing an exception.
     * Note that throwing exception will prevent data probe from starting.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_MAX_SYNONYMS_THRESHOLD_ERROR} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>maxSynonymThresholdError</code> property:
     * <pre class="brush: js">
     * {
     *      "maxSynonymThresholdError": true
     * }
     * </pre>
     *
     * @return Whether or not exceeding {@link #getMaxElementSynonyms()} will trigger a warning log or
     *      throwing an exception.
     * @see #getMaxElementSynonyms()
     */
    default boolean isMaxSynonymsThresholdError() { return DFLT_MAX_SYNONYMS_THRESHOLD_ERROR; }

    /**
     * Gets timeout in ms after which the unused conversation element is automatically "forgotten".
     * <p>
     * Just like in a normal human conversation if we talk about, say, "Chicago", and then don't mention it
     * for certain period of time during further dialog, the conversation participants subconsciously "forget"
     * about it and exclude it from conversation context. In other words, the term "Chicago" is no longer in
     * conversation's short-term-memory.
     * <p>
     * Note that both conversation timeout and {@link #getConversationDepth() depth}
     * combined define the expiration policy for the conversation management. These two properties allow to
     * fine tune for different types of dialogs. For example, setting longer timeout and smaller depth mimics
     * slow-moving but topic-focused conversation. Alternatively, settings shorter timeout and longer depth better
     * supports fast-moving wide-ranging conversation that may cover multiple topics.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_CONV_TIMEOUT_MS} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>conversationTimeout</code> property:
     * <pre class="brush: js">
     * {
     *      "conversationTimeout": 300000
     * }
     * </pre>
     *
     * @return Timeout in ms after which the unused conversation element is automatically "forgotten".
     * @see #getConversationDepth()
     */
    default long getConversationTimeout() { return DFLT_CONV_TIMEOUT_MS; }

    /**
     * Gets maximum number of requests after which the unused conversation element is automatically "forgotten".
     * <p>
     * Just like in a normal human conversation if we talk about, say, "Chicago", and then don't mention it
     * for a certain number of utterances during further dialog, the conversation participants subconsciously "forget"
     * about it and exclude it from conversation context. In other words, the term "Chicago" is no longer in
     * conversation's short-term-memory.
     * <p>
     * Note that both conversation {@link #getConversationTimeout() timeout} and depth
     * combined define the expiration policy for the conversation management. These two properties allow to
     * fine tune for different types of dialogs. For example, setting longer timeout and smaller depth mimics
     * slow-moving but topic-focused conversation. Alternatively, settings shorter timeout and longer depth better
     * supports fast-moving wide-ranging conversation that may cover multiple topics.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_CONV_DEPTH} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>conversationDepth</code> property:
     * <pre class="brush: js">
     * {
     *      "conversationDepth": 5
     * }
     * </pre>
     *
     * @return Maximum number of requests after which the unused conversation element is automatically "forgotten".
     * @see #getConversationTimeout()
     */
    default int getConversationDepth() { return DFLT_CONV_DEPTH; }

    /**
     * Gets an optional map of restricted named entity combinations (linkage). Returned map is a map of entity ID to a set
     * of other entity IDs, with each key-value pair defining the restricted combination. Restricting certain entities
     * from being linked (or referenced) by some other entities allows to reduce "wasteful" parsing variant
     * generation. For example, it we know that entity with ID "adjective" cannot be sorted, we can restrict it
     * from being linked with <code>nlpcraft:limit</code> and <code>nlpcraft:sort</code> entities to reduce the
     * amount of parsing variants being generated.
     * <p>
     * Only the following built-in entities can be restricted (i.e., to be the keys in the returned map):
     * <ul>
     *     <li><code>nlpcraft:limit</code></li>
     *     <li><code>nlpcraft:sort</code></li>
     *     <li><code>nlpcraft:relation</code></li>
     * </ul>
     * Note that entity cannot be restricted to itself (entity ID cannot appear as key as well as a
     * part of the value's set).
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>restrictedCombinations</code> property:
     * <pre class="brush: js">
     * {
     *      "restrictedCombinations": {
     *          "nlpcraft:limit": ["adjective"],
     *          "nlpcraft:sort": ["adjective"]
     *      }
     * }
     * </pre>
     *
     * @return Optional map of restricted named entity combinations. Can be empty but never {@code null}.
     * @see NCVariant
     */
    default Map<String, Set<String>> getRestrictedCombinations() {
        return Collections.emptyMap();
    }

    // TODO: 0 .. 1
    // Empty - means disabled.  default. Can be overridden by each elements.
    default Optional<Double> getContextWordStrictLevel() {
        return Optional.empty();
    }
}
