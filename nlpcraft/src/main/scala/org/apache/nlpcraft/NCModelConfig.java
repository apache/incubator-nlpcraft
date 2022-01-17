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

import java.util.*;

/**
 *
 */
public class NCModelConfig extends NCPropertyMapAdapter {
    private final String id, name, ver, desc, origin;

    /**
     * @param id
     * @param name
     * @param ver
     */
    public NCModelConfig(String id, String name, String ver) {
        this(id, name, ver, null, null);
    }

    /**
     *
     * @param id
     * @param name
     * @param ver
     * @param desc
     * @param origin
     */
    public NCModelConfig(String id, String name, String ver, String desc, String origin) {
        Objects.requireNonNull(id, "Model ID cannot be null.");
        Objects.requireNonNull(name, "Model name cannot be null.");
        Objects.requireNonNull(ver, "Model version cannot be null.");

        this.id = id;
        this.name = name;
        this.ver = ver;
        this.desc = desc;
        this.origin = origin != null ? origin : getClass().getCanonicalName();
    }

    /**
     * Gets unique, <i>immutable</i> ID of this model.
     *
     * @return Unique, <i>immutable</i> ID of this model.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets descriptive name of this model.
     *
     * @return Descriptive name for this model.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version of this model using semantic versioning.
     *
     * @return A version compatible with (<a href="http://www.semver.org">www.semver.org</a>) specification.
     */
    public String getVersion() {
        return ver;
    }

    /**
     * Gets optional short model description. This can be displayed by the management tools.
     * Default implementation retusrns <code>null</code>.
     *
     * @return Optional short model description. Can return <code>null</code>.
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Gets the origin of this model like name of the class, file path or URL.
     * Default implementation return current class name.
     *
     * @return Origin of this model like name of the class, file path or URL.
     */
    public String getOrigin() {
        return origin;
    }
}
