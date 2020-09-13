package org.apache.nlpcraft.probe.mgrs.nlp

import java.io.Serializable
import java.util

import org.apache.nlpcraft.common.TOK_META_ALIASES_KEY
import org.apache.nlpcraft.common.nlp.NCNlpSentence
import org.apache.nlpcraft.model.impl.{NCTokenImpl, NCVariantImpl}
import org.apache.nlpcraft.model.intent.impl.NCIntentSolver
import org.apache.nlpcraft.model.{NCElement, NCModel, NCVariant}
import org.apache.nlpcraft.probe.mgrs.NCSynonym

import scala.collection.JavaConverters._
import scala.collection.{Map, Seq, mutable}

/**
  *
  * @param model
  * @param solver
  * @param synonyms
  * @param synonymsDsl
  * @param addStopWordsStems
  * @param exclStopWordsStems
  * @param suspWordsStems
  * @param elements
  */
case class NCModelData(
    model: NCModel,
    solver: NCIntentSolver,
    synonyms: Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , Seq[NCSynonym]]], // Fast access map.
    synonymsDsl: Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , Seq[NCSynonym]]], // Fast access map.
    addStopWordsStems: Set[String],
    exclStopWordsStems: Set[String],
    suspWordsStems: Set[String],
    elements: Map[String /*Element ID*/ , NCElement],
    samples: Map[String, Seq[String]]
) {
    /**
      * Makes variants for given sentences.
      *
      * @param srvReqId Server request ID.
      * @param sens Sentences.
      */
    def makeVariants(srvReqId: String, sens: Seq[NCNlpSentence]): Seq[NCVariant] = {
        val seq = sens.map(_.toSeq.map(nlpTok ⇒ NCTokenImpl(this, srvReqId, nlpTok) → nlpTok))
        val toks = seq.map(_.map { case (tok, _) ⇒ tok })

        case class Key(id: String, from: Int, to: Int)

        val keys2Toks = toks.flatten.map(t ⇒ Key(t.getId, t.getStartCharIndex, t.getEndCharIndex) → t).toMap
        val partsKeys = mutable.HashSet.empty[Key]

        seq.flatten.foreach { case (tok, tokNlp) ⇒
            if (tokNlp.isUser) {
                val userNotes = tokNlp.filter(_.isUser)

                require(userNotes.size == 1)

                val optList: Option[util.List[util.HashMap[String, Serializable]]] = userNotes.head.dataOpt("parts")

                optList match {
                    case Some(list) ⇒
                        val keys =
                            list.asScala.map(m ⇒
                                Key(
                                    m.get("id").asInstanceOf[String],
                                    m.get("startcharindex").asInstanceOf[Integer],
                                    m.get("endcharindex").asInstanceOf[Integer]
                                )
                            )
                        val parts = keys.map(keys2Toks)

                        parts.zip(list.asScala).foreach { case (part, map) ⇒
                            map.get(TOK_META_ALIASES_KEY) match {
                                case null ⇒ // No-op.
                                case aliases ⇒ part.getMetadata.put(TOK_META_ALIASES_KEY, aliases.asInstanceOf[Object])
                            }
                        }

                        tok.setParts(parts)
                        partsKeys ++= keys

                    case None ⇒ // No-op.
                }
            }
        }

        //  We can't collapse parts earlier, because we need them here (setParts method, few lines above.)
        toks.filter(sen ⇒
            !sen.exists(t ⇒
                t.getId != "nlpcraft:nlp" &&
                    partsKeys.contains(Key(t.getId, t.getStartCharIndex, t.getEndCharIndex))
            )
        ).map(p ⇒ new NCVariantImpl(p.asJava))
    }
}
