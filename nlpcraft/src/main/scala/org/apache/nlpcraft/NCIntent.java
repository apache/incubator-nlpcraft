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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotation to bind an intent with the method serving as its callback.
 * 
 * @see NCIntentRef
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
@Target(value={METHOD, TYPE})
@Repeatable(NCIntent.NCIntentList.class)
public @interface NCIntent {
    /**
     * Intent specification using IDL.
     *
     * @return Intent specification using IDL.
     */
    String value() default "";

    /**
     * Grouping annotation required for when more than one {@link NCIntent} annotation is used.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Target(value={METHOD, TYPE})
    @interface NCIntentList {
        /**
         * Gets the list of all {@link NCIntent} annotations attached to the callback or class.
         *
         * @return List of all {@link NCIntent} annotations attached to the callback or class.
         */
        NCIntent[] value();
    }
}
