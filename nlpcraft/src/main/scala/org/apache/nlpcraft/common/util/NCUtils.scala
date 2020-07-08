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

package org.apache.nlpcraft.common.util

import java.io._
import java.math.RoundingMode
import java.net._
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.sql.Timestamp
import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.concurrent.{ExecutorService, TimeUnit}
import java.util.jar.JarFile
import java.util.stream.Collectors
import java.util.zip.{ZipInputStream, GZIPInputStream => GIS, GZIPOutputStream ⇒ GOS}
import java.util.{Locale, Properties, Random, Timer, TimerTask, Calendar ⇒ C}

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.gson.Gson
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.blowfish.NCBlowfishHasher
import resource._

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.language.{implicitConversions, postfixOps}
import scala.reflect.runtime.universe._
import scala.sys.SystemProperties
import scala.util.control.Exception.ignoring
import scala.util.{Failure, Success}

/**
  * Project-wide, global utilities ans miscellaneous functions.
  */
object NCUtils extends LazyLogging {
    final val REGEX_FIX = "//"
    final val DSL_FIX = "^^"

    final val DFLT_PROBE_TOKEN = "3141592653589793"

    private val idGen = new NCIdGenerator(NCBlowfishHasher.salt(), 8)

    // Various decimal formats.
    private final val DEC_FMT0 = mkDecimalFormat("#0")
    private final val DEC_FMT1 = mkDecimalFormat("#0.0")
    private final val DEC_FMT2 = mkDecimalFormat("#0.00")

    private final lazy val DEC_FMT_SYMS = new DecimalFormatSymbols(Locale.US)

    private final lazy val GSON = new Gson()
    private final lazy val YAML = {
        new ObjectMapper(new YAMLFactory).
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
            registerModule(new DefaultScalaModule()).
            setSerializationInclusion(Include.NON_NULL).
            setSerializationInclusion(Include.NON_EMPTY)
    }

    private def mkDecimalFormat(ptrn: String) = {
        val df = new DecimalFormat(ptrn, DEC_FMT_SYMS)

        df.setRoundingMode(RoundingMode.DOWN)

        df
    }

    // OS kinds.
    private var win95 = false
    private var win98 = false
    private var winNt = false
    private var winVista = false
    private var win7 = false
    private var win8 = false
    private var unknownWin = false
    private var win2k = false
    private var winXp = false
    private var win2003 = false
    private var win2008 = false
    private var unix = false
    private var solaris = false
    private var linux = false
    private var netware = false
    private var mac = false

    // Unix suffixes.
    private final val UNIX_SFX = Seq("ix", "inux", "olaris", "un", "ux", "sco", "bsd", "att")

    detectOs()

    // Detects current OS.
    private def detectOs() {
        val os = sys.props("os.name").toLowerCase

        if (os.contains("win"))
            if (os.contains("95")) win95 = true
            else if (os.contains("98")) win98 = true
            else if (os.contains("nt")) winNt = true
            else if (os.contains("2000")) win2k = true
            else if (os.contains("vista")) winVista = true
            else if (os.contains("xp")) winXp = true
            else if (os.contains("2003")) win2003 = true
            else if (os.contains("2008")) win2008 = true
            else if (os.contains("7")) win7 = true
            else if (os.contains("8")) win8 = true
            else unknownWin = true
        else if (os.contains("netware")) netware = true
        else if (os.contains("mac os")) mac = true
        else {
            unix = UNIX_SFX.exists(os.contains(_))

            if (os.contains("olaris")) solaris = true
            else if (os.contains("inux")) linux = true
        }
    }

    // OS kinds.
    lazy val isWindow95: Boolean = win95
    lazy val isWindow98: Boolean = win98
    lazy val isWindowNt: Boolean = winNt
    lazy val isWindowsVista: Boolean = winVista
    lazy val isWindows7: Boolean = win7
    lazy val isWindows8: Boolean = win8
    lazy val isUnknownWindows: Boolean = unknownWin
    lazy val isWindows2k: Boolean = win2k
    lazy val isWindowsXp: Boolean = winXp
    lazy val isWindows2003: Boolean = win2003
    lazy val isWindows2008: Boolean = win2008
    lazy val isUnix: Boolean = unix
    lazy val isSolaris: Boolean = solaris
    lazy val isLinux: Boolean = linux
    lazy val isNetware: Boolean = netware
    lazy val isMac: Boolean = mac
    lazy val isNix: Boolean = mac || linux || solaris || unix
    lazy val isWindows: Boolean = win95 || win98 || winNt || winVista || win7 || win8 || win2k || winXp || win2003 || win2008

    private final val UTC = ZoneId.of("UTC")

    private final val RND = new Random()

    private val sysProps = new SystemProperties

    /**
      * Gets now in UTC timezone.
      */
    def nowUtc(): ZonedDateTime = ZonedDateTime.now(UTC)

    /**
      * Gets now in UTC timezone in milliseconds representation.
      */
    def nowUtcMs(): Long = Instant.now().toEpochMilli

    /**
      * Gets now in UTC timezone in SQL Timestamp representation.
      */
    def nowUtcTs(): Timestamp = new Timestamp(Instant.now().toEpochMilli)

