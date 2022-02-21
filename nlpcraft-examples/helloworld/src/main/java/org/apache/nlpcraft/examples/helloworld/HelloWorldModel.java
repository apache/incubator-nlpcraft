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

import org.apache.nlpcraft.*;
import org.apache.nlpcraft.nlp.entity.parser.nlp.NCNLPEntityParser;
import org.apache.nlpcraft.nlp.token.parser.opennlp.NCOpenNLPTokenParser;

/**
 * Hello World example data model.
 * <p>
 * This trivial example simply responds with 'Hello World!' on any user input.
 * This is the simplest user model that can be defined.
 * <p>
 * See 'README.md' file in the same folder for running and testing instructions.
 */
public class HelloWorldModel extends NCModelAdapter {
    public HelloWorldModel(String tokMdlSrc, String posMdlSrc, String lemmaDicSrc) {
        super(
            new NCModelConfig("nlpcraft.helloworld.ex", "HelloWorld Example Model", "1.0"),
            new NCModelPipelineBuilder(
                new NCOpenNLPTokenParser(tokMdlSrc, posMdlSrc, lemmaDicSrc),
                new NCNLPEntityParser() // TODO: Required at least one parser.
            ).build()
        );
    }

    @Override
    public NCResult onContext(NCContext ctx) {
        NCResult res = new NCResult();

        res.setType(NCResultType.ASK_RESULT);
        res.setBody("Hello World! This model returns the same result for any input...");

        return res;
    }
}