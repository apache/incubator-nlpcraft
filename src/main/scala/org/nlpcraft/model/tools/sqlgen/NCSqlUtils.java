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

package org.nlpcraft.model.tools.sqlgen;

import org.nlpcraft.model.NCToken;
import org.nlpcraft.model.tools.sqlgen.impl.NCSqlUtilsAdapter;

import java.util.List;

/**
 * TODO: add description.
 */
public class NCSqlUtils {
    public static NCSqlLimit extractLimit(NCSqlSchema schema, List<NCToken> variant, NCToken limitTok) {
        return NCSqlUtilsAdapter.extractLimit(schema, variant, limitTok);
    }

    public static List<NCSqlSimpleCondition> extractDateRangeConditions(NCSqlSchema schema, NCToken colTok, NCToken dateTok) {
        return NCSqlUtilsAdapter.extractDateRangeConditions(schema, colTok, dateTok);
    }

    public static List<NCSqlSimpleCondition> extractNumConditions(NCSqlSchema schema, NCToken colTok, NCToken numTok) {
        return NCSqlUtilsAdapter.extractNumConditions(schema, colTok, numTok);
    }

    public static List<NCSqlInCondition> extractValuesConditions(NCSqlSchema schema, NCToken... allValsToks) {
        return NCSqlUtilsAdapter.extractValuesConditions(schema, allValsToks);
    }

    public static NCSqlSort extractSorts(NCSqlSchema schema, List<NCToken> variant, NCToken sortTok) {
        return NCSqlUtilsAdapter.extractSorts(schema, variant, sortTok);
    }

    public static NCSqlAggregate extractAggregate(NCSqlSchema schema, List<NCToken> variant, NCToken aggrFunc, NCToken aggrGroupOpt) {
        return NCSqlUtilsAdapter.extractAggregate(schema, variant, aggrFunc, aggrGroupOpt);
    }

    public static NCSqlTable extractTable(NCSqlSchema schema, NCToken tok) {
        return NCSqlUtilsAdapter.extractTable(schema, tok);
    }

    public static NCSqlColumn extractColumn(NCSqlSchema schema, NCToken tok) {
        return NCSqlUtilsAdapter.extractColumn(schema, tok);
    }

    public static NCSqlDateRange extractDateRange(NCToken tok) {
        return NCSqlUtilsAdapter.extractDateRange(tok);
    }

    public static NCToken findAnyColumnToken(NCToken tok) {
        return NCSqlUtilsAdapter.findAnyColumnToken(tok);
    }
}
