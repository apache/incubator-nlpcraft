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
import java.util.List;

/**
 * Object presentation of SQL join (foreign key).
 * <p>
 * In JSON/YAML generated model SQL joins are declared with the following data model
 * metadata (example):
 * <pre class="brush: js">
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
 * @see NCSqlSchemaBuilder#makeSchema(NCModel)
 * @see NCSqlSchema#getJoins() 
 */
public interface NCSqlJoin {
    /**
     * Gets the name of the originating table.
     *
     * @return Name of the originating table.
     */
    String getFromTable();

    /**
     * Gets the name of the target table.
     *
     * @return Name of the target table.
     */
    String getToTable();

    /**
     * Gets the list of columns in originating table.
     *
     * @return List of columns in originating table.
     */
    List<String> getFromColumns();

    /**
     * Gets the list of columns in target table.
     *
     * @return List of columns in target table.
     */
    List<String> getToColumns();

    /**
     * Gets type of the join.
     *
     * @return Type of join.
     */
    NCSqlJoinType getType();
}
