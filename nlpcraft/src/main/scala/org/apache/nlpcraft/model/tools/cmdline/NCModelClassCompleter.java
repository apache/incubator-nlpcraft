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

package org.apache.nlpcraft.model.tools.cmdline;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Collectors;

/**
 * Completer for model classes from classpath. Currently only JAR files are supported.
 */
class NCModelClassCompleter {
    /**
     *
     * @param jarPath Path of the JAR file.
     * @return Set of class names from the given JAR file.
     * @throws IOException Thrown in case of any I/O errors.
     */
    public Set<String> getClassNamesFromJar(String jarPath) throws IOException {
        assert jarPath != null && jarPath.toLowerCase().endsWith(".jar");

        Set<String> classNames = new HashSet<>();

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".class"))
                    classNames.add(entry.getName()
                        .replace("/", ".")
                        .replace(".class", "")
                    );
            }

            return classNames;
        }
    }

    /**
     *
     * @param cp List of classpath entries.
     * @return Set of model class name for the given classpath.
     * @throws IOException Thrown in case of any I/O errors.
     */
    public Set<String> getModelClassNamesFromClasspath(List<String> cp) throws MalformedURLException, IOException, ClassNotFoundException {
        URL[] urls = cp.stream().map(entry -> new URL("jar:file" + entry + "!/")).distinct().toArray();

        Set<String> mdlClasses = new HashSet<>();


        Set<String> classNames = getClassNamesFromJarFile(jarFile);

        try (URLClassLoader cl = URLClassLoader.newInstance(urls)) {
            for (String name : classNames) {
                classes.add(clazz);
            }
        }
        return classes;
    }
}
