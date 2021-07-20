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
 * Note that if multiple intents bound to the same callback method, all such intents should have the named
 * terms specified by this annotation.
 * <p>
 * Here's an example of using this annotation (from <a target=_new href="https://nlpcraft.apache.org/examples/light_switch.html">LightSwitch</a> example):
 * <pre class="brush: java, highlight: [10,11]">
 * {@literal @}NCIntent("import('intents.idl')")
 * {@literal @}NCIntent("intent=act term(act)={has(tok_groups(), 'act')} term(loc)={trim(tok_id()) == 'ls:loc'}*")
 * {@literal @}NCIntentSample(Array(
 *     "Turn the lights off in the entire house.",
 *     "Switch on the illumination in the master bedroom closet.",
 *     "Get the lights on.",
 *     "Please, put the light out in the upstairs bedroom."
 * ))
 * def onMatch(
 *     {@literal @}NCIntentTerm("act") actTok: NCToken,
 *     {@literal @}NCIntentTerm("loc") locToks: List[NCToken]
 * ): NCResult = {
 *     ...
 * }
 * </pre>
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html#binding">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples">examples</a>.
 *
 * @see NCIntent
 * @see NCIntentRef
 * @see NCIntentSample
 * @see NCIntentSampleRef
 * @see NCIntentSkip
 * @see NCIntentMatch
 * @see NCModel#onMatchedIntent(NCIntentMatch)
 */
@Documented
@Retention(value=RUNTIME)
@Target(value=PARAMETER)
public @interface NCIntentTerm {
    /**
     * ID of the intent term.
     *
     * @return ID of the intent term.
     */
    String value();
}
