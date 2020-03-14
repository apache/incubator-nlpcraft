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

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.server.sql.NCSql.Implicits.RsParser
import org.apache.nlpcraft.server.mdo.impl._

/**
  * User MDO.
  */
@NCMdoEntity(table = "nc_user")
case class NCUserMdo(
    @NCMdoField(column = "id", pk = true) id: Long,
    @NCMdoField(column = "company_id") companyId: Long,
    @NCMdoField(column = "ext_id") extId: Option[String],
    @NCMdoField(column = "email") email: Option[String],
    @NCMdoField(column = "first_name") firstName: Option[String],
    @NCMdoField(column = "last_name") lastName: Option[String],
    @NCMdoField(column = "avatar_url") avatarUrl: Option[String],
    @NCMdoField(column = "passwd_salt") passwordSalt: Option[String],
    @NCMdoField(column = "is_admin") isAdmin: Boolean,
    @NCMdoField(column = "created_on") createdOn: Timestamp,
    @NCMdoField(column = "last_modified_on") lastModifiedOn: Timestamp
) extends NCAnnotatedMdo[NCUserMdo]

object NCUserMdo {
    implicit val x: RsParser[NCUserMdo] =
        NCAnnotatedMdo.mkRsParser(classOf[NCUserMdo])

    def apply(
        id: Long,
        companyId: Long,
        extId: Option[String],
        email: Option[String],
        firstName: Option[String],
        lastName: Option[String],
        avatarUrl: Option[String],
        passwordSalt: Option[String],
        isAdmin: Boolean
    ): NCUserMdo = {
        val now = U.nowUtcTs()

        NCUserMdo(id, companyId, extId, email, firstName, lastName, avatarUrl, passwordSalt, isAdmin, now, now)
    }

    def apply(
        id: Long,
        companyId: Long,
        extId: Option[String],
        email: Option[String],
        firstName: Option[String],
        lastName: Option[String],
        avatarUrl: Option[String],
        passwordSalt: Option[String],
        isAdmin: Boolean,
        createdOn: Timestamp
    ): NCUserMdo = {
        require(createdOn != null, "Created date cannot be null.")

        NCUserMdo(id, companyId, extId, email, firstName, lastName, avatarUrl, passwordSalt, isAdmin, createdOn, createdOn)
    }
}
