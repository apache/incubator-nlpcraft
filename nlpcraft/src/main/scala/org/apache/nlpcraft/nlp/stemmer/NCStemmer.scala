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

package org.apache.nlpcraft.nlp.stemmer

import org.apache.nlpcraft.nlp.parsers.*

/**
  * Trait defining a general stemmer. Stemming is the process of reducing inflected (or sometimes derived)
  * words to their word stem, base or root form—generally a written word form. Note that stemmer is used by
  * several built-in pipeline components.
  *
  * Read more about stemming at [[https://en.wikipedia.org/wiki/Stemming]].
  */
trait NCStemmer:
    /**
      * Gets a stem for a given text. Note that unlike lemma the stemmatization process does not
      * require a context for the given word, i.e. the stemmatization can be performed on individual word.
      *
      * @param word Text to stemmatize.
      */
    def stem(word: String): String
