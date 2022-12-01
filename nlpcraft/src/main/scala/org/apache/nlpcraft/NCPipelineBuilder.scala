/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft

import opennlp.tools.stemmer.PorterStemmer
import org.apache.nlpcraft.internal.util.NCResourceReader
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.enrichers.*

import java.util.Objects

class NCPipelineBuilder:
    import scala.collection.mutable.ArrayBuffer as Buf

    private var tokParser: Option[NCTokenParser] = None
    private val tokEnrichers: Buf[NCTokenEnricher] = Buf.empty
    private val entEnrichers: Buf[NCEntityEnricher] = Buf.empty
    private val entParsers: Buf[NCEntityParser] = Buf.empty
    private val tokVals: Buf[NCTokenValidator] = Buf.empty
    private val entVals: Buf[NCEntityValidator] = Buf.empty
    private val entMappers: Buf[NCEntityMapper] = Buf.empty
    private var varFilters: Buf[NCVariantFilter] = Buf.empty

    /**
      *
      * @return */
    private def mkEnStemmer: NCSemanticStemmer =
        new NCSemanticStemmer:
            final private val ps: PorterStemmer = new PorterStemmer
            override def stem(txt: String): String = ps.stem(txt)

    private def mkEnOpenNLPTokenParser: NCOpenNLPTokenParser =
        new NCOpenNLPTokenParser(NCResourceReader.getPath("opennlp/en-token.bin"))

    /**
      * @param tokEnrichers
      * @return This instance for call chaining. */
    def withTokenEnrichers(tokEnrichers: List[NCTokenEnricher]): NCPipelineBuilder =
        require(tokEnrichers != null, "List of token enrichers cannot be null.")
        tokEnrichers.foreach((p: NCTokenEnricher) => require(p != null, "Token enricher cannot be null."))
        this.tokEnrichers ++= tokEnrichers
        this

    /**
      * @param tokEnricher
      * @return This instance for call chaining. */
    def withTokenEnricher(tokEnricher: NCTokenEnricher): NCPipelineBuilder =
        require(tokEnricher != null, "Token enricher cannot be null.")
        this.tokEnrichers += tokEnricher
        this

    /**
      * @param entEnrichers
      * @return This instance for call chaining. */
    def withEntityEnrichers(entEnrichers: List[NCEntityEnricher]): NCPipelineBuilder =
        require(entEnrichers != null, "List of entity enrichers cannot be null.")
        entEnrichers.foreach((p: NCEntityEnricher) => require(p != null, "Entity enrichers cannot be null."))
        this.entEnrichers ++= entEnrichers
        this

    /**
      * @param entEnricher
      * @return This instance for call chaining. */
    def withEntityEnricher(entEnricher: NCEntityEnricher): NCPipelineBuilder = 
        require(entEnricher != null, "Entity enricher cannot be null.")
        this.entEnrichers += entEnricher
        this

    /**
      * @param entParsers
      * @return This instance for call chaining. */
    def withEntityParsers(entParsers: List[NCEntityParser]): NCPipelineBuilder =
        require(entParsers != null, "List of entity parsers cannot be null.")
        entParsers.foreach((p: NCEntityParser) => require(p != null, "Entity parser cannot be null."))
        this.entParsers ++= entParsers
        this

    /**
      * @param entParser
      * @return This instance for call chaining. */
    def withEntityParser(entParser: NCEntityParser): NCPipelineBuilder =
        require(entParser != null, "Entity parser cannot be null.")
        this.entParsers += entParser
        this

    /**
      * @param tokVals
      * @return This instance for call chaining. */
    def withTokenValidators(tokVals: List[NCTokenValidator]): NCPipelineBuilder =
        require(tokVals != null, "List of token validators cannot be null.")
        tokVals.foreach((p: NCTokenValidator) => require(p != null, "Token validator cannot be null."))
        this.tokVals ++= tokVals
        this


    /**
      * @param tokVal
      * @return This instance for call chaining. */
    def withTokenValidator(tokVal: NCTokenValidator): NCPipelineBuilder =
        require(tokVal != null, "Token validator cannot be null.")
        this.tokVals += tokVal
        this

    /**
      * @param entVals
      * @return This instance for call chaining. */
    def withEntityValidators(entVals: List[NCEntityValidator]): NCPipelineBuilder =
        require(entVals != null, "List of entity validators cannot be null.")
        entVals.foreach((p: NCEntityValidator) => require(p != null, "Entity validators cannot be null."))
        this.entVals ++= entVals
        this

    /**
      * @param entVal
      * @return This instance for call chaining. */
    def withEntityValidator(entVal: NCEntityValidator): NCPipelineBuilder =
        require(entVal != null, "Entity validator cannot be null.")
        this.entVals += entVal
        this

    /**
      * @param varFilters
      * @return This instance for call chaining. */
    def withVariantFilters(varFilters: List[NCVariantFilter]): NCPipelineBuilder =
        this.varFilters ++= varFilters
        this

    /**
      *
      * @param tokParser
      * @return */
    def withTokenParser(tokParser: NCTokenParser): NCPipelineBuilder =
        require(tokParser != null, "Token parser cannot be null.")
        this.tokParser = tokParser.?
        this

    /**
      *
      * @param entMappers
      * @return This instance for call chaining.
      */
    def withEntityMappers(entMappers: List[NCEntityMapper]): NCPipelineBuilder =
        require(entMappers != null, "List of entity mappers cannot be null.")
        entMappers.foreach((p: NCEntityMapper) => require(p != null, "Entity mapper cannot be null."))
        this.entMappers ++= entMappers
        this

    /**
      * @param entMapper
      * @return This instance for call chaining. */
    def withEntityMapper(entMapper: NCEntityMapper): NCPipelineBuilder =
        require(entMapper != null, "Entity mapper cannot be null.")
        this.entMappers += entMapper
        this

    /**
      * */
    private def setEnComponents(): Unit =
        tokParser = mkEnOpenNLPTokenParser.?
        tokEnrichers += new NCOpenNLPLemmaPosTokenEnricher(NCResourceReader.getPath("opennlp/en-pos-maxent.bin"), NCResourceReader.getPath("opennlp/en-lemmatizer.dict"))
        tokEnrichers += new NCEnStopWordsTokenEnricher
        tokEnrichers += new NCEnSwearWordsTokenEnricher(NCResourceReader.getPath("badfilter/swear_words.txt"))
        tokEnrichers += new NCEnQuotesTokenEnricher
        tokEnrichers += new NCEnDictionaryTokenEnricher
        tokEnrichers += new NCEnBracketsTokenEnricher

    /**
      *
      * @param lang
      * @param macros
      * @param elms
      * @return */
    def withSemantic(lang: String, macros: Map[String, String], elms: List[NCSemanticElement]): NCPipelineBuilder =
        require(lang != null, "Language cannot be null.")
        require(macros != null, "Macros elements cannot be null.")
        require(elms != null, "Model elements cannot be null.")
        require(macros != null, "Macros cannot be null.")
        require(elms.nonEmpty, "Model elements cannot be empty.")

        lang.toUpperCase match
            case "EN" =>
                setEnComponents()
                entParsers += NCSemanticEntityParser(mkEnStemmer, mkEnOpenNLPTokenParser, macros, elms)
            case _ => require(false, s"Unsupported language: $lang")
        this

    /**
      *
      * @param lang
      * @param elms
      * @return */
    def withSemantic(lang: String, elms: List[NCSemanticElement]): NCPipelineBuilder = withSemantic(lang, Map.empty, elms)

    /**
      *
      * @param lang
      * @param mdlSrc
      */
    def withSemantic(lang: String, mdlSrc: String): NCPipelineBuilder =
        require(lang != null, "Language cannot be null.")
        require(mdlSrc != null, "Model source cannot be null.")
        lang.toUpperCase match
            case "EN" =>
                setEnComponents()
                this.entParsers += NCSemanticEntityParser(mkEnStemmer, mkEnOpenNLPTokenParser, mdlSrc)
            case _ => require(false, s"Unsupported language: $lang")
        this


    /**
      * @return */
    def build: NCPipeline =
        require(tokParser.nonEmpty, "Token parser must be defined.")
        require(entParsers.nonEmpty, "At least one entity parser must be defined.")

        new NCPipeline():
            override def getTokenParser: NCTokenParser = tokParser.get
            override def getTokenEnrichers: List[NCTokenEnricher] = tokEnrichers.toList
            override def getEntityEnrichers: List[NCEntityEnricher] = entEnrichers.toList
            override def getEntityParsers: List[NCEntityParser] = entParsers.toList
            override def getTokenValidators: List[NCTokenValidator] = tokVals.toList
            override def getEntityValidators: List[NCEntityValidator] = entVals.toList
            override def getVariantFilters: List[NCVariantFilter] = varFilters.toList
            override def getEntityMappers: List[NCEntityMapper] = entMappers.toList
