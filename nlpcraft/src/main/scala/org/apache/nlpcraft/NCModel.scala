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

package org.apache.nlpcraft

/**
  * User data model
  * <p>
  * Data model is a holder for user-define NLP processing logic that provides an interface to your data sources like a
  * database or a SaaS application. NLPCraft employs model-as-a-code approach where entire data model is an
  * implementation of this interface which can be developed using any JVM programming language like Java, Scala,
  * Kotlin, or Groovy. The instance of this interface is passed to {@link NCModelClient} class and contains:
  * <ul>
  * <li>Model {@link # getConfig ( ) configurfation}.</li>
  * <li>Model {@link # getPipeline ( ) processing pipeline}.</li>
  * <li>Life-cycle callbacks.</li>
  * </ul>
  * Note that model-as-a-code approach natively supports any software life cycle tools and frameworks like various
  * build tools, CI/SCM tools, IDEs, etc. You don't need an additional web-based tools to manage
  * some aspects of your data models - your entire model and all of its components are part of your project's source code.
  * <p>
  * In most cases, one would use a convenient {@link NCModelAdapter} adapter to implement this interface. Here's a snippet
  * of the user data model from LightSwitch example:
  * <pre class="brush: java, highlight: [1]">
  * public class LightSwitchJavaModel extends NCModelAdapter {
  * public LightSwitchJavaModel() {
  * super(
  * new NCModelConfig("nlpcraft.lightswitch.java.ex", "LightSwitch Example Model", "1.0"),
  * new NCPipelineBuilder().withSemantic("en", "lightswitch_model.yaml").build()
  * );
  * }
  *
  * &#64;NCIntent("intent=ls term(act)={has(ent_groups, 'act')} term(loc)={# == 'ls:loc'}*")
  * NCResult onMatch(
  * &#64;NCIntentTerm("act") NCEntity actEnt,
  * &#64;NCIntentTerm("loc") List&lt;NCEntity&gt; locEnts
  * ) {
  * String status=actEnt.getId().equals("ls:on")?"on":"off";
  * String locations=locEnts.isEmpty() ? "entire house":
  * locEnts.stream().map(NCEntity::mkText).collect(Collectors.joining(", "));
  *
  * return new NCResult(
  * "Lights are [" + status + "] in [" + locations.toLowerCase() + "].",
  * NCResultType.ASK_RESULT
  * );
  * }
  * }
  * </pre>
  *
  * @see NCModelClient
  * @see NCModelAdapter */
