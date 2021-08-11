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

package org.apache.nlpcraft.model.tools.cmdline

import org.apache.commons.lang3.SystemUtils
import org.apache.nlpcraft.common.U
import org.junit.jupiter.api.Test

import java.io.{BufferedReader, File, FileFilter, InputStreamReader}
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.collection.mutable
import scala.util.Using

/**
  * This test designed only for maven tests (mvn clean verify)
  * It cannot be started together with other server instances.
  */
class NCCliSpec {
    private def check(dirs: File*): Unit = dirs.foreach(d => require(d.exists() && d.isDirectory, s"Invalid folder: $d"))

    private def getAllDepsJar(dirTarget: File): File = {
        val jars = dirTarget.listFiles(new FileFilter {
            override def accept(f: File): Boolean = f.isFile && f.getName.toLowerCase().endsWith("all-deps.jar")
        })

        require(jars != null && jars.length == 1, s"Required jar file not found in ${dirTarget.getAbsolutePath}")

        jars.head
    }

    private def makeProcess(
        scriptArg: String,
        script: String,
        allDepsJar: File,
        dirBin: File,
        timeoutSecs: Int,
        expectedLines: String*
    ): Process = {
        val args = if (SystemUtils.IS_OS_UNIX) Seq("bash", "-f", script, scriptArg) else Seq(script, scriptArg)

        val builder = new ProcessBuilder(args: _*)

        builder.environment().put("CP", allDepsJar.getAbsolutePath)
        builder.directory(dirBin)
        builder.redirectErrorStream(true)

        val process = builder.start()

        val cdl = new CountDownLatch(1)
        var done = false

        val thread = new Thread() {
            override def run(): Unit = {
                Using.resource { new BufferedReader(new InputStreamReader(process.getInputStream)) }(reader => {
                    var line: String = reader.readLine()

                    while (line != null && !done) {
                        if (expectedLines.exists(line.contains)) {
                            done = true

                            println(s"$scriptArg finished fine by expected line: '$line'")
                            println()

                            cdl.countDown()
                        }
                        else {
                            println(s"($scriptArg) $line")

                            line = reader.readLine()
                        }
                    }
                })
            }
        }

        thread.start()

        cdl.await(timeoutSecs, TimeUnit.SECONDS)

        U.stopThread(thread)

        require(done, s"Command cannot be started: $scriptArg")

        process
    }

    @Test
    def test(): Unit = {
        // All folders should be exists because tests started from maven phase, after build project.
        val ext = if (SystemUtils.IS_OS_UNIX) "sh" else "cmd"
        val dirUsr = new File(SystemUtils.USER_DIR).getParentFile.getParentFile
        val dirBin = new File(dirUsr, "bin")
        val dirTarget = new File(dirUsr, "nlpcraft/target")
        val script = new File(dirBin, s"nlpcraft.$ext").getAbsolutePath

        check(dirUsr, dirBin, dirTarget)

        val allDepsJar = getAllDepsJar(dirTarget)

        def make(scriptArg: String, timeoutSecs: Int, expectedLines: String*): Process =
            makeProcess(scriptArg, script, allDepsJar, dirBin, timeoutSecs, expectedLines: _*)

        val procs = mutable.Buffer.empty[Process]

        def stopInstances(): Unit = {
            // Both variant (stopped or already stopped) are fine.
            def stop(cmd: String): Process = make(cmd, 10, "has been stopped", "not found")

            procs += stop("stop-server")
            procs += stop("stop-probe")
        }

        try {
            stopInstances()

            procs += make("start-server", 120, "Started on")
            procs += make("start-probe", 30, "Started on")
            procs += make("info", 10, "Local server")
        }
        finally
            try
                stopInstances()
            finally
                procs.foreach(_.destroy())
    }
}
