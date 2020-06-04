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

package org.apache.nlpcraft.model.impl

import java.io.Serializable
import java.util.Collections

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp.NCNlpSentenceToken
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.NCModelDecorator

import scala.collection.JavaConverters._
import scala.collection.{Seq, mutable}

/**
  *
  * @param srvReqId Server request ID.
  * @param id
  * @param grps
  * @param parentId
  * @param value
  * @param meta
  */
private[nlpcraft] class NCTokenImpl(
    mdl: NCModelView,
    srvReqId: String,
    id: String,
    grps: Seq[String],
    parentId: String,
    ancestors: Seq[String],
    value: String,
    startCharIndex: Int,
    endCharIndex: Int,
    meta: Map[String, Object]
) extends NCToken with Serializable {
    require(mdl != null)
    require(srvReqId != null)
    require(id != null)
    require(grps != null)
    require(ancestors != null)
    require(meta != null)

    private final val hash =
        Seq(srvReqId, id, startCharIndex, endCharIndex).map(_.hashCode()).foldLeft(0)((a, b) ⇒ 31 * a + b)

    private var parts = Seq.empty[NCToken]
    
    override lazy val getModel: NCModelView = mdl
    override lazy val getMetadata: java.util.Map[String, Object] = mutable.HashMap(meta.toSeq:_ *).asJava // We need mutable metadata.
    override lazy val getServerRequestId: String = srvReqId
    override lazy val getId: String = id
    override lazy val getGroups: java.util.List[String] = grps.asJava
    override lazy val getParentId: String = parentId
    override lazy val getAncestors: java.util.List[String] = ancestors.asJava
    override lazy val getValue: String = value
    override lazy val getStartCharIndex: Int = startCharIndex
    override lazy val getEndCharIndex: Int = endCharIndex
    override lazy val getAliases: java.util.List[String] = meta(TOK_META_ALIASES_KEY, Collections.emptyList())
    override def getPartTokens: java.util.List[NCToken] = parts.asJava
    
    def setParts(parts: Seq[NCToken]): Unit = this.parts = parts
    
    override def equals(other: Any): Boolean = other match {
        case t: NCTokenImpl ⇒
            getServerRequestId == t.getServerRequestId &&
            getId == t.getId &&
            getStartCharIndex == t.getStartCharIndex &&
            getEndCharIndex == t.getEndCharIndex
            
        case _ ⇒ false
    }

    override def hashCode(): Int = hash

    override def toString: String =
        s"Token [" +
            s"id=$id, " +
            s"text=${meta[String]("nlpcraft:nlp:normtext")}, " +
            s"groups=${if (grps == null) null else grps.mkString(", ")}, " +
            s"parentId=$parentId, " +
            s"value=$value" +
        s"]"
}

private[nlpcraft] object NCTokenImpl {
    def apply(mdl: NCModelDecorator, srvReqId: String, tok: NCNlpSentenceToken): NCTokenImpl = {
        // nlpcraft:nlp and some optional (after collapsing).
        require(tok.size <= 2, s"Unexpected token [size=${tok.size}, token=$tok]")

        val md = mutable.HashMap.empty[String, java.io.Serializable]

        tok.foreach(n ⇒ {
            val id = n.noteType.toLowerCase

            n.asMetadata().foreach { case (k, v) ⇒ md += s"$id:$k" → v}
        })

        val usrNotes = tok.filter(_.isUser)

        // No overlapping allowed at this point.
        require(usrNotes.size <= 1, s"Unexpected elements notes: $usrNotes")

        def convertMeta(): Map[String, AnyRef] = md.toMap.map(p ⇒ p._1 → p._2.asInstanceOf[AnyRef])

        usrNotes.headOption match {
            case Some(usrNote) ⇒
                require(mdl.elements.contains(usrNote.noteType), s"Element is not found: ${usrNote.noteType}")

                val elm = mdl.elements(usrNote.noteType)

                val ancestors = mutable.ArrayBuffer.empty[String]
                var prntId = elm.getParentId

                while (prntId != null) {
                    ancestors += prntId

                    prntId = mdl.
                        elements.
                        getOrElse(prntId, throw new AssertionError(s"Element not found: $prntId")).
                        getParentId
                }

                // Special synthetic meta data element.
                md.put("nlpcraft:nlp:freeword", false)

                if (elm.getMetadata != null)
                    elm.getMetadata.asScala.foreach { case (k, v) ⇒ md.put(k, v.asInstanceOf[java.io.Serializable]) }

                new NCTokenImpl(
                    mdl.model,
                    srvReqId = srvReqId,
                    id = elm.getId,
                    grps = elm.getGroups.asScala,
                    parentId = elm.getParentId,
                    ancestors = ancestors,
                    value = usrNote.dataOpt("value").orNull,
                    startCharIndex = tok.startCharIndex,
                    endCharIndex = tok.endCharIndex,
                    meta = convertMeta()
                )

            case None ⇒
                require(tok.size <= 2)

                val note = tok.toSeq.minBy(n ⇒ if (n.isNlp) 1 else 0)

                val isStop: Boolean = md("nlpcraft:nlp:stopword").asInstanceOf[Boolean]

                // Special synthetic meta data element.
                md.put("nlpcraft:nlp:freeword", !isStop && note.isNlp)

                new NCTokenImpl(
                    mdl.model,
                    srvReqId = srvReqId,
                    id = note.noteType, // Use NLP note type as synthetic element ID.
                    grps = Seq(note.noteType), // Use NLP note type as synthetic element group.
                    parentId = null,
                    ancestors = Seq.empty,
                    value = null,
                    startCharIndex = tok.startCharIndex,
                    endCharIndex = tok.endCharIndex,
                    meta = convertMeta()
                )
        }
    }
}
