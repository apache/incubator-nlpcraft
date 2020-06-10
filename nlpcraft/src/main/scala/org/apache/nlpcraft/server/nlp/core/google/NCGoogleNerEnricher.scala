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

package org.apache.nlpcraft.server.nlp.core.google

import com.google.cloud.language.v1.{AnalyzeEntitiesRequest, AnalyzeEntitiesResponse, Document, EncodingType, Entity, EntityMention, LanguageServiceClient}
import io.opencensus.trace.Span
import org.apache.nlpcraft.common.{NCE, NCService}
import org.apache.nlpcraft.common.nlp.{NCNlpSentence, NCNlpSentenceNote}
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.nlp.core.NCNlpNerEnricher

import scala.collection.JavaConverters._

/**
  * Google language cloud service NER enricher.
  */
object NCGoogleNerEnricher extends NCService with NCNlpNerEnricher with NCIgniteInstance {
    @volatile private var srv: LanguageServiceClient = _

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        try {
            srv = LanguageServiceClient.create()

            logger.trace("Google Natural Language client started.")

            // Credential file validation occurs when first request sent.
            ask("Hi!")

            logger.trace("Google Natural Language validation request processed.")
        }
        catch {
            case e: Exception ⇒
                throw new NCE(
                    "Google Natural Language may not be configured correctly. " +
                    "Make sure that environment variable 'GOOGLE_APPLICATION_CREDENTIALS' points " +
                    "to a valid Google JSON credential file.",
                    e
                )
        }

        super.start()
    }

    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        if (srv != null)
            srv.close()
        
        super.stop()
    }

    override def enrich(ns: NCNlpSentence, enabledBuiltInTokens: Set[String], parent: Span = null): Unit =
        startScopedSpan("enrich", parent, "srvReqId" → ns.srvReqId, "txt" → ns.text) { _ ⇒
            try {
                val resp = ask(ns.text)
    
                case class Holder(entityType: String, entity: Entity, mention: EntityMention, salience: Double)
    
                resp.getEntitiesList.asScala.flatMap(e ⇒ {
                    val typLc = e.getType.toString.toLowerCase
    
                    if (e.getMentionsList != null && enabledBuiltInTokens.contains(typLc)) {
                        e.getMentionsList.asScala.flatMap(m ⇒ {
                            val span = m.getText
    
                            val start = span.getBeginOffset
    
                            val t1Opt = ns.find(_.startCharIndex == start)
                            val t2Opt = ns.find(_.endCharIndex == start + span.getContent.length)
    
                            if (t1Opt.isEmpty || t2Opt.isEmpty)
                                Seq.empty
                            else {
                                val from = t1Opt.get.startCharIndex
                                val to = t2Opt.get.endCharIndex
    
                                if (m.getText.getContent == e.getName)
                                    ns.
                                        filter(t ⇒ t.startCharIndex >= from && t.endCharIndex <= to).
                                        map(t ⇒ t → Holder(typLc, e, m, e.getSalience))
                                else
                                    Seq.empty
                            }
                        })
                    }
                    else
                        Seq.empty
                }).groupBy { case (_, h) ⇒ h }.map { case (h, seq) ⇒ h → seq.map { case (t, _) ⇒ t } }.
                    foreach { case (h, toks) ⇒
                        // Pure java style used to avoid serialization problems with scala proxies classes.
                        val beginOffsets = new java.util.ArrayList[Int]()
                        val contents = new java.util.ArrayList[String]()
                        val types = new java.util.ArrayList[String]()
    
                        h.entity.getMentionsList.asScala.foreach(p ⇒ {
                            beginOffsets.add(p.getText.getBeginOffset)
                            contents.add(p.getText.getContent)
                            types.add(p.getType.toString)
                        })
    
                        val note =
                            NCNlpSentenceNote(
                                toks.map(_.index),
                                s"google:${h.entityType}",
                                "meta" → {
                                    // To be sure that it is serializable map.
                                    val meta = new java.util.HashMap[String, String]()
    
                                    meta.putAll(h.entity.getMetadataMap)
    
                                    meta
                                },
                                "salience" → h.salience,
                                "mentionsBeginOffsets" → beginOffsets,
                                "mentionsContents" → contents,
                                "mentionsTypes" → types
                            )
    
                        toks.foreach(_.add(note))
                    }
            }
            catch {
                case e: Exception ⇒ throw new NCE("Google Natural Language request failed.", e)
            }
        }

    /**
      *
      * @param txt
      * @return
      */
    private def ask(txt: String): AnalyzeEntitiesResponse =
        srv.analyzeEntities(
            AnalyzeEntitiesRequest.
                newBuilder.
                setDocument(
                    Document.
                        newBuilder.
                        setContent(txt).
                        setType(Document.Type.PLAIN_TEXT).
                        build
                ).
                setEncodingType(EncodingType.UTF8).
                build
        )
}
