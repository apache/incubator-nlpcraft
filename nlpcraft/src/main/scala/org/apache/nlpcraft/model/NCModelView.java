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

import java.util.*;

/**
 * Read-only view on data model. Model view defines a declarative, or configurable, part of the model.
 * All properties in this interface can be defined or overridden in JSON/YAML external
 * presentation when used with {@link NCModelFileAdapter} adapter.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCModel
 * @see NCModelAdapter
 * @see NCModelFileAdapter
 */
public interface NCModelView extends NCMetadata {
    /**
     * Default value returned from {@link #getJiggleFactor()} method.
     */
    int DFLT_JIGGLE_FACTOR = 2;

    /**
     * Default value returned from {@link #getJiggleFactor()} method.
     */
    Map<String, Object> DFLT_METADATA = new HashMap<>();

    /**
     * Default value returned from {@link #getMaxUnknownWords()} method.
     */
    int DFLT_MAX_UNKNOWN_WORDS = Integer.MAX_VALUE;

    /**
     * Default value returned from {@link #getMaxFreeWords()} method.
     */
    int DFLT_MAX_FREE_WORDS = Integer.MAX_VALUE;

    /**
     * Default value returned from {@link #getMaxSuspiciousWords()} method.
     */
    int DFLT_MAX_SUSPICIOUS_WORDS = 0;

    /**
     * Default value returned from {@link #getMinWords()} method.
     */
    int DFLT_MIN_WORDS = 1;

    /**
     * Default value returned from {@link #getMaxWords()} method.
     */
    int DFLT_MAX_WORDS = 50;

    /**
     * Default value returned from {@link #getMinTokens()} method.
     */
    int DFLT_MIN_TOKENS = 0;

    /**
     * Default value returned from {@link #getMaxTokens()} method.
     */
    int DFLT_MAX_TOKENS = 50;

    /**
     * Default value returned from {@link #getMinNonStopwords()} method.
     */
    int DFLT_MIN_NON_STOPWORDS = 0;

    /**
     * Default value returned from {@link #isNonEnglishAllowed()} method.
     */
    boolean DFLT_IS_NON_ENGLISH_ALLOWED = true;

    /**
     * Default value returned from {@link #isNotLatinCharsetAllowed()} method.
     */
    boolean DFLT_IS_NOT_LATIN_CHARSET_ALLOWED = false;

    /**
     * Default value returned from {@link #isSwearWordsAllowed()} method.
     */
    boolean DFLT_IS_SWEAR_WORDS_ALLOWED = false;

    /**
     * Default value returned from {@link #isNoNounsAllowed()} method.
     */
    boolean DFLT_IS_NO_NOUNS_ALLOWED = true;

    /**
     * Default value returned from {@link #isPermutateSynonyms()} method.
     */
    boolean DFLT_IS_PERMUTATE_SYNONYMS = true;

    /**
     * Default value returned from {@link #isDupSynonymsAllowed()} method.
     */
    boolean DFLT_IS_DUP_SYNONYMS_ALLOWED = true;

    /**
     * Default value returned from {@link #getMaxTotalSynonyms()} method.
     */
    int DFLT_MAX_TOTAL_SYNONYMS = Integer.MAX_VALUE;

    /**
     * Default value returned from {@link #isNoUserTokensAllowed()} method.
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
     * will be automatically rejected. Typically for command or query-oriented models this should be set to
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
     * Measure of how much sparsity is allowed when user input words are reordered in attempt to
     * match the multi-word synonyms. Zero means no reordering is allowed. One means
     * that a word in a synonym can move only one position left or right, and so on. Empirically
     * the value of {@code 2} proved to be a good default value in most cases. Note that larger
     * values mean that synonym words can be almost in any random place in the user input which makes
     * synonym matching practically meaningless. Maximum value is <code>4</code>.
     * <p>
     * <b>Default</b>
     * <br>
     * If not provided by the model the default value {@link #DFLT_JIGGLE_FACTOR} will be used.
     * <p>
     * <b>JSON</b>
     * <br>
     * If using JSON/YAML model presentation this is set by <code>jiggleFactor</code> property:
     * <pre class="brush: js">
     * {
     *      "jiggleFactor": 2
     * }
     * </pre>
     *
     * @return Word jiggle factor (sparsity measure).
     */
    default int getJiggleFactor() {
        return DFLT_JIGGLE_FACTOR;
    }

    /**
     * Gets optional user defined model metadata that can be set by the developer and accessed later.
     * By default returns an empty map. Note that this metadata is different from the one returned
     * by {@link NCToken#getMetadata()} method.
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
     * @return Optional user defined model metadata.
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
     * By default the semantic data model detects its elements by their synonyms, regexp or DSL expressions. However,
     * in some cases these methods are not expressive enough. In such cases, a user-defined parser can be defined
     * for the model that would allow the user to define its own NER logic to detect the model elements in the user
     * input programmatically. Note that there can be only one custom parser per model and it can detect any number
     * of model elements (named entities).
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
     * @return Custom user parsers for model elements or {@code null} if not used (default).
     */
    default List<NCCustomParser> getParsers() {
        return Collections.emptyList();
    }

    /**
     * Gets a set of model elements or named entities. Model can have zero or more user defined elements.
     * <p>
     * An element is the main building block of the semantic model. Data model element defines a named entity
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
     *                 "{&lt;WEATHER&gt;|*} &lt;HISTORY&gt;",
     *                 "&lt;HISTORY&gt; {&lt;OF&gt;|*} &lt;WEATHER&gt;"
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
     * @return Set of built-in tokens, potentially empty, that should be enabled and detected for this model.
     */
    default Set<String> getEnabledBuiltInTokens() {
        return DFLT_ENABLED_BUILTIN_TOKENS;
    }
}
