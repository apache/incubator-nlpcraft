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

package org.apache.nlpcraft

import org.apache.nlpcraft.*

trait NCMatchInput {
    // Context data.
    def getModelConfig: NCModelConfig
    def getRequest: NCRequest
    def getTokens: List[NCToken]
    def getConversation: NCConversation

    // Variant, passed one by one from sorted list.
    def getVariant4Match: NCVariant

    // Helper methods.
    def getAllEntities: List[NCEntity] = getVariant4Match.getEntities ++ getConversation.getStm
    def hasDialogIdsBefore(idsRegex: String): Boolean = true // TBI

}
