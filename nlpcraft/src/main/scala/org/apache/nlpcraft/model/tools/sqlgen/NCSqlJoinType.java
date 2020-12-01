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

/**
 * Type of the SQL join.
 * <p>
 * In JSON/YAML generated model SQL join type is declared with the following data model
 * metadata (example):
 * <pre class="brush: js, highlight: [8]">
 *   sql:joins:
 *   - fromtable: "orders"
 *     fromcolumns:
 *     - "customer_id"
 *     totable: "customers"
 *     tocolumns:
 *     - "customer_id"
 *     jointype: "left"
 * </pre>
 *
 * Refer to <a target=_ href="https://en.wikipedia.org/wiki/Join_(SQL)">SQL Join</a>.
 */
public enum NCSqlJoinType {
    /** The INNER JOIN keyword selects records that have matching values in both tables. */
    INNER,

    /** The LEFT JOIN keyword returns all records from the left table, and the matched records from the right table. */
    LEFT,

    /** The RIGHT JOIN keyword returns all records from the right table, and the matched records from the left table. */
    RIGHT,

    /** The FULL OUTER JOIN keyword returns all records when there is a match in left or right table records. */
    OUTER
}
