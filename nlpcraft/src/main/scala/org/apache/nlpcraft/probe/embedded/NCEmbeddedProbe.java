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

package org.apache.nlpcraft.probe.embedded;

import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.model.*;
import org.apache.nlpcraft.model.tools.test.*;
import org.apache.nlpcraft.probe.*;
import org.apache.nlpcraft.probe.mgrs.nlp.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Embedded probe controller. Typically, data probes are launched in their own independent JVMs. However,
 * in some cases it is more convenient for model implementation or preferable for performance reasons to host
 * a data model (and hence the data probe) in the same "client" JVM.
 * <p>
 * The standard processing flow with the data probe running in a separate JVM looks like this:
 * <br>
 * <code>
 *     App ⇒ <b>JVM</b><sub>1</sub>(REST Server) ⇒ <b>JVM</b><sub>2</sub>(Probe) ⇒ <b>JVM</b><sub>1</sub>(REST Server) ⇒ App
 * </code>
 * <br>
 * There are at least 4 networks hops between client application request and response.
 * <p>
 * However, when using native Java Client or {@link NCTestClient Java Test Client} in embedded probe mode
 * the processing flow is shortened:
 * <br>
 * <code>
 *     <b>JVM</b><sub>1</sub>(App) ⇒ <b>JVM</b><sub>2</sub>(REST Server) ⇒ <b>JVM</b><sub>1</sub>(Probe -> App)
 * </code>
 * <br>
 * In this case there are only 2 hops as both client application and the data probe (and the model) are hosted
 * in the same JVM.
 * <p>
 * Notes:
 * <ul>
 *     <li>
 *         Embedded probe is only available for JVM processes (and can be used with any JVM languages).
 *     </li>
 *     <li>
 *         There can be only one embedded probe per JVM.
 *     </li>
 *     <li>
 *         Once data probe is stopped and cannot be started again in the same JVM.
 *     </li>
 *     <li>
 *         Even though the caller can register local-JVM listener for the query results, these results
 *         will still be asynchronously delivered to the REST server in the usual manner so that other clients
 *         could fetch these results to maintain internal logging, tracing and metrics. If the client
 *         application hosting data model and its probe <i>is the only client</i> for that model it needs to cancel the
 *         request on the REST server after receiving a local-JVM callback to release associated
 *         resources on the REST server.
 *     </li>
 * </ul>
 */
public class NCEmbeddedProbe {
    private static void waitForFuture(CompletableFuture<?> fut) {
        while (!fut.isDone()) {
            try {
                fut.get();
            }
            catch (InterruptedException | ExecutionException ignored) {}
        }
    }

    /**
     * Start the embedded probe with given configuration file. It is equivalent to starting a probe using
     * <code>-config=cfgFile</code> command line argument.
     *
     * @param cfgFile Configuration file path. It should be either a full path or the file name
     *      that can be found in the current working directory or on the classpath as a class loader
     *      resource.
     * @throws NCException Thrown in case of any errors starting the data probe.
     */
    public static void start(String cfgFile) {
        CompletableFuture<Void> fut = new CompletableFuture<>();

        NCProbeBoot$.MODULE$.start(cfgFile, fut);

        waitForFuture(fut);
    }

    /**
     *
     * @param classes
     */
    private static void checkModelClasses(Class<? extends NCModel>[] classes) {
        if (classes.length == 0)
            throw new NCException("At least one model class must be provided when starting embedded probe.");
    }

    /**
     * Starts the embedded probe with default configuration and specified models to deploy.
     * 
     * @param mdlClasses One or more data model classes to be deployed by the embedded probe.
     * @throws NCException Thrown in case of any errors starting the data probe.
     */
    @SafeVarargs
    public static void start(Class<? extends NCModel>... mdlClasses) {
        checkModelClasses(mdlClasses);

        CompletableFuture<Void> fut = new CompletableFuture<>();

        NCProbeBoot$.MODULE$.start(mdlClasses, fut);

        waitForFuture(fut);
    }

    /**
     * Starts the embedded probe with default configuration and specified overrides.
     *
     * @param probeId Probe ID.
     * @param tok Probe token.
     * @param upLink Probe up-link to the server.
     * @param dnLink Probe down-link from the server.
     * @param mdlClasses One or more data model classes to be deployed by the embedded probe.
     * @throws NCException Thrown in case of any errors starting the data probe.
     */
    @SafeVarargs
    public static void start(
        String probeId,
        String tok,
        String upLink,
        String dnLink,
        Class<? extends NCModel>... mdlClasses) {
        checkModelClasses(mdlClasses);

        CompletableFuture<Void> fut = new CompletableFuture<>();

        NCProbeBoot$.MODULE$.start(probeId, tok, upLink, dnLink, mdlClasses, fut);

        waitForFuture(fut);
    }

    /**
     * Stops the embedded probe, if it was started before. Note that the probe cannot be started again
     * in the same JVM process.
     *
     * @throws NCException Thrown in case of any errors stopping the data probe.
     */
    public static void stop() {
        NCProbeBoot$.MODULE$.stop();
    }

    /**
     * Registers the callback on query processing results. Results from all models deployed on this embedded probe
     * will trigger this callback.
     *
     * @param cb Callback to register.
     * @throws NCException Thrown in case of any errors registering a callback.
     */
    public static void registerCallback(Consumer<NCEmbeddedResult> cb) {
        NCProbeEnrichmentManager$.MODULE$.addEmbeddedCallback(cb);
    }

    /**
     * Unregisters previously registered callback. Ignored if given callback wasn't registered before.
     * 
     * @param cb Callback to unregister.
     * @throws NCException Thrown in case of any errors unregistering a callback.
     */
    public static void unregisterCallback(Consumer<NCEmbeddedResult> cb) {
        NCProbeEnrichmentManager$.MODULE$.removeEmbeddedCallback(cb);
    }
}
