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
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

@Mod("nlpcraft_mod")
public class ExampleMod {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Gson gson = new Gson();
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
        LOGGER.debug("Processing command:" + command);
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
            // TODO
            NCSignIn sign = new NCSignIn();
            sign.email = "admin@admin.com";
            sign.passwd = "admin";

            token = post("signin", gson.toJson(sign), NCSignResponse.class).map(x -> x.acsTok);
        }

        return token;
    }

    private <T> Optional<T> post(String url, String postJson, Class<T> clazz) {
        try {
            String str = "http://0.0.0.0:8081/api/v1/" + url;

            HttpURLConnection http = (HttpURLConnection) new URL(str).openConnection();
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setConnectTimeout(1_000);
            http.setReadTimeout(1_000);

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

    private class AskParams {
        private final String mdlId = "nlpcraft.minecraft.ex";
        private String acsTok;
        private String txt;

        @Override
        public String toString() {
            return "AskParams{" +
                    "acsTok='" + acsTok + '\'' +
                    ", mdlId='" + mdlId + '\'' +
                    ", txt='" + txt + '\'' +
                    '}';
        }
    }

    private class NCResponse {
        private String status;
        private NCState state;

        @Override
        public String toString() {
            return "NCResponse{" +
                    "status='" + status + '\'' +
                    ", state=" + state +
                    '}';
        }
    }

    private class NCState {
        private Integer errorCode;
        private String error;
        private String status;
        private String resBody;

        @Override
        public String toString() {
            return "NCState{" +
                    "errorCode=" + errorCode +
                    ", error='" + error + '\'' +
                    ", status='" + status + '\'' +
                    ", resBody='" + resBody + '\'' +
                    '}';
        }
    }

    private class NCSignIn {
        String email;
        String passwd;
    }

    private class NCSignResponse {
        private String acsTok;
    }
}
