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

import org.apache.nlpcraft.model.tools.test.*;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Annotation to define one or more samples of the user input that should match a corresponding intent. This
 * annotation can only be used together with {@link NCIntent} or {@link NCIntentRef} annotations on the callback
 * methods. Method can have multiple annotations of this type and each annotation can define multiple input
 * examples.
 * <p>
 * Note that the samples provided by this annotation not only serve the documentation purpose but are also
 * used by {@link NCTestAutoModelValidator} class from built-in test framework for auto-validation between
 * data models and intents.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCIntent
 * @see NCIntentRef
 * @see NCIntentTerm
 * @see NCIntentSkip
 * @see NCIntentMatch
 * @see NCModel#onMatchedIntent(NCIntentMatch)
 * @see NCTestAutoModelValidator
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=METHOD)
public @interface NCIntentSample {
    /**
     * Gets a list of user input samples that should match corresponding intent. This annotation should be
     * attached the intent callback method.
     *
     * @return Set of user input examples that should match corresponding intent.
     */
    String[] value();
}
