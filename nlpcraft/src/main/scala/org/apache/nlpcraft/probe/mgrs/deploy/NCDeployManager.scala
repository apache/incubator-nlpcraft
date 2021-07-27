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

package org.apache.nlpcraft.probe.mgrs.deploy

import java.io._
import java.lang.reflect.{InvocationTargetException, Method, Modifier, ParameterizedType, Type, WildcardType}
import java.util
import java.util.function.Function
import java.util.jar.JarInputStream
import java.util.regex.{Pattern, PatternSyntaxException}
import io.opencensus.trace.Span
import org.apache.nlpcraft.model.NCModelView._
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.makro.NCMacroParser
import org.apache.nlpcraft.common.nlp.core.{NCNlpCoreManager, NCNlpPorterStemmer}
import org.apache.nlpcraft.common.util.NCUtils.{IDL_FIX, REGEX_FIX}
import org.apache.nlpcraft.model._
import org.apache.nlpcraft.model.factories.basic.NCBasicModelFactory
import org.apache.nlpcraft.model.intent.compiler.NCIdlCompiler
import org.apache.nlpcraft.model.intent.solver.NCIntentSolver
import org.apache.nlpcraft.model.intent._
import org.apache.nlpcraft.probe.mgrs.NCProbeSynonymChunkKind.{IDL, REGEX, TEXT}
import org.apache.nlpcraft.probe.mgrs.{NCProbeModel, NCProbeSynonym, NCProbeSynonymChunk, NCProbeSynonymsWrapper}

import scala.util.Using
import scala.compat.java8.OptionConverters._
import scala.collection.mutable
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, MapHasAsScala, SetHasAsScala}
import scala.util.control.Exception._

/**
  * Model deployment manager.
  */
object NCDeployManager extends NCService {
    private final val TOKENS_PROVIDERS_PREFIXES = Set("nlpcraft:", "google:", "stanford:", "opennlp:", "spacy:")
    private final val ID_REGEX = "^[_a-zA-Z]+[a-zA-Z0-9:\\-_]*$"

    private final val CLS_INTENT = classOf[NCIntent]
    private final val CLS_INTENT_REF = classOf[NCIntentRef]
    private final val CLS_QRY_RES = classOf[NCResult]
    private final val CLS_SLV_CTX = classOf[NCIntentMatch]
    private final val CLS_SAMPLE = classOf[NCIntentSample]
    private final val CLS_SAMPLE_REF = classOf[NCIntentSampleRef]

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
    
    type Callback = (String /* ID */, Function[NCIntentMatch, NCResult])
    type Intent = (NCIdlIntent, Callback)
    type Sample = (String/* Intent ID */, Seq[Seq[String]] /* List of list of input samples for that intent. */)
    
    private final val SEPARATORS = Seq('?', ',', '.', '-', '!')

    private final val SUSP_SYNS_CHARS = Seq("?", "*", "+")

    private final val MAX_CTXWORD_VALS_CNT = 10000
    private final val MAX_CTXWORD_SAMPLES_CNT = 1000

    @volatile private var data: mutable.ArrayBuffer[NCProbeModel] = _
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

        for (makro <- macros.keys if !set.exists(_.contains(makro)))
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

        for (elm <- mdl.getElements.asScala) {
            if (!elm.getId.matches(ID_REGEX))
                throw new NCE(
                s"Model element ID does not match regex [" +
                    s"mdlId=$mdlId, " +
                    s"elmId=${elm.getId}, " +
                    s"regex=$ID_REGEX" +
                s"]"
            )
        }

        checkMacros(mdl)

        val parser = new NCMacroParser

        // Initialize macro parser.
        mdl.getMacros.asScala.foreach(t => parser.addMacro(t._1, t._2))

        for (elm <- mdl.getElements.asScala)
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
            for (word: String <- jc.asScala.toSet) yield
                if (hasWhitespace(word))
                    throw new NCE(s"Model property cannot contain a string with whitespaces [" +
                        s"mdlId=$mdlId, " +
                        s"property=$name, " +
                        s"word='$word'" +
                    s"]")
                else
                    NCNlpCoreManager.stem(word)

        val addStopWords = checkAndStemmatize(mdl.getAdditionalStopWords, "additionalStopWords")
        val exclStopWords = checkAndStemmatize(mdl.getExcludedStopWords, "excludedStopWords")
        val suspWords = checkAndStemmatize(mdl.getSuspiciousWords, "suspiciousWord")

        checkStopwordsDups(mdlId, addStopWords, exclStopWords)

        val syns = mutable.HashSet.empty[SynonymHolder]

        def ok(b: Boolean, exp: Boolean): Boolean = if (exp) b else !b
        def idl(syns: Set[SynonymHolder], idl: Boolean): Set[SynonymHolder] = syns.filter(s => ok(s.syn.hasIdl, idl))
        def sparse(syns: Set[SynonymHolder], sp: Boolean): Set[SynonymHolder] = syns.filter(s => ok(s.syn.sparse, sp))

        var cnt = 0
        val maxCnt = mdl.getMaxTotalSynonyms

