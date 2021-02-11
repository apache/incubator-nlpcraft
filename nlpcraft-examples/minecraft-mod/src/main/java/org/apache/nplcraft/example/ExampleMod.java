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

package org.apache.nplcraft.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileUtil;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("nlpcraft_mod")
public class ExampleMod {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MODEL_ID = "nlpcraft.minecraft.ex";
    private final Gson gson = new Gson();
    private NCSignIn creds;
    private String baseUrl;
    private MinecraftServer server;
    private Optional<String> token = Optional.empty();
    private boolean inRecursion = false;

    public ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.server = event.getServer();
    }

    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) {
        if (inRecursion) {
            inRecursion = false;
            return;
        }
        String command = event.getParseResults().getReader().getString();
        Vector2f rotation = event.getParseResults().getContext().getSource().getRotation();
        askProbe(command).map(r -> r.state)
                .filter(s -> s.errorCode == null)
                .map(s -> s.resBody)
                .ifPresent(s -> {
                    LOGGER.info("Command {} was converted to {}", command, s);
                    event.setCanceled(true);
                    inRecursion = true;
                    server.getCommandManager().handleCommand(server.getCommandSource(), "/" + s);
                });
    }

    private <T extends ForgeRegistryEntry<?>> void dumpRegistry(DefaultedRegistry<T> registry) {
        Dump dump = new Dump();
        dump.version = server.getMinecraftVersion();
        // regular name -> registry name
        dump.data = registry.stream().filter(x -> x.getRegistryName() != null).collect(Collectors.toMap(
                x -> transformPath(x.getRegistryName().getPath()),
                x -> x.getRegistryName().toString())
        );
        // add matching like grass -> grass_block
        dump.data.putAll(registry.stream()
                .filter(x -> x.getRegistryName() != null && x.getRegistryName().getPath().endsWith("_block"))
                .collect(Collectors.toMap(
                        x -> transformPath(x.getRegistryName().getPath().replace("_block", "")),
                        x -> x.getRegistryName().toString())
                )
        );
        LOGGER.info(gson.toJson(dump));
    }

    private String transformPath(String path) {
        return path.replaceAll("_", " ");
    }

    private Optional<NCResponse> askProbe(String txt) {
        AskParams params = new AskParams();
        params.txt = txt.startsWith("/") ? txt.substring(1) : txt;

        Optional<String> optional = getToken();
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        params.acsTok = optional.get();

        return post("ask/sync", gson.toJson(params), NCResponse.class);
    }

    private Optional<String> getToken() {
        if (!token.isPresent()) {
            loadSettings();

            token = post("signin", gson.toJson(creds), NCSignResponse.class).map(x -> x.acsTok);
        }

        return token;
    }

    private <T> Optional<T> post(String url, String postJson, Class<T> clazz) {
        try {
            String str = baseUrl + url;

            HttpURLConnection http = (HttpURLConnection) new URL(str).openConnection();
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setConnectTimeout(1_000);
            http.setReadTimeout(3_000);

            http.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(http.getOutputStream());
            wr.writeBytes(postJson);
            wr.flush();
            wr.close();

            LOGGER.debug("Command sent to NC server");

            BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));

            T response = gson.fromJson(in, clazz);

            return Optional.of(response);
        } catch (Exception e) {
            LOGGER.error(e);
        }

        return Optional.empty();
    }

    private void loadSettings() {
        creds = new NCSignIn();
        creds.email = "admin@admin.com";
        creds.passwd = "admin";
        String host = "0.0.0.0";
        String port = "8081";

        Path configDir = Paths.get("config");

        Path jsonPath = FileUtil.resolveResourcePath(configDir, "nlpcraft-settings", ".json");

        try {
            Reader reader = Files.newBufferedReader(jsonPath);

            NCSettings settings = gson.fromJson(reader, NCSettings.class);
            creds.email = settings.email;
            creds.passwd = settings.passwd;
            host = settings.host;
            port = settings.port;
        } catch (NoSuchFileException e) {
            LOGGER.info("Credentials were not found");
        } catch (IOException e) {
            LOGGER.error(e);
        }

        baseUrl = "http://" + host + ":" + port + "/api/v1/";
    }

    private class AskParams {
        private final String mdlId = MODEL_ID;
        private String acsTok;
        private String txt;
    }

    private class NCResponse {
        private String status;
        private NCState state;
    }

    private class NCState {
        private Integer errorCode;
        private String error;
        private String status;
        private String resBody;
    }

    private class NCSignIn {
        private String email;
        private String passwd;
    }

    private class NCSignResponse {
        private String acsTok;
    }

    private class NCSettings {
        private String email;
        private String passwd;
        private String host;
        private String port;
    }

    private class Dump {
        private String version;
        private Map<String, String> data;
    }
}
