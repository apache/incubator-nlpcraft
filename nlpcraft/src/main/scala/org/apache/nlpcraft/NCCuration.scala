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

package org.apache.nlpcraft

/**
  * A type of rejection indicating that human curation is required. Curation is typically an indication that input
  * query is likely valid but needs a human correction like a typo fix, slang resolution, etc.
  *
  * Note that NLPCraft does not handle the curation process itself but only allows to indicate the curation
  * request by throwing this exception. Curation is a special type of rejection. The user code is responsible for
  * he actual handling of the curation logic.
  *
  * @param msg Curation message.
  * @param cause Optional cause of this exception.
  */
class NCCuration(msg: String, cause: Throwable = null) extends NCRejection(msg, cause)