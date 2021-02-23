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
    @NCIntentSample("make it rain")
    fun onWeatherMatch(ctx: NCIntentMatch, @NCIntentTerm("arg") tok: NCToken): NCResult {
        if (ctx.isAmbiguous) {
            throw NCRejection("Ambiguous request")
        }

        return NCResult.text("weather ${tok.id}")
    }

    @NCIntentRef("timeIntent")
    @NCIntentSample("set time to evening")
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
    @NCIntentSample(
        "give me iron sword",
        "give me 10 grass blocks",
        "give PlayerName a jigsaw"
    )
    fun onGiveMatch(
        ctx: NCIntentMatch,
        @NCIntentTerm("item") item: NCToken,
        @NCIntentTerm("action") target: NCToken,
        @NCIntentTerm("quantity") quantity: Optional<NCToken>
    ): NCResult {
        if (ctx.isAmbiguous) {
            throw NCRejection("Ambiguous request")
        }

        val itemRegistry = dumps["item"]!![item.value]!!
        val player = target.partTokens[1].player()
        val itemQuantity = quantity.map(NCToken::toInt).orElse(1)

        return NCResult.text("give $player $itemRegistry $itemQuantity")
    }

    @NCIntentRef("fillIntent")
    @NCIntentSample(
        "make a box of sand in front of me",
        "make a cube of gold near me",
        "make a line of grass with length of 2 near me",

        "create a rectangle of dirt in front of PlayerName",
        "make a box of sand with the size of 2 10 meters in front of me"
    )
    fun onFillMatch(
        ctx: NCIntentMatch,
        @NCIntentTerm("shape") shape: NCToken,
        @NCIntentTerm("block") block: NCToken,
        @NCIntentTerm("len") length: Optional<NCToken>,
        @NCIntentTerm("position") position: NCToken,
    ): NCResult {
        return FIllMatchProcessor.process(ctx, shape, block, length, position)
    }

    override fun getAbstractTokens(): MutableSet<String> {
        return mutableSetOf("mc:player")
    }
}
