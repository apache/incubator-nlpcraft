package org.apache.nlpcraft.examples.pizzeria.components

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.entity.parser.stanford.NCStanfordNLPEntityParser
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.enrichers.NCEnStopWordsTokenEnricher
import org.apache.nlpcraft.nlp.parsers.{NCSemanticEntityParser, NCSemanticStemmer}

import java.util.Properties

/**
  * PizzeriaModel pipeline, based on Stanford NLP engine, including model custom components. */
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

        import PizzeriaOrderMapperDesc as D

        new NCPipelineBuilder().
            withTokenParser(tokParser).
            withTokenEnricher(new NCEnStopWordsTokenEnricher()).
            withEntityParser(new NCStanfordNLPEntityParser(stanford, Set("number"))).
            withEntityParser(NCSemanticEntityParser(stemmer, tokParser, "pizzeria_model.yaml")).
            withEntityMapper(PizzeriaOrderMapper(extra = D("ord:pizza:size", "ord:pizza:size:value"), dests = D("ord:pizza", "ord:pizza:size"))).
            withEntityMapper(PizzeriaOrderMapper(extra = D("stanford:number", "stanford:number:nne"), dests = D("ord:pizza", "ord:pizza:qty"), D("ord:drink", "ord:drink:qty"))).
            withEntityValidator(new PizzeriaOrderValidator()).
            build