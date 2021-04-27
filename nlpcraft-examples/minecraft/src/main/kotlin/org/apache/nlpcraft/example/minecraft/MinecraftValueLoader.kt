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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.model.NCElement
import org.apache.nlpcraft.model.NCModelFileAdapter
import org.apache.nlpcraft.model.NCValue
import org.apache.nlpcraft.model.NCValueLoader

/**
 * Data loader from JSON data files.
 * These files which prepared via 'minectaft-mod' module org.apache.nplcraft.example.minecraft.utils.GameFilesDump
 * for this supported `minecraft` server version.
 */
class MinecraftValueLoader : NCValueLoader {
    private data class Dump(val version: String, val data: Map<String, String>)

    private class NCMinecraftValue(private var name: String, private var registry: String) : NCValue {
        override fun getName(): String {
            return name
        }

        override fun getSynonyms(): MutableList<String> {
            return mutableListOf(name)
        }

        override fun toString(): String {
            return registry
        }
    }

    private val mapper = jacksonObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS)

    companion object {
        internal var dumps = mutableMapOf<String, Map<String, String>>()
    }

    override fun load(owner: NCElement?): MutableSet<NCValue> {
        val type = owner!!.metax<String>("mc:type")

        val inputStream =
            NCModelFileAdapter::class.java.classLoader.getResourceAsStream("${type}.json") ?:
            throw NCException("Minecraft object dump not found: ${type}.json")

        val dump =
            try {
                mapper.readValue(inputStream, Dump::class.java)
            }
            catch (e: Exception) {
                throw NCException("Failed to read file: ${type}.json", e)
            }

        dumps[type] = dump.data

        return dump.data.map { x -> NCMinecraftValue(x.key, x.value) }.toMutableSet()
    }
}

