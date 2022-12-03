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

import java.time.Duration

/**
  * Model configuration factory.
  */
object NCModelConfig:
    val DFLT_CONV_TIMEOUT: Long = Duration.ofMinutes(60).toMillis
    val DFLT_CONV_DEPTH = 3

    /**
      * Creates model configuration with given parameters.
      *
      * @param id Unique, immutable, global model ID.
      * @param name Human readable model name.
      * @param ver Model version.
      */
    def apply(id: String, name: String, ver: String): NCModelConfig =
        new NCPropertyMapAdapter with NCModelConfig:
            override val getId: String = id
            override val getName: String = name
            override val getVersion: String = ver

    /**
      * Creates model configuration with given parameters.
      *
      * @param id Unique, immutable, global model ID.
      * @param name Human readable model name.
      * @param ver Model version.
      * @param desc Model description.
      * @param orig Model origin.
      */
    def apply(id: String, name: String, ver: String, desc: String, orig: String): NCModelConfig =
        new NCPropertyMapAdapter with NCModelConfig:
            override val getId: String = id
            override val getName: String = name
            override val getVersion: String = ver
            override val getDescription: Option[String] = desc.?
            override val getOrigin: Option[String] = orig.?

    /**
      * Creates model configuration with given parameters.
      *
      * @param id Unique, immutable, global model ID.
      * @param name Human readable model name.
      * @param ver Model version.
      * @param desc Model description.
      * @param orig Model origin.
      * @param convTimeout Conversation timeout in millis.
      * @param convDepth Maximum conversation depth.
      */
    def apply(id: String, name: String, ver: String, desc: String, orig: String, convTimeout: Long, convDepth: Int): NCModelConfig =
        new NCPropertyMapAdapter with NCModelConfig:
            override val getId: String = id
            override val getName: String = name
            override val getVersion: String = ver
            override val getDescription: Option[String] = desc.?
            override val getOrigin: Option[String] = orig.?
            override val getConversationTimeout: Long = convTimeout
            override val getConversationDepth: Int = convDepth

import org.apache.nlpcraft.NCModelConfig.*

/**
  * Model configuration container.
  *
  * @see [[NCModel.getConfig]]
  */
trait NCModelConfig extends NCPropertyMap:
    /**
      * Gets unique, immutable, global model ID.
      */
    def getId: String

    /**
      * Gets readable model name. Note that unlike model ID the model name can be changed.
      */
    def getName: String

    /**
      * Gets model version.
      */
    def getVersion: String

    /**
      * Gets optional model description. Default value is `None`.
      */
    def getDescription: Option[String] = None

    /**
      * Gets optional model origin. Default value is `None`.
      */
    def getOrigin: Option[String] = None

    /**
      * Gets timeout in millis after which the current conversation for a given user will
      * be "forgotten" and its context removed from the STM.
      */
    def getConversationTimeout: Long = DFLT_CONV_TIMEOUT

    /**
      * Gets maximum depth of the conversation (i.e. number of subsequent individual user requests) after
      * which the older requests will be "forgotten" and removed from the conversation context.
      */
    def getConversationDepth: Int = DFLT_CONV_DEPTH
