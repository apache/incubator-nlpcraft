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
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.{NCNlpCoreManager, NCNlpPorterStemmer}
import org.apache.nlpcraft.common.util.NCUtils.{DSL_FIX, REGEX_FIX}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.factories.basic.NCBasicModelFactory
import org.apache.nlpcraft.model.intent.impl.{NCIntentDslCompiler, NCIntentSolver}
import org.apache.nlpcraft.model.intent.utils.NCDslIntent
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind.{DSL, REGEX, TEXT}
import org.apache.nlpcraft.probe.mgrs.{NCProbeModel, NCProbeSynonym, NCProbeSynonymChunk}
import org.apache.nlpcraft.probe.mgrs.model.NCModelSynonymDslCompiler
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

    @volatile private var data: ArrayBuffer[NCProbeModel] = _
    @volatile private var mdlFactory: NCModelFactory = _

    object Config extends NCConfigurable {
        private final val pre = "nlpcraft.probe"

        // It should reload config.
        def modelFactoryType: Option[String] = getStringOpt(s"$pre.modelFactory.type")
        def modelFactoryProps: Option[Map[String, String]] = getMapOpt(s"$pre.modelFactory.properties")
        def models: String = getString(s"$pre.models")
        def jarsFolder: Option[String] = getStringOpt(s"$pre.jarsFolder")
    }

    /**
      *
      * @param elmId Element ID.
      * @param syn Element synonym.
      */
    case class SynonymHolder(elmId: String, syn: NCProbeSynonym)

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
      */
    private def checkMacros(mdl: NCModel): Unit = {
        val macros = mdl.getMacros.asScala
        val set = mdl.getElements.asScala.flatMap(_.getSynonyms.asScala) ++ macros.values

        for (makro ← macros.keys if !set.exists(_.contains(makro)))
            logger.warn(s"Unused macro detected [mdlId=${mdl.getId}, macro=$makro]")
    }

    /**
      *
      * @param mdl
      * @return
      */
    @throws[NCE]
    private def wrap(mdl: NCModel): NCProbeModel = {
        require(mdl != null)

        checkModelConfig(mdl)

        val mdlId = mdl.getId

        for (elm ← mdl.getElements.asScala)
            if (!elm.getId.matches(ID_REGEX))
                throw new NCE(s"Model element ID does not match regex [" +
                    s"mdlId=$mdlId, " +
                    s"elmId=${elm.getId}, " +
                    s"regex=$ID_REGEX" +
                s"]")

        checkMacros(mdl)

        val parser = new NCMacroParser

        // Initialize macro parser.
        mdl.getMacros.asScala.foreach(t ⇒ parser.addMacro(t._1, t._2))

        for (elm ← mdl.getElements.asScala)
            checkElement(mdl, elm)

        checkElementIdsDups(mdl)
        checkCyclicDependencies(mdl)

        /**
         *
         * @param jc
         * @param name
         * @return
         */
        def checkAndStemmatize(jc: java.util.Set[String], name: String): Set[String] =
            for (word: String ← jc.asScala.toSet) yield
                if (hasWhitespace(word))
                    throw new NCE(s"Model property cannot contain a string with whitespaces [" +
                        s"mdlId=$mdlId, " +
                        s"name=$name, " +
                        s"word='$word'" +
                    s"]")
                else
                    NCNlpCoreManager.stem(word)

        val addStopWords = checkAndStemmatize(mdl.getAdditionalStopWords, "additionalStopword")
        val exclStopWords = checkAndStemmatize(mdl.getExcludedStopWords, "excludedStopword")
        val suspWords = checkAndStemmatize(mdl.getSuspiciousWords, "suspiciousWord")

        checkStopwordsDups(mdlId, addStopWords, exclStopWords)

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
                chunks: Seq[NCProbeSynonymChunk]): Unit = {
                def add(chunks: Seq[NCProbeSynonymChunk], isDirect: Boolean): Unit = {
                    val holder = SynonymHolder(
                        elmId = elmId,
                        syn = NCProbeSynonym(isElementId, isValueName, isDirect, value, chunks)
                    )

                    if (syns.add(holder)) {
                        cnt += 1

                        if (cnt > maxCnt)
                            throw new NCE(s"Too many total synonyms detected [" +
                                s"mdlId=$mdlId, " +
                                s"cnt=$cnt, " +
                                s"max=$maxCnt" +
                            s"]")

                        logger.trace(
                            s"Synonym #${syns.size} added [" +
                                s"mdlId=$mdlId, " +
                                s"elmId=$elmId, " +
                                s"syn=${chunks.mkString(" ")}, " +
                                s"value=${if (value == null) "<null>" else value}" +
                            s"]"
                        )
                    }
                    else
                        logger.trace(
                            s"Synonym already added (safely ignoring) [" +
                                s"mdlId=$mdlId, " +
                                s"elmId=$elmId, " +
                                s"syn=${chunks.mkString(" ")}, " +
                                s"value=${if (value == null) "<null>" else value}" +
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
            def chunkSplit(s: String): Seq[NCProbeSynonymChunk] = {
                val x = s.trim()

                val chunks = ListBuffer.empty[String]

                var start = 0
                var curr = 0
                val len = x.length - (2 + 2) // 2 is a prefix/suffix length. Hack...

                def splitUp(s: String): Seq[String] = s.split(" ").map(_.trim).filter(_.nonEmpty).toSeq

                def processChunk(fix: String): Unit = {
                    chunks ++= splitUp(x.substring(start, curr))

                    x.indexOf(fix, curr + fix.length) match {
                        case -1 ⇒
                            throw new NCE(s"Invalid synonym definition [" +
                                s"mdlId=$mdlId, " +
                                s"chunks=$x" +
                            s"]")

                        case n ⇒
                            chunks += x.substring(curr, n + fix.length)
                            start = n + fix.length
                            curr = start
                    }
                }

                def isFix(fix: String): Boolean =
                    x.charAt(curr) == fix.charAt(0) && x.charAt(curr + 1) == fix.charAt(1)

                while (curr < len) {
                    if (isFix(REGEX_FIX))
                        processChunk(REGEX_FIX)
                    else if (isFix(DSL_FIX))
                        processChunk(DSL_FIX)
                    else
                        curr += 1
                }

                chunks ++= splitUp(x.substring(start))

                chunks.map(mkChunk(mdlId, _))
            }

            /**
              *
              * @param id
              */
            @throws[NCE]
            def chunkIdSplit(id: String): Seq[NCProbeSynonymChunk] = {
                val chunks = chunkSplit(NCNlpCoreManager.tokenize(id).map(_.token).mkString(" "))

                // IDs can only be simple strings.
                if (chunks.exists(_.kind != TEXT))
                    throw new NCE(s"Invalid element or value ID format [" +
                        s"mdlId=$mdlId, " +
                        s"id=$id" +
                    s"]")

                chunks
            }

            // Add element ID as a synonyms.
            Seq(chunkIdSplit(elmId))
                .distinct
                .foreach(chunks ⇒ addSynonym(
                    isElementId = true,
                    isValueName = false,
                    null,
                    chunks
                ))

            // Add straight element synonyms.
            (for (syn ← elm.getSynonyms.asScala.flatMap(parser.expand)) yield chunkSplit(syn))
                .distinct
                .foreach(chunks ⇒ addSynonym(
                    isElementId = false,
                    isValueName = false,
                    null,
                    chunks
                ))

            val vals =
                (if (elm.getValues != null) elm.getValues.asScala else Seq.empty) ++
                (if (elm.getValueLoader != null) elm.getValueLoader.load(elm).asScala else Seq.empty)

            // Add value synonyms.
            for (v ← vals.map(p ⇒ p.getName → p).toMap.values) {
                val valName = v.getName
                val valSyns = v.getSynonyms.asScala

                val nameChunks = Seq(chunkIdSplit(valName))

                // Add value name as a synonyms.
                nameChunks.distinct.foreach(chunks ⇒ addSynonym(
                    isElementId = false,
                    isValueName = true,
                    valName,
                    chunks
                ))

                var skippedOneLikeName = false

                val chunks = valSyns.flatMap(parser.expand).flatMap(valSyn ⇒ {
                    val valSyns = chunkSplit(valSyn)

                    if (nameChunks.contains(valSyns) && !skippedOneLikeName) {
                        skippedOneLikeName = true

                        None
                    }
                    else
                        Some(valSyns)
                })

                chunks.distinct.foreach(chunks ⇒ addSynonym(
                    isElementId = false,
                    isValueName = false,
                    valName,
                    chunks
                ))
            }
        }

        // Discard value loaders.
        for (elm ← mdl.getElements.asScala) {
            val ldr = elm.getValueLoader

            if (ldr != null)
                ldr.onDiscard()
        }

        val allAliases = syns
            .flatMap(_.syn)
            .groupBy(_.origText)
            .map(x ⇒ (x._1, x._2.map(_.alias).filter(_ != null)))
            .values
            .flatten
            .toList

        // Check for DSl alias uniqueness.
        if (U.containsDups(allAliases))
            throw new NCE(s"Duplicate DSL alias found [" +
                s"mdlId=$mdlId, " +
                s"dups=${allAliases.diff(allAliases.distinct).mkString(", ")}" +
            s"]")

        val idAliasDups = mdl.getElements.asScala.map(_.getId).intersect(allAliases.toSet)

        // Check that DSL aliases don't intersect with element IDs.
        if (idAliasDups.nonEmpty)
            throw new NCE(s"Model element IDs and DSL aliases intersect [" +
                s"mdlId=$mdlId, " +
                s"dups=${idAliasDups.mkString(", ")}" +
            "]")

        val dupSyns = mutable.Buffer.empty[(Seq[String], String)]

        // Check for synonym dups across all elements.
        for (
            ((syn, isDirect), holders) ←
                syns.groupBy(p ⇒ (p.syn.mkString(" "), p.syn.isDirect)) if holders.size > 1 && isDirect
        ) {
            dupSyns.append((
                holders.map(p ⇒ s"id=${p.elmId}${if (p.syn.value == null) "" else s", value=${p.syn.value}"}").toSeq,
                syn
            ))
        }

        if (dupSyns.nonEmpty) {
            val tbl = NCAsciiTable("Elements", "Dup Synonym")

            dupSyns.foreach(row ⇒ tbl += (
                row._1,
                row._2
            ))

            if (mdl.isDupSynonymsAllowed) {
                logger.trace(s"Duplicate synonyms found in '$mdlId' model:\n${tbl.toString}")

                logger.warn(s"Duplicate synonyms found in '$mdlId' model - turn on TRACE logging to see them.")
                logger.warn(s"Model '$mdlId' allows duplicate synonyms but the large number may degrade the performance.")
            } else {
                logger.warn(s"Duplicate synonyms found in '$mdlId' model:\n${tbl.toString}")

                throw new NCE(s"Duplicated synonyms found and not allowed - check warning messages [mdlId=$mdlId]")
            }
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
                case ids if ids.nonEmpty ⇒
                    throw new NCE(s"Duplicate intent IDs found [" +
                        s"mdlId=$mdlId, " +
                        s"ids=${ids.mkString(",")}" +
                    s"]")
                case _ ⇒ ()
            }

            solver = new NCIntentSolver(
                intents.toList.map(x ⇒ (x._1, (z: NCIntentMatch) ⇒ x._2.apply(z)))
            )
        }
        else
            logger.warn(s"Model has no intent: $mdlId")

        NCProbeModel(
            model = mdl,
            solver = solver,
            intents = intents.keySet.toSeq,
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
            case Left(e) ⇒ throw new NCE(s"Failed to instantiate model factory for: $clsName", e)
            case Right(factory) ⇒ factory
        }

    /**
      *
      * @param clsName Model class name.
      */
    @throws[NCE]
    private def makeModelWrapper(clsName: String): NCProbeModel =
        try
            wrap(
                makeModelFromSource(
                    Thread.currentThread().getContextClassLoader.loadClass(clsName).asSubclass(classOf[NCModel]),
                    clsName
                )
            )
        catch {
            case e: Throwable ⇒ throw new NCE(s"Failed to instantiate model for: $clsName", e)
        }

    /**
      *
      * @param set
      * @return
      */
    private def mkFastAccessMap(set: Set[SynonymHolder]): Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , Seq[NCProbeSynonym]]] =
        set
            .groupBy(_.elmId)
            .map {
                case (elmId, holders) ⇒ (
                    elmId,
                    holders
                        .map(_.syn)
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
        catching(classOf[Throwable]) either mdlFactory.mkModel(cls) match {
            case Left(e) ⇒
                throw new NCE(s"Failed to instantiate model [" +
                    s"cls=${cls.getName}, " +
                    s"factory=${mdlFactory.getClass.getName}, " +
                    s"src=$src" +
                "]", e)

            case Right(model) ⇒ model
        }

    /**
      *
      * @param jarFile JAR file to extract from.
      */
    @throws[NCE]
    private def extractModels(jarFile: File): Seq[NCProbeModel] = {
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

    /**
     *
     * @param parent Optional parent span.
     * @throws NCE
     * @return
     */
    @throws[NCE]
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ ⇒
        data = ArrayBuffer.empty[NCProbeModel]

        mdlFactory = new NCBasicModelFactory

        // Initialize model factory (if configured).
        Config.modelFactoryType match {
            case Some(mft) ⇒
                mdlFactory = makeModelFactory(mft)

                mdlFactory.initialize(Config.modelFactoryProps.getOrElse(Map.empty[String, String]).asJava)

            case None ⇒ // No-op.
        }

        data ++= Config.models.split(",").map(_.trim).map(makeModelWrapper)

        Config.jarsFolder match {
            case Some(jarsFolder) ⇒
                val jarsFile = new File(jarsFolder)

                if (!jarsFile.exists())
                    throw new NCE(s"Probe configuration JAR folder path does not exist: $jarsFolder")
                if (!jarsFile.isDirectory)
                    throw new NCE(s"Probe configuration JAR folder path is not a directory: $jarsFolder")

                val src = this.getClass.getProtectionDomain.getCodeSource
                val locJar = if (src == null) null else new File(src.getLocation.getPath)

                for (jar ← scanJars(jarsFile) if jar != locJar)
                    data ++= extractModels(jar)

            case None ⇒ // No-op.
        }

        val ids = data.map(_.model.getId).toList

        if (U.containsDups(ids))
            throw new NCE(s"Duplicate model IDs detected: ${ids.mkString(", ")}")

        ackStart()
    }

    /**
     *
     * @param parent Optional parent span.
     * @throws NCE
     */
    @throws[NCE]
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ ⇒
        if (mdlFactory != null)
            mdlFactory.terminate()

        if (data != null)
            data.clear()

        ackStop()
    }

    /**
      *
      * @return
      */
    def getModels: Seq[NCProbeModel] = data

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
                            throw new NCE(s"Cyclic parent dependency starting at model element [" +
                                s"mdlId=${mdl.getId}, " +
                                s"elmId=${x.getId}" +
                            s"]")
                        else {
                            seen += parentId

                            x = mdl.getElements.asScala.find(_.getId == parentId) getOrElse {
                                throw new NCE(s"Unknown parent ID for model element [" +
                                    s"mdlId=${mdl.getId}, " +
                                    s"parentId=${x.getId}" +
                                s"]")

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
                throw new NCE(s"Duplicate model element ID [" +
                    s"mdlId=${mdl.getId}, " +
                    s"elmId=$id" +
                s"]")
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
            throw new NCE(s"Model element ID is not provided [" +
                s"mdlId=${mdl.getId}, " +
                s"elm=${elm.toString}" +
            s"]")
        else if (elm.getId.length == 0)
            throw new NCE(s"Model element ID cannot be empty [" +
                s"mdlId=${mdl.getId}, " +
                s"elm=${elm.toString}]" +
            s"]")
        else {
            val elmId = elm.getId

            if (elmId.toLowerCase.startsWith("nlpcraft:"))
                throw new NCE(s"Model element ID type cannot start with 'nlpcraft:' [" +
                    s"mdlId=${mdl.getId}, " +
                    s"elmId=$elmId" +
                s"]")

            if (hasWhitespace(elmId))
                throw new NCE(s"Model element ID cannot have whitespaces [" +
                    s"mdlId=${mdl.getId}, " +
                    s"elmId=$elmId" +
                s"]")
        }

    /**
      *
      * @param mdl Model.
      */
    private def checkModelConfig(mdl: NCModel): Unit = {
        val mdlId = mdl.getId

        @throws[NCE]
        def checkMandatoryString(value: String, name: String, maxLen: Int): Unit =
            if (value == null)
                throw new NCE(s"Model property not provided [" +
                    s"mdlId=$mdlId, " +
                    s"name=$name" +
                s"]")
            else if (value.isEmpty)
                throw new NCE(s"Model property cannot be empty [" +
                    s"mdlId=$mdlId, " +
                    s"name=$name" +
                s"]")
            else if (value.length > maxLen)
                throw new NCE(s"Model property is too long (max length $maxLen) [" +
                    s"mdlId=$mdlId, " +
                    s"name=$name, " +
                    s"length=${value.length}" +
                s"]")

        @throws[NCE]
        def checkNum(v: Long, name: String, min: Long = 0L, max: Long = Long.MaxValue): Unit =
            if (v < min || v > max)
                throw new NCE(s"Model property is out of range [" +
                    s"mdlId=$mdlId, " +
                    s"name=$name, " +
                    s"value=$v," +
                    s"min=$min, " +
                    s"max=$max" +
                s"]")

        @throws[NCE]
        def checkCollection(name: String, col: Any): Unit =
            if (col == null)
                throw new NCE(s"Model property can be empty but cannot be null [" +
                    s"mdlId=$mdlId, " +
                    s"name=$name" +
                s"]")

        checkMandatoryString(mdl.getId, "id", 32)
        checkMandatoryString(mdl.getName, "name", 64)
        checkMandatoryString(mdl.getVersion, "version", 16)

        checkNum(mdl.getConversationTimeout, "conversationTimeout")
        checkNum(mdl.getMaxUnknownWords, "maxUnknownWords")
        checkNum(mdl.getMaxFreeWords, "maxFreeWords")
        checkNum(mdl.getMaxSuspiciousWords, "maxSuspiciousWords")
        checkNum(mdl.getMinWords, "minWords", min = 1)
        checkNum(mdl.getMinNonStopwords, "minNonStopwords")
        checkNum(mdl.getMinTokens, "minTokens")
        checkNum(mdl.getMaxTokens, "maxTokens", max = 100)
        checkNum(mdl.getMaxWords, "maxWords", min = 1, max = 100)
        checkNum(mdl.getJiggleFactor, "jiggleFactor", max = 4)
        checkNum(mdl.getMaxElementSynonyms, "maxSynonymsThreshold", min = 1)
        checkNum(mdl.getConversationDepth, "conversationDepth", min = 1)

        checkCollection("additionalStopWords", mdl.getAdditionalStopWords)
        checkCollection("elements", mdl.getElements)
        checkCollection("enabledBuiltInTokens", mdl.getEnabledBuiltInTokens)
        checkCollection("excludedStopWords", mdl.getExcludedStopWords)
        checkCollection("parsers", mdl.getParsers)
        checkCollection("suspiciousWords", mdl.getSuspiciousWords)
        checkCollection("macros", mdl.getMacros)
        checkCollection("metadata", mdl.getMetadata)

        val unsToks =
            mdl.getEnabledBuiltInTokens.asScala.filter(t ⇒
                // 'stanford', 'google', 'opennlp', 'spacy' - any names, not validated.
                t == null ||
                !TOKENS_PROVIDERS_PREFIXES.exists(typ ⇒ t.startsWith(typ)) ||
                // 'nlpcraft' names validated.
                (t.startsWith("nlpcraft:") && !NCModelView.DFLT_ENABLED_BUILTIN_TOKENS.contains(t))
            )

        if (unsToks.nonEmpty)
            throw new NCE(s"Invalid token IDs for 'enabledBuiltInTokens' model property [" +
                s"mdlId=${mdl.getId}, " +
                s"ids=${unsToks.mkString(", ")}" +
            s"]")
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
            val b = s.syn.exists(_.kind == DSL)

            if (dsl) b else !b
        })

    /**
     *
     * @param mdlId Model ID.
     * @param chunk Synonym chunk.
     * @return
     */
    @throws[NCE]
    private def mkChunk(mdlId: String, chunk: String): NCProbeSynonymChunk = {
        def stripSuffix(fix: String, s: String): String = s.slice(fix.length, s.length - fix.length)

        // Regex synonym.
        if (startsAndEnds(REGEX_FIX, chunk)) {
            val ptrn = stripSuffix(REGEX_FIX, chunk)

            if (ptrn.length > 0)
                try
                    NCProbeSynonymChunk(kind = REGEX, origText = chunk, regex = Pattern.compile(ptrn))
                catch {
                    case e: PatternSyntaxException ⇒
                        throw new NCE(s"Invalid regex synonym syntax detected [" +
                            s"mdlId=$mdlId, " +
                            s"chunk=$chunk" +
                        s"]", e)
                }
            else
                throw new NCE(s"Empty regex synonym detected [" +
                    s"mdlId=$mdlId, " +
                    s"chunk=$chunk" +
                s"]")
        }
        // DSL-based synonym.
        else if (startsAndEnds(DSL_FIX, chunk)) {
            val dsl = stripSuffix(DSL_FIX, chunk)
            val compUnit = NCModelSynonymDslCompiler.parse(dsl)

            val x = NCProbeSynonymChunk(alias = compUnit.alias, kind = DSL, origText = chunk, dslPred = compUnit.predicate)

            x
        }
        // Regular word.
        else
            NCProbeSynonymChunk(kind = TEXT, origText = chunk, wordStem = NCNlpCoreManager.stem(chunk))
    }

    /**
     *
     * @param mdlId Model ID.
     * @param adds Additional stopword stems.
     * @param excls Excluded stopword stems.
     */
    @throws[NCE]
    private def checkStopwordsDups(mdlId: String, adds: Set[String], excls: Set[String]): Unit = {
        val dups = adds.intersect(excls)

        if (dups.nonEmpty)
            throw new NCE(s"Duplicate stems detected between additional and excluded stopwords [" +
                s"mdlId=$mdlId, " +
                s"dups=${dups.mkString(",")}" +
            s"]")
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
      * @param cls
      * @return
      */
    private def class2Str(cls: Class[_]): String = if (cls == null) "null" else s"'${cls.getSimpleName}'"

    /**
      *
      * @param mtd
      * @return
      */
    private def method2Str(mtd: Method): String = {
        val cls = mtd.getDeclaringClass.getSimpleName
        val name = mtd.getName
        val args = mtd.getParameters.map(_.getType.getSimpleName).mkString(", ")

        s"$cls#$name($args)"
    }

    /**
      *
      * @param mtd
      * @param argIdx
      * @param cxtFirstParam
      */
    private def arg2Str(mtd: Method, argIdx: Int, cxtFirstParam: Boolean): String =
        s"#${argIdx + (if (cxtFirstParam) 1 else 0)} of ${method2Str(mtd)}"

    /**
      *
      * @param mtd
      * @param mdl
      * @param intent
      */
    @throws[NCE]
    private def prepareCallback(mtd: Method, mdl: NCModel, intent: NCDslIntent): Callback = {
        val mdlId = mdl.getId

        // Checks method result type.
        if (mtd.getReturnType != CLS_QRY_RES)
            throw new NCE(s"Unexpected result type for @NCIntent annotated method [" +
                s"mdlId=$mdlId, " +
                s"type=${class2Str(mtd.getReturnType)}, " +
                s"callback=${method2Str(mtd)}" +
            s"]")

        val allParamTypes = mtd.getParameterTypes.toSeq

        val ctxFirstParam = allParamTypes.nonEmpty && allParamTypes.head == CLS_SLV_CTX

        def getTokensSeq[T](data: Seq[T]): Seq[T] =
            if (data == null)
                Seq.empty
            else if (ctxFirstParam)
                data.drop(1)
            else
                data

        val allAnns = mtd.getParameterAnnotations
        val tokParamAnns = getTokensSeq(allAnns).filter(_ != null)
        val tokParamTypes = getTokensSeq(allParamTypes)

        // Checks tokens parameters annotations count.
        if (tokParamAnns.length != tokParamTypes.length)
            throw new NCE(s"Unexpected annotations count for @NCIntent annotated method [" +
                s"mdlId=$mdlId, " +
                s"count=${tokParamAnns.size}, " +
                s"callback=${method2Str(mtd)}" +
            s"]")

        // Gets terms identifiers.
        val termIds = tokParamAnns.toList.zipWithIndex.map {
            case (anns, idx) ⇒
                def mkArg(): String = arg2Str(mtd, idx, ctxFirstParam)

                val annsTerms = anns.filter(_.isInstanceOf[NCIntentTerm])

                // Each method arguments (second and later) must have one NCIntentTerm annotation.
                annsTerms.length match {
                    case 1 ⇒ annsTerms.head.asInstanceOf[NCIntentTerm].value()

                    case 0 ⇒
                        throw new NCE(s"Missing @NCIntentTerm annotation for [" +
                            s"mdlId=$mdlId, " +
                            s"arg=${mkArg()}" +
                        s"]")

                    case _ ⇒
                        throw new NCE(s"Too many @NCIntentTerm annotations for [" +
                            s"mdlId=$mdlId, " +
                            s"arg=${mkArg()}" +
                        s"]")
                }
            }

        if (U.containsDups(termIds))
            throw new NCE(s"Duplicate term IDs in @NCIntentTerm annotations [" +
                s"mdlId=$mdlId, " +
                s"dups=${U.getDups(termIds).mkString(", ")}, " +
                s"callback=${method2Str(mtd)}" +
            s"]")

        val terms = intent.terms.toSeq

        // Checks correctness of term IDs.
        // Note we don't restrict them to be duplicated.
        val intentTermIds = terms.filter(_.getId != null).map(_.getId)
        val invalidIds = termIds.filter(id ⇒ !intentTermIds.contains(id))

        if (invalidIds.nonEmpty) {
            // Report only the first one for simplicity & clarity.
            throw new NCE(s"Unknown term ID in @NCIntentTerm annotation [" +
                s"mdlId=$mdlId, " +
                s"id='${invalidIds.head}', " +
                s"callback=${method2Str(mtd)}" +
            s"]")
        }

        val paramGenTypes = getTokensSeq(mtd.getGenericParameterTypes)

        require(tokParamTypes.length == paramGenTypes.length)

        // Checks parameters.
        checkTypes(mdlId, mtd, tokParamTypes, paramGenTypes, ctxFirstParam)

        // Checks limits.
        val allLimits = terms.map(t ⇒ t.getId → (t.getMin, t.getMax)).toMap

        checkMinMax(mdlId, mtd, tokParamTypes, termIds.map(allLimits), ctxFirstParam)

        // Prepares invocation method.
        (ctx: NCIntentMatch) ⇒ {
            invoke(
                mtd,
                mdl,
                (
                    (if (ctxFirstParam) Seq(ctx)
                    else Seq.empty) ++
                        prepareParams(mdlId, mtd, tokParamTypes, termIds.map(ctx.getTermTokens), ctxFirstParam)
                    ).toArray
            )
        }
    }

    /**
      *
      * @param mtd
      * @param mdl
      * @param args
      */
    @throws[NCE]
    private def invoke(mtd: Method, mdl: NCModel, args: Array[AnyRef]): NCResult = {
        val mdlId = mdl.getId

        var flag = mtd.canAccess(mdl)

        try {
            if (!flag) {
                mtd.setAccessible(true)

                flag = true
            }
            else
                flag = false

            mtd.invoke(mdl, args: _*).asInstanceOf[NCResult]
        }
        catch {
            case e: InvocationTargetException ⇒ e.getTargetException match {
                case e: NCIntentSkip ⇒ throw e
                case e: NCRejection ⇒ throw e
                case e: NCE ⇒ throw e
                case e: Throwable ⇒
                    throw new NCE(s"Invocation error [" +
                        s"mdlId=$mdlId, " +
                        s"callback=${method2Str(mtd)}" +
                    s"]", e)
            }

            case e: Throwable ⇒
                throw new NCE(s"Unexpected invocation error [" +
                    s"mdlId=$mdlId, " +
                    s"callback=${method2Str(mtd)}" +
                s"]", e)
        }
        finally
            if (flag)
                try
                    mtd.setAccessible(false)
                catch {
                    case e: SecurityException ⇒
                        throw new NCE(s"Access or security error [" +
                            s"mdlId=$mdlId, " +
                            s"callback=${method2Str(mtd)}" +
                        s"]", e)
                }
    }

    /**
      *
      * @param mdlId
      * @param mtd
      * @param paramClss
      * @param argsList
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def prepareParams(
        mdlId: String,
        mtd: Method,
        paramClss: Seq[Class[_]],
        argsList: Seq[util.List[NCToken]],
        ctxFirstParam: Boolean
    ): Seq[AnyRef] =
        paramClss.zip(argsList).zipWithIndex.map { case ((paramCls, argList), i) ⇒
            def mkArg(): String = arg2Str(mtd, i, ctxFirstParam)

            val toksCnt = argList.size()

            // Single token.
            if (paramCls == CLS_TOKEN) {
                if (toksCnt != 1)
                    throw new NCE(s"Expected single token (found $toksCnt) in @NCIntentTerm annotated argument [" +
                        s"mdlId=$mdlId, " +
                        s"arg=${mkArg()}" +
                    s"]")

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
                    case _ ⇒
                        throw new NCE(s"Too many tokens ($toksCnt) for scala.Option[_] @NCIntentTerm annotated argument [" +
                            s"mdlId$mdlId, " +
                            s"arg=${mkArg()}" +
                        s"]")
                }
            else if (paramCls == CLS_JAVA_OPT)
                toksCnt match {
                    case 0 ⇒ util.Optional.empty()
                    case 1 ⇒ util.Optional.of(argList.get(0))
                    case _ ⇒
                        throw new NCE(s"Too many tokens ($toksCnt) for java.util.Optional @NCIntentTerm annotated argument [" +
                            s"mdlId$mdlId, " +
                            s"arg=${mkArg()}" +
                            s"]")
                }
            else
                // All allowed arguments types already checked...
                throw new AssertionError(s"Unexpected callback @NCIntentTerm argument type [" +
                    s"mdlId=$mdlId, " +
                    s"type=$paramCls, " +
                    s"arg=${mkArg()}" +
                s"]")
        }

    /**
      *
      * @param mdlId
      * @param mtd
      * @param paramCls
      * @param paramGenTypes
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkTypes(mdlId: String, mtd: Method, paramCls: Seq[Class[_]], paramGenTypes: Seq[Type], ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == paramGenTypes.length)

        paramCls.zip(paramGenTypes).zipWithIndex.foreach { case ((pClass, pGenType), i) ⇒
            def mkArg(): String = arg2Str(mtd, i, ctxFirstParam)

            // Token.
            if (pClass == CLS_TOKEN) {
                // No-op.
            }
            else if (pClass.isArray) {
                val compType = pClass.getComponentType

                if (compType != CLS_TOKEN)
                    throw new NCE(s"Unexpected array element type for @NCIntentTerm annotated argument [" +
                        s"mdlId=$mdlId, " +
                        s"type=${class2Str(compType)}, " +
                        s"arg=${mkArg()}" +
                    s"]")
            }
            // Tokens collection and optionals.
            else if (COMP_CLS.contains(pClass))
                pGenType match {
                    case pt: ParameterizedType ⇒
                        val actTypes = pt.getActualTypeArguments
                        val compTypes = if (actTypes == null) Seq.empty else actTypes.toSeq

                        if (compTypes.length != 1)
                            throw new NCE(
                                s"Unexpected generic types count for @NCIntentTerm annotated argument [" +
                                    s"mdlId=$mdlId, " +
                                    s"count=${compTypes.length}, " +
                                    s"arg=${mkArg()}" +
                                s"]")

                        val compType = compTypes.head

                        compType match {
                            case _: Class[_] ⇒
                                val genClass = compTypes.head.asInstanceOf[Class[_]]

                                if (genClass != CLS_TOKEN)
                                    throw new NCE(s"Unexpected generic type for @NCIntentTerm annotated argument [" +
                                        s"mdlId=$mdlId, " +
                                        s"type=${class2Str(genClass)}, " +
                                        s"arg=${mkArg()}" +
                                    s"]")
                            case _ ⇒
                                throw new NCE(s"Unexpected generic type for @NCIntentTerm annotated argument [" +
                                    s"mdlId=$mdlId, " +
                                    s"type=${compType.getTypeName}, " +
                                    s"arg=${mkArg()}" +
                                    s"]")
                        }

                    case _ ⇒ throw new NCE(s"Unexpected parameter type for @NCIntentTerm annotated argument [" +
                        s"mdlId=$mdlId, " +
                        s"type=${pGenType.getTypeName}, " +
                        s"arg=${mkArg()}" +
                    s"]")
                }
            // Other types.
            else
                throw new NCE(s"Unexpected parameter type for @NCIntentTerm annotated argument [" +
                    s"mdlId=$mdlId, " +
                    s"type=${class2Str(pClass)}, " +
                    s"arg=${mkArg()}" +
                s"]")
        }
    }

    /**
      *
      * @param mdlId
      * @param mtd
      * @param paramCls
      * @param limits
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkMinMax(mdlId: String, mtd: Method, paramCls: Seq[Class[_]], limits: Seq[(Int, Int)], ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == limits.length)

        paramCls.zip(limits).zipWithIndex.foreach { case ((cls, (min, max)), i) ⇒
            def mkArg(): String = arg2Str(mtd, i, ctxFirstParam)

            val p1 = "its @NCIntentTerm annotated argument"
            val p2 = s"[mdlId=$mdlId, arg=${mkArg()}]"

            // Argument is single token but defined as not single token.
            if (cls == CLS_TOKEN && (min != 1 || max != 1))
                throw new NCE(s"@Intent term must have [1,1] quantifier because $p1 is a single value $p2")
            // Argument is not single token but defined as single token.
            else if (cls != CLS_TOKEN && (min == 1 && max == 1))
                throw new NCE(s"@Intent term has [1,1] quantifier but $p1 is not a single value $p2")
            // Argument is optional but defined as not optional.
            else if ((cls == CLS_SCALA_OPT || cls == CLS_JAVA_OPT) && (min != 0 || max != 1))
                throw new NCE(s"@Intent term must have [0,1] quantifier because $p1 is optional $p2")
            // Argument is not optional but defined as optional.
            else if ((cls != CLS_SCALA_OPT && cls != CLS_JAVA_OPT) && (min == 0 && max == 1))
                throw new NCE(s"@Intent term has [0,1] quantifier but $p1 is not optional $p2")
        }
    }

    /**
      *
      * @param mdl
      */
    @throws[NCE]
    private def scanIntents(mdl: NCModel): Map[NCDslIntent, Callback] = {
        val mdlId = mdl.getId
        
        mdl.getClass.getDeclaredMethods.flatMap(m ⇒ {
            // Direct in-the-class and referenced intents.
            val clsArr = m.getAnnotationsByType(CLS_INTENT)
            val refArr = m.getAnnotationsByType(CLS_INTENT_REF)

            if (clsArr.length > 1 || refArr.length > 1 || (clsArr.nonEmpty && refArr.nonEmpty))
                throw new NCE(s"Only one @NCIntent or @NCIntentRef annotation is allowed for callback [" +
                    s"mdlId=$mdlId, " +
                    s"callback=${method2Str(m)}" +
                s"]")

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
                                    throw new NCE(s"Duplicate intent IDs found [" +
                                        s"mdlId=$mdlId, " +
                                        s"origin=${adapter.getOrigin}, " +
                                        s"ids=${ids.mkString(",")}" +
                                    s"]")

                                case _ ⇒ ()
                            }

                            compiledIntents.find(_.id == refId) match {
                                case Some(intent) ⇒ Some(intent, m)
                                case None ⇒
                                    throw new NCE(
                                        s"@IntentRef($refId) references unknown intent ID [" +
                                            s"mdlId=$mdlId, " +
                                            s"refId=$refId, " +
                                            s"callback=${method2Str(m)}" +
                                        s"]")
                            }

                        case _ ⇒
                            throw new NCE(s"@IntentRef annotation can only be used for models extending 'NCModelFileAdapter' class [" +
                                s"mdlId=$mdlId, " +
                                s"callback=${method2Str(m)}" +
                            s"]")
                    }
                else
                    None
            }
        })
        .map {
            case (intent, m) ⇒ intent → prepareCallback(m, mdl, intent)
        }.toMap
    }

    /**
      * Scans given model for intent samples.
      *
      * @param mdl Model to scan.
      */
    @throws[NCE]
    private def scanSamples(mdl: NCModel): Map[String, Seq[String]] = {
        var annFound = false
        val mdlId = mdl.getId

        val samples =
            mdl.getClass.getDeclaredMethods.flatMap(mtd ⇒ {
                def mkMethodName: String = s"${mtd.getDeclaringClass.getName}#${mtd.getName}(...)"

                val smpAnn = mtd.getAnnotation(CLS_SAMPLE)
                val intAnn = mtd.getAnnotation(CLS_INTENT)
                val refAnn = mtd.getAnnotation(CLS_INTENT_REF)

                if (smpAnn != null || intAnn != null || refAnn != null) {
                    annFound = true

                    def mkIntentId(): String =
                        if (intAnn != null)
                            NCIntentDslCompiler.compile(intAnn.value(), mdlId).id
                        else if (refAnn != null)
                            refAnn.value().trim
                        else
                            throw new AssertionError()

                    if (smpAnn != null) {
                        if (intAnn == null && refAnn == null) {
                            logger.warn(s"`@NCTestSample annotation without corresponding @NCIntent or @NCIntentRef annotations [" +
                                s"mdlId=$mdlId, " +
                                s"callback=$mkMethodName" +
                            s"]")

                            None
                        }
                        else {
                            val samples = smpAnn.value().toList

                            if (samples.isEmpty) {
                                logger.warn(s"@NCTestSample annotation is empty [" +
                                    s"mdlId=$mdlId, " +
                                    s"callback=$mkMethodName" +
                                s"]")

                                None
                            }
                            else if (U.containsDups(samples)) {
                                logger.warn(s"@NCTestSample annotation has duplicates [" +
                                    s"mdlId=$mdlId, " +
                                    s"callback=$mkMethodName, " +
                                    s"dups=${U.getDups(samples).mkString("'", ", ", "'")}" +
                                s"]")

                                Some(mkIntentId() → samples.distinct)
                            }
                            else
                                Some(mkIntentId() → samples)
                        }
                    }
                    else {
                        logger.warn(s"@NCTestSample annotation is missing [" +
                            s"mdlId=$mdlId, " +
                            s"callback=$mkMethodName" +
                        s"]")

                        None
                    }
                }
                else
                    None
            }).toMap

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
                        logger.warn(s"@IntentSample sample doesn't contain any direct synonyms [" +
                            s"mdlId=$mdlId, " +
                            s"sample='$s'" +
                        s"]")
            }

        samples
    }
}
