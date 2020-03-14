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

package org.apache.nlpcraft.examples.alarm;

import org.junit.jupiter.api.*;
import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.model.tools.test.NCTestClient;
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Timer model test.
 *
 * @see AlarmModel
 */
class AlarmTest {
    static private NCTestClient client;
    
    @BeforeAll
    static void setUp() throws NCException, IOException {
        client = new NCTestClientBuilder().newBuilder().build();
        
        client.open("nlpcraft.alarm.ex"); // See alarm_model.json
    }
    
    @AfterAll
    static void tearDown() throws NCException, IOException {
        client.close();
    }
    
    @Test
    void test() throws NCException, IOException {
        assertTrue(client.ask("Ping me in 3 minutes").isOk());
        assertTrue(client.ask("Buzz me in an hour and 15mins").isOk());
        assertTrue(client.ask("Set my alarm for 30s").isOk());
    }
}
