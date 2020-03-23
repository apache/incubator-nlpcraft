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

package org.apache.nlpcraft.model;

import org.apache.nlpcraft.common.*;

/**
 * Exception to indicate that user input cannot be processed as is. This exception can be thrown from
 * intent callbacks.
 * <p>
 * This exception typically indicates that user has not provided enough information in the input string
 * to have it processed automatically. In most cases this means that the user's input is either too short
 * or too simple, too long or too complex, missing required context, or unrelated to requested data model.
 */
public class NCRejection extends NCException {
    /**
     * Creates new rejection exception with given message.
     *
     * @param msg Rejection message.
     */
    public NCRejection(String msg) {
        super(msg);
    }

    /**
     * Creates new rejection exception with given message and cause.
     *
     * @param msg Rejection message. 
     * @param cause Cause of this exception.
     */
    public NCRejection(String msg, Throwable cause) {
        super(msg, cause);
    }
}
