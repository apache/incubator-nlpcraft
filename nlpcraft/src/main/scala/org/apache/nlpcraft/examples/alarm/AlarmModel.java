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

package org.apache.nlpcraft.examples.alarm;

import org.apache.nlpcraft.model.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

import static java.time.temporal.ChronoUnit.*;

/**
 * Alarm example data model.
 * <p>
 * This example provides a simple "alarm clock" interface where you can ask to set the timer
 * for a specific duration from now expressed in hours, minutes and/or seconds. You can say "ping me in 3 minutes",
 * "buzz me in an hour and 15 minutes", or "set my alarm for 30 secs". When the timers is up it will
 * simply print out "BEEP BEEP BEEP" in the probe console.
 * <p>
 * See 'README.md' file in the same folder for running & testing instructions.
 */
public class AlarmModel extends NCModelFileAdapter {
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("HH'h' mm'm' ss's'").withZone(ZoneId.systemDefault());
    
    private final Timer timer = new Timer();
    
    public AlarmModel() {
        // Loading the model from the file in the classpath.
        super("org/apache/nlpcraft/examples/alarm/alarm_model.json");
    }

    /**
     * Callback on intent match.
     *
     * @param ctx Intent solver context.
     * @return Query result.
     */
    @NCIntentRef("alarm")
    @NCIntentSample({
        "Ping me in 3 minutes",
        "Buzz me in an hour and 15mins",
        "Set my alarm for 30s"
    })
    private NCResult onMatch(
        NCIntentMatch ctx,
        @NCIntentTerm("nums") List<NCToken> numToks
    ) {
        if (ctx.isAmbiguous())
            throw new NCRejection("Not exact match.");

        long unitsCnt = numToks.stream().map(tok -> (String)tok.meta("num:unit")).distinct().count();
        
        if (unitsCnt != numToks.size())
            throw new NCRejection("Ambiguous time units.");
    
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dt = now;
    
        for (NCToken num : numToks) {
            String unit = num.meta("nlpcraft:num:unit");
    
            // Skip possible fractional to simplify.
            long v = ((Double)num.meta("nlpcraft:num:from")).longValue();
            
            if (v <= 0)
                throw new NCRejection("Value must be positive: " + unit);
    
            switch (unit) {
                case "second": { dt = dt.plusSeconds(v); break; }
                case "minute": { dt = dt.plusMinutes(v); break; }
                case "hour": { dt = dt.plusHours(v); break; }
                case "day": { dt = dt.plusDays(v); break; }
                case "week": { dt = dt.plusWeeks(v); break; }
                case "month": { dt = dt.plusMonths(v); break; }
                case "year": { dt = dt.plusYears(v); break; }
        
                default:
                    // It shouldn't be assert, because 'datetime' unit can be extended.
                    throw new NCRejection("Unsupported time unit: " + unit);
            }
        }
    
        long ms = now.until(dt, MILLIS);
        
        assert ms >= 0;
    
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    System.out.println(
                        "BEEP BEEP BEEP for: " + ctx.getContext().getRequest().getNormalizedText() + ""
                    );
                }
            },
            ms
        );
    
        return NCResult.text("Timer set for: " + FMT.format(dt));
    }
}
