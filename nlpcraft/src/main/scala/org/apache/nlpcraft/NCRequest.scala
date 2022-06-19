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
  * Descriptor for the input user request.
  *
  * @see NCContext#getRequest() */
trait NCRequest:
    /**
      * Gets ID of the user on behalf of which this request was submitted. User ID is used by
      * NLPCraft to manage the conversation state. It can be any value as long as it is constant
      * and globally unique for the given user.
      *
      * @return User ID. */
    def getUserId: String

    /**
      * Gets globally unique ID of the current request.
      * <p>
      * A request is defined as a processing of a one user input request.
      * Note that the model can be accessed multiple times during processing of a single user request
      * and therefore multiple instances of this interface can return the same request ID. In fact, users
      * of this interfaces can use this fact by using this ID, for example, as a map key for a session
      * scoped storage.
      *
      * @return Request ID. */
    def getRequestId: String

    /**
      * @return */
    def getText: String

    /**
      * Gets UTC/GMT timestamp in millis when user input was received.
      *
      * @return UTC/GMT timestamp in ms when user input was received. */
    def getReceiveTimestamp: Long

    /**
      * Gets optional user request data.
      *
      * @return Optional user request data, can be empty but never {@code null}. */
    def getRequestData: Map[String, Any]
