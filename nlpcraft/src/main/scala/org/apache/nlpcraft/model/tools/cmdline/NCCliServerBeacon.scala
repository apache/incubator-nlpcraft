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

package org.apache.nlpcraft.model.tools.cmdline

/**
 *
 * @param pid
 * @param dbUrl
 * @param dbDriver
 * @param dbPoolMin
 * @param dbPoolMax
 * @param dbPoolInit
 * @param dbPoolInc
 * @param dbInit
 * @param restEndpoint
 * @param upLink
 * @param downLink
 * @param startMs
 * @param nlpEngine
 * @param tokenProviders
 * @param extConfigUrl
 * @param beaconPath
 * @param restApi
 * @param extConfigCheckMd5
 * @param acsToksScanMins
 * @param acsToksExpireMins
 * @param logPath
 * @param ph
 */
case class NCCliServerBeacon(
    pid: Long,
    dbUrl: String,
    dbDriver: String,
    dbPoolMin: Int,
    dbPoolMax: Int,
    dbPoolInit: Int,
    dbPoolInc: Int,
    dbInit: Boolean,
    restEndpoint: String,
    upLink: String,
    downLink: String,
    startMs: Long,
    nlpEngine: String,
    tokenProviders: String,
    extConfigUrl: String,
    beaconPath: String,
    restApi: String,
    extConfigCheckMd5: Boolean,
    acsToksScanMins: Int,
    acsToksExpireMins: Int,
    @transient var logPath: String = null,
    @transient var ph: ProcessHandle = null
) {
    lazy val restUrl: String = "http://" + restEndpoint
}

