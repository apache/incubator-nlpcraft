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

package org.apache.nlpcraft.common.extcfg

import io.opencensus.trace.Span
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.extcfg.NCExternalConfigType._
import org.apache.nlpcraft.common.module.NCModule
import org.apache.nlpcraft.common.module.NCModule.{NCModule, PROBE, SERVER}
import org.apache.nlpcraft.common.pool.NCThreadPoolManager
import org.apache.nlpcraft.common.{NCE, NCService, U}

import java.io._
import java.net.URL
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala
import scala.util.Using

/**
  * External configuration manager.
  */
object NCExternalConfigManager extends NCService {
    private final val DFLT_DIR = ".nlpcraft/extcfg"
    private final val MD5_FILE = "md5.txt"
    private implicit final val ec: ExecutionContext = NCThreadPoolManager.getSystemContext

    case class Holder(typ: NCExternalConfigType, files: Set[String], modules: Set[NCModule])

    private final val FILES =
        Set(
            Holder(
                GEO,
                Set("cc_by40_geo_config.zip"),
                Set(SERVER)
            ),
            Holder(
                BADFILTER,
                Set("swear_words.txt"),
                Set(SERVER, PROBE)
            ),
            Holder(
                SPELL,
                Set("cc_by40_spell_config.zip"),
                Set(SERVER)
            ),
            Holder(
                OPENNLP,
                Set(
                    "en-pos-maxent.bin",
                    "en-ner-location.bin",
                    "en-ner-date.bin",
                    "en-token.bin",
                    "en-lemmatizer.dict",
                    "en-ner-percentage.bin",
                    "en-ner-person.bin",
                    "en-ner-money.bin",
                    "en-ner-time.bin",
                    "en-ner-organization.bin"
                ),
                Set(SERVER, PROBE)
            )
        )

    private object Config extends NCConfigurable {
        val url: String = getString("nlpcraft.extConfig.extUrl")
        val checkMd5: Boolean = getBool("nlpcraft.extConfig.checkMd5")
        val dir: File = new File(getStringOpt("nlpcraft.extConfig.locDir").getOrElse(s"${U.homeFileName(DFLT_DIR)}"))

        @throws[NCE]
        def check(): Unit = checkAndPrepareDir(Config.dir)
    }

    Config.check()

    private case class Download(fileName: String, typ: NCExternalConfigType) {
        val destDir: File = new File(Config.dir, type2String(typ))
        val file: File = new File(destDir, fileName)
        val isZip: Boolean = {
            val lc = file.getName.toLowerCase

            lc.endsWith(".gz") || lc.endsWith(".zip")
        }
    }

    case class FileHolder(name: String, typ: NCExternalConfigType) {
        val dir = new File(Config.dir, type2String(typ))

        checkAndPrepareDir(dir)

        val file: File = new File(dir, name)
    }

    private object Md5 {
        case class Key(typ: NCExternalConfigType, resource: String)

        private lazy val m: Map[Key, String] = {
            val url = s"${Config.url}/$MD5_FILE"

            try
                Using.resource(Source.fromURL(url)) { src =>
                    src.getLines().map(_.trim()).filter(s => s.nonEmpty && !s.startsWith("#")).map(f = p => {
                        def splitPair(s: String, sep: String): (String, String) = {
                            val seq = s.split(sep).map(_.strip)

                            if (seq.length != 2 || seq.exists(_.isEmpty))
                                throw new NCE(s"Unexpected '$url' file line format: '$p'")

                            (seq(0), seq(1))
                        }

                        val (resPath, md5) = splitPair(p, " ")
                        val (t, res) = splitPair(resPath, "/")

                        Key(string2Type(t), res) -> md5
                    }).toList.toMap
                }
            catch {
                case e: IOException => throw new NCE(s"Failed to read: '$url'", e)
            }
        }

        /**
          *
          * @param f
          * @param typ
          */
        @throws[NCE]
        def isValid(f: File, typ: NCExternalConfigType): Boolean = {
            val v1 = m.getOrElse(Key(typ, f.getName), throw new NCE(s"MD5 data not found for: '${f.getAbsolutePath}'"))

            val v2 =
                try
                    Using.resource(Files.newInputStream(f.toPath)) { in => DigestUtils.md5Hex(in) }
                catch {
                    case e: IOException => throw new NCE(s"Failed to get MD5 for: '${f.getAbsolutePath}'", e)
                }

            v1 == v2
        }
    }

    /**
      * Starts this service.
      *
      * @param parent Optional parent span.
      */
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { _ =>
        ackStarting()

        require(NCExternalConfigType.values.size == FILES.map(_.typ).toSeq.distinct.size)

        val module = NCModule.getModule

        val mFiles = FILES.filter(_.modules.contains(module)).map(p => p.typ -> p).toMap

        if (mFiles.nonEmpty) {
            val m = new ConcurrentHashMap[NCExternalConfigType, File]

            U.executeParallel(
                mFiles.values.flatMap(p => p.files.map(f => FileHolder(f, p.typ))).toSeq.map(f => () => processFile(f, m)): _*
            )

            val downTypes = m.asScala

            if (downTypes.nonEmpty) {
                U.executeParallel(downTypes.values.toSeq.map(d => () => clearDir(d)): _*)
                U.executeParallel(
                    downTypes.keys.toSeq.
                        flatMap(t => mFiles(t).files.toSeq.map(f => Download(f, t))).map(d => () => download(d)): _*
                )
            }
        }

        ackStarted()
    }

