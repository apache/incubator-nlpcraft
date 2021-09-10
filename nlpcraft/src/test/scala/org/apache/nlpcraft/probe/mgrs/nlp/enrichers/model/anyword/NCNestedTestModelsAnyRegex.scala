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

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model.anyword

import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model.anyword.adapters._

/**
  *
  */
trait NCNestedTestModelAnyRegex extends NCNestedModelAnyAdapter {
    override def anyDefinition: String = "{//[a-zA-Z0-9]+//}"
}

class NCNestedTestModelAnyRegex1 extends NCNestedTestModelAny1 with NCNestedTestModelAnyRegex
class NCNestedTestModelAnyRegex2 extends NCNestedTestModelAny2 with NCNestedTestModelAnyRegex
class NCNestedTestModelAnyRegex3 extends NCNestedTestModelAny3 with NCNestedTestModelAnyRegex
class NCNestedTestModelAnyRegex4 extends NCNestedTestModelAny4 with NCNestedTestModelAnyRegex
class NCNestedTestModelAnyRegex5 extends NCNestedTestModelAny5 with NCNestedTestModelAnyRegex
class NCNestedTestModelAnyRegex6 extends NCNestedTestModelAny6 with NCNestedTestModelAnyRegex
class NCNestedTestModelAnyRegex7 extends NCNestedTestModelAny7 with NCNestedTestModelAnyRegex