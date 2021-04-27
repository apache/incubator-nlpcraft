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

package org.apache.nplcraft.example.minecraft.utils;

import com.google.gson.Gson;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Utility for getting data from minecraft. These values are used for preparing synonyms for user defined elements.
 */
public class NCMinecraftFilesDump {
    private final static Gson GSON = new Gson();

    private static class Dump {
        private String version;
        private Map<String, String> data;
    }

    private static <T extends ForgeRegistryEntry<?>> void dumpRegistry(DefaultedRegistry<T> registry, String version) {
        Dump dump = new Dump();

        dump.version = version;

        // Regular name -> registry name.
        dump.data =
            registry.stream().filter(x -> x.getRegistryName() != null).
                collect(Collectors.toMap(
                    x -> transformPath(x.getRegistryName().getPath()),
                    x -> x.getRegistryName().toString())
                );
        // Add matching like grass -> grass_block.
        dump.data.putAll(registry.stream()
            .filter(x -> x.getRegistryName() != null && x.getRegistryName().getPath().endsWith("_block"))
            .collect(Collectors.toMap(
                x -> transformPath(x.getRegistryName().getPath().replace("_block", "")),
                x -> x.getRegistryName().toString())
            )
        );

        System.out.println(GSON.toJson(dump));
    }

    private static String transformPath(String path) {
        return path.replaceAll("_", " ");
    }

    /**
     * App entry point.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        String type = args[0];
        String version = args[1];

        if (type.equals("block")) {
            dumpRegistry(Registry.BLOCK, version);
        }
        else if (type.equals("item")) {
            dumpRegistry(Registry.ITEM, version);
        }
        else {
            System.err.println("Unknown type");
        }
    }
}
