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

package org.apache.nlpcraft.common.nlp.pos

import scala.collection.immutable.HashMap

/**
 * Penn Treebank POS helper.
 */
object NCPennTreebank {
    // http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
    private final val PENN_TREEBANK = HashMap[String, String] (
        "CC" → "Coordinating conjunction",
        "CD" → "Cardinal number",
        "DT" → "Determiner",
        "EX" → "Existential there",
        "FW" → "Foreign word",
        "IN" → "Preposition or sub. conjunction",
        "JJ" → "Adjective",
        "JJR" → "Adjective, comparative",
        "JJS" → "Adjective, superlative",
        "LS" → "List item marker",
        "MD" → "Modal",
        "NN" → "Noun, singular or mass",
        "NNS" → "Noun, plural",
        "NNP" → "Proper noun, singular",
        "NNPS" → "Proper noun, plural",
        "PDT" → "Predeterminer",
        "POS" → "Possessive ending",
        "PRP" → "Personal pronoun",
        "PRP$" → "Possessive pronoun",
        "RB" → "Adverb",
        "RBR" → "Adverb, comparative",
        "RBS" → "Adverb, superlative",
        "RP" → "Particle",
        "SYM" → "Symbol",
        "TO" → "To",
        "UH" → "Interjection",
        "VB" → "Verb, base form",
        "VBD" → "Verb, past tense",
        "VBG" → "Verb, gerund or present part",
        "VBN" → "Verb, past participle",
        "VBP" → "Verb, non-3rd person sing. present",
        "VBZ" → "Verb, 3rd person sing. present",
        "WDT" → "Wh-determiner",
        "WP" → "Wh-pronoun",
        "WP$" → "Possessive wh-pronoun",
        "WRB" → "Wh-adverb"
    )

    // Synthetic token.
    final val SYNTH_POS = "---"
    final val SYNTH_POS_DESC = "Synthetic tag"

    // Useful POS tags sets.
    final val NOUNS_POS = Seq("NN", "NNS", "NNP", "NNPS")
    final val VERBS_POS = Seq("VB", "VBD", "VBG", "VBN", "VBP", "VBZ")
    final val WHS_POS = Seq("WDT", "WP", "WP$", "WRB")
    final val JJS_POS = Seq("JJ", "JJR", "JJS")

    // Accessors.
    def description(tag: String): Option[String] = if (isSynthetic(tag)) Some(SYNTH_POS_DESC) else PENN_TREEBANK.get(tag)
    def contains(tag: String): Boolean = PENN_TREEBANK.contains(tag)
    def isSynthetic(tag: String): Boolean =  tag == SYNTH_POS
}
