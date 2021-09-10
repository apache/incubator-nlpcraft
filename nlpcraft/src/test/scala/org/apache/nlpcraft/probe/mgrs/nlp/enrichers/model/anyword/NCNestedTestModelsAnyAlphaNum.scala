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
trait NCNestedTestModelAnyAlphaNum extends NCNestedModelAnyAdapter {
    override def anyDefinition: String = "{^^{is_alphanum(tok_txt)}^^}"
}

class NCNestedTestModelAnyAlphaNum1 extends NCNestedTestModelAny1 with NCNestedTestModelAnyAlphaNum
class NCNestedTestModelAnyAlphaNum2 extends NCNestedTestModelAny2 with NCNestedTestModelAnyAlphaNum
class NCNestedTestModelAnyAlphaNum3 extends NCNestedTestModelAny3 with NCNestedTestModelAnyAlphaNum
class NCNestedTestModelAnyAlphaNum4 extends NCNestedTestModelAny4 with NCNestedTestModelAnyAlphaNum
class NCNestedTestModelAnyAlphaNum5 extends NCNestedTestModelAny5 with NCNestedTestModelAnyAlphaNum
class NCNestedTestModelAnyAlphaNum6 extends NCNestedTestModelAny6 with NCNestedTestModelAnyAlphaNum
class NCNestedTestModelAnyAlphaNum7 extends NCNestedTestModelAny7 with NCNestedTestModelAnyAlphaNum