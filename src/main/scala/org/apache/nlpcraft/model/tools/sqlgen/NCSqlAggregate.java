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
 * Object presentation of SQL aggregate. You can get an instance of this interface by
 * using {@link NCSqlExtractor#extractAggregate(NCToken, NCToken)} method.
 *
 * @see NCSqlSchemaBuilder#makeSchema(NCModel)
 * @see NCSqlExtractorBuilder#build(NCSqlSchema, NCVariant)
 * @see NCSqlExtractor#extractAggregate(NCToken, NCToken)
 */
public interface NCSqlAggregate {
    /**
     * Gets potentially empty list of SQL functions for this SQL aggregate.
     *
     * @return List of SQL functions for this aggregate. Can be empty.
     */
    List<NCSqlFunction> getSelect();

    /**
     * Gets potentially empty list of group-by columns for this SQL aggregate.
     *
     * @return List of group-by columns. Can be empty.
     */
    List<NCSqlColumn> getGroupBy();
}
