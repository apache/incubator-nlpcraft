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

package org.apache.nlpcraft.common.util

import com.typesafe.scalalogging.*
import org.apache.nlpcraft.common.NCException
import org.apache.nlpcraft.common.ansi.NCAnsi.*

import java.io.*
import java.net.*
import java.util.Random
import java.util.regex.Pattern
import scala.annotation.tailrec
import scala.sys.SystemProperties
import scala.util.control.Exception.ignoring

/**
  *
  */
object NCUtils extends LazyLogging:
    final val NL = System getProperty "line.separator"
    private val RND = new Random()
    private val sysProps = new SystemProperties
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
    def notNull[T <: AnyRef](v: T, dflt: T): T = if (v == null) dflt else v

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
                val s = s"${" " * indent}${if (first) ansiBlue("+-+ ") else "   "}${bo(y(line))}"
                logger.log(s)
                first = false
            })

            val traces = x.getStackTrace.filter { t =>
                val mtdName = t.getMethodName
                val clsName = t.getClassName

                // Clean up trace.
                clsName.startsWith("org.apache.nlpcraft") &&
                    !clsName.startsWith("org.apache.nlpcraft.common.opencensus") &&
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