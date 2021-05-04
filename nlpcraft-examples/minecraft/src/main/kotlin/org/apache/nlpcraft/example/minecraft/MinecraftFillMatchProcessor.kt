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

import org.apache.nlpcraft.example.minecraft.MinecraftValueLoader.Companion.dumps
import org.apache.nlpcraft.model.*
import java.util.*

/**
 * Special processor for support 'fillIntent' intent processing.
 * Is is designed as separated class to simplify main model class.
 */
class MinecraftFillMatchProcessor {
    internal data class Coordinate(val x: Int = 0, val y: Int = 0, val z: Int = 0) {
        override fun toString(): String {
            return "$x $y $z"
        }

        fun relative(): String {
            return "~$x ~$y ~$z"
        }

        fun relativeRotated(): String {
            return "^$x ^$y ^$z"
        }
    }

    companion object {
        fun process(
            @NCIntentTerm("shape") shape: NCToken,
            @NCIntentTerm("block") blockToken: NCToken,
            @NCIntentTerm("len") length: Optional<NCToken>,
            @NCIntentTerm("position") position: NCToken
        ): NCResult {
            val (from, to) = resultCoordinates(transformLength(length), shape.id)
            val block = dumps["item"]!![blockToken.value]!!
            val player = findPlayer(position)
            val positionCoordinate = positionCoordinate(position)

            return NCResult.text(
                "execute at $player positioned ${positionCoordinate.relative()} rotated 0 0 run " +
                    "fill ${from.relativeRotated()} ${to.relativeRotated()} $block"
            )
        }

        private fun resultCoordinates(length: Int, shape: String): Pair<Coordinate, Coordinate> {
            return when (shape) {
                "line" -> Coordinate(-length / 2) to
                    Coordinate((length - 1) / 2)
                "square" -> Coordinate(-length / 2, 0, -length / 2) to
                    Coordinate((length - 1) / 2, 0, (length - 1) / 2)
                "cube" -> Coordinate(-length / 2, -length / 2, -length / 2) to
                    Coordinate((length - 1) / 2, (length - 1) / 2, (length - 1) / 2)
                else -> throw NCRejection("Unsupported shape")
            }
        }

        private fun positionCoordinate(position: NCToken): Coordinate {
            return when (position.id) {
                "position:player" -> Coordinate()
                "position:front" -> Coordinate(0, 0, transformLength(Optional.of(position), 10))
                else -> throw NCRejection("Unsupported position")
            }
        }

        private fun transformLength(length: Optional<NCToken>, default: Int = 5): Int {
            return length.flatMap { x ->
                x.partTokens.stream()
                    .filter { it.id == "nlpcraft:num" }
                    .findAny()
                    .map { it.meta<Double>("nlpcraft:num:from").toInt() }
            }.orElse(default)
        }

        private fun findPlayer(position: NCToken): String {
            val part = position.partTokens.stream()
                    .filter { it.id == "mc:player" }
                    .findAny()
                    .orElseThrow { AssertionError("Player wasn't found") }

            return if (part.lemma == "i" || part.lemma == "my") "@p" else part.originalText ?: "@p"
        }
    }
}
