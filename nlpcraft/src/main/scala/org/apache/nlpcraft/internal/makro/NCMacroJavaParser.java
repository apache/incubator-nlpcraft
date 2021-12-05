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

package org.apache.nlpcraft.internal.makro;

import java.util.Set;

/**
 * Java adapter for macro parser (so that Java reflection could work).
 */
public class NCMacroJavaParser implements NCMacroJavaParserTrait {
    private final NCMacroParser impl = new NCMacroParser();

    /**
     * Expands given macro DSL string.
     *
     * @param s Macro DSL string to expand.
     * @return Set of macro expansions for a given macro DSL string.
     */
    public Set<String> expand(String s) {
        return impl.expandJava(s);
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
