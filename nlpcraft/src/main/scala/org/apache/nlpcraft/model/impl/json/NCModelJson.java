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

package org.apache.nlpcraft.model.impl.json;

import java.util.*;

import static org.apache.nlpcraft.model.NCModel.*;

/**
 * Parsing bean.
 */
public class NCModelJson {
    private String id;
    private String name;
    private String version;
    private String description;
    private Map<String, Object> metadata;
    private NCMacroJson[] macros;
    private NCElementJson[] elements;
    private String[] additionalStopwords;
    private String[] excludedStopwords;
    private String[] suspiciousWords;
    private String[] enabledBuiltInTokens;
    private String[] intents;
    private String[] parsers;

    private int maxUnknownWords = DFLT_MAX_UNKNOWN_WORDS;
    private int maxFreeWords = DFLT_MAX_FREE_WORDS;
    private int maxSuspiciousWords = DFLT_MAX_SUSPICIOUS_WORDS;
    private int minWords = DFLT_MIN_WORDS;
    private int maxWords = DFLT_MAX_WORDS;
    private int minTokens = DFLT_MIN_TOKENS;
    private int maxTokens = DFLT_MAX_TOKENS;
    private int minNonStopwords = DFLT_MIN_NON_STOPWORDS;
    private boolean isNonEnglishAllowed = DFLT_IS_NON_ENGLISH_ALLOWED;
    private boolean isNotLatinCharsetAllowed = DFLT_IS_NOT_LATIN_CHARSET_ALLOWED;
    private boolean isSwearWordsAllowed = DFLT_IS_SWEAR_WORDS_ALLOWED;
    private boolean isNoNounsAllowed = DFLT_IS_NO_NOUNS_ALLOWED;
    private boolean isNoUserTokensAllowed = DFLT_IS_NO_USER_TOKENS_ALLOWED;
    private int jiggleFactor = DFLT_JIGGLE_FACTOR;
    private boolean isDupSynonymsAllowed = DFLT_IS_DUP_SYNONYMS_ALLOWED;
    private int maxTotalSynonyms = DFLT_MAX_TOTAL_SYNONYMS;
    private boolean isPermutateSynonyms = DFLT_IS_PERMUTATE_SYNONYMS;

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     *
     * @return
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     *
     * @param metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     *
     * @return
     */
    public NCMacroJson[] getMacros() {
        return macros;
    }

    /**
     *
     * @param macros
     */
    public void setMacros(NCMacroJson[] macros) {
        this.macros = macros;
    }

    /**
     *
     * @return
     */
    public NCElementJson[] getElements() {
        return elements;
    }

    /**
     *
     * @param elements
     */
    public void setElements(NCElementJson[] elements) {
        this.elements = elements;
    }

    /**
     *
     * @return
     */
    public String[] getAdditionalStopwords() {
        return additionalStopwords;
    }

    /**
     *
     * @param additionalStopwords
     */
    public void setAdditionalStopwords(String[] additionalStopwords) {
        this.additionalStopwords = additionalStopwords;
    }

    /**
     *
     * @return
     */
    public String[] getExcludedStopwords() {
        return excludedStopwords;
    }

    /**
     *
     * @param excludedStopwords
     */
    public void setExcludedStopwords(String[] excludedStopwords) {
        this.excludedStopwords = excludedStopwords;
    }

    /**
     *
     * @return
     */
    public String[] getSuspiciousWords() {
        return suspiciousWords;
    }

