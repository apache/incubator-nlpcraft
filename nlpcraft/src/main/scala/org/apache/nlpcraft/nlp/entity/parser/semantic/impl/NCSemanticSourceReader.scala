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
package org.apache.nlpcraft.nlp.entity.parser.semantic.impl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.nlpcraft.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.*
import org.apache.nlpcraft.nlp.entity.parser.semantic.impl.NCSemanticSourceType.*

import java.io.InputStream
import java.util
import java.util.{List as JList, Map as JMap, Set as JSet}
import scala.jdk.CollectionConverters.*

/**
  *
  */
private[impl] object NCSemanticSourceType:
    def detect(src: String): NCSemanticSourceType =
        val lc = src.toLowerCase

        if lc.endsWith(".json") || lc.endsWith(".js") then JSON
        else if lc.endsWith(".yaml") || lc.endsWith(".yml") then YAML
        else E(s"Expected `yaml` or `json` formats, but got: $src")

/**
  *
  */
private[impl] enum NCSemanticSourceType:
    case JSON, YAML

/**
  *
  */
private[impl] case class NCSemanticSourceData(macros: Map[String, String], elements: Seq[NCSemanticElement])

/**
  *
  */
private[impl] object NCSemanticSourceReader:
    case class Element  (
        id: String,
        description: String,
        groups: Seq[String],
        synonyms: Set[String],
        values: Map[String, Set[String]],
        properties: Map[String, Object]
    )
    case class Source(macros: Map[String, String], elements: Seq[Element])

    private def nvl[T](seq: Seq[T]): JList[T] = if seq == null then null else seq.asJava
    private def nvl[T](set: Set[T]): JSet[T] = if set == null then null else set.asJava
    private def nvl[T, R](seq: Seq[T], to: T => R): Seq[R] = if seq == null then null else seq.map(to)
    private def nvlValues(m: Map[String, Set[String]]): JMap[String, JSet[String]] =
        if m == null then null else m.map { (k, v) => k -> v.asJava }.asJava
    private def nvlProperties(m: Map[String, AnyRef]): JMap[String, Object] =
        if m == null then null else m.asJava

    private def convertElement(e: Element): NCSemanticElement =
        if e == null then null
        else
            new NCPropertyMapAdapter with NCSemanticElement:
                override val getId: String = e.id
                override val getGroups: JSet[String] = nvl(e.groups.toSet)
                override val getValues: JMap[String, JSet[String]] = nvlValues(e.values)
                override val getSynonyms: JSet[String] = nvl(e.synonyms)
                override val getProperties: JMap[String, AnyRef] = nvlProperties(e.properties)

    /**
      *
      * @param is
      * @param typ
      * @return
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

        NCSemanticSourceData(src.macros, nvl(src.elements, convertElement))