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

/**
 * User-defined semantic data model.
 * <p>
 * Data model is a central concept in NLPCraft defining an interface to your data sources
 * like a database or a SaaS application. NLPCraft employs model-as-a-code approach where entire data model
 * is an implementation of this interface which can be developed using any JVM programming language
 * like Java, Scala, Kotlin, or Groovy. Data model definition is split into two interfaces: {@link NCModelView}
 * that defines the declarative, configuration, part of the model that is usually defined in an external
 * JSON or YAML file, and this interface that provides various life-cycle callbacks.
 * <p>
 * Generally, a data model defines:
 * <ul>
 *     <li>Set of model {@link NCElement elements} (a.k.a. named entities) to be detected in the user input.</li>
 *     <li>Zero or more <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">intent</a> callbacks.</li>
 *     <li>Common model configuration and life-cycle callbacks.</li>
 * </ul>
 * Note that model-as-a-code approach natively supports any software life cycle tools and frameworks like various
 * build tools, CI/SCM tools, IDEs, etc. You don't have to resort to additional web-based tools to manage some aspects of
 * your data models - your entire model and all of its components are part of your project's source code.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCModelAdapter
 * @see NCModelFileAdapter
 */
public interface NCModel extends NCModelView, NCLifecycle {
    /**
     * A callback to accept or reject a parsed variant. This callback is called before any other
     * callbacks at the beginning of the processing pipeline and it is called for each parsed variant.
     * <p>
     * Note that a given user input can have one or more possible different parsing variants. Depending on model
     * configuration a user input can produce hundreds or even thousands of parsing variants that can significantly slow
     * down the overall processing. This method allows to filter out unnecessary parsing variants based on variety of
     * user-defined factors like number of tokens, presence of a particular token in the variant, etc.
     * <p>
     * By default, this method accepts all variants (returns {@code true}).
     *
     * @param var A variant (list of tokens) to accept or reject.
     * @return {@code True} to accept variant for further processing, {@code false} otherwise.
     */
    default boolean onParsedVariant(NCVariant var) {
        return true;
    }

    /**
     * A callback that is called when a fully assembled query context is ready. This callback is called after
     * all {@link #onParsedVariant(NCVariant)} callbacks are called but before any {@link #onMatchedIntent(NCIntentMatch)}
     * are called, i.e. right before the intent matching is performed. It's called always once per user request processing.
     * Typical use case for this callback is to perform logging, debugging, statistic or usage collection,
     * explicit update or initialization of conversation context, security audit or validation, etc.
     * <p>
     * Default implementation returns {@code null}.
     *
     * @param ctx Query context.
     * @return Optional query result to return interrupting the default workflow. Specifically, if this method returns
     *      a non-{@code null} result, it will be returned to the caller immediately overriding default behavior. If
     *      the method returns {@code null} - the default processing flow will continue.
     * @throws NCRejection This callback can throw this rejection exception to abort user request processing.
     */
    default NCResult onContext(NCContext ctx) throws NCRejection {
        return null;
    }

    /**
     * A callback that is called when intent was successfully matched but right before its callback is called.
     * This callback is called after {@link #onContext(NCContext)} is called and may be called multiple times
     * depending on its return value. If {@code true} is returned than the default workflow will continue and
     * the matched intent's callback will be called. However, if {@code false} is returned than the entire
     * existing set of parsing variants will be matched against all declared intents again. Returning {@code false}
     * allows this method to alter the state of the model (like soft-reset conversation or change  metadata) and
     * force the full re-evaluation of the parsing variants against all declared intents. Note that user logic should
     * be careful not to induce infinite loop in this behavior.
     * <p>
     * Note that this callback may not be called at all based on the return value
     * of {@link #onContext(NCContext)} callback. Typical use case for this callback is to perform logging, debugging,
     * statistic or usage collection, explicit update or initialization of conversation context, security audit
     * or validation, etc.
     * <p>
     * By default, this method returns {@code true}.
     *
     * @param ctx Intent match context - the same instance that's passed to the matched intent callback.
     * @return If {@code true} is returned than the default workflow will continue and the matched intent's callback
     *      will be called. However, if {@code false} is returned than the entire existing set of parsing variants will
     *      be matched against all declared intents again. Returning {@code false} allows this method to alter the state of
     *      the model (like soft-reset conversation or change metadata) and force the re-evaluation of the parsing
     *      variants against all declared intents. Note that user logic should be careful not to induce infinite loop in
     *      this behavior.
     * @throws NCRejection This callback can throw the rejection exception to abort user request processing. In this
     *      case the {@link #onRejection(NCIntentMatch, NCRejection)} callback will be called next.
     */
    default boolean onMatchedIntent(NCIntentMatch ctx) throws NCRejection {
        return true;
    }

    /**
     * A callback that is called when successful result is obtained from the intent callback and right before sending it
     * back to the caller. This callback is called after {@link #onMatchedIntent(NCIntentMatch)} is called.
     * Note that this callback may not be called at all, and if called - it's called only once.
     * Typical use case for this callback is to perform logging, debugging, statistic or usage collection,
     * explicit update or initialization of conversation context, security audit or validation, etc.
     * <p>
     * Default implementation is a no-op returning {@code null}.
     *
     * @param ctx Intent match context - the same instance that's passed to the matched intent callback that
     *      produced this result.
     * @param res Existing result.
     * @return Optional query result to return interrupting the default workflow. Specifically, if this method returns
     *      a non-{@code null} result, it will be returned to the caller immediately overriding default behavior and
     *      existing query result or error processing, if any. If the method returns {@code null} - the default
     *      processing flow will continue.
     */
    default NCResult onResult(NCIntentMatch ctx, NCResult res) {
        return null;
    }

    /**
     * A callback that is called when intent callback threw {@link NCRejection} exception.
     * This callback is called after {@link #onMatchedIntent(NCIntentMatch)} is called.
     * Note that this callback may not be called at all, and if called - it's called only once.
     * Typical use case for this callback is to perform logging, debugging, statistic or usage collection,
     * explicit update or initialization of conversation context, security audit or validation, etc.
     * <p>
     * Default implementation is a no-op returning {@code null}.
     *
     * @param ctx Optional intent match context - the same instance that's passed to the matched intent callback that
     *      produced this rejection. It is {@code null} if rejection was triggered outside of the intent callback.
     * @param e Rejection exception.
     * @return Optional query result to return interrupting the default workflow. Specifically, if this method returns
     *      a non-{@code null} result, it will be returned to the caller immediately overriding default behavior and
     *      existing query result or error processing, if any. If the method returns {@code null} - the default
     *      processing flow will continue.
     */
    default NCResult onRejection(NCIntentMatch ctx, NCRejection e) {
        return null;
    }

    /**
     * A callback that is called when intent callback failed with unexpected exception.
     * Note that this callback may not be called at all, and if called - it's called only once.
     * Typical use case for this callback is to perform logging, debugging, statistic or usage collection,
     * explicit update or initialization of conversation context, security audit or validation, etc.
     * <p>
     * Default implementation is a no-op returning {@code null}.
     *
     * @param ctx Intent match context - the same instance that's passed to the matched intent that
     *      produced this error.
     * @param e Failure exception.
     * @return Optional query result to return interrupting the default workflow. Specifically, if this method returns
     *      a non-{@code null} result, it will be returned to the caller immediately overriding default behavior and
     *      existing query result or error processing, if any. If the method returns {@code null} - the default
     *      processing flow will continue.
     */
    default NCResult onError(NCContext ctx, Throwable e) {
        return null;
    }
}
