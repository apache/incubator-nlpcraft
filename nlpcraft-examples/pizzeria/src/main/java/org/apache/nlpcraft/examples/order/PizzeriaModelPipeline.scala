package org.apache.nlpcraft.examples.order

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.examples.order.components.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.NCStanfordNLPEntityParser
import org.apache.nlpcraft.nlp.token.enricher.NCEnStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser
import org.apache.nlpcraft.*

import scala.jdk.CollectionConverters.*
import java.util.Properties

/**
  *
  */
object PizzeriaModelPipeline:
    val PIPELINE: NCPipeline =
        val stanford =
            val props = new Properties()
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
            new StanfordCoreNLP(props)
        val tokParser = new NCStanfordNLPTokenParser(stanford)
        val stemmer = new NCSemanticStemmer():
            private val ps = new PorterStemmer
            override def stem(txt: String): String = ps.synchronized { ps.stem(txt) }

        import ElementExtender as Ex
        import EntityData as D

        new NCPipelineBuilder().
            withTokenParser(tokParser).
            withTokenEnricher(new NCEnStopWordsTokenEnricher()).
            withEntityParser(new NCStanfordNLPEntityParser(stanford, "number")).
            withEntityParser(new NCSemanticEntityParser(stemmer, tokParser, "pizzeria_model.yaml")).
            withEntityMappers(
                Seq(
                    Ex(Seq(D("ord:pizza", "ord:pizza:size")), D("ord:pizza:size", "ord:pizza:size:value")),
                    Ex(Seq(D("ord:pizza", "ord:pizza:qty"), D("ord:drink", "ord:drink:qty")), D("stanford:number", "stanford:number:nne")),
                ).asJava
            ).
            withEntityValidator(new RequestValidator()).
            build()
