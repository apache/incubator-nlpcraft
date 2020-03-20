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
import java.util.*;

/**
 * Utility method for extracting SQL components from {@link NCToken} tokens.
 */
public class NCSqlExtractors {
    /**
     * Extracts limit object from given parameters.
     * 
     * @param schema SQL schema.
     * @param variant Parsing variant.
     * @param limitTok Limit token with ID <code>nlpcraft:limit</code>.
     * @return SQL limit object extracted from given parsing variant and limit token.
     */
    public static NCSqlLimit extractLimit(NCSqlSchema schema, NCVariant variant, NCToken limitTok) {
        return NCSqlExtractorsImpl.extractLimit(schema, variant, limitTok);
    }

    public static List<NCSqlSimpleCondition> extractDateRangeConditions(NCSqlSchema schema, NCToken colTok, NCToken dateTok) {
        return NCSqlExtractorsImpl.extractDateRangeConditions(schema, colTok, dateTok);
    }

    public static List<NCSqlSimpleCondition> extractNumConditions(NCSqlSchema schema, NCToken colTok, NCToken numTok) {
        return NCSqlExtractorsImpl.extractNumConditions(schema, colTok, numTok);
    }

    public static List<NCSqlInCondition> extractValuesConditions(NCSqlSchema schema, NCToken... allValsToks) {
        return NCSqlExtractorsImpl.extractValuesConditions(schema, allValsToks);
    }

    public static NCSqlSort extractSorts(NCSqlSchema schema, NCVariant variant, NCToken sortTok) {
        return NCSqlExtractorsImpl.extractSorts(schema, variant, sortTok);
    }

    public static NCSqlAggregate extractAggregate(NCSqlSchema schema, NCVariant variant, NCToken aggrFunc, NCToken aggrGroupOpt) {
        return NCSqlExtractorsImpl.extractAggregate(schema, variant, aggrFunc, aggrGroupOpt);
    }

    public static NCSqlTable extractTable(NCSqlSchema schema, NCToken tok) {
        return NCSqlExtractorsImpl.extractTable(schema, tok);
    }

    public static NCSqlColumn extractColumn(NCSqlSchema schema, NCToken tok) {
        return NCSqlExtractorsImpl.extractColumn(schema, tok);
    }

    public static NCSqlDateRange extractDateRange(NCToken tok) {
        return NCSqlExtractorsImpl.extractDateRange(tok);
    }
}
