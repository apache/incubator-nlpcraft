/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.server.nlp.core.stanford

import java.util

import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.{CoreAnnotation, CoreLabel}
import edu.stanford.nlp.pipeline.{Annotation, Annotator}
import edu.stanford.nlp.process.AbstractTokenizer
import edu.stanford.nlp.util.TypesafeMap

import scala.language.implicitConversions
import collection.convert.ImplicitConversions._

/**
  * Custom annotator.
  *
  * Implementation is copy of edu.stanford.nlp.pipeline.TokenizerAnnotator with NCNlpTokenizer instead of default.
  */
class NCStanfordAnnotator extends Annotator {
    override def annotate(annotation: Annotation): Unit =
        if (annotation.containsKey(classOf[TextAnnotation])) {
            val text = annotation.get(classOf[TextAnnotation])

            val tokens = NCStanfordTokenizer(text).tokenize

            setNewlineStatus(tokens)
            setTokenBeginTokenEnd(tokens)

            annotation.set(classOf[TokensAnnotation], tokens)
        }
        else
            throw new RuntimeException("Tokenizer unable to find text in annotation: " + annotation)

    override def requires = new util.HashSet[Class[_ <: CoreAnnotation[_]]]()

    override def requirementsSatisfied =
        new util.HashSet[Class[_ <: CoreAnnotation[_]]](
            util.Arrays.asList(classOf[TextAnnotation],
                classOf[TokensAnnotation],
                classOf[CharacterOffsetBeginAnnotation],
                classOf[CharacterOffsetEndAnnotation],
                classOf[BeforeAnnotation],
                classOf[AfterAnnotation],
                classOf[TokenBeginAnnotation],
                classOf[TokenEndAnnotation],
                classOf[PositionAnnotation],
                classOf[IndexAnnotation],
                classOf[OriginalTextAnnotation],
                classOf[ValueAnnotation],
                classOf[IsNewlineAnnotation])
        )

    /**
      *
      * @param toks
      */
    private def setNewlineStatus(toks: util.List[CoreLabel]): Unit = // label newlines
        for (tok ← toks)
            tok.set(
                classOf[IsNewlineAnnotation],
                tok.word == AbstractTokenizer.NEWLINE_TOKEN && (tok.endPosition - tok.beginPosition == 1)
            )

    /**
      *
      * @param toks
      */
    private def setTokenBeginTokenEnd(toks: util.List[CoreLabel]): Unit =
        toks.zipWithIndex.foreach { case (tok, idx) ⇒
            tok.set(classOf[TokenBeginAnnotation], idx)
            tok.set(classOf[TokenEndAnnotation], idx + 1)
        }

    /**
      *
      * @param claxx
      * @return
      */
    private implicit def convert(claxx: Class[_]): Class[_ <: TypesafeMap.Key[Any]] =
        claxx.asInstanceOf[Class[_ <: TypesafeMap.Key[Any]]]
}
