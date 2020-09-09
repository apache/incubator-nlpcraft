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

package org.apache.nlpcraft.probe.mgrs.deploy

import java.io._
import java.util.jar.{JarInputStream => JIS}
import java.util.regex.{Pattern, PatternSyntaxException}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.NCNlpCoreManager
import org.apache.nlpcraft.common.util.NCUtils.{DSL_FIX, REGEX_FIX}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.factories.basic.NCBasicModelFactory
import org.apache.nlpcraft.model.intent.impl.{NCIntentScanner, NCIntentSolver}
import org.apache.nlpcraft.probe.mgrs.NCSynonymChunkKind.{DSL, REGEX, TEXT}
import org.apache.nlpcraft.probe.mgrs.{NCSynonym, NCSynonymChunk, deploy}
import org.apache.nlpcraft.probe.mgrs.model.NCModelSynonymDslCompiler
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.convert.DecorateAsScala
import scala.collection.{Seq, mutable}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.Exception._

/**
  * Model deployment manager.
  */
object NCDeployManager extends NCService with DecorateAsScala {
    private final val TOKENS_PROVIDERS_PREFIXES = Set("nlpcraft:", "google:", "stanford:", "opennlp:", "spacy:")
    private final val ID_REGEX = "^[_a-zA-Z]+[a-zA-Z0-9:-_]*$"

    @volatile private var wrappers: ArrayBuffer[NCModelWrapper] = _
    @volatile private var modelFactory: NCModelFactory = _

    object Config extends NCConfigurable {
        private final val pre = "nlpcraft.probe"

        // It should reload config.
        def modelFactoryType: Option[String] = getStringOpt(s"$pre.modelFactory.type")
        def modelFactoryProps: Option[Map[String, String]] = getMapOpt(s"$pre.modelFactory.properties")
        def models: Seq[String] = getStringList(s"$pre.models")
        def jarsFolder: Option[String] = getStringOpt(s"$pre.jarsFolder")
    }

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
      * Gives a list of JAR files at given path.
      *
      * @param path Path to scan.
      * @return
      */
    private def scanJars(path: File): Seq[File] = {
        val jars = path.listFiles(new FileFilter {
            override def accept(f: File): Boolean =
                f.isFile && f.getName.toLowerCase.endsWith(".jar")
        })

        if (jars == null) Seq.empty else jars.toSeq
    }

    /**
      *
      * @param mdl
      * @return
      */
    @throws[NCE]
    private def wrap(mdl: NCModel): NCModelWrapper = {
        checkCollection("additionalStopWords", mdl.getAdditionalStopWords)
        checkCollection("elements", mdl.getElements)
        checkCollection("enabledBuiltInTokens", mdl.getEnabledBuiltInTokens)
        checkCollection("excludedStopWords", mdl.getExcludedStopWords)
        checkCollection("parsers", mdl.getParsers)
        checkCollection("suspiciousWords", mdl.getSuspiciousWords)
        checkCollection("macros", mdl.getMacros)
        checkCollection("metadata", mdl.getMetadata)

        // Scan for intent annotations in the model class.
        val intents = NCIntentScanner.scan(mdl)

        val mdlId = mdl.getId

        val parser = new NCMacroParser

        // Initialize macro parser.
        mdl.getMacros.asScala.foreach(t ⇒ parser.addMacro(t._1, t._2))

        var solver: NCIntentSolver = null

        if (intents.nonEmpty) {
            // Check the uniqueness of intent IDs.
            U.getDups(intents.keys.toSeq.map(_.id)) match {
                case ids if ids.nonEmpty ⇒ throw new NCE(s"Duplicate intent IDs found for '$mdlId' model: ${ids.mkString(",")}")
                case _ ⇒ ()
            }

            logger.info(s"Intents found in the model: $mdlId")

            solver = new NCIntentSolver(
                intents.toList.map(x ⇒ (x._1, (z: NCIntentMatch) ⇒ x._2.apply(z)))
            )
        }
        else
            logger.warn(s"Model has no intents: $mdlId")

        checkModelConfig(mdl)

        for (elm ← mdl.getElements.asScala)
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
        for (elm ← mdl.getElements.asScala) {
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
                    add(chunks, isDirect = true)
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

            idChunks.distinct.foreach(ch ⇒ addSynonym(isElementId = true, isValueName = false, null, ch))

            // Add straight element synonyms (dups printed as warnings).
            val synsChunks = for (syn ← elm.getSynonyms.asScala.flatMap(parser.expand)) yield chunkSplit(syn)

            if (U.containsDups(synsChunks.flatten))
                logger.trace(s"Element synonyms duplicate (ignoring) [" +
                    s"model=${mdl.getId}, " +
                    s"elementId=$elmId, " +
                    s"synonym=${synsChunks.diff(synsChunks.distinct).distinct.map(_.mkString(",")).mkString(";")}" +
                    s"]"
                )

            synsChunks.distinct.foreach(ch ⇒ addSynonym(isElementId = false, isValueName = false, null, ch))

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
                idChunks.distinct.foreach(ch ⇒ addSynonym(isElementId = false, isValueName = true, valId, ch))

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

                chunks.distinct.foreach(ch ⇒ addSynonym(isElementId = false, isValueName = false, valId, ch))
            }
        }

