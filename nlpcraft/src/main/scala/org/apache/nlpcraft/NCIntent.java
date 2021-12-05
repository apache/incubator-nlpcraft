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
 * Annotation to bind an intent with the method serving as its callback. This annotation takes a string value
 * that defines an intent via IDL. This annotation can also be applied to a model's class in
 * which case it will just declare the intent without binding it and the callback method will need to
 * use {@link NCIntentRef} annotation to actually bind it to the declared intent. Note that multiple intents
 * can be bound to the same callback method, but only one callback method can be bound with a given intent.
 * <p>
 * Here's an example of using this annotation (from <a target=_new href="https://nlpcraft.apache.org/examples/light_switch.html">LightSwitch</a> example):
 * <pre class="brush: java, highlight: [1,2]">
 * {@literal @}NCIntent("import('intents.idl')")
 * {@literal @}NCIntent("intent=act term(act)={has(tok_groups, 'act')} term(loc)={# == 'ls:loc'}*")
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
