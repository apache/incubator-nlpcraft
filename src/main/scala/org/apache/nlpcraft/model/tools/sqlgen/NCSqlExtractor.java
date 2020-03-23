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
import org.apache.nlpcraft.common.NCException;
import java.util.*;

/**
 * Utility methods for extracting various SQL components from {@link NCToken} tokens. Instances
 * of this interface are created using {@link NCSqlExtractorBuilder} builder.
 * <p>
 * Note that {@link NCSqlExtractorBuilder} builder requires {@link NCSqlSchema} and {@link NCVariant}
 * objects when creating an instance of SQL extractor. Many methods in this interface will search
 * that parsing variant and schema to find necessary referenced tokens.
 * <p>
 * Note also that wherever necessary the implementation will scan part (constituent) tokens as well
 * (see {@link NCToken#findPartTokens(String...)} for more information).
 *
 * @see NCSqlExtractorBuilder
 */
public interface NCSqlExtractor {
    /**
     * Extracts limit object from the token.
     *
     * @param limitTok Limit token with ID <code>nlpcraft:limit</code>.
     * @return SQL limit object extracted from given token.
     * @throws NCException Thrown in case of any errors.
     */
    NCSqlLimit extractLimit(NCToken limitTok);

    /**
     * Extracts date range conditions object from given tokens. Result list will have multiple conditions that
     * all will have to be AND-combined to make <code>colTok</code> to be within a date range
     * defined by <code>dateTok</code> parameter.
     *
     * @param colTok Token representing detected SQL column (i.e. detected model element that belongs
     *      to <code>column</code> group).
     * @param dateTok Date range token with ID <code>nlpcraft:date</code>.
     * @return List of conditions extracted from given parameters that have to be AND-combined.
     * @throws NCException Thrown in case of any errors.
     */
    List<NCSqlSimpleCondition> extractDateRangeConditions(NCToken colTok, NCToken dateTok);

    /**
     * Extracts numeric conditions object from given tokens. Result list will have multiple conditions that
     * all will have to be AND-combined to make <code>colTok</code> act as an operand to the numeric
     * condition defined by <code>numTok</code> parameter.
     *
     * @param colTok Token representing detected SQL column (i.e. detected model element that belongs
     *      to <code>column</code> group).
     * @param numTok Numeric token with ID <code>nlpcraft:num</code>. This token provides numeric value
     *      as well as type of logical condition on that numeric value.
     * @return List of conditions extracted from given parameters that have to be AND-combined.
     * @throws NCException Thrown in case of any errors.
     */
    List<NCSqlSimpleCondition> extractNumConditions(NCToken colTok, NCToken numTok);

    /**
     *
     * @param valToks Zero or more tokens representing values for the SQL IN condition. Note that only
     *      those token that have non-{@code null} values (see {@link NCToken#getValue()} method) will
     *      be used in the result conditions.
     * @return List of conditions extracted from given parameters that have to be AND-combined.
     * @throws NCException Thrown in case of any errors.
     */
    List<NCSqlInCondition> extractInConditions(NCToken... valToks);

    /**
     *
     * @param sortTok
     * @return
     * @throws NCException Thrown in case of any errors.
     */
    NCSqlSort extractSort(NCToken sortTok);

    /**
     *
     * @param tblTok
     * @return
     * @throws NCException Thrown in case of any errors.
     */
    NCSqlTable extractTable(NCToken tblTok);

    /**
     *
     * @param colTok
     * @return
     * @throws NCException Thrown in case of any errors.
     */
    NCSqlColumn extractColumn(NCToken colTok);

    /**
     * 
     * @param dateTok
     * @return
     * @throws NCException Thrown in case of any errors.
     */
    NCSqlDateRange extractDateRange(NCToken dateTok);
}
