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
 */

package org.apache.nlpcraft.examples.weather;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.model.tools.test.NCTestClient;
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder;
import org.apache.nlpcraft.model.tools.test.NCTestResult;
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Weather model test. Note that this example demonstrates the testing of the proper
 * intent selection.
 *
 * @see WeatherModel
 */
class WeatherTest {
    private static final Gson GSON = new Gson();
    private static final Type TYPE_MAP_RESP = new TypeToken<HashMap<String, Object>>() {}.getType();

    private NCTestClient cli;

    /**
     * Checks given intent.
     *
     * @param txt Sentence.
     * @param intentId Intent ID.
     * @param shouldBeSame Equal vs. non-equal intent ID flag.
     */
    private void checkIntent(String txt, String intentId, boolean shouldBeSame) throws NCException, IOException {
        NCTestResult res = cli.ask(txt);

        assertTrue(res.isOk(), () -> res.getResultError().get());

        assert res.getResult().isPresent();

        Map<String, Object> map = GSON.fromJson(res.getResult().get(), TYPE_MAP_RESP);

        if (shouldBeSame)
            assertEquals(intentId, map.get("intentId"));
        else
            assertNotEquals(intentId, map.get("intentId"));
    }

    @BeforeEach
    void setUp() throws NCException, IOException {
        NCEmbeddedProbe.start(WeatherModel.class);

        cli = new NCTestClientBuilder().newBuilder().build();

        cli.open("nlpcraft.weather.ex");  // See weather_model.json
    }

    @AfterEach
    void tearDown() throws NCException, IOException {
        if (cli != null)
            cli.close();

        NCEmbeddedProbe.stop();
    }

    @Test
    void test() throws NCException, IOException {
        // Empty parameter.
        assertTrue(cli.ask("").isFailed());

        // Only latin charset is supported.
        assertTrue(cli.ask("El tiempo en España").isFailed());

        // Should be passed.
        checkIntent("What's the local weather forecast?", "fcast", true);
        checkIntent("What's the weather in Moscow?", "curr", true);
        // Can be answered with conversation.
        checkIntent("Chance of snow?", "curr", true);
        checkIntent("Moscow", "curr", true);

        cli.clearConversation();

        // Cannot be answered without conversation.
        assertTrue(cli.ask("Moscow").isFailed());
    }
}
