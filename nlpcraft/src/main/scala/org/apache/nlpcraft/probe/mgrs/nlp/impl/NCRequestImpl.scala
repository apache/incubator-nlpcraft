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

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.impl._

import java.util.{Collections, Optional}
import scala.collection.JavaConverters._
import scala.collection._
import scala.compat.java8.OptionConverters._

/**
 *
 * @param nlpMeta NLP server sentence metadata.
 * @param srvReqId Server request ID.
 */
case class NCRequestImpl(nlpMeta: Map[String, Any], srvReqId: String) extends NCMetadataAdapter with NCRequest {
    override lazy val getServerRequestId: String = srvReqId
    override lazy val getNormalizedText: String = {
        val s: String = nlpMeta("NORMTEXT").asInstanceOf[String]

        s.toLowerCase
    }
    override lazy val getReceiveTimestamp: Long = nlpMeta("RECEIVE_TSTAMP").asInstanceOf[Long] // UTC.
    override lazy val getClientAgent: Optional[String] = getOpt("USER_AGENT")
    override lazy val getRemoteAddress: Optional[String] = getOpt("REMOTE_ADDR")
    override lazy val getRequestData: java.util.Map[String, Object] = getOpt[String]("DATA").asScala match {
        case Some(json) => U.jsonToJavaMap(json)
        case None => Map.empty[String, Object].asJava
    }
    override lazy val getCompany: NCCompany = new NCCompanyImpl(
        nlpMeta("COMPANY_ID").asInstanceOf[Long],
        nlpMeta("COMPANY_NAME").asInstanceOf[String],
        getOpt("COMPANY_WEBSITE"),
        getOpt("COMPANY_COUNTRY"),
        getOpt("COMPANY_REGION"),
        getOpt("COMPANY_CITY"),
        getOpt("COMPANY_ADDRESS"),
        getOpt("COMPANY_POSTAL"),
        getOpt("COMPANY_META").orElse(Collections.emptyMap())
    )
    override lazy val getUser: NCUser = new NCUserImpl(
        nlpMeta("USER_ID").asInstanceOf[Long],
        getOpt("FIRST_NAME"),
        getOpt("LAST_NAME"),
        getOpt("EMAIL"),
        getOpt("AVATAR_URL"),
        getOpt("META").orElse(Collections.emptyMap()),
        nlpMeta("IS_ADMIN").asInstanceOf[Boolean],
        nlpMeta("SIGNUP_TSTAMP").asInstanceOf[Long]
    )

    private def getOpt[T](key: String): Optional[T] =
        nlpMeta.get(key) match {
            case Some(v) => Optional.of(v.asInstanceOf[T])
            case None => Optional.empty()
        }
}
