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

import org.apache.nlpcraft.server.mdo.impl._
import org.apache.nlpcraft.server.sql.NCSql.Implicits.RsParser

/**
  * Feedback MDO.
  */
@NCMdoEntity(table = "feedback")
case class NCFeedbackMdo(
    @NCMdoField(column = "id", pk = true) id: Long,
    @NCMdoField(column = "srv_req_id") srvReqId: String,
    @NCMdoField(column = "user_id") userId: Long,
    @NCMdoField(column = "score") score: Double,
    @NCMdoField(column = "comment") comment: Option[String],
    @NCMdoField(column = "created_on") createdOn: Timestamp
) extends NCAnnotatedMdo[NCFeedbackMdo]

object NCFeedbackMdo {
    implicit val x: RsParser[NCFeedbackMdo] =
        NCAnnotatedMdo.mkRsParser(classOf[NCFeedbackMdo])
}
