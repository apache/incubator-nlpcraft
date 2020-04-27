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

package org.apache.nlpcraft.probe.mgrs.model

import java.util.regex.{Pattern, PatternSyntaxException}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.util.NCUtils._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.probe.mgrs.NCSynonymChunkKind._
import org.apache.nlpcraft.probe.mgrs.deploy._
import org.apache.nlpcraft.probe.mgrs.{NCModelDecorator, NCSynonym, NCSynonymChunk}

import collection.convert.ImplicitConversions._
import scala.collection.convert.DecorateAsScala
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.Exception._

/**
  * Model manager.
  */
object NCModelManager extends NCService with DecorateAsScala {
    private final val TOKENS_PROVIDERS_PREFIXES = Set("nlpcraft:", "google:", "stanford:", "opennlp:", "spacy:")
    
    // Deployed models keyed by their IDs.
    @volatile private var models: mutable.Map[String, NCModelDecorator] = _

    // Access mutex.
    private final val mux = new Object()

    /**
      *
      * @param elementId Element ID.
      * @param synonym Element synonym.
      */
    case class SynonymHolder(
        elementId: String,
        synonym: NCSynonym
    )
    
    /**
      * @param mdl Data model.
      */
    private def addNewModel(mdl: NCModel): Unit = {
        require(Thread.holdsLock(mux))

        checkModelConfig(mdl)

        val parser = new NCMacroParser
        val macros = mdl.getMacros

        // Initialize macro parser.
        if (macros != null)
            macros.asScala.foreach(t ⇒ parser.addMacro(t._1, t._2))
        
        models += mdl.getId → verifyAndDecorate(mdl, parser)

        // Init callback on the model.
        mdl.onInit()
    }

    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        models = mutable.HashMap.empty[String, NCModelDecorator]

        mux.synchronized {
            NCDeployManager.getModels.foreach(addNewModel)

            if (models.isEmpty)
                throw new NCException("No models deployed.")

            val tbl = NCAsciiTable("Model ID", "Name", "Ver.", "Elements", "Synonyms")

            models.values.foreach(mdl ⇒ {
                val synCnt = mdl.synonyms.values.flatMap(_.values).flatten.size

                tbl += (
                    mdl.model.getId,
                    mdl.model.getName,
                    mdl.model.getVersion,
                    mdl.elements.keySet.size,
                    synCnt
                )
            })

            tbl.info(logger, Some(s"Models deployed: ${models.size}\n"))
            
            addTags(
                span,
                "deployedModels" → models.values.map(_.model.getId).mkString(",")
            )
        }

