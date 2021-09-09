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
trait NCNestedTestModelAnyNotSpace extends NCNestedModelAnyAdapter {
    override def anyDefinition: String = "{^^{tok_txt != ' '}^^}"
}

class NCNestedTestModelAnyNotSpace1 extends NCNestedTestModelAny1 with NCNestedTestModelAnyNotSpace
class NCNestedTestModelAnyNotSpace2 extends NCNestedTestModelAny2 with NCNestedTestModelAnyNotSpace
class NCNestedTestModelAnyNotSpace3 extends NCNestedTestModelAny3 with NCNestedTestModelAnyNotSpace
class NCNestedTestModelAnyNotSpace4 extends NCNestedTestModelAny4 with NCNestedTestModelAnyNotSpace
class NCNestedTestModelAnyNotSpace5 extends NCNestedTestModelAny5 with NCNestedTestModelAnyNotSpace
class NCNestedTestModelAnyNotSpace6 extends NCNestedTestModelAny6 with NCNestedTestModelAnyNotSpace
class NCNestedTestModelAnyNotSpace7 extends NCNestedTestModelAny7 with NCNestedTestModelAnyNotSpace