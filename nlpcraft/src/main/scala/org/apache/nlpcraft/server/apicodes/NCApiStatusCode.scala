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

package org.apache.nlpcraft.server.apicodes

import scala.language.implicitConversions

/**
  * Enumeration for all APIs status codes.
  */
object NCApiStatusCode extends Enumeration {
    type NCApiStatusCode = Value

    // API codes.
    val API_OK: Value = Value
    
    // Query state machine status.
    val QRY_ENLISTED: Value = Value // Query has been enlisted for processing.
    val QRY_READY: Value = Value // Query final result is ready.

    // Support string conversion.
    implicit def m1(status: NCApiStatusCode): String = status.toString
}
