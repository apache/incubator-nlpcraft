package org.apache.nlpcraft.model

import java.util

import org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator
import org.junit.jupiter.api.Test

import scala.collection.JavaConverters._

/**
  * Sample annotation test model.
  */
class NCIntentSampleSpecModel extends NCModelAdapter(
    "nlpcraft.sample.ann.model.test", "Sample annotation Test Model", "1.0"
) {
    private implicit def convert(s: String): NCResult = NCResult.text(s)

    override def getElements: util.Set[NCElement] = Set(mkElement("x1"), mkElement("x2")).asJava

    private def mkElement(id: String): NCElement =
        new NCElement {
            override def getId: String = id
        }

    @NCIntent("intent=intent1 term={id=='x1'}")
    @NCIntentSample(Array("x1", "x1"))
    @NCIntentSample(Array("unknown", "unknown"))
    private def onX1(ctx: NCIntentMatch): NCResult = "OK"

    @NCIntentSample(Array("x1", "x2", "x3"))
    @NCIntentSample(Array("x1", "x2"))
    @NCIntentSample(Array("x1"))
    @NCIntent("intent=intent2 term={id=='x2'}")
    private def onX2(ctx: NCIntentMatch): NCResult = "OK"
}

/**
  * Sample annotation test.
  */
class NCIntentSampleSpec {
    @Test
    def test(): Unit = {
        System.setProperty("NLPCRAFT_TEST_MODELS", "org.apache.nlpcraft.model.NCIntentSampleSpecModel")

        // Note that this validation can print validation warnings for this 'NCIntentSampleSpecModel' model.
        // Its is expected behaviour because not model is tested, but validation itself.
        NCTestAutoModelValidator.isValid()
    }
}
