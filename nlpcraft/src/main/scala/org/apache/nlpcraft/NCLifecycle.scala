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
  * Lifecycle callbacks for various pipeline components.
  *
  * @see NCTokenParser
  * @see NCTokenEnricher
  * @see NCTokenValidator
  * @see NCEntityParser
  * @see NCEntityEnricher
  * @see NCEntityValidator */
trait NCLifecycle:
    /**
      * Called when the component starts. Default implementation is no-op.
      *
      * @param cfg Configuration of the model this component is associated with. */
    def onStart(cfg: NCModelConfig): Unit = () // No-op.

    /**
      * Called when the component stops. Default implementation is no-op.
      *
      * @param cfg Configuration of the model this component is associated with. */
    def onStop(cfg: NCModelConfig): Unit = () // No-op.
