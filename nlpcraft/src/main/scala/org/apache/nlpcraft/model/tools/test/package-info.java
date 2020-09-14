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

/**
 * Contains model testing framework.
 * <p>
 * Here's an code snippet of using direct <b>test client</b> for <code>Alarm Clock</code> example illustrating
 * the usage of test framework together with JUnit 5:
 * <pre class="brush: java">
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
 *         // Empty parameter.
 *         assertTrue(cli.ask("").isFailed());
 *
 *         // Only latin charset is supported.
 *         assertTrue(cli.ask("El tiempo en España").isFailed());
 *
 *         // Should be passed.
 *         assertTrue(cli.ask("Ping me in 3 minutes").isOk());
 *         assertTrue(cli.ask("Buzz me in an hour and 15mins").isOk());
 *         assertTrue(cli.ask("Set my alarm for 30s").isOk());
 *     }
 * }
 * </pre>
 * <p>
 * You can also <b>auto-test</b> the same model by using {@link org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator} class without any
 * additional coding utilizing {@link org.apache.nlpcraft.model.NCIntentSample} annotation on the models' callback method. Add necessary classpath to:
 * <pre class="brush: plain">
 *     java -ea -DNLPCRAFT_TEST_MODELS=org.apache.nlpcraft.examples.alarm.AlarmModel org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
 * </pre>
 */
package org.apache.nlpcraft.model.tools.test;