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

package org.apache.nlpcraft.common

/**
  * Rejection error codes.
  */
object NCErrorCodes {
    /** Rejection by the data model. */
    final val MODEL_REJECTION = 1
    /** Unexpected system error. */
    final val UNEXPECTED_ERROR = 100
    /** Model's result is too big. */
    final val RESULT_TOO_BIG = 101
    /** Recoverable system error. */
    final val SYSTEM_ERROR = 102
    /** Too many unknown words. */
    final val MAX_UNKNOWN_WORDS = 10001
    /** Sentence is too complex. */
    final val MAX_FREE_WORDS = 10002
    /** Too many suspicious or unrelated words. */
    final val MAX_SUSPICIOUS_WORDS = 10003
    /** Swear words are not allowed. */
    final val ALLOW_SWEAR_WORDS = 10004
    /** Sentence contains no nouns. */
    final val ALLOW_NO_NOUNS = 10005
    /** Only latin charset is supported. */
    final val ALLOW_NON_LATIN_CHARSET = 10006
    /** Only english language is supported. */
    final val ALLOW_NON_ENGLISH = 10007
    /** Sentence seems unrelated to data model. */
    final val ALLOW_NO_USER_TOKENS = 10008
    /** Sentence is too short. */
    final val MIN_WORDS = 10009
    /** Sentence is ambiguous. */
    final val MIN_NON_STOPWORDS = 10010
    /** Sentence is too short. */
    final val MIN_TOKENS = 10011
    /** Sentence is too long. */
    final val MAX_TOKENS = 10012
}
