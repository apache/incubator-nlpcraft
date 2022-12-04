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

/**
  * Convenient builder for [[NCPipeline]] instance.
  */
class NCPipelineBuilder:
    import scala.collection.mutable.ArrayBuffer as Buf

    private var tokParser: Option[NCTokenParser] = None
    private val tokEnrichers: Buf[NCTokenEnricher] = Buf.empty
    private val entEnrichers: Buf[NCEntityEnricher] = Buf.empty
    private val entParsers: Buf[NCEntityParser] = Buf.empty
    private val tokVals: Buf[NCTokenValidator] = Buf.empty
    private val entVals: Buf[NCEntityValidator] = Buf.empty
    private val entMappers: Buf[NCEntityMapper] = Buf.empty
    private val varFilters: Buf[NCVariantFilter] = Buf.empty

    private def mkEnStemmer: NCSemanticStemmer =
        new NCSemanticStemmer:
            final private val ps: PorterStemmer = new PorterStemmer
            override def stem(txt: String): String = ps.stem(txt)

    private def mkEnOpenNLPTokenParser: NCOpenNLPTokenParser =
        new NCOpenNLPTokenParser(NCResourceReader.getPath("opennlp/en-token.bin"))

    /**
      * Adds given token enrichers to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param tokEnrichers Token enrichers to add this pipeline builder.
      */
    def withTokenEnrichers(tokEnrichers: List[NCTokenEnricher]): NCPipelineBuilder =
        require(tokEnrichers != null, "List of token enrichers cannot be null.")
        tokEnrichers.foreach(withTokenEnricher)
        this

    /**
      * Adds given token enricher to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param tokEnricher Token enricher to add this pipeline builder.
      */
    def withTokenEnricher(tokEnricher: NCTokenEnricher): NCPipelineBuilder =
        require(tokEnricher != null, "Token enricher cannot be null.")
        this.tokEnrichers += tokEnricher
        this

    /**
      * Adds given entity enrichers to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entEnrichers Entity enrichers to add this pipeline builder.
      */
    def withEntityEnrichers(entEnrichers: List[NCEntityEnricher]): NCPipelineBuilder =
        require(entEnrichers != null, "List of entity enrichers cannot be null.")
        entEnrichers.foreach(withEntityEnricher)
        this

    /**
      * Adds given entity enricher to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entEnricher Entity enricher to add this pipeline builder.
      */
    def withEntityEnricher(entEnricher: NCEntityEnricher): NCPipelineBuilder = 
        require(entEnricher != null, "Entity enricher cannot be null.")
        this.entEnrichers += entEnricher
        this

    /**
      * Adds given entity parsers to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entParsers Entity parsers to add this pipeline builder.
      */
    def withEntityParsers(entParsers: List[NCEntityParser]): NCPipelineBuilder =
        require(entParsers != null, "List of entity parsers cannot be null.")
        entParsers.foreach(withEntityParser)
        this

    /**
      * Adds given entity parser to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entParser Entity parser to add this pipeline builder.
      */
    def withEntityParser(entParser: NCEntityParser): NCPipelineBuilder =
        require(entParser != null, "Entity parser cannot be null.")
        this.entParsers += entParser
        this

    /**
      * Adds given token validators to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param tokVals Token validators to add this pipeline builder.
      */
    def withTokenValidators(tokVals: List[NCTokenValidator]): NCPipelineBuilder =
        require(tokVals != null, "List of token validators cannot be null.")
        tokVals.foreach(withTokenValidator)
        this

    /**
      * Adds given token validator to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param tokVal Token validator to add this pipeline builder.
      */
    def withTokenValidator(tokVal: NCTokenValidator): NCPipelineBuilder =
        require(tokVal != null, "Token validator cannot be null.")
        this.tokVals += tokVal
        this

    /**
      * Adds given entity validators to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entVals Entity validators to add this pipeline builder.
      */
    def withEntityValidators(entVals: List[NCEntityValidator]): NCPipelineBuilder =
        require(entVals != null, "List of entity validators cannot be null.")
        entVals.foreach(withEntityValidator)
        this

    /**
      * Adds given entity validator to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entVal Entity validator to add this pipeline builder.
      */
    def withEntityValidator(entVal: NCEntityValidator): NCPipelineBuilder =
        require(entVal != null, "Entity validator cannot be null.")
        this.entVals += entVal
        this

    /**
      * Adds given variant filters to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param varFilters Variant filters to add this pipeline builder.
      */
    def withVariantFilters(varFilters: List[NCVariantFilter]): NCPipelineBuilder =
        require(varFilters != null, "List of variant filters cannot be null.")
        varFilters.foreach(withVariantFilter)
        this

    /**
      * Adds given variant filter to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param varFilter Variant filter to add this pipeline builder.
      */
    def withVariantFilter(varFilter: NCVariantFilter): NCPipelineBuilder =
        require(varFilter != null, "Variant filter cannot be null.")
        this.varFilters ++= varFilters
        this

    /**
      * Adds given token parser to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param tokParser Token parser to add this pipeline builder.
      */
    def withTokenParser(tokParser: NCTokenParser): NCPipelineBuilder =
        require(tokParser != null, "Token parser cannot be null.")
        this.tokParser = tokParser.?
        this

    /**
      * Adds given entity mappers to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entMappers Entity mappers to add this pipeline builder.
      */
    def withEntityMappers(entMappers: List[NCEntityMapper]): NCPipelineBuilder =
        require(entMappers != null, "List of entity mappers cannot be null.")
        entMappers.foreach(withEntityMapper)
        this

    /**
      * Adds given entity mapper to the pipeline builder. Note that this method returns
      * this instance for convenient call chaining.
      *
      * @param entMapper Entity mapper to add this pipeline builder.
      */
    def withEntityMapper(entMapper: NCEntityMapper): NCPipelineBuilder =
        require(entMapper != null, "Entity mapper cannot be null.")
        this.entMappers += entMapper
        this

    /**
      *
      */
    private def setEnComponents(): Unit =
        tokParser = mkEnOpenNLPTokenParser.?
        tokEnrichers += new NCOpenNLPLemmaPosTokenEnricher(NCResourceReader.getPath("opennlp/en-pos-maxent.bin"), NCResourceReader.getPath("opennlp/en-lemmatizer.dict"))
        tokEnrichers += new NCEnStopWordsTokenEnricher
        tokEnrichers += new NCEnSwearWordsTokenEnricher(NCResourceReader.getPath("badfilter/swear_words.txt"))
        tokEnrichers += new NCEnQuotesTokenEnricher
        tokEnrichers += new NCEnDictionaryTokenEnricher
        tokEnrichers += new NCEnBracketsTokenEnricher

    /**
      * Shortcut to configure pipeline with [[NCSemanticEntityParser]].
      *
      * @param lang ISO 639-1 language code. Currently, only "en" (English) is supported.
      * @param macros Macros to use with [[NCSemanticEntityParser]].
      * @param elms Semantic elements to use with [[NCSemanticEntityParser]].
      */
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
      * Shortcut to configure pipeline with [[NCSemanticEntityParser]].
      *
      * @param lang ISO 639-1 language code. Currently, only "en" (English) is supported.
      * @param elms Semantic elements to use with [[NCSemanticEntityParser]].
      */
    def withSemantic(lang: String, elms: List[NCSemanticElement]): NCPipelineBuilder = withSemantic(lang, Map.empty, elms)

    /**
      * Shortcut to configure pipeline with [[NCSemanticEntityParser]].
      *
      * @param lang ISO 639-1 language code. Currently, only "en" (English) is supported.
      * @param mdlSrc Classpath resource, file path or URL for YAML or JSON semantic model definition file.
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
      * Builds new [[NCPipeline]] instance with previously provided components.
      */
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
