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
import java.util.*;

/**
 * Utility methods for extracting various SQL components from {@link NCToken} tokens. Instances
 * of this interface are created using {@link NCSqlExtractorBuilder} builder.
 *
 * @see NCSqlExtractorBuilder
 */
public interface NCSqlExtractor {
    /**
     * Extracts limit object from the token.
     *
     * @param limitTok Limit token with ID <code>nlpcraft:limit</code>.
     * @return SQL limit object extracted from given token.
     * @throws NCSqlException Thrown in case of any errors.
     */
    NCSqlLimit extractLimit(NCToken limitTok);

    /**
     * Extracts date range conditions object from given tokens.
     *
     * @param colTok Token representing detected SQL column (i.e. detected model element that belongs
     *      to <code>column</code> group).
     * @param dateTok Date range token with ID <code>nlpcraft:date</code>.
     * @return List of date range conditions extracted from given parameters.
     * @throws NCSqlException Thrown in case of any errors.
     */
    List<NCSqlSimpleCondition> extractDateRangeConditions(NCToken colTok, NCToken dateTok);

    /**
     *
     * @param colTok
     * @param numTok
     * @return
     * @throws NCSqlException Thrown in case of any errors.
     */
    List<NCSqlSimpleCondition> extractNumConditions(NCToken colTok, NCToken numTok);

    /**
     *
     * @param valToks
     * @return
     * @throws NCSqlException Thrown in case of any errors.
     */
    List<NCSqlInCondition> extractInConditions(NCToken... valToks);

    /**
     *
     * @param sortTok
     * @return
     * @throws NCSqlException Thrown in case of any errors.
     */
    NCSqlSort extractSort(NCToken sortTok);

    /**
     * 
     * @param aggrFunTok
     * @param aggrGrpTok
     * @return
     * @throws NCSqlException Thrown in case of any errors.
     */
    NCSqlAggregate extractAggregate(NCToken aggrFunTok, NCToken aggrGrpTok);

    /**
     *
     * @param tblTok
     * @return
     * @throws NCSqlException Thrown in case of any errors.
     */
    NCSqlTable extractTable(NCToken tblTok);

    /**
     *
     * @param colTok
     * @return
     * @throws NCSqlException Thrown in case of any errors.
     */
    NCSqlColumn extractColumn(NCToken colTok);

    /**
     * 
     * @param dateTok
     * @return
     * @throws NCSqlException Thrown in case of any errors.
     */
    NCSqlDateRange extractDateRange(NCToken dateTok);
}