    /**
     * Stops this service.
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ =>
        ackStopping()
        ackStopped()
    }

    /**
     *
     * @param typ Resource type.
     * @param res Resource name.
     * @return
     */
    private def mkFile(typ: NCExternalConfigType, res: String): File = {
        val file = new File(Config.dir, getResourcePath(typ, res))

        if (!file.exists() || !file.canRead)
            throw new NCE(
                s"Configuration file '$res' not found or not readable. Delete all files from an external " +
                s"configuration folder '${Config.dir}' and restart."
            )

        file
    }

    /**
      *
      * @param typ
      * @param res
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def getContent(typ: NCExternalConfigType, res: String, parent: Span = null): String =
        startScopedSpan("getContent", parent, "res" -> res) { _ =>
            mkString(U.readFile(mkFile(typ, res)))
        }

    /**
      *
      * @param typ
      * @param res
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def getStream(typ: NCExternalConfigType, res: String, parent: Span = null): InputStream =
        startScopedSpan("getStream", parent, "res" -> res) { _ =>
            new BufferedInputStream(new FileInputStream(mkFile(typ, res)))
        }

    /**
      * The external resources have higher priority.
      *
      * @param resDir
      * @param resFilter
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def getDirContent(
        typ: NCExternalConfigType, resDir: String, resFilter: String => Boolean, parent: Span = null
    ): LazyList[NCExternalConfigHolder] =
        startScopedSpan("getDirContent", parent, "resDir" -> resDir) { _ =>
            val resDirPath = getResourcePath(typ, resDir)

            val d = new File(Config.dir, resDirPath)

            if (!d.exists || !d.isDirectory)
                throw new NCE(s"'${d.getAbsolutePath}' is not a valid folder.")

            val files =
                d.listFiles(new FileFilter { override def accept(f: File): Boolean = f.isFile && resFilter(f.getName) })

            if (files != null)
                files.to(LazyList).map(f => NCExternalConfigHolder(typ, f.getName, mkString(U.readFile(f))))
            else
                LazyList.empty
        }

    /**
      *
      * @param h
      * @param m
      */
    @throws[NCE]
    private def processFile(h: FileHolder, m: ConcurrentHashMap[NCExternalConfigType, File]): Unit =
        if (h.file.exists()) {
            if (h.file.isDirectory)
                throw new NCE(s"Unexpected folder (expecting a file): ${h.file.getAbsolutePath}")

            if (h.file.length() == 0 || Config.checkMd5 && !Md5.isValid(h.file, h.typ)) {
                logger.warn(
                    s"File '${h.file.getAbsolutePath}' appears to be corrupted. " +
                        s"All related files will be deleted and downloaded again."
                )

                m.put(h.typ, h.dir)
            }
        }
        else
            m.put(h.typ, h.dir)

    /**
      *
      * @param d
      */
    @throws[NCE]
    private def download(d: Download): Unit = {
        val filePath = d.file.getAbsolutePath
        val url = s"${Config.url}/${type2String(d.typ)}/${d.file.getName}"

        try
            Using.resource(new BufferedInputStream(new URL(url).openStream())) { src =>
                Using.resource(new FileOutputStream(d.file)) { dest =>
                    IOUtils.copy(src, dest)
                }

                logger.info(s"One-time download for external config [url='$url', file='$filePath']")
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to download external config [url='$url', file='$filePath']", e)
        }

        def safeDelete(): Unit =
            if (!d.file.delete())
                logger.warn(s"Couldn't delete file: '$filePath'")

        if (Config.checkMd5 && !Md5.isValid(d.file, d.typ)) {
            safeDelete()

            throw new NCE(s"Unexpected md5 sum for downloaded file: '$filePath'")
        }

        if (d.isZip) {
            val destDirPath = d.destDir.getAbsolutePath

            try {
                U.unzip(filePath, destDirPath)

                logger.trace(s"File unzipped [file='$filePath', dest='$destDirPath']")
            }
            catch {
                case e: NCE =>
                    safeDelete()

                    throw e
            }
        }
    }

    /**
      *
      * @param typ
      */
    private def type2String(typ: NCExternalConfigType): String = typ.toString.toLowerCase

    /**
      *
      * @param s
      */
    @throws[NCE]
    private def string2Type(s: String) =
        try
            NCExternalConfigType.withName(s.toUpperCase)
        catch {
            case e: IllegalArgumentException => throw new NCE(s"Invalid type: '$s'", e)
        }

    /**
      *
      * @param res
      */
    private def mkString(res: Seq[String]): String = res.mkString("\n")

    /**
      *
      * @param d
      */
    @throws[NCE]
    private def checkAndPrepareDir(d: File): Unit =
        if (d.exists()) {
            if (!d.isDirectory)
                throw new NCE(s"'${d.getAbsolutePath}' is not a valid folder.")
        }
        else {
            if (!d.mkdirs())
                throw new NCE(s"'${d.getAbsolutePath}' folder cannot be created.")
        }

    /**
      *
      * @param typ
      * @param res
      */
    private def getResourcePath(typ: NCExternalConfigType, res: String): String = s"${type2String(typ)}/$res"

    /**
      *
      * @param d
      */
    @throws[NCE]
    private def clearDir(d: File): Unit = {
        val path = d.getAbsolutePath

        U.clearFolder(path)

        logger.debug(s"Folder cleared: '$path'")
    }
}