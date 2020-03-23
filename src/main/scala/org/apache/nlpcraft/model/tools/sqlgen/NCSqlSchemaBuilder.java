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

import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.model.*;
import org.apache.nlpcraft.model.tools.sqlgen.impl.*;

/**
 * Builder for {@link NCSqlSchema} instances. Once you have {@link NCSqlSchema}
 * you can also use utility methods from {@link NCSqlExtractor}.
 *
 * @see NCSqlModelGenerator
 * @see NCSqlExtractor
 */
public class NCSqlSchemaBuilder {
    /**
     * Builds object presentation for SQL schema from given data model. Note that the model must be
     * generated by {@link NCSqlModelGenerator} class.
     *
     * @param model Data model to generate object SQL schema presentation. Data model must be
     *      generated by {@link NCSqlModelGenerator} class.
     * @return Object presentation of the SQL schema for a given data model.
     * @throws NCException Thrown in case of any errors.
     * @see NCSqlModelGenerator
     * @see NCSqlExtractor
     */
    public static NCSqlSchema makeSchema(NCModel model) {
        return NCSqlSchemaBuilderImpl.makeSchema(model);
    }
}
