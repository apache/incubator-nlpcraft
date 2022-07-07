package org.apache.nlpcraft.examples.pizzeria

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.Properties
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.examples.pizzeria.components.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.token.enricher.NCEnStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.NCStanfordNLPEntityParser


/**
  * PizzeriaModel pipeline, based on Stanford NLP engine, including model custom components.
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

        import PizzeriaOrderExtender as Ex, EntityData as D

        new NCPipelineBuilder().
            withTokenParser(tokParser).
            withTokenEnricher(new NCEnStopWordsTokenEnricher()).
            withEntityParser(new NCStanfordNLPEntityParser(stanford, Set("number"))).
            withEntityParser(NCSemanticEntityParser(stemmer, tokParser, "pizzeria_model.yaml")).
            withEntityMappers(
                List(
                    Ex(Seq(D("ord:pizza", "ord:pizza:size")), D("ord:pizza:size", "ord:pizza:size:value")),
                    Ex(Seq(D("ord:pizza", "ord:pizza:qty"), D("ord:drink", "ord:drink:qty")), D("stanford:number", "stanford:number:nne")),
                )
            ).
            withEntityValidator(new PizzeriaOrderValidator()).
            build