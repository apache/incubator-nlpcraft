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
 * NLPCraft lifecycle component.
 * <p>
 * This interface defines lifecycle hooks for user-defined components that are instantiated by NLPCraft like
 * probe components, custom NER parsers and value loaders. This hooks can be used to integrated with various IoC
 * frameworks, control lifecycle of external libraries and systems, perform initialization and cleanup
 * operations, etc.
 * <p>
 * Note that probe lifecycle components are configured via <code>nlpcraft.probe.lifecycle </code> probe
 * configuration property that accept list of fully qualified class names where each class should implement this
 * interface. See <a target=_ href="https://nlpcraft.apache.org/server-and-probe.html">documentation</a> on how to configure
 * a data probe.
 */
public interface NCLifecycle {
    /**
     * Called before lifecycle component is used and just after its creation. This method is guaranteed to be
     * called. Default implementation is no-op.
     */
    default void onInit() {}
    
    /**
     * Called after lifecycle components is no longer needed. Unlike {@link #onInit()} method this call is
     * optional and not guaranteed. Default implementation is a no-op.
     */
    default void onDiscard() {}
}
