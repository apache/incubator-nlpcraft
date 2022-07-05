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
  * An item of the dialog flow. Dialog flow is a chronologically ordered list of dialog flow items. Each item
  * represents a snapshot of winning intent's match and its associated data. List of dialog flow items
  * is passed into a custom user-defined dialog flow match method.
  *
  * @see NCConversation#getDialogFlow()
  */
trait NCDialogFlowItem:
    /**
      * Gets the intent match container.
      *
      * @return Intent match container.
      */
    def getIntentMatch: NCIntentMatch

    /**
      * Gets the input request descriptor.
      *
      * @return Input request descriptor.
      */
    def getRequest: NCRequest

    /**
      * Gets the winning intent's result.
      *
      * @return Winning intent's result. // TODO: None for debugAsk.
      */
    def getResult: Option[NCResult]
