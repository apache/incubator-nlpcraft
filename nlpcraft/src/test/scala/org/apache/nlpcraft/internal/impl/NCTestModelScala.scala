package org.apache.nlpcraft.internal.impl

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.util.opennlp.*
import org.junit.jupiter.api.Test

object NCTestModelScala:
    @NCIntentImport(Array("scan/idl.idl"))
    object NCTestModelScalaObj extends NCModel:
        override def getConfig: NCModelConfig = CFG
        override def getPipeline: NCModelPipeline = EN_PIPELINE

        @NCIntent("intent=intent1 term(single)~{# == 'id1'} term(list)~{# == 'id2'}[0,10] term(opt)~{# == 'id3'}?")
        @NCIntentSample(Array("What are the least performing categories for the last quarter?"))
        def intent1(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: Seq[NCEntity], // scala Seq.
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = new NCResult()

        @NCIntentRef("intent2")
        @NCIntentSampleRef("scan/samples.txt")
        def intent2(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity], // scala List.
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ): NCResult = new NCResult()

    @NCIntentImport(Array("scan/idl.idl"))
    class NCTestModelScalaClass extends NCModel:
        override def getConfig: NCModelConfig = CFG
        override def getPipeline: NCModelPipeline = EN_PIPELINE

        @NCIntent("intent=intent1 term(single)~{# == 'id1'} term(list)~{# == 'id2'}[0,10] term(opt)~{# == 'id3'}?")
        @NCIntentSample(Array("What are the least performing categories for the last quarter?"))
        def intent1(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: Seq[NCEntity], // scala Seq.
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ) = new NCResult()

        @NCIntentRef("intent2")
        @NCIntentSampleRef("scan/samples.txt")
        def intent2(
            @NCIntentTerm("single") single: NCEntity,
            @NCIntentTerm("list") list: List[NCEntity], // scala List.
            @NCIntentTerm("opt") opt: Option[NCEntity]
        ) = new NCResult()

    def mkModel: NCModel =
        new NCModelAdapter(CFG, EN_PIPELINE):
            @NCIntentImport(Array("scan/idl.idl"))
            @NCIntent("intent=intent1 term(single)~{# == 'id1'} term(list)~{# == 'id2'}[0,10] term(opt)~{# == 'id3'}?")
            @NCIntentSample(Array("What are the least performing categories for the last quarter?"))
            def intent1(
                @NCIntentTerm("single") single: NCEntity,
                @NCIntentTerm("list") list: Seq[NCEntity], // scala Seq.
                @NCIntentTerm("opt") opt: Option[NCEntity]
            ): NCResult = new NCResult()

            @NCIntentRef("intent2")
            @NCIntentSampleRef("scan/samples.txt")
            def intent2(
                @NCIntentTerm("single") single: NCEntity,
                @NCIntentTerm("list") list: List[NCEntity], // scala List.
                @NCIntentTerm("opt") opt: Option[NCEntity]
            ): NCResult = new NCResult()