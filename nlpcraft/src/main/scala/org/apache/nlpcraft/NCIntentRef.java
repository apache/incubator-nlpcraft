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

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation referencing an intent defined outside of callback method declaration.
 * 
 * @see NCIntent
 * @see NCIntentTerm
 * @see NCIntentSample
 * @see NCIntentSampleRef
 * @see NCModelAddClasses
 * @see NCModelAddPackage
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
     * Grouping annotation required for when more than one {@link NCIntentRef} annotation is used.
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
