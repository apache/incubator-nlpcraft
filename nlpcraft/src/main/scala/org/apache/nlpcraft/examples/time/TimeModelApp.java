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

package org.apache.nlpcraft.examples.time;

import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe;

import java.util.Collections;

/**
 * An app that demo the usage of embedded probe. This is an alternative way to
 * deploy data models using embedded probe that can run in any host JVM application.
 */
public class TimeModelApp {
    /**
     * Main entry point.
     *
     * @param args Command like arguments (none required).
     * @throws Exception Thrown in case of any errors.
     */
    public static void main(String[] args) throws Exception {
        // Start the data probe "in place" with 'TimeModel' model.
        if (NCEmbeddedProbe.start(null, Collections.singletonList(TimeModel.class.getName())))
            Thread.currentThread().join();
    }
}
