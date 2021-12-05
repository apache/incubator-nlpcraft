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

package org.apache.nlpcraft;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to add one or more JVM packages that contain classes with intent callbacks. This annotation should be
 * applied to the main model class. When found the internal intent detection algorithm will recursively scan these
 * additional packages and their classes searching for intent callbacks.
 * <p>
 * Additionally with {@link NCModelAddClasses} annotation, these two annotations allowing to have model implementation,
 * i.e. intent callbacks, in external classes not linked through sub-type relationship to the main model class. This
 * approach provides greater modularity, isolated testability and overall coding efficiencies for the larger models
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html#binding">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples">examples</a>.
 *
 * @see NCModelAddClasses
 * @see NCIntentRef
 * @see NCIntentTerm
 * @see NCIntentSample
 * @see NCIntentSampleRef
 * @see NCIntentSkip
 * @see NCIntentMatch
 */
@Retention(value=RUNTIME)
@Target(value=TYPE)
public @interface NCModelAddPackage {
    /**
     * Array of JVM package names to recursively scan for intent callbacks.
     *
     * @return Array of JVM package names to recursively scan for intent callbacks.
     */
    String[] value();
}