        val valLdrs = mutable.HashSet.empty[NCValueLoader]

        for (elm ← mdl.getElements.asScala) {
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
                .getElements.asScala
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
                s"element=${
                    holders.map(
                        p ⇒ s"id=${p.elementId}${if (p.synonym.value == null) "" else s", value=${p.synonym.value}"}"
                    ).mkString("(", ",", ")")
                }, " +
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
            mdl.getElements.asScala.map(_.getId).toSet ++
                Set("nlpcraft:nlp") ++
                mdl.getEnabledBuiltInTokens.asScala
        )
        mdl.getMetadata.put(MDL_META_ALL_GRP_IDS_KEY,
            mdl.getElements.asScala.flatMap(_.getGroups.asScala).toSet ++
                Set("nlpcraft:nlp") ++
                mdl.getEnabledBuiltInTokens.asScala
        )

        deploy.NCModelWrapper(
            proxy = mdl,
            solver = solver,
            synonyms = mkFastAccessMap(filter(syns, dsl = false)),
            synonymsDsl = mkFastAccessMap(filter(syns, dsl = true)),
            addStopWordsStems = addStopWords,
            exclStopWordsStems = exclStopWords,
            suspWordsStems = suspWords,
            elements = mdl.getElements.asScala.map(elm ⇒ (elm.getId, elm)).toMap
        )
    }

    /**
      *
      * @param name
      * @param col
      */
    @throws[NCE]
    private def checkCollection(name: String, col: Any): Unit =
        if (col == null)
            throw new NCE(s"Collection '$name' can be empty but cannot be null.")

    /**
      *
      * @param clsName Factory class name.
      */
    @throws[NCE]
    private def makeModelFactory(clsName: String): NCModelFactory =
        catching(classOf[Throwable]) either Thread.currentThread().getContextClassLoader.
            loadClass(clsName).
            getDeclaredConstructor().
            newInstance().
            asInstanceOf[NCModelFactory]
        match {
            case Left(e) ⇒ throw new NCE(s"Failed to instantiate model factory: $clsName", e)
            case Right(factory) ⇒ factory
        }

    /**
      *
      * @param clsName Model class name.
      */
    @throws[NCE]
    private def makeModelWrapper(clsName: String): NCModelWrapper =
        try
            wrap(
                makeModelFromSource(
                    Thread.currentThread().getContextClassLoader.loadClass(clsName).asSubclass(classOf[NCModel]),
                    clsName
                )
            )
        catch {
            case e: Throwable ⇒ throw new NCE(s"Failed to instantiate model: $clsName", e)
        }

    /**
      *
      * @param set
      * @return
      */
    private def mkFastAccessMap(set: Set[SynonymHolder]): Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , Seq[NCSynonym]]] =
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

    /**
      *
      * @param cls Model class.
      * @param src Model class source.
      */
    @throws[NCE]
    private def makeModelFromSource(cls: Class[_ <: NCModel], src: String): NCModel =
        catching(classOf[Throwable]) either modelFactory.mkModel(cls) match {
            case Left(e) ⇒
                throw new NCE(s"Failed to instantiate model [" +
                    s"class=${cls.getName}, " +
                    s"factory=${modelFactory.getClass.getName}, " +
                    s"source=$src" +
                    "]", e)

            case Right(model) ⇒ model
        }

    /**
      *
      * @param jarFile JAR file to extract from.
      */
    @throws[NCE]
    private def extractModels(jarFile: File): Seq[NCModelWrapper] = {
        val clsLdr = Thread.currentThread().getContextClassLoader

        val classes = mutable.ArrayBuffer.empty[Class[_ <: NCModel]]

        managed(new JIS(new BufferedInputStream(new FileInputStream(jarFile)))) acquireAndGet { in ⇒
            var entry = in.getNextJarEntry

            while (entry != null) {
                if (!entry.isDirectory && entry.getName.endsWith(".class")) {
                    val clsName = entry.getName.substring(0, entry.getName.length - 6).replace('/', '.')

                    try {
                        val cls = clsLdr.loadClass(clsName)

                        if (classOf[NCModel].isAssignableFrom(cls) && !cls.isInterface)
                            classes += cls.asSubclass(classOf[NCModel])
                    }
                    catch {
                        // Errors are possible for JARs like log4j etc, which have runtime dependencies.
                        // We don't need these messages in log beside trace, so ignore...
                        case _: ClassNotFoundException ⇒ ()
                        case _: NoClassDefFoundError ⇒ ()
                    }
                }

                entry = in.getNextJarEntry
            }
        }

        classes.map(cls ⇒
            wrap(
                makeModelFromSource(cls, jarFile.getPath)
            )
        )
    }

    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        modelFactory = new NCBasicModelFactory
        wrappers = ArrayBuffer.empty[NCModelWrapper]

        // Initialize model factory (if configured).
        Config.modelFactoryType match {
            case Some(mft) ⇒
                modelFactory = makeModelFactory(mft)

                modelFactory.initialize(Config.modelFactoryProps.getOrElse(Map.empty[String, String]).asJava)

            case None ⇒ // No-op.
        }

        wrappers ++= Config.models.map(makeModelWrapper)

        Config.jarsFolder match {
            case Some(jarsFolder) ⇒
                val jarsFile = new File(jarsFolder)

                if (!jarsFile.exists())
                    throw new NCE(s"JAR folder path '$jarsFolder' does not exist.")
                if (!jarsFile.isDirectory)
                    throw new NCE(s"JAR folder path '$jarsFolder' is not a directory.")

                val src = this.getClass.getProtectionDomain.getCodeSource
                val locJar = if (src == null) null else new File(src.getLocation.getPath)

                for (jar ← scanJars(jarsFile) if jar != locJar)
                    wrappers ++= extractModels(jar)

            case None ⇒ // No-op.
        }

        // Verify models' identities.
        wrappers.foreach(w ⇒ {
            val mdl = w.proxy
            val mdlName = mdl.getName
            val mdlId = mdl.getId
            val mdlVer = mdl.getVersion

            if (mdlId == null)
                throw new NCE(s"Model ID is not provided: $mdlName")
            if (mdlName == null)
                throw new NCE(s"Model name is not provided: $mdlId")
            if (mdlVer == null)
                throw new NCE(s"Model version is not provided: $mdlId")
            if (mdlName != null && mdlName.isEmpty)
                throw new NCE(s"Model name cannot be empty string: $mdlId")
            if (mdlId != null && mdlId.isEmpty)
                throw new NCE(s"Model ID cannot be empty string: $mdlId")
            if (mdlVer != null && mdlVer.length > 16)
                throw new NCE(s"Model version cannot be empty string: $mdlId")
            if (mdlName != null && mdlName.length > 64)
                throw new NCE(s"Model name is too long (64 max): $mdlId")
            if (mdlId != null && mdlId.length > 32)
                throw new NCE(s"Model ID is too long (32 max): $mdlId")
            if (mdlVer != null && mdlVer.length > 16)
                throw new NCE(s"Model version is too long (16 max): $mdlId")

            for (elm ← mdl.getElements.asScala)
                if (!elm.getId.matches(ID_REGEX))
                    throw new NCE(s"Model element ID '${elm.getId}' does not match '$ID_REGEX' regex in: $mdlId")
        })

        if (U.containsDups(wrappers.map(_.proxy.getId).toList))
            throw new NCE("Duplicate model IDs detected.")

        super.start()
    }

    @throws[NCE]
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        if (modelFactory != null)
            modelFactory.terminate()

        if (wrappers != null)
            wrappers.clear()

        super.stop()
    }

    /**
      *
      * @return
      */
    def getModels: Seq[NCModelWrapper] = wrappers

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

                Seq(seq) ++
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
        for (elm ← mdl.getElements.asScala) {
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

                            x = mdl.getElements.asScala.find(_.getId == parentId) getOrElse {
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

        for (id ← mdl.getElements.asScala.map(_.getId))
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
    private def checkElement(mdl: NCModel, elm: NCElement): Unit =
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
            mdl.getEnabledBuiltInTokens.asScala.filter(t ⇒
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
      * Checks whether or not given string has any whitespaces.
      *
      * @param s String to check.
      * @return
      */
    private def hasWhitespace(s: String): Boolean = s.exists(_.isWhitespace)

    private def filter(set: mutable.HashSet[SynonymHolder], dsl: Boolean): Set[SynonymHolder] =
        set.toSet.filter(s ⇒ {
            val b = s.synonym.exists(_.kind == DSL)

            if (dsl) b else !b
        })

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
}
