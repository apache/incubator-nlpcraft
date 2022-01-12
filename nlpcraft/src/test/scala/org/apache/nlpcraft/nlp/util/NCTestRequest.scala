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

/**
  * Request test implementation.
  *
  * @param txt
  * @param userId
  * @param reqId
  * @param ts
  * @param data
  */
case class NCTestRequest(
    txt: String,
    userId: String = null,
    reqId: String = null,
    ts: Long = -1,
    data: JMap[String, AnyRef] = null
) extends NCRequest:
    override def getUserId: String = userId
    override def getRequestId: String = reqId
    override def getText: String = txt
    override def getReceiveTimestamp: Long = ts
    override def getRequestData: JMap[String, AnyRef] = data

/**
  * Java side helper.
  */
object NCTestRequest:
    /**
      *
      * @param txt
      * @return
      */
    def apply(txt: String): NCTestRequest = new NCTestRequest(txt)