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
import java.lang.reflect.{InvocationTargetException, Method, ParameterizedType, Type}
import java.util
import java.util.function.Function
import java.util.jar.JarInputStream
import java.util.regex.{Pattern, PatternSyntaxException}

import io.opencensus.trace.Span
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.{NCNlpCoreManager, NCNlpPorterStemmer}
import org.apache.nlpcraft.common.util.NCUtils.{DSL_FIX, REGEX_FIX}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.factories.basic.NCBasicModelFactory
import org.apache.nlpcraft.model.intent.impl.{NCIntentDslCompiler, NCIntentSolver}
import org.apache.nlpcraft.model.intent.utils.NCDslIntent
import org.apache.nlpcraft.probe.mgrs.NCSynonymChunkKind.{DSL, REGEX, TEXT}
import org.apache.nlpcraft.probe.mgrs.{NCSynonym, NCSynonymChunk, deploy}
import org.apache.nlpcraft.probe.mgrs.model.NCModelSynonymDslCompiler
import org.apache.nlpcraft.probe.mgrs.nlp.NCModelData
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.convert.DecorateAsScala
import scala.collection.{Map, Seq, Set, mutable}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.Exception._

/**
  * Model deployment manager.
  */
object NCDeployManager extends NCService with DecorateAsScala {
    private final val TOKENS_PROVIDERS_PREFIXES = Set("nlpcraft:", "google:", "stanford:", "opennlp:", "spacy:")
    private final val ID_REGEX = "^[_a-zA-Z]+[a-zA-Z0-9:-_]*$"

    private final val CLS_INTENT = classOf[NCIntent]
    private final val CLS_INTENT_REF = classOf[NCIntentRef]
    private final val CLS_TERM = classOf[NCIntentTerm]
    private final val CLS_QRY_RES = classOf[NCResult]
    private final val CLS_SLV_CTX = classOf[NCIntentMatch]
    private final val CLS_SAMPLE = classOf[NCIntentSample]

    // Java and scala lists.
    private final val CLS_SCALA_SEQ = classOf[Seq[_]]
    private final val CLS_SCALA_LST = classOf[List[_]]
    private final val CLS_SCALA_OPT = classOf[Option[_]]
    private final val CLS_JAVA_LST = classOf[util.List[_]]
    private final val CLS_JAVA_OPT = classOf[util.Optional[_]]

    private final val CLS_TOKEN = classOf[NCToken]

    private final val COMP_CLS: Set[Class[_]] = Set(
        CLS_SCALA_SEQ,
        CLS_SCALA_LST,
        CLS_SCALA_OPT,
        CLS_JAVA_LST,
        CLS_JAVA_OPT
    )

    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    type Callback = Function[NCIntentMatch, NCResult]

    @volatile private var wrappers: ArrayBuffer[NCModelData] = _
    @volatile private var modelFactory: NCModelFactory = _

    object Config extends NCConfigurable {
        private final val pre = "nlpcraft.probe"

        // It should reload config.
        def modelFactoryType: Option[String] = getStringOpt(s"$pre.modelFactory.type")
        def modelFactoryProps: Option[Map[String, String]] = getMapOpt(s"$pre.modelFactory.properties")
        def models: Seq[String] = getStringList(s"$pre.models")
        def jarsFolder: Option[String] = getStringOpt(s"$pre.jarsFolder")

