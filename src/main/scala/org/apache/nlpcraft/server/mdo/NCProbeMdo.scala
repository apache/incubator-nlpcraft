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

/**
  * Probe MDO.
  */
@NCMdoEntity(sql = false)
case class NCProbeMdo(
    @NCMdoField probeToken: String,
    @NCMdoField probeId: String,
    @NCMdoField probeGuid: String,
    @NCMdoField probeApiVersion: String,
    @NCMdoField probeApiDate: java.sql.Date,
    @NCMdoField osVersion: String,
    @NCMdoField osName: String,
    @NCMdoField osArch: String,
    @NCMdoField startTstamp: Timestamp,
    @NCMdoField tmzId: String,
    @NCMdoField tmzAbbr: String,
    @NCMdoField tmzName: String,
    @NCMdoField userName: String,
    @NCMdoField javaVersion: String,
    @NCMdoField javaVendor: String,
    @NCMdoField hostName: String,
    @NCMdoField hostAddr: String,
    @NCMdoField macAddr: String,
    @NCMdoField models: Set[NCProbeModelMdo]
) extends NCAnnotatedMdo[NCProbeMdo] {
    override def hashCode(): Int = probeToken.hashCode() * 37 + probeId.hashCode
    override def equals(obj: scala.Any): Boolean =
        obj match {
            case x: NCProbeMdo ⇒ x.probeId == probeId && x.probeToken == probeToken
            case _ ⇒ false
        }
}
