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

package org.apache.nlpcraft.model.tools.cmdline

/**
 *
 * @param pid
 * @param ver
 * @param relDate
 * @param id
 * @param token
 * @param upLink
 * @param downLink
 * @param jarsFolder
 * @param models
 * @param beaconPath
 * @param startMs
 * @param logPath
 * @param ph
 */
case class NCCliProbeBeacon (
    pid: Long,
    ver: String,
    relDate: String,
    id: String,
    token: String,
    upLink: String,
    downLink: String,
    jarsFolder: String,
    models: String,
    beaconPath: String,
    startMs: Long,
    @transient var logPath: String = null,
    @transient var ph: ProcessHandle = null
)
