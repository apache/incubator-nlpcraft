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
 *
 */

package org.apache.nlpcraft.example

import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.example.MinecraftObjectValueLoader.Companion.dumps
import org.apache.nlpcraft.model.*
import java.util.*

@Suppress("unused")
class MinecraftModel : NCModelFileAdapter("minecraft.yaml") {
    @NCIntentRef("weatherIntent")
    fun onWeatherMatch(ctx: NCIntentMatch, @NCIntentTerm("arg") tok: NCToken): NCResult {
        if (ctx.isAmbiguous) {
            throw NCRejection("Ambiguous request")
        }

        return NCResult.text("weather ${tok.id}")
    }

    @NCIntentRef("timeIntent")
    fun onTimeMatch(ctx: NCIntentMatch, @NCIntentTerm("arg") tok: NCToken): NCResult {
        if (ctx.isAmbiguous) {
            throw NCRejection("Ambiguous request")
        }

        val time: Int = when (tok.id) {
            "morning" -> 23000
            "day" -> 1000
            "afternoon" -> 6000
            "evening" -> 12000
            "night" -> 12000
            "midnight" -> 18000
            else -> null
        } ?: throw NCException("Invalid token id")

        return NCResult.text("time set $time")
    }

    @NCIntentRef("giveIntent")
    fun onGiveMatch(
        ctx: NCIntentMatch,
        @NCIntentTerm("item") item: NCToken,
        @NCIntentTerm("target") target: NCToken,
        @NCIntentTerm("quantity") quantity: Optional<NCToken>
    ): NCResult {
        if (ctx.isAmbiguous) {
            throw NCRejection("Ambiguous request")
        }

        val itemRegistry = dumps["item"]!![item.value]!!
        val player = if (target.normText() == "me") "@p" else target.originalText ?: "@p"

        val itemQuantity = quantity
            .flatMap { x -> x.metaOpt<Double>("nlpcraft:num:from") }
            .map { x -> x.toLong() }
            .orElse(1)

        return NCResult.text("give $player $itemRegistry $itemQuantity")
    }

    @NCIntentRef("fillIntent")
    fun onFillMatch(
        ctx: NCIntentMatch,
        @NCIntentTerm("shape") shape: NCToken,
        @NCIntentTerm("block") block: NCToken,
        @NCIntentTerm("len") length: NCToken,
        @NCIntentTerm("position") position: NCToken,
    ): NCResult {
        TODO()
    }

    private fun NCToken.normText(): String {
        return this.meta("nlpcraft:nlp:normtext")
    }
}
