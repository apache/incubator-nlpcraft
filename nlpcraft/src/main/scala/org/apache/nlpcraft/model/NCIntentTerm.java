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
 * Annotation to mark callback parameter to receive intent term's tokens. This is a companion annotation
 * to {@link NCIntent} and {@link NCIntentRef} annotations and can only be used for
 * the parameters of the methods that are annotated with {@link NCIntent} or {@link NCIntentRef}.
 * {@code NCIntentTerm} takes a term ID as its only mandatory parameter and should be applied to callback
 * method parameters to get the tokens associated with that term (if and when the intent was matched and that
 * callback was invoked).
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCIntent
 * @see NCIntentRef
 * @see NCIntentSample
 * @see NCIntentSkip
 * @see NCIntentMatch
 * @see NCModel#onMatchedIntent(NCIntentMatch)
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=PARAMETER)
public @interface NCIntentTerm {
    String value();
}
