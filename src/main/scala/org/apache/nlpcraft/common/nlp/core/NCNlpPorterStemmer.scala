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

package org.apache.nlpcraft.common.nlp.core

import scala.language.{implicitConversions, postfixOps}

/**
 * Implementation of Porter's stemming algorithm.
 *
 * See http://snowball.tartarus.org/algorithms/porter/stemmer.html
 * for description of the algorithm itself.
 *
 * Implementation is loosely based on work by Evgeny Kotelnikov 'evgeny.kotelnikov@gmail.com'
 */
object NCNlpPorterStemmer  {
    /**
     * Gets a stem for given word. Skips any words less than 3 characters.
     *
     * @param word A word to get stem for.
     */
    def stem(word: String): String = if (word.length <= 2) word.toLowerCase else stem0(word)
    
    /**
     * Gets a stem for given word.
     *
     * @param word A word to get stem for.
     */
    private def stem0(word: String): String = {
        // Deal with plurals and past participles.
        var stem = new Word(word).replace(
            "sses" → "ss",
            "ies" → "i",
            "ss" → "ss",
            "s" → ""
        )
        
        if ((stem matchedBy ((~v ~) + "ed")) || (stem matchedBy ((~v ~) + "ing")))
            stem = stem.
                replace(~v ~)(
                    "ed" → "",
                    "ing" → ""
                ).
                replace(
                    "at" → "ate",
                    "bl" → "ble",
                    "iz" → "ize",
                    (~d and not(~L or ~S or ~Z)) → singleLetter,
                    (m == 1 and ~o) → "e"
                )
        else
            stem = stem.replace(
                ((m > 0) + "eed") → "ee"
            )
        
        stem = stem.
            replace(
                ((~v ~) + "y") → "i"
            ).
            replace(m > 0)(
                "ational" → "ate",
                "tional" → "tion",
                "enci" → "ence",
                "anci" → "ance",
                "izer" → "ize",
                "abli" → "able",
                "alli" → "al",
                "entli" → "ent",
                "eli" → "e",
                "ousli" → "ous",
                "ization" → "ize",
                "ation" → "ate",
                "ator" → "ate",
                "alism" → "al",
                "iveness" → "ive",
                "fulness" → "ful",
                "ousness" → "ous",
                "aliti" → "al",
                "iviti" → "ive",
                "biliti" → "ble"
            ).
            replace(m > 0)(
                "icate" → "ic",
                "ative" → "",
                "alize" → "al",
                "iciti" → "ic",
                "ical" → "ic",
                "ful" → "", "ness" → ""
            ).
            replace(m > 1)(
                "al" → "",
                "ance" → "",
                "ence" → "",
                "er" → "",
                "ic" → "",
                "able" → "",
                "ible" → "",
                "ant" → "",
                "ement" → "",
                "ment" → "",
                "ent" → "",
                ((~S or ~T) + "ion") → "",
                "ou" → "",
                "ism" → "",
                "ate" → "",
                "iti" → "",
                "ous" → "",
                "ive" → "",
                "ize" → ""
            )
        
        // Tide up a little bit.
        stem = stem replace(((m > 1) + "e") → "", ((m == 1 and not(~o)) + "e") → "")
        stem = stem replace ((m > 1 and ~d and ~L) → singleLetter)
        
        stem.toString
    }
    
    // Pattern that is matched against the lemma.
    private case class Pattern(cond: Condition, sfx: String)
    
    // Condition, that is checked against the beginning of the lemma.
    private case class Condition(f: Word ⇒ Boolean) {
        def + = Pattern(this, _: String)
        def unary_~ : Condition = this
        def ~ : Condition = this
        def and(condition: Condition) = Condition(word ⇒ f(word) && condition.f(word))
        def or(condition: Condition) = Condition(word ⇒ f(word) || condition.f(word))
    }
    
    private final val EMPTY_COND = Condition(_ ⇒ true)
    
    private def not: Condition ⇒ Condition = {
        case Condition(f) ⇒ Condition(!f(_))
    }
    
    private val S = Condition(_ endsWith "s")
    private val Z = Condition(_ endsWith "z")
    private val L = Condition(_ endsWith "l")
    private val T = Condition(_ endsWith "t")
    private val d = Condition(_.endsWithCC)
    private val o = Condition(_.endsWithCVC)
    private val v = Condition(_.containsVowels)
    
    private object m {
        def >(measure: Int) = Condition(_.measure > measure)
        def ==(measure: Int) = Condition(_.measure == measure)
    }
    
    private case class StemBuilder(build: Word ⇒ Word)
    
    private def suffixStemBuilder(sfx: String) = StemBuilder(_ + sfx)
    
    private val singleLetter = StemBuilder(_ trimSuffix 1)
    
    private class Word(s: String) {
        private val w = s.toLowerCase
        
        def trimSuffix(sfxLen: Int) = new Word(w substring(0, w.length - sfxLen))
        
        def endsWith: String ⇒ Boolean = w endsWith
        
        def +(sfx: String) = new Word(w + sfx)
        
        def satisfies: Condition ⇒ Boolean = (_: Condition).f(this)
        
        def hasConsonantAt(pos: Int): Boolean =
            (w.indices contains pos) && (w(pos) match {
                case 'a' | 'e' | 'i' | 'o' | 'u' ⇒ false
                case 'y' if hasConsonantAt(pos + 1) ⇒ false
                case _ ⇒ true
            })
        
        def hasVowelAt: Int ⇒ Boolean = !hasConsonantAt(_: Int)
        
        def containsVowels: Boolean = w.indices exists hasVowelAt
        
        def endsWithCC: Boolean =
            (w.length > 1) &&
                (w(w.length - 1) == w(w.length - 2)) &&
                hasConsonantAt(w.length - 1)
        
        def endsWithCVC: Boolean =
            (w.length > 2) &&
                hasConsonantAt(w.length - 1) &&
                hasVowelAt(w.length - 2) &&
                hasConsonantAt(w.length - 3) &&
                !(Set('w', 'x', 'y') contains w(w.length - 2))
        
        
        def measure: Int = w.indices.count(pos ⇒ hasVowelAt(pos) && hasConsonantAt(pos + 1))
        
        def matchedBy: Pattern ⇒ Boolean = {
            case Pattern(cond, sfx) ⇒ endsWith(sfx) && (trimSuffix(sfx.length) satisfies cond)
        }
        
        def replace(replaces: (Pattern, StemBuilder)*): Word = {
            for ((ptrn, builder) ← replaces if matchedBy(ptrn))
                return builder build trimSuffix(ptrn.sfx.length)
            
            this
        }
        
        def replace(cmnCond: Condition)(replaces: (Pattern, StemBuilder)*): Word =
            replace(replaces map {
                case (Pattern(cond, sfx), builder) ⇒ (Pattern(cmnCond and cond, sfx), builder)
            }: _*)
        
        override def toString: String = w
    }
    
    // Implicits.
    private implicit def c1[P, SB](r: (P, SB))(implicit ev1: P ⇒ Pattern, ev2: SB ⇒ StemBuilder): (Pattern, StemBuilder) = (r._1, r._2)
    private implicit def c2: String ⇒ Pattern = Pattern(EMPTY_COND, _)
    private implicit def c3: Condition ⇒ Pattern = Pattern(_, "")
    private implicit def c4: String ⇒ StemBuilder = suffixStemBuilder
}