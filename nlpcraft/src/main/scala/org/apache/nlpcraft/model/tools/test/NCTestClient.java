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

package org.apache.nlpcraft.model.tools.test;

import java.io.IOException;

/**
 * Model testing client. This client can be used for convenient unit testing of the models together
 * with any popular unit testing framework such as <a href="http://www.testng.org">TestNG</a> or
 * <a href="https://junit.org">JUnit</a>. The instance of test client should be obtained
 * via {@link NCTestClientBuilder}.
 * <p>
 * Here's an code snippet from <code>Alarm Clock</code> example illustrating
 * the usage of test framework together with JUnit 5:
 * <pre class="brush: java, highlight: [6, 8, 13, 19, 22, 25, 26, 27]">
 * public class AlarmTest {
 *     private NCTestClient cli;
 *
 *     &#64;BeforeEach
 *     void setUp() throws NCException, IOException {
 *         NCEmbeddedProbe.start(AlarmModel.class);
 *
 *         cli = new NCTestClientBuilder().newBuilder().build();
 *
 *         cli.open("nlpcraft.alarm.ex");
 *     }
 *
 *     &#64;AfterEach
 *     void tearDown() throws NCException, IOException {
 *         if (cli != null)
 *             cli.close();
 *
 *         NCEmbeddedProbe.stop();
 *     }
 *
 *     &#64;Test
 *     public void test() throws NCException, IOException {
 *         // Should be passed.
 *         assertTrue(cli.ask("Ping me in 3 minutes").isOk());
 *         assertTrue(cli.ask("Buzz me in an hour and 15mins").isOk());
 *         assertTrue(cli.ask("Set my alarm for 30s").isOk());
 *     }
 * }
 * </pre>
 * 
 * @see NCTestClientBuilder
 */
public interface NCTestClient {
    /**
     * Tests single sentence and returns its result.
     *
     * @param txt Text sentence to test.
     * @return Sentence result.
     * @throws NCTestClientException Thrown if any test system errors occur.
     * @throws IOException Thrown in case of I/O errors.
     */
    NCTestResult ask(String txt) throws NCTestClientException, IOException;
    
    /**
     * Connects test client to the server for testing with given model ID.
     *
     * @param mdlId Model ID to open this client for.
     * @throws NCTestClientException Thrown if any test system errors occur.
     * @throws IOException Thrown in case of I/O errors.
     */
    void open(String mdlId) throws NCTestClientException, IOException;

    /**
     * Closes test client connection to the server.
     *
     * @throws NCTestClientException Thrown if any test system errors occur.
     * @throws IOException Thrown in case of I/O errors.
     */
    void close() throws NCTestClientException, IOException;

    /**
     * Clears conversation for this test client. This method will clear conversation for
     * its configured user.
     *
     * @throws NCTestClientException Thrown if any test system errors occur.
     * @throws IOException Thrown in case of I/O errors.
     */
    void clearConversation() throws NCTestClientException, IOException;
}
