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

package org.apache.nlpcraft.server.json

import java.io.{IOException, _}
import java.util.zip._

import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json.{compactRender ⇒ liftCompact, prettyRender ⇒ liftPretty, _}
import org.apache.nlpcraft.common._

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.matching.Regex

/**
 * Project-wide, Lift-based general JSON wrapper.
 */
class NCJson(val json: JValue) {
    import NCJson._

    require(json != null)

    // Delegate to underlying JValue.
    override def hashCode(): Int = json.hashCode()
    override def equals(obj: scala.Any): Boolean = json.equals(obj)

    /**
     * Convenient method to get JSON unboxed value with given type and name.
     *
     * @param fn Field name.
     * @tparam T Type of the value.
     */
    @throws[NCJ]
    def field[T](fn: String): T =
        try
            json \ fn match {
                case JNothing | null ⇒ throw MissingJsonField(fn)
                case v: JValue ⇒ v.values.asInstanceOf[T]
            }
        catch {
            case e: MissingJsonField ⇒ throw e // Rethrow.
            case e: Throwable ⇒ throw InvalidJsonField(fn, e)
        }

    /**
      * Tests whether given JSON field present or not.
      *
      * @param fn JSON field name.
      */
    def hasField(fn: String): Boolean =
        json \ fn match {
            case JNothing | null ⇒ false
            case _: JValue ⇒ true
        }

    /**
     * Convenient method to get JSON unboxed value with given type and name.
     *
     * @param fn Field name.
     * @tparam T Type of the value.
     */
    def fieldOpt[T](fn: String): Option[T] =
        try
            json \ fn match {
                case JNothing ⇒ None
                case v: JValue ⇒ Some(v.values.asInstanceOf[T])
            }
        catch {
            case _: Throwable ⇒ None
        }

    /**
     * Renders this JSON with proper new-lines and indentation (suitable for human readability).
     *
     * @return String presentation of this JSON object.
     */
    def pretty: String = liftPretty(json)

    /**
     * Renders this JSON in a compact form (suitable for exchange).
     *
     * @return String presentation of this JSON object.
     */
    def compact: String = liftCompact(json)

    /**
     * Zips this JSON object into array of bytes using GZIP.
     */
    def gzip(): Array[Byte] = {
        val out = new ByteArrayOutputStream(1024)

        try {
            val gzip = new GZIPOutputStream(out)

            gzip.write(compact.getBytes)

            gzip.close()

            out.toByteArray
        }
        // Let IOException to propagate unchecked (since it shouldn't appear here by the spec).
        finally {
            out.close()
        }
    }

    override def toString: String = compact
}

/**
 * Static scope for JSON wrapper.
 */
object NCJson {
    private type NCJ = NCJsonException

    // Specific control flow exceptions.
    case class InvalidJson(js: String) extends NCJ(s"Malformed JSON syntax in: $js") with LazyLogging {
        // Log right away.
        logger.error(s"Malformed JSON syntax in: $js")
    }

    case class InvalidJsonField(fn: String, cause: Throwable) extends NCJ(s"Invalid '$fn' JSON field <" +
        cause.getMessage + ">", cause) with LazyLogging {
        require(cause != null)

        // Log right away.
        logger.error(s"Invalid '$fn' JSON field <${cause.getMessage}>")
    }

    case class MissingJsonField(fn: String) extends NCJ(s"Missing mandatory '$fn' JSON field.") with LazyLogging {
        // Log right away.
        logger.error(s"Missing mandatory '$fn' JSON field.")
    }

    implicit val formats: DefaultFormats.type = net.liftweb.json.DefaultFormats

    // Regex for numbers with positive exponent part with explicit + in notation. Example 2E+5.
    // Also, these numbers should be pre-fixed and post-fixed by restricted JSON symbols set.
    private val EXP_REGEX = {
        val mask = "[-+]?([0-9]+\\.?[0-9]*|\\.[0-9]+)([eE]\\+[0-9]+)"
        val pre = Seq(' ', '"', '[', ',', ':')
        val post = Seq(' ', '"', ']', ',', '}')

        def makeMask(chars: Seq[Char]): String = s"[${chars.map(ch ⇒ s"\\$ch").mkString}]"

        new Regex(s"${makeMask(pre)}$mask${makeMask(post)}")
    }

    /**
     * Creates JSON wrapper from given string.
     *
     * @param js JSON string presentation.
     * @return JSON wrapper.
     */
    @throws[NCJ]
    def apply(js: String): NCJson = {
        require(js != null)

        JsonParser.parseOpt(processExpNumbers(js)) match {
            case Some(a) ⇒ new NCJson(a)
            case _ ⇒ throw InvalidJson(js)
        }
    }

