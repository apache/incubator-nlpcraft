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
  *
  */
object NCModelConfig:
    val DFLT_CONV_TIMEOUT: Long = Duration.ofMinutes(60).toMillis
    val DFLT_CONV_DEPTH = 3

    def apply(id: String, name: String, ver: String): NCModelConfig =
        new NCModelConfig ():
            override val getId: String = id
            override val getName: String = name
            override val getVersion: String = ver

    def apply(id: String, name: String, ver: String, desc: String, orig: String): NCModelConfig =
        new NCModelConfig() :
            override val getId: String = id
            override val getName: String = name
            override val getVersion: String = ver
            override val getDescription: Option[String] = desc.?
            override val getOrigin: Option[String] = orig.?

    def apply(id: String, name: String, ver: String, desc: String, orig: String, convTimeout: Long, convDepth: Int): NCModelConfig = new NCModelConfig() :
        override val getId: String = id
        override val getName: String = name
        override val getVersion: String = ver
        override val getDescription: Option[String] = desc.?
        override val getOrigin: Option[String] = orig.?
        override val getConversationTimeout: Long = convTimeout
        override val getConversationDepth: Int = convDepth

import org.apache.nlpcraft.NCModelConfig.*

/**
  *
  */
trait NCModelConfig extends NCPropertyMapAdapter:
    def getId: String
    def getName: String
    def getVersion: String
    def getDescription: Option[String] = None
    def getOrigin: Option[String] = None
    def getConversationTimeout: Long = DFLT_CONV_TIMEOUT
    def getConversationDepth: Int = DFLT_CONV_DEPTH
