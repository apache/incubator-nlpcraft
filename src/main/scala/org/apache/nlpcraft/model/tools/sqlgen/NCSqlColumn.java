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

/**
 * Object presentation of SQL column.
 * <p>
 * In JSON/YAML generated model SQL column is represented by the model element (example):
 * <pre class="brush: js">
 *   elements:
 *     - id: "col:orders_customer_id"
 *       groups:
 *       - "column"
 *       synonyms:
 *       - "{customer_id|customer &lt;ID&gt;}"
 *       - "orders {customer_id|customer &lt;ID&gt;}"
 *       - "{customer_id|customer &lt;ID&gt;} &lt;OF&gt; orders"
 *       metadata:
 *         sql:name: "customer_id"
 *         sql:tablename: "orders"
 *         sql:datatype: 12
 *         sql:isnullable: true
 *         sql:ispk: false
 *       description: "Auto-generated from 'orders.customer_id' column."
 *       valueLoader: "org.apache.nlpcraft.examples.sql.db.SqlValueLoader"
 * </pre>
 * Few notes:
 * <ul>
 *     <li>
 *         All model elements representing SQL column have ID in a form of <code>col:sql_table_name</code>.
 *     </li>
 *     <li>
 *         All model elements representing SQL column belong to <code>column</code> group.
 *     </li>
 *     <li>
 *         These model elements have auto-generated synonyms and set of mandatory metadata.
 *     </li>
 *     <li>
 *         User can freely add group membership, change synonyms, add new metadata, add or change value loader.
 *     </li>
 * </ul>
 *
 * @see NCSqlSchemaBuilder#makeSchema(NCModel)
 * @see NCSqlExtractorBuilder#build(NCSqlSchema, NCVariant) 
 * @see NCSqlExtractor#extractColumn(NCToken)
 * @see NCSqlTable#getColumns()
 * @see NCSqlSchema#getAllColumns() 
 */
public interface NCSqlColumn {
    /**
     * Gets name of the table this column belongs to.
     * <p>
     * In JSON/YAML generated model the table name is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:tablename: "customer_id"
     * </pre>
     *
     * @return Name of the table this column belongs to.
     */
    String getTable();

    /**
     * Gets native name of this column.
     * <p>
     * In JSON/YAML generated model the native column name is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:name: "customer_id"
     * </pre>
     *
     * @return Name of this column.
     */
    String getColumn();

    /**
     * Gets JDBC <a target="new" href="https://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">data type</a>
     * for this column.
     * <p>
     * In JSON/YAML generated model the data type is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:datatype: 12
     * </pre>
     *
     * @return JDBC <a target="new" href="https://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">data type</a>
     *      for this column.
     */
    int getDataType();

    /**
     * Tests whether or not this column is a primary key column.
     * <p>
     * In JSON/YAML generated model the primary key flag is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:ispk: false
     * </pre>
     *
     * @return Whether or not this column is a primary key column.
     */
    boolean isPk();

    /**
     * Tests whether or not this column is nullable.
     * <p>
     * In JSON/YAML generated model the nullable flag is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:isnullable: false
     * </pre>
     *
     * @return Whether or not this column is nullable.
     */
    boolean isNullable();
}
