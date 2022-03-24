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
 *
 */
public interface NCModel {
    /**
     *
     * @return
     */
    NCModelConfig getConfig();

    /**
     *
     * @return
     */
    NCPipeline getPipeline();

    /**
     *
     * @param vrn
     * @return
     */
    default boolean onVariant(NCVariant vrn) {
        return true;
    }

    /**
     *
     * @param ctx
     * @return
     * @throws NCRejection
     */
    default NCResult onContext(NCContext ctx) throws NCRejection {
        return null;
    }

    /**
     *
     * @param ctx
     * @return
     * @throws NCRejection
     */
    default boolean onMatchedIntent(NCIntentMatch ctx) throws NCRejection {
        return true;
    }

    /**
     *
     * @param ctx
     * @param res
     * @return
     */
    default NCResult onResult(NCIntentMatch ctx, NCResult res) {
        return null;
    }

    /**
     *
     * @param ctx
     * @param e
     * @return
     */
    default NCResult onRejection(NCIntentMatch ctx, NCRejection e) {
        return null;
    }

    /**
     *
     * @param ctx
     * @param e
     * @return
     */
    default NCResult onError(NCContext ctx, Throwable e) {
        return null;
    }
}
