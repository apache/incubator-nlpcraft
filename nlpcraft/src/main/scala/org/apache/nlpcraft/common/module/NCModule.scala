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

package org.apache.nlpcraft.common.module

import org.apache.nlpcraft.common.{NCE, U}

/**
 *
 */
object NCModule extends Enumeration {
    final val PROPERTY = "NLPCRAFT_MODULE"

    type NCModule = Value

    val SERVER, PROBE, CLI: Value = Value

    def getCurrent: NCModule = NCModule.withName(U.sysEnv(PROPERTY).getOrElse(throw new NCE(s"Module is not set: $PROPERTY")))
    def setCurrent(m: NCModule): Unit = System.setProperty(NCModule.PROPERTY, m.toString)
}