        super.start()
    }

    /**
      *
      * @param mdl
      */
    private def discardModel(mdl: NCModel): Unit = {
        require(Thread.holdsLock(mux))

        ignoring(classOf[Throwable]) {
            // Ack.
            logger.info(s"Model discarded: ${mdl.getId}")

            mdl.onDiscard()
        }
    }

    /**
      * Stops this component.
      */
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        mux.synchronized {
            if (models != null)
                models.values.foreach(m ⇒ discardModel(m.model))
        }

        super.stop()
    }

    /**
     *
     * @param fix Prefix and suffix.
     * @param s String to search prefix and suffix in.
     * @return
     */
    private def startsAndEnds(fix: String, s: String): Boolean =
        s.startsWith(fix) && s.endsWith(fix)

    /**
      *
      * @param s
      * @return
      */
    @throws[NCE]
    private def chunkSplit(s: String): Seq[NCSynonymChunk] = {
        val x = s.trim()
        
        val chunks = ListBuffer.empty[String]
        
        var start = 0
        var curr = 0
        val len = x.length - (2 + 2) // 2 is a prefix/suffix length. Hack...
        
        def splitUp(s: String): Seq[String] = s.split(" ").map(_.trim).filter(_.nonEmpty).toSeq
        
        def processChunk(fix: String): Unit = {
            chunks ++= splitUp(x.substring(start, curr))
    
            x.indexOf(fix, curr + fix.length) match {
                case -1 ⇒ throw new NCE(s"Invalid synonym definition in: $x")
                case n ⇒
                    chunks += x.substring(curr, n + fix.length)
                    start = n + fix.length
                    curr = start
            }
        }
        
        def isFix(fix: String): Boolean =
            x.charAt(curr) == fix.charAt(0) &&
            x.charAt(curr + 1) == fix.charAt(1)
        
        while (curr < len) {
            if (isFix(REGEX_FIX))
                processChunk(REGEX_FIX)
            else if (isFix(DSL_FIX))
                processChunk(DSL_FIX)
            else
                curr += 1
        }
        
        chunks ++= splitUp(x.substring(start))
        
        chunks.map(mkChunk)
    }

    /**
      *
      * @param chunk Synonym chunk.
      * @return
      */
    @throws[NCE]
    private def mkChunk(chunk: String): NCSynonymChunk = {
        def stripSuffix(fix: String, s: String): String = s.slice(fix.length, s.length - fix.length)

        // Regex synonym.
        if (startsAndEnds(REGEX_FIX, chunk)) {
            val ptrn = stripSuffix(REGEX_FIX, chunk)

            if (ptrn.length > 0)
                try
                    NCSynonymChunk(kind = REGEX, origText = chunk, regex = Pattern.compile(ptrn))
                catch {
                    case e: PatternSyntaxException ⇒ throw new NCE(s"Invalid regex syntax in: $chunk", e)
                }
            else
                throw new NCE(s"Empty regex synonym detected: $chunk")
        }
        // DSL-based synonym.
        else if (startsAndEnds(DSL_FIX, chunk)) {
            val dsl = stripSuffix(DSL_FIX, chunk)
            val compUnit = NCModelSynonymDslCompiler.parse(dsl)
    
            val x = NCSynonymChunk(alias = compUnit.alias, kind = DSL, origText = chunk, dslPred = compUnit.predicate)
            
            x
        }
        // Regular word.
        else
            NCSynonymChunk(kind = TEXT, origText = chunk, wordStem = NCNlpCoreManager.stem(chunk))
    }

    /**
      *
      * @param adds Additional stopword stems.
      * @param excls Excluded stopword stems.
      */
    @throws[NCE]
    private def checkStopwordsDups(adds: Set[String], excls: Set[String]): Unit = {
        val cross = adds.intersect(excls)

        if (cross.nonEmpty)
            throw new NCE(s"Duplicate stems in additional and excluded stopwords: '${cross.mkString(",")}'")
    }
    
    /**
      * Verifies given model and makes a decorator optimized for model enricher.
      *
      * @param mdl Model to verify and decorate.
      * @param parser Initialized macro parser.
      * @return Model decorator.
      */
    @throws[NCE]
    private def verifyAndDecorate(mdl: NCModel, parser: NCMacroParser): NCModelDecorator = {
        for (elm ← mdl.getElements)
            checkElement(mdl, elm)

        checkElementIdsDups(mdl)
        checkCyclicDependencies(mdl)

        val addStopWords = checkAndStemmatize(mdl.getAdditionalStopWords, "Additional stopword")
        val exclStopWords = checkAndStemmatize(mdl.getExcludedStopWords, "Excluded stopword")
        val suspWords = checkAndStemmatize(mdl.getSuspiciousWords, "Suspicious word")

        checkStopwordsDups(addStopWords, exclStopWords)

        val syns = mutable.HashSet.empty[SynonymHolder]

        var cnt = 0
        val maxCnt = mdl.getMaxTotalSynonyms

        // Process and check elements.
        for (elm ← mdl.getElements) {
            val elmId = elm.getId

            def addSynonym(
                isElementId: Boolean,
                isValueName: Boolean,
                value: String,
                chunks: Seq[NCSynonymChunk]): Unit = {
                def add(chunks: Seq[NCSynonymChunk], isDirect: Boolean): Unit = {
                    val holder = SynonymHolder(
                        elementId = elmId,
                        synonym = NCSynonym(isElementId, isValueName, isDirect, value, chunks)
                    )

                    if (syns.add(holder)) {
                        cnt += 1

                        if (cnt > maxCnt)
                            throw new NCE(s"Too many synonyms detected [" +
                                s"model=${mdl.getId}, " +
                                s"max=$maxCnt" +
                                s"]")

                        if (value == null)
                            logger.trace(s"Synonym #${syns.size} added [" +
                                s"model=${mdl.getId}, " +
                                s"elementId=$elmId, " +
                                s"synonym=${chunks.mkString(" ")}" +
                                s"]")
                        else
                            logger.trace(s"Synonym #${syns.size} added [" +
                                s"model=${mdl.getId}, " +
                                s"elementId=$elmId, " +
                                s"synonym=${chunks.mkString(" ")}, " +
                                s"value=$value" +
                                s"]")
                    }
                    else
                        logger.trace(
                            s"Synonym already added (ignoring) [" +
                                s"model=${mdl.getId}, " +
                                s"elementId=$elmId, " +
                                s"synonym=${chunks.mkString(" ")}, " +
                                s"value=$value" +
                                s"]"
                        )
                }

                if (mdl.isPermutateSynonyms && !isElementId && chunks.forall(_.wordStem != null))
                    simplePermute(chunks).map(p ⇒ p.map(_.wordStem) → p).toMap.values.foreach(p ⇒ add(p, p == chunks))
                else
                    add(chunks, true)
            }

            /**
              *
              * @param id
              * @return
              */
            def chunkIdSplit(id: String): Seq[NCSynonymChunk] = {
                val chunks = chunkSplit(NCNlpCoreManager.tokenize(id).map(_.token).mkString(" "))

                // IDs can only be simple strings.
                if (chunks.exists(_.kind != TEXT))
                    throw new NCE(s"Invalid ID: $id")

                chunks
            }

            // Add element ID as a synonyms (dups ignored).
            val idChunks = Seq(chunkIdSplit(elmId))

            idChunks.distinct.foreach(ch ⇒ addSynonym(true, false, null, ch))

            // Add straight element synonyms (dups printed as warnings).
            val synsChunks = for (syn ← elm.getSynonyms.flatMap(parser.expand)) yield chunkSplit(syn)

            if (U.containsDups(synsChunks.flatten))
                logger.trace(s"Element synonyms duplicate (ignoring) [" +
                    s"model=${mdl.getId}, " +
                    s"elementId=$elmId, " +
                    s"synonym=${synsChunks.diff(synsChunks.distinct).distinct.map(_.mkString(",")).mkString(";")}" +
                    s"]"
                )

            synsChunks.distinct.foreach(ch ⇒ addSynonym(false, false, null, ch))

            val vals =
                (if (elm.getValues != null) elm.getValues.asScala else Seq.empty) ++
                (if (elm.getValueLoader != null) elm.getValueLoader.load(elm).asScala else Seq.empty)

            // Add value synonyms.
            val valNames = vals.map(_.getName)

            if (U.containsDups(valNames))
                logger.trace(s"Element values names duplicate (ignoring) [" +
                    s"model=${mdl.getId}, " +
                    s"elementId=$elmId, " +
                    s"names=${valNames.diff(valNames.distinct).distinct.mkString(",")}" +
                    s"]"
                )

            for (v ← vals.map(p ⇒ p.getName → p).toMap.values) {
                val valId = v.getName
                val valSyns = v.getSynonyms.asScala

                val idChunks = Seq(chunkIdSplit(valId))

                // Add value name as a synonyms (dups ignored)
                idChunks.distinct.foreach(ch ⇒ addSynonym(false, true, valId, ch))

                // Add straight value synonyms (dups printed as warnings)
                var skippedOneLikeName = false

                val chunks =
                    valSyns.flatMap(parser.expand).flatMap(valSyn ⇒ {
                        val valSyns = chunkSplit(valSyn)

                        if (idChunks.contains(valSyns) && !skippedOneLikeName) {
                            skippedOneLikeName = true

                            None
                        }
                        else
                            Some(valSyns)
                    })

                if (U.containsDups(chunks.toList))
                    logger.trace(s"Element synonyms duplicate (ignoring) [" +
                        s"model=${mdl.getId}, " +
                        s"elementId=$elmId, " +
                        s"value=$valId, " +
                        s"synonym=${chunks.diff(chunks.distinct).distinct.map(_.mkString(",")).mkString(";")}" +
                        s"]"
                    )

                chunks.distinct.foreach(ch ⇒ addSynonym(false, false, valId, ch))
            }
        }
        
        val valLdrs = mutable.HashSet.empty[NCValueLoader]
        
        for (elm ← mdl.getElements) {
            val ldr = elm.getValueLoader
            
            if (ldr != null)
                valLdrs += ldr
        }
        
        // Discard value loaders, if any.
        for (ldr ← valLdrs)
            ldr.onDiscard()

        var foundDups = false
        
        val allAliases =
            syns
            .flatMap(_.synonym)
            .groupBy(_.origText)
            .map(x ⇒ (x._1, x._2.map(_.alias).filter(_ != null)))
            .values
            .flatten
            .toList
        
        // Check for DSl alias uniqueness.
        if (U.containsDups(allAliases)) {
            for (dupAlias ← allAliases.diff(allAliases.distinct))
                logger.warn(s"Duplicate DSL alias '$dupAlias' found for model: ${mdl.getId}")
            
            throw new NCE(s"Duplicate DSL aliases found for model '${mdl.getId}'- check log messages.")
        }
        
        val idAliasDups =
            mdl
            .getElements
            .map(_.getId)
            .intersect(allAliases.toSet)
        
        // Check that DSL aliases don't intersect with element IDs.
        if (idAliasDups.nonEmpty) {
            for (dup ← idAliasDups)
                logger.warn(s"Duplicate element ID and DSL alias '$dup' found for model: ${mdl.getId}")
    
            throw new NCE(s"Duplicate element ID and DSL aliases found for model '${mdl.getId}'- check log messages.")
        }
    
        // Check for synonym dups across all elements.
        for (
            ((syn, isDirect), holders) ←
                syns.groupBy(p ⇒ (p.synonym.mkString(" "), p.synonym.isDirect)) if holders.size > 1 && isDirect
        ) {
            logger.trace(s"Duplicate synonym detected (ignoring) [" +
                s"model=${mdl.getId}, " +
                s"element=${holders.map(
                    p ⇒ s"id=${p.elementId}${if (p.synonym.value == null) "" else s", value=${p.synonym.value}"}"
                ).mkString("(", ",", ")")}, " +
                s"synonym=$syn" +
                s"]"
            )

            foundDups = true
        }

        if (foundDups) {
            if (!mdl.isDupSynonymsAllowed)
                throw new NCE(s"Duplicated synonyms are not allowed for model '${mdl.getId}' - check trace messages.")

            logger.warn(s"Found duplicate synonyms - check trace logging for model: ${mdl.getId}")
            logger.warn(s"Duplicates are allowed by '${mdl.getId}' model but large number may degrade the performance.")
        }
    
        mdl.getMetadata.put(MDL_META_ALL_ALIASES_KEY, allAliases.toSet)
        mdl.getMetadata.put(MDL_META_ALL_ELM_IDS_KEY,
            mdl.getElements.map(_.getId).toSet ++
            Set("nlpcraft:nlp") ++
            mdl.getEnabledBuiltInTokens
        )
        mdl.getMetadata.put(MDL_META_ALL_GRP_IDS_KEY,
            mdl.getElements.flatMap(_.getGroups.asScala).toSet ++
            Set("nlpcraft:nlp") ++
            mdl.getEnabledBuiltInTokens
        )

        /**
          *
          * @param set
          * @return
          */
        def mkFastAccessMap(set: Set[SynonymHolder]): Map[String/*Element ID*/, Map[Int/*Synonym length*/, Seq[NCSynonym]]] =
            set
                .groupBy(_.elementId)
                .map {
                    case (elmId, holders) ⇒ (
                        elmId,
                        holders
                            .map(_.synonym)
                            .groupBy(_.size)
                            .map {
                                // Sort synonyms from most important to least important.
                                case (k, v) ⇒ (k, v.toSeq.sorted.reverse)
                            }
                    )
                }

        def filter(set: mutable.HashSet[SynonymHolder], dsl: Boolean): Set[SynonymHolder] =
            set.toSet.filter(s ⇒ {
                val b = s.synonym.exists(_.kind == DSL)

                if (dsl) b else !b
            })

        NCModelDecorator(
            model = mdl,
            synonyms = mkFastAccessMap(filter(syns, dsl = false)),
            synonymsDsl = mkFastAccessMap(filter(syns, dsl = true)),
            additionalStopWordsStems = addStopWords,
            excludedStopWordsStems = exclStopWords,
            suspiciousWordsStems = suspWords,
            elements = mdl.getElements.map(elm ⇒ (elm.getId, elm)).toMap
        )
    }

    /**
      * Permutes and drops duplicated.
      * For a given multi-word synonym we allow a single word move left or right only one position per permutation
      * (i.e. only one word jiggles per permutation).
      * E.g. for "A B C D" synonym we'll have only the following permutations:
      * "A, B, C, D"
      * "A, B, D, C"
      * "A, C, B, D"
      * "B, A, C, D"
      *
      * @param seq Initial sequence.
      * @return Permutations.
      */
    private def simplePermute[T](seq: Seq[T]): Seq[Seq[T]] =
        seq.length match {
            case 0 ⇒ Seq.empty
            case 1 ⇒ Seq(seq)
            case n ⇒
                def permute(idx1: Int, idx2: Int): Seq[T] =
                    seq.zipWithIndex.map { case (t, idx) ⇒
                        if (idx == idx1)
                            seq(idx2)
                        else if (idx == idx2)
                            seq(idx1)
                        else
                            t
                    }

                Seq(seq)++
                    seq.zipWithIndex.flatMap { case (_, idx) ⇒
                        if (idx == 0)
                            Seq(permute(0, 1))
                        else if (idx == n - 1)
                            Seq(permute(n - 2, n - 1))
                        else
                            Seq(permute(idx - 1, idx), permute(idx, idx + 1))
                    }.distinct
        }

    /**
      *
      * @param jc
      * @param name
      * @return
      */
    private def checkAndStemmatize(jc: java.util.Set[String], name: String): Set[String] =
        for (word: String ← jc.asScala.toSet) yield
            if (hasWhitespace(word))
                throw new NCE(s"$name cannot have whitespace: '$word'")
            else
                NCNlpCoreManager.stem(word)

    /**
      * Checks cyclic child-parent dependencies.
      *
      * @param mdl Model.
      */
    @throws[NCE]
    private def checkCyclicDependencies(mdl: NCModel): Unit =
        for (elm ← mdl.getElements) {
            if (elm.getParentId != null) {
                val seen = mutable.ArrayBuffer.empty[String]

                var parentId: String = null
                var x = elm

                do {
                    parentId = x.getParentId

                    if (parentId != null) {
                        if (seen.contains(parentId))
                            throw new NCE(s"Cyclic parent dependency starting at model element '${x.getId}'.")
                        else {
                            seen += parentId

                            x = mdl.getElements.find(_.getId == parentId) getOrElse {
                                throw new NCE(s"Unknown parent ID '$parentId' for model element '${x.getId}'.")

                                null
                            }
                        }
                    }
                }
                while (parentId != null)
            }
        }

    /**
      *
      * @param mdl Model.
      */
    @throws[NCE]
    private def checkElementIdsDups(mdl: NCModel): Unit = {
        val ids = mutable.HashSet.empty[String]

        for (id ← mdl.getElements.toList.map(_.getId))
            if (ids.contains(id))
                throw new NCE(s"Duplicate model element ID '$id'.")
            else
                ids += id
    }

    /**
      * Verifies model element in isolation.
      *
      * @param mdl Model.
      * @param elm Element to verify.
      */
    @throws[NCE]
    private def checkElement(mdl: NCModel, elm: NCElement): Unit = {
        if (elm.getId == null)
            throw new NCE(s"Model element ID is not provided.'")
        else if (elm.getId.length == 0)
            throw new NCE(s"Model element ID cannot be empty.'")
        else {
            val elmId = elm.getId

            if (elmId.toLowerCase.startsWith("nlpcraft:"))
                throw new NCE(s"Model element '$elmId' type cannot start with 'nlpcraft:'.")

            if (hasWhitespace(elmId))
                throw new NCE(s"Model element ID '$elmId' cannot have whitespaces.")
        }
    }

    /**
      * Checks whether or not given string has any whitespaces.
      *
      * @param s String to check.
      * @return
      */
    private def hasWhitespace(s: String): Boolean =
        s.exists(_.isWhitespace)

    /**
      *
      * @param mdl Model.
      */
    private def checkModelConfig(mdl: NCModel): Unit = {
        def checkInt(v: Int, name: String, min: Int = 0, max: Int = Integer.MAX_VALUE): Unit =
            if (v < min)
                throw new NCE(s"Invalid model configuration value '$name' [value=$v, min=$min]")
            else if (v > max)
                throw new NCE(s"Invalid model configuration value '$name' [value=$v, max=$min]")

        checkInt(mdl.getMaxUnknownWords, "maxUnknownWords")
        checkInt(mdl.getMaxFreeWords, "maxFreeWords")
        checkInt(mdl.getMaxSuspiciousWords, "maxSuspiciousWords")
        checkInt(mdl.getMinWords, "minWords", min = 1)
        checkInt(mdl.getMinNonStopwords, "minNonStopwords")
        checkInt(mdl.getMinTokens, "minTokens")
        checkInt(mdl.getMaxTokens, "maxTokens", max = 100)
        checkInt(mdl.getMaxWords, "maxWords", min = 1, max = 100)
        checkInt(mdl.getJiggleFactor, "jiggleFactor", max = 4)

        val unsToks =
            mdl.getEnabledBuiltInTokens.filter(t ⇒
                // 'stanford', 'google', 'opennlp', 'spacy' - any names, not validated.
                t == null ||
                !TOKENS_PROVIDERS_PREFIXES.exists(typ ⇒ t.startsWith(typ)) ||
                // 'nlpcraft' names validated.
                (t.startsWith("nlpcraft:") && !NCModelView.DFLT_ENABLED_BUILTIN_TOKENS.contains(t))
            )

        if (unsToks.nonEmpty)
            throw new NCE(s"Invalid model 'enabledBuiltInTokens' token IDs: ${unsToks.mkString(", ")}")
    }

    /**
      *
      * @return
      */
    def getAllModels(parent: Span = null): List[NCModelDecorator] =
        startScopedSpan("getAllModels", parent) { _ ⇒
            mux.synchronized {
                models.values.toList
            }
        }

    /**
      *
      * @param id Model ID.
      * @return
      */
    def getModel(id: String, parent: Span = null): Option[NCModelDecorator] =
        startScopedSpan("getModel", parent, "id" → id) { _ ⇒
            mux.synchronized {
                models.get(id)
            }
        }
}