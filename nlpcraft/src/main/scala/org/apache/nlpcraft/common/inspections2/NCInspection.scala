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

package org.apache.nlpcraft.common.inspections2

import io.opencensus.trace.Span

/**
 * Inspection descriptor.
 */
trait NCInspection {
    /**
     * Globally unique, internal inspection ID.
     *
     * @return
     */
    def id(): String

    /**
     * User-visible name of the inspection.
     *
     * @return
     */
    def name(): String

    /**
     * Short, one-line, description.
     *
     * @return
     */
    def synopsis(): String

    /**
     *
     * @return
     */
    def parameters(): Seq[NCInspectionParameter]

    /**
     * Full description of this inspection additionally to the synopsis.
     *
     * @return
     */
    def description(): Option[String]

    /**
     *
     * @return
     */
    def isServerSide: Boolean
}
