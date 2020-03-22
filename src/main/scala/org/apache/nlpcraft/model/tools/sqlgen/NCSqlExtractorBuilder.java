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
import org.apache.nlpcraft.model.tools.sqlgen.impl.*;

/**
 * Builder for {@link NCSqlExtractor} instances.
 *
 * @see NCSqlModelGenerator
 * @see NCSqlExtractor
 */
public class NCSqlExtractorBuilder {
    /**
     * Builds and returns new SQL extractor for given SQL schema and parsing variant.
     * 
     * @param schema SQL schema object to create an extractor for.
     * @param variant Parsing variant (i.e. list of all tokens) to act as a context for
     *      the extraction wherever necessary.
     * @return Newly created SQL extractor.
     */
    public static NCSqlExtractor build(NCSqlSchema schema, NCVariant variant) {
        return new NCSqlExtractorImpl(schema, variant);
    }
}
