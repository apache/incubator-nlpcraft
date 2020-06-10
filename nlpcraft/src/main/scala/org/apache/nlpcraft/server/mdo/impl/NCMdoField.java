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

package org.apache.nlpcraft.server.mdo.impl;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks constructor field for automated support for (a) JSON conversion, and (b) SQL CRUD operations.
 * Annotations 'NCMdoEntity' and 'NCMdoField' should be used together.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface NCMdoField {
    /**
     * Whether or not to include into SQL CRUD operations.
     */                                                              
    boolean sql() default true;

    /**
     * Whether or not to include into JSON export.
     */
    boolean json() default true;

    /**
     * Optional function name to generate JSON value for the field.
     *
     * By default the actual field value will be used for JSON export.
     * This converter function can be used to modify this default behavior.
     *
     * Converter function can have zero or one parameter only. If it has one parameter
     * the actual field value will be passed in to convert. Function should return a
     * new value to be used in JSON export.
     */
    String jsonConverter() default "";

    /**
     * SQL column name. This is mandatory if 'table' is specified in 'NCMdoEntity' annotation.
     */
    String column() default "";

    /**
     * Custom JSON field name to use instead of source code parameter name.
     */
    String jsonName() default "";

    /**
     * Custom JDBC type to use instead of default JDBC type mapping.
     */
    int jdbcType() default Integer.MIN_VALUE;

    /**
     * Wether or not this field is a primary key.
     */
    boolean pk() default false;
}
