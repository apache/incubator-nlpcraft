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

package org.apache.nlpcraft.example.minecraft

import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.example.minecraft.MinecraftValueLoader.Companion.dumps
import org.apache.nlpcraft.model.*
import java.util.*

/**
 * Minecraft example data model.
 * See 'README.md' file for installation, running and testing instructions.
 */
class MinecraftModel : NCModelFileAdapter("minecraft.yaml") {
    /**
     * Weather intent callback.
     */
    @NCIntentRef("weatherIntent")
    @NCIntentSample(
        "make it rain",
        "cast the sun rays",
        "it's rather rainy today",
        "heavy storm is coming",
        "make it drizzle now",
        "start downpour",
        "start the rain",
        "make the raindrops",
        "start the raindrops downpour",
        "make it a wet weather",
        "make it a drizzle weather conditions",
        "make it balmy",
        "set it to sunny climate",
        "make it super stormy",
        "start a hurricane",
        "cast super squall"
    )
    fun onWeatherMatch(@Suppress("UNUSED_PARAMETER") ctx: NCIntentMatch, @NCIntentTerm("arg") tok: NCToken): NCResult {
        return NCResult.text("weather ${tok.id}")
    }

    /**
     * Time intent callback.
     */
    @NCIntentRef("timeIntent")
    @NCIntentSample(
        "set time to evening",
        "now is evening",
        "night",
        "it's midnight"
    )
    fun onTimeMatch(@Suppress("UNUSED_PARAMETER") ctx: NCIntentMatch, @NCIntentTerm("arg") tok: NCToken): NCResult {
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

    /**
     * Give intent callback.
     */
    @NCIntentRef("giveIntent")
    @NCIntentSample(
        "give me iron sword",
        "give me 10 grass blocks",
        "give #PlayerName a jigsaw",
        "give #PlayerName 1 kilogram of feathers",
        "give potion to me"
    )
    fun onGiveMatch(
        @Suppress("UNUSED_PARAMETER") ctx: NCIntentMatch,
        @NCIntentTerm("item") item: NCToken,
        @NCIntentTerm("action") target: NCToken,
        @NCIntentTerm("quantity") quantity: Optional<NCToken>
    ): NCResult {
        val itemRegistry = dumps["item"]!![item.value]!!

        val part = target.partTokens[1]
        val player = if (part.lemma == "i" || part.lemma == "my") "@p" else part.originalText ?: "@p"

        val itemQuantity = if (quantity.isPresent) quantity.get().meta<Double>("nlpcraft:num:from").toInt() else 1

        return NCResult.text("give $player $itemRegistry $itemQuantity")
    }

    /**
     * Fill intent callback.
     */
    @NCIntentRef("fillIntent")
    @NCIntentSample(
        "make a box of sand in front of me",
        "make a cube of gold near me",
        "make a line of grass with length of 2 near me",
        "create a rectangle of dirt in front of #PlayerName",
        "make a box of sand with the size of 2 10 meters in front of me"
    )
    fun onFillMatch(
        @NCIntentTerm("shape") shape: NCToken,
        @NCIntentTerm("block") block: NCToken,
        @NCIntentTerm("len") length: Optional<NCToken>,
        @NCIntentTerm("position") position: NCToken,
    ): NCResult {
        return MinecraftFillMatchProcessor.process(shape, block, length, position)
    }
}