        // Process and check elements.
        for (elm <- mdl.getElements.asScala) {
            val elmId = elm.getId

            // Checks before macros processing.
            val susp = elm.getSynonyms.asScala.filter(syn => SUSP_SYNS_CHARS.exists(susp => syn.contains(susp)))

            if (susp.nonEmpty)
                logger.warn(
                    s"Suspicious synonyms detected [" +
                        s"mdlId=$mdlId, " +
                        s"elementId=$elmId, " +
                        s"synonyms=[${susp.mkString(", ")}]" +
                    s"]"
                )

            val sparseElem = elm.isSparse.orElse(mdl.isSparse)
            val permuteElem = elm.isPermutateSynonyms.orElse(mdl.isPermutateSynonyms)

            def addSynonym(
                isElementId: Boolean,
                isValueName: Boolean,
                value: String,
                chunks: Seq[NCProbeSynonymChunk]
            ): Unit = {
                def add(chunks: Seq[NCProbeSynonymChunk], perm: Boolean, sparse: Boolean, isDirect: Boolean): Unit = {
                    val holder = SynonymHolder(
                        elmId = elmId,
                        syn = NCProbeSynonym(isElementId, isValueName, isDirect, value, chunks, sparse, perm)
                    )

                    if (syns.add(holder)) {
                        cnt += 1

                        if (mdl.isMaxSynonymsThresholdError && cnt > maxCnt)
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

                val sp = sparseElem && chunks.size > 1

                if (
                    permuteElem &&
                    !sparseElem &&
                    !isElementId &&
                    chunks.forall(_.wordStem != null)
                )
                    simplePermute(chunks).map(p => p.map(_.wordStem) -> p).toMap.values.foreach(seq =>
                        add(seq, isDirect = seq == chunks, perm = true, sparse = sp)
                    )
                else
                    add(chunks, isDirect = true, perm = permuteElem, sparse = sp)
            }

            /**
              *
              * @param s
              * @return
              */
            @throws[NCE]
            def chunkSplit(s: String): Seq[NCProbeSynonymChunk] = {
                val x = s.trim()

                val chunks = mutable.ArrayBuffer.empty[String]

                var start = 0
                var curr = 0
                val len = x.length - (2 + 2) // 2 is a prefix/suffix length. Hack...

                def processChunk(fix: String): Unit = {
                    chunks ++= U.splitTrimFilter(x.substring(start, curr), " ")

                    x.indexOf(fix, curr + fix.length) match {
                        case -1 =>
                            throw new NCE(s"Invalid synonym definition [" +
                                s"mdlId=$mdlId, " +
                                s"chunks=$x" +
                            s"]")

                        case n =>
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
                    else if (isFix(IDL_FIX))
                        processChunk(IDL_FIX)
                    else
                        curr += 1
                }

                chunks ++= U.splitTrimFilter(x.substring(start), " ")

                chunks.map(mkChunk(mdl, _))
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
                .foreach(chunks => addSynonym(
                    isElementId = true,
                    isValueName = false,
                    null,
                    chunks
                ))

            // Add straight element synonyms.
            (for (syn <- elm.getSynonyms.asScala.flatMap(parser.expand)) yield chunkSplit(syn))
                .distinct
                .foreach(chunks => addSynonym(
                    isElementId = false,
                    isValueName = false,
                    null,
                    chunks
                ))

            val vals =
                (if (elm.getValues != null) elm.getValues.asScala else Seq.empty) ++
                (
                    elm.getValueLoader.asScala match {
                        case Some(ldr) => ldr.load(elm).asScala
                        case None => Seq.empty
                    }
                )

            // Add value synonyms.
            for (v <- vals.map(p => p.getName -> p).toMap.values) {
                val valName = v.getName
                val valSyns = v.getSynonyms.asScala

                val nameChunks = Seq(chunkIdSplit(valName))

                // Add value name as a synonyms.
                nameChunks.distinct.foreach(chunks => addSynonym(
                    isElementId = false,
                    isValueName = true,
                    valName,
                    chunks
                ))

                var skippedOneLikeName = false

                val chunks = valSyns.flatMap(parser.expand).flatMap(valSyn => {
                    val valSyns = chunkSplit(valSyn)

                    if (nameChunks.contains(valSyns) && !skippedOneLikeName) {
                        skippedOneLikeName = true

                        None
                    }
                    else
                        Some(valSyns)
                })

                chunks.distinct.foreach(chunks => addSynonym(
                    isElementId = false,
                    isValueName = false,
                    valName,
                    chunks
                ))
            }
        }

        if (cnt > maxCnt && !mdl.isMaxSynonymsThresholdError)
            logger.warn(
                s"Too many total synonyms detected [" +
                  s"mdlId=$mdlId, " +
                  s"cnt=$cnt, " +
                  s"max=$maxCnt" +
              s"]")

        // Validates context words parameters.
        val elems = mdl.getElements.asScala

        val ctxCatElems = elems.flatMap(e =>
            e.getCategoryConfidence.asScala match {
                case Some(v) => Some(e.getId -> v)
                case None => None
            }
        ).toMap

        if (ctxCatElems.nonEmpty) {
            val ids = ctxCatElems.filter { case (_, conf) => conf < 0 || conf > 1  }.keys

            if (ids.nonEmpty)
                // TODO:
                throw new NCE(s"Context word confidences are out of range (0..1) for elements : ${ids.mkString(", ")}")

            val cnt =
                elems.map(e =>
                    if (e.getValues != null)
                        e.getValues.asScala.map(
                            p => if (p.getSynonyms != null) p.getSynonyms.asScala.count(!_.contains(" ")) else 0
                        ).sum + 1 // 1 for value name.
                    else
                        0
                ).sum

            if (cnt > MAX_CTXWORD_VALS_CNT)
                // TODO: do we need print recommended value.?
                logger.warn(
                    s"Too many values synonyms detected for context words elements [" +
                        s"mdlId=$mdlId, " +
                        s"cnt=$cnt," +
                        s"recommendedMax=$MAX_CTXWORD_VALS_CNT" +
                        s"]"
                )
        }

        // Discard value loaders.
        for (elm <- mdl.getElements.asScala)
            elm.getValueLoader.ifPresent(_.onDiscard())

        val allAliases = syns
            .flatMap(_.syn)
            .groupBy(_.origText)
            .map(x => (x._1, x._2.map(_.alias).filter(_ != null)))
            .values
            .toSeq
            .flatten
            .toList

        // Check for IDL alias uniqueness.
        if (U.containsDups(allAliases))
            throw new NCE(s"Duplicate IDL synonym alias found [" +
                s"mdlId=$mdlId, " +
                s"dups=${allAliases.diff(allAliases.distinct).mkString(", ")}" +
            s"]")

        val idAliasDups = mdl.getElements.asScala.map(_.getId).intersect(allAliases.toSet)

        // Check that IDL aliases don't intersect with element IDs.
        if (idAliasDups.nonEmpty)
            throw new NCE(s"Model element IDs and IDL synonym aliases intersect [" +
                s"mdlId=$mdlId, " +
                s"dups=${idAliasDups.mkString(", ")}" +
            "]")

        val dupSyns = mutable.Buffer.empty[(Seq[String], String)]

        // Check for synonym dups across all elements.
        for (
            ((syn, isDirect), holders) <-
                syns.groupBy(p => (p.syn.mkString(" "), p.syn.isDirect)) if holders.size > 1 && isDirect
        ) {
            dupSyns.append((
                holders.map(p => s"id=${p.elmId}${if (p.syn.value == null) "" else s", value=${p.syn.value}"}").toSeq,
                syn
            ))
        }

        if (dupSyns.nonEmpty) {
            if (mdl.isDupSynonymsAllowed) {
                val tbl = NCAsciiTable("Elements", "Dup Synonym")

                dupSyns.foreach(row => tbl += (
                    row._1,
                    row._2
                ))

                logger.trace(s"Duplicate synonyms (${dupSyns.size}) found in '$mdlId' model.")
                logger.trace(s"  ${b("|--")} NOTE: ID of the model element is its default built-in synonym - you don't need to add it explicitly to the list of synonyms.")
                logger.trace(s"  ${b("+--")} Model '$mdlId' allows duplicate synonyms but the large number may degrade the performance.")
                logger.trace(tbl.toString)

                logger.warn(s"Duplicate synonyms (${dupSyns.size}) found in '$mdlId' model - turn on TRACE logging to see them.")
            }
            else
                throw new NCE(s"Duplicated synonyms found and not allowed [mdlId=$mdlId]")
        }

        // Scan for intent annotations in the model class.
        val intents = scanIntents(mdl)

        var solver: NCIntentSolver = null

        if (intents.nonEmpty) {
            // Check the uniqueness of intent IDs.
            U.getDups(intents.map(_._1).toSeq.map(_.id)) match {
                case ids if ids.nonEmpty =>
                    throw new NCE(s"Duplicate intent IDs [" +
                        s"mdlId=$mdlId, " +
                        s"mdlOrigin=${mdl.getOrigin}, " +
                        s"ids=${ids.mkString(",")}" +
                    s"]")
                case _ => ()
            }

            solver = new NCIntentSolver(
                intents.toList.map(x => (x._1, (z: NCIntentMatch) => x._2._2.apply(z)))
            )
        }
        else
            logger.warn(s"Model has no intent: $mdlId")

        val samples = scanSamples(mdl)

        if (ctxCatElems.nonEmpty && samples.size > MAX_CTXWORD_SAMPLES_CNT)
            // TODO: do we need print recommended value.?
            logger.warn(
                s"Too many samples detected for context words elements [" +
                    s"mdlId=$mdlId, " +
                    s"cnt=${samples.size}," +
                    s"recommended=$MAX_CTXWORD_SAMPLES_CNT" +
                    s"]"
            )

        val simple = idl(syns.toSet, idl = false)

        def toMap(set: Set[SynonymHolder]): Map[String, Seq[NCProbeSynonym]] =
            set.groupBy(_.elmId).map(p => p._1 -> p._2.map(_.syn).toSeq.sorted.reverse)

        NCProbeModel(
            model = mdl,
            solver = solver,
            intents = intents.map(_._1).toSeq,
            continuousSynonyms = mkFastAccessMap(sparse(simple, sp = false), NCProbeSynonymsWrapper(_)),
            sparseSynonyms = toMap(sparse(simple, sp = true)),
            idlSynonyms = toMap(idl(syns.toSet, idl = true)),
            addStopWordsStems = addStopWords,
            exclStopWordsStems = exclStopWords,
            suspWordsStems = suspWords,
            elements = mdl.getElements.asScala.map(elm => (elm.getId, elm)).toMap,
            samples = samples
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
            case Left(e) => throw new NCE(s"Failed to instantiate model factory for: $clsName", e)
            case Right(factory) => factory
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
            case e: Throwable => throw new NCE(s"Failed to instantiate model: $clsName", e)
        }

    /**
      *
      * @param set
      * @return
      */
    private def mkFastAccessMap[T](set: Set[SynonymHolder], f: Seq[NCProbeSynonym] => T):
        Map[String /*Element ID*/ , Map[Int /*Synonym length*/ , T]] =
        set
            .groupBy(_.elmId)
            .map {
                case (elmId, holders) => (
                    elmId,
                    holders
                        .map(_.syn)
                        .groupBy(_.size)
                        .map {
                            // Sort synonyms from most important to least important.
                            case (k, v) => (k, f(v.toSeq))
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
            case Left(e) => e match {
                case _: NCE => throw e
                case _ =>
                    throw new NCE(s"Failed to instantiate model [" +
                        s"cls=${cls.getName}, " +
                        s"factory=${mdlFactory.getClass.getName}, " +
                        s"src=$src" +
                        "]",
                        e
                    )
            }

            case Right(model) => model
        }

    /**
      *
      * @param jarFile JAR file to extract from.
      */
    @throws[NCE]
    private def extractModels(jarFile: File): Seq[NCProbeModel] = {
        val clsLdr = Thread.currentThread().getContextClassLoader

        val classes = mutable.ArrayBuffer.empty[Class[_ <: NCModel]]

        Using.resource(new JarInputStream(new BufferedInputStream(new FileInputStream(jarFile)))) { in =>
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
                        case _: ClassNotFoundException => ()
                        case _: NoClassDefFoundError => ()
                    }
                }

                entry = in.getNextJarEntry
            }
        }

        classes.map(cls =>
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
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        data = mutable.ArrayBuffer.empty[NCProbeModel]

        mdlFactory = Config.modelFactoryType match {
            case Some(mft) =>
                val mf = makeModelFactory(mft)

                mf.initialize(Config.modelFactoryProps.getOrElse(Map.empty[String, String]).asJava)

                mf

            case None => new NCBasicModelFactory
        }

        data ++= U.splitTrimFilter(Config.models, ",").map(makeModelWrapper)

        Config.jarsFolder match {
            case Some(jarsFolder) =>
                val jarsFile = new File(jarsFolder)

                if (!jarsFile.exists())
                    throw new NCE(s"Probe configuration JAR folder path does not exist: $jarsFolder")
                if (!jarsFile.isDirectory)
                    throw new NCE(s"Probe configuration JAR folder path is not a directory: $jarsFolder")

                val src = this.getClass.getProtectionDomain.getCodeSource
                val locJar = if (src == null) null else new File(src.getLocation.getPath)

                for (jar <- scanJars(jarsFile) if jar != locJar)
                    data ++= extractModels(jar)

            case None => // No-op.
        }

        val ids = data.map(_.model.getId).toList

        if (U.containsDups(ids))
            throw new NCE(s"Duplicate model IDs detected: ${ids.mkString(", ")}")

        ackStarted()
    }

    /**
     *
     * @param parent Optional parent span.
     * @throws NCE
     */
    @throws[NCE]
    override def stop(parent: Span = null): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()

        if (mdlFactory != null)
            mdlFactory.terminate()

        if (data != null)
            data.clear()

        ackStopped()
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
            case 0 => Seq.empty
            case 1 => Seq(seq)
            case n =>
                def permute(idx1: Int, idx2: Int): Seq[T] =
                    seq.zipWithIndex.map { case (t, idx) =>
                        if (idx == idx1)
                            seq(idx2)
                        else if (idx == idx2)
                            seq(idx1)
                        else
                            t
                    }

                Seq(seq) ++
                    seq.zipWithIndex.flatMap { case (_, idx) =>
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
        for (elm <- mdl.getElements.asScala) {
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

        for (id <- mdl.getElements.asScala.map(_.getId))
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
        else if (elm.getId.isEmpty)
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
        def checkNum(v: Long, name: String, min: Long, max: Long): Unit =
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

        checkMandatoryString(mdl.getId, "id", MODEL_ID_MAXLEN)
        checkMandatoryString(mdl.getName, "name", MODEL_NAME_MAXLEN)
        checkMandatoryString(mdl.getVersion, "version", MODEL_VERSION_MAXLEN)

        checkNum(mdl.getConversationTimeout, "conversationTimeout", CONV_TIMEOUT_MIN, CONV_TIMEOUT_MAX)
        checkNum(mdl.getMaxUnknownWords, "maxUnknownWords", MAX_UNKNOWN_WORDS_MIN, MAX_UNKNOWN_WORDS_MAX)
        checkNum(mdl.getMaxFreeWords, "maxFreeWords", MAX_FREE_WORDS_MIN, MAX_FREE_WORDS_MAX)
        checkNum(mdl.getMaxSuspiciousWords, "maxSuspiciousWords", MAX_SUSPICIOUS_WORDS_MIN, MAX_SUSPICIOUS_WORDS_MAX)
        checkNum(mdl.getMinWords, "minWords", MIN_WORDS_MIN, MIN_WORDS_MAX)
        checkNum(mdl.getMinNonStopwords, "minNonStopwords", MIN_NON_STOPWORDS_MIN, MIN_NON_STOPWORDS_MAX)
        checkNum(mdl.getMinTokens, "minTokens", MIN_TOKENS_MIN, MIN_TOKENS_MAX)
        checkNum(mdl.getMaxTokens, "maxTokens", MAX_TOKENS_MIN, MAX_TOKENS_MAX)
        checkNum(mdl.getMaxWords, "maxWords", MAX_WORDS_MIN, MAX_WORDS_MAX)
        checkNum(mdl.getMaxElementSynonyms, "maxSynonymsThreshold", MAX_SYN_MIN, MAX_SYN_MAX)
        checkNum(mdl.getConversationDepth, "conversationDepth", CONV_DEPTH_MIN, CONV_DEPTH_MAX)

        checkCollection("additionalStopWords", mdl.getAdditionalStopWords)
        checkCollection("elements", mdl.getElements)
        checkCollection("enabledBuiltInTokens", mdl.getEnabledBuiltInTokens)
        checkCollection("abstractTokens", mdl.getAbstractTokens)
        checkCollection("excludedStopWords", mdl.getExcludedStopWords)
        checkCollection("parsers", mdl.getParsers)
        checkCollection("suspiciousWords", mdl.getSuspiciousWords)
        checkCollection("macros", mdl.getMacros)
        checkCollection("metadata", mdl.getMetadata)
        checkCollection("restrictedCombinations", mdl.getRestrictedCombinations)

        mdl.getElements.asScala.foreach(e => checkMandatoryString(e.getId,"element.id", MODEL_ELEMENT_ID_MAXLEN))

        for ((elm, restrs: util.Set[String]) <- mdl.getRestrictedCombinations.asScala) {
            if (elm != "nlpcraft:limit" && elm != "nlpcraft:sort" && elm != "nlpcraft:relation")
                throw new NCE(s"Unsupported restricting element [" +
                    s"mdlId=$mdlId, " +
                    s"elmId=$elm" +
                s"]. Only 'nlpcraft:limit', 'nlpcraft:sort', and 'nlpcraft:relation' are allowed.")
            if (restrs.contains(elm))
                throw new NCE(s"Element cannot be restricted to itself [" +
                    s"mdlId=$mdlId, " +
                    s"elmId=$elm" +
                s"]")
        }

        val unsToksBlt =
            mdl.getEnabledBuiltInTokens.asScala.filter(t =>
                // 'stanford', 'google', 'opennlp', 'spacy' - any names, not validated.
                t == null ||
                !TOKENS_PROVIDERS_PREFIXES.exists(typ => t.startsWith(typ)) ||
                // 'nlpcraft' names validated.
                (t.startsWith("nlpcraft:") && !NCModelView.DFLT_ENABLED_BUILTIN_TOKENS.contains(t))
            )

        if (unsToksBlt.nonEmpty)
            throw new NCE(s"Invalid token IDs for 'enabledBuiltInTokens' model property [" +
                s"mdlId=${mdl.getId}, " +
                s"ids=${unsToksBlt.mkString(", ")}" +
            s"]")

        // We can't check other names because they can be created by custom parsers.
        val unsToksAbstract = mdl.getAbstractTokens.asScala.filter(t => t == null || t == "nlpcraft:nlp")

        if (unsToksAbstract.nonEmpty)
            throw new NCE(s"Invalid token IDs for 'abstractToken' model property [" +
                s"mdlId=${mdl.getId}, " +
                s"ids=${unsToksAbstract.mkString(", ")}" +
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
     * @param mdl Model.
     * @param chunk Synonym chunk.
     * @return
     */
    @throws[NCE]
    private def mkChunk(mdl: NCModel, chunk: String): NCProbeSynonymChunk = {
        def stripSuffix(fix: String, s: String): String = s.slice(fix.length, s.length - fix.length)

        val mdlId = mdl.getId

        // Regex synonym.
        if (startsAndEnds(REGEX_FIX, chunk)) {
            val ptrn = stripSuffix(REGEX_FIX, chunk)
            if (ptrn.nonEmpty) {
                try
                    NCProbeSynonymChunk(kind = REGEX, origText = chunk, regex = Pattern.compile(ptrn))
                catch {
                    case e: PatternSyntaxException =>
                        throw new NCE(s"Invalid regex synonym syntax detected [" +
                            s"mdlId=$mdlId, " +
                            s"chunk=$chunk" +
                            s"]", e)
                }
            }
            else
                throw new NCE(s"Empty regex synonym detected [" +
                    s"mdlId=$mdlId, " +
                    s"chunk=$chunk" +
                    s"]")
        }
        // IDL-based synonym.
        else if (startsAndEnds(IDL_FIX, chunk)) {
            val idl = stripSuffix(IDL_FIX, chunk)
            val compUnit = NCIdlCompiler.compileSynonym(idl, mdl, mdl.getOrigin)

            val x = NCProbeSynonymChunk(alias = compUnit.alias.orNull, kind = IDL, origText = chunk, idlPred = compUnit.pred)

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
      * @param wct
      * @return
      */
    private def wc2Str(wct: WildcardType): String = if (wct == null) "null" else s"'${wct.getTypeName}'"

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
    private def prepareCallback(mtd: Method, mdl: NCModel, intent: NCIdlIntent): Callback = {
        val mdlId = mdl.getId

        // Checks method result type.
        if (mtd.getReturnType != CLS_QRY_RES)
            throw new NCE(s"Unexpected result type for @NCIntent annotated method [" +
                s"mdlId=$mdlId, " +
                s"intentId=${intent.id}, " +
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
        val tokParamAnns = getTokensSeq(allAnns.toIndexedSeq).filter(_ != null)
        val tokParamTypes = getTokensSeq(allParamTypes)

        // Checks tokens parameters annotations count.
        if (tokParamAnns.length != tokParamTypes.length)
            throw new NCE(s"Unexpected annotations count for @NCIntent annotated method [" +
                s"mdlId=$mdlId, " +
                s"intentId=${intent.id}, " +
                s"count=${tokParamAnns.size}, " +
                s"callback=${method2Str(mtd)}" +
            s"]")

        // Gets terms IDs.
        val termIds = tokParamAnns.toList.zipWithIndex.map {
            case (anns, idx) =>
                def mkArg(): String = arg2Str(mtd, idx, ctxFirstParam)

                val annsTerms = anns.filter(_.isInstanceOf[NCIntentTerm])

                // Each method arguments (second and later) must have one NCIntentTerm annotation.
                annsTerms.length match {
                    case 1 => annsTerms.head.asInstanceOf[NCIntentTerm].value()

                    case 0 =>
                        throw new NCE(s"Missing @NCIntentTerm annotation for [" +
                            s"mdlId=$mdlId, " +
                            s"intentId=${intent.id}, " +
                            s"arg=${mkArg()}" +
                        s"]")

                    case _ =>
                        throw new NCE(s"Too many @NCIntentTerm annotations for [" +
                            s"mdlId=$mdlId, " +
                            s"intentId=${intent.id}, " +
                            s"arg=${mkArg()}" +
                        s"]")
                }
            }

        if (U.containsDups(termIds))
            throw new NCE(s"Duplicate term IDs in @NCIntentTerm annotations [" +
                s"mdlId=$mdlId, " +
                s"intentId=${intent.id}, " +
                s"dups=${U.getDups(termIds).mkString(", ")}, " +
                s"callback=${method2Str(mtd)}" +
            s"]")

        val terms = intent.terms

        // Checks correctness of term IDs.
        // Note we don't restrict them to be duplicated.
        val intentTermIds = terms.flatMap(_.id)
        val invalidIds = termIds.filter(id => !intentTermIds.contains(id))

        if (invalidIds.nonEmpty) {
            // Report only the first one for simplicity & clarity.
            throw new NCE(s"Unknown term ID in @NCIntentTerm annotation [" +
                s"mdlId=$mdlId, " +
                s"intentId=${intent.id}, " +
                s"termId=${invalidIds.head}, " +
                s"callback=${method2Str(mtd)}" +
            s"]")
        }

        val paramGenTypes = getTokensSeq(mtd.getGenericParameterTypes.toIndexedSeq)

        require(tokParamTypes.length == paramGenTypes.length)

        // Checks parameters.
        checkTypes(mdl, mtd, tokParamTypes, paramGenTypes, ctxFirstParam)

        // Checks limits.
        val allLimits = terms.map(t => t.id.orNull -> (t.min, t.max)).toMap

        checkMinMax(mdl, mtd, tokParamTypes, termIds.map(allLimits), ctxFirstParam)

        // Prepares invocation method.
        (
            mtd.toString,
            (ctx: NCIntentMatch) => {
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
        )
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

        val obj = if (Modifier.isStatic(mtd.getModifiers)) null else mdl

        var flag = mtd.canAccess(obj)

        try {
            if (!flag) {
                mtd.setAccessible(true)

                flag = true
            }
            else
                flag = false

            mtd.invoke(obj, args: _*).asInstanceOf[NCResult]
        }
        catch {
            case e: InvocationTargetException => e.getTargetException match {
                case e: NCIntentSkip => throw e
                case e: NCRejection => throw e
                case e: NCE => throw e
                case e: Throwable =>
                    throw new NCE(s"Intent callback invocation error [" +
                        s"mdlId=$mdlId, " +
                        s"callback=${method2Str(mtd)}" +
                    s"]", e)
            }

            case e: Throwable =>
                throw new NCE(s"Unexpected intent callback invocation error [" +
                    s"mdlId=$mdlId, " +
                    s"callback=${method2Str(mtd)}" +
                s"]", e)
        }
        finally
            if (flag)
                try
                    mtd.setAccessible(false)
                catch {
                    case e: SecurityException =>
                        throw new NCE(s"Access or security error in intent callback [" +
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
        paramClss.zip(argsList).zipWithIndex.map { case ((paramCls, argList), i) =>
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
            // Scala and Java list of tokens.
            else if (paramCls == CLS_SCALA_SEQ)
                argList.asScala.toSeq
            else if (paramCls == CLS_SCALA_LST)
                argList.asScala.toList
            else if (paramCls == CLS_JAVA_LST)
                argList
            // Scala and java optional token.
            else if (paramCls == CLS_SCALA_OPT)
                toksCnt match {
                    case 0 => None
                    case 1 => Some(argList.get(0))
                    case _ =>
                        throw new NCE(s"Too many tokens ($toksCnt) for scala.Option[_] @NCIntentTerm annotated argument [" +
                            s"mdlId$mdlId, " +
                            s"arg=${mkArg()}" +
                        s"]")
                }
            else if (paramCls == CLS_JAVA_OPT)
                toksCnt match {
                    case 0 => util.Optional.empty()
                    case 1 => util.Optional.of(argList.get(0))
                    case _ =>
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
      * @param mdl
      * @param mtd
      * @param paramCls
      * @param paramGenTypes
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkTypes(
        mdl: NCModel,
        mtd: Method,
        paramCls: Seq[Class[_]],
        paramGenTypes: Seq[Type],
        ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == paramGenTypes.length)

        val mdlId = mdl.getId

        paramCls.zip(paramGenTypes).zipWithIndex.foreach { case ((pClass, pGenType), i) =>
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
                        s"mdlOrigin=${mdl.getOrigin}, " +
                        s"type=${class2Str(compType)}, " +
                        s"arg=${mkArg()}" +
                    s"]")
            }
            // Tokens collection and optionals.
            else if (COMP_CLS.contains(pClass))
                pGenType match {
                    case pt: ParameterizedType =>
                        val actTypes = pt.getActualTypeArguments
                        val compTypes = if (actTypes == null) Seq.empty else actTypes.toSeq

                        if (compTypes.length != 1)
                            throw new NCE(
                                s"Unexpected generic types count for @NCIntentTerm annotated argument [" +
                                    s"mdlId=$mdlId, " +
                                    s"mdlOrigin=${mdl.getOrigin}, " +
                                    s"count=${compTypes.length}, " +
                                    s"arg=${mkArg()}" +
                                s"]")

                        val compType = compTypes.head

                        compType match {
                            // Java, Scala, Groovy.
                            case _: Class[_] =>
                                val genClass = compTypes.head.asInstanceOf[Class[_]]

                                if (genClass != CLS_TOKEN)
                                    throw new NCE(s"Unexpected generic type for @NCIntentTerm annotated argument [" +
                                        s"mdlId=$mdlId, " +
                                        s"mdlOrigin=${mdl.getOrigin}, " +
                                        s"type=${class2Str(genClass)}, " +
                                        s"arg=${mkArg()}" +
                                    s"]")
                            // Kotlin.
                            case _: WildcardType =>
                                val wildcardType = compTypes.head.asInstanceOf[WildcardType]

                                val lowBounds = wildcardType.getLowerBounds
                                val upBounds = wildcardType.getUpperBounds

                                if (lowBounds.nonEmpty || upBounds.size != 1 || upBounds(0) != CLS_TOKEN)
                                    throw new NCE(
                                        s"Unexpected Kotlin generic type for @NCIntentTerm annotated argument [" +
                                            s"mdlId=$mdlId, " +
                                            s"mdlOrigin=${mdl.getOrigin}, " +
                                            s"type=${wc2Str(wildcardType)}, " +
                                            s"arg=${mkArg()}" +
                                        s"]")
                            case _ =>
                                throw new NCE(s"Unexpected generic type for @NCIntentTerm annotated argument [" +
                                    s"mdlId=$mdlId, " +
                                    s"mdlOrigin=${mdl.getOrigin}, " +
                                    s"type=${compType.getTypeName}, " +
                                    s"arg=${mkArg()}" +
                                s"]")
                        }

                    case _ => throw new NCE(s"Unexpected parameter type for @NCIntentTerm annotated argument [" +
                        s"mdlId=$mdlId, " +
                        s"mdlOrigin=${mdl.getOrigin}, " +
                        s"type=${pGenType.getTypeName}, " +
                        s"arg=${mkArg()}" +
                    s"]")
                }
            // Other types.
            else
                throw new NCE(s"Unexpected parameter type for @NCIntentTerm annotated argument [" +
                    s"mdlId=$mdlId, " +
                    s"mdlOrigin=${mdl.getOrigin}, " +
                    s"type=${class2Str(pClass)}, " +
                    s"arg=${mkArg()}" +
                s"]")
        }
    }

    /**
      *
      * @param mdl
      * @param mtd
      * @param paramCls
      * @param limits
      * @param ctxFirstParam
      */
    @throws[NCE]
    private def checkMinMax(
        mdl: NCModel,
        mtd: Method,
        paramCls: Seq[Class[_]],
        limits: Seq[(Int, Int)],
        ctxFirstParam: Boolean): Unit = {
        require(paramCls.length == limits.length)

        val mdlId = mdl.getId

        paramCls.zip(limits).zipWithIndex.foreach { case ((cls, (min, max)), i) =>
            def mkArg(): String = arg2Str(mtd, i, ctxFirstParam)

            val p1 = "its @NCIntentTerm annotated argument"
            val p2 = s"[" +
                s"mdlId=$mdlId, " +
                s"mdlOrigin=${mdl.getOrigin}, " +
                s"arg=${mkArg()}" +
            s"]"

            // Argument is single token but defined as not single token.
            if (cls == CLS_TOKEN && (min != 1 || max != 1))
                throw new NCE(s"Intent term must have [1,1] quantifier because $p1 is a single value $p2")
            // Argument is not single token but defined as single token.
            else if (cls != CLS_TOKEN && (min == 1 && max == 1))
                throw new NCE(s"Intent term has [1,1] quantifier but $p1 is not a single value $p2")
            // Argument is optional but defined as not optional.
            else if ((cls == CLS_SCALA_OPT || cls == CLS_JAVA_OPT) && (min != 0 || max != 1))
                throw new NCE(s"Intent term must have [0,1] quantifier because $p1 is optional $p2")
            // Argument is not optional but defined as optional.
            else if ((cls != CLS_SCALA_OPT && cls != CLS_JAVA_OPT) && (min == 0 && max == 1))
                throw new NCE(s"Intent term has [0,1] quantifier but $p1 is not optional $p2")
        }
    }

    /**
      * Gets its own methods including private and accessible from parents.
      *
      * @param o Object.
      * @return Methods.
      */
    private def getAllMethods(o: AnyRef): Set[Method] = {
        val claxx = o.getClass

        (claxx.getDeclaredMethods ++ claxx.getMethods).toSet
    }

    /**
      *
      * @param mdl
      */
    @throws[NCE]
    private def scanIntents(mdl: NCModel): Set[Intent] = {
        val mdlId = mdl.getId
        val intentDecls = mutable.Buffer.empty[NCIdlIntent]
        val intents = mutable.Buffer.empty[Intent]

        // First, get intent declarations from the JSON/YAML file, if any.
        mdl match {
            case adapter: NCModelFileAdapter =>
                intentDecls ++= adapter
                    .getIntents
                    .asScala
                    .flatMap(NCIdlCompiler.compileIntents(_, mdl, mdl.getOrigin))

            case _ => ()
        }

        // Second, scan class for class-level @NCIntent annotations (intent declarations).
        val mdlCls = mdl.meta[String](MDL_META_MODEL_CLASS_KEY)

        if (mdlCls != null) {
            try {
                val cls = Class.forName(mdlCls)

                for (ann <- cls.getAnnotationsByType(CLS_INTENT); intent <- NCIdlCompiler.compileIntents(ann.value(), mdl, mdlCls))
                    if (intentDecls.exists(_.id == intent.id))
                        throw new NCE(s"Duplicate intent ID [" +
                            s"mdlId=$mdlId, " +
                            s"mdlOrigin=${mdl.getOrigin}, " +
                            s"class=$mdlCls, " +
                            s"id=${intent.id}" +
                        s"]")
                    else
                        intentDecls += intent
            }
            catch {
                case _: ClassNotFoundException => throw new NCE(s"Failed to scan class for @NCIntent annotation: $mdlCls")
            }
        }

        // Third, scan all methods for intent-callback bindings.
        for (m <- getAllMethods(mdl)) {
            val mtdStr = method2Str(m)

            def bindIntent(intent: NCIdlIntent, cb: Callback): Unit = {
                if (intents.exists(i => i._1.id == intent.id && i._2._1 != cb._1))
                    throw new NCE(s"The intent cannot be bound to more than one callback [" +
                        s"mdlId=$mdlId, " +
                        s"mdlOrigin=${mdl.getOrigin}, " +
                        s"class=$mdlCls, " +
                        s"intentId=${intent.id}" +
                    s"]")
                else {
                    intentDecls += intent
                    intents += (intent -> prepareCallback(m, mdl, intent))
                }
            }

            // Process inline intent declarations by @NCIntent annotation.
            for (ann <- m.getAnnotationsByType(CLS_INTENT); intent <- NCIdlCompiler.compileIntents(ann.value(), mdl, mtdStr))
                if (intentDecls.exists(_.id == intent.id) || intents.exists(_._1.id == intent.id))
                    throw new NCE(s"Duplicate intent ID [" +
                        s"mdlId=$mdlId, " +
                        s"mdlOrigin=${mdl.getOrigin}, " +
                        s"callback=$mtdStr, " +
                        s"id=${intent.id}" +
                    s"]")
                else
                    bindIntent(intent, prepareCallback(m, mdl, intent))

            // Process intent references from @NCIntentRef annotation.
            for (ann <- m.getAnnotationsByType(CLS_INTENT_REF)) {
                val refId = ann.value().trim

                intentDecls.find(_.id == refId) match {
                    case Some(intent) => bindIntent(intent, prepareCallback(m, mdl, intent))
                    case None => throw new NCE(
                        s"""@NCIntentRef("$refId") references unknown intent ID [""" +
                            s"mdlId=$mdlId, " +
                            s"mdlOrigin=${mdl.getOrigin}, " +
                            s"refId=$refId, " +
                            s"callback=$mtdStr" +
                        s"]")
                }
            }
        }

        val unusedIntents = intentDecls.filter(i => !intents.exists(_._1.id == i.id))

        if (unusedIntents.nonEmpty)
            logger.warn(s"Declared but unused intents: [" +
                s"mdlId=$mdlId, " +
                s"mdlOrigin=${mdl.getOrigin}, " +
                s"intentIds=${unusedIntents.map(_.id).mkString("(", ", ", ")")}]"
            )

        intents.toSet
    }

    /**
      * Scans given model for intent samples.
      *
      * @param mdl Model to scan.
      */
    @throws[NCE]
    private def scanSamples(mdl: NCModel): Set[Sample] = {
        val mdlId = mdl.getId

        val samples = mutable.Buffer.empty[Sample]

        for (m <- getAllMethods(mdl)) {
            val mtdStr = method2Str(m)

            val smpAnns = m.getAnnotationsByType(CLS_SAMPLE)
            val smpAnnsRef = m.getAnnotationsByType(CLS_SAMPLE_REF)
            val intAnns = m.getAnnotationsByType(CLS_INTENT)
            val refAnns = m.getAnnotationsByType(CLS_INTENT_REF)

            if (smpAnns.nonEmpty || smpAnnsRef.nonEmpty) {
                if (intAnns.isEmpty && refAnns.isEmpty)
                    throw new NCE(s"@NCIntentSample or @NCIntentSampleRef annotations without corresponding @NCIntent or @NCIntentRef annotations: $mtdStr")
                else {
                    def read[T](arr: Array[T], annName: String, getValue: T => Seq[String]): Seq[Seq[String]] = {
                        val seq = arr.toSeq.map(getValue).map(_.map(_.strip).filter(s => s.nonEmpty && s.head != '#'))

                        if (seq.exists(_.isEmpty))
                            logger.warn(s"$annName annotation has no samples: $mtdStr")

                        seq
                    }

                    val seqSeq =
                        read[NCIntentSample](
                            smpAnns, "@NCIntentSample", _.value().toSeq
                        ) ++
                        read[NCIntentSampleRef](
                            smpAnnsRef, "@NCIntentSampleRef", a => U.readAnySource(a.value())
                        )

                    if (U.containsDups(seqSeq.flatten.toList))
                        logger.warn(s"@NCIntentSample and @NCIntentSampleRef annotations have duplicates (safely ignoring): $mtdStr")

                    val distinct = seqSeq.map(_.distinct).distinct

                    for (ann <- intAnns; intent <- NCIdlCompiler.compileIntents(ann.value(), mdl, mtdStr))
                        samples += (intent.id -> distinct)

                    for (ann <- refAnns)
                        samples += (ann.value() -> distinct)
                }
            }
            else if (intAnns.nonEmpty || refAnns.nonEmpty)
                logger.warn(s"@NCIntentSample or @NCIntentSampleRef annotations are missing for: $mtdStr")
        }

        if (samples.nonEmpty) {
            val parser = new NCMacroParser

            mdl.getMacros.asScala.foreach { case (name, str) => parser.addMacro(name, str) }

            val allSyns: Set[Seq[String]] =
                mdl.getElements.
                    asScala.
                    flatMap(_.getSynonyms.asScala.flatMap(parser.expand)).
                    map(NCNlpPorterStemmer.stem).map(_.split(" ").toSeq).
                    toSet

            case class Case(modelId: String, sample: String)

            val processed = mutable.HashSet.empty[Case]

            samples.
                flatMap { case (_, smp) => smp.flatten.map(_.toLowerCase) }.
                map(s => s -> SEPARATORS.foldLeft(s)((s, ch) => s.replaceAll(s"\\$ch", s" $ch "))).
                foreach {
                    case (s, sNorm) =>
                        if (processed.add(Case(mdlId, s))) {
                            val seq: Seq[String] = sNorm.split(" ").toIndexedSeq.map(NCNlpPorterStemmer.stem)

                            if (!allSyns.exists(_.intersect(seq).nonEmpty)) {
                                // Not a warning since the parent class can contain direct synonyms (NLPCRAFT-348).
                                // See NLPCRAFT-349 for the additional issue.
                                logger.debug(s"@NCIntentSample or @NCIntentSampleRef sample doesn't contain any direct synonyms (check if its parent class contains any) [" +
                                    s"mdlId=$mdlId, " +
                                    s"origin=${mdl.getOrigin}, " +
                                    s"""sample="$s"""" +
                                s"]")
                            }
                        }

                }
        }

        samples.toSet
    }
}
