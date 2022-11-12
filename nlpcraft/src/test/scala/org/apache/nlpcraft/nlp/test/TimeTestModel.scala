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
            override def tryMatch(ctx: NCContext, v: NCVariant): Option[TimeData] =
                Option.when(v.getEntities.length == 1 && v.getEntities.count(_.getId == "x:time") == 1)(TimeData())
            override def mkResult(ctx: NCContext, data: TimeData): NCResult = NCResult("Asked for local") // TBI.
        ,
        new NCIntent2[CityTimeData] :
            override def tryMatch(ctx: NCContext, v: NCVariant): Option[CityTimeData] =
                val cities = v.getEntities.filter(_.getId == "opennlp:location")
                val times = v.getEntities.filter(_.getId == "x:time")
                Option.when(v.getEntities.length == 2 && cities.length == 1 && times.length == 1)(CityTimeData(cities.head.mkText))
            override def mkResult(ctx: NCContext, data: CityTimeData): NCResult = NCResult(s"Asked for ${data.city}") // TBI.
    )
