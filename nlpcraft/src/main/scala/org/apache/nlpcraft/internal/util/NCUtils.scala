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

package org.apache.nlpcraft.internal.util

import com.google.gson.GsonBuilder
import com.typesafe.scalalogging.*
import org.apache.nlpcraft.NCToken
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.ansi.NCAnsi.*

import java.io.*
import java.net.*
import java.util.{Random, UUID}
import java.util.regex.Pattern
import java.util.zip.*
import scala.annotation.tailrec
import scala.collection.{IndexedSeq, Seq}
import scala.concurrent.duration.Duration
import scala.concurrent.*
import scala.io.Source
import scala.sys.SystemProperties
import scala.util.Using
import scala.util.control.Exception.ignoring
import scala.io.BufferedSource

/**
  * 
  */
object NCUtils extends LazyLogging:
    final val NL = System getProperty "line.separator"
    private val RND = new Random()
    private val sysProps = new SystemProperties
    private final lazy val GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private final val ANSI_SEQ = Pattern.compile("\u001B\\[[?;\\d]*[a-zA-Z]")
    private val ANSI_FG_8BIT_COLORS = for (i <- 16 to 255) yield ansi256Fg(i)
    private val ANSI_BG_8BIT_COLORS = for (i <- 16 to 255) yield ansi256Bg(i)
    private val ANSI_FG_4BIT_COLORS = Seq(
        ansiRedFg,
        ansiGreenFg,
        ansiBlueFg,
        ansiYellowFg,
        ansiWhiteFg,
        ansiBlackFg,
        ansiCyanFg
    )
    private val ANSI_BG_4BIT_COLORS = Seq(
        ansiRedBg,
        ansiGreenBg,
        ansiBlueBg,
        ansiYellowBg,
        ansiWhiteBg,
        ansiBlackBg,
        ansiCyanBg
    )
    private val ANSI_4BIT_COLORS = for (fg <- ANSI_FG_4BIT_COLORS; bg <- ANSI_BG_4BIT_COLORS) yield s"$fg$bg"

    /**
      * Gets system property, or environment variable (in that order), or `None` if none exists.
      *
      * @param s Name of the system property or environment variable.
      */
    def sysEnv(s: String): Option[String] = sysProps.get(s).orElse(sys.env.get(s))

    /**
      * Tests whether given system property of environment variable is set or not.
      *
      * @param s @param s Name of the system property or environment variable.
      * @return
      */
    def isSysEnvSet(s: String): Boolean = sysProps.get(s).nonEmpty || sys.env.contains(s)

    /**
      * Creates object from JSON string.
      *
      * @param js JSON string.
      */
    def jsonToObject(js: String): AnyRef =
        try
            GSON.fromJson(js, classOf[Object])
        catch
            case e: Exception => throw new NCException(s"Failed to convert JSON string to map: $js", e)

    /**
      * Returns `true` if given system property, or environment variable is provided and has value
      * 'true'. In all other cases returns `false`.
      *
      * @param s Name of the system property or environment variable.
      */
    def isSysEnvTrue(s: String): Boolean = sysEnv(s) match
        case None => false
        case Some(v) => java.lang.Boolean.valueOf(v) == java.lang.Boolean.TRUE

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
    def getRandomSeq[T](seq: Seq[T], n: Int): Seq[T] =
        require(seq.lengthCompare(n) >= 0)

        val src = scala.collection.mutable.ArrayBuffer.empty[T] ++ seq
        val dest = scala.collection.mutable.ArrayBuffer.empty[T]

        (0 until n).foreach(_ => dest += src.remove(RND.nextInt(src.size)))

        dest.toSeq

    /**
      * Prints 4-bit ASCII-logo.
      */
    def asciiLogo4Bit(): String =
        raw"$ansiBlueFg    _   ____     $ansiCyanFg ______           ______   $ansiReset$NL" +
        raw"$ansiBlueFg   / | / / /___  $ansiCyanFg/ ____/________ _/ __/ /_  $ansiReset$NL" +
        raw"$ansiBlueFg  /  |/ / / __ \$ansiCyanFg/ /   / ___/ __ `/ /_/ __/  $ansiReset$NL" +
        raw"$ansiBlueFg / /|  / / /_/ /$ansiCyanFg /___/ /  / /_/ / __/ /_    $ansiReset$NL" +
        raw"$ansiBold$ansiRedFg/_/ |_/_/ .___/$ansiRedFg\____/_/   \__,_/_/  \__/      $ansiReset$NL" +
        raw"$ansiBold$ansiRedFg       /_/                                              $ansiReset$NL"

    /**
      * Prints 8-bit ASCII-logo.
      */
    def asciiLogo8Bit1(): String =
        fgRainbow4Bit(
            raw"${ansi256Fg(28)}    _   ____      ______           ______   $ansiReset$NL" +
            raw"${ansi256Fg(64)}   / | / / /___  / ____/________ _/ __/ /_  $ansiReset$NL" +
            raw"${ansi256Fg(100)}  /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/  $ansiReset$NL" +
            raw"${ansi256Fg(136)} / /|  / / /_/ / /___/ /  / /_/ / __/ /_    $ansiReset$NL" +
            raw"${ansi256Fg(172)}/_/ |_/_/ .___/\____/_/   \__,_/_/  \__/    $ansiReset$NL" +
            raw"${ansi256Fg(208)}       /_/                                  $ansiReset$NL"
        )

    /**
      * Prints 8-bit ASCII-logo.
      */
    def asciiLogo8Bit(): String =
        val startColor = getRandom(Seq(16, 22, 28, 34, 40, 46))
        val range = 6

        (for (lineIdx <- Seq(
            raw"    _   ____      ______           ______   $NL",
            raw"   / | / / /___  / ____/________ _/ __/ /_  $NL",
            raw"  /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/  $NL",
            raw" / /|  / / /_/ / /___/ /  / /_/ / __/ /_    $NL",
            raw"/_/ |_/_/ .___/\____/_/   \__,_/_/  \__/    $NL",
            raw"       /_/                                  $NL"
        ).zipWithIndex) yield {
            val line = lineIdx._1
            val idx = lineIdx._2
            val start = startColor + (36 * idx)
            val end = start + range - 1

            gradAnsi8BitFgLine(line, start, end)
        })
        .mkString("")

    /**
      *
      * @param line
      * @param startColor Inclusive.
      * @param endColor Inclusive.
      * @return
      */
    def gradAnsi8BitFgLine(line: String, startColor: Int, endColor: Int): String =
        line.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            val ch = zip._1
            val idx = zip._2
            val color = startColor + idx % (endColor - startColor + 1)

            buf ++= s"${ansi256Fg(color)}$ch"
        })
        .toString + ansiReset

    /**
      *
      * @param line
      * @param startColor Inclusive.
      * @param endColor Inclusive.
      * @return
      */
    def gradAnsi8BitBgLine(line: String, startColor: Int, endColor: Int): String =
        line.zipWithIndex.foldLeft(new StringBuilder())((buf, zip) => {
            val ch = zip._1
            val idx = zip._2
            val color = startColor + idx % (endColor - startColor + 1)

            buf ++= s"${ansi256Bg(color)}$ch"
        })
        .toString + ansiReset

    /**
      *
      * @param s
      * @return
      */
    def fgRainbow4Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_FG_4BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def fgRainbow8Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_FG_8BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def bgRainbow4Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_BG_4BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def bgRainbow8Bit(s: String, addOn: String = ""): String = rainbowImpl(s, ANSI_BG_8BIT_COLORS, addOn)

    /**
      *
      * @param s
      * @return
      */
    def rainbow4Bit(s: String, addOn: String = ""): String = randomRainbowImpl(s, ANSI_4BIT_COLORS, addOn)

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
    def colorJson(json: String): String =
        val buf = new StringBuilder
        var inQuotes = false
        var isValue = false

        for (ch <- json)
            ch match
                case ':' if !inQuotes => buf ++= r(":"); isValue = true
                case '[' | ']' | '{' | '}' if !inQuotes => buf ++= y(s"$ch"); isValue = false
                case ',' if !inQuotes => buf ++= ansi256Fg(213, s"$ch"); isValue = false
                case '"' =>
                    if inQuotes then
                        buf ++= ansi256Fg(105, s"$ch")
                    else
                        buf ++= s"${ansi256Fg(105)}$ch"
                        buf ++= (if isValue then G else ansiCyanFg)

                    inQuotes = !inQuotes

                case _ => buf ++= s"$ch"


        buf.append(RST)
        buf.toString()

    /**
      * Shortcut - current timestamp in milliseconds.
      */
    def now(): Long = System.currentTimeMillis()

    /**
      *
      * @param v
      * @param dflt
      * @tparam T
      * @return
      */
    def notNull[T <: AnyRef](v: T, dflt: T): T = if v == null then dflt else v

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
        trimFilter(s.split(sep).toIndexedSeq)

    /**
      * Recursively removes quotes from given string.
      *
      * @param s
      * @return
      */
    @tailrec
    def trimQuotes(s: String): String =
        val z = s.strip
        if (z.startsWith("'") && z.endsWith("'")) || (z.startsWith("\"") && z.endsWith("\"")) then
            trimQuotes(z.substring(1, z.length - 1))
        else
            z

    /**
      * Recursively removes quotes and replaces escaped quotes from given string.
      *
      * @param s
      * @return
      */
    @tailrec
    def trimEscapesQuotes(s: String): String =
        val z = s.strip
        if z.nonEmpty then
            if z.head == '\'' && z.last == '\'' then
                trimEscapesQuotes(z.substring(1, z.length - 1).replace("\'", "'"))
            else if z.head == '"' && z.last == '"' then
                trimEscapesQuotes(z.substring(1, z.length - 1).replace("\\\"", "\""))
            else
                z
        else
            z

    /**
      * Recursively removes quotes and replaces escaped quotes from given string.
      *
      * @param s
      * @return
      */
    @tailrec
    def escapesQuotes(s: String): String =
        if s.nonEmpty then
            if s.head == '\'' && s.last == '\'' then
                escapesQuotes(s.substring(1, s.length - 1).replace("\'", "'"))
            else if (s.head == '"' && s.last == '"')
                escapesQuotes(s.substring(1, s.length - 1).replace("\\\"", "\""))
            else
                s
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
    def escapeJson(s: String): String =
        val len = s.length
        if len == 0 then
            ""
        else
            val sb = new StringBuilder
            for (ch <- s.toCharArray)
                ch match
                    case '\\' | '"' => sb += '\\' += ch
                    case '/' => sb += '\\' += ch
                    case '\b' => sb ++= "\\b"
                    case '\t' => sb ++= "\\t"
                    case '\n' => sb ++= "\\n"
                    case '\f' => sb ++= "\\f"
                    case '\r' => sb ++= "\\r"
                    case _ =>
                        if ch < ' ' then
                            val t = "000" + Integer.toHexString(ch)
                            sb ++= "\\u" ++= t.substring(t.length - 4)

                        else
                            sb += ch

            sb.toString()

    /**
      *
      * @param logger
      * @param title
      * @param e
      */
    def prettyError(logger: Logger, title: String, e: Throwable): Unit =
        // Keep the full trace in the 'trace' log level.
        logger.trace(title, e)

        prettyErrorImpl(new PrettyErrorLogger {
            override def log(s: String): Unit = logger.error(s)
        }, title, e)
    
    /**
      *
      * @param title
      * @param e
      */
    def prettyError(title: String, e: Throwable): Unit = prettyErrorImpl(new PrettyErrorLogger(), title, e)

    sealed class PrettyErrorLogger:
        def log(s: String): Unit = System.err.println(s)

    /**
      *
      * @param logger
      * @param title
      * @param e
      */
    private def prettyErrorImpl(logger: PrettyErrorLogger, title: String, e: Throwable): Unit =
        logger.log(title)

        val INDENT = 2
        var x = e
        var indent = INDENT
        while (x != null)
            var first = true
            var errMsg = x.getLocalizedMessage
            if errMsg == null then errMsg = "<null>"
            val exClsName = if !x.isInstanceOf[NCException] then s"$ansiRedFg[${x.getClass.getCanonicalName}]$ansiReset " else ""
            val trace = x.getStackTrace.find(!_.getClassName.startsWith("scala.")).getOrElse(x.getStackTrace.head)
            val fileName = trace.getFileName
            val lineNum = trace.getLineNumber
            val msg =
                if fileName == null || lineNum < 0 then
                    s"$exClsName$errMsg"
                else
                    s"$exClsName$errMsg $ansiCyanFg->$ansiReset ($fileName:$lineNum)"

            msg.split("\n").foreach(line => {
                val s = s"${" " * indent}${if first then ansiBlue("+-+ ") else "   "}${bo(y(line))}"
                logger.log(s)
                first = false
            })

            val traces = x.getStackTrace.filter { t =>
                val mtdName = t.getMethodName
                val clsName = t.getClassName

                // Clean up trace.
                clsName.startsWith("org.apache.nlpcraft") &&
                    !clsName.startsWith("org.apache.nlpcraft.internal.opencensus") &&
                    !mtdName.contains("startScopedSpan") &&
                    !mtdName.contains('$')
            }
            for (trace <- traces)
                val fileName = trace.getFileName
                val lineNum = trace.getLineNumber
                val mtdName = trace.getMethodName
                val clsName = trace.getClassName.replace("org.apache.nlpcraft", "o.a.n")

                logger.log(s"${" " * indent}  ${b("|")} $clsName.$mtdName $ansiCyanFg->$ansiReset ($fileName:$lineNum)")

            indent += INDENT

            x = x.getCause

    /**
      * Makes thread.
      *
      * @param name Name.
      * @param body Thread body.
      */
    def mkThread(name: String)(body: Thread => Unit): Thread =
        new Thread(name):
            @volatile private var stopped = false

            override def isInterrupted: Boolean = super.isInterrupted || stopped
            override def interrupt(): Unit =  stopped = true; super.interrupt()

            override def run(): Unit =
                logger.trace(s"Thread started: $name")

                try
                    body(this)
                    logger.trace(s"Thread exited: $name")
                catch
                    case _: InterruptedException => logger.trace(s"Thread interrupted: $name")
                    case e: Throwable => prettyError(logger, s"Unexpected error during '$name' thread execution:", e)
                finally
                    stopped = true

    /**
      * Gets resource existing flag.
      *
      * @param res Resource.
      */
    def isResource(res: String): Boolean = getClass.getClassLoader.getResourceAsStream(res) != null

    /**
      *
      * @param url URL to check.
      * @return
      */
    def isUrl(url: String): Boolean =
        try
            new URL(url)
            true
        catch
            case _: MalformedURLException => false

    /**
      *
      * @param path Local file path to check.
      * @return
      */
    def isFile(path: String): Boolean =
        val f = new File(path)
        f.exists() && f.isFile

    /**
      *
      * @param src Local filesystem path, resources file or URL.
      */
    def getStream(src: String): InputStream =
        if isFile(src) then new FileInputStream(new File(src))
        else if isResource(src) then
            getClass.getClassLoader.getResourceAsStream(src) match
                case in if in != null => in
                case _ => throw new NCException(s"Resource not found: $src")
        else if isUrl(src) then new URL(src).openStream()
        else throw new NCException(s"Source not found or unsupported: $src")

    /**
      * Makes thread.
      *
      * @param name Name.
      * @param body Thread body.
      */
    def mkThread(name: String, body: Runnable): Thread =
        mkThread(name) { _ => body.run() }

    /**
      * Sleeps number of milliseconds properly handling exceptions.
      *
      * @param delay Number of milliseconds to sleep.
      */
    def sleep(delay: Long): Unit =
        try
            Thread.sleep(delay)
        catch
            case _: InterruptedException => Thread.currentThread().interrupt()
            case e: Throwable => prettyError(logger, "Unhandled exception caught during sleep:", e)


    /**
      * Interrupts thread and waits for its finish.
      *
      * @param t Thread.
      */
    def stopThread(t: Thread): Unit =
        if t != null then
            t.interrupt()
            try
                t.join()
            catch
                case _: InterruptedException => logger.trace("Thread joining was interrupted (ignoring).")

    /**
      * Interrupts thread.
      *
      * @param t Thread.
      */
    def interruptThread(t: Thread): Unit = if t != null then t.interrupt()

    /**
      * Checks duplicated elements in collection.
      *
      * @param list Collection. Note, it should be list.
      * @param seen Checked elements.
      * @see #getDups
      */
    @tailrec
    def containsDups[T](list: List[T], seen: Set[T] = Set.empty[T]): Boolean =
        list match
            case x :: xs => if (seen.contains(x)) true else containsDups(xs, seen + x)
            case _ => false

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
    def distinct[T](seq: List[T]): List[T] = if containsDups(seq) then seq.distinct else seq

    /**
      * Safely and silently closes the client socket.
      *
      * @param sock Client socket to close.
      */
    def close(sock: Socket): Unit =
        if sock != null then
            ignoring(classOf[IOException]) {
                sock.close()
            }

    /**
      * Safely and silently closes the server socket.
      *
      * @param sock Server socket to close.
      */
    def close(sock: ServerSocket): Unit =
        if sock != null then
            ignoring(classOf[IOException]) {
                sock.close()
            }

    /**
      *
      * @param in Stream.
      */
    def close(in: InputStream): Unit =
        if in != null then
            ignoring(classOf[IOException]) {
                in.close()
            }

    /**
      *
      * @param out Stream.
      */
    def close(out: OutputStream): Unit =
        if out != null then
            ignoring(classOf[IOException]) {
                out.close()
            }

    /**
      * Closes auto-closeable ignoring any exceptions.
      *
      * @param a Resource to close.
      */
    def close(a: AutoCloseable): Unit =
        if a != null then
            ignoring(classOf[Exception]) {
                a.close()
            }

    /**
      *
      * @param s
      * @return
      */
    def decapitalize(s: String): String = s"${s.head.toLower}${s.tail}"

    /**
      *
      * @param s
      * @return
      */
    def capitalize(s: String): String = s"${s.head.toUpper}${s.tail}"

    /**
      * Makes absolute path starting from working directory.
      *
      * @param path Path.
      */
    def mkPath(path: String): String = new File(s"${new File("").getAbsolutePath}/$path").getAbsolutePath

    /**
      * Generates read-only text file with given path and strings.
      * Used by text files auto-generators.
      *
      * @param path Path of the output file.
      * @param lines Text data.
      * @param sort Whether to sort output or not.
      */
    def mkTextFile(path: String, lines: scala.Iterable[Any], sort: Boolean = true): Unit =
        val file = new File(path)

        Using.resource(new PrintStream(file)) {
            ps =>
                import java.util.*

                // Could be long for large sequences...
                val seq = if sort then lines.map(_.toString).toSeq.sorted else lines

                ps.println(s"#")
                ps.println(s"# Licensed to the Apache Software Foundation (ASF) under one or more")
                ps.println(s"# contributor license agreements.  See the NOTICE file distributed with")
                ps.println(s"# this work for additional information regarding copyright ownership.")
                ps.println(s"# The ASF licenses this file to You under the Apache License, Version 2.0")
                ps.println(s"# (the 'License'); you may not use this file except in compliance with")
                ps.println(s"# the License.  You may obtain a copy of the License at")
                ps.println(s"#")
                ps.println(s"#      https://www.apache.org/licenses/LICENSE-2.0")
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

    /**
      * Reads lines from given file.
      *
      * @param path Zipped file path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    def readGzipPath(path: String, enc: String = "UTF-8", log: Logger = logger): List[String] =
        readGzipFile(new File(path), enc, log)

    /**
      * Reads lines from given file.
      *
      * @param f Zipped file to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    def readGzipFile(f: File, enc: String, log: Logger = logger): List[String] =
        try
            Using.resource(Source.fromInputStream(new GZIPInputStream(new FileInputStream(f)), enc)) { src =>
                getAndLog(src.getLines().map(p => p).toList, f, log)
            }
        catch
            case e: IOException => throw new NCException(s"Failed to read GZIP file: ${f.getAbsolutePath}", e)

    /**
      * Reads bytes from given file.
      *
      * @param f File.
      * @param log Logger.
      */
    def readFileBytes(f: File, log: Logger = logger): Array[Byte] =
        try
            val arr = new Array[Byte](f.length().toInt)

            Using.resource(new FileInputStream(f))(_.read(arr))

            getAndLog(arr, f, log)
        catch
            case e: IOException => throw new NCException(s"Error reading file: $f", e)


    /**
      * Gzip file.
      *
      * @param path File path.
      * @param log Logger.
      */
    def gzipPath(path: String, log: Logger = logger): Unit = gzipFile(new File(path), log)

    /**
      * Gzip file.
      *
      * @param f File.
      * @param log Logger.
      */
    def gzipFile(f: File, log: Logger = logger): Unit =
        val gz = s"${f.getAbsolutePath}.gz"

        // Do not user BOS here - it makes files corrupted.
        try
            Using.resource(new GZIPOutputStream(new FileOutputStream(gz))) { stream =>
                stream.write(readFileBytes(f))
                stream.flush()
            }
        catch
            case e: IOException => throw new NCException(s"Error gzip file: $f", e)

        if !f.delete() then throw new NCException(s"Error while deleting file: $f")

        logger.trace(s"File gzipped [source=$f, destination=$gz]")

    /**
      *
      * @param data
      * @param f
      * @param log
      */
    private def getAndLog[T](data: T, f: File, log: Logger = logger): T =
        log.trace(s"Loaded file: ${f.getAbsolutePath}")

        data

    /**
      * Reads lines from given resource.
      *
      * @param res Resource path to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    def readResource(res: String, enc: String = "UTF-8", log: Logger = logger): List[String] =
        val list =
            try
                Using.resource(Source.fromInputStream(getStream(res), enc))(_.getLines().toSeq).toList
            catch
                case e: IOException => throw new NCException(s"Failed to read stream: $res", e)
    
        log.trace(s"Loaded resource: $res")

        list

    /**
      *
      * @param in
      * @return
      */
    private def readLcTrimFilter(in: BufferedSource): List[String] =
        in.getLines().map(_.toLowerCase.strip).filter(s => s.nonEmpty && s.head!= '#').toList

    /**
      * Reads lines from given stream converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param res Zipped resource to read from.
      * @param enc Encoding.
      * @param log Logger to use.
      */
    def readTextGzipResource(res: String, enc: String, log: Logger = logger): List[String] =
        val list =
            try
                Using.resource(Source.fromInputStream(new GZIPInputStream(getStream(res)), enc))(readLcTrimFilter)
            catch
                case e: IOException => throw new NCException(s"Failed to read stream: $res", e)

        log.trace(s"Loaded resource: $res")

        list

    /**
      * Reads lines from given stream converting to lower case, trimming, and filtering
      * out empty lines and comments (starting with '#').
      *
      * @param in Stream to read from.
      * @param enc Encoding.
      */
    def readTextStream(in: InputStream, enc: String): List[String] =
        try
            Using.resource(Source.fromInputStream(in, enc)) { src =>
                readLcTrimFilter(src)
            }
        catch
            case e: IOException => throw new NCException(s"Failed to read stream.", e)

    /**
      *
      * @param bodies
      * @param ec
      */
    def execPar(bodies: (() => Any)*)(ec: ExecutionContext): Unit =
        bodies.map(body => Future { body() } (ec)).foreach(Await.result(_, Duration.Inf))

    /**
      *
      * @return
      */
    def genUUID(): UUID = UUID.randomUUID()

    /**
      * Gets all sequential permutations of tokens in this NLP sentence.
      *
      * For example, if NLP sentence contains "a, b, c, d" tokens, then
      * this function will return the sequence of following token sequences in this order:
      * "a b c d"
      * "a b c"
      * "b c d"
      * "a b"
      * "b c"
      * "c d"
      * "a"
      * "b"
      * "c"
      * "d"
      *
      * NOTE: this method will not return any permutations with a quoted token.
      *
      * @param tokens Tokens.
      * @param stopWords Whether or not include tokens marked as stop words.
      * @param maxLen Maximum number of tokens in the sequence.
      */
    def tokenMix(tokens: Seq[NCToken], stopWords: Boolean = false, maxLen: Int = Integer.MAX_VALUE): Seq[Seq[NCToken]] =
        val toks = tokens.filter(t => stopWords || (!stopWords && !t.isStopWord))

        (for (n <- toks.length until 0 by -1 if n <= maxLen) yield toks.sliding(n)).flatten

    /**
      * Gets all sequential permutations of tokens in this NLP sentence.
      * This method is like a 'tokenMix', but with all combinations of stop-words (with and without)
      *
      * @param tokens Tokens.
      * @param maxLen Maximum number of tokens in the sequence.
      */
    def tokenMixWithStopWords(tokens: Seq[NCToken], maxLen: Int = Integer.MAX_VALUE): Seq[Seq[NCToken]] =
        /**
          * Gets all combinations for sequence of mandatory tokens with stop-words and without.
          *
          * Example:
          * 'A (stop), B, C(stop) -> [A, B, C]; [A, B]; [B, C], [B]
          * 'A, B(stop), C(stop) -> [A, B, C]; [A, B]; [A, C], [A].
          *
          * @param toks Tokens.
          */
        def permutations(toks: Seq[NCToken]): Seq[Seq[NCToken]] = 
            def multiple(seq: Seq[Seq[Option[NCToken]]], t: NCToken): Seq[Seq[Option[NCToken]]] =
                if seq.isEmpty then
                    if t.isStopWord then IndexedSeq(IndexedSeq(Some(t)), IndexedSeq(None)) else IndexedSeq(IndexedSeq(Some(t)))
                else
                    (for (subSeq <- seq) yield subSeq :+ Some(t)) ++ (if t.isStopWord then for (subSeq <- seq) yield subSeq :+ None else Seq.empty)

            var res: Seq[Seq[Option[NCToken]]] = Seq.empty
            for (t <- toks) res = multiple(res, t)
            res.map(_.flatten).filter(_.nonEmpty)

        tokenMix(tokens, stopWords = true, maxLen).
            flatMap(permutations).
            filter(_.nonEmpty).
            distinct.
            sortBy(seq => (-seq.length, seq.head.getStartCharIndex))
