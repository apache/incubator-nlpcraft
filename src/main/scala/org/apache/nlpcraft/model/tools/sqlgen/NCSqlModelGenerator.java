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

package org.apache.nlpcraft.model.tools.sqlgen;

import org.apache.nlpcraft.model.*;
import org.apache.nlpcraft.model.tools.sqlgen.impl.NCSqlModelGeneratorImpl;

/**
 * Command line utility to generate NLPCraft model stub from given SQL RDBMS.
 * <p>
 * You need to provide JDBC URL and driver, database schema, as well as optional set of tables and columns for which you
 * want to generate NLPCraft model stub. Run this class with <code>--help</code> parameter to get a full documentation:
 * <pre class="brush:plain">
 * java -cp apache-nlpcraft-x.x.x-all-deps.jar org.apache.nlpcraft.model.tools.sqlgen.NCSqlModelGenerator --help
 * </pre>
 * <p>
 * After the model stub is generated:
 * <ul>
 *     <li>
 *         Modify and extend generated model stub to your own needs. In most cases, you'll need
 *         to add, remove or modify auto-generated synonyms, add intents, etc. Note, however, that generated model
 *         is fully complete and can be used as is.
 *     </li>
 *     <li>
 *         Use generated YAML/JSON-based model together with {@link NCModelFileAdapter}
 *         class to instantiate model from this file.
 *     </li>
 *     <li>
 *         Use {@link NCSqlSchemaBuilder#makeSchema(NCModel)} method to get an object representation of the
 *         SQL data schema for the model. You can use this object representation along with many utility
 *         methods in {@link NCSqlUtils} class to efficiently auto-generate SQL queries against the original RDBMS.
 *     </li>
 * </ul>
 */
public class NCSqlModelGenerator {
    /**
     * Runs SQL model generator with given command line parameters.
     * 
     * @param args Command line parameters. Execute with <code>--help</code> parameter to get a full
     *      documentation.
     * @throws NCSqlException Thrown in case of any errors.
     */
    public static void main(String[] args) {
        // Calling out Scala engine.
        NCSqlModelGeneratorImpl.process(args);

        System.exit(0);
    }
}
