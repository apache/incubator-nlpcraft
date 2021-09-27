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

package org.apache.nlpcraft.model.impl

import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.nlp.NCNlpSentenceToken
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.NCProbeModel

import java.io.{Serializable => JSerializable}
import java.lang
import java.util.{Collections, List => JList}
import scala.collection.mutable
import scala.jdk.CollectionConverters.{CollectionHasAsScala, SeqHasAsJava}

/**
  *
  * @param srvReqId Server request ID.
  * @param id
  * @param grps
  * @param parentId
  * @param value
  * @param meta
  * @param isAbstractProp
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
    meta: java.util.Map[String, Object],
    isAbstractProp: Boolean
) extends NCMetadataAdapter(meta) with NCToken with JSerializable {
    require(mdl != null)
    require(srvReqId != null)
    require(id != null)
    require(grps != null)
    require(ancestors != null)
    require(meta != null)

    private final val hash = U.mkJavaHash(mdl.getId, srvReqId, id, startCharIndex, endCharIndex)

    private var parts = Seq.empty[NCToken]

    override lazy val getModel: NCModelView = mdl
    override lazy val getServerRequestId: String = srvReqId
    override lazy val getId: String = id
    override lazy val getGroups: JList[String] = grps.asJava
    override lazy val getParentId: String = parentId
    override lazy val getAncestors: JList[String] = ancestors.asJava
    override lazy val getValue: String = value
    override lazy val getStartCharIndex: Int = startCharIndex
    override lazy val getEndCharIndex: Int = endCharIndex
    override lazy val getAliases: java.util.Set[String] = meta(TOK_META_ALIASES_KEY, Collections.emptySet())
    override lazy val isAbstract: Boolean = isAbstractProp
    override def getPartTokens: JList[NCToken] = parts.asJava

    def setParts(parts: Seq[NCToken]): Unit = this.parts = parts

    override def equals(other: Any): Boolean = other match {
        case t: NCTokenImpl =>
            getServerRequestId == t.getServerRequestId &&
            getId == t.getId &&
            getStartCharIndex == t.getStartCharIndex &&
            getEndCharIndex == t.getEndCharIndex

        case _ => false
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
    def apply(mdl: NCProbeModel, srvReqId: String, tok: NCNlpSentenceToken): NCTokenImpl = {
        // nlpcraft:nlp and some optional (after collapsing).
        require(tok.size <= 2, s"Unexpected token [size=${tok.size}, token=$tok]")

        val md = new java.util.HashMap[String, AnyRef]()

        tok.foreach(n => {
            val id = n.noteType.toLowerCase

            n.asMetadata().foreach { case (k, v) => md.put(s"$id:$k", v.asInstanceOf[AnyRef]) }
        })

        val usrNotes = tok.filter(_.isUser)

        // No overlapping allowed at this point.
        require(usrNotes.size <= 1, s"Unexpected elements notes: $usrNotes")

        usrNotes.headOption match {
            case Some(usrNote) =>
                require(mdl.elements.contains(usrNote.noteType), s"Element is not found: ${usrNote.noteType}")

                val elm = mdl.elements(usrNote.noteType)

                val ancestors = mutable.ArrayBuffer.empty[String]
                var parentId = elm.getParentId

                while (parentId != null) {
                    ancestors += parentId

                    parentId = mdl.
                        elements.
                        getOrElse(parentId, throw new AssertionError(s"Element not found: $parentId")).
                        getParentId
                }

                // Special synthetic meta data element.
                md.put("nlpcraft:nlp:freeword", java.lang.Boolean.FALSE)

                md.putAll(elm.getMetadata)

                new NCTokenImpl(
                    mdl.model,
                    srvReqId = srvReqId,
                    id = elm.getId,
                    grps = elm.getGroups.asScala.toSeq,
                    parentId = elm.getParentId,
                    ancestors = ancestors.toSeq,
                    value = usrNote.dataOpt("value").orNull,
                    startCharIndex = tok.startCharIndex,
                    endCharIndex = tok.endCharIndex,
                    meta = md,
                    isAbstractProp = mdl.model.getAbstractTokens.contains(elm.getId)
                )

            case None =>
                require(tok.size <= 2)

                val note = tok.toSeq.minBy(n => if (n.isNlp) 1 else 0)

                val isStop = md.get("nlpcraft:nlp:stopword").asInstanceOf[Boolean]

                // Special synthetic meta data element.
                md.put("nlpcraft:nlp:freeword", lang.Boolean.valueOf(!isStop && note.isNlp))

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
                    meta = md,
                    isAbstractProp = mdl.model.getAbstractTokens.contains(note.noteType)
                )
        }
    }
}
