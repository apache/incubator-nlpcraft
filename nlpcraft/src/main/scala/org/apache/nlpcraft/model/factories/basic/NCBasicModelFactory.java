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

package org.apache.nlpcraft.model.factories.basic;

import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.model.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Default implementation of {@link NCModelFactory} interface.
 * <p>
 * This factory doesn't have any configuration properties and uses {@link java.lang.reflect.Constructor#newInstance(Object...)} to construct {@link NCModel}s.
 * </p>
 * Basic factory have to be specified in probe configuration. Here's
 * a <code>probe.conf</code> from <a target="github" href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examplesnames">Names</a> example
 * using Spring-based factory:
 * <pre class="brush:js, highlight: [11, 12, 13, 14, 15, 16]">
 * nlpcraft {
 *     probe {
 *         id = "names"
 *         token = "3141592653589793"
 *         upLink = "localhost:8201"   # Server to probe data pipe.
 *         downLink = "localhost:8202" # Probe to server data pipe.
 *         jarsFolder = null
 *         models = [
 *             "org.apache.nlpcraft.examples.names.NamesModel"
 *         ]
 *         modelFactory = {
 *             type = "org.apache.nlpcraft.model.factories.spring.NCSpringModelFactory"
 *             properties = {
 *                 javaConfig = "org.apache.nlpcraft.examples.names.NamesConfig"
 *             }
 *         }
 *         lifecycle = [
 *         ]
 *         resultMaxSizeBytes = 1048576
 *     }
 *     nlpEngine = "opennlp"
 * }
 * </pre>
 * <p>
 *     Lines 10-15 specify data model factory and its configuration properties.
 * </p>
 */
public class NCBasicModelFactory implements NCModelFactory {
    @Override
    public void initialize(Map<String, String> props) {
        // No-op.
    }

    @Override
    public NCModel mkModel(Class<? extends NCModel> type) {
        try {
            NCModel mdl = type.getConstructor().newInstance();

            mdl.getMetadata().put(org.apache.nlpcraft.common.package$.MODULE$.MDL_META_MODEL_CLASS_KEY(), type.getCanonicalName());

            return mdl;
        }
        catch (NoSuchMethodException e) {
            throw new NCException(String.format("Model class does not have no-arg constructor: %s", type.getCanonicalName()), e);
        }
        catch (InstantiationException e) {
            throw new NCException(String.format("Model class cannot be an abstract class: %s", type.getCanonicalName()), e);
        }
        catch (InvocationTargetException e) {
            throw new NCException(String.format("Model no-arg constructor failed: %s", type.getCanonicalName()), e);
        }
        catch (ExceptionInInitializerError e) {
            throw new NCException(String.format("Model class initialization failed: %s", type.getCanonicalName()), e);
        }
        catch (Exception e) {
            throw new NCException(String.format("Failed to instantiate model: %s", type.getCanonicalName()), e);
        }
    }

    @Override
    public void terminate() {
        // No-op.  
    }
}