trait NCModel:
    /**
      * Gets model configuration.
      *
      * @return Model configuration. */
    def getConfig: NCModelConfig

    /**
      * Gets model NLP processing pipeline.
      *
      * @return NLP processing pipeline. */
    def getPipeline: NCPipeline

    /**
      * A callback to accept or reject a parsed variant. This callback is called before any other callbacks at the
      * beginning of the processing pipeline, and it is called for each parsed variant.
      * <p>
      * Note that a given input query can have one or more possible different parsing variants. Depending on model
      * configuration an input query can produce hundreds or even thousands of parsing variants that can significantly
      * slow down the overall processing. This method allows to filter out unnecessary parsing variants based on
      * variety of user-defined factors like number of entities, presence of a particular entity in the variant, etc.
      * <p>
      * By default, this method accepts all variants (returns {@code true}).
      *
      * @param vrn A parsing variant to accept or reject.
      * @return {@code True} to accept variant for further processing, {@code false} otherwise. */
    def onVariant(vrn: NCVariant) = true

    /**
      * A callback that is called when a fully assembled query context is ready. This callback is called after
      * all {@link # onVariant ( NCVariant )} callbacks are called but before any {@link # onMatchedIntent ( NCIntentMatch )} are
      * called, i.e. right before the intent matching is performed. It's called always once per input query processing.
      * Typical use case for this callback is to perform logging, debugging, statistic or usage collection, explicit
      * update or initialization of conversation context, security audit or validation, etc.
      * <p>
      * Default implementation returns {@code null}.
      *
      * @param ctx Input query context.
      * @return Optional query result to return interrupting the default workflow. Specifically, if this method
      * returns a non-{@code null} result, it will be returned to the caller immediately overriding default behavior.
      * If the method returns {@code null} - the default processing flow will continue.
      * @throws NCRejection This callback can throw the rejection exception to abort input query processing. */
    @throws[NCRejection] def onContext(ctx: NCContext): NCResult = null

    /**
      * A callback that is called when intent was successfully matched but right before its callback is called. This
      * callback is called after {@link # onContext ( NCContext )} is called and may be called multiple times
      * depending on its return value. If {@code true} is returned than the default workflow will continue and the
      * matched intent's callback will be called. However, if {@code null} is returned than the entire existing set of
      * parsing variants will be matched against all declared intents again. Returning {@code false} allows this
      * method to alter the state of the model (like soft-reset conversation or change metadata) and force the
      * full re-evaluation of the parsing variants against all declared intents.
      * <p>
      * Note that user logic should be careful not to induce infinite loop in this behavior.
      * Note that this callback may not be called at all based on the return value of {@link # onContext ( NCContext )} callback.
      * Typical use case for this callback is to perform logging, debugging, statistic or usage collection, explicit
      * update or initialization of conversation context, security audit or validation, etc.
      * <p>
      * By default, this method returns {@code true}.
      *
      * @param im Intent match context - the same instance that's passed to the matched intent callback.
      * @return If {@code true} is returned than the default workflow will continue and the matched intent's
      * callback will be called. However, if {@code false} is returned than the entire existing set of
      * parsing variants will be matched against all declared intents again. Returning false allows this
      * method to alter the state of the model (like soft-reset conversation or change metadata) and force
      * the re-evaluation of the parsing variants against all declared intents. Note that user logic should be
      * careful not to induce infinite loop in this behavior.
      * @throws NCRejection This callback can throw the rejection exception to abort user request processing. In this
      * case the {@link # onRejection ( NCIntentMatch, NCRejection)} callback will be called next. */
    @throws[NCRejection] def onMatchedIntent(ctx: NCContext, im: NCIntentMatch) = true

    /**
      * A callback that is called when successful result is obtained from the intent callback and right before
      * sending it back to the caller. This callback is called after {@link # onMatchedIntent ( NCIntentMatch )} is called.
      * Note that this callback may not be called at all, and if called - it's called only once. Typical use case
      * for this callback is to perform logging, debugging, statistic or usage collection, explicit update or
      * initialization of conversation context, security audit or validation, etc.
      * <p>
      * Default implementation is a no-op returning {@code null}.
      *
      * @param im Intent match context - the same instance that's passed to the matched intent callback
      * that produced this result.
      * @param res Existing result.
      * @return Optional query result to return interrupting the default workflow. Specifically, if this
      * method returns a non-{@code null} result, it will be returned to the caller immediately overriding
      * default behavior and existing query result or error processing, if any. If the method returns {@code null} -
      * the default processing flow will continue. */
    def onResult(ctx: NCContext, im: NCIntentMatch, res: NCResult): NCResult = null

    /**
      * A callback that is called when intent callback threw NCRejection exception. This callback is called
      * after {@link # onMatchedIntent ( NCIntentMatch )} is called. Note that this callback may not be called at all,
      * and if called - it's called only once. Typical use case for this callback is to perform logging, debugging,
      * statistic or usage collection, explicit update or initialization of conversation context, security audit or
      * validation, etc.
      * <p>
      * Default implementation is a no-op returning {@code null}.
      *
      * @param ctx Optional intent match context - the same instance that's passed to the matched intent callback
      * that produced this rejection. It is {@code null} if rejection was triggered outside the intent callback.
      * @param e Rejection exception.
      * @return Optional query result to return interrupting the default workflow. Specifically, if this method
      * returns a non-{@code null} result, it will be returned to the caller immediately overriding default behavior
      * and existing query result or error processing, if any. If the method returns {@code null} - the default
      * processing flow will continue. */
    def onRejection(ctx: NCContext, im: NCIntentMatch, e: NCRejection): NCResult = null

    /**
      * A callback that is called when intent callback failed with unexpected exception. Note that this callback may
      * not be called at all, and if called - it's called only once. Typical use case for this callback is
      * to perform logging, debugging, statistic or usage collection, explicit update or initialization of conversation
      * context, security audit or validation, etc.
      * <p>
      * Default implementation is a no-op returning {@code null}.
      *
      * @param ctx Intent match context - the same instance that's passed to the matched intent that produced this error.
      * @param e Failure exception.
      * @return Optional query result to return interrupting the default workflow. Specifically, if this method
      * returns a non-{@code null} result, it will be returned to the caller immediately overriding default
      * behavior and existing query result or error processing, if any. If the method returns {@code null} - the
      * default processing flow will continue. */
    def onError(ctx: NCContext, e: Throwable): NCResult = null
