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

package org.apache.nlpcraft.nlp.parsers.impl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.parsers.*
import org.apache.nlpcraft.nlp.parsers.NCSemanticElement
import org.apache.nlpcraft.nlp.parsers.impl.NCSemanticSourceType.*

import java.io.InputStream
import java.util

/**
  *
  */
private[parsers] object NCSemanticSourceType:
    def detect(src: String): NCSemanticSourceType =
        val lc = src.toLowerCase

        if lc.endsWith(".json") || lc.endsWith(".js") then JSON
        else if lc.endsWith(".yaml") || lc.endsWith(".yml") then YAML
        else E(s"Expected `yaml` or `json` formats, but got: $src")

/**
  *
  */
private[parsers] enum NCSemanticSourceType:
    case JSON, YAML

/**
  *
  */
private[parsers] case class NCSemanticSourceData(macros: Map[String, String], elements: Seq[NCSemanticElement])

/**
  *
  */
private[parsers] object NCSemanticSourceReader:
    case class Element  (
        id: String,
        description: String,
        groups: Seq[String],
        synonyms: Set[String],
        values: Map[String, Set[String]],
        properties: Map[String, Object]
    )
    case class Source(macros: Map[String, String], elements: Seq[Element])

    private def nvlElements[T, R](seq: Seq[T], to: T => R): Seq[R] = if seq == null then Seq.empty else seq.map(to)

    private def convertElement(e: Element): NCSemanticElement =
        if e == null then null
        else
            new NCPropertyMapAdapter with NCSemanticElement:
                override val getId: String = e.id
                override val getGroups: Set[String] =
                    val gs = e.groups

                    if gs != null && gs.nonEmpty then gs.toSet else super.getGroups
                override val getValues: Map[String, Set[String]] = e.values
                override val getSynonyms: Set[String] = e.synonyms
                override val getProperties: Map[String, AnyRef] = e.properties

    /**
      *
      * @param is
      * @param typ
      */
    def read(is: InputStream, typ: NCSemanticSourceType): NCSemanticSourceData =
        val mapper =
            typ match
                case JSON => new ObjectMapper()
                case YAML => new ObjectMapper(new YAMLFactory())

        val src = mapper.
            registerModule(DefaultScalaModule).
            enable(JsonParser.Feature.ALLOW_COMMENTS).
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true).
            readValue(is, classOf[Source])

        NCSemanticSourceData(src.macros, nvlElements(src.elements, convertElement))