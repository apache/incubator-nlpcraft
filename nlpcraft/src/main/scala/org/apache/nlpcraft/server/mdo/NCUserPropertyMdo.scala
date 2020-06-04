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
  * User property MDO.
  */
@NCMdoEntity(table = "nc_user_property")
case class NCUserPropertyMdo(
    @NCMdoField(column = "id", pk = true) id: Long,
    @NCMdoField(column = "user_id") userId: Long,
    @NCMdoField(column = "property") property: String,
    @NCMdoField(column = "value") value: String,
    @NCMdoField(column = "created_on") createdOn: Timestamp,
    @NCMdoField(column = "last_modified_on") lastModifiedOn: Timestamp
) extends NCAnnotatedMdo[NCUserPropertyMdo]

object NCUserPropertyMdo {
    implicit val x: RsParser[NCUserPropertyMdo] =
        NCAnnotatedMdo.mkRsParser(classOf[NCUserPropertyMdo])
}