    /**
      * Escapes given string for JSON according to RFC 4627 http://www.ietf.org/rfc/rfc4627.txt.
      *
      * @param s String to escape.
      * @return Escaped string.
      */
    def escapeJson(s: String): String = {
        val len = s.length

        if (len == 0)
            ""
        else {
            val sb = new StringBuilder

            for (ch ← s.toCharArray)
                ch match {
                    case '\\' | '"' ⇒ sb += '\\' += ch
                    case '/' ⇒ sb += '\\' += ch
                    case '\b' ⇒ sb ++= "\\b"
                    case '\t' ⇒ sb ++= "\\t"
                    case '\n' ⇒ sb ++= "\\n"
                    case '\f' ⇒ sb ++= "\\f"
                    case '\r' ⇒ sb ++= "\\r"
                    case _ ⇒
                        if (ch < ' ') {
                            val t = "000" + Integer.toHexString(ch)

                            sb ++= "\\u" ++= t.substring(t.length - 4)
                        }
                        else
                            sb += ch
                }

            sb.toString()
        }
    }

    /**
      * Converts closure to a runnable.
      *
      * @param f Closure to convert.
      */
    implicit def toRun(f: ⇒ Unit): Runnable = () ⇒ try {
        f
    }
    catch {
        case _: InterruptedException ⇒ Thread.currentThread().interrupt()
        case e: Throwable ⇒ logger.error("Unhandled exception caught.", e)
    }

    /**
      * Destroys given process (using proper waiting algorithm).
      *
      * @param proc Process to destroy. No-op if `null`.
      */
    def destroyProcess(proc: Process): Unit = {
        if (proc != null) {
            proc.destroy()

            while (!proc.waitFor(100, TimeUnit.MILLISECONDS)) {
                Thread.sleep(100)

                proc.destroy()
            }
        }
    }

    /**
      * Type case with option.
      */
    def as[T: Manifest](any: Any): Option[T] = any match {
        case _: T ⇒ Some(any.asInstanceOf[T])
        case _ ⇒ None
    }

    /**
      *
      * @param body Expression that can produce [[InterruptedException]].
      */
    def ignoreInterrupt(body: ⇒ Unit): Unit =
        try {
            body
        }
        catch {
            case _: InterruptedException ⇒ ()
        }

    /**
      * Converts object's package name into path.
      */
    def toPath(a: Any): String = toPath(a.getClass)

    /**
      * Converts class into path.
      */
    def toPath(`class`: Class[_]): String = `class`.getPackage.getName.replaceAll("\\.", "/")

    /**
      * Reads lines from given file.
      *
      * @param path File path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readPath(path: String, enc: String, log: Logger = logger): Iterator[String] =
        readFile(new File(path), enc, log)

    /**
      * Reads lines from given resource.
      *
      * @param res Resource path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readResource(res: String, enc: String, log: Logger = logger): Iterator[String] =
        readStream(getStream(res), enc, log)

    /**
      * Reads lines from given file.
      *
      * @param path Zipped file path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readGzipPath(path: String, enc: String, log: Logger = logger): Iterator[String] =
        readGzipFile(new File(path), enc, log)

    /**
      * Reads lines from given file.
      *
      * @param f File to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readFile(f: File, enc: String, log: Logger = logger): Iterator[String] = {
        var src: Source = null

        try {
            src = Source.fromFile(f, enc)

            val data = src.getLines().map(p ⇒ p)

            log.trace(s"Loaded file: ${f.getAbsolutePath}")

            data
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read file: ${f.getAbsolutePath}", e)
        }
    }

    /**
      * Reads lines from given stream.
      *
      * @param in Stream to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readStream(in: InputStream, enc: String, log: Logger = logger): Iterator[String] =
        try
            Source.fromInputStream(in, enc).getLines().map(p ⇒ p)
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read stream", e)
        }

    /**
      * Reads lines from given file converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param f File to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextFile(f: File, enc: String, log: Logger = logger): Iterator[String] = {
        var src: Source = null

        try {
            src = Source.fromFile(f, enc)

            val data = src.getLines().map(_.toLowerCase.trim).filter(s ⇒ !s.isEmpty && !s.startsWith("#"))

            log.trace(s"Loaded file: ${f.getAbsolutePath}")

            data
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read file: ${f.getAbsolutePath}", e)
        }
    }

    /**
      * Reads lines from given stream converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param in Stream to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextStream(in: InputStream, enc: String, log: Logger = logger): Iterator[String] =
        try {
            Source.fromInputStream(in, enc).getLines().map(_.toLowerCase.trim).
                filter(s ⇒ !s.isEmpty && !s.startsWith("#"))
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read stream", e)
        }

    /**
      * Reads lines from given file converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param f Zipped file to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextGzipFile(f: File, enc: String, log: Logger = logger): Iterator[String] =
        try {
            val data = Source.fromInputStream(new GIS(new FileInputStream(f)), enc).getLines().map(_.toLowerCase.trim).
                filter(s ⇒ !s.isEmpty && !s.startsWith("#"))

            log.trace(s"Loaded file: ${f.getAbsolutePath}")

            data
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read file: ${f.getAbsolutePath}", e)
        }

    /**
      * Reads lines from given stream converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param res Zipped resource to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextGzipResource(res: String, enc: String, log: Logger = logger): Iterator[String] =
        try
            Source.fromInputStream(new GIS(getStream(res)), enc).getLines().map(_.toLowerCase.trim).
                filter(s ⇒ !s.isEmpty && !s.startsWith("#"))
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read stream", e)
        }

    /**
      * Reads lines from given file converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param path File path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextPath(path: String, enc: String, log: Logger = logger): Iterator[String] =
        readTextFile(new File(path), enc, log)

    /**
      * Reads lines from given resource converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param res Resource to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextResource(res: String, enc: String, log: Logger = logger): Iterator[String] =
        readTextStream(getStream(res), enc, log)

    /**
      * Reads lines from given file converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param path Zipped file path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextGzipPath(path: String, enc: String, log: Logger = logger): Iterator[String] =
        readTextGzipFile(new File(path), enc, log)

    /**
      *
      * @param path Folder path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextFolder(path: String, enc: String, log: Logger = logger): Iterator[String] =
        (for (file ← new File(path).listFiles()) yield
            readTextFile(file, enc, logger)).toIterator.flatten

    /**
      * Converts given name into properly capitalized first and last name.
      *
      * @param name Full name.
      */
    def toFirstLastName(name: String): (String, String) = {
        val parts = name.trim.split(' ')

        val firstName = formatName(parts.head)
        val lastName = formatName(parts.tail.mkString(" "))

        (firstName, lastName)
    }

