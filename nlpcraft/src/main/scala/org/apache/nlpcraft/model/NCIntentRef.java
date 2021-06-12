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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotations referencing an intent defined outside of callback method declaration. Multiple such annotations
 * can be applied to the callback method. Note that multiple intents can be bound to the same callback method,
 * but only one callback method can be bound with a given intent.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples">examples</a>.
 *
 * @see NCIntent
 * @see NCIntentTerm
 * @see NCIntentSample
 * @see NCIntentSkip
 * @see NCIntentMatch
 * @see NCModel#onMatchedIntent(NCIntentMatch)
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=METHOD)
@Repeatable(NCIntentRef.NCIntentRefList.class)
public @interface NCIntentRef {
    /**
     * ID of the intent defined externally.
     *
     * @return ID of the intent defined externally.
     */
    String value() default "";

    /**
     * Grouping annotation required for when more than one {@link NCIntentRef} annotation is attached to the
     * callback.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value=METHOD)
    @Documented
    @interface NCIntentRefList {
        /**
         * Gets the list of all {@link NCIntentRef} annotations attached to the callback.
         *
         * @return List of all {@link NCIntentRef} annotations attached to the callback.
         */
        NCIntentRef[] value();
    }
}
