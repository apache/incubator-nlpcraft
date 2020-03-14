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

package org.apache.nlpcraft.model.factories.spring;

import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.model.*;
import org.springframework.context.annotation.*;
import org.springframework.context.support.*;
import java.util.*;

/**
 * Factory that delegates construction of {@link NCModel}s to the Spring Framework.
 * <p>
 * This factory requires one of the following configuration properties:
 * </p>
 * <ul>
 * <li>{@value #JAVA_CONFIG_PROP} - name a class that is annotated with the Spring's {@link Configuration} annotation
 * </li>
 * <li>{@value #XML_CONFIG_PROP} - path to an XML files with Spring bean definitions</li>
 * </ul>
 * Spring factory have to be specified in probe configuration. Here's
 * a <code>probe.conf</code> from <a target="github" href="https://github.com/apache/incubator-nlpcraft/tree/master/src/main/scala/org/apache/nlpcraft/examples/names">Names</a> example
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
 *     versionCheckDisable = false
 *  }
 * </pre>
 * <p>
 *     Lines 10-15 specify data model factory and its configuration properties.
 * </p>
 */
public class NCSpringModelFactory implements NCModelFactory {
    /**
     * Configuration property for Java Config-based Spring configuration.
     */
    public static final String JAVA_CONFIG_PROP = "javaConfig";
    
    /**
     * Configuration property for XML-based Spring configuration.
     */
    public static final String XML_CONFIG_PROP = "xmlConfig";
    
    /**
     * Spring application context.
     */
    private AbstractApplicationContext ctx;
    
    @Override
    public void initialize(Map<String, String> props) {
        if (ctx != null)
            throw new IllegalStateException(String.format("%s already initialized.", getClass().getSimpleName()));
        
        String javaCfg = props.get(JAVA_CONFIG_PROP);
        String xmlCfg = props.get(XML_CONFIG_PROP);
        
        if (javaCfg != null && xmlCfg != null) {
            throw new NCException(
                String.format("%s not configured properly ('%s' and '%s' can't be specified simultaneously)",
                    getClass().getSimpleName(),
                    JAVA_CONFIG_PROP,
                    XML_CONFIG_PROP
                )
            );
        }
        else if (javaCfg == null && xmlCfg == null)
            throw new NCException(
                String.format(
                    "%s not configured properly (either '%s' or '%s' property must be specified).",
                    getClass().getSimpleName(),
                    JAVA_CONFIG_PROP,
                    XML_CONFIG_PROP)
            );
        else if (javaCfg != null)
            ctx = mkJavaContext(javaCfg);
        else
            ctx = mkXmlContext(xmlCfg);
    }
    
    @Override
    public NCModel mkModel(Class<? extends NCModel> claxx) {
        if (ctx == null)
            throw new IllegalStateException(String.format("%s is not initialized.", getClass().getSimpleName()));
        
        return ctx.getBean(claxx);
    }
    
    @Override
    public void terminate() {
        if (ctx != null)
            try {
                ctx.close();
            }
            finally {
                ctx = null;
            }
    }
    
    /**
     * @param claxx
     * @return
     */
    private static AbstractApplicationContext mkJavaContext(String claxx) {
        try {
            return new AnnotationConfigApplicationContext(Class.forName(claxx));
        }
        catch (Exception e) {
            throw new NCException(String.format("Failed to load Spring configuration class [name=%s]", claxx), e);
        }
    }
    
    /**
     * @param xmlPath
     * @return
     */
    private static AbstractApplicationContext mkXmlContext(String xmlPath) {
        return new FileSystemXmlApplicationContext(xmlPath);
    }
}