    /**
     * 
     * @param suspiciousWords
     */
    public void setSuspiciousWords(String[] suspiciousWords) {
        this.suspiciousWords = suspiciousWords;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public int getMaxUnknownWords() {
        return maxUnknownWords;
    }

    /**
     *
     * @param maxUnknownWords
     */
    public void setMaxUnknownWords(int maxUnknownWords) {
        this.maxUnknownWords = maxUnknownWords;
    }

    /**
     *
     * @return
     */
    public int getMaxFreeWords() {
        return maxFreeWords;
    }

    /**
     *
     * @param maxFreeWords
     */
    public void setMaxFreeWords(int maxFreeWords) {
        this.maxFreeWords = maxFreeWords;
    }

    /**
     *
     * @return
     */
    public int getMaxSuspiciousWords() {
        return maxSuspiciousWords;
    }

    /**
     *
     * @param maxSuspiciousWords
     */
    public void setMaxSuspiciousWords(int maxSuspiciousWords) {
        this.maxSuspiciousWords = maxSuspiciousWords;
    }

    /**
     *
     * @return
     */
    public int getMinWords() {
        return minWords;
    }

    /**
     *
     * @param minWords
     */
    public void setMinWords(int minWords) {
        this.minWords = minWords;
    }

    /**
     *
     * @return
     */
    public int getMaxWords() {
        return maxWords;
    }

    /**
     *
     * @param maxWords
     */
    public void setMaxWords(int maxWords) {
        this.maxWords = maxWords;
    }

    /**
     *
     * @return
     */
    public int getMinTokens() {
        return minTokens;
    }

    /**
     *
     * @param minTokens
     */
    public void setMinTokens(int minTokens) {
        this.minTokens = minTokens;
    }

    /**
     *
     * @return
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     *
     * @param maxTokens
     */
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     *
     * @return
     */
    public int getMinNonStopwords() {
        return minNonStopwords;
    }

    /**
     *
     * @param minNonStopwords
     */
    public void setMinNonStopwords(int minNonStopwords) {
        this.minNonStopwords = minNonStopwords;
    }

    /**
     *
     * @return
     */
    public boolean isNonEnglishAllowed() {
        return isNonEnglishAllowed;
    }

    /**
     *
     * @param nonEnglishAllowed
     */
    public void setNonEnglishAllowed(boolean nonEnglishAllowed) {
        isNonEnglishAllowed = nonEnglishAllowed;
    }

    /**
     *
     * @return
     */
    public boolean isNotLatinCharsetAllowed() {
        return isNotLatinCharsetAllowed;
    }

    /**
     *
     * @param notLatinCharsetAllowed
     */
    public void setNotLatinCharsetAllowed(boolean notLatinCharsetAllowed) {
        isNotLatinCharsetAllowed = notLatinCharsetAllowed;
    }

    /**
     *
     * @return
     */
    public boolean isSwearWordsAllowed() {
        return isSwearWordsAllowed;
    }

    /**
     *
     * @param swearWordsAllowed
     */
    public void setSwearWordsAllowed(boolean swearWordsAllowed) {
        isSwearWordsAllowed = swearWordsAllowed;
    }

    /**
     *
     * @return
     */
    public boolean isNoNounsAllowed() {
        return isNoNounsAllowed;
    }

    /**
     *
     * @param noNounsAllowed
     */
    public void setNoNounsAllowed(boolean noNounsAllowed) {
        isNoNounsAllowed = noNounsAllowed;
    }

    /**
     *
     * @return
     */
    public boolean isNoUserTokensAllowed() {
        return isNoUserTokensAllowed;
    }

    /**
     *
     * @param noUserTokensAllowed
     */
    public void setNoUserTokensAllowed(boolean noUserTokensAllowed) {
        isNoUserTokensAllowed = noUserTokensAllowed;
    }

    /**
     *
     * @return
     */
    public int getJiggleFactor() {
        return jiggleFactor;
    }

    /**
     *
     * @param jiggleFactor
     */
    public void setJiggleFactor(int jiggleFactor) {
        this.jiggleFactor = jiggleFactor;
    }

    /**
     *
     * @return
     */
    public boolean isDupSynonymsAllowed() {
        return isDupSynonymsAllowed;
    }
    
    /**
     *
     * @param dupSynonymsAllowed
     */
    public void setDupSynonymsAllowed(boolean dupSynonymsAllowed) {
        isDupSynonymsAllowed = dupSynonymsAllowed;
    }
    
    /**
     *
     * @return
     */
    public int getMaxTotalSynonyms() {
        return maxTotalSynonyms;
    }
    
    /**
     *
     * @param maxTotalSynonyms
     */
    public void setMaxTotalSynonyms(int maxTotalSynonyms) {
        this.maxTotalSynonyms = maxTotalSynonyms;
    }

    /**
     * 
     * @param isPermutateSynonyms
     */
    public void setPermutateSynonyms(boolean isPermutateSynonyms) {
        this.isPermutateSynonyms = isPermutateSynonyms;
    }

    /**
     *
     * @return
     */
    public boolean isPermutateSynonyms() {
        return isPermutateSynonyms;
    }
    
    /**
     *
     * @return
     */
    public String[] getEnabledBuiltInTokens() {
        return enabledBuiltInTokens;
    }
    
    /**
     *
     * @param enabledBuiltInTokens Set of enabled built-in token IDs.
     */
    public void setEnabledBuiltInTokens(String[] enabledBuiltInTokens) {
        this.enabledBuiltInTokens = enabledBuiltInTokens;
    }
    
    /**
     *
     * @return
     */
    public String[] getIntents() {
        return intents;
    }
    
    /**
     *
     * @param intents
     */
    public void setIntents(String[] intents) {
        this.intents = intents;
    }

    /**
     *
     * @return
     */
    public String[] getParsers() {
        return parsers;
    }

    /**
     *
     * @param parsers
     */
    public void setParsers(String[] parsers) {
        this.parsers = parsers;
    }
}
