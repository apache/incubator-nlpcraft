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

import org.apache.nlpcraft.example.MinecraftObjectValueLoader.Companion.dumps
import org.apache.nlpcraft.model.*

class FIllMatchProcessor {
    companion object {
        fun process(
            ctx: NCIntentMatch,
            @NCIntentTerm("shape") shape: NCToken,
            @NCIntentTerm("block") blockToken: NCToken,
            @NCIntentTerm("len") length: NCToken,
            @NCIntentTerm("position") position: NCToken
        ): NCResult {
            val (from, to) = resultCoordinates(length = length.toInt(), shape.id)
            val block = dumps["item"]!![blockToken.value]!!

            // TODO: Use user rotation
            // TODO: handle y coordinate for cube
            return NCResult.text("execute at @p positioned ~ ~ ~*1 rotated 0 0 run fill ${from.relativeRotated()} ${to.relativeRotated()} $block")
        }

        private fun resultCoordinates(length: Int, shape: String): Pair<Coordinate, Coordinate> {
            return when (shape) {
                "line" -> Coordinate(-length / 2) to Coordinate((length - 1) / 2)
                "square" -> Coordinate(-length / 2,0, -length / 2) to Coordinate((length - 1) / 2, 0,(length - 1) / 2)
                "cube" -> Coordinate(-length / 2,-length / 2, -length / 2) to Coordinate((length - 1) / 2, (length - 1) / 2,(length - 1) / 2)
                else -> {
                    throw NCRejection("Unsupported shape")
                }
            }
        }
    }
}