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

import org.apache.nlpcraft.internal.impl.NCModelClientImpl;

import java.util.Map;
import java.util.function.Predicate;

/**
 *
 */
public class NCModelClient implements AutoCloseable {
    private final NCModelClientImpl impl;

    /**
     *
     * @param mdl
     */
    public NCModelClient(NCModel mdl) {
        this.impl = new NCModelClientImpl(mdl);
    }

    /**
     *
     * @param txt
     * @param data
     * @param usrId
     * @return
     * @throws NCException
     */
    public NCResult ask(String txt, Map<String, Object> data, String usrId) {
        return impl.ask(txt, data, usrId);
    }

    /**
     *
     * @param usrId
     * @throws NCException
     */
    public void clearStm(String usrId) {
        impl.clearStm(usrId);
    }

    /**
     *
     * @param usrId
     * @param filter
     */
    public void clearStm(String usrId, Predicate<NCEntity> filter) {
        impl.clearStm(usrId, filter);
    }

    /**
     *
     * @param usrId
     * @throws NCException
     */
    public void clearDialog(String usrId) {
        impl.clearDialog(usrId);
    }

    /**
     *
     * @param usrId
     * @param filter
     */
    public void clearDialog(String usrId, Predicate<NCDialogFlowItem> filter) {
        impl.clearDialog(usrId, filter);
    }

    /**
     *
     */
    @Override
    public void close() {
        impl.close();
    }

    /**
     *
     */
    public void validateSamples() {
        impl.validateSamples();
    }

    /**
     * TODO:
     * Gets callback information which contains intent ID and callback arguments entities.
     * Note that
     *  - Callback is not called in this case.
     *  - if model `onContext` method overrided - error thrown because we don't find intents in this case.
     *
     *  Callback.
     *   - You can call callback only one time.
     *   - You can't call callback if it is not last request.
     *   - if you call callback and 'saveHistory' flag was true - dialog overriden by callback result instead of saved before empty result.
     *   - if you call callback and 'saveHistory' flag was false - history data is still ignored.
     *   - No matter of callback execution time - history data based on request timestamp.
     *
     * @param txt
     * @param data
     * @param usrId
     * @param saveHistory if true that found intent data added to dialog flow (with empty NCResult, bacause callback wasn't called) and STM.
     *                    if false that found intent is not saved in STM and dialog flow.
     * @return
     */
    public NCCallbackData debugAsk(String txt, Map<String, Object> data, String usrId, boolean saveHistory) {
        return impl.debugAsk(txt, data, usrId, saveHistory);
    }
}
