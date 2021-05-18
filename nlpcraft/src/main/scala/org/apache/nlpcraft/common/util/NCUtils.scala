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
import java.lang.reflect.Type
import java.math.RoundingMode
import java.net._
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths, _}
import java.nio.file.attribute.BasicFileAttributes
import java.sql.Timestamp
import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, LinkedBlockingQueue, RejectedExecutionHandler, ThreadFactory, ThreadPoolExecutor, TimeUnit}
import java.util.jar.JarFile
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.zip.{ZipInputStream, GZIPInputStream => GIS, GZIPOutputStream => GOS}
import java.util.{Locale, Properties, Random, Timer, TimerTask, Calendar => C}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.gson.{GsonBuilder, JsonElement}
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.apache.nlpcraft.common.blowfish.NCBlowfishHasher
import org.apache.nlpcraft.common.version.NCVersion
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import scala.annotation.tailrec
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.{BufferedSource, Source}
import scala.jdk.CollectionConverters._
import scala.language.{implicitConversions, postfixOps}
import scala.reflect.{ClassTag, classTag}
import scala.reflect.runtime.universe._
import scala.sys.SystemProperties
import scala.util.control.Exception.ignoring
import scala.util._

/**
  * Project-wide, global utilities ans miscellaneous functions.
  */
object NCUtils extends LazyLogging {
    private final val ANSI_SEQ = Pattern.compile("\u001B\\[[?;\\d]*[a-zA-Z]")

    final val REGEX_FIX = "//"
    final val IDL_FIX = "^^"

    final val DFLT_PROBE_TOKEN = "3141592653589793"

    final val NL = System getProperty "line.separator"

    private val idGen = new NCIdGenerator(NCBlowfishHasher.salt(), 8)

    private final val DISABLE_GA_PROP = "NLPCRAFT_DISABLE_GA"

    private lazy val ANSI_FG_COLORS = Seq(
        ansiRedFg,
        ansiGreenFg,
        ansiBlueFg,
        ansiYellowFg,
        ansiWhiteFg,
        ansiBlackFg,
        ansiCyanFg
    )
    private lazy val ANSI_BG_COLORS = Seq(
        ansiRedBg,
        ansiGreenBg,
        ansiBlueBg,
        ansiYellowBg,
        ansiWhiteBg,
        ansiBlackBg,
        ansiCyanBg
    )
    private lazy val ANSI_COLORS = for (fg <- ANSI_FG_COLORS; bg <- ANSI_BG_COLORS) yield s"$fg$bg"

    // Various decimal formats.
    private final val DEC_FMT0 = mkDecimalFormat("#0")
    private final val DEC_FMT1 = mkDecimalFormat("#0.0")
    private final val DEC_FMT2 = mkDecimalFormat("#0.00")

    private final lazy val DEC_FMT_SYMS = new DecimalFormatSymbols(Locale.US)

    private final lazy val GSON = new GsonBuilder().setPrettyPrinting().create()
    private final lazy val YAML = new ObjectMapper(new YAMLFactory).
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
        registerModule(new DefaultScalaModule()).
        setSerializationInclusion(Include.NON_NULL).
        setSerializationInclusion(Include.NON_EMPTY)

    private def mkDecimalFormat(ptrn: String) = {
        val df = new DecimalFormat(ptrn, DEC_FMT_SYMS)

        df.setRoundingMode(RoundingMode.DOWN)

        df
    }

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
     * Strips ANSI escape sequences from the given string.
     *
     * @param s
     * @return
     */
    def stripAnsi(s: String): String =
        ANSI_SEQ.matcher(s).replaceAll("")

    /**
     * Trims each sequence string and filters out empty ones.
     *
     * @param s String to process.
     * @return
     */
    def trimFilter(s: Seq[String]): Seq[String] =
        s.map(_.strip).filter(_.nonEmpty)

    /**
     * Splits, trims and filters empty strings for the given string.
     *
     * @param s String to split.
     * @param sep Separator (regex) to split by.
     * @return
     */
    def splitTrimFilter(s: String, sep: String): Seq[String] =
        trimFilter(s.split(sep))

    /**
     * Recursively removes quotes from given string.
     *
     * @param s
     * @return
     */
    @tailrec
    def trimQuotes(s: String): String = {
        val z = s.strip

        if ((z.startsWith("'") && z.endsWith("'")) || (z.startsWith("\"") && z.endsWith("\"")))
            trimQuotes(z.substring(1, z.length - 1))
        else
            z
    }

    /**
     * Recursively removes quotes and replaces escaped quotes from given string.
     *
     * @param s
     * @return
     */
    @tailrec
    def trimEscapesQuotes(s: String): String = {
        val z = s.strip

        if (z.nonEmpty) {
            if (z.head == '\'' && z.last == '\'')
                trimEscapesQuotes(z.substring(1, z.length - 1).replace("\'", "'"))
            else if (z.head == '"' && z.last == '"')
                trimEscapesQuotes(z.substring(1, z.length - 1).replace("\\\"", "\""))
            else
                z
        }
        else
            z
    }

