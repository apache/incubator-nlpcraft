/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft;

/**
 * User data model
 * <p>
 * Data model is a holder for user-define NLP processing logic that provides an interface to your data sources like a
 * database or a SaaS application. NLPCraft employs model-as-a-code approach where entire data model is an
 * implementation of this interface which can be developed using any JVM programming language like Java, Scala,
 * Kotlin, or Groovy. The instance of this interface is passed to {@link NCModelClient} class and contains:
 * <ul>
 *     <li>Model {@link #getConfig() configurfation}.</li>
 *     <li>Model {@link #getPipeline() processing pipeline}.</li>
 *     <li>Life-cycle callbacks.</li>
 * </ul>
 * Note that model-as-a-code approach natively supports any software life cycle tools and frameworks like various
 * build tools, CI/SCM tools, IDEs, etc. You don't need an additional web-based tools to manage
 * some aspects of your data models - your entire model and all of its components are part of your project's source code.
 * <p>
 * In most cases, one would use a convenient {@link NCModelAdapter} adapter to implement this interface. Here's a snippet
 * of the user data model from Lighswitch example:
 * <pre class="brush: java, highlight: [1]">
 * public class LightSwitchJavaModel extends NCModelAdapter {
 *     public LightSwitchJavaModel() {
 *         super(
 *             new NCModelConfig("nlpcraft.lightswitch.java.ex", "LightSwitch Example Model", "1.0"),
 *             new NCPipelineBuilder().withSemantic("en", "lightswitch_model.yaml").build()
 *         );
 *     }
 *
 *     &#64;NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
 *     NCResult onMatch(
 *          &#64;NCIntentTerm("act") NCEntity actEnt,
 *          &#64;NCIntentTerm("loc") List&lt;NCEntity&gt; locEnts
 *      ) {
 *          String status=actEnt.getId().equals("ls:on")?"on":"off";
 *          String locations=locEnts.isEmpty() ? "entire house":
 *              locEnts.stream().map(NCEntity::mkText).collect(Collectors.joining(", "));
 *
 *          return new NCResult(
 *              "Lights are [" + status + "] in [" + locations.toLowerCase() + "].",
 *              NCResultType.ASK_RESULT
 *          );
 *      }
 * }
 * </pre>
 *
 * @see NCModelClient
 * @see NCModelAdapter
 */
public interface NCModel {
    /**
     * Gets model configuration.
     *
     * @return Model configuraiton.
     */
    NCModelConfig getConfig();

    /**
     * Gets model NLP processing pipeline.
     *
     * @return NLP processing pipeline.
     */
    NCPipeline getPipeline();

    /**
     * A callback to accept or reject a parsed variant. This callback is called before any other callbacks at the
     * beginning of the processing pipeline and it is called for each parsed variant.
     * <p>
     * Note that a given input query can have one or more possible different parsing variants. Depending on model
     * configuration an input query can produce hundreds or even thousands of parsing variants that can significantly
     * slow down the overall processing. This method allows to filter out unnecessary parsing variants based on
     * variety of user-defined factors like number of entities, presence of a particular entnity in the variant, etc.
     * <p>
     * By default, this method accepts all variants (returns {@code true}).
     *
     * @param vrn A parsing variant to accept or reject.
     * @return {@code True} to accept variant for further processing, {@code false} otherwise.
     */
    default boolean onVariant(NCVariant vrn) {
        return true;
    }

    /**
     * A callback that is called when a fully assembled query context is ready. This callback is called after
     * all {@link #onVariant(NCVariant)} callbacks are called but before any {@link #onMatchedIntent(NCIntentMatch)} are
     * called, i.e. right before the intent matching is performed. It's called always once per input query processing.
     * Typical use case for this callback is to perform logging, debugging, statistic or usage collection, explicit
     * update or initialization of conversation context, security audit or validation, etc.
     * <p>
     * Default implementation returns {@code null}.
     *
     * @param ctx Input query context.
     * @return Optional query result to return interrupting the default workflow. Specifically, if this method
     *      returns a non-{@code null} result, it will be returned to the caller immediately overriding default behavior.
     *      If the method returns {@code null} - the default processing flow will continue.
     * @throws NCRejection This callback can throw the rejection exception to abort input query processing.
     */
    default NCResult onContext(NCContext ctx) throws NCRejection {
        return null;
    }

    /**
     *
     * @param ctx Input query context.
     * @return
     * @throws NCRejection
     */
    default boolean onMatchedIntent(NCIntentMatch ctx) throws NCRejection {
        return true;
    }

    /**
     *
     * @param ctx Input query context.
     * @param res
     * @return
     */
    default NCResult onResult(NCIntentMatch ctx, NCResult res) {
        return null;
    }

    /**
     *
     * @param ctx Input query context.
     * @param e
     * @return
     */
    default NCResult onRejection(NCIntentMatch ctx, NCRejection e) {
        return null;
    }

    /**
     *
     * @param ctx Input query context.
     * @param e
     * @return
     */
    default NCResult onError(NCContext ctx, Throwable e) {
        return null;
    }
}
