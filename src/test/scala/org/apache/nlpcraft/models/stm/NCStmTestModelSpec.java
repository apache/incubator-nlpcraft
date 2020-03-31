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

package org.apache.nlpcraft.models.stm;

import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.model.tools.test.NCTestClient;
import org.apache.nlpcraft.model.tools.test.NCTestClientBuilder;
import org.apache.nlpcraft.model.tools.test.NCTestResult;
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Server and probe with deployed org.apache.nlpcraft.models.stm.NCStmTestModel should be started before.
 */
class NCStmTestModelSpec {
    private NCTestClient cli;
    
    @BeforeEach
    void setUp() throws NCException, IOException {
        NCEmbeddedProbe.start(NCStmTestModel.class);

        cli = new NCTestClientBuilder().newBuilder().build();
        
        cli.open("nlpcraft.stm.test"); // See phone_model.json
    }
    
    @AfterEach
    void tearDown() throws NCException, IOException {
        if (cli != null)
            cli.close();

        NCEmbeddedProbe.stop();
    }
    
    /**
     * @param req
     * @param expResp
     * @throws IOException
     */
    private void check(String req, String expResp) throws IOException {
        NCTestResult res = cli.ask(req);
    
        assertTrue(res.isOk());
        assertTrue(res.getResult().isPresent());
        assertEquals(expResp, res.getResult().get());
    }
    
    /**
     * Checks behaviour. It is based on intents and elements groups.
     *
     * @throws Exception
     */
    @Test
    void test() throws Exception {
        for (int i = 0; i < 3; i++) {
            check("sale", "sale");
            check("best", "sale_best_employee");
            check("buy", "buy");
            check("best", "buy_best_employee");
            check("sale", "sale");
        }
    }
}
