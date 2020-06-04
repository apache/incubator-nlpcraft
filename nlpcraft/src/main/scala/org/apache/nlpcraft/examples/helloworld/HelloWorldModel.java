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

package org.apache.nlpcraft.examples.helloworld;

import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.model.NCContext;
import org.apache.nlpcraft.model.NCModelAdapter;
import org.apache.nlpcraft.model.NCResult;

/**
 * Hello World example data model.
 * <p>
 * This trivial example simply responds with 'Hello World!' on any user input.
 * This is the simplest user model that can be defined.
 * <p>
 * See 'README.md' file in the same folder for running instructions.
 * 
 * @see HelloWorldTest
 */
public class HelloWorldModel extends NCModelAdapter {
    /**
     * Initializes model.
     *
     * @throws NCException If any errors occur.
     */
    public HelloWorldModel() throws NCException {
        super("nlpcraft.helloworld.ex", "HelloWorld Example Model", "1.0");
    }

    @Override
    public NCResult onContext(NCContext ctx) {
        return NCResult.html(
            "Hello World! This model returns the same result for any input..."
        );
    }
}