        // TODO: property name.
        def synonymsWarnValue: Int = getInt(s"$pre.synonymsWarnValue")
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
    private def wrap(mdl: NCModel): NCModelData = {
        val mdlId = mdl.getId

        @throws[NCE]
        def checkCollection(name: String, col: Any): Unit =
            if (col == null)
                throw new NCE(s"Collection can be empty but cannot be null [modelId=$mdlId, name=$name]")

        checkCollection("additionalStopWords", mdl.getAdditionalStopWords)
        checkCollection("elements", mdl.getElements)
        checkCollection("enabledBuiltInTokens", mdl.getEnabledBuiltInTokens)
        checkCollection("excludedStopWords", mdl.getExcludedStopWords)
        checkCollection("parsers", mdl.getParsers)
        checkCollection("suspiciousWords", mdl.getSuspiciousWords)
        checkCollection("macros", mdl.getMacros)
        checkCollection("metadata", mdl.getMetadata)

        checkModelConfig(mdl)

        val allSyns = mdl.getElements.asScala.flatMap(_.getSynonyms.asScala)

        mdl.getMacros.asScala.keys.foreach(makro ⇒
            if (!allSyns.exists(_.contains(makro)))
                logger.warn(s"Unused macro [modelId=$mdlId, macro=$makro]")
        )

        val parser = new NCMacroParser

        // Initialize macro parser.
        mdl.getMacros.asScala.foreach(t ⇒ parser.addMacro(t._1, t._2))

        checkSynonyms(mdl, parser)

        for (elm ← mdl.getElements.asScala)
            checkElement(mdl, elm)

        checkElementIdsDups(mdl)
        checkCyclicDependencies(mdl)

        def checkAndStemmatize(jc: java.util.Set[String], name: String): Set[String] =
            for (word: String ← jc.asScala.toSet) yield
                if (hasWhitespace(word))
                    throw new NCE(s"$name cannot have whitespace: '$word' for model: $mdlId")
                else
                    NCNlpCoreManager.stem(word)

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
                                s"modelId=$mdlId, " +
                                s"max=$maxCnt" +
                                s"]")

