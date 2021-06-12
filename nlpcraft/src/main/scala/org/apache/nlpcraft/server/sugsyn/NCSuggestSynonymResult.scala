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

package org.apache.nlpcraft.server.sugsyn

import java.util.{List => JList}

/**
 * Result of the model synonym suggestion tool.
 *
 * @param modelId ID of the model suggestion tool was running on.
 * @param minScore Min score input parameter.
 * @param durationMs Duration of the tool run.
 * @param timestamp Timestamp of the tool run start.
 * @param error Error message, or `null` if no errors occurred.
 * @param suggestions List of synonym suggestion.
 * @param warnings Warnings.
 */
case class NCSuggestSynonymResult(
    modelId: String,
    minScore: Double,
    durationMs: Long,
    timestamp: Long,
    error: String,
    suggestions: JList[AnyRef],
    warnings: JList[String]
)
