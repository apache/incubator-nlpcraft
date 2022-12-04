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
  * A pipeline component that allows to validate a list of tokens produced by token parser before they get
  * sent further down the pipeline. This is one of the user-defined
  * components of the processing [[NCPipeline pipeline]]. See [[NCPipeline]] for documentation on the token
  * parser place in the overall processing pipeline.
  *
  * @see [[NCPipeline.getTokenValidators]]
  * @see [[NCEntity]]
  * @see [[NCToken]]
  * @see [[NCTokenParser]]
  * @see [[NCTokenEnricher]]
  * @see [[NCEntityParser]]
  * @see [[NCEntityEnricher]]
  * @see [[NCEntityValidator]]
  * @see [[NCEntityMapper]]
  */
trait NCTokenValidator extends NCLifecycle:
    /**
      * Validates given list of tokens. If validation fails this method should throw an [[NCException]]. Note that
      * token validator is called for an empty list of tokens as well.
      *
      * @param req Input request,
      * @param cfg Model configuration.
      * @param toks List of tokens to validate. Can be empty but never `null`.
      */
    def validate(req: NCRequest, cfg: NCModelConfig, toks: List[NCToken]): Unit


