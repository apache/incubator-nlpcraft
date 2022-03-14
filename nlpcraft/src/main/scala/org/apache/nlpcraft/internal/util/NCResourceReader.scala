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

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.apache.nlpcraft.*
import org.apache.nlpcraft.internal.util.NCUtils.*

import java.io.*
import java.net.URL
import java.nio.file.Files
import scala.collection.immutable.Map
import scala.io.Source
import scala.util.Using

/**
  * Caching resource reader for files that cannot be shipped with Apache release.
  */
object NCResourceReader extends LazyLogging:
    private final val DFLT_DIR = new File(System.getProperty("user.home"), ".nlpcraft/extcfg").getAbsolutePath
    private final val BASE_URL = "https://github.com/apache/incubator-nlpcraft/raw/external_config/external"
    private final val MD5_FILE_URL = s"$BASE_URL/md5.txt"

    /**
      *
      * @param dir
      * @return
      */
    private def mkDir(dir: String): File =
        val normDir = if dir != null then dir else DFLT_DIR
        val f = new File(normDir)

        if f.exists then if !f.isDirectory then E(s"Invalid folder: $normDir")
        else if !f.mkdirs then E(s"Cannot create folder: $normDir")

        f

    /**
      *
      * @param url
      * @return
      */
    private def readMd5(url: String): Map[String, String] =
        try
            Using.resource(Source.fromURL(url)) { src =>
                src.getLines().map(_.trim()).filter(s => s.nonEmpty && !s.startsWith("#")).map(p => {
                    val seq = p.split(" ").map(_.strip)

                    if seq.length != 2 || seq.exists(_.isEmpty) then
                        throw new NCException(s"Unexpected '$url' file line format: '$p'")

                    seq.head -> seq.last
                }).toList.toMap
            }
        catch case e: IOException => throw new NCException(s"Failed to read: '$url'", e)

    /**
      *
      * @param f
      */
    private def delete(f: File): Unit =
        if !f.delete() then E(s"Couldn't delete file: ${f.getAbsolutePath}")
        else logger.info(s"File deleted: ${f.getAbsolutePath}")

    /**
      *
      * @param f
      * @param md5
      * @return
      */
    private def getMd5(f: File, md5: Map[String, String]): String =
        val path = f.getAbsolutePath
        val nameLen = f.getName.length

        md5.
            flatMap { (resPath, md5) => if path.endsWith(resPath) && resPath.length >= nameLen then Option(md5) else None }.
            to(LazyList).
            headOption.
            getOrElse(throw new NCException(s"MD5 data not found for: '$path'"))

    /**
      *
      * @param f
      * @param md5
      * @return
      */
    private def isValid(f: File, md5: Map[String, String]): Boolean =
        val v1 = getMd5(f, md5)
        val v2 =
            try Using.resource(Files.newInputStream(f.toPath)) { in => DigestUtils.md5Hex(in) }
            catch case e: IOException => throw new NCException(s"Failed to get MD5 for: '${f.getAbsolutePath}'", e)

        v1 == v2

    /**
      *
      * @param in
      * @param dest
      */
    private def copy(in: InputStream, dest: String): Unit =
        Using.resource(new FileOutputStream(dest)) { out => IOUtils.copy(in, out) }

    /**
      *
      * @param path
      * @param outFile
      * @param md5
      * @return
      */
    private def download(path: String, outFile: String, md5: Map[String, String]): File =
        mkDir(new File(outFile).getParent)
        val url = s"$BASE_URL/$path"
        try
            Using.resource(new BufferedInputStream(new URL(url).openStream())) { src =>
                copy(src, outFile)
                logger.info(s"One-time download for external config [url='$url', file='$outFile']")

                val f = new File(outFile)
                if !isValid(f, md5) then throw new NCException(s"Invalid downloaded file [url='$url'")
                f
            }
        catch case e: IOException => throw new NCException(s"Failed to download external config [url='$url', file='$outFile']", e)

    /**
      *
      * @param path
      * @return
      */
    def get(path: String): File =
        val md5 = readMd5(MD5_FILE_URL)
        var f = new File(path)

        def validateOrDownload(f: File): File =
            if isValid(f, md5) then
                logger.info(s"File found: ${f.getAbsolutePath}")
                f
            else
                delete(f)
                download(path, f.getAbsolutePath, md5)

        // Path.
        if NCUtils.isFile(f) then
            validateOrDownload(f)
        else
            // Path in default folder.
            f = new File(DFLT_DIR, path)

            if NCUtils.isFile(f) then
                validateOrDownload(f)
            else
                // Resource.
                if NCUtils.isResource(path) then
                    getClass.getClassLoader.getResourceAsStream(path) match
                        case in if in != null =>
                            copy(in, f.getAbsolutePath)
                            validateOrDownload(f)
                        case _ => E(s"Resource not found: $path")
                // URL.
                else if NCUtils.isUrl(path) then
                    IOUtils.copy(new URL(path), f)
                    validateOrDownload(f)
                else
                    // Download.
                    download(path, f.getAbsolutePath, md5)

    /**
      *
      * @param path
      * @return
      */
    def getPath(path: String): String = get(path).getAbsolutePath
