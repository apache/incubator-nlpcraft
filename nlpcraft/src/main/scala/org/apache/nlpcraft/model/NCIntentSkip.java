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
 * Control flow exception to skip current intent. This exception can be thrown by the intent
 * callback to indicate that current intent should be skipped (even though
 * it was matched and its callback was called). If there's more than one intent matched the next best matching intent
 * will be selected and its callback will be called.
 * <p>
 * This exception becomes useful when it is hard or impossible to encode the entire matching logic using just
 * declarative intent DSL. In these cases the intent definition can be relaxed and the "last mile" of intent
 * matching can happen inside of the intent callback's user logic. If it is determined that intent in fact does
 * not match then throwing this exception allows to try next best matching intent, if any.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/intent-matching.html">Intent Matching</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCIntent
 * @see NCIntentTerm
 * @see NCIntentRef
 * @see NCIntentMatch
 * @see NCModel#onMatchedIntent(NCIntentMatch)
 */
public class NCIntentSkip extends NCException {
    /**
     * Creates new intent skip exception.
     */
    public NCIntentSkip() {
        super("Intent skipped.");
    }

    /**
     * Creates new intent skip exception with given debug message.
     * 
     * @param msg Skip message for debug output.
     */
    public NCIntentSkip(String msg) {
        super(msg);
    }
}
