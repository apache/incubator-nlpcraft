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

package org.apache.nlpcraft.annotations;

import org.apache.nlpcraft.NCIntentMatch;
import org.apache.nlpcraft.NCIntentSkip;
import org.apache.nlpcraft.NCModel;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The corpus of intent samples that is used for documentaiton and model auto-validation.
 *
 * @see NCIntentSampleRef
 * @see NCIntent
 * @see NCIntentRef
 * @see NCIntentTerm
 * @see NCModelAddClasses
 * @see NCModelAddPackage
 * @see NCIntentSkip
 * @see NCIntentMatch
 * @see NCModel#onMatchedIntent(NCIntentMatch)
 * @see NCTestAutoModelValidator
 */
@Retention(value=RUNTIME)
@Target(value=METHOD)
@Repeatable(NCIntentSample.NCIntentSampleList.class)
public @interface NCIntentSample {
    /**
     * Gets a list of user input samples that should match corresponding intent. This annotation should be
     * attached the intent callback method.
     *
     * @return Set of user input examples that should match corresponding intent.
     */
    String[] value();

    /**
     * Grouping annotation required for when more than one {@link NCIntentSample} annotation is used.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value=METHOD)
    @Documented
    @interface NCIntentSampleList {
        /**
         * Gets the list of all {@link NCIntentSample} annotations attached to the callback.
         *
         * @return List of all {@link NCIntentSample} annotations attached to the callback.
         */
        NCIntentSample[] value();
    }
}
