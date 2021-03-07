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

// ============================
// Test intents and predicates.
// ============================

// Re-usable predicate #1.
fragment=p1
    term={1 == 2}

// Intent #1.
intent=i1
    flow=/org.package#method1/ // User-code flow predicate.
    fragment(p1, {'a': true, 'b': {'Москва': [1, 2, 3]}}) /*  Macro-expansion. */
    term~{length("some text") > 0} // Normal term.

// Intent #2.
intent=i2
    flow="a[^0-9]b"
    meta={'a': true, 'b': {'Москва': [1, 2, 3]}}
    term(t1)={2 == 2 && size(id()) != -25}

intent=i3
    flow="a[^0-9]b"
    term(t1)={
        has(
            json("{'a': true, 'b\'2': {'arr': [1, 2, 3]}}"),
            map("موسكو\"", 'v1\'v1', "k2", "v2")
        )
    }