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
import java.util.Optional;

/**
 * Object presentation of SQL table.
 * 
 * @see NCSqlSchemaBuilder#makeSchema(NCModel) 
 */
public interface NCSqlTable {
    /**
     * Gets table name.
     * <p>
     * In JSON/YAML generated model table name is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *      sql:name: "orders"
     * </pre>
     * Note also that all elements declaring SQL tables belong to <code>table</code> group.
     * @return table name.
     */
    String getTable();

    /**
     * Gets collections of this table columns.
     *
     * @return Collections of this table columns.
     */
    List<NCSqlColumn> getColumns();

    /**
     * Gets default sort descriptor.
     * <p>
     * In JSON/YAML generated model the default sort list is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:defaultsort:
     *     - "order_id#desc"
     * </pre>
     * Note the <code>table#{asc|desc}</code> notation for identifying column name and sort order.
     *
     * @return Default sort descriptor.
     */
    List<NCSqlSort> getDefaultSort();

    /**
     * Gets the list of the column names for the default select set.
     * <p>
     * In JSON/YAML generated model the default select list is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:defaultselect:
     *     - "order_id"
     *     - "order_date"
     *     - "required_date"
     * </pre>
     *
     * @return List of the column names for the default select set.
     */
    List<String> getDefaultSelect();

    /**
     * Gets the list of extra tables this table is referencing. Extra tables are joined together with this table
     * for default selection. Often, a single domain dataset if spread over multiple tables and this
     * allows to have a meaningful default selection.
     * <p>
     * In JSON/YAML generated model the extra tables list is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:extratables:
     *     - "other_table"
     *     - "another_table"
     * </pre>
     *
     * @return List of extra tables this table is referencing.
     */
    List<String> getExtraTables();

    /**
     * Gets a column that defines a default date for this table. Note that this column
     * can belong to another table.
     * <p>
     * In JSON/YAML generated model the default date column is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:defaultdate: "orders.order_date"
     * </pre>
     * Note <code>table.column</code> notation for the table and column names.
     *
     * @return Column that defines a default date for this table
     */
    Optional<NCSqlColumn> getDefaultDate();
}
