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

package org.apache.nlpcraft.server.nlp.preproc

import io.opencensus.trace.Span
import org.apache.nlpcraft.common.NCService
import org.apache.nlpcraft.server.nlp.spell.NCSpellCheckManager

import scala.collection._

/**
  * Centralized pre-processor for raw text coming from user.
  */
object NCPreProcessManager extends NCService {
    // List of unambiguous contractions.
    private final val CONTRACTIONS: Map[String, Seq[String]] =
        Map[String, String](
            "aren't" → "are not",
            "can't" → "cannot",
            "aren't" → "are not",
            "can't" → "cannot",
            "could've" → "could have",
            "couldn't" → "could not",
            "didn't" → "did not",
            "doesn't" → "does not",
            "don't" → "do not",
            "hadn't" → "had not",
            "hasn't" → "has not",
            "haven't" → "have not",
            "he'll" → "he will",
            "how'd" → "how did",
            "how'll" → "how will",
            "i'll" → "I will",
            "i'm" → "I am",
            "i've" → "I have",
            "isn't" → "is not",
            "it'll" → "it will",
            "let's" → "let us",
            "ma'am" → "madam",
            "might've" → "might have",
            "must've" → "must have",
            "needn't" → "need not",
            "o'clock" → "of the clock",
            "shan't" → "shall not",
            "she'll" → "she will",
            "should've" → "should have",
            "shouldn't" → "should not",
            "they'll" → "they will",
            "they're" → "they are",
            "they've" → "they have",
            "wasn't" → "was not",
            "we'll" → "we will",
            "we're" → "we are",
            "we've" → "we have",
            "weren't" → "were not",
            "what'll" → "what will",
            "what're" → "what are",
            "where'd" → "where did",
            "where've" → "where have",
            "who'll" → "who will",
            "won't" → "will not",
            "would've" → "would have",
            "wouldn't" → "would not",
            "y'all" → "you are all",
            "you'll" → "you will",
            "you're" → "you are",
            "you've" → "you have"
        ).map(p ⇒ p._1 → p._2.split(' ').toSeq)
    
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ ⇒
        super.start()
    }
    
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
      * Replaces contractions. Note that this method can change text case. It forces lower case on replacements.
      *
      * @param sen Input sentence.
      * @return
      */
    private def replaceContractions(sen: Seq[String]): Seq[String] =
        sen.flatMap(s ⇒ {
            CONTRACTIONS.get(s.toLowerCase) match {
                case Some(seq) ⇒ seq
                case None ⇒ Seq(s)
            }
        })
    
    /**
      *
      * @param sen        Input sentence.
      * @param spellCheck Spell check flag.
      * @return
      */
    private def collect(sen: Seq[String], spellCheck: Boolean): String =
        if (spellCheck)
            sen.map(NCSpellCheckManager.check).map(_.trim).filter(!_.isEmpty).mkString(" ")
        else
            sen.map(_.trim).filter(!_.isEmpty).mkString(" ")
    
    /**
      * Performs all pre-processing and normalizes the given input raw text.
      *
      * @param rawTxt     Raw text to normalize.
      * @param spellCheck Using spell checking flag.
      * @return Normalized, pre-processed text.
      * @param parent Optional parent span.
      */
    def normalize(rawTxt: String, spellCheck: Boolean, parent: Span = null): String =
        startScopedSpan("normalize", parent, "txt" → rawTxt, "spellCheck" → spellCheck) { _ ⇒
            // Fix Apple/MacOS smart quotes & dashes.
            val s0 = rawTxt.trim().
                replace('‘', '\'').
                replace('’', '\'').
                replace('”', '"').
                replace('”', '"').
                replace('—', '-')
 
            collect(
                replaceContractions(
                    collect(
                        s0.split(' ').toSeq,
                        spellCheck
                    )
                        .split(' ').toSeq
                
                ),
                spellCheck = false
            )
        }
}
