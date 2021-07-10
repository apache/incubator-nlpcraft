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

package org.apache.nlpcraft.examples.phone;

import org.apache.nlpcraft.model.*;
import java.util.*;
import java.util.stream.*;


/**
 * Phone example data model. This example simulates voice phone dialing a-la Siri or Google Assistant.
 * It doesn't actually do a phone dialing but it recognizes the recipient and returns it back. You
 * can easily add access to the a contact database and issue the call via many existing REST services.
 * <p>
 * Note that this example is using NE tokens from Google Natural Language service. 'google' provider should
 * be enabled to run this example - see 'tokenProviders' section in server configuration.
 * <p>
 * Make sure to do the following:
 * - change config for server in nlpcraft.conf to add google  (tokenProviders = “nlpcraft,google”)
 * - set GOOGLE_APPLICATION_CREDENTIALS environment variable to point to the json file with Google credentials
 * See 'README.md' file in the same folder for running and testing instructions.
 */
public class PhoneModel extends NCModelFileAdapter {
    /**
     * Initializes the model.
     */
    public PhoneModel() {
        // Load model from JSON file on the classpath.
        super("phone_model.json");
    }

    /**
     * Intent callback. Note that we disabled the conversation so the sentences like "call again!" are not
     * supported by this example (but this support can be easily added, if necessary).
     *
     * @param ctx Intent match context.
     * @return Query result.
     */
    @NCIntent("" +
        "intent=action " +
        "term={tok_id() == 'phone:act'} " +
        // Either organization, person or a phone number (or a combination of).
        "term(rcpt)={has(list('google:organization', 'google:person', 'google:phone_number'), tok_id())}[1,3]"
    )
    @NCIntentSample({
        "Call to Apple office",
        "Can you please ping John Smith?",
        "Could you dial +7 (931) 188 34 58 and ask Mike?"
    })
    NCResult onMatch(
        NCIntentMatch ctx,
        @NCIntentTerm("rcpt") List<NCToken> rcptToks
    ) {
        String rcpt = rcptToks.stream().map(tok -> {
            String txt = tok.meta("nlpcraft:nlp:origtext");

            switch (tok.getId()) {
                case "google:organization": return String.format("organization: '%s'", txt);
                case "google:person": return String.format("person: '%s'", txt);
                case "google:phone_number": return String.format("phone: '%s'", txt);

                default: {
                    assert false;

                    return null;
                }
            }
        }).sorted().collect(Collectors.joining(", "));

        // Integrate here for the actual lookup into contact database
        // and calling out with Twilio, etc.

        return NCResult.text(String.format("Calling %s", rcpt));
    }
}