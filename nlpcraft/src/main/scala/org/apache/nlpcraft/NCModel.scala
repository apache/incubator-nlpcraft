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
  * Data model.
  *
  * Data model is a key entity in NLPCraft and contains:
  *  - Model [[NCModel.getConfig configuration]].
  *  - Model [[NCModel.getPipeline processing pipeline]].
  *  - Life-cycle callbacks.
  *
  * NLPCraft employs model-as-a-code approach where entire data model is an implementation of just this interface.
  * The instance of this interface is passed to [[NCModelClient]] class.
  * Note that the model-as-a-code approach natively supports any software life cycle tools and frameworks like various
  * build tools, CI/SCM tools, IDEs, etc. You don't need any additional tools to manage
  * some aspects of your data models - your entire model and all of its components are part of your project's
  * source code.
  *
  *
  * @see [[NCModelClient]]
  * @see [[NCModelConfig]]
  * @see [[NCPipeline]]
  */
trait NCModel(cfg: NCModelConfig, pipeline: NCPipeline):
    /**
      * Gets model configuration.
      *
      * @return Model configuration.
      */
    def getConfig: NCModelConfig = cfg

    /**
      * Gets model NLP processing pipeline.
      *
      * @return NLP processing pipeline.
      */
    def getPipeline: NCPipeline = pipeline

    /**
      * A callback that is called when a fully assembled query context is ready. This is the first of the callbacks
      * that is called on the model and right before the intent matching is performed. It's called always once per
      * input query processing. Typical use case for this callback is to perform logging, debugging, statistic or
      * usage collection, explicit update or initialization of conversation context, security audit or validation, etc.
      *
      * Default implementation returns `None`.
      *
      * @param ctx Input query context.
      * @return Optional query result to return interrupting the default processing workflow. Specifically, if
      *         this method returns a `Some` result, it will be returned to the caller immediately interrupting
      *         default processing workflow. If the method returns `None` - the default processing flow will continue.
      * @throws NCRejection This callback can throw the rejection exception to abort input query processing.
      */
    @throws[NCRejection]
    def onContext(ctx: NCContext): Option[NCResult] = None

    /**
      * A callback that is called when intent was successfully matched but right before its callback is called. This
      * callback is called after [[onContext()]] is called and may be called multiple times depending on its return
      * value. If `true` is returned than the default processing workflow will continue and the
      * matched intent's callback will be called. However, if `false` is returned than the entire existing set of
      * parsing variants will be matched against all declared intents again. Returning `false` allows this
      * method to alter the state of the model (like soft-reset conversation or change metadata) and force a
      * full re-evaluation of the parsing variants against all declared intents again.
      *
      * Note that user logic should be careful not to induce infinite loop in this behavior.
      * Note that this callback may not be called at all based on the return value of [[onContext()]] callback.
      * Typical use case for this callback is to perform logging, debugging, statistic or usage collection, explicit
      * update or initialization of conversation context, security audit or validation, etc.
      *
      * By default, this method returns `true`.
      *
      * @param im Intent match context - the same instance that's passed to the matched intent callback.
      * @return If `true` is returned than the default workflow will continue and the matched intent's
      *         callback will be called. However, if `false` is returned than the entire existing set of
      *         parsing variants will be matched against all declared intents again. Returning false allows this
      *         method to alter the state of the model (like soft-reset conversation or change metadata) and force
      *         the re-evaluation of the parsing variants against all declared intents again. Note that user logic
      *         should be careful not to induce infinite loop in this behavior.
      * @throws NCRejection This callback can throw the rejection exception to abort user request processing. In this
      *         case the [[onRejection()]] callback will be called next.
      */
    @throws[NCRejection]
    def onMatchedIntent(ctx: NCContext, im: NCIntentMatch): Boolean = true

    /**
      * A callback that is called when successful result is obtained from the intent callback and right before
      * sending it back to the caller. This callback is called after [[onMatchedIntent()]] is called.
      * Note that this callback may not be called at all, and if called - it's called only once. Typical use case
      * for this callback is to perform logging, debugging, statistic or usage collection, explicit update or
      * initialization of conversation context, security audit or validation, etc.
      *
      * Default implementation is a no-op returning `None`.
      *
      * @param im Intent match context - the same instance that's passed to the matched intent callback
      *         that produced this result.
      * @param res Existing result.
      * @return Optional query result to return interrupting the default workflow. Specifically, if this
      *         method returns a `Some` result, it will be returned to the caller immediately overriding
      *         default behavior and existing query result or error processing, if any. If the method returns `None` -
      *         the default processing flow will continue.
      */
    def onResult(ctx: NCContext, im: NCIntentMatch, res: NCResult): Option[NCResult] = None

    /**
      * A callback that is called when intent callback threw NCRejection exception. This callback is called
      * after [[onMatchedIntent()]] is called. Note that this callback may not be called at all,
      * and if called - it's called only once. Typical use case for this callback is to perform logging, debugging,
      * statistic or usage collection, explicit update or initialization of conversation context, security audit or
      * validation, etc.
      *
      * Default implementation is a no-op returning `None`.
      *
      * @param ctx Intent match context - the same instance that's passed to the matched intent that produced this error.
      * @param im Optional intent match context - the same instance that's passed to the matched intent callback
      *         that produced this result. It is `None` if rejection was triggered outside the intent callback.
      * @param e Rejection exception.
      * @return Optional query result to return interrupting the default workflow. Specifically, if this method
      *         returns a `Some` result, it will be returned to the caller immediately overriding default behavior
      *         and existing query result or error processing, if any. If the method returns `None` - the default
      *         processing flow will continue.
      */
    def onRejection(ctx: NCContext, im: Option[NCIntentMatch], e: NCRejection): Option[NCResult] = None

    /**
      * A callback that is called when intent callback failed with unexpected exception. Note that this callback may
      * not be called at all, and if called - it's called only once. Typical use case for this callback is
      * to perform logging, debugging, statistic or usage collection, explicit update or initialization of conversation
      * context, security audit or validation, etc.
      *
      * Default implementation is a no-op returning `None`.
      *
      * @param ctx Intent match context - the same instance that's passed to the matched intent that produced this error.
      * @param e Failure exception.
      * @return Optional query result to return interrupting the default workflow. Specifically, if this method
      *         returns a `Some` result, it will be returned to the caller immediately overriding default
      *         behavior and existing query result or error processing, if any. If the method returns `None` - the
      *         default processing flow will continue.
      */
    def onError(ctx: NCContext, e: Throwable): Option[NCResult] = None