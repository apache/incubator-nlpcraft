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

package org.apache.nlpcraft.probe.mgrs.nlp.impl

import java.util.Optional

import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.impl._

import scala.collection._

/**
 *
 * @param meta NLP server sentence metadata.
 * @param srvReqId Server request ID.
 */
case class NCRequestImpl(meta: Map[String, Any], srvReqId: String) extends NCRequest {
    override lazy val getServerRequestId: String = srvReqId
    override lazy val getNormalizedText: String = {
        val s: String = meta("NORMTEXT").asInstanceOf[String]
        
        s.toLowerCase
    }
    override lazy val getReceiveTimestamp: Long = meta("RECEIVE_TSTAMP").asInstanceOf[Long] // UTC.
    override lazy val getClientAgent: Optional[String] = getOpt("USER_AGENT")
    override lazy val getRemoteAddress: Optional[String] = getOpt("REMOTE_ADDR")
    override lazy val getData: Optional[String] = getOpt("DATA")
    override lazy val getCompany: NCCompany = new NCCompanyImpl(
        meta("COMPANY_ID").asInstanceOf[Long],
        meta("COMPANY_NAME").asInstanceOf[String],
        getOpt("COMPANY_WEBSITE"),
        getOpt("COMPANY_COUNTRY"),
        getOpt("COMPANY_REGION"),
        getOpt("COMPANY_CITY"),
        getOpt("COMPANY_ADDRESS"),
        getOpt("COMPANY_POSTAL")
    )
    override lazy val getUser: NCUser = new NCUserImpl(
        meta("USER_ID").asInstanceOf[Long],
        getOpt("FIRST_NAME"),
        getOpt("LAST_NAME"),
        getOpt("EMAIL"),
        getOpt("AVATAR_URL"),
        getOpt("USER_PROPS"),
        meta("IS_ADMIN").asInstanceOf[Boolean],
        meta("SIGNUP_TSTAMP").asInstanceOf[Long]
    )
    
    private def getOpt[T](key: String): Optional[T] =
        meta.get(key) match {
            case Some(v) ⇒ Optional.of(v.asInstanceOf[T])
            case None ⇒ Optional.empty()
        }
}
