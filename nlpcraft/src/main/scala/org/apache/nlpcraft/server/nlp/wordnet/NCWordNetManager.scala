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

package org.apache.nlpcraft.server.nlp.wordnet

import io.opencensus.trace.Span
import net.sf.extjwnl.data.POS._
import net.sf.extjwnl.data.{IndexWord, POS, PointerType}
import net.sf.extjwnl.dictionary.{Dictionary, MorphologicalProcessor}
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.NCService

import scala.collection.JavaConverters._

/**
  * WordNet manager.
  */
object NCWordNetManager extends NCService {
    @volatile private var dic: Dictionary = _
    @volatile private var morph: MorphologicalProcessor = _
    
    private def pennPos2WordNet(pennPos: String): Option[POS] =
        pennPos.head match {
            case 'N' ⇒ Some(NOUN)
            case 'V' ⇒ Some(VERB)
            case 'J' ⇒ Some(ADJECTIVE)
            case 'R' ⇒ Some(ADVERB)
            
            case _ ⇒ None
        }
    
    // Process WordNet formatted multi-word entries (they are split with '_').
    private def normalize(str: String) = str.replaceAll("_", " ")
    
    // Converts words.
    private def convert(str: String, initPos: POS, targetPos: POS): Seq[String] = {
        val word = dic.getIndexWord(initPos, str)
        
        if (word != null)
            word.getSenses.asScala.flatMap(synset ⇒
                synset.getPointers(PointerType.DERIVATION).asScala.flatMap(p ⇒ {
                    val trg = p.getTargetSynset

                    if (trg.getPOS == targetPos)
                        trg.getWords.asScala.map(p ⇒ normalize(p.getLemma))
                    else
                        Seq.empty
                })
            ).distinct
        else
            Seq.empty[String]
    }

    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        dic =  Dictionary.getDefaultResourceInstance
        morph = dic.getMorphologicalProcessor

        super.start()
    }
    
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }
    
    /**
      * Gets a sequence of possible nouns relatives for the given adjective.
      *
      * @param adj An adjective to match.
      * @return A number of possible noun relatives.
      */
    def getNNsForJJ(adj: String): Seq[String] = convert(adj, ADJECTIVE, NOUN)
    
    /**
      * Gets a sequence of possible adjective relatives for the given noun.
      *
      * @param noun A noun to match.
      * @return A number of possible adjective relatives.
      */
    def getJJsForNN(noun: String): Seq[String] = convert(noun, NOUN, ADJECTIVE)
    
    /**
      * Gets base form using more precision method.
      *
      * It drops base form like 'Alice'→'louse', 'God'→'od' and 'better'→'well'
      * which produced by WordNet if the exact base form not found.
      *
      * @param lemma Lemma to get a WordNet base form.
      * @param pennPos Lemma's Penn Treebank POS tag.
      */
    def getBaseForm(lemma: String, pennPos: String, syns: Set[String] = null): String =
        pennPos2WordNet(pennPos) match {
            case Some(wnPos) ⇒
                morph.lookupBaseForm(wnPos, lemma) match {
                    case wnWord: IndexWord ⇒
                        val wnLemma = wnWord.getLemma
                        val synonyms = if (syns == null) getSynonyms(lemma, pennPos).flatten.toSet else syns
                        
                        if (synonyms.contains(wnLemma))
                            wnLemma
                        else
                            lemma
                    case null ⇒ lemma
                }
                
            // For unsupported POS tags - return the input lemma.
            case None ⇒ lemma
        }
    
    /**
      * Gets synonyms for given lemma and its POS tag.
      *
      * @param lemma Lemma to find synonyms for.
      * @param pennPos Lemma's Penn Treebank POS tag.
      */
    def getSynonyms(lemma: String, pennPos: String): Seq[Seq[String]] = {
        val res: Seq[Seq[String]] = pennPos2WordNet(pennPos) match {
            case Some(wnPos) ⇒
                val wnWord = dic.lookupIndexWord(wnPos, lemma)
                
                if (wnWord == null)
                    Seq.empty
                else
                    wnWord.getSynsetOffsets match {
                        case synsOffs: Array[Long] ⇒
                            synsOffs.
                                map(dic.getSynsetAt(wnPos, _)).
                                filter(_.getPOS == wnPos).
                                map(
                                    _.getWords.asScala.
                                        map(_.getLemma.toLowerCase).
                                        filter(_ != lemma).
                                        map(normalize).toSeq
                                )
                        
                        case null ⇒ Seq.empty
                    }
                
            // Invalid POS.
            case None ⇒ Seq.empty
        }
        
        res.filter(_.nonEmpty)
    }
}
