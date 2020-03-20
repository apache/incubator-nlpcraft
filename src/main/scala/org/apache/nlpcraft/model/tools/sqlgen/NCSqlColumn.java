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
 *
 * @see NCSqlSchemaBuilder#makeSchema(NCModel)
 */
public interface NCSqlColumn {
    /**
     * Gets name of the table this column belongs to.
     * <p>
     * In JSON/YAML generated model the table name is declared with the following element
     * metadata (example):
     * <pre class="brush: js">
     *     sql:extratables:
     *     - "other_part_table"
     *     - "another_part_table"
     * </pre>
     *
     * @return Name of the table this column belongs to.
     */
    String getTable();

    /**
     * Gets name of this column.
     *
     * @return Name of this column.
     */
    String getColumn();

    /**
     * Gets JDBC <a target="new" href="https://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">data type</a>
     * for this column.
     *
     * @return JDBC <a target="new" href="https://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">data type</a>
     *      for this column.
     */
    int getDataType();

    /**
     * Tests whether or not this column is a primary key column.
     * 
     * @return Whether or not this column is a primary key column.
     */
    boolean isPk();

    /**
     * Tests whether or not this column is nullable.
     *
     * @return Whether or not this column is nullable.
     */
    boolean isNullable();
}