    /**
     * Creates JSON wrapper from given Lift `JValue` object.
     *
     * @param json Lift `JValue` AST object.
     * @return JSON wrapper.
     */
    @throws[NCJ]
    def apply(json: JValue): NCJson = {
        require(json != null)

        new NCJson(json)
    }


    /**
     * Unzips array of bytes into string.
     *
     * @param arr Array of bytes produced by 'gzip' method.
     */
    def unzip2String(arr: Array[Byte]): String = {
        val in = new ByteArrayInputStream(arr)
        val out = new ByteArrayOutputStream(1024)

        val tmpArr = new Array[Byte](512)

        try {
            val gzip = new GZIPInputStream(in)

            var n = gzip.read(tmpArr, 0, tmpArr.length)

            while (n > 0) {
                out.write(tmpArr, 0, n)

                n = gzip.read(tmpArr, 0, tmpArr.length)
            }

            gzip.close()

            // Trim method added to delete last symbol of ZLIB compression
            // protocol (NULL - 'no error' flag) http://www.zlib.net/manual.html
            out.toString("UTF-8").trim
        }
        // Let IOException to propagate unchecked (since it shouldn't appear here by the spec).
        finally {
            out.close()
            in.close()
        }
    }

    /**
     * Unzips array of bytes into JSON object.
     *
     * @param arr Array of bytes produced by 'gzip' method.
     */
    def unzip2Json(arr: Array[Byte]): NCJson = NCJson(unzip2String(arr))

    /**
     * Reads file.
     *
     * @param f File to extract from.
     */
    private def readFile(f: File): String = removeComments(U.readFile(f, "UTF8").mkString)

    /**
      * Reads stream.
      *
      * @param in Stream to extract from.
      */
    private def readStream(in: InputStream): String = removeComments(U.readStream(in, "UTF8").mkString)

    /**
     * Extracts type `T` from given JSON `file`.
     *
     * @param f File to extract from.
     * @param ignoreCase Whether or not to ignore case.
     * @tparam T Type of the object to extract.
     */
    @throws[NCE]
    def extractFile[T: Manifest](f: java.io.File, ignoreCase: Boolean): T =
        try
            if (ignoreCase) NCJson(readFile(f).toLowerCase).json.extract[T] else NCJson(readFile(f)).json.extract[T]
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read: ${f.getAbsolutePath}", e)
            case e: Throwable ⇒ throw new NCE(s"Failed to parse: ${f.getAbsolutePath}", e)
        }

    /**
     * Removes C-style /* */ multi-line comments from JSON.
     *
     * @param json JSON text.
     */
    private def removeComments(json: String): String = json.replaceAll("""/\*(\*(?!/)|[^*])*\*/""", "")

    /**
     * Extracts type `T` from given JSON `file`.
     *
     * @param path File path to extract from.
     * @param ignoreCase Whether or not to ignore case.
     * @tparam T Type of the object to extract.
     */
    @throws[NCE]
    def extractPath[T: Manifest](path: String, ignoreCase: Boolean): T = extractFile(new java.io.File(path), ignoreCase)

    /**
      * Extracts type `T` from given JSON `file`.
      *
      * @param res Resource to extract from.
      * @param ignoreCase Whether or not to ignore case.
      * @tparam T Type of the object to extract.
      */
    @throws[NCE]
    def extractResource[T: Manifest](res: String, ignoreCase: Boolean): T =
        try {
            val in = U.getStream(res)

            if (ignoreCase) NCJson(readStream(in).toLowerCase).json.extract[T] else NCJson(readStream(in)).json.extract[T]
        }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read: $res", e)
            case e: Throwable ⇒ throw new NCE(s"Failed to parse: $res", e)
        }

    // Gets string with removed symbol + from exponent part of numbers.
    // It is developed to avoid Lift parsing errors during processing numbers like '2E+2'.
    @tailrec
    def processExpNumbers(s: String): String =
        EXP_REGEX.findFirstMatchIn(s) match {
            case Some(m) ⇒ processExpNumbers(m.before + m.group(0).replaceAll("\\+", "") + m.after)
            case None ⇒ s
        }

    // Implicit conversions.
    implicit def x(jv: JValue): NCJson = new NCJson(jv)
    implicit def x1(js: NCJson): JValue = js.json
    implicit def x2(likeJs: NCJsonLike): JValue = likeJs.toJson.json
    implicit def x3(likeJs: NCJsonLike): NCJson = likeJs.toJson
    implicit def x4(js: NCJson): String = js.compact
    implicit def x4(js: NCJsonLike): String = js.toJson.compact
}
