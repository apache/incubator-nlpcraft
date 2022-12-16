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

import com.typesafe.scalalogging.*
import org.apache.nlpcraft.*
import com.google.gson.*

import java.io.*
import java.net.*
import java.time.{ZoneId, Instant, ZonedDateTime}
import java.util.concurrent.{CopyOnWriteArrayList, ExecutorService, TimeUnit}
import java.util.regex.Pattern
import java.util.zip.*
import java.util.{Random, TimeZone}

import scala.annotation.tailrec
import scala.collection.{IndexedSeq, Seq, mutable}
import scala.concurrent.*
import scala.jdk.CollectionConverters.*
import scala.concurrent.duration.Duration
import scala.io.*
import scala.sys.SystemProperties
import scala.util.Using
/**
  * 
  */
object NCUtils extends LazyLogging:
    private val sysProps = new SystemProperties
    private final lazy val GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    /**
      * Gets system property, or environment variable (in that order), or `None` if none exists.
      *
      * @param s Name of the system property or environment variable.
      */
    def sysEnv(s: String): Option[String] = sysProps.get(s).orElse(sys.env.get(s))

    /**
      * Shortcut to convert given JSON to Scala map with default mapping.
      *
      * @param json JSON to convert.
      */
    @throws[Exception]
    def jsonToScalaMap(json: String): Map[String, Object] =
        GSON.fromJson(json, classOf[java.util.HashMap[String, Object]]).asScala.toMap

    /**
      * Shortcut to convert given JSON to Java map with default mapping.
      *
      * @param json JSON to convert.
      */
    def jsonToJavaMap(json: String): java.util.Map[String, Object] =
        try GSON.fromJson(json, classOf[java.util.HashMap[String, Object]])
        catch case e: Exception => E(s"Cannot deserialize JSON to map: '$json'", e)

    /**
      * Gets now in UTC timezone in milliseconds representation.
      */
    def nowUtcMs(): Long = Instant.now().toEpochMilli

    /**
      * Shortcut - current timestamp in milliseconds.
      */
    def now(): Long = System.currentTimeMillis()

    /**
      * Trims each sequence string and filters out empty ones.
      *
      * @param s String to process.
      */
    private def trimFilter(s: Seq[String]): Seq[String] = s.map(_.strip).filter(_.nonEmpty)

    /**
      * Splits, trims and filters empty strings for the given string.
      *
      * @param s String to split.
      * @param sep Separator (regex) to split by.
      */
    def splitTrimFilter(s: String, sep: String): Seq[String] =
        trimFilter(s.split(sep).toIndexedSeq)

    /**
      * Recursively removes quotes from given string.
      *
      * @param s
      */
    @tailrec
    def trimQuotes(s: String): String =
        val z = s.strip
        if z.startsWith("'") && z.endsWith("'") || z.startsWith("\"") && z.endsWith("\"") then
            trimQuotes(z.substring(1, z.length - 1))
        else
            z

    /**
      * Recursively removes quotes and replaces escaped quotes from given string.
      *
      * @param s
      */
    @tailrec
    private def trimEscapesQuotes(s: String): String =
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
      */
    @tailrec
    def escapesQuotes(s: String): String =
        if s.nonEmpty then
            if s.head == '\'' && s.last == '\'' then
                escapesQuotes(s.substring(1, s.length - 1).replace("\'", "'"))
            else if s.head == '"' && s.last == '"' then
                escapesQuotes(s.substring(1, s.length - 1).replace("\\\"", "\""))
            else
                s
        else
            s

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
            override def interrupt(): Unit =
                stopped = true
                super.interrupt()

            override def run(): Unit =
                logger.trace(s"Thread started: $name")

                try
                    body(this)
                    logger.trace(s"Thread exited: $name")
                catch
                    case _: InterruptedException => logger.trace(s"Thread interrupted: $name")
                    case e: Throwable => logger.warn(s"Unexpected error during '$name' thread execution:", e)
                finally
                    stopped = true

    /**
      *
      * @param prefix
      * @param mdlId
      * @param body
      */
    def mkThread(prefix: String, mdlId: String)(body: Thread => Unit): Thread = mkThread(s"$prefix-@$mdlId")(body)

    /**
      * Gets resource existing flag.
      *
      * @param res Resource.
      */
    def isResource(res: String): Boolean = getClass.getClassLoader.getResourceAsStream(res) != null

    /**
      *
      * @param url URL to check.
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
      */
    def isFile(path: String): Boolean =
        val f = new File(path)
        f.exists() && f.isFile

    /**
      *
      * @param f
      */
    def isFile(f: File): Boolean = f.exists() && f.isFile

    /**
      *
      * @param src Local filesystem path, resources file or URL.
      */
    def getStream(src: String): InputStream =
        if isFile(src) then new FileInputStream(new File(src))
        else if isResource(src) then
            getClass.getClassLoader.getResourceAsStream(src) match
                case in if in != null => in
                case _ => E(s"Resource not found: $src")
        else if isUrl(src) then new URL(src).openStream()
        else E(s"Source not found or unsupported: $src")

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
            case e: Throwable => logger.warn("Unhandled exception caught during sleep:", e)


    /**
      * Interrupts thread and waits for its finish.
      *
      * @param t Thread.
      */
    def stopThread(t: Thread): Unit =
        if t != null then
            t.interrupt()
            try t.join()
            catch case _: InterruptedException => logger.trace("Thread joining was interrupted (ignoring).")

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
            case x :: xs => if seen.contains(x) then true else containsDups(xs, seen + x)
            case _ => false

    /**
      * Gets set of duplicate values from given sequence (potentially empty).
      *
      * @param seq Sequence to check for dups from.
      * @tparam T
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
            try sock.close()
            catch case _: Exception => ()

    /**
      * Safely and silently closes the server socket.
      *
      * @param sock Server socket to close.
      */
    def close(sock: ServerSocket): Unit =
        if sock != null then
            try sock.close()
            catch case _: Exception => ()

    /**
      *
      * @param in Stream.
      */
    def close(in: InputStream): Unit =
        if in != null then
            try in.close()
            catch case _: Exception => ()

    /**
      *
      * @param out Stream.
      */
    def close(out: OutputStream): Unit =
        if out != null then
            try out.close()
            catch case _: Exception => ()

    /**
      * Closes auto-closeable ignoring any exceptions.
      *
      * @param a Resource to close.
      */
    def close(a: AutoCloseable): Unit =
        if a != null then
            try a.close()
            catch case _: Exception => ()

    /**
      *
      * @param s
      */
    def decapitalize(s: String): String = s"${s.head.toLower}${s.tail}"

    /**
      *
      * @param s
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
      * Reads bytes from given file.
      *
      * @param f File.
      * @param log Logger.
      */
    private def readFileBytes(f: File, log: Logger = logger): Array[Byte] =
        try
            val arr = new Array[Byte](f.length().toInt)
            Using.resource(new FileInputStream(f))(_.read(arr))
            getAndLog(arr, f, log)
        catch
            case e: IOException => E(s"Error reading file: $f", e)


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
    private def gzipFile(f: File, log: Logger = logger): Unit =
        val gz = s"${f.getAbsolutePath}.gz"

        // Do not user BOS here - it makes files corrupted.
        try
            Using.resource(new GZIPOutputStream(new FileOutputStream(gz))) { stream =>
                stream.write(readFileBytes(f))
                stream.flush()
            }
        catch
            case e: IOException => E(s"Error gzip file: $f", e)

        if !f.delete() then E(s"Error while deleting file: $f")

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
      *  Reads lines from given resource.
      *
      * @param res
      * @param enc
      * @param strip
      * @param convert
      * @param filterText
      * @param log
      * @return
      */
    def readLines(
        res: String | File | InputStream,
        enc: String = "UTF-8",
        strip: Boolean = false,
        convert: String => String = s => s,
        filterText: Boolean = false,
        log: Logger = logger
    ): Iterator[String] =
        try
            val (stream, name) =
                res match
                    case is: InputStream => (is, is.getClass.getName)
                    case s: String => (new BufferedInputStream(getStream(s)), s)
                    case f: File => (new BufferedInputStream(new FileInputStream(f)), f.getAbsolutePath)

            val out = Source.fromInputStream(stream, enc).getLines().flatMap(line =>
                val s = convert(if strip then line.strip else line)
                Option.when(!filterText || s.nonEmpty && s.head != '#')(s)
            )
            log.info(s"Loaded resource: $name")
            out
        catch case e: IOException => E(s"Failed to read stream: $res", e)

    /**
      *
      * @param res
      * @param enc
      * @param strip
      * @param convert
      * @param filterText
      * @param log
      * @return
      */
    def readGzipLines(
        res: String,
        enc: String = "UTF-8",
        strip: Boolean = false,
        convert: String => String = s => s,
        filterText: Boolean = false,
        log: Logger = logger
    ): Iterator[String] = readLines(new GZIPInputStream(getStream(res)), enc, strip, convert, filterText, log)

    /**
      *
      * @param bodies
      * @param ec
      */
    def execPar(bodies: Seq[() => Any])(ec: ExecutionContext): Unit =
        val errs = new CopyOnWriteArrayList[Throwable]()

        bodies.map(body => Future {
            try
                body()
            catch
                case e: Throwable => errs.add(e)

        } (ec)).foreach(Await.result(_, Duration.Inf))

        if !errs.isEmpty then
            errs.forEach(e => logger.error("Parallel execution error.", e))
            E("Parallel execution failed - see previous error log.")

    /**
      * Shuts down executor service and waits for its finish.
      *
      * @param es Executor service.
      */
    def shutdownPool(es: ExecutorService): Unit =
        if es != null then
            es.shutdown()

            try
                es.awaitTermination(Long.MaxValue, TimeUnit.MILLISECONDS)
            catch
                case _: InterruptedException => () // Safely ignore.

    /**
      *
      * @param tok
      * @param name
      * @tparam T
      * @return
      */
    def getProperty[T](tok: NCToken, name: String): T =
        tok.get(name).getOrElse(throw new NCException(
            s"'$name' property not found in token [index=${tok.getIndex}, text=${tok.getText}, properties=${tok.keysSet}]")
        )