    /**
      * Properly capitalizes name (first name or last name).
      *
      * @param name First or last name.
      */
    def formatName(name: String): String = {
        name.trim.toLowerCase.capitalize
    }

    /**
      * Makes daily timer.
      *
      * @param name Timer name.
      * @param body Body function.
      * @param hour Hours of start.
      * @param mins Minutes of start. Optional.
      * @param secs Seconds of start. Optional.
      */
    def mkDailyTimer(name: String, body: Unit ⇒ Unit, hour: Int, mins: Int = 0, secs: Int = 0): Timer = {
        val timer = new Timer()

        val cal = C.getInstance()

        val now = cal.getTime

        cal.set(C.HOUR_OF_DAY, hour)
        cal.set(C.MINUTE, mins)
        cal.set(C.SECOND, secs)

        if (cal.getTime.before(now))
            cal.add(C.DAY_OF_YEAR, 1)

        val firstTime = cal.getTime

        val period = 24 * 60 * 60 * 1000

        timer.schedule(
            new TimerTask {
                override def run(): Unit = {
                    val now = System.currentTimeMillis()

                    try {
                        body(())

                        logger.debug(s"Timer task executed [name=$name, execution-time=${System.currentTimeMillis() - now}]")
                    }
                    catch {
                        case e: Throwable ⇒ logger.error(s"Error executing daily timer [name=$name]", e)
                    }
                }
            },
            firstTime,
            period
        )

        logger.trace(s"Timer started [name=$name, first-execution-time=$firstTime, period=$period]")

        timer
    }

