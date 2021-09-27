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

package org.apache.nlpcraft.examples.minecraft

import org.apache.nlpcraft.NCTestContext
import org.apache.nlpcraft.NCTestEnvironment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * JUnit model spec.
 */
@NCTestEnvironment(model = MinecraftModel::class, startClient = true)
class NCMinecraftModelSpec : NCTestContext() {
    @Test
    fun test() {
        val res = client.ask("make a box of sand with the size of 2 10 meters in front of me")
        assertEquals(
            "execute at @p positioned ~0 ~0 ~10 rotated 0 0 run fill ^-1 ^0 ^-1 ^0 ^0 ^0 minecraft:sand",
            res.result.get()
        )
    }
}