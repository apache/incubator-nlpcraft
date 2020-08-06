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

package org.apache.nlpcraft.model;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotation to define an intent on the method serving as its callback. This annotation takes a string value
 * that defines an intent via intent DSL.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCIntentRef
 * @see NCIntentTerm
 * @see NCIntentSample
 * @see NCIntentSkip
 * @see NCIntentMatch
 * @see NCModel#onMatchedIntent(NCIntentMatch) 
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=METHOD)
public @interface NCIntent {
    /**
     * Intent specification using intent DSL.
     *
     * @return Intent specification using intent DSL.
     */
    String value() default "";
}
