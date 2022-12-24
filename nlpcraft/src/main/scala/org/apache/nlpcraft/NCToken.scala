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

package org.apache.nlpcraft

/**
  * Represents a contiguous substring of the original input text produced by [[NCTokenParser]].
  * A token is the result of [[NCTokenParser tokenization]] - the process of demarcating and classifying
  * sections of a string of input characters. See [[NCPipeline]] for documentation on the tokens place in the
  * overall processing pipeline.
  *
  * Note that both [[NCToken]] and [[NCEntity]] interfaces extend [[NCPropertyMap]] interface
  * that allows them to store custom metadata properties. Parser, enrichers and validators for tokens
  * and entities use this capability to store and check their properties in tokens and entities.
  *
  * @see [[NCEntity]]
  * @see [[NCTokenParser]]
  * @see [[NCTokenEnricher]]
  * @see [[NCTokenValidator]]
  * @see [[NCEntityParser]]
  * @see [[NCEntityEnricher]]
  * @see [[NCEntityValidator]]
  * @see [[NCEntityMapper]]
  */
trait NCToken extends NCPropertyMap:
    /**
      * Gets the text of this token.
      *
      * @return Text of this token.
      */
    def getText: String

    /**
      * Gets the index of this token in the list returned by [[NCTokenParser.tokenize()]] method.
      *
      * @return Zero-based index of this token in the list returned by [[NCTokenParser.tokenize()]] method
      */
    def getIndex: Int

    /**
      * Gets the inclusive start position of this token's text in the original input text supplied to
      * [[NCTokenParser.tokenize()]] method.
      *
      * @return Start position (inclusive) of this token's text in the original input text.
      */
    def getStartCharIndex: Int

    /**
      * Gets the inclusive end position of this token's text in the original input text supplied to
      * [[NCTokenParser.tokenize()]] method.
      *
      * @return End position (inclusive) of this token's text in the original input text.
      */
    def getEndCharIndex: Int
