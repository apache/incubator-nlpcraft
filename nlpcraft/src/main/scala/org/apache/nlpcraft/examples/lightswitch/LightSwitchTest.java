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

package org.apache.nlpcraft.examples.lightswitch;

import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.model.tools.test.NCTestClient;
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder;
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Light switch model test.
 *
 * @see LightSwitchModel
 */
class LightSwitchTest {
    private NCTestClient cli;

    @BeforeEach
    void setUp() throws NCException, IOException {
        NCEmbeddedProbe.start(LightSwitchModel.class);

        cli = new NCTestClientBuilder().newBuilder().build();

        cli.open("nlpcraft.lightswitch.ex");
    }

    @AfterEach
    void tearDown() throws NCException, IOException {
        if (cli != null)
            cli.close();

        NCEmbeddedProbe.stop();
    }

    @Test
    void test() throws NCException, IOException {
        assertTrue(cli.ask("Turn the lights off in the entire house.").isOk());
        assertTrue(cli.ask("Switch on the illumination in the master bedroom closet.").isOk());
        assertTrue(cli.ask("Get the lights on.").isOk());
        assertTrue(cli.ask("Please, put the light out in the upstairs bedroom.").isOk());
        assertTrue(cli.ask("Set the lights on in the entire house.").isOk());
        assertTrue(cli.ask("Turn the lights off in the guest bedroom.").isOk());
        assertTrue(cli.ask("Could you please switch off all the lights?").isOk());
        assertTrue(cli.ask("Dial off illumination on the 2nd floor.").isOk());
        assertTrue(cli.ask("Please, no lights!").isOk());
        assertTrue(cli.ask("Kill off all the lights now!").isOk());
        assertTrue(cli.ask("No lights in the bedroom, please.").isOk());
    }
}
