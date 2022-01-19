package org.apache.nlpcraft.nlp.util.stanford

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.apache.nlpcraft.NCModelConfig
import org.apache.nlpcraft.nlp.token.parser.stanford.NCStanfordNLPTokenParser
import org.apache.nlpcraft.nlp.util.NCTestPipeline

import java.util.Properties

/**TODO: add test with configured models for various languages. */

final val CFG = new NCModelConfig("testId", "test", "1.0")

/**
  *
  */
final val STANFORD =
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner")
    new StanfordCoreNLP(props)

/**
  *
  */
final val EN_STANFORD_PIPELINE = NCTestPipeline(new NCStanfordNLPTokenParser(STANFORD))
