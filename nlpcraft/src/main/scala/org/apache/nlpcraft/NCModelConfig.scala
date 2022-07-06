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

    def apply(id: String, name: String, ver: String) = new NCModelConfig(id, name, ver)
    def apply(id: String, name: String, ver: String, desc: String, orig: String) =
        new NCModelConfig(id, name, ver, Option(desc), Option(orig))
import org.apache.nlpcraft.NCModelConfig.*

/**
  *
  * @param id
  * @param name
  * @param version
  * @param description
  * @param origin
  * @param conversationTimeout
  * @param conversationDepth
  */
case class NCModelConfig(
    id: String,
    name: String,
    version: String,
    description: Option[String] = None,
    origin: Option[String] = None,
    conversationTimeout: Long = DFLT_CONV_TIMEOUT,
    conversationDepth: Int = DFLT_CONV_DEPTH
) extends NCPropertyMapAdapter