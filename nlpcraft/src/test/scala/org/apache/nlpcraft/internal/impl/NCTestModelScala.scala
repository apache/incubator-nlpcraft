package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.Test

object NCTestModelScala:
    def mkModel: NCModel =
        new NCModelAdapter(CFG, EN_PIPELINE):
            @NCIntent("intent=intent1 term(single)~{# == 'id1'} term(list)~{# == 'id2'}}[0,7] term(opt)~{# == 'id3'}?")
            @NCIntentSample(Array("What are the least performing categories for the last quarter?"))
            def intent1(
                @NCIntentTerm("single") single: NCToken,
                @NCIntentTerm("list") list: Seq[NCToken], // scala Seq.
                @NCIntentTerm("opt") opt: Option[NCToken]
            ): NCResult = new NCResult()

            @NCIntentRef("scan/idl.idl/intent2") // TODO:
            @NCIntentSampleRef("scan/samples.txt")
            def intent2(
                @NCIntentTerm("single") single: NCToken,
                @NCIntentTerm("list") list: List[NCToken], // scala List.
                @NCIntentTerm("opt") opt: Option[NCToken]
            ): NCResult = new NCResult()