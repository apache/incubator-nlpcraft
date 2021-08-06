package org.apache.nlpcraft.examples.solarsystem.loaders

import org.apache.nlpcraft.model.{NCElement, NCValue, NCValueLoader}

import java.util
import scala.jdk.CollectionConverters.{SeqHasAsJava, SetHasAsJava}

trait SolarSystemValueLoader extends NCValueLoader {
    def getData: Seq[String]

    private def mkValue(v: String): NCValue =
        new NCValue {
            override def getName: String = v
            override def getSynonyms: util.List[String] = Seq(v.toLowerCase).asJava
        }

    override def load(owner: NCElement): util.Set[NCValue] = getData.map(mkValue).toSet.asJava
}