    /**
     * Recursively removes quotes and replaces escaped quotes from given string.
     *
     * @param s
     * @return
     */
    @tailrec
    def escapesQuotes(s: String): String =
        if (s.nonEmpty) {
            if (s.head == '\'' && s.last == '\'')
                escapesQuotes(s.substring(1, s.length - 1).replace("\'", "'"))
            else if (s.head == '"' && s.last == '"')
                escapesQuotes(s.substring(1, s.length - 1).replace("\\\"", "\""))
            else
                s
        }
        else
            s

    /**
     *
     * @param s
     * @param sep
     * @return
     */
    def normalize(s: String, sep: String): String =
        splitTrimFilter(s, sep).mkString(sep)

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

            for (ch <- s.toCharArray)
                ch match {
                    case '\\' | '"' => sb += '\\' += ch
                    case '/' => sb += '\\' += ch
                    case '\b' => sb ++= "\\b"
                    case '\t' => sb ++= "\\t"
                    case '\n' => sb ++= "\\n"
                    case '\f' => sb ++= "\\f"
                    case '\r' => sb ++= "\\r"
                    case _ =>
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
    implicit def toRun(f: => Unit): Runnable = () => try {
        f
    }
    catch {
        case _: InterruptedException => Thread.currentThread().interrupt()
        case e: Throwable => prettyError(logger, "Unhandled exception caught:", e)
    }

    /**
      * Destroys given process (using proper waiting algorithm).
      *
      * @param proc Process to destroy. No-op if `null`.
      */
    def destroyProcess(proc: java.lang.Process): Unit = {
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
        case _: T => Some(any.asInstanceOf[T])
        case _ => None
    }

    /**
      *
      * @param body Expression that can produce [[InterruptedException]].
      */
    def ignoreInterrupt(body: => Unit): Unit =
        try {
            body
        }
        catch {
            case _: InterruptedException => ()
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
    def readPath(path: String, enc: String = "UTF-8", log: Logger = logger): List[String] =
        readFile(new File(path), enc, log)

    /**
      * Reads lines from given resource.
      *
      * @param res Resource path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readResource(res: String, enc: String = "UTF-8", log: Logger = logger): List[String] = readStream(getStream(res), enc, log)

    /**
      * Maps lines from the given resource to an object.
      *
      * @param res Resource path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      * @param mapper Function to map lines.
      */
    @throws[NCE]
    def mapResource[T](res: String, enc: String = "UTF-8", log: Logger = logger, mapper: Iterator[String] => T): T =
        mapStream(getStream(res), enc, log, mapper)

    /**
      * Reads lines from given file.
      *
      * @param path Zipped file path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readGzipPath(path: String, enc: String = "UTF-8", log: Logger = logger): List[String] =
        readGzipFile(new File(path), enc, log)

    /**
      * Reads lines from given file.
      *
      * @param f File to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readFile(f: File, enc: String = "UTF-8", log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromFile(f, enc)) { src =>
                getAndLog(src.getLines().map(p => p).toList, f, log)
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to read file: ${f.getAbsolutePath}", e)
        }

    /**
      * Reads lines from given stream.
      *
      * @param in Stream to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readStream(in: InputStream, enc: String = "UTF-8", log: Logger = logger): List[String] =
        mapStream(in, enc, log, _.map(p => p).toList)

    /**
      * Maps lines from the given stream to an object.
      *
      * @param in Stream to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      * @param mapper Function to read lines.
      */
    @throws[NCE]
    def mapStream[T](in: InputStream, enc: String, log: Logger = logger, mapper: Iterator[String] => T): T =
        try {
            Using.resource(Source.fromInputStream(in, enc)) { src =>
                mapper(src.getLines())
            }
        }
        catch {
            case e: IOException => throw new NCE(s"Failed to read stream.", e)
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
    def readTextFile(f: File, enc: String, log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromFile(f, enc)) { src =>
                getAndLog(
                    readLcTrimFilter(src),
                    f,
                    logger
                )
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to read text file: ${f.getAbsolutePath}", e)
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
    def readTextGzipFile(f: File, enc: String, log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromInputStream(new GIS(new FileInputStream(f)), enc)) { src =>
                getAndLog(
                    readLcTrimFilter(src),
                    f,
                    log
                )
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to read text GZIP file: ${f.getAbsolutePath}", e)
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
    def readTextStream(in: InputStream, enc: String, log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromInputStream(in, enc)) { src =>
                readLcTrimFilter(src)
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to read stream.", e)
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
    def readTextGzipResource(res: String, enc: String, log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromInputStream(new GIS(getStream(res)), enc)) { src =>
                readLcTrimFilter(src)
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to read stream.", e)
        }

    /**
     *
     * @param in
     * @return
     */
    private def readLcTrimFilter(in: BufferedSource): List[String] =
        in.getLines().map(_.toLowerCase.strip).filter(s => s.nonEmpty && !s.startsWith("#")).toList

    /**
      * Reads lines from given file converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param path File path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    @throws[NCE]
    def readTextPath(path: String, enc: String, log: Logger = logger): List[String] =
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
    def readTextResource(res: String, enc: String, log: Logger = logger): List[String] =
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
    def readTextGzipPath(path: String, enc: String, log: Logger = logger): List[String] =
        readTextGzipFile(new File(path), enc, log)
    
    /**
      *
      * @param s
      * @return
      */
    def decapitalize(s: String): String =
        s.head.toLower + s.tail

    /**
      * Converts given name into properly capitalized first and last name.
      *
      * @param name Full name.
      */
    def toFirstLastName(name: String): (String, String) = {
        val parts = name.strip.split(' ')

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
        name.strip.toLowerCase.capitalize
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
    def mkDailyTimer(name: String, body: Unit => Unit, hour: Int, mins: Int = 0, secs: Int = 0): Timer = {
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
                        case e: Throwable => prettyError(logger, s"Error executing daily '$name' timer:", e)
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
    def readGzipFile(f: File, enc: String, log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromInputStream(new GIS(new FileInputStream(f)), enc)) { src =>
                getAndLog(src.getLines().map(p => p).toList, f, log)
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to read GZIP file: ${f.getAbsolutePath}", e)
        }

    /**
      *
      * @param in Zipped stream to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      * @return
      */
    @throws[NCE]
    def readGzipResource(in: InputStream, enc: String, log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromInputStream(new GIS(in), enc)) { src =>
                src.getLines().map(p => p).toList
            }
        catch {
            case e: IOException => throw new NCE(s"Failed to read stream", e)
        }

    /**
      * Reads bytes from given file.
      *
      * @param path File path.
      * @param log Logger.
      */
    @throws[NCE]
    def readPathBytes(path: String, log: Logger = logger): Array[Byte] = readFileBytes(new File(path), log)

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

            Using.resource(new FileInputStream(f)) { in =>
                in.read(arr)
            }

            getAndLog(arr, f, log)
        }
        catch {
            case e: IOException => throw new NCE(s"Error reading file: $f", e)
        }
    }

    /**
      *
      * @param data
      * @param f
      * @param log
      * @tparam T
      * @return
      */
    private def getAndLog[T](data: T, f: File, log: Logger = logger): T = {
        log.trace(s"Loaded file: ${f.getAbsolutePath}")

        data
    }

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
            Using.resource(new GOS(new FileOutputStream(gz))) { stream =>
                stream.write(readFileBytes(f))

                stream.flush()
            }
        catch {
            case e: IOException => throw new NCE(s"Error gzip file: $f", e)
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
    def mkTextFile(path: String, lines: Iterable[Any], sort: Boolean = true): Unit = {
        val file = new File(path)

        Using.resource(new PrintStream(file)) {
            ps =>
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
      * Serializes data.
      *
      * @param obj Data.
      */
    @throws[NCE]
    def serialize(obj: Any): Array[Byte] = {
        try {
            Using.resource(new ByteArrayOutputStream()) { baos =>
                Using.resource(objOutput(baos)) { out =>
                    out.writeObject(obj)
                }

                baos.toByteArray
            }
        }
        catch {
            case e: IOException => throw new NCE(s"Error serializing data: $obj", e)
        }
    }

    /**
     * Serializes data from file.
     *
     * @param path File path.
     */
    @throws[NCE]
    def serializePath(path: String, obj: Any): Unit =
        try {
            Using.resource(objOutput(new FileOutputStream(path))) { out =>
                out.writeObject(obj)
            }

            logger.info(s"File $path is written.")
        }
        catch {
            case e: IOException => throw new NCE(s"Error writing file: $path", e)
        }

    /**
      * Serializes data from file.
      *
      * @param file File.
      */
    @throws[NCE]
    def serialize(file: File, obj: Any): Unit = serializePath(file.getAbsolutePath, obj)

    /**
      * Deserializes data from file.
      *
      * @param path File path.
      */
    @throws[NCE]
    def deserializePath[T](path: String, log: Logger = logger): T =
        try {
            val res = Using.resource(objInput(new FileInputStream(path))) { in =>
                in.readObject().asInstanceOf[T]
            }

            log.trace(s"Read file: $path")

            res
        }
        catch {
            case e: IOException => throw new NCE(s"Error reading path: $path", e)
        }

    /**
      * Deserializes data.
      *
      * @param arr File path.
      */
    @throws[NCE]
    def deserialize[T](arr: Array[Byte]): T =
        try
            Using.resource(objInput(new ByteArrayInputStream(arr))) { in =>
                in.readObject().asInstanceOf[T]
            }
        catch {
            case e: IOException => throw new NCE(s"Error deserialization data", e)
        }

    /**
      * Deserializes data from file.
      *
      * @param file File.
      * @param log Logger.
      */
    @throws[NCE]
    def deserialize[T](file: File, log: Logger = logger): T = deserializePath(file.getAbsolutePath, log)

    /**
     *
     * @param in
     */
    private def objInput(in: InputStream): ObjectInputStream =
        new ObjectInputStream(new BufferedInputStream(in))

    /**
     *
     * @param out
     */
    private def objOutput(out: OutputStream): ObjectOutputStream =
        new ObjectOutputStream(new BufferedOutputStream(out))

    /**
      * Wrap string value.
      *
      * @param s String value.
      */
    def wrapQuotes(s: String): String = s""""$s""""

    /**
      * Recursively removes all files and nested directories in a given folder.
      *
      * @param rootDir Folder to remove all nested files and directories in it.
      * @param delFolder Flag, deleted or not root folder itself.
      */
    @throws[NCE]
    def clearFolder(rootDir: String, delFolder: Boolean = false): Unit = {
        val rootPath = Paths.get(rootDir)

        try
            Files.walkFileTree(rootPath, new SimpleFileVisitor[Path] {
                private def delete(path: Path): FileVisitResult = {
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
        catch {
            case e: IOException => throw new NCE(s"Couldn't clear folder: '$rootDir'", e)
        }

        if (delFolder && !new File(rootDir).delete())
            throw new NCE(s"Couldn't delete folder: '$rootDir'")
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
        body: Unit => T,
        onFailure: Throwable => Unit = _ => Unit,
        onSuccess: T => Unit = (_: T) => ())(implicit ec: ExecutionContext): Future[T] = {
        val fut = Future {
            body(())
        }(ec)

        fut.onComplete {
            case Success(ok) => onSuccess(ok)
            case Failure(err) => onFailure(err)
        }(ec)

        fut
    }

    /**
      * Makes thread.
      *
      * @param name Name.
      * @param body Thread body.
      */
    def mkThread(name: String)(body: Thread => Unit): Thread =
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
                    case _: InterruptedException => logger.trace(s"Thread interrupted: $name")
                    case e: Throwable => prettyError(logger, s"Unexpected error during '$name' thread execution:", e)
                }
                finally
                    stopped = true
            }
        }

    /**
     * Makes thread.
     *
     * @param name Name.
     * @param body Thread body.
     */
    def mkThread(name: String, body: Runnable): Thread =
        mkThread(name) { _ => body.run() }

    /**
      * System-wide process of normalizing emails (trim & lower case).
      *
      * @param email Email to normalize.
      */
    def normalizeEmail(email: String): String = email.strip.toLowerCase

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
     * Tests whether given system property of environment variable is set or not.
     *
     * @param s @param s Name of the system property or environment variable.
     * @return
     */
    def isSysEnvSet(s: String): Boolean =
        sysProps.get(s).nonEmpty || sys.env.contains(s)

    /**
      * Returns `true` if given system property, or environment variable is provided and has value
      * 'true'. In all other cases returns `false`.
      *
      * @param s Name of the system property or environment variable.
      */
    def isSysEnvTrue(s: String): Boolean =
        sysEnv(s) match {
            case None => false
            case Some(v) => java.lang.Boolean.valueOf(v) == java.lang.Boolean.TRUE
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

        (0 until n).foreach(_ => dest += src.remove(RND.nextInt(src.size)))

        dest.toSeq
    }

    /**
      * Gets system property, or environment variable (in that order), or throws exception if none exists.
      *
      * @param s Name of the system property or environment variable.
      */
    @throws[NCE]
    def mandatorySysEnv(s: String): String =
        sysEnv(s) match {
            case Some(v) => v
            case None => throw new NCE(s"Cannot find environment variable or system property: $s")
        }

    /**
      * Compresses given string.
      *
      * @param rawStr String to compress.
      * @return Compressed Base64-encoded string.
      */
    @throws[NCE]
    def compress(rawStr: String): String = {
        val arr = new ByteArrayOutputStream(1024)

        try {
            Using.resource(new GOS(arr)) { zip =>
                zip.write(rawStr.getBytes)
            }

            Base64.encodeBase64String(arr.toByteArray)
        }
        catch {
            case e: Exception => throw new NCE("Error during data compression.", e)
        }
    }

    /**
      * Decompresses given Base64-encoded previously compressed string.
      *
      * @param zipStr Compressed string.
      * @return Uncompressed string.
      */
    @throws[NCE]
    def uncompress(zipStr: String): String =
        try
            IOUtils.toString(new GIS(new ByteArrayInputStream(Base64.decodeBase64(zipStr))), Charset.defaultCharset())
        catch {
            case e: Exception => throw new NCE("Error during data decompression.", e)
        }

    /**
      * Sleeps number of milliseconds properly handling exceptions.
      *
      * @param delay Number of milliseconds to sleep.
      */
    def sleep(delay: Long): Unit =
        try
            Thread.sleep(delay)
        catch {
            case _: InterruptedException => Thread.currentThread().interrupt()
            case e: Throwable => prettyError(logger, "Unhandled exception caught during sleep:", e)
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
                case _: InterruptedException => logger.trace("Thread joining was interrupted (ignoring).")
            }
        }

    /**
     * Interrupts thread.
     *
     * @param t Thread.
     */
    def interruptThread(t: Thread): Unit =
        if (t != null)
            t.interrupt()

    /**
      * Shuts down executor service and waits for its finish.
      *
      * @param es Executor service.
      */
    def shutdownPool(es: ExecutorService): Unit =
        if (es != null) {
            es.shutdown()

            try
                es.awaitTermination(Long.MaxValue, TimeUnit.MILLISECONDS)
            catch {
                case _: InterruptedException => () // Safely ignore.
            }
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
    def neon(s: String): Boolean = s != null && s.nonEmpty

    /**
      * Generates (a relatively) globally unique ID good for a short-term usage.
      */
    def genGuid(): String = idGen.encrypt(System.currentTimeMillis(), System.nanoTime()).
        toUpperCase.grouped(4).filter(_.length() == 4).mkString("-")

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
      * @param list Collection. Note, it should be list.
      * @param seen Checked elements.
      * @see #getDups
      */
    @annotation.tailrec
    def containsDups[T](list: List[T], seen: Set[T] = Set.empty[T]): Boolean =
        list match {
            case x :: xs => if (seen.contains(x)) true else containsDups(xs, seen + x)
            case _ => false
        }

    /**
      * Gets set of duplicate values from given sequence (potentially empty).
      *
      * @param seq Sequence to check for dups from.
      * @tparam T
      * @return
      * @see #containsDups
      */
    def getDups[T](seq: Seq[T]): Set[T] = seq.diff(seq.distinct).toSet

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
     *
     * @param url
     */
    def getUrlDocument(url: String): Option[Document] =
        Option(scala.util.Try { Jsoup.connect(url).get() } getOrElse null)

    /**
     * Records anonymous GA screen view event. Ignores any errors.
     *
     * @param cd Content description for GA measurement protocol.
     */
    def gaScreenView(cd: String): Unit = {
        if (!isSysEnvSet(DISABLE_GA_PROP)) {
            logger.debug(s"To disable anonymous Google Analytics access set '${c(DISABLE_GA_PROP)}' system property.")

            try {
                val anonym = NetworkInterface.getByInetAddress(InetAddress.getLocalHost) match {
                    case null => 555
                    case nif =>
                        val addr = nif.getHardwareAddress

                        if (addr == null)
                            555
                        else
                            addr.mkString(",").hashCode
                }

                HttpClient.newHttpClient.send(
                    HttpRequest.newBuilder()
                        .uri(
                            URI.create("http://www.google-analytics.com/collect")
                        )
                        .POST(
                            HttpRequest.BodyPublishers.ofString(
                            s"v=1&" +
                                s"t=screenview&" +
                                s"tid=UA-180663034-1&" + // 'nlpcraft.apache.org' web property.
                                s"cid=$anonym&" + // Hide any user information (anonymous user).
                                s"aip=&" + // Hide user IP (anonymization).
                                s"an=nlpcraft&" +
                                s"av=${NCVersion.getCurrent.version}&" +
                                s"aid=org.apache.nlpcraft&" +
                                s"cd=$cd"
                            )
                        )
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                )
            }
            catch {
                case _: Exception => () // Ignore.
            }
        }
    }

    /**
      * Formats given double number with provided precision.
      *
      * @param num Number to format.
      * @param precision Number of digits after decimal point.
      */
    def format(num: Double, precision: Int): String = precision match {
        case 0 => DEC_FMT0.format(num)
        case 1 => DEC_FMT1.format(num)
        case _ => DEC_FMT2.format(num)
    }

    /**
     *
     * @param logger
     * @param title
     * @param e
     */
    def prettyError(logger: Logger, title: String, e: Throwable): Unit = {
        // Keep the full trace in the 'trace' log level.
        logger.trace(title, e)

        prettyErrorImpl(new PrettyErrorLogger {
            override def log(s: String): Unit = logger.error(s)
        }, title, e)
    }

    /**
     *
     * @param title
     * @param e
     */
    def prettyError(title: String, e: Throwable): Unit =
        prettyErrorImpl(new PrettyErrorLogger(), title, e)

    sealed class PrettyErrorLogger {
        def log(s: String): Unit = System.err.println(s)
    }

    /**
     *
     * @param logger
     * @param title
     * @param e
     */
    private def prettyErrorImpl(logger: PrettyErrorLogger, title: String, e: Throwable): Unit = {
        logger.log(title)

        val INDENT = 2

        var x = e
        var indent = INDENT

        while (x != null) {
            var first = true

            var errMsg = x.getLocalizedMessage

            if (errMsg == null)
                errMsg = "<null>"

            val exClsName = if (!x.isInstanceOf[NCE]) s"$ansiRedFg[${x.getClass.getCanonicalName}]$ansiReset " else ""

            val trace = x.getStackTrace.find(!_.getClassName.startsWith("scala.")).getOrElse(x.getStackTrace.head)

            val fileName = trace.getFileName
            val lineNum = trace.getLineNumber

            val msg =
                if (fileName == null || lineNum < 0)
                    s"$exClsName$errMsg"
                else
                    s"$exClsName$errMsg $ansiCyanFg->$ansiReset ($fileName:$lineNum)"

            msg.split("\n").foreach(line => {
                val s = s"${" " * indent}${if (first) ansiBlue("+-+ ") else "   "}${bo(y(line))}"

                logger.log(s)

                first = false
            })

            val traces = x.getStackTrace.filter {t =>
                val mtdName = t.getMethodName
                val clsName = t.getClassName

                // Clean up trace.
                clsName.startsWith("org.apache.nlpcraft") &&
                !clsName.startsWith("org.apache.nlpcraft.common.opencensus") &&
                !mtdName.contains("startScopedSpan") &&
                !mtdName.contains('$')
            }

            for (trace <- traces) {
                val fileName = trace.getFileName
                val lineNum = trace.getLineNumber
                val mtdName = trace.getMethodName
                val clsName = trace.getClassName.replace("org.apache.nlpcraft", "o.a.n")

                logger.log(s"${" " * indent}  ${b("|")} $clsName.$mtdName $ansiCyanFg->$ansiReset ($fileName:$lineNum)")
            }

            indent += INDENT

            x = x.getCause
        }
    }

    /**
     * Prints ASCII-logo.
     */
     def asciiLogo(): String =
        raw"$ansiBlueFg    _   ____     $ansiCyanFg ______           ______   $ansiReset$NL" +
        raw"$ansiBlueFg   / | / / /___  $ansiCyanFg/ ____/________ _/ __/ /_  $ansiReset$NL" +
        raw"$ansiBlueFg  /  |/ / / __ \$ansiCyanFg/ /   / ___/ __ `/ /_/ __/  $ansiReset$NL" +
        raw"$ansiBlueFg / /|  / / /_/ /$ansiCyanFg /___/ /  / /_/ / __/ /_    $ansiReset$NL" +
        raw"$ansiBold$ansiRedFg/_/ |_/_/ .___/$ansiRedFg\____/_/   \__,_/_/  \__/      $ansiReset$NL" +
        raw"$ansiBold$ansiRedFg       /_/                                              $ansiReset$NL"

    /**
     *
     * @param s
     * @return
     */
    def fgRainbow(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_FG_COLORS, addOn)

    /**
     *
     * @param s
     * @return
     */
    def bgRainbow(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_BG_COLORS, addOn)

    /**
     *
     * @param s
     * @return
     */
    def rainbow(s: String, addOn: String = ""): String = randomRainbowImpl(s, ANSI_COLORS, addOn)

    /**
     *
     * @param s
     * @param colors
     * @param addOn
     * @return
     */
    private def randomRainbowImpl(s: String, colors: Seq[String], addOn: String): String =
        s.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            buf ++= s"${colors(RND.nextInt(colors.size))}$addOn${zip._1}"
        })
        .toString + ansiReset

    /**
     *
     * @param s
     * @param colors
     * @param addOn
     * @return
     */
    private def rainbowImpl(s: String, colors: Seq[String], addOn: String): String =
        s.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            buf ++= s"${colors(zip._2 % colors.size)}$addOn${zip._1}"
        })
        .toString + ansiReset

    /**
     * ANSI color JSON string.
     *
     * @param json JSON string to color.
     * @return
     */
    def colorJson(json: String): String = {
        val buf = new StringBuilder

        var inQuotes = false

        for (ch <- json) {
            ch match {
                case ':' if !inQuotes => buf ++= r(":")
                case '[' | ']' | '{' | '}' if !inQuotes => buf ++= y(s"$ch")
                case ',' if !inQuotes => buf ++= g(s"$ch")
                case '"' =>
                    if (inQuotes)
                        buf ++= b(s"$ch")
                    else
                        buf ++= s"$ansiBlueFg$ch$ansiCyanFg"

                    inQuotes = !inQuotes

                case _ => buf ++= s"$ch"
            }
        }

        buf.append(RST)

        buf.toString()
    }

    /**
     *
     * @param json
     * @return
     */
    def prettyJson(json: String): String = {
        if (json == null || json.isEmpty)
            ""
        else
            try
                GSON.toJson(GSON.getAdapter(classOf[JsonElement]).fromJson(json))
            catch {
                case _: Exception => ""
            }
    }

    /**
     *
     * @param json
     * @return
     */
    def isValidJson(json: String): Boolean =
        scala.util.Try(GSON.getAdapter(classOf[JsonElement]).fromJson(json)).isSuccess

    /**
     *
     * @param json
     * @param field
     * @return
     */
    @throws[Exception]
    def getJsonStringField(json: String, field: String): String =
        GSON.getAdapter(classOf[JsonElement]).fromJson(json).getAsJsonObject.get(field).getAsString

    /**
     *
     * @param json
     * @param field
     * @return
     */
    @throws[Exception]
    def getJsonIntField(json: String, field: String): Int =
        GSON.getAdapter(classOf[JsonElement]).fromJson(json).getAsJsonObject.get(field).getAsInt

    /**
     *
     * @param json
     * @tparam T
     * @return
     */
    def jsonToObject[T](json: String, typ: Type): T =
        GSON.fromJson(json, typ)

    /**
     *
     * @param json
     * @tparam T
     * @return
     */
    def jsonToObject[T](json: String, cls: Class[T]): T =
        GSON.fromJson(json, cls)

    /**
     * Shortcut to convert given JSON to Scala map with default mapping.
     *
     * @param json JSON to convert.
     * @return
     */
    @throws[Exception]
    def jsonToScalaMap(json: String): Map[String, Object] =
        GSON.fromJson(json, classOf[java.util.HashMap[String, Object]]).toMap

    /**
     * Shortcut to convert given JSON to Java map with default mapping.
     *
     * @param json JSON to convert.
     * @return
     */
    @throws[NCE]
    def jsonToJavaMap(json: String): java.util.Map[String, Object] = {
        try
            GSON.fromJson(json, classOf[java.util.HashMap[String, Object]])
        catch {
            case e: Exception => throw new NCE(s"Cannot deserialize JSON to map: '$json'", e)
        }
    }

    /**
     *
     * @param json
     * @param field
     * @return
     */
    @throws[NCE]
    def getJsonBooleanField(json: String, field: String): Boolean =
        try
            GSON.getAdapter(classOf[JsonElement]).fromJson(json).getAsJsonObject.get(field).getAsBoolean
        catch {
            case e: Exception => throw new NCE(s"Cannot deserialize JSON to map: '$json'", e)
        }

    /**
     *
     * @param namePrefix
     * @param threadNum
     * @return
     */
    def mkThreadPool(namePrefix: String, threadNum: Int = Runtime.getRuntime.availableProcessors() * 8): ThreadPoolExecutor =
        new ThreadPoolExecutor(
            1,
            threadNum,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue[Runnable],
            new ThreadFactory {
                val thNum = new AtomicInteger(1)

                override def newThread(r: Runnable): Thread =
                    new Thread(r, s"pool-$namePrefix-thread-${thNum.getAndIncrement()}")
            },
            new RejectedExecutionHandler() {
                // Ignore rejections.
                override def rejectedExecution(r: Runnable, exec: ThreadPoolExecutor): Unit = ()
            }
        )

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

        Using.resource(new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) { in =>
            var entry = in.getNextEntry

            while (entry != null) {
                val f = new File(outDir, entry.getName)

                if (!entry.isDirectory) {
                    mkDir(f.getParentFile)

                    try {
                        if (!f.createNewFile())
                            throw new NCE(s"File cannot be created: ${f.getAbsolutePath}")

                        Using.resource(new BufferedOutputStream(new FileOutputStream(f))) { out =>
                            IOUtils.copy(in, out)
                        }
                    }
                    catch {
                        case e: IOException => throw new NCE(s"IO error processing file: ${f.getAbsolutePath}.", e)
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
     * Makes standard Java hashcode.
     *
     * @param items Items to construct the hashcode from.
     * @return Hashcode.
     */
    def mkJavaHash(items: Any*): Int = Seq(items: _*).map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)

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
            case e: IOException => throw new NCE(s"Error reading properties: $s", e)
        }

        p
    }

    /**
      * Converts error with its trace to string.
      *
      * @param t Error.
      */
    def toString(t: Throwable): String =
        Using.resource(new ByteArrayOutputStream()) { out =>
            Using.resource(new PrintStream(out)) { ps =>
                t.printStackTrace(ps)

                new String(out.toByteArray, "UTF-8")
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
            case "file" =>
                Using.resource(new InputStreamReader(getStream(resDir))) { reader =>
                    Using.resource(new BufferedReader(reader)) { bReader =>
                        bReader.lines().collect(Collectors.toList[String]).asScala.map(p => s"$resDir/$p")
                    }
                }
                .toSeq

            case "jar" =>
                val jar = new JarFile(URLDecoder.decode(url.getPath.substring(5, url.getPath.indexOf("!")), "UTF-8"))
                val entries = jar.entries

                val res = mutable.ArrayBuffer.empty[String]

                while (entries.hasMoreElements) {
                    val name = entries.nextElement.getName

                    if (name.startsWith(resDir) && name != s"$resDir/")
                        res += name
                }

                res.toSeq

            case _ => throw new NCE(s"Cannot list files for: $resDir")
        }
    }

    /**
      * Gets external IP.
      */
    @throws[IOException]
    def getExternalIp: String =
        Using.resource(new URL("http://checkip.amazonaws.com").openStream()) { is =>
            Using.resource(new InputStreamReader(is)) { reader =>
                Using.resource(new BufferedReader(reader)) { bufReader =>
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
    def jsonToObject(js: String): AnyRef =
        try
            GSON.fromJson(js, classOf[Object])
        catch {
            case e: Exception => throw new NCE(s"Failed to convert JSON string to map: $js", e)
        }

    /**
      *
      * @param bodies
      * @param ec
      */
    def executeParallel(bodies: (() => Any)*)(implicit ec: ExecutionContext): Unit = {
        bodies.map(body => {
            Future {
                body()
            }(ec)
        }).foreach(Await.result(_, Duration.Inf))
    }
    
    /**
      * Calling a method with one parameter and a non-null return value.
     *
      * @param objFac
      * @param mtdName
      * @param arg
      * @tparam T
      * @return
      */
    @throws[NCE]
    def callMethod[T: ClassTag, R: ClassTag](objFac: () => Any, mtdName: String, arg: T): R =
        try {
            val obj: Any = objFac()

            if (obj == null)
                throw new NCE(s"Invalid 'null' object created attempting to call method: $mtdName")

            val argCls = classTag[T].runtimeClass
            val retCls = classTag[R].runtimeClass

            def mkErrors = s"[" +
                s"name=${obj.getClass.getName}#$mtdName(...), " +
                s"argType=${argCls.getCanonicalName}, " +
                s"retType=${retCls.getCanonicalName}" +
            s"]"

            val mtd =
                try
                    obj.getClass.getMethod(mtdName, argCls)
                catch {
                    case e: NoSuchMethodException => throw new NCE(s"Method not found $mkErrors", e)
                }

            var flag = mtd.canAccess(obj)

            try {
                if (!flag) {
                    mtd.setAccessible(true)

                    flag = true
                }
                else
                    flag = false

                val res =
                    try
                        mtd.invoke(obj, arg.asInstanceOf[Object])
                    catch {
                        case e: Throwable => throw new NCE(s"Failed to execute method $mkErrors", e)
                    }

                if (res == null)
                    throw new NCE(s"Unexpected 'null' result for method execution $mkErrors")

                try
                    res.asInstanceOf[R]
                catch {
                    case e: ClassCastException => throw new NCE(s"Invalid method result type $mkErrors", e)
                }
            }
            finally {
                if (flag)
                    mtd.setAccessible(false)
            }
        }
        catch {
            case e: NCE => throw e
            case e: Throwable => throw new NCE(s"Unexpected error calling method: $mtdName(...)", e)
        }

    /**
      *
      * @param clsName Fully qualified class name to create object of.
      * @tparam T Type of the object to create.
      * @return New instance of the specified type.
      */
    @throws[NCE]
    def mkObject[T](clsName: String): T = {
        try
            // Try Java reflection first.
            Class.forName(clsName).getDeclaredConstructor().newInstance().asInstanceOf[T]
        catch {
            case _: Throwable =>
                // Try Scala reflection second.
                val mirror = runtimeMirror(getClass.getClassLoader)

                try
                    mirror.reflectModule(mirror.staticModule(clsName)).instance.asInstanceOf[T]
                catch {
                    case e: Throwable => throw new NCE(s"Error initializing object of type: $clsName", e)
                }
        }
    }

    /**
      * Gets simple class name of the caller removing '$' for Scala classes.
      *
      * @param clazz Class object.
      * @param simpleName Simple name flag.
      * @return Simple class name.
      */
    def cleanClassName(clazz: Class[_], simpleName: Boolean = true): String = {
        val cls = if (simpleName) clazz.getSimpleName else clazz.getName

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
        idx.zipWithIndex.tail.map { case (v, i) => Math.abs(v - idx(i - 1)) }.sum - idx.length + 1

    /**
      * Extracts type `T` from given YAML `file`.
      *
      * @param f File to extract from.
      * @param ignoreCase Flag.
      * @tparam T Type of the object to extract.
      */
    @throws[NCE]
    def extractYamlFile[T](f: File, ignoreCase: Boolean, tr: TypeReference[T]): T =
        extractYamlString(readFile(f).mkString("\n"), f.getAbsolutePath, ignoreCase, tr)

    /**
      * Extracts type `T` from given YAML `resource`.
      *
      * @param res Resource to extract from.
      * @param ignoreCase Flag.
      * @tparam T Type of the object to extract.
      */
    @throws[NCE]
    def extractYamlResource[T](res: String, ignoreCase: Boolean, tr: TypeReference[T]): T =
        extractYamlString(readStream(getStream(res)).mkString("\n"), res, ignoreCase, tr)

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
            case e: IOException => throw new NCE(s"Failed to read: $res", e)
            case e: Throwable => throw new NCE(s"Failed to parse: $res", e)
        }

    /**
      *
      * @return
      */
    def getYamlMapper: ObjectMapper = YAML

    /**
      *
      * @param list
      * @tparam T
      * @return
      */
    def permute[T](list: List[List[T]]): List[List[T]] =
        list match {
            case Nil => List(Nil)
            case head :: tail => for (h <- head; t <- permute(tail)) yield h :: t
        }

    /**
      *
      * @param idxs
      * @return
      */
    def isContinuous(idxs: Seq[Int]): Boolean = {
        require(idxs.nonEmpty)

        idxs.size match {
            case 0 => throw new AssertionError()
            case 1 => true
            case _ =>
                val list = idxs.view

                list.zip(list.tail).forall { case (x, y) => x + 1 == y }
        }
    }

    /**
      *
      * @param idxs
      * @return
      */
    def isIncreased(idxs: Seq[Int]): Boolean = {
        require(idxs.nonEmpty)

        idxs.size match {
            case 0 => throw new AssertionError()
            case 1 => true
            case _ =>
                val list = idxs.view

                !list.zip(list.tail).exists { case (x, y) => x > y }
        }
    }

    /**
      *
      * @param s
      */
    def isSuitableConfig(s: String): Boolean = {
        def isFile: Boolean = {
            val f = new File(s)

            f.exists() && f.isFile
        }

        def isResource: Boolean = getClass.getClassLoader.getResource(s) != null

        def isUrl: Boolean =
            try {
                new URL(s)

                true
            }
            catch {
                case _: MalformedURLException => false
            }

        isFile || isResource || isUrl
    }
}