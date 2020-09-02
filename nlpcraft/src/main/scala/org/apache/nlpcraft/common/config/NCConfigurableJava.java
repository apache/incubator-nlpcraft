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

package org.apache.nlpcraft.common.config;

import com.typesafe.config.Config;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Quick adapter for usage in Java code.
 */
public class NCConfigurableJava {
    private static Config cfg = null;

    /**
     *
     */
    public NCConfigurableJava() {
        // No-op.
    }

    /**
     *
     * @param extCfg External config to set.
     */
    public static void setConfig(Config extCfg) {
        cfg = extCfg;
    }

    /**
     * Gets mandatory configuration property in `host:port` format.
     *
     * @param name Full configuration property path (name).
     * @return Pair of host and port from given configuration property.
     */
    public Pair<String, Integer> getHostPort(String name) {
        checkMandatory(name);

        String ep = getString(name);

        int i = ep.indexOf(':');

        if (i <= 0)
            abortWith(String.format("Invalid 'host:port' endpoint format: %s", ep));

        try {
            return new Pair<String, Integer>(ep.substring(0, i), Integer.parseInt(ep.substring(i + 1)));
        }
        catch (NumberFormatException e) {
            abortWith(String.format("Invalid 'host:port' endpoint port: %s", ep));

            throw new AssertionError();
        }
    }

    /**
     * Gets mandatory configuration property in `host:port` format.
     *
     * @param name Full configuration property path (name).
     * @param dfltHost Default host value.
     * @param dfltPort Default port value.
     * @return Pair of host and port from given configuration property or default values.
     */
    public Pair<String, Integer> getHostPortOrElse(String name, String dfltHost, int dfltPort) {
        return cfg.hasPath(name) ? getHostPort(name) :  new Pair<String, Integer>(dfltHost, dfltPort);
    }

    /**
     *
     * @param errMsgs Optional error messages.
     */
    void abortWith(String... errMsgs) {
        Logger logger = LoggerFactory.getLogger(getClass());

        Arrays.stream(errMsgs).sequential().forEach(logger::error);

        // Abort immediately.
        System.exit(1);
    }

    /**
     *
     * @param name Full configuration property path (name).
     */
    private void checkMandatory(String name) {
        if (!cfg.hasPath(name))
            abortWith(String.format("Mandatory configuration property '%s' not found.", name));
    }


    /**
     * Gets mandatory configuration property.
     *
     * @param name Full configuration property path (name).
     * @return Integer configuration property.
     */
    public int getInt(String name) {
        checkMandatory(name);

        return cfg.getInt(name);
    }

    /**
     * Gets mandatory configuration property.
     *
     * @param name Full configuration property path (name).
     * @return Boolean configuration property.
     */
    public boolean getBool(String name) {
        checkMandatory(name);

        return cfg.getBoolean(name);
    }

    /**
     * Gets mandatory configuration property.
     *
     * @param name Full configuration property path (name).
     * @return Long configuration property.
     */
    public long getLong(String name) {
        checkMandatory(name);

        return cfg.getLong(name);
    }

    /**
     * Gets optional configuration property with default fallback value.
     *
     * @param name Full configuration property path (name).
     * @param dflt Default fallback value.
     * @return String configuration property or default value.
     */
    public String getStringOrElse(String name, String dflt) {
        return cfg.hasPath(name) ? cfg.getString(name) : dflt;
    }

    /**
     * Gets mandatory configuration property.
     *
     * @param name Full configuration property path (name).
     * @return String configuration property.
     */
    public String getString(String name) {
        checkMandatory(name);

        return cfg.getString(name);
    }
}
