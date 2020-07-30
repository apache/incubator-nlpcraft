package org.apache.nlpcraft.common.nlp.core.opennlp

import org.apache.nlpcraft.common.nlp.core.{NCNlpTokenizer, NCNlpTokenizerFactory}

/**
 * OpenNLP tokenizer factory.
 */
class NCOpenNlpTokenizerFactory extends NCNlpTokenizerFactory {
    /**
     * Creates a new tokenizer.
     */
    override def mkTokenizer(): NCNlpTokenizer = NCOpenNlpTokenizer
}
