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

package org.apache.nlpcraft.model.conversation;

import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.examples.weather.WeatherModel;
import org.apache.nlpcraft.model.tools.test.NCTestClient;
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder;
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @see WeatherModel
 */
class ConversationTest {
    private NCTestClient cli;

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
        assertTrue(cli.ask("What's the weather in Moscow?").isOk());

        // Can be answered with conversation.
        assertTrue(cli.ask("Chance of snow?").isOk());
        assertTrue(cli.ask("Moscow").isOk());

        cli.clearConversation();

        // Cannot be answered without conversation.
        assertTrue(cli.ask("Moscow").isFailed());
    }
}
