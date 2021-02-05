package org.apache.nlpcraft.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.model.NCElement
import org.apache.nlpcraft.model.NCModelFileAdapter
import org.apache.nlpcraft.model.NCValue
import org.apache.nlpcraft.model.NCValueLoader

class MinecraftObjectValueLoader : NCValueLoader {
    companion object {
        internal var dumps = mutableMapOf<String, Map<String, String>>()
    }

    override fun load(owner: NCElement?): MutableSet<NCValue> {
        val type = owner!!.metax<String>("minecraft:type")

        val inputStream = NCModelFileAdapter::class.java.classLoader.getResourceAsStream("${type}.json")
            ?: throw NCException("Minecraft object dump not found: ${type}.json")

        val mapper = jacksonObjectMapper()

        val dump = try {
            mapper.readValue(inputStream, Dump::class.java)
        } catch (e: Exception) {
            throw NCException("Failed to read file: ${type}.json", e)
        }

        dumps[type] = dump.data

        return dump.data.map { x -> NCMinecraftValue(x.key, x.value) }.toMutableSet()
    }
}

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

private data class Dump(val version: String, val data: Map<String, String>)
