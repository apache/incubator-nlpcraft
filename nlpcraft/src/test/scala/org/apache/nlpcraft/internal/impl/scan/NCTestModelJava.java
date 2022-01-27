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

package org.apache.nlpcraft.internal.impl.scan;

import org.apache.nlpcraft.NCIntent;
import org.apache.nlpcraft.NCIntentImport;
import org.apache.nlpcraft.NCIntentRef;
import org.apache.nlpcraft.NCIntentSample;
import org.apache.nlpcraft.NCIntentSampleRef;
import org.apache.nlpcraft.NCIntentTerm;
import org.apache.nlpcraft.NCModel;
import org.apache.nlpcraft.NCModelAdapter;
import org.apache.nlpcraft.NCResult;
import org.apache.nlpcraft.NCEntity;
import org.apache.nlpcraft.nlp.util.opennlp.NCTestConfigJava;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public class NCTestModelJava {
    /**
     *
     * @return
     */
    public static NCModel mkModel() {
        return
            new NCModelAdapter(NCTestConfigJava.CFG, NCTestConfigJava.EN_PIPELINE) {
                @NCIntentImport({"scan/idl.idl"})
                @NCIntent(
                    "intent=locInt term(single)~{# == 'id1'} term(list)~{# == 'id2'}[0,10] term(opt)~{# == 'id3'}?"
                )
                @NCIntentSample("What are the least performing categories for the last quarter?")
                NCResult intent(
                    @NCIntentTerm("single") NCEntity single,
                    @NCIntentTerm("list") List<NCEntity> list,
                    @NCIntentTerm("opt") Optional<NCEntity> opt
                ) {
                    return new NCResult();
                }

                @NCIntentRef("impIntId")
                @NCIntentSampleRef("scan/samples.txt")
                NCResult intentImport(
                    @NCIntentTerm("single") NCEntity single,
                    @NCIntentTerm("list") List<NCEntity> list,
                    @NCIntentTerm("opt") Optional<NCEntity> opt
                ) {
                    return new NCResult();
                }
            };
    }
}
