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
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Lifecycle;
import net.minecraft.item.Item;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraft.block.Block;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility for getting data from minecraft. These values are used for preparing synonyms for user defined elements.
 */
public class NCMinecraftFilesDump {
    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static class Json {
        private String version;
        private Map<String, String> data;
    }

    /**
     * @param writer
     * @param s
     * @throws IOException
     */
    private static void write(BufferedWriter writer, String s) throws IOException {
        writer.write(s);
        writer.newLine();
    }

    /**
     * @param type
     * @param reg
     * @param ver
     * @param <T>
     * @throws IOException
     */
    private static <T extends ForgeRegistryEntry<?>> void write(String type, Registry<T> reg, String ver) throws IOException {
        Json js = new Json();

        js.version = ver;

        // Regular name -> registry name.
        js.data =
            reg.stream().filter(x -> x.getRegistryName() != null).
                collect(Collectors.toMap(
                    x -> transformPath(x.getRegistryName().getPath()),
                    x -> x.getRegistryName().toString())
                );
        // Add matching like grass -> grass_block.
        js.data.putAll(reg.stream()
            .filter(x -> x.getRegistryName() != null && x.getRegistryName().getPath().endsWith("_block"))
            .collect(Collectors.toMap(
                x -> transformPath(x.getRegistryName().getPath().replace("_block", "")),
                x -> x.getRegistryName().toString())
            )
        );

        File f = new File(type + ".json");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            write(writer, "/*");
            write(writer, " * Licensed to the Apache Software Foundation (ASF) under one or more");
            write(writer, " * contributor license agreements.  See the NOTICE file distributed with");
            write(writer, " * this work for additional information regarding copyright ownership.");
            write(writer, " * The ASF licenses this file to You under the Apache License, Version 2.0");
            write(writer, " * (the 'License'); you may not use this file except in compliance with");
            write(writer, " * the License.  You may obtain a copy of the License at");
            write(writer, " *");
            write(writer, " *     http://www.apache.org/licenses/LICENSE-2.0");
            write(writer, " *");
            write(writer, " * Unless required by applicable law or agreed to in writing, software");
            write(writer, " * distributed under the License is distributed on an 'AS IS' BASIS,");
            write(writer, " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
            write(writer, " * See the License for the specific language governing permissions and");
            write(writer, " * limitations under the License.");
            write(writer, " *");
            write(writer, " * Auto-generated on: " + new Date());
            write(writer, " * Dump file with minecraft '" + type + "' game objects.");
            write(writer, " * Made for the game version: " + ver);
            write(writer, " */");

            writer.newLine();

            writer.write(GSON.toJson(js));
        }

        System.out.println("File prepared: " + f.getAbsolutePath());
    }

    /**
     * @param path
     * @return
     */
    private static String transformPath(String path) {
        return path.replaceAll("_", " ");
    }

    /**
     * @param type
     * @param <T>
     * @return
     */
    private static <T extends IForgeRegistryEntry<T>> DefaultedRegistry<T> mkRegistry(String type) {
        RegistryKey<Registry<T>> orCreateRootKey = RegistryKey.getOrCreateRootKey(new ResourceLocation(type));

        return GameData.getWrapper(orCreateRootKey, Lifecycle.experimental(), "air");
    }

    /**
     * Application entry point.
     *
     * @param args Command line arguments.
     * @throws IOException If any IO error occurs.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            throw new IllegalArgumentException("2 mandatory parameters should be defined: 'type' and 'version'.");

        String type = args[0];
        String ver = args[1];

        if (!type.equals("block") && !type.equals("item"))
            throw new IllegalArgumentException("Unsupported type, supported are 'block' and 'item'.");

        if (type.equals("block")) {
            DefaultedRegistry<Block> reg = mkRegistry("block");

            write(type, reg, ver);
        }
        else {
            DefaultedRegistry<Item> reg = mkRegistry("item");

            write(type, reg, ver);
        }
    }
}
