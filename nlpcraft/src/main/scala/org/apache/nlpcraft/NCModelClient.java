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

import java.util.Map;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 */
public class NCModelClient implements NCLifecycle {
    private final NCModel mdl;

    /**
     *
     * @param mdl
     */
    public NCModelClient(NCModel mdl) {
        this.mdl = mdl;
    }

    /**
     *
     * @throws NCException
     */
    private static void verify() throws NCException {
        // TODO:
    }

    private static void start(ExecutorService s, List<? extends NCLifecycle> list, NCModelConfig cfg) {
        assert s != null;

        if (list != null)
            list.forEach(p -> s.execute(() -> p.start(cfg)));
    }

    private static void stop(ExecutorService s, List<? extends NCLifecycle> list) {
        assert s != null;

        if (list != null)
            list.forEach(p -> s.execute(() -> p.stop()));
    }

    private static void stopExecutorService(ExecutorService s) {
        try {
            s.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            throw new NCException("Thread interrupted.", e);
        }
    }

    private static ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void start(NCModelConfig cfg) {
        verify();

        ExecutorService s = getExecutorService();

        try {
            start(s, cfg.getTokenParsers(), cfg);
            start(s, cfg.getEntityParsers(), cfg);
            start(s, cfg.getEntityEnrichers(), cfg);
            start(s, cfg.getTokenEnrichers(), cfg);
        }
        finally {
            stopExecutorService(s);
        }
    }

    @Override
    public void stop() {
        NCModelConfig cfg = mdl.getConfig();
        ExecutorService s = getExecutorService();

        try {
            stop(s, cfg.getTokenEnrichers());
            stop(s, cfg.getEntityEnrichers());
            stop(s, cfg.getEntityParsers());
            stop(s, cfg.getTokenEnrichers());
        }
        finally {
            stopExecutorService(s);
        }
    }

    /**
     *
     * @param txt
     * @param data
     * @param usrId
     * @return
     * @throws NCException
     */
    public CompletableFuture<NCResult> ask(String txt, Map<String, Object> data, String usrId) {
        return null; // TODO
    }

    /**
     *
     * @param txt
     * @param data
     * @param usrId
     * @return
     * @throws NCException
     */
    public NCResult askSync(String txt, Map<String, Object> data, String usrId) {
        return null; // TODO
    }

    /**
     *
     * @param usrId
     * @throws NCException
     */
    public void clearConversation(String usrId) {
        // TODO
    }

    /**
     *
     * @param usrId
     * @throws NCException
     */
    public void clearDialog(String usrId) {
        // TODO
    }
}
