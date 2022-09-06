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
  * A descriptor of the intent callback returned by [[NCModelClient.debugAsk()]] method. This descriptor
  * defines the callback for the intent that was detected as a winning intent but whose callback wasn't
  * fired as per [[NCModelClient.debugAsk()]] method's semantic.
  *
  * Using this descriptor the user can execute callback itself, if necessary.
  *
  * @see [[NCModelClient.debugAsk()]]
  */
trait NCMatchedCallback:
    /**
      * Gets ID of the matched intent.
      */
    def getIntentId: String

    /**
      * Gets the list of callback arguments as list of list of entities.
      *
      * @see [[getCallback]]
      */
    def getCallbackArguments: List[List[NCEntity]]

    /**
      * Gets the callback function that takes list of list of entities and returns
      * an instance of [[NCResult]] class.
      *
      * @see [[getCallbackArguments]]
      */
    def getCallback: List[List[NCEntity]] => NCResult
