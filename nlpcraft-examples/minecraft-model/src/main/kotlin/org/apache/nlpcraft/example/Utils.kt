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

import org.apache.nlpcraft.model.NCToken

private var firstPersonWords = setOf("me", "my", "i")

internal fun NCToken.normText(): String {
    return this.meta("nlpcraft:nlp:normtext")
}

internal fun NCToken.toInt(): Int {
    return this.meta<Double>("nlpcraft:num:from").toInt()
}

internal fun player(target: NCToken): String {
    return if (firstPersonWords.contains(target.normText())) "@p" else target.originalText ?: "@p"
}

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