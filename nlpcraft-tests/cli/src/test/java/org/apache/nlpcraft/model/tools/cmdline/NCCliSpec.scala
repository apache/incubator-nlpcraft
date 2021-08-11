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
import org.junit.jupiter.api.{BeforeEach, Test}

import java.io.{BufferedReader, File, FileFilter, InputStreamReader}
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.collection.mutable
import scala.util.Using

/**
  * This test designed only for maven tests ('mvn clean verify')
  *  - Project target folder should contains built jars.
  *  - This test cannot be started together with other server instances.
  */
class NCCliSpec {
    private var dirBin: File  = _
    private var script: File  = _
    private var allDepsJar: File = _

    case class ProcessWrapper(process: Process, listener: Thread) {
        def destroy(): Unit = {
            process.destroy()
            U.stopThread(listener)
        }
    }

    @BeforeEach
    def before(): Unit = {
        val dirUsr = new File(SystemUtils.USER_DIR).getParentFile.getParentFile
        val dirTarget = new File(dirUsr, "nlpcraft/target")

        dirBin = new File(dirUsr, "bin")
        script = new File(dirBin, s"nlpcraft.${if (SystemUtils.IS_OS_UNIX) "sh" else "cmd"}")

        // All folders should be exists because tests started from maven phase, after build project.
        Seq(dirUsr, dirBin, dirTarget).foreach(d => require(d.exists() && d.isDirectory, s"Invalid folder: $d"))

        allDepsJar = {
            val jars = dirTarget.listFiles(new FileFilter {
                override def accept(f: File): Boolean = f.isFile && f.getName.toLowerCase().endsWith("all-deps.jar")
            })

            require(jars != null && jars.length == 1, s"Required jar file not found in ${dirTarget.getAbsolutePath}")

            jars.head
        }
    }

    private def start(arg: String, timeoutSecs: Int, expectedLines: String*): ProcessWrapper = {
        val scriptArgs =
            if (SystemUtils.IS_OS_UNIX)
                Seq("bash", "-f", script.getAbsolutePath, arg)
            else
                Seq(script.getAbsolutePath, arg)

        val builder =
            new ProcessBuilder(scriptArgs: _*).
                directory(dirBin).
                redirectErrorStream(true)

        builder.environment().put("CP", allDepsJar.getAbsolutePath)

        val proc = builder.start()

        val cdl = new CountDownLatch(1)
        var isStarted = false

        val thread = new Thread() {
            override def run(): Unit = {
                Using.resource { new BufferedReader(new InputStreamReader(proc.getInputStream)) }(reader => {
                    var line = reader.readLine()

                    while (line != null && !isStarted) {
                        if (expectedLines.exists(line.contains)) {
                            isStarted = true

                            println(s"$arg started fine. Expected line found: '$line'")
                            println()

                            cdl.countDown()
                        }
                        else {
                            println(s"($arg) $line")

                            line = reader.readLine()
                        }
                    }
                })
            }
        }

        thread.start()

        val w = ProcessWrapper(proc, thread)

        cdl.await(timeoutSecs, TimeUnit.SECONDS)

        if (!isStarted) {
            w.destroy()

            throw new RuntimeException(s"Command cannot be started: $arg")
        }

        w
    }

    @Test
    def test(): Unit = {
        val procs = mutable.Buffer.empty[ProcessWrapper]

        def stopInstances(): Unit = {
            // Both variant (stopped or already stopped) are fine.
            procs += start("stop-server", 10, "has been stopped", "Local server not found")
            procs += start("stop-probe", 10, "has been stopped", "Local probe not found")
        }

        try {
            stopInstances()

            procs += start("start-server", 120, "Started on")
            procs += start("start-probe", 30, "Started on")
            procs += start("info", 10, "Local server")
        }
        finally
            try
                stopInstances()
            finally
                procs.foreach(_.destroy())
    }
}