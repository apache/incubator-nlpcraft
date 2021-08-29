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

import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to define samples of the user input that should match an intent. This
 * annotation should be used together with {@link NCIntent} or {@link NCIntentRef} annotations on the callback
 * methods. Method can have multiple annotations of this type and each annotation can define multiple input
 * examples. See similar {@link NCIntentSampleRef} annotation that allows to load samples from external resources like
 * file or URL.
 * <p>
 * The corpus of intent samples serve several important roles in NLPCraft:
 * <ul>
 *     <li>
 *          It provide code level documentation on what type of user input given intent is supposed to match on.
 *          In many cases having {@link NCIntent} and {@link NCIntentSample} annotations on the intent callback
 *          method allows to see all the main ingredients of the language comprehension in one place.
 *     </li>
 *     <li>
 *         It provides a necessary corpus for automated unit and regression testing used by
 *         {@link NCTestAutoModelValidator} class from
 *         <a href="https://nlpcraft.apache.org/tools/test_framework.html">built-in test framework</a>.
 *         This class auto-validates that provided samples are matched on by their corresponding intents.
 *     </li>
 *     <li>
 *         This corpus is used by various statistical tools like
 *         <a href="https://nlpcraft.apache.org/tools/syn_tool.html">synonyms tool</a> and category value enrichment. Both
 *         of these tools utilize Google's BERT and Facebook fasttext models and require at least minimal corpus of
 *         samples for each intent.
 *     </li>
 * </ul>
 * <p>
 * Here's an example of using this annotation (from <a target=_new href="https://nlpcraft.apache.org/examples/light_switch.html">LightSwitch</a> example):
 * <pre class="brush: java, highlight: [2]">
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
