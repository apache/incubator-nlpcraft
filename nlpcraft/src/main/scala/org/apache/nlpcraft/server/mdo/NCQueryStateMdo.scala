/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.server.mdo

import java.sql.Timestamp

import org.apache.nlpcraft.server.sql.NCSql.Implicits.RsParser
import org.apache.nlpcraft.server.mdo.impl._

/**
  * Query state MDO.
  */
@NCMdoEntity(sql = false)
case class NCQueryStateMdo(
    @NCMdoField srvReqId: String,
    @NCMdoField modelId: String,
    @NCMdoField var probeId: Option[String] = None, // Optional probe ID.
    @NCMdoField userId: Long,
    @NCMdoField companyId: Long,
    @NCMdoField email: Option[String],
    @NCMdoField text: String, // Text of the initial question.
    @NCMdoField userAgent: Option[String],
    @NCMdoField remoteAddress: Option[String],
    @NCMdoField createTstamp: Timestamp, // Creation timestamp.
    @NCMdoField var updateTstamp: Timestamp, // Last update timestamp.
    @NCMdoField var status: String,
    @NCMdoField enabledLog: Boolean,
    @NCMdoField var logJson: Option[String] = None,
    @NCMdoField var intentId: Option[String] = None,
    // Query OK.
    @NCMdoField var resultType: Option[String] = None,
    @NCMdoField var resultBody: Option[String] = None,
    // Query ERROR.
    @NCMdoField var error: Option[String] = None,
    @NCMdoField var errorCode: Option[Int] = None
) extends NCAnnotatedMdo[NCQueryStateMdo]

object NCQueryStateMdo {
    implicit val x: RsParser[NCQueryStateMdo] =
        NCAnnotatedMdo.mkRsParser(classOf[NCQueryStateMdo])
}
