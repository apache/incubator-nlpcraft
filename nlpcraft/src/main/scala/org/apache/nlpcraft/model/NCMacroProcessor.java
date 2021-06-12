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

import org.apache.nlpcraft.common.NCException;
import org.apache.nlpcraft.common.makro.NCMacroJavaParserTrait;

import java.util.Set;

/**
 * Standalone synonym macro DSL processor.
 * <p>
 * This processor provides the same macro support as the built-in macro support in data models.
 * It is a general purpose macro-processor and it can be used standalone when testing synonyms,
 * developing NERs, visualizing synonyms in toolchains, etc.
 * <p>
 * Read full documentation on synonym macro DSL in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples">examples</a>.
 */
public class NCMacroProcessor {
    private final NCMacroJavaParserTrait impl = mkImpl();

    // Need to do that in order to avoid Javadoc failures due to mixed Scala/Java project.
    private static NCMacroJavaParserTrait mkImpl() {
        try {
            return (NCMacroJavaParserTrait)Class.forName("org.apache.nlpcraft.common.makro.NCMacroJavaParser")
                .getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            throw new NCException("Error initializing object of type: org.apache.nlpcraft.common.makro.NCMacroJavaParser", e);
        }
    }

    /**
     * Expands given macro DSL string.
     * <p>
     * Read full documentation on synonym macro DSL in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
     * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft-examples">examples</a>.
     *
     * @param s Macro DSL string to expand.
     * @return Set of macro expansions for a given macro DSL string.
     */
    public Set<String> expand(String s) {
        return impl.expand(s);
    }

    /**
     * Adds or overrides given macro.
     *
     * @param name Macro name (typically an upper case string).
     *     It must start with '&lt;' and end with '&gt;' symbol.
     * @param macro Value of the macro (any arbitrary string).
     * @return {@code true} if an existing macro was overridden, {@code false} otherwise.
     */
    public boolean addMacro(String name, String macro) {
        boolean f = impl.hasMacro(name);

        impl.addMacro(name, macro);

        return f;
    }

    /**
     * Removes macro.
     *
     * @param name Name of the macro to remove.
     * @return {@code true} if given macro was indeed found and removed, {@code false} otherwise.
     */
    public boolean removeMacro(String name) {
        boolean f = impl.hasMacro(name);

        impl.removeMacro(name);

        return f;
    }

    /**
     * Tests whether this processor has given macro.
     *
     * @param name Name of the macro to test.
     * @return {@code true} if macro was found, {@code false} otherwise.
     */
    public boolean hasMacro(String name) {
        return impl.hasMacro(name);
    }
}
