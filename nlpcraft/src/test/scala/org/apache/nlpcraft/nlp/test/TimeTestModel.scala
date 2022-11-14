package org.apache.nlpcraft.nlp.test

import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.parsers.NCOpenNLPEntityParser
import org.apache.nlpcraft.internal.util.NCResourceReader


class TimeTestModel extends NCModel(
    NCModelConfig("nlpcraft.test.ex", "Test", "1.0"),
    pipeline = new NCPipelineBuilder().
        withSemantic("en", "time_model.yaml").
        withEntityParser(NCOpenNLPEntityParser(NCResourceReader.getPath("opennlp/en-ner-location.bin"))).
        build
):
    case class TimeData()
    case class CityTimeData(city: String)

    // Add this method to API.
    override def getIntents: List[NCIntent2[_]] = List(
        new NCIntent2[TimeData] :
            override def getId: String = "id1"
            override def tryMatch(mi: NCMatchInput): Option[TimeData] =
                val varEnts = mi.getVariant4Match.getEntities

                Option.when(varEnts.length == 1 && varEnts.count(_.getId == "x:time") == 1)(TimeData())
            override def mkResult(mi: NCMatchInput, data: TimeData): NCResult = NCResult("Asked for local") // TBI.
        ,
        new NCIntent2[CityTimeData] :
            override def getId: String = "id2"
            override def tryMatch(mi: NCMatchInput): Option[CityTimeData] =
                val varEnts = mi.getVariant4Match.getEntities
                val allEnts = mi.getAllEntities

                val cities = varEnts.filter(_.getId == "opennlp:location")
                val times = allEnts.filter(_.getId == "x:time")
                Option.when(cities.length == 1 && times.length == 1 && varEnts.forall(p => p.getId == "opennlp:location" || p.getId == "x:time"))(CityTimeData(cities.head.mkText))
            override def mkResult(mi: NCMatchInput, data: CityTimeData): NCResult = NCResult(s"Asked for ${data.city}") // TBI.
    )
