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

package org.apache.nlpcraft.server.lifecycle

import org.apache.nlpcraft.common.NCE

/**
  * Server lifecycle component.
  */
trait NCServerLifecycle {
    /**
      * Called after Ignite started but before any other server components are started.
      * Default implementation is a no-op.
      */
    @throws[NCE]
    def beforeStart(): Unit = {}
    
    /**
      * Called after all server components are successfully started.
      * Default implementation is a no-op.
      */
    @throws[NCE]
    def afterStart(): Unit = {}
    
    /**
      * Called during server shutdown before any server components are stopped.
      * Default implementation is a no-op.
      */
    @throws[NCE]
    def beforeStop(): Unit = {}
    
    /**
      * Called during server shutdown after all other server components are stopped.
      * Default implementation is a no-op.
      */
    @throws[NCE]
    def afterStop(): Unit = {}
}
