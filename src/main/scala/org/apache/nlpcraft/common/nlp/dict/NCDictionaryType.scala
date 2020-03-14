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

package org.apache.nlpcraft.common.nlp.dict

/**
 * Dictionary type.
 *
 * `MOBY` dictionary contains several databases, each of them is set of words.
 * This type defines databases, which are used in the Dictionary service.
 */
object NCDictionaryType extends Enumeration {
    type NCDictionaryType = Value

    // Database which contains the most common names used in the United States and Great Britain.
    val WORD_PROPER_NOUN: Value = Value

    // Database which contains single words, excluding proper names, acronyms, or compound words and phrases.
    val WORD: Value = Value

    // Database which contains common dictionary words.
    val WORD_COMMON: Value = Value

    // Database which contains common acronyms & abbreviations.
    val WORD_ACRONYM: Value = Value

    // Database which contains the 1,000 most frequently used English words.
    val WORD_TOP_1000: Value = Value
}
