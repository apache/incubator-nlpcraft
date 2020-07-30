package org.apache.nlpcraft.common.nlp.core.stanford

import org.apache.nlpcraft.common.nlp.core.{NCNlpTokenizerFactory, NCNlpTokenizer}

/**
 * Stanford tokenizer factory.
 */
class NCStanfordTokenizerFactory extends NCNlpTokenizerFactory {
    /**
     * Creates a new tokenizer.
     */
    override def mkTokenizer(): NCNlpTokenizer = NCStanfordTokenizer
}
