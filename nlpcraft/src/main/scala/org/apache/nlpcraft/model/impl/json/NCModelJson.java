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
    private String[] additionalStopWords;
    private String[] excludedStopWords;
    private String[] suspiciousWords;
    private String[] enabledBuiltInTokens;
    private String[] abstractTokens;
    private String[] intents;
    private String[] parsers;
    private Map<String, String[]> restrictedCombinations;

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
    private boolean isDupSynonymsAllowed = DFLT_IS_DUP_SYNONYMS_ALLOWED;
    private int maxTotalSynonyms = DFLT_MAX_TOTAL_SYNONYMS;
    private boolean isPermutateSynonyms = DFLT_IS_PERMUTATE_SYNONYMS;
    private boolean isSparse = DFLT_IS_SPARSE;
    private int maxElementSynonyms = DFLT_MAX_TOTAL_SYNONYMS;
    private boolean maxSynonymsThresholdError = DFLT_MAX_SYNONYMS_THRESHOLD_ERROR;
    private long conversationTimeout = DFLT_CONV_TIMEOUT_MS;
    private int conversationDepth = DFLT_CONV_DEPTH;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    public NCMacroJson[] getMacros() {
        return macros;
    }
    public void setMacros(NCMacroJson[] macros) {
        this.macros = macros;
    }
    public NCElementJson[] getElements() {
        return elements;
    }
    public void setElements(NCElementJson[] elements) {
        this.elements = elements;
    }
    public String[] getAdditionalStopWords() {
        return additionalStopWords;
    }
    public void setAdditionalStopWords(String[] additionalStopWords) {
        this.additionalStopWords = additionalStopWords;
    }
    public String[] getExcludedStopWords() {
        return excludedStopWords;
    }
    public void setExcludedStopWords(String[] excludedStopWords) {
        this.excludedStopWords = excludedStopWords;
    }
    public String[] getSuspiciousWords() {
        return suspiciousWords;
    }
    public void setSuspiciousWords(String[] suspiciousWords) {
        this.suspiciousWords = suspiciousWords;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getMaxUnknownWords() {
        return maxUnknownWords;
    }
    public void setMaxUnknownWords(int maxUnknownWords) {
        this.maxUnknownWords = maxUnknownWords;
    }
    public int getMaxFreeWords() {
        return maxFreeWords;
    }
    public void setMaxFreeWords(int maxFreeWords) {
        this.maxFreeWords = maxFreeWords;
    }
    public int getMaxSuspiciousWords() {
        return maxSuspiciousWords;
    }
    public void setMaxSuspiciousWords(int maxSuspiciousWords) {
        this.maxSuspiciousWords = maxSuspiciousWords;
    }
    public int getMinWords() {
        return minWords;
    }
    public void setMinWords(int minWords) {
        this.minWords = minWords;
    }
    public int getMaxWords() {
        return maxWords;
    }
    public void setMaxWords(int maxWords) {
        this.maxWords = maxWords;
    }
    public int getMinTokens() {
        return minTokens;
    }
    public void setMinTokens(int minTokens) {
        this.minTokens = minTokens;
    }
    public int getMaxTokens() {
        return maxTokens;
    }
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    public int getMinNonStopwords() {
        return minNonStopwords;
    }
    public void setMinNonStopwords(int minNonStopwords) {
        this.minNonStopwords = minNonStopwords;
    }
    public boolean isNonEnglishAllowed() {
        return isNonEnglishAllowed;
    }
    public void setNonEnglishAllowed(boolean nonEnglishAllowed) {
        isNonEnglishAllowed = nonEnglishAllowed;
    }
    public boolean isNotLatinCharsetAllowed() {
        return isNotLatinCharsetAllowed;
    }
    public void setNotLatinCharsetAllowed(boolean notLatinCharsetAllowed) { isNotLatinCharsetAllowed = notLatinCharsetAllowed; }
    public boolean isSwearWordsAllowed() {
        return isSwearWordsAllowed;
    }
    public void setSwearWordsAllowed(boolean swearWordsAllowed) {
        isSwearWordsAllowed = swearWordsAllowed;
    }
    public boolean isNoNounsAllowed() {
        return isNoNounsAllowed;
    }
    public void setNoNounsAllowed(boolean noNounsAllowed) {
        isNoNounsAllowed = noNounsAllowed;
    }
    public boolean isNoUserTokensAllowed() {
        return isNoUserTokensAllowed;
    }
    public void setNoUserTokensAllowed(boolean noUserTokensAllowed) {
        isNoUserTokensAllowed = noUserTokensAllowed;
    }
    public boolean isSparse() {
        return isSparse;
    }
    public void setSparse(boolean sparse) {
        isSparse = sparse;
    }
    public boolean isDupSynonymsAllowed() {
        return isDupSynonymsAllowed;
    }
    public void setDupSynonymsAllowed(boolean dupSynonymsAllowed) {
        isDupSynonymsAllowed = dupSynonymsAllowed;
    }
    public int getMaxTotalSynonyms() {
        return maxTotalSynonyms;
    }
    public void setMaxTotalSynonyms(int maxTotalSynonyms) {
        this.maxTotalSynonyms = maxTotalSynonyms;
    }
    public void setPermutateSynonyms(boolean isPermutateSynonyms) {
        this.isPermutateSynonyms = isPermutateSynonyms;
    }
    public boolean isPermutateSynonyms() {
        return isPermutateSynonyms;
    }
    public String[] getEnabledBuiltInTokens() {
        return enabledBuiltInTokens;
    }
    public void setEnabledBuiltInTokens(String[] enabledBuiltInTokens) { this.enabledBuiltInTokens = enabledBuiltInTokens; }
    public String[] getAbstractTokens() {
        return abstractTokens;
    }
    public void setAbstractTokens(String[] abstractTokens) {
        this.abstractTokens = abstractTokens;
    }
    public String[] getIntents() {
        return intents;
    }
    public void setIntents(String[] intents) {
        this.intents = intents;
    }
    public String[] getParsers() {
        return parsers;
    }
    public void setParsers(String[] parsers) {
        this.parsers = parsers;
    }
    public int getMaxElementSynonyms() {
        return maxElementSynonyms;
    }
    public void setMaxElementSynonyms(int maxElementSynonyms) {
        this.maxElementSynonyms = maxElementSynonyms;
    }
    public boolean isMaxSynonymsThresholdError() {
        return maxSynonymsThresholdError;
    }
    public void setMaxSynonymsThresholdError(boolean maxSynonymsThresholdError) { this.maxSynonymsThresholdError = maxSynonymsThresholdError; }
    public long getConversationTimeout() {
        return conversationTimeout;
    }
    public void setConversationTimeout(long conversationTimeout) {
        this.conversationTimeout = conversationTimeout;
    }
    public int getConversationDepth() {
        return conversationDepth;
    }
    public void setConversationDepth(int conversationDepth) {
        this.conversationDepth = conversationDepth;
    }
    public Map<String, String[]> getRestrictedCombinations() {
        return restrictedCombinations;
    }
    public void setRestrictedCombinations(Map<String, String[]> restrictedCombinations) { this.restrictedCombinations = restrictedCombinations;}
}
