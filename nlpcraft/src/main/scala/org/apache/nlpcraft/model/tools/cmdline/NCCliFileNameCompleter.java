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

import org.jline.builtins.Styles;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.OSUtils;
import org.jline.utils.StyleResolver;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * File name completer heavily based on JLine3 internal implementation.
 */
class NCCliFileNameCompleter
{
    private static final StyleResolver resolver = Styles.lsStyle();

    /**
     *
     * @param reader Line reader for JLine.
     * @param pathBuf Current path string.
     * @param candidates List of completion candidates to fill.
     */
    void fillCandidates(LineReader reader, String pathBuf, final List<Candidate> candidates) {
        Path curPath;
        String curBuf;

        String sep = getSeparator(reader.isSet(LineReader.Option.USE_FORWARD_SLASH));

        int lastSep = pathBuf.lastIndexOf(sep);
        
        try {
            if (lastSep >= 0) {
                curBuf = pathBuf.substring(0, lastSep + 1);

                if (curBuf.startsWith("~")) {
                    if (curBuf.startsWith("~" + sep))
                        curPath = getUserHome().resolve(curBuf.substring(2));
                    else
                        curPath = getUserHome().getParent().resolve(curBuf.substring(1));
                }
                else
                    curPath = getUserDir().resolve(curBuf);
            }
            else {
                curBuf = "";
                curPath = getUserDir();
            }

            try (DirectoryStream<Path> dir = Files.newDirectoryStream(curPath, this::accept)) {
                dir.forEach(path -> {
                    String value = curBuf + path.getFileName().toString();

                    if (Files.isDirectory(path)) {
                        candidates.add(
                            new Candidate(
                                value + (reader.isSet(LineReader.Option.AUTO_PARAM_SLASH) ? sep : ""),
                                getDisplay(reader.getTerminal(), path, resolver, sep),
                                null,
                                null,
                                reader.isSet(LineReader.Option.AUTO_REMOVE_SLASH) ? sep : null,
                                null,
                                false
                            )
                        );
                    } else {
                        candidates.add(
                            new Candidate(
                                value,
                                getDisplay(reader.getTerminal(), path, resolver, sep),
                                null,
                                null,
                                null,
                                null,
                                true
                            )
                        );
                    }
                });
            }
            catch (IOException e) {
                // Ignore.
            }
        }
        catch (Exception e) {
            // Ignore.
        }
    }

    /**
     *
     * @param path Path to check.
     */
    private boolean accept(Path path) {
        try {
            return !Files.isHidden(path) && Files.isReadable(path);
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     *
     */
    private Path getUserDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    /**
     *
     */
    private Path getUserHome() {
        return Paths.get(System.getProperty("user.home"));
    }

    /**
     *
     * @param useForwardSlash
     */
    private String getSeparator(boolean useForwardSlash) {
        return useForwardSlash ? "/" : getUserDir().getFileSystem().getSeparator();
    }

    /**
     *
     * @param term JLine terminal.
     * @param path Path.
     * @param resolver Style resolver.
     * @param sep Path separator.
     */
    private String getDisplay(Terminal term, Path path, StyleResolver resolver, String sep) {
        AttributedStringBuilder sb = new AttributedStringBuilder();

        String name = path.getFileName().toString();

        int idx = name.lastIndexOf(".");

        String type = idx != -1 ? ".*" + name.substring(idx): null;

        if (Files.isSymbolicLink(path))
            sb.styled(resolver.resolve(".ln"), name).append("@");
        else if (Files.isDirectory(path))
            sb.styled(resolver.resolve(".di"), name).append(sep);
        else if (Files.isExecutable(path) && !OSUtils.IS_WINDOWS)
            sb.styled(resolver.resolve(".ex"), name).append("*");
        else if (type != null && resolver.resolve(type).getStyle() != 0)
            sb.styled(resolver.resolve(type), name);
        else if (Files.isRegularFile(path))
            sb.styled(resolver.resolve(".fi"), name);
        else
            sb.append(name);

        return sb.toAnsi(term);
    }
}
