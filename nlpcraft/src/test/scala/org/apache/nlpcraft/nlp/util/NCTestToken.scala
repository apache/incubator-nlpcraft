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

package org.apache.nlpcraft.nlp.util

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.util.NCTestPipeline.*

import java.util
import java.util.Map as JMap
import scala.jdk.CollectionConverters.*

/**
  * Request test implementation.
  *
  * @param txt
  * @param userId
  * @param reqId
  * @param ts
  * @param data
  */
case class NCTestToken(
    txt: String = "<text:undefined>",
    idx: Int = -1,
    start: Int = -1,
    end: Int = -1,
    lemma: String = null,
    pos: String = null,
    data: JMap[String, AnyRef] = null
) extends NCPropertyMapAdapter with NCToken:
    if data != null then data.asScala.foreach { (k, v) => put(k, v)}

    override def getText: String = txt
    override def getIndex: Int = idx
    override def getStartCharIndex: Int = start
    override def getEndCharIndex: Int = end
    override def getLemma: String = if lemma  != null then lemma else txt
    override def getPos: String = if pos  != null then pos else "undefined"

