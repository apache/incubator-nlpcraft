/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.internal.util

import org.junit.jupiter.api.Test

/**
  *
  */
class NCResourceSpec:
    @Test
    def test(): Unit =
        // Get and delete.
        var f = NCResourceReader.get("opennlp/en-lemmatizer.dict")
        require(f.delete())
        println(s"Deleted: ${f.getAbsolutePath}")

        // Download.
        f = NCResourceReader.get("opennlp/en-lemmatizer.dict")
        // From cache.
        f = NCResourceReader.get("opennlp/en-lemmatizer.dict")
        // By absolute path.
        f = NCResourceReader.get(f.getAbsolutePath)

        require(f.exists())