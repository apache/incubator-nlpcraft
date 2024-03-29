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
  * Control flow exception to skip current intent. This exception can be thrown by the intent callback to indicate
  * that current intent should be skipped (even though it was matched and its callback was called). If there's more
  * than one intent matched the next best matching intent will be selected and its callback will be called.
  *
  * This exception becomes useful when it is hard or impossible to encode the entire matching logic using just
  * declarative IDL. In these cases the intent definition can be relaxed and the "last mile" of intent
  * matching can happen inside the intent callback's user logic. If it is determined that intent in fact does
  * not match then throwing this exception allows to try next best matching intent, if any.
  *
  * @see [[NCModel.onMatchedIntent()]]
  */
class NCIntentSkip(msg: String, cause: Throwable = null) extends NCException(msg, cause)