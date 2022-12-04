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
  * Convenient factory for creating [[NCResult]] instances.
  */
object NCResult:
    /**
      * Creates new result instance with given parameters.
      *
      * @param body Result body.
      * @param resultType Result type.
      * @param intentId Intent ID that produced the result.
      */
    def apply(body: Any, resultType: NCResultType, intentId: String): NCResult =
        new NCResult():
            def getBody: Any = body
            def getType: NCResultType = resultType
            def getIntentId: Option[String] = intentId.?

    /**
      * Creates new result instance with given parameters. Note that intent ID will be set to `None`.
      *
      * @param body Result body.
      * @param resultType Result type.
      */
    def apply(body: Any, resultType: NCResultType): NCResult =
        new NCResult():
            def getBody: Any = body
            def getType: NCResultType = resultType
            def getIntentId: Option[String] = None

    /**
      * Creates new result instance with given parameters. Note that intent ID will be set to `None` and
      * result type will be set to [[NCResultType.ASK_RESULT]].
      *
      * @param body Result body.
      */
    def apply(body: Any): NCResult =
        new NCResult() :
            def getBody: Any = body
            def getType: NCResultType = NCResultType.ASK_RESULT
            def getIntentId: Option[String] = None

/**
  * Intent callback result.
  */
trait NCResult:
    /**
      * Body of the result.
      */
    def getBody: Any

    /**
      * Type of the result.
      */
    def getType: NCResultType

    /**
      * Optional ID of the intent, if any, that produced that result.
      */
    def getIntentId: Option[String]