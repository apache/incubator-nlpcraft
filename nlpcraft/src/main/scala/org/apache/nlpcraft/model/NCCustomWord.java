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

/**
 * A partially enriched token with a basic set of NLP properties used by custom NER parser.
 *
 * @see NCModel#getParsers()
 * @see NCToken
 */
public interface NCCustomWord {
    /**
     * Gets normalized text for this word.
     * 
     * @return Normalized text.
     */
    String getNormalizedText();

    /**
     * Gets original text for this word.
     * 
     * @return Original text.
     */
    String getOriginalText();

    /**
     * Gets start character index of this word in the original text.
     *
     * @return Start character index of this word.
     */
    int getStartCharIndex();

    /**
     * Gets end character index of this word in the original text.
     * 
     * @return End character index of this word.
     */
    int getEndCharIndex();

    /**
     * Gets Penn Treebank POS tag for this word. Note that additionally to standard Penn Treebank POS
     * tags NLPCraft introduces {@code '---'} synthetic tag to indicate a POS tag for multiword part.
     * Learn more at <a href="http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html">http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html</a>
     *
     * @return Penn Treebank POS tag for this word.
     */
    String getPos();

    /**
     * Gets description of Penn Treebank POS tag. Learn more at <a href="http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html">http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html</a>
     *
     * @return Description of Penn Treebank POS tag.
     */
    String getPosDescription();

    /**
     * Gets the lemma of this word, a canonical form of this word. Note that stemming and lemmatization
     * allow to reduce inflectional forms and sometimes derivationally related forms of a word to a
     * common base form. Lemmatization refers to the use of a vocabulary and morphological analysis
     * of words, normally aiming to remove inflectional endings only and to return the base or dictionary
     * form of a word, which is known as the lemma. Learn
     * more at <a href="https://nlp.stanford.edu/IR-book/html/htmledition/stemming-and-lemmatization-1.html">https://nlp.stanford.edu/IR-book/html/htmledition/stemming-and-lemmatization-1.html</a>     *
     *
     * @return Lemma of this word.
     */
    String getLemma();

    /**
     * Gets the stem of this word. Note that stemming and lemmatization allow to reduce inflectional forms
     * and sometimes derivationally related forms of a word to a common base form. Unlike lemma,
     * stemming is a basic heuristic process that chops off the ends of words in the hope of achieving
     * this goal correctly most of the time, and often includes the removal of derivational affixes.
     * Learn more at <a href="https://nlp.stanford.edu/IR-book/html/htmledition/stemming-and-lemmatization-1.html">https://nlp.stanford.edu/IR-book/html/htmledition/stemming-and-lemmatization-1.html</a>
     *
     * @return Stem of this word.
     */
    String getStem();

    /**
     * Gets whether or not this word is a stopword. Stopwords are some extremely common words which
     * add little value in helping understanding user input and are excluded from the processing
     * entirely. For example, words like {@code a, the, can, of, about, over}, etc. are typical
     * stopwords in English. NLPCraft has built-in set of stopwords. Each model can also
     * provide its own set of included and excluded stopwords.
     *
     * @return Whether or not this word is a stopword.
     */
    boolean isStopWord();

    /**
     * Gets whether or not this word is surrounded by any of {@code '[', ']', '{', '}', '(', ')'} brackets.
     *
     * @return Whether or not this word is surrounded by any of {@code '[', ']', '{', '}', '(', ')'} brackets.
     */
    boolean isBracketed();

    /**
     * Gets whether or not this word is surrounded by single or double quotes.
     *
     * @return Whether or not this word is surrounded by single or double quotes.
     */
    boolean isQuoted();

    /**
     * Tests whether or not this token is found in Princeton WordNet database.
     *
     * @return Princeton WordNet database inclusion flag.
     */
    boolean isKnownWord();

    /**
     * Tests whether or not the given token is a swear word. NLPCraft has built-in list of common English swear words.
     *
     * @return Swear word flag.
     */
    boolean isSwearWord();

    /**
     * Tests whether the given token represents an English word. Note that this only checks that token's text
     * consists of characters of English alphabet, i.e. the text doesn't have to be necessary
     * a known valid English word.
     *
     * @return Whether this token represents an English word.
     */
    boolean isEnglish();
}