    /**
      * Reads lines from given file.
      *
      * @param f Zipped file to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readGzipFile(f: File, enc: String, log: Logger = logger): Iterator[String] =
        try {
            val data = Source.fromInputStream(new GIS(new FileInputStream(f)), enc).getLines().map(p ⇒ p)

            log.trace(s"Loaded file: ${f.getAbsolutePath}")

            data
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read file: ${f.getAbsolutePath}", e)
        }

    /**
      *
      * @param in Zipped stream to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      * @return
      */
    @throws[NCE]
    def readGzipResource(in: InputStream, enc: String, log: Logger = logger): Iterator[String] =
        try
            Source.fromInputStream(new GIS(in), enc).getLines().map(p ⇒ p)
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read stream", e)
        }

    /**
      * Reads bytes from given file.
      *
      * @param path File path.
      * @param log Logger.
      */
    @throws[NCE]
    def readPathBytes(path: String, log: Logger = logger): Array[Byte] =
        readFileBytes(new File(path), log)

    /**
      * Reads bytes from given file.
      *
      * @param f File.
      * @param log Logger.
      */
    @throws[NCE]
    def readFileBytes(f: File, log: Logger = logger): Array[Byte] = {
        try {
            val arr = new Array[Byte](f.length().toInt)

            managed(new FileInputStream(f)) acquireAndGet { in ⇒
                in.read(arr)
            }

            logger.trace(s"File read: $f")

            arr
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error reading file: $f", e)
        }
    }

    /**
      * Reads bytes from given file.
      *
      * @param f File to read from.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readBinaryFile(f: File, log: Logger = logger): Array[Byte] = {
        var src: Source = null

        try {
            src = Source.fromFile(f, "ISO-8859-1")

            val data = src.map(_.toByte).toArray

            log.trace(s"Loaded file: ${f.getAbsolutePath}")

            data
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read file: ${f.getAbsolutePath}", e)
        }
    }

    /**
      * Reads bytes from given file.
      *
      * @param path File path to read from.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readBinaryPath(path: String, log: Logger = logger): Array[Byte] =
        readBinaryFile(new File(path), log)

    /**
      * Gzip file.
      *
      * @param f File.
      * @param log Logger.
      */
    @throws[NCE]
    def gzipFile(f: File, log: Logger = logger): Unit = {
        val gz = s"${f.getAbsolutePath}.gz"

        // Do not user BOS here - it makes files corrupted.
        try
            managed(new GOS(new FileOutputStream(gz))) acquireAndGet { stream ⇒
                stream.write(readFileBytes(f))

                stream.flush()
            }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error gzip file: $f", e)
        }

        if (!f.delete())
            throw new NCE(s"Error while deleting file: $f")

        logger.trace(s"File gzipped [source=$f, destination=$gz]")
    }

    /**
      * Gzip file.
      *
      * @param path File path.
      * @param log Logger.
      */
    @throws[NCE]
    def gzipPath(path: String, log: Logger = logger): Unit = gzipFile(new File(path), log)

    /**
      * Generates read-only text file with given path and strings.
      * Used by text files auto-generators.
      *
      * @param path Path of the output file.
      * @param lines Text data.
      * @param sort Whether to sort output or not.
      */
    @throws[IOException]
    def mkTextFile(path: String, lines: Traversable[Any], sort: Boolean = true) {
        val file = new File(path)

        managed(new PrintStream(file)) acquireAndGet {
            ps ⇒
                import java.util._

                // Could be long for large sequences...
                val seq =
                    if (sort)
                        lines.map(_.toString).toSeq.sorted
                    else
                        lines

                ps.println(s"#")
                ps.println(s"# Licensed to the Apache Software Foundation (ASF) under one or more")
                ps.println(s"# contributor license agreements.  See the NOTICE file distributed with")
                ps.println(s"# this work for additional information regarding copyright ownership.")
                ps.println(s"# The ASF licenses this file to You under the Apache License, Version 2.0")
                ps.println(s"# (the 'License'); you may not use this file except in compliance with")
                ps.println(s"# the License.  You may obtain a copy of the License at")
                ps.println(s"#")
                ps.println(s"#      http://www.apache.org/licenses/LICENSE-2.0")
                ps.println(s"#")
                ps.println(s"# Unless required by applicable law or agreed to in writing, software")
                ps.println(s"# distributed under the License is distributed on an 'AS IS' BASIS,")
                ps.println(s"# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.")
                ps.println(s"# See the License for the specific language governing permissions and")
                ps.println(s"# limitations under the License.")
                ps.println(s"#")
                ps.println(s"# Auto-generated on: ${new Date()}")
                ps.println(s"# Total lines: ${seq.size}")
                ps.println(s"#")
                ps.println(s"# +-------------------------+")
                ps.println(s"# | DO NOT MODIFY THIS FILE |")
                ps.println(s"# +-------------------------+")
                ps.println(s"#")
                ps.println()

                seq.foreach(ps.println)

                // Make the file as read-only.
                file.setWritable(false, false)
        }

        // Ack.
        println(s"File generated: $path")
    }

    /**
      * Gets resource stream from classpath.
      *
      * @param res Resource.
      */
    @throws[NCE]
    def getStream(res: String): InputStream = {
        val in = getClass.getClassLoader.getResourceAsStream(res)

        if (in == null)
            throw new NCE(s"Resource not found: $res")

        in
    }

    /**
      * Gets resource existing flag.
      *
      * @param res Resource.
      */
    @throws[NCE]
    def hasResource(res: String): Boolean =
        getClass.getClassLoader.getResourceAsStream(res) != null

    /**
      * Serializes data from file.
      *
      * @param path File path.
      */
    @throws[NCE]
    def serializePath(path: String, obj: Any): Unit = {
        try {
            managed(new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) acquireAndGet { out ⇒
                out.writeObject(obj)
            }

            logger.info(s"File $path is written.")
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error writing file: $path", e)
        }
    }

    /**
      * Serializes data.
      *
      * @param obj Data.
      */
    @throws[NCE]
    def serialize(obj: Any): Array[Byte] = {
        try {
            managed(new ByteArrayOutputStream()) acquireAndGet { baos ⇒
                managed(new ObjectOutputStream(new BufferedOutputStream(baos))) acquireAndGet { out ⇒
                    out.writeObject(obj)
                }

                baos.toByteArray
            }
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error serialization data: $obj", e)
        }
    }

    /**
      * Serializes data from file.
      *
      * @param file File.
      */
    @throws[NCE]
    def serialize(file: File, obj: Any): Unit = {
        try {
            managed(new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) acquireAndGet { out ⇒
                out.writeObject(obj)
            }

            logger.info(s"File $file is written.")
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error writing file: $file", e)
        }
    }

    /**
      * Deserializes data from file.
      *
      * @param path File path.
      */
    @throws[NCE]
    def deserializePath[T](path: String, log: Logger = logger): T =
        try {
            val res = managed(new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)))) acquireAndGet { in ⇒
                in.readObject().asInstanceOf[T]
            }

            log.trace(s"Read file: $path")

            res
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error reading file: $path", e)
        }

    /**
      * Deserializes data.
      *
      * @param arr File path.
      */
    @throws[NCE]
    def deserialize[T](arr: Array[Byte]): T =
        try {
            managed(new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(arr)))) acquireAndGet { in ⇒
                in.readObject().asInstanceOf[T]
            }
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error deserialization data", e)
        }

    /**
      * Deserializes data from file.
      *
      * @param f File.
      * @param log Logger.
      */
    @throws[NCE]
    def deserialize[T](f: File, log: Logger = logger): T =
        try {
            val res = managed(new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)))) acquireAndGet { in ⇒
                in.readObject().asInstanceOf[T]
            }

            log.trace(s"Read file: ${f.getAbsolutePath}")

            res
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error reading file: $f", e)
        }

    /**
      * Wrap string value.
      *
      * @param s String value.
      */
    def wrapQuotes(s: String): String =
        s""""$s""""

    /**
      * Recursively removes all files and nested directories in a given folder.
      * Provided root folder itself is not removed.
      *
      * @param rootDir Folder to remove all nested files and directories in it.
      */
    @throws[IOException]
    def clearFolder(rootDir: String) {
        val rootPath = Paths.get(rootDir)

        Files.walkFileTree(rootPath, new SimpleFileVisitor[Path] {
            private def delete(path: Path) = {
                Files.delete(path)

                FileVisitResult.CONTINUE
            }

            override def postVisitDirectory(dir: Path, e: IOException): FileVisitResult =
                if (e == null)
                    if (!dir.equals(rootPath))
                        delete(dir)
                    else
                        FileVisitResult.CONTINUE
                else
                    throw e

            override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = delete(file)
        })
    }

    /**
      * Convenient utility to create future with given body and optional callbacks and execution context.
      *
      * @param body Body.
      * @param onFailure On error optional callback. No-op if not provided.
      * @param onSuccess On success optional callback. No-op if not provided.
      * @param ec Optional execution context. If not provided - the default Scala execution context will be used.
      */
    def asFuture[T](
        body: Unit ⇒ T,
        onFailure: Throwable ⇒ Unit = _ ⇒ Unit,
        onSuccess: T ⇒ Unit = (_: T) ⇒ ())(implicit ec: ExecutionContext = global): Future[T] = {
        val fut = Future {
            body(())
        }(ec)

        fut.onComplete {
            case Success(ok) ⇒ onSuccess(ok)
            case Failure(err) ⇒ onFailure(err)
        }(ec)

        fut
    }

    /**
      * Makes thread.
      *
      * @param name Name.
      * @param body Thread body.
      */
    def mkThread(name: String)(body: Thread ⇒ Unit): Thread =
        new Thread(name) {
            @volatile private var stopped = false

            override def isInterrupted: Boolean = super.isInterrupted || stopped

            override def interrupt(): Unit = {
                stopped = true

                super.interrupt()
            }

            override def run(): Unit = {
                logger.trace(s"Thread started: $name")

                try {
                    body(this)

                    logger.trace(s"Thread exited: $name")
                }
                catch {
                    case _: InterruptedException ⇒ logger.trace(s"Thread interrupted: $name")
                    case e: Throwable ⇒ logger.error(s"Unexpected error during thread execution: $name", e)
                }
                finally
                    stopped = true
            }
        }

    /**
      * System-wide process of normalizing emails (trim & lower case).
      *
      * @param email Email to normalize.
      */
    def normalizeEmail(email: String): String = email.trim.toLowerCase

    /**
      * Makes size restricted synchronized map.
      */
    def mkLRUMap[K, V](name: String, maxSize: Int): java.util.Map[K, V] =
        java.util.Collections.synchronizedMap(
            new java.util.LinkedHashMap[K, V]() {
                override def removeEldestEntry(eldest: java.util.Map.Entry[K, V]): Boolean = {
                    val b = size() > maxSize

                    if (b)
                        logger.warn(s"Map is too big (removing LRU item) [" +
                            s"name=$name, " +
                            s"max-size=$maxSize" +
                            s"]"
                        )

                    b
                }
            }
        )

    /**
      * Gets system property, or environment variable (in that order), or `None` if none exists.
      *
      * @param s Name of the system property or environment variable.
      */
    def sysEnv(s: String): Option[String] =
        sysProps.get(s).orElse(sys.env.get(s))

    /**
      * Returns `true` if given system property, or environment variable is provided and has value
      * 'true'. In all other cases returns `false`.
      *
      * @param s Name of the system property or environment variable.
      */
    def isSysEnvTrue(s: String): Boolean =
        sysEnv(s) match {
            case None ⇒ false
            case Some(v) ⇒ java.lang.Boolean.valueOf(v) == java.lang.Boolean.TRUE
        }

    /**
      * Gets random value from given sequence.
      *
      * @param seq Sequence.
      */
    def getRandom[T](seq: Seq[T]): T = seq(RND.nextInt(seq.size))

    /**
      * Makes random filled sequence with given length from initial.
      *
      * @param seq Initial sequence.
      * @param n Required sequence length.
      */
    def getRandomSeq[T](seq: Seq[T], n: Int): Seq[T] = {
        require(seq.lengthCompare(n) >= 0)

        val src = scala.collection.mutable.ArrayBuffer.empty[T] ++ seq
        val dest = scala.collection.mutable.ArrayBuffer.empty[T]

        (0 until n).foreach(_ ⇒ dest += src.remove(RND.nextInt(src.size)))

        dest
    }

    /**
      * Gets system property, or environment variable (in that order), or throws exception if none exists.
      *
      * @param s Name of the system property or environment variable.
      */
    @throws[NCE]
    def mandatorySysEnv(s: String): String =
        sysEnv(s) match {
            case Some(v) ⇒ v
            case None ⇒ throw new NCE(s"Cannot find environment variable or system property: $s")
        }

    /**
      * Compresses given string.
      *
      * @param rawStr String to compress.
      * @return Compressed Base64-encoded string.
      */
    def compress(rawStr: String): String = {
        val arr = new ByteArrayOutputStream(1024)

        managed(new GOS(arr)) acquireAndGet { zip ⇒
            zip.write(rawStr.getBytes)
        }

        Base64.encodeBase64String(arr.toByteArray)
    }

    /**
      * Uncompresses given Base64-encoded previously compressed string.
      *
      * @param zipStr Compressed string.
      * @return Uncompressed string.
      */
    def uncompress(zipStr: String): String =
        IOUtils.toString(new GIS(new ByteArrayInputStream(Base64.decodeBase64(zipStr))), Charset.defaultCharset())

    /**
      * Pimps integers with KB, MB, GB units of measure.
      *
      * @param v Integer value.
      */
    implicit class IntMemoryUnits(v: Int) {
        def TB: Int = v * 1024 * 1024 * 1024 * 1024

        def GB: Int = v * 1024 * 1024 * 1024

        def MB: Int = v * 1024 * 1024

        def KB: Int = v * 1024

        def tb: Int = TB

        def gb: Int = GB

        def mb: Int = MB

        def kb: Int = KB
    }

    /**
      * Pimps longs with KB, MB, GB units of measure.
      *
      * @param v Long value.
      */
    implicit class LongMemoryUnits(v: Long) {
        def TB: Long = v * 1024 * 1024 * 1024 * 1024

        def GB: Long = v * 1024 * 1024 * 1024

        def MB: Long = v * 1024 * 1024

        def KB: Long = v * 1024

        def tb: Long = TB

        def gb: Long = GB

        def mb: Long = MB

        def kb: Long = KB
    }


    /**
      * Pimps integers with time units.
      *
      * @param v Integer value.
      */
    implicit class IntTimeUnits(v: Int) {
        def MSECS: Int = v

        def MS: Int = v

        def SECS: Int = v * 1000

        def MINS: Int = v * 1000 * 60

        def HOURS: Int = v * 1000 * 60 * 60

        def DAYS: Int = v * 1000 * 60 * 60 * 24

        def msecs: Int = MSECS

        def ms: Int = MS

        def secs: Int = SECS

        def mins: Int = MINS

        def hours: Int = HOURS

        def days: Int = DAYS
    }

    /**
      * Pimps long with time units.
      *
      * @param v Long value.
      */
    implicit class LongTimeUnits(v: Long) {
        def MSECS: Long = v

        def MS: Long = v

        def SECS: Long = v * 1000

        def MINS: Long = v * 1000 * 60

        def HOURS: Long = v * 1000 * 60 * 60

        def DAYS: Long = v * 1000 * 60 * 60 * 24

        def msecs: Long = MSECS

        def ms: Long = MS

        def secs: Long = SECS

        def mins: Long = MINS

        def hours: Long = HOURS

        def days: Long = DAYS
    }

    /**
      * Sleeps number of msec properly handling exceptions.
      *
      * @param delay Number of msec to sleep.
      */
    def sleep(delay: Long): Unit =
        try
            Thread.sleep(delay)
        catch {
            case _: InterruptedException ⇒ Thread.currentThread().interrupt()
            case e: Throwable ⇒ logger.error("Unhandled exception caught during sleep.", e)
        }

    /**
      * Interrupts thread and waits for its finish.
      *
      * @param t Thread.
      */
    def stopThread(t: Thread): Unit =
        if (t != null) {
            t.interrupt()

            try
                t.join()
            catch {
                case _: InterruptedException ⇒ logger.trace("Thread joining was interrupted (ignoring).")
            }
        }

    /**
      * Shuts down executor services and waits for their finish.
      *
      * @param ess Executor services.
      */
    def shutdownPools(ess: ExecutorService*): Unit = {
        val seq = ess.filter(_ != null)

        seq.foreach(_.shutdown())
        seq.foreach(es ⇒
            try
                es.awaitTermination(Long.MaxValue, TimeUnit.MILLISECONDS)
            catch {
                case _: InterruptedException ⇒ () // Safely ignore.
            }
        )
    }

    /**
      * Gets full path for given file name in user's home folder.
      *
      * @param file File name.
      */
    def homeFileName(file: String): String = new File(System.getProperty("user.home"), file).getAbsolutePath

    /**
      * Non Empty Or Null (NEON).
      *
      * @param s String to check.
      */
    def neon(s: String): Boolean = s != null && !s.isEmpty

    /**
      * Generates (relatively) unique ID good for a short-term usage.
      */
    def genGuid(): String = idGen.encrypt(System.currentTimeMillis(), System.nanoTime())

    /**
      * Converts non-empty sequence of '\n' and '\s' into one ' '.
      *
      * @param s Object to remove spaces from.
      */
    def zipSpaces(s: AnyRef): String = s.toString.replaceAll("""[\n\s]+""", " ")

    /**
      * Pimps `Option[T]` with `getOrFail` function that improves on standard
      * `get` by adding user-defined descriptive error message in case of `None`.
      *
      * @param opt Option to pimp.
      */
    implicit class GetOrFail[T](val opt: Option[T]) extends AnyVal {
        @throws[NCE]
        def getOrFail(errMsg: String): T = if (opt.isDefined) opt.get else throw new NCE(errMsg)
    }

    /**
      * Checks duplicated elements in collection.
      *
      * @param seq Collection.
      * @param seen Checked elements.
      * @see #getDups
      */
    @annotation.tailrec
    def containsDups[T](seq: Seq[T], seen: Set[T] = Set.empty[T]): Boolean =
        seq match {
            case x :: xs ⇒ if (seen.contains(x)) true else containsDups(xs, seen + x)
            case _ ⇒ false
        }

    /**
      * Gets set of duplicate values from given sequence (potentially empty).
      *
      * @param seq Sequence to check for dups from.
      * @tparam T
      * @return
      * @see #containsDups
      */
    def getDups[T](seq: Seq[T]): Set[T] =
        seq.diff(seq.distinct).toSet

    /**
      * Gets a sequence without dups. It works by checking for dups first, before creating a new
      * sequence if dups are found. It's more efficient when dups are rare.
      *
      * @param seq Sequence with potential dups.
      */
    def distinct[T](seq: List[T]): List[T] =
        if (containsDups(seq))
            seq.distinct
        else
            seq

    /**
      * Safely and silently closes the client socket.
      *
      * @param sock Client socket to close.
      */
    def close(sock: Socket): Unit =
        if (sock != null)
            ignoring(classOf[IOException]) {
                sock.close()
            }

    /**
      *
      * @param e
      * @return
      */
    @tailrec
    def getOriginCause(e: Throwable): Throwable =
        if (e == null)
            null
        else
            e.getCause match {
                case null ⇒ e // Original cause (bottom of the stack trace).
                case t ⇒ getOriginCause(t)
            }

    /**
      * Safely and silently closes the server socket.
      *
      * @param sock Server socket to close.
      */
    def close(sock: ServerSocket): Unit =
        if (sock != null)
            ignoring(classOf[IOException]) {
                sock.close()
            }

    /**
      *
      * @param in Stream.
      */
    def close(in: InputStream): Unit =
        if (in != null)
            ignoring(classOf[IOException]) {
                in.close()
            }

    /**
      *
      * @param out Stream.
      */
    def close(out: OutputStream): Unit =
        if (out != null)
            ignoring(classOf[IOException]) {
                out.close()
            }

    /**
      * Closes auto-closeable ignoring any exceptions.
      *
      * @param a Resource to close.
      */
    def close(a: AutoCloseable): Unit =
        if (a != null)
            ignoring(classOf[Exception]) {
                a.close()
            }


    /**
      * Formats given double number with provided precision.
      *
      * @param num Number to format.
      * @param precision Number of digits after decimal point.
      */
    def format(num: Double, precision: Int): String = precision match {
        case 0 ⇒ DEC_FMT0.format(num)
        case 1 ⇒ DEC_FMT1.format(num)
        case _ ⇒ DEC_FMT2.format(num)
    }

    /**
      * Unzips file.
      *
      * @param zipFile Zip file.
      * @param outDir Output folder.
      */
    @throws[NCE]
    def unzip(zipFile: String, outDir: String): Unit = {
        @throws[NCE]
        def mkDir(dir: File): Unit =
            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs())
                    throw new NCE(s"Folder cannot be created: ${dir.getAbsolutePath}")
            }

        mkDir(new File(outDir))

        managed(new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) acquireAndGet { in ⇒
            var entry = in.getNextEntry

            while (entry != null) {
                val f = new File(outDir, entry.getName)

                if (!entry.isDirectory) {
                    mkDir(f.getParentFile)

                    try {
                        if (!f.createNewFile())
                            throw new NCE(s"File cannot be created: ${f.getAbsolutePath}")

                        managed(new BufferedOutputStream(new FileOutputStream(f))) acquireAndGet { out ⇒
                            IOUtils.copy(in, out)
                        }
                    }
                    catch {
                        case e: IOException ⇒ throw new NCE(s"IO error processing file: ${f.getAbsolutePath}.", e)
                    }
                }

                entry = in.getNextEntry
            }
        }
    }

    /**
      * Tokenize string splitting by space.
      *
      * @param s String for tokenization.
      */
    def tokenizeSpace(s: String): Seq[String] = s.split(" ")

    /**
      * Makes SHA256 hash.
      *
      * @param s String.
      */
    def mkSha256Hash(s: String): String = DigestUtils.sha256Hex(s)

    /**
      * Makes properties file based on input string.
      *
      * @param s String.
      */
    @throws[NCE]
    def mkProperties(s: String): Properties = {
        val p = new Properties()

        try
            p.load(new StringReader(s))
        catch {
            case e: IOException ⇒ throw new NCE(s"Error reading properties: $s", e)
        }

        p
    }

    /**
      * Converts error with its trace to string.
      *
      * @param t Error.
      */
    def toString(t: Throwable): String =
        managed(new ByteArrayOutputStream()) acquireAndGet { out ⇒
            managed(new PrintStream(out)) acquireAndGet { ps ⇒
                t.printStackTrace(ps)

                new String(out.toByteArray, "UTF8")
            }
        }

    /**
      * Makes absolute path starting from working directory.
      *
      * @param path Path.
      */
    def mkPath(path: String): String = new File(s"${new File("").getAbsolutePath}/$path").getAbsolutePath

    /**
      * Gets either environment variable or system property based path with given name.
      *
      * @param s Environment variable or system property name.
      */
    @throws[NCE]
    def getSysEnvPath(s: String): String = {
        // NOTE: system property overrides environment variable.
        val v = U.mandatorySysEnv(s)

        if (!new File(v).exists())
            throw new NCE(s"Path '$v' does not exist.")

        v
    }

    /**
      * Gets resources from resources folder.
      * based on http://www.uofr.net/~greg/java/get-resource-listing.html
      *
      * @param resDir Folder.
      */
    def getFilesResources(resDir: String): Seq[String] = {
        val clazz = getClass

        val url = {
            val x = clazz.getClassLoader.getResource(resDir)

            if (x != null) x else clazz.getClassLoader.getResource(clazz.getName.replace(".", "/") + ".class")
        }

        url.getProtocol match {
            case "file" ⇒
                managed(new InputStreamReader(getStream(resDir))) acquireAndGet { reader ⇒
                    managed(new BufferedReader(reader)) acquireAndGet { bReader ⇒
                        bReader.lines().collect(Collectors.toList[String]).asScala.map(p ⇒ s"$resDir/$p")
                    }
                }
            case "jar" ⇒
                val jar = new JarFile(URLDecoder.decode(url.getPath.substring(5, url.getPath.indexOf("!")), "UTF-8"))
                val entries = jar.entries

                val res = mutable.ArrayBuffer.empty[String]

                while (entries.hasMoreElements) {
                    val name = entries.nextElement.getName

                    if (name.startsWith(resDir) && name != s"$resDir/")
                        res += name
                }

                res
            case _ ⇒ throw new NCE(s"Cannot list files for: $resDir")
        }
    }

    /**
      *
      * @param resDir Resources folder.
      * @param extDirOpt External folder. Optional.
      * @param resFilter File filter.
      */
    def getContent(resDir: String, extDirOpt: Option[String], resFilter: String ⇒ Boolean): Stream[(String, String)] = {
        // The external resources have higher priority.
        val extData =
            extDirOpt match {
                case Some(extDir) ⇒
                    require(new File(extDir).exists())

                    val d = new File(extDir, resDir)

                    if (d.exists && d.isDirectory) {
                        val arr =
                            d.listFiles(new FileFilter {
                                override def accept(p: File): Boolean = p.isFile && resFilter(p.getName)
                            })

                        val seq: Seq[File] = if (arr != null) arr else Seq.empty

                        seq.map(f ⇒ f.getName → f).toMap
                    } else
                        Map.empty
                case None ⇒ Map.empty
            }

        val resData =
            if (hasResource(resDir))
                getFilesResources(resDir).filter(resFilter).map(p ⇒ new File(p).getName → p).toMap -- extData.keySet
            else
                Map.empty

        extData.values.toStream.map(f ⇒ f.getName → readFile(f, "UTF-8").mkString("\n")) ++
            resData.toStream.map(p ⇒ p._1 → readStream(getStream(p._2), "UTF-8").mkString("\n"))
    }

    /**
      *
      * @param res Resource name.
      * @param extDirOpt External folder. Optional.
      */
    def getContent(res: String, extDirOpt: Option[String]): String =
        (
            // The external resource has higher priority.
            extDirOpt match {
                case Some(extDir) ⇒
                    require(new File(extDir).exists())

                    val f = new File(extDir, res)

                    if (f.exists() && f.isFile) readFile(f, "UTF-8") else readStream(getStream(res), "UTF-8")
                case None ⇒ readStream(getStream(res), "UTF-8")
            }
        ).mkString("\n")

    /**
      * Gets external IP.
      */
    @throws[IOException]
    def getExternalIp: String =
        managed(new URL("http://checkip.amazonaws.com").openStream()) acquireAndGet { is ⇒
            managed(new InputStreamReader(is)) acquireAndGet { reader ⇒
                managed(new BufferedReader(reader)) acquireAndGet { bufReader ⇒
                    bufReader.readLine()
                }
            }
        }

    /**
      * Gets internal IP.
      */
    @throws[IOException]
    def getInternalAddress: InetAddress = {
        var res: Option[InetAddress] = None

        val en = NetworkInterface.getNetworkInterfaces

        while (en.hasMoreElements && res.isEmpty) {
            val nic = en.nextElement

            if (nic != null && nic.isUp) {
                val as = nic.getInetAddresses

                while (as.hasMoreElements && res.isEmpty) {
                    val addr = as.nextElement

                    if (
                        !addr.isLoopbackAddress && !addr.isLinkLocalAddress && addr.isInstanceOf[Inet4Address]
                    )
                        res = Some(addr)
                }
            }
        }

        res.getOrElse(InetAddress.getLocalHost)
    }

    /**
      * Creates object from JSON string.
      *
      * @param js JSON string.
      */
    @throws[NCE]
    def js2Obj(js: String): AnyRef =
        try
            GSON.fromJson(js, classOf[Object])
        catch {
            case e: Exception ⇒ throw new NCE(s"Failed to convert JSON string to map: $js", e)
        }

    /**
      *
      * @param bodies
      * @param ec
      */
    def executeParallel(bodies: (() ⇒ Any)*)(implicit ec: ExecutionContext = global): Unit =
        bodies.map(body ⇒ {
            Future {
                body()
            }(ec)
        }).foreach(f ⇒ Await.result(f, Duration.Inf))

    /**
      *
      * @param clsName Fully qualified class name to create object of.
      * @tparam T Type of the object to create.
      * @return New instance of the specified type.
      */
    def mkObject[T](clsName: String): T = {
        try
            // Try Java reflection first.
            Class.forName(clsName).getDeclaredConstructor().newInstance().asInstanceOf[T]
        catch {
            case _: Throwable ⇒
                // Try Scala reflection second.
                val mirror = runtimeMirror(getClass.getClassLoader)

                try
                    mirror.reflectModule(mirror.staticModule(clsName)).instance.asInstanceOf[T]
                catch {
                    case e: Throwable ⇒ throw new NCE(s"Error initializing object of type: $clsName", e)
                }
        }
    }

    /**
      * Gets simple class name of the caller removing '$' for Scala classes.
      *
      * @param clazz Class object.
      * @return Simple class name.
      */
    def cleanClassName(clazz: Class[_]): String = {
        val cls = clazz.getSimpleName

        if (cls.endsWith("$"))
            cls.substring(0, cls.length - 1)
        else
            cls
    }

    /**
      *
      * @param srvReqId Server request ID.
      * @return
      */
    def mkLogHolderKey(srvReqId: String): String = s"__NC_LOG_HOLDER_$srvReqId"

    /**
      * Sparsity depth (or rank) as sum of all gaps in indexes. Gap is a non-consecutive index.
      *
      * @param idx Sequence of indexes.
      * @return
      */
    def calcSparsity(idx: Seq[Int]): Int =
        idx.zipWithIndex.tail.map { case (v, i) ⇒ Math.abs(v - idx(i - 1)) }.sum - idx.length + 1

    /**
      * Extracts type `T` from given YAML `file`.
      *
      * @param f File to extract from.
      * @param ignoreCase Flag.
      * @tparam T Type of the object to extract.
      */
    @throws[NCE]
    def extractYamlFile[T](f: File, ignoreCase: Boolean, tr: TypeReference[T]): T =
        extractYamlString(readFile(f, "UTF8").mkString("\n"), f.getAbsolutePath, ignoreCase, tr)

    /**
      * Extracts type `T` from given YAML `resource`.
      *
      * @param res Resource to extract from.
      * @param ignoreCase Flag.
      * @tparam T Type of the object to extract.
      */
    @throws[NCE]
    def extractYamlResource[T](res: String, ignoreCase: Boolean, tr: TypeReference[T]): T =
        extractYamlString(readStream(getStream(res), "UTF8").mkString("\n"), res, ignoreCase, tr)

    /**
      * Extracts type `T` from given YAML `data`.
      *
      * @param data String data to extract from.
      * @param res Resource (for errors messages)
      * @param ignoreCase Flag.
      * @tparam T Type of the object to extract.
      */
    @throws[NCE]
    def extractYamlString[T](data: String, res: String, ignoreCase: Boolean, tr: TypeReference[T]): T =
        try
            YAML.readValue(if (ignoreCase) data.toLowerCase else data, tr)
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read: $res", e)
            case e: Throwable ⇒ throw new NCE(s"Failed to parse: $res", e)
        }

    /**
      *
      * @return
      */
    def getYamlMapper: ObjectMapper = YAML
}