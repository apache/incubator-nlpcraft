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

package org.apache.nlpcraft.internal.nlp.util

import org.apache.nlpcraft.*

/**
  * Token test implementation.
  *
  * @param txt
  * @param lemma
  * @param stem
  * @param pos
  * @param isStop
  * @param start
  * @param end
  */
case class NCTestToken(
    txt: String,
    lemma: String = null,
    stem: String = null,
    pos: String = null,
    isStop: Boolean = false,
    start: Int = -1,
    end: Int = -1
) extends NCPropertyMapAdapter with NCToken:
    override def getText: String = txt
    override def getLemma: String = lemma
    override def getStem: String = stem
    override def getPos: String = pos
    override def isStopWord: Boolean = isStop
    override def getStartCharIndex: Int = start
    override def getEndCharIndex: Int = end
    override def getLength: Int = end - start + 1