                        if (value == null)
                            logger.trace(s"Synonym #${syns.size} added [" +
                                s"modelId=$mdlId, " +
                                s"elementId=$elmId, " +
                                s"synonym=${chunks.mkString(" ")}" +
                                s"]"
                            )
                        else
                            logger.trace(s"Synonym #${syns.size} added [" +
                                s"modelId=$mdlId, " +
                                s"elementId=$elmId, " +
                                s"synonym=${chunks.mkString(" ")}, " +
                                s"value=$value" +
                                s"]"
                            )
                    }
                    else
                        logger.trace(
                            s"Synonym already added (ignoring) [" +
                                s"modelId=$mdlId, " +
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
              * @param s
              * @return
              */
            @throws[NCE]
            def chunkSplit(s: String): Seq[NCSynonymChunk] = {
                val x = s.trim()

                val chunks = ListBuffer.empty[String]

                var start = 0
                var curr = 0
                val len = x.length - (2 + 2) // 2 is a prefix/suffix length. Hack...

                def splitUp(s: String): Seq[String] = s.split(" ").map(_.trim).filter(_.nonEmpty).toSeq

                def processChunk(fix: String): Unit = {
                    chunks ++= splitUp(x.substring(start, curr))

                    x.indexOf(fix, curr + fix.length) match {
                        case -1 ⇒ throw new NCE(s"Invalid synonym definition in: $x, model=$mdlId")
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
              * @param id
              */
            @throws[NCE]
            def chunkIdSplit(id: String): Seq[NCSynonymChunk] = {
                val chunks = chunkSplit(NCNlpCoreManager.tokenize(id).map(_.token).mkString(" "))

                // IDs can only be simple strings.
                if (chunks.exists(_.kind != TEXT))
                    throw new NCE(s"Invalid ID: $id for modelId: $mdlId")

                chunks
            }

            // Add element ID as a synonyms (dups ignored).
            val idChunks = Seq(chunkIdSplit(elmId))

            idChunks.distinct.foreach(ch ⇒ addSynonym(isElementId = true, isValueName = false, null, ch))

            // Add straight element synonyms (dups printed as warnings).
            val synsChunks = for (syn ← elm.getSynonyms.asScala.flatMap(parser.expand)) yield chunkSplit(syn)

            if (U.containsDups(synsChunks.flatten))
                logger.trace(s"Element synonyms duplicate (ignoring) [" +
                    s"modelId=$mdlId, " +
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
                    s"modelId=$mdlId, " +
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
                        s"modelId=$mdlId, " +
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
                logger.warn(s"Duplicate DSL alias '$dupAlias' found for model: $mdlId")

            throw new NCE(s"Duplicate DSL aliases found for model '$mdlId'- check log messages.")
        }

        val idAliasDups = mdl.getElements.asScala.map(_.getId).intersect(allAliases.toSet)

        // Check that DSL aliases don't intersect with element IDs.
        if (idAliasDups.nonEmpty) {
            for (dup ← idAliasDups)
                logger.warn(s"Duplicate element ID and DSL alias '$dup' found for model: $mdlId")

            throw new NCE(s"Duplicate element ID and DSL aliases found for model '$mdlId'- check log messages.")
        }

        // Check for synonym dups across all elements.
        for (
            ((syn, isDirect), holders) ←
                syns.groupBy(p ⇒ (p.synonym.mkString(" "), p.synonym.isDirect)) if holders.size > 1 && isDirect
        ) {
            logger.trace(s"Duplicate synonym detected (ignoring) [" +
                s"modelId=$mdlId, " +
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
                throw new NCE(s"Duplicated synonyms are not allowed for model '$mdlId' - check trace messages.")

            logger.warn(s"Found duplicate synonyms - check trace logging for model: $mdlId")
            logger.warn(s"Duplicates are allowed by '$mdlId' model but large number may degrade the performance.")
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

        // Scan for intent annotations in the model class.
        val intents = scanIntents(mdl)
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

        NCModelData(
            model = mdl,
            solver = solver,
            synonyms = mkFastAccessMap(filter(syns, dsl = false)),
            synonymsDsl = mkFastAccessMap(filter(syns, dsl = true)),
            addStopWordsStems = addStopWords.toSet,
            exclStopWordsStems = exclStopWords.toSet,
            suspWordsStems = suspWords.toSet,
            elements = mdl.getElements.asScala.map(elm ⇒ (elm.getId, elm)).toMap,
            samples = scanSamples(mdl)
        )
    }

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
    private def makeModelWrapper(clsName: String): NCModelData =
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
    private def extractModels(jarFile: File): Seq[NCModelData] = {
        val clsLdr = Thread.currentThread().getContextClassLoader

        val classes = mutable.ArrayBuffer.empty[Class[_ <: NCModel]]

        managed(new JarInputStream(new BufferedInputStream(new FileInputStream(jarFile)))) acquireAndGet { in ⇒
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
        wrappers = ArrayBuffer.empty[NCModelData]

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
            val mdl = w.model
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

        val ids = wrappers.map(_.model.getId).toList

        if (U.containsDups(ids))
            throw new NCE(s"Duplicate model IDs detected: ${ids.mkString(", ")}")

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
    def getModels: Seq[NCModelData] = wrappers

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
                            throw new NCE(s"Cyclic parent dependency starting at model element '${x.getId}', modelId: ${mdl.getId}.")
                        else {
                            seen += parentId

                            x = mdl.getElements.asScala.find(_.getId == parentId) getOrElse {
                                throw new NCE(s"Unknown parent ID '$parentId' for model element '${x.getId}', modelId: ${mdl.getId}.")

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
                throw new NCE(s"Duplicate model element ID '$id', modelId: ${mdl.getId}.")
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
            throw new NCE(s"Model element ID is not provided.', modelId: ${mdl.getId}.")
        else if (elm.getId.length == 0)
            throw new NCE(s"Model element ID cannot be empty.', modelId: ${mdl.getId}.")
        else {
            val elmId = elm.getId

            if (elmId.toLowerCase.startsWith("nlpcraft:"))
                throw new NCE(s"Model element '$elmId' type cannot start with 'nlpcraft:', modelId: ${mdl.getId}.")

            if (hasWhitespace(elmId))
                throw new NCE(s"Model element ID '$elmId' cannot have whitespaces, modelId: ${mdl.getId}.")
        }

    /**
      *
      * @param mdl Model.
      */
    private def checkModelConfig(mdl: NCModel): Unit = {
        def checkInt(v: Int, name: String, min: Int = 0, max: Int = Integer.MAX_VALUE): Unit =
            if (v < min)
                throw new NCE(s"Invalid model configuration value '$name' [value=$v, min=$min], modelId: ${mdl.getId}.")
            else if (v > max)
                throw new NCE(s"Invalid model configuration value '$name' [value=$v, max=$min], modelId: ${mdl.getId}.")

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
            throw new NCE(s"Invalid model 'enabledBuiltInTokens' token IDs: ${unsToks.mkString(", ")}, modelId: ${mdl.getId}.")
    }

    /**
      * Checks whether or not given string has any whitespaces.
      *
      * @param s String to check.
      * @return
      */
    private def hasWhitespace(s: String): Boolean = s.exists(_.isWhitespace)

    /**
      *
      * @param set
      * @param dsl
      */
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


    @throws[NCE]
    private def checkSynonyms(mdl: NCModel, parser: NCMacroParser): Unit = {
        val mdlSyns = mdl.getElements.asScala.map(p ⇒ p.getId → p.getSynonyms.asScala.flatMap(parser.expand))

        val mdlId = mdl.getId

        mdlSyns.foreach { case (elemId, syns) ⇒
            val size = syns.size

            if (size == 0)
                logger.warn(s"Element '$elemId' doesn't have synonyms [modelId=$mdlId]")
            else if (size > Config.synonymsWarnValue)
                logger.warn(
                    s"Element '$elemId' has too many ($size) synonyms. " +
                        s"Make sure this is truly necessary [modelId=$mdlId]"
                )

            val others = mdlSyns.filter {
                case (otherId, _) ⇒ otherId != elemId
            }

            val cross = others.filter {
                case (_, othSyns) ⇒ othSyns.intersect(syns).nonEmpty
            }.toMap.keys.mkString(",")

            if (cross.nonEmpty) {
                val msg = s"Element has duplicate synonyms with element [modelId=$mdlId, elementId=$elemId, cross=$cross]"

                // TODO: it it ok?
                if (mdl.isDupSynonymsAllowed)
                    logger.warn(msg)
                else
                    throw new NCE(msg)
            }
        }
    }

    /**
      *
      * @param cls
      * @return
      */
    private def class2Str(cls: Class[_]): String = if (cls == null) "null" else s"'${cls.getSimpleName}'"

    /**
      *
      * @param m
      * @return
      */
    private def method2Str(m: Method): String = {
        val cls = m.getDeclaringClass.getSimpleName
        val name = m.getName
        val args = m.getParameters.map(_.getType.getSimpleName).mkString(", ")

        s"method '$cls#$name($args)'"
    }

    /**
      *
      * @param m
      * @param argIdx
      * @param cxtFirstParam
      */
    private def arg2Str(m: Method, argIdx: Int, cxtFirstParam: Boolean): String =
        s"argument #${argIdx + (if (cxtFirstParam) 1 else 0)} of ${method2Str(m)}"

    /**
      *
      * @param m
      * @param mdl
      * @param intent
      */
    @throws[NCE]
    private def prepareCallback(m: Method, mdl: NCModel, intent: NCDslIntent): Callback = {
        // Checks method result type.
        if (m.getReturnType != CLS_QRY_RES)
            throw new NCE(s"@NCIntent error - unexpected result type ${class2Str(m.getReturnType)} for ${method2Str(m)}, model=${mdl.getId}")

        val allParamTypes = m.getParameterTypes.toSeq

        val ctxFirstParam = allParamTypes.nonEmpty && allParamTypes.head == CLS_SLV_CTX

        def getTokensSeq[T](data: Seq[T]): Seq[T] =
            if (data == null)
                Seq.empty
            else if (ctxFirstParam)
                data.drop(1)
            else
                data

        val allAnns = m.getParameterAnnotations
        val tokParamAnns = getTokensSeq(allAnns).filter(_ != null)
        val tokParamTypes = getTokensSeq(allParamTypes)

        // Checks tokens parameters annotations count.
        if (tokParamAnns.length != tokParamTypes.length)
            throw new NCE(
                s"@NCIntent error - unexpected annotations count ${tokParamAnns.size} for ${method2Str(m)}, " +
                    s"model=${mdl.getId}"
            )

        // Gets terms identifiers.
        val termIds =
            tokParamAnns.zipWithIndex.
                map { case (anns, idx) ⇒
                    def mkArg: String = arg2Str(m, idx, ctxFirstParam)

                    val annsTerms = anns.filter(_.isInstanceOf[NCIntentTerm])

                    // Each method arguments (second and later) must have one NCIntentTerm annotation.
                    annsTerms.length match {
                        case 1 ⇒ annsTerms.head.asInstanceOf[NCIntentTerm].value()

                        case 0 ⇒
                            throw new NCE(
                                s"@NCIntentTerm error - missed annotation ${class2Str(CLS_TERM)} for $mkArg, " +
                                    s"model=${mdl.getId}"
                            )
                        case _ ⇒
                            throw new NCE(
                                s"@NCIntentTerm error -too many annotations ${class2Str(CLS_TERM)} for $mkArg, " +
                                    s"model=${mdl.getId}"
                            )
                    }
                }

        val terms = intent.terms.toSeq

        // Checks correctness of term IDs.
        // Note we don't restrict them to be duplicated.
        val intentTermIds = terms.filter(_.getId != null).map(_.getId)
        val invalidIds = termIds.filter(id ⇒ !intentTermIds.contains(id))

        if (invalidIds.nonEmpty)
            throw new NCE(
                s"@NCIntentTerm error - invalid term identifiers '${invalidIds.mkString(", ")}' for ${method2Str(m)}" +
                    s"model=${mdl.getId}"
            )

        val paramGenTypes = getTokensSeq(m.getGenericParameterTypes)

        require(tokParamTypes.length == paramGenTypes.length)

        // Checks parameters.
        checkTypes(mdl.getId, m, tokParamTypes, paramGenTypes, ctxFirstParam)

        // Checks limits.
        val allLimits = terms.map(t ⇒ t.getId → (t.getMin, t.getMax)).toMap

        checkMinMax(mdl.getId, m, tokParamTypes, termIds.map(allLimits), ctxFirstParam)

        // Prepares invocation method.
        (ctx: NCIntentMatch) ⇒ {
            invoke(
                m,
                mdl,
                (
                    (if (ctxFirstParam) Seq(ctx)
                    else Seq.empty) ++
                        prepareParams(mdl.getId, m, tokParamTypes, termIds.map(ctx.getTermTokens), ctxFirstParam)
                    ).toArray
            )
        }
    }

    /**
      *
      * @param m
      * @param mdl
      * @param args
      */
    @throws[NCE]
    private def invoke(m: Method, mdl: NCModel, args: Array[AnyRef]): NCResult = {
        var flag = m.canAccess(mdl)

        try {
            if (!flag) {
                m.setAccessible(true)

                flag = true
            }
            else
                flag = false

            m.invoke(mdl, args: _*).asInstanceOf[NCResult]
        }
        catch {
            case e: InvocationTargetException ⇒
                e.getTargetException match {
                    case e: NCIntentSkip ⇒ throw e
                    case e: NCRejection ⇒ throw e
                    case e: NCE ⇒ throw e
                    case e: Throwable ⇒ throw new NCE(s"Invocation error in ${method2Str(m)}, model=${mdl.getId}", e)
                }
            case e: Throwable ⇒ throw new NCE(s"Invocation error in ${method2Str(m)}, model=${mdl.getId}", e)
        }
        finally
            if (flag)
                try
                    m.setAccessible(false)
                catch {
                    case e: SecurityException ⇒ throw new NCE(s"Access error in ${method2Str(m)}, model=${mdl.getId}", e)
                }
    }

    /**
      *
      * @param mdlId
      * @param m
      * @param paramClss
      * @param argsList
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def prepareParams(
        mdlId: String,
        m: Method,
        paramClss: Seq[Class[_]],
        argsList: Seq[util.List[NCToken]],
        ctxFirstParam: Boolean
    ): Seq[AnyRef] =
        paramClss.zip(argsList).zipWithIndex.map { case ((paramCls, argList), i) ⇒
            def mkArg: String = arg2Str(m, i, ctxFirstParam)

            val toksCnt = argList.size()

            // Single token.
            if (paramCls == CLS_TOKEN) {
                if (toksCnt != 1)
                    throw new NCE(s"@NCIntentTerm error - expected single token, but found $toksCnt for $mkArg, model=$mdlId")

                argList.get(0)
            }
            // Array of tokens.
            else if (paramCls.isArray)
                argList.asScala.toArray
            // Scala and java list of tokens.
            else if (paramCls == CLS_SCALA_SEQ)
                argList.asScala
            else if (paramCls == CLS_SCALA_LST)
                argList.asScala.toList
            else if (paramCls == CLS_JAVA_LST)
                argList
            // Scala and java optional token.
            else if (paramCls == CLS_SCALA_OPT)
                toksCnt match {
                    case 0 ⇒ None
                    case 1 ⇒ Some(argList.get(0))
                    case _ ⇒ throw new NCE(s"@NCIntentTerm error - too many tokens $toksCnt for option $mkArg, model=$mdlId")
                }
            else if (paramCls == CLS_JAVA_OPT)
                toksCnt match {
                    case 0 ⇒ util.Optional.empty()
                    case 1 ⇒ util.Optional.of(argList.get(0))
                    case _ ⇒ throw new NCE(s"@NCIntentTerm error - too many tokens $toksCnt for optional $mkArg, model=$mdlId")
                }
            else
            // Arguments types already checked.
                throw new AssertionError(s"Unexpected type $paramCls for $mkArg, model=$mdlId")
        }

    /**
      *
      * @param mdlId
      * @param m
      * @param paramCls
      * @param paramGenTypes
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkTypes(mdlId: String, m: Method, paramCls: Seq[Class[_]], paramGenTypes: Seq[Type], ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == paramGenTypes.length)

        paramCls.zip(paramGenTypes).zipWithIndex.foreach { case ((pClass, pGenType), i) ⇒
            def mkArg: String = arg2Str(m, i, ctxFirstParam)

            // Token.
            if (pClass == CLS_TOKEN) {
                // No-op.
            }
            else if (pClass.isArray) {
                val compType = pClass.getComponentType

                if (compType != CLS_TOKEN)
                    throw new NCE(s"@NCIntentTerm error - unexpected array element type ${class2Str(compType)} for $mkArg, model=$mdlId")
            }
            // Tokens collection and optionals.
            else if (COMP_CLS.contains(pClass))
                pGenType match {
                    case pt: ParameterizedType ⇒
                        val actTypes = pt.getActualTypeArguments
                        val compTypes = if (actTypes == null) Seq.empty else actTypes.toSeq

                        if (compTypes.length != 1)
                            throw new NCE(
                                s"@NCIntentTerm error - unexpected generic types count ${compTypes.length} for $mkArg, model=$mdlId"
                            )

                        val compType = compTypes.head

                        compType match {
                            case _: Class[_] ⇒
                                val genClass = compTypes.head.asInstanceOf[Class[_]]

                                if (genClass != CLS_TOKEN)
                                    throw new NCE(
                                        s"@NCIntentTerm error - unexpected generic type ${class2Str(genClass)} for $mkArg, model=$mdlId"
                                    )
                            case _ ⇒
                                throw new NCE(
                                    s"@NCIntentTerm error - unexpected generic type ${compType.getTypeName} for $mkArg, model=$mdlId"
                                )
                        }

                    case _ ⇒ throw new NCE(
                        s"@NCIntentTerm error - unexpected parameter type ${pGenType.getTypeName} for $mkArg, model=$mdlId"
                    )
                }
            // Other types.
            else
                throw new NCE(s"@NCIntentTerm error - unexpected parameter type ${class2Str(pClass)} for $mkArg, model=$mdlId")
        }
    }

    /**
      *
      * @param mdlId
      * @param m
      * @param paramCls
      * @param limits
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkMinMax(mdlId: String, m: Method, paramCls: Seq[Class[_]], limits: Seq[(Int, Int)], ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == limits.length)

        paramCls.zip(limits).zipWithIndex.foreach { case ((cls, (min, max)), i) ⇒
            def mkArg: String = arg2Str(m, i, ctxFirstParam)

            // Argument is single token but defined as not single token.
            if (cls == CLS_TOKEN && (min != 1 || max != 1))
                throw new NCE(
                    s"@NCIntentTerm error - term must have [1,1] quantifier for $mkArg " +
                        s"because this argument is a single value, model=$mdlId"
                )
            // Argument is not single token but defined as single token.
            else if (cls != CLS_TOKEN && (min == 1 && max == 1))
                throw new NCE(
                    s"@NCIntentTerm error - term has [1,1] quantifier for $mkArg " +
                        s"but this argument is not a single value, model=$mdlId"
                )
            // Argument is optional but defined as not optional.
            else if ((cls == CLS_SCALA_OPT || cls == CLS_JAVA_OPT) && (min != 0 || max != 1))
                throw new NCE(
                    s"@NCIntentTerm error - term must have [0,1] quantifier for $mkArg " +
                        s"because this argument is optional, model=$mdlId"
                )
            // Argument is not optional but defined as optional.
            else if ((cls != CLS_SCALA_OPT && cls != CLS_JAVA_OPT) && (min == 0 && max == 1))
                throw new NCE(
                    s"@NCIntentTerm error - term has [0,1] quantifier for $mkArg " +
                        s"but this argument is not optional, model=$mdlId"
                )
        }
    }

    /**
      *
      * @param mdl
      */
    @throws[NCE]
    private def scanIntents(mdl: NCModel): Map[NCDslIntent, Callback] =
        mdl.getClass.getDeclaredMethods.flatMap(m ⇒ {
            // Direct in-the-class and referenced intents.
            val clsArr = m.getAnnotationsByType(CLS_INTENT)
            val refArr = m.getAnnotationsByType(CLS_INTENT_REF)

            if (clsArr.length > 1 || refArr.length > 1 || (clsArr.nonEmpty && refArr.nonEmpty))
                throw new NCE(
                    s"Only one @NCIntent or @NCIntentRef annotation is allowed in: ${method2Str(m)}, model=${mdl.getId}"
                )

            val cls = m.getAnnotation(CLS_INTENT)

            if (cls != null)
                Some(NCIntentDslCompiler.compile(cls.value(), mdl.getId), m)
            else {
                val ref = m.getAnnotation(CLS_INTENT_REF)

                if (ref != null)
                    mdl match {
                        case adapter: NCModelFileAdapter ⇒
                            val refId = ref.value().trim

                            val compiledIntents = adapter
                                .getIntents
                                .asScala
                                .map(NCIntentDslCompiler.compile(_, mdl.getId))

                            U.getDups(compiledIntents.toSeq.map(_.id)) match {
                                case ids if ids.nonEmpty ⇒
                                    throw new NCE(
                                        s"Duplicate intent IDs found for model from " +
                                            s"'${adapter.getOrigin}': ${ids.mkString(",")}, model=${mdl.getId}"
                                    )
                                case _ ⇒ ()
                            }

                            compiledIntents.find(_.id == refId) match {
                                case Some(intent) ⇒ Some(intent, m)
                                case None ⇒
                                    throw new NCE(
                                        s"@IntentRef($refId) references unknown intent ID '$refId' " +
                                            s"in ${method2Str(m)}, model=${mdl.getId}"
                                    )
                            }

                        case _ ⇒
                            throw new NCE(
                                s"@IntentRef annotation in ${method2Str(m)} can be used only " +
                                    s"for models extending 'NCModelFileAdapter', model=${mdl.getId}"
                            )
                    }
                else
                    None
            }
        })
            .map {
                case (intent, m) ⇒ intent → prepareCallback(m, mdl, intent)
            }.toMap

    /**
      * Scans given model for intent samples.
      *
      * @param mdl Model to scan.
      */
    @throws[NCE]
    private def scanSamples(mdl: NCModel): Map[String, Seq[String]] = {
        var annFound = false

        val samples =
            mdl.getClass.getDeclaredMethods.flatMap(method ⇒ {
                def mkMethodName: String = s"${method.getDeclaringClass.getName}#${method.getName}(...)"

                val smpAnn = method.getAnnotation(CLS_SAMPLE)
                val intAnn = method.getAnnotation(CLS_INTENT)
                val refAnn = method.getAnnotation(CLS_INTENT_REF)

                if (smpAnn != null || intAnn != null || refAnn != null) {
                    annFound = true

                    def mkIntentId(): String =
                        if (intAnn != null)
                            NCIntentDslCompiler.compile(intAnn.value(), mdl.getId).id
                        else if (refAnn != null)
                            refAnn.value().trim
                        else
                            throw new AssertionError()

                    if (smpAnn != null) {
                        if (intAnn == null && refAnn == null) {
                            logger.warn(
                                "@NCTestSample annotation without corresponding @NCIntent or @NCIntentRef annotations " +
                                    s"[modelId=${mdl.getId}, callback=$mkMethodName]")

                            None
                        }
                        else {
                            val samples = smpAnn.value().toList

                            if (samples.isEmpty) {
                                logger.warn(
                                    "@NCTestSample annotation is empty " +
                                        s"[modelId=${mdl.getId}, callback=$mkMethodName]"
                                )

                                None
                            }
                            else
                                Some(mkIntentId() → samples)
                        }
                    }
                    else {
                        logger.warn(
                            "@NCTestSample annotation is missing " +
                                s"[modelId=${mdl.getId}, callback=$mkMethodName]"
                        )

                        None
                    }
                }
                else
                    None
            }).toMap

        if (!annFound)
            logger.warn(s"No intents found [modelId=${mdl.getId}")

        val parser = new NCMacroParser

        mdl.getMacros.asScala.foreach { case (name, str) ⇒ parser.addMacro(name, str) }

        val allSyns: Set[Seq[String]] =
            mdl.getElements.
                asScala.
                flatMap(_.getSynonyms.asScala.flatMap(parser.expand)).
                map(NCNlpPorterStemmer.stem).map(_.split(" ").toSeq).
                toSet

        samples.
            flatMap { case (_, samples) ⇒ samples.map(_.toLowerCase) }.
            map(s ⇒ s → SEPARATORS.foldLeft(s)((s, ch) ⇒ s.replaceAll(s"\\$ch", s" $ch "))).
            foreach {
                case (s, sNorm) ⇒
                    val seq: Seq[String] = sNorm.split(" ").map(NCNlpPorterStemmer.stem)

                    if (!allSyns.exists(_.intersect(seq).nonEmpty))
                        logger.warn(s"Intent sample doesn't contain any direct synonyms [modelId=${mdl.getId}, sample=$s]")
            }

        samples
    }
}
