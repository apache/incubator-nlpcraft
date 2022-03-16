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

package org.apache.nlpcraft.examples.time;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.nlpcraft.NCContext;
import org.apache.nlpcraft.NCModelAdapter;
import org.apache.nlpcraft.NCModelConfig;
import org.apache.nlpcraft.NCModelPipeline;
import org.apache.nlpcraft.NCModelPipelineBuilder;
import org.apache.nlpcraft.NCRejection;
import org.apache.nlpcraft.NCRequest;
import org.apache.nlpcraft.NCResult;
import org.apache.nlpcraft.NCResultType;

import java.util.HashMap;
import java.util.Map;

public class EchoModel extends NCModelAdapter {
    /**
     *
     */
    public EchoModel() {
        super(
            new NCModelConfig("nlpcraft.echo.ex", "Echo Example Model", "1.0"),
            new NCModelPipelineBuilder().build()
        );
    }

    @Override
    public NCResult onContext(NCContext ctx) throws NCRejection {
        NCRequest req = ctx.getRequest();

        Map<String, Object> m = new HashMap<>();

        m.put("text", req.getText());
        m.put("reqId", req.getRequestId());
        m.put("receiveTimestamp", req.getReceiveTimestamp());
        m.put("userId", req.getUserId());
        m.put("reqData", req.getRequestData());
        m.put("variants", ctx.getVariants());
        m.put("tokens", ctx.getTokens());

        return new NCResult(
            new GsonBuilder().setPrettyPrinting().create().toJson(m),
            NCResultType.ASK_RESULT
        );
    }
}
