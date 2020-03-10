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
 * Here's an code snippet from <code>Alarm Clock</code> example illustrating
 * the usage of test framework together with JUnit 5:
 * <pre class="brush: java">
 * public class AlarmTest {
 *     private NCTestClient client;
 *
 *     &#64;BeforeEach
 *     void setUp() throws NCException, IOException {
 *         client = new NCTestClientBuilder().newBuilder().build();
 *
 *         client.open("nlpcraft.alarm.ex");
 *     }
 *
 *     &#64;AfterEach
 *     void tearDown() throws NCException, IOException {
 *         client.close();
 *     }
 *
 *     &#64;Test
 *     public void test() throws NCException, IOException {
 *         // Empty parameter.
 *         assertTrue(client.ask("").isFailed());
 *
 *         // Only latin charset is supported.
 *         assertTrue(client.ask("El tiempo en España").isFailed());
 *
 *         // Should be passed.
 *         assertTrue(client.ask("Ping me in 3 minutes").isOk());
 *         assertTrue(client.ask("Buzz me in an hour and 15mins").isOk());
 *         assertTrue(client.ask("Set my alarm for 30s").isOk());
 *     }
 * }
 * </pre>
 */
package org.nlpcraft.model.tools.test;