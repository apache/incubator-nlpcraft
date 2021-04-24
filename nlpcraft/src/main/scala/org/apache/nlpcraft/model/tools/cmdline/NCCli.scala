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

package org.apache.nlpcraft.model.tools.cmdline

import java.io._
import java.lang.ProcessBuilder.Redirect
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util
import java.util.Date
import java.util.regex.Pattern
import java.util.zip.ZipInputStream
import com.google.common.base.CaseFormat

import javax.lang.model.SourceVersion
import javax.net.ssl.SSLException
import org.apache.commons.io.IOUtils
import org.apache.commons.io.input.{ReversedLinesFileReader, Tailer, TailerListenerAdapter}
import org.apache.commons.lang3.SystemUtils
import org.apache.commons.lang3.time.DurationFormatUtils
import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.apache.nlpcraft.common.ansi.{NCAnsi, NCAnsiProgressBar, NCAnsiSpinner}
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common.module.NCModule
import org.jline.reader._
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.DefaultParser.Bracket
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.AttributedString
import org.jline.utils.InfoCmp.Capability
import org.apache.nlpcraft.model.tools.cmdline.NCCliRestSpec._
import org.apache.nlpcraft.model.tools.cmdline.NCCliCommands._
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.compat.Platform.currentTime
import scala.compat.java8.OptionConverters._
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}
import scala.util.control.Exception.ignoring

/**
 * NLPCraft CLI.
 */
object NCCli extends NCCliBase {
    var exitStatus = 0

    var term: Terminal = _

    NCModule.setModule(NCModule.CLI)

    // Project templates for 'gen-project' command.
    private lazy val PRJ_TEMPLATES: Map[String, Seq[String]] = {
        val m = mutable.HashMap.empty[String, Seq[String]]

        try
            managed(new ZipInputStream(U.getStream("cli/templates.zip"))) acquireAndGet { zis ⇒
                var entry = zis.getNextEntry

                while (entry != null) {
                    val buf = new StringWriter

                    IOUtils.copy(zis, buf, StandardCharsets.UTF_8)

                    m += entry.getName → buf.toString.split("\n")

                    entry = zis.getNextEntry
                }
            }
        catch {
            case e: IOException ⇒ throw new NCE(s"Failed to read templates", e)
        }

        m.toMap
    }

    case class HttpRestResponse(
        code: Int,
        data: String
    )

    case class ReplState(
        var isServerOnline: Boolean = false,
        var isProbeOnline: Boolean = false,
        var accessToken: Option[String] = None, // Access token obtain with 'userEmail'.
        var userEmail: Option[String] = None, // Email of the user with 'accessToken'.
        var serverLog: Option[File] = None,
        var probeLog: Option[File] = None,
        var probes: List[Probe] = Nil // List of connected probes.
    ) {
        /**
         * Resets server sub-state.
         */
        def resetServer(): Unit = {
            isServerOnline = false
            accessToken = None
            userEmail = None
            serverLog = None
            probes = Nil
        }

        /**
         * Resets probe sub-state.
         */
        def resetProbe(): Unit = {
            isProbeOnline = false
            probeLog = None
        }
    }

    private val state = ReplState()

    private final val NO_LOGO_CMD = CMDS.find(_.name == "no-logo").get
    private final val NO_ANSI_CMD = CMDS.find(_.name == "no-ansi").get
    private final val ANSI_CMD = CMDS.find(_.name == "ansi").get
    private final val QUIT_CMD = CMDS.find(_.name == "quit").get
    private final val HELP_CMD = CMDS.find(_.name == "help").get
    private final val REST_CMD = CMDS.find(_.name == "rest").get
    private final val CALL_CMD = CMDS.find(_.name == "call").get
    private final val ASK_CMD = CMDS.find(_.name == "ask").get
    private final val SUGSYN_CMD = CMDS.find(_.name == "sugsyn").get
    private final val STOP_SRV_CMD = CMDS.find(_.name == "stop-server").get
    private final val SRV_INFO_CMD = CMDS.find(_.name == "info-server").get
    private final val PRB_INFO_CMD = CMDS.find(_.name == "info-probe").get
    private final val STOP_PRB_CMD = CMDS.find(_.name == "stop-probe").get

    /**
     * @param cmd
     * @param args
     * @param id
     * @param dflt
     */
    @throws[MissingParameter]
    private def getParam(cmd: Command, args: Seq[Argument], id: String, dflt: String = null): String =
        args.find(_.parameter.id == id).flatMap(_.value) match {
            case Some(v) ⇒ v
            case None ⇒
                if (dflt == null)
                    throw MissingParameter(cmd, id)

                dflt
        }

    @throws[InvalidParameter]
    private def getIntParam(cmd: Command, args: Seq[Argument], id: String, dflt: Int): Int = {
        getParamOpt(cmd, args, id) match {
            case Some(num) ⇒
                try
                    Integer.parseInt(num)
                catch {
                    case _: Exception ⇒ throw InvalidParameter(cmd, id)
                }

            case None ⇒ dflt // Default.
        }
    }

    @throws[InvalidParameter]
    private def getDoubleParam(cmd: Command, args: Seq[Argument], id: String, dflt: Double): Double = {
        getParamOpt(cmd, args, id) match {
            case Some(num) ⇒
                try
                    java.lang.Double.parseDouble(num)
                catch {
                    case _: Exception ⇒ throw InvalidParameter(cmd, id)
                }

            case None ⇒ dflt // Default.
        }
    }

    /**
     * @param cmd
     * @param args
     * @param id
     */
    private def getParamOpt(cmd: Command, args: Seq[Argument], id: String): Option[String] =
        args.find(_.parameter.id == id).flatMap(_.value)

    /**
     * @param cmd
     * @param args
     * @param id
     */
    private def isParam(cmd: Command, args: Seq[Argument], id: String): Boolean =
        args.exists(_.parameter.id == id)

    /**
     * @param cmd
     * @param args
     * @param id
     */
    private def getParamOrNull(cmd: Command, args: Seq[Argument], id: String): String =
        args.find(_.parameter.id == id) match {
            case Some(arg) ⇒ U.trimQuotes(arg.value.get)
            case None ⇒ null
        }

    /**
     *
     * @param cmd
     * @param args
     * @param id
     * @return
     */
    private def getFlagParam(cmd: Command, args: Seq[Argument], id: String, dflt: Boolean): Boolean =
        args.find(_.parameter.id == id) match {
            case Some(_) ⇒ true
            case None ⇒ dflt
        }

    /**
     *
     * @param cmd
     * @param args
     * @param id
     * @return
     */
    private def getCpParam(cmd: Command, args: Seq[Argument], id: String): String =
        getParamOpt(cmd, args, id) match {
            case Some(path) ⇒ normalizeCp(U.trimQuotes(path))
            case None ⇒ null
        }

    /**
     *
     * @param cmd
     * @param args
     * @param id
     * @return
     */
    private def getPathParam(cmd: Command, args: Seq[Argument], id: String, dflt: String = null): String = {
        def makePath(p: String): String = {
            val normPath = refinePath(U.trimQuotes(p))

            checkFilePath(normPath)

            normPath
        }

        getParamOpt(cmd, args, id) match {
            case Some(path) ⇒ makePath(path)
            case None ⇒ if (dflt == null) null else makePath(dflt)
        }
    }

    /**
     *
     * @param path
     */
    private def refinePath(path: String): String = {
        require(path != null)

        if (path.nonEmpty && path.head == '~') new File(SystemUtils.getUserHome, path.tail).getAbsolutePath else path
    }

    /**
     *
     * @param path
     */
    private def checkFilePath(path: String): Unit = {
        val file = new File(path)

        if (!file.exists() || !file.isFile)
            throw new IllegalArgumentException(s"File not found: ${c(file.getAbsolutePath)}")
    }

    /**
     * Checks whether given list of models contains class names outside of NLPCraft project.
     *
     * @param mdls Comma-separated list of fully qualified class names for data models.
     * @return
     */
    private def hasExternalModels(mdls: String): Boolean =
        U.splitTrimFilter(mdls, ",").exists(!_.startsWith("org.apache.nlpcraft."))

    /**
     * Handles tilda and checks that every component of the given class path exists relative to the current user working
     * directory of this process.
     *
     * @param cp Classpath to normalize.
     * @return
     */
    private def normalizeCp(cp: String): String =
        U.splitTrimFilter(cp, CP_WIN_NIX_SEPS_REGEX).map(refinePath).map(path ⇒ {
            val normPath = refinePath(path)

            if (!normPath.contains("*") && !new File(normPath).exists())
                throw new IllegalStateException(s"Classpath not found: ${c(path)}")
            else
                normPath
        })
        .mkString(CP_SEP)

    /**
     *
     */
    private def cleanUpTempFiles(): Unit = {
        val tstamp = currentTime - 1000 * 60 * 60 * 24 * 2 // 2 days ago.

        val files = new File(SystemUtils.getUserHome, ".nlpcraft").listFiles()
        
        if (files != null)
            for (file <- files)
                if (file.lastModified() < tstamp) {
                    val name = file.getName
    
                    if (name.startsWith("server_log") || name.startsWith("server_log") || name.startsWith(".pid_"))
                        file.delete()
                }
    }

    /**
     *
     * @param endpoint
     * @return
     */
    private def restHealth(endpoint: String): Int =
        httpGet(endpoint, "health", mkHttpHandler(_.getStatusLine.getStatusCode))

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdRest(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val restPath = getParam(cmd, args, "path") // REST call path (NOT a file system path).
        val json = U.trimQuotes(getParam(cmd, args, "json"))

        httpRest(cmd, restPath, json)
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not running from REPL.
     */
    private [cmdline] def cmdStartServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val cfgPath = getPathParam(cmd, args, "config")
        val igniteCfgPath = getPathParam(cmd, args, "igniteConfig")
        val noWait = getFlagParam(cmd, args, "noWait", dflt = false)
        val timeoutMins = getIntParam(cmd, args, "timeoutMins", 2)
        val jvmOpts = getParamOpt(cmd, args, "jvmopts") match {
            case Some(opts) ⇒ U.splitTrimFilter(U.trimQuotes(opts), " ")
            case None ⇒ Seq("-ea", "-Xms2048m", "-XX:+UseG1GC")
        }

        // Ensure that there isn't another local server running.
        loadServerBeacon() match {
            case Some(b) ⇒ throw new IllegalStateException(
                s"Existing server (pid ${c(b.pid)}) detected. " +
                s"Use ${c("'stop-server'")} command to stop it, if necessary."
            )
            case None ⇒ ()
        }

        val logTstamp = currentTime

        // Server log redirect.
        val output = new File(SystemUtils.getUserHome, s".nlpcraft/server_log_$logTstamp.txt")

        // Store in REPL state right away.
        state.serverLog = Some(output)

        val srvArgs = mutable.ArrayBuffer.empty[String]

        srvArgs += JAVA
        srvArgs ++= jvmOpts

        JVM_OPTS_RT_WARNS.foreach(srvArgs += _)

        srvArgs += "-DNLPCRAFT_ANSI_COLOR_DISABLED=true" // No ANSI colors for text log output to the file.
        srvArgs += "-cp"
        srvArgs += JAVA_CP
        srvArgs += "org.apache.nlpcraft.NCStart"
        srvArgs += "-server"

        if (cfgPath != null)
            srvArgs += s"-config=$cfgPath"
        if (igniteCfgPath != null)
            srvArgs += s"-igniteConfig=$igniteCfgPath"

        val srvPb = new ProcessBuilder(srvArgs.asJava)

        srvPb.directory(new File(USR_WORK_DIR))
        srvPb.redirectErrorStream(true)

        val bleachPb = new ProcessBuilder(
            JAVA,
            "-ea",
            "-cp",
            s"$JAVA_CP",
            "org.apache.nlpcraft.model.tools.cmdline.NCCliAnsiBleach"
        )

        bleachPb.directory(new File(USR_WORK_DIR))
        bleachPb.redirectOutput(Redirect.appendTo(output))

        try {
            // Start the 'server | bleach > server log output' process pipeline.
            val procs = ProcessBuilder.startPipeline(Seq(srvPb, bleachPb).asJava)

            val srvPid = procs.get(0).pid()

            // Store mapping file between PID and timestamp (once we have server PID).
            // Note that the same timestamp is used in server log file.
            ignoring(classOf[IOException]) {
                new File(SystemUtils.getUserHome, s".nlpcraft/.pid_${srvPid}_tstamp_$logTstamp").createNewFile()
            }

            logln(s"Server output: ${c(output.getAbsolutePath)}")

            /**
             *
             */
            def showTip(): Unit = {
                val tbl = new NCAsciiTable()

                tbl += (s"${g("stop-server")}", "Stop the server.")
                tbl += (s"${g("info-server")}", "Get server information.")
                tbl += (s"${g("ping-server")}", "Ping the server.")
                tbl += (s"${g("tail-server")}", "Tail the server log.")

                logln(s"Handy commands:\n${tbl.toString}")
            }

            if (noWait) {
                logln(s"Server is starting...")

                showTip()
            }
            else {
                val progressBar = new NCAnsiProgressBar(
                    term.writer(),
                    NUM_SRV_SERVICES,
                    15,
                    true,
                    // ANSI is NOT disabled & we ARE NOT running from IDEA or Eclipse...
                    NCAnsi.isEnabled && IS_SCRIPT
                )

                log(s"Server is starting ")

                progressBar.start()

                // Tick progress bar "almost" right away to indicate the progress start.
                new Thread(() => {
                    Thread.sleep(1.secs)

                    progressBar.ticked()
                })
                .start()

                val tailer = Tailer.create(
                    state.serverLog.get,
                    new TailerListenerAdapter {
                        override def handle(line: String): Unit = {
                            if (TAILER_PTRN.matcher(line).matches())
                                progressBar.ticked()
                        }
                    },
                    500.ms
                )

                var beacon: NCCliServerBeacon = null
                var online = false
                val endOfWait = currentTime + timeoutMins.mins

                while (currentTime < endOfWait && !online && ProcessHandle.of(srvPid).isPresent) {
                    if (progressBar.completed) {
                        // First, load the beacon, if any.
                        if (beacon == null)
                            beacon = loadServerBeacon(autoSignIn = true).orNull

                        // Once beacon is loaded, ensure that REST endpoint is live.
                        if (beacon != null)
                            online = Try(restHealth(beacon.restUrl) == 200).getOrElse(false)
                    }

                    if (!online)
                        Thread.sleep(2.secs) // Check every 2 secs.
                }

                tailer.stop()
                progressBar.stop()

                if (!online && currentTime >= endOfWait) // Timed out - attempt to kill the timed out process...
                    ProcessHandle.of(srvPid).asScala match {
                        case Some(ph) ⇒
                            if (ph.destroy())
                                error(s"Timed out server process terminated.")

                            if (beacon != null && beacon.beaconPath != null)
                                new File(beacon.beaconPath).delete()

                        case None ⇒ ()
                    }

                if (!online) {
                    logln(r(" [Error]"))

                    error(s"Server start failed - check full log for errors: ${c(output.getAbsolutePath)}")

                    tailFile(output.getAbsolutePath, 20)
                }
                else {
                    logln(g(" [OK]"))

                    logServerInfo(beacon)

                    showTip()
                }
            }
        }
        catch {
            case e: Exception ⇒ error(s"Server failed to start: ${y(e.getLocalizedMessage)}")
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not running from REPL.
     */
    private [cmdline] def cmdTestModel(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        if (loadServerBeacon().isEmpty)
            throw NoLocalServer()

        val cfgPath = getPathParam(cmd, args, "config")
        val addCp = getCpParam(cmd, args, "cp")
        val mdls = getParamOrNull(cmd, args, "models")
        val jvmOpts = getParamOpt(cmd, args, "jvmopts") match {
            case Some(opts) ⇒ U.splitTrimFilter(U.trimQuotes(opts), " ")
            case None ⇒ Seq("-ea", "-Xms1024m")
        }

        if (mdls != null) {
            if (hasExternalModels(mdls) && addCp == null)
                throw new IllegalStateException(
                    s"Additional classpath is required when deploying your own models. " +
                    s"Use ${c("--cp")} parameters to provide additional classpath.")
        }

        if (mdls == null && addCp != null)
            warn(s"Additional classpath (${c("--cp")}) but no models (${c("--models")}).")

        val jvmArgs = mutable.ArrayBuffer.empty[String]

        jvmArgs += JAVA
        jvmArgs ++= jvmOpts

        JVM_OPTS_RT_WARNS.foreach(jvmArgs += _)

        if (cfgPath != null)
            jvmArgs += s"-DNLPCRAFT_PROBE_CONFIG=$cfgPath"

        if (mdls != null)
            jvmArgs += s"-DNLPCRAFT_TEST_MODELS=$mdls"

        if (!NCAnsi.isEnabled)
            jvmArgs += "-DNLPCRAFT_ANSI_COLOR_DISABLED=true"

        jvmArgs += "-cp"

        if (addCp != null)
            jvmArgs += s"$JAVA_CP$CP_SEP$addCp".replace(s"$CP_SEP$CP_SEP", CP_SEP)
        else
            jvmArgs += JAVA_CP

        jvmArgs += "org.apache.nlpcraft.model.tools.test.NCTestAutoModelValidator"

        val validatorPb = new ProcessBuilder(jvmArgs.asJava)

        validatorPb.directory(new File(USR_WORK_DIR))
        validatorPb.inheritIO()

        try {
            validatorPb.start().onExit().get()
        }
        catch {
            case _: InterruptedException ⇒ () // Ignore.
            case e: Exception ⇒ error(s"Failed to run model validator: ${y(e.getLocalizedMessage)}")
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not running from REPL.
     */
    private [cmdline] def cmdStartProbe(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        // Ensure that there is a local server running since probe
        // cannot finish its start unless there's a server to connect to.
        if (loadServerBeacon().isEmpty)
            throw NoLocalServer()

        // Ensure that there isn't another local probe running.
        loadProbeBeacon() match {
            case Some(b) ⇒ throw new IllegalStateException(
                s"Existing probe (pid ${c(b.pid)}) detected. " +
                s"Use ${c("'stop-probe'")} command to stop it, if necessary."
            )
            case None ⇒ ()
        }

        val cfgPath = getPathParam(cmd, args, "config")
        val noWait = getFlagParam(cmd, args, "noWait", dflt = false)
        val addCp = getCpParam(cmd, args, "cp")
        val timeoutMins = getIntParam(cmd, args, "timeoutMins", 1)
        val mdls = getParamOrNull(cmd, args, "models")
        val jvmOpts = getParamOpt(cmd, args, "jvmopts") match {
            case Some(opts) ⇒ U.splitTrimFilter(U.trimQuotes(opts), " ")
            case None ⇒ Seq("-ea", "-Xms1024m")
        }

        if (mdls != null) {
            if (hasExternalModels(mdls) && addCp == null)
                throw new IllegalStateException(
                    s"Additional classpath is required when deploying user models. " +
                    s"Use ${c("--cp")} parameters to provide additional classpath.")
        }

        if (mdls == null && addCp != null)
            warn(s"Additional classpath (${c("--cp")}) specified but no models (${c("--models")}).")

        val logTstamp = currentTime

        // Server log redirect.
        val output = new File(SystemUtils.getUserHome, s".nlpcraft/probe_log_$logTstamp.txt")

        // Store in REPL state right away.
        state.probeLog = Some(output)

        val prbArgs = mutable.ArrayBuffer.empty[String]

        prbArgs += JAVA
        prbArgs ++= jvmOpts

        JVM_OPTS_RT_WARNS.foreach(prbArgs += _)

        prbArgs += "-DNLPCRAFT_ANSI_COLOR_DISABLED=true" // No ANSI colors for text log output to the file.

        if (mdls != null)
            prbArgs += "-Dconfig.override_with_env_vars=true"

        prbArgs += "-cp"
        prbArgs += (if (addCp == null) JAVA_CP else s"$JAVA_CP$CP_SEP$addCp".replace(s"$CP_SEP$CP_SEP", CP_SEP))
        prbArgs += "org.apache.nlpcraft.NCStart"
        prbArgs += "-probe"

        if (cfgPath != null)
            prbArgs += s"-config=$cfgPath"

        val prbPb = new ProcessBuilder(prbArgs.asJava)

        if (mdls != null)
            prbPb.environment().put("CONFIG_FORCE_nlpcraft_probe_models", mdls)

        prbPb.directory(new File(USR_WORK_DIR))
        prbPb.redirectErrorStream(true)

        val bleachPb = new ProcessBuilder(
            JAVA,
            "-ea",
            "-cp",
            JAVA_CP,
            "org.apache.nlpcraft.model.tools.cmdline.NCCliAnsiBleach"
        )

        bleachPb.directory(new File(USR_WORK_DIR))
        bleachPb.redirectOutput(Redirect.appendTo(output))

        try {
            // Start the 'probe | bleach > probe log output' process pipeline.
            val procs = ProcessBuilder.startPipeline(Seq(prbPb, bleachPb).asJava)

            val prbPid = procs.get(0).pid()

            // Store mapping file between PID and timestamp (once we have probe PID).
            // Note that the same timestamp is used in probe log file.
            ignoring(classOf[IOException]) {
                new File(SystemUtils.getUserHome, s".nlpcraft/.pid_${prbPid}_tstamp_$logTstamp").createNewFile()
            }

            logln(s"Probe output: ${c(output.getAbsolutePath)}")

            /**
             *
             */
            def showTip(): Unit = {
                val tbl = new NCAsciiTable()

                tbl += (s"${g("stop-probe")}", "Stop the probe.")
                tbl += (s"${g("info")}", "Get server & probe information.")

                logln(s"Handy commands:\n${tbl.toString}")
            }

            if (noWait) {
                logln(s"Probe is starting...")

                showTip()
            }
            else {
                val progressBar = new NCAnsiProgressBar(
                    term.writer(),
                    NUM_PRB_SERVICES,
                    15,
                    true,
                    // ANSI is NOT disabled & we ARE NOT running from IDEA or Eclipse...
                    NCAnsi.isEnabled && IS_SCRIPT
                )

                log(s"Probe is starting ")

                progressBar.start()

                // Tick progress bar "almost" right away to indicate the progress start.
                new Thread(() => {
                    Thread.sleep(1.secs)

                    progressBar.ticked()
                })
                .start()

                val tailer = Tailer.create(
                    state.probeLog.get,
                    new TailerListenerAdapter {
                        override def handle(line: String): Unit = {
                            if (TAILER_PTRN.matcher(line).matches())
                                progressBar.ticked()
                        }
                    },
                    500.ms
                )

                var beacon: NCCliProbeBeacon = null
                val endOfWait = currentTime + timeoutMins.mins

                while (currentTime < endOfWait && beacon == null && ProcessHandle.of(prbPid).isPresent) {
                    if (progressBar.completed) {
                        // Load the beacon, if any.
                        if (beacon == null)
                            beacon = loadProbeBeacon().orNull
                    }

                    if (beacon == null)
                        Thread.sleep(2.secs) // Check every 2 secs.
                }

                tailer.stop()
                progressBar.stop()

                if (currentTime >= endOfWait)
                    ProcessHandle.of(prbPid).asScala match {
                        case Some(ph) ⇒
                            if (ph.destroy())
                                error(s"Timed out probe process terminated.")

                            if (beacon != null && beacon.beaconPath != null)
                                new File(beacon.beaconPath).delete()

                        case None ⇒ ()
                    }

                if (beacon == null) {
                    logln(r(" [Error]"))

                    error(s"Probe start failed - check full log for errors: ${c(output.getAbsolutePath)}")

                    tailFile(output.getAbsolutePath, 20)
                }
                else {
                    logln(g(" [OK]"))

                    logProbeInfo(beacon)

                    // Reload server beacon to pick up new connected probe.
                    loadServerBeacon()

                    // Log all connected probes (including this one).
                    logConnectedProbes()

                    showTip()
                }
            }
        }
        catch {
            case e: Exception ⇒ error(s"Probe failed to start: ${y(e.getLocalizedMessage)}")
        }
    }

    /**
     *
     * @return
     */
    private def getRestEndpointFromBeacon: String =
        loadServerBeacon() match {
            case Some(beacon) ⇒ beacon.restUrl
            case None ⇒ throw NoLocalServer()
        }

    /**
     *
     * @param path
     * @param lines
     */
    private def tailFile(path: String, lines: Int): Unit =
        try
            managed(new ReversedLinesFileReader(new File(path), StandardCharsets.UTF_8)) acquireAndGet { in ⇒
                var tail = List.empty[String]

                breakable {
                    for (_ ← 0 until lines)
                        in.readLine() match {
                            case null ⇒ break
                            case line ⇒ tail ::= line
                        }
                }

                val cnt = tail.size

                logln(bb(w(s"+----< ${K}Last $cnt log lines $W>---")))
                tail.foreach(line ⇒ logln(s"${bb(w("| "))}  $line"))
                logln(bb(w(s"+----< ${K}Last $cnt log lines $W>---")))
            }
        catch {
            case e: Exception ⇒ error(s"Failed to read log file: ${e.getLocalizedMessage}")
        }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdTailServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val lines = getIntParam(cmd, args, "lines", 20)

        if (lines <= 0)
            throw InvalidParameter(cmd, "lines")

        loadServerBeacon() match {
            case Some(beacon) ⇒ tailFile(beacon.logPath, lines)
            case None ⇒ throw NoLocalServer()
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdTailProbe(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val lines = getIntParam(cmd, args, "lines", 20)

        if (lines <= 0)
            throw InvalidParameter(cmd, "lines")

        loadProbeBeacon() match {
            case Some(beacon) ⇒ tailFile(beacon.logPath, lines)
            case None ⇒ throw NoLocalProbe()
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdPingServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val num = getIntParam(cmd, args, "number", 1)

        var i = 0

        val endpoint = getRestEndpointFromBeacon

        while (i < num) {
            log(s"(${i + 1} of $num) pinging server at ${b(endpoint)} ")

            val spinner = new NCAnsiSpinner(
                term.writer(),
                // ANSI is NOT disabled & we ARE NOT running from IDEA or Eclipse...
                NCAnsi.isEnabled && IS_SCRIPT
            )

            spinner.start()

            val startMs = currentTime

            try
                restHealth(endpoint) match {
                    case 200 ⇒
                        spinner.stop()

                        logln(g("OK") + " " + c(s"[${currentTime - startMs}ms]"))

                    case code: Int ⇒
                        spinner.stop()

                        logln(r("FAIL") + s" [HTTP ${y(code.toString)}]")
                }
            catch {
                case _: SSLException ⇒
                    spinner.stop()

                    logln(r("FAIL") + s" ${y("[SSL error]")}")

                case _: IOException ⇒
                    spinner.stop()

                    logln(r("FAIL") + s" ${y("[I/O error]")}")
            }

            i += 1

            if (i < num)
                // Pause between pings.
                Thread.sleep(500.ms)
        }
    }

    /**
     * Loads and returns server beacon file.
     *
     * @param autoSignIn
     * @return
     */
    private def loadServerBeacon(autoSignIn: Boolean = false): Option[NCCliServerBeacon] = {
        val beaconOpt = try {
            val beacon = (
                managed(
                    new ObjectInputStream(
                        new FileInputStream(
                            new File(SystemUtils.getUserHome, SRV_BEACON_PATH)
                        )
                    )
                ) acquireAndGet {
                    _.readObject()
                }
            )
            .asInstanceOf[NCCliServerBeacon]

            ProcessHandle.of(beacon.pid).asScala match {
                case Some(ph) ⇒
                    beacon.ph = ph

                    // See if we can detect server log if server was started by this script.
                    val files = new File(SystemUtils.getUserHome, ".nlpcraft").listFiles(new FilenameFilter {
                        override def accept(dir: File, name: String): Boolean =
                            name.startsWith(s".pid_$ph")
                    })

                    if (files != null && files.size == 1) {
                        val split = files(0).getName.split("_")

                        if (split.size == 4) {
                            val logFile = new File(SystemUtils.getUserHome, s".nlpcraft/server_log_${split(3)}.txt")

                            if (logFile.exists())
                                beacon.logPath = logFile.getAbsolutePath
                        }
                    }

                    Some(beacon)
                case None ⇒
                    // Attempt to clean up stale beacon file.
                    new File(SystemUtils.getUserHome, SRV_BEACON_PATH).delete()

                    None
            }
        }
        catch {
            case _: Exception ⇒ None
        }

        beaconOpt match {
            case Some(beacon) ⇒
                state.isServerOnline = true

                try {
                    // Attempt to sign in with the default account.
                    if (autoSignIn && state.accessToken.isEmpty)
                        httpPostResponseJson(
                            beacon.restUrl,
                            "signin",
                            s"""{"email": "$DFLT_USER_EMAIL", "passwd": "$DFLT_USER_PASSWD"}""") match {
                            case Some(json) ⇒
                                Option(Try(U.getJsonStringField(json, "acsTok"))) match {
                                    case Some(tok) ⇒
                                        state.userEmail = Some(DFLT_USER_EMAIL)
                                        state.accessToken = Some(tok.get)
                                    case None ⇒
                                        state.userEmail = None
                                        state.accessToken = None

                                }
                            case None ⇒ ()
                        }

                    // Attempt to get all connected probes if successfully signed in prior.
                    if (state.accessToken.isDefined)
                        httpPostResponseJson(
                            beacon.restUrl,
                            "probe/all",
                            "{\"acsTok\": \"" + state.accessToken.get + "\"}") match {
                            case Some(json) ⇒ state.probes =
                                Try(
                                    U.jsonToObject[ProbeAllResponse](json, classOf[ProbeAllResponse]).probes.toList
                                ).getOrElse(Nil)
                            case None ⇒ ()
                        }
                }
                catch {
                    case _: Exception ⇒
                        // Reset REPL state.
                        state.resetServer()
                }

            case None ⇒
                // Reset REPL state.
                state.resetServer()
        }

        beaconOpt
    }

    /**
     * Loads and returns probe beacon file.
     *
     * @return
     */
    private def loadProbeBeacon(): Option[NCCliProbeBeacon] = {
        val beaconOpt = try {
            val beacon = (
                managed(
                    new ObjectInputStream(
                        new FileInputStream(
                            new File(SystemUtils.getUserHome, PRB_BEACON_PATH)
                        )
                    )
                ) acquireAndGet {
                    _.readObject()
                }
            )
            .asInstanceOf[NCCliProbeBeacon]

            ProcessHandle.of(beacon.pid).asScala match {
                case Some(ph) ⇒
                    beacon.ph = ph

                    // See if we can detect probe log if server was started by this script.
                    val files = new File(SystemUtils.getUserHome, ".nlpcraft").listFiles(new FilenameFilter {
                        override def accept(dir: File, name: String): Boolean =
                            name.startsWith(s".pid_$ph")
                    })

                    if (files != null && files.size == 1) {
                        val split = files(0).getName.split("_")

                        if (split.size == 4) {
                            val logFile = new File(SystemUtils.getUserHome, s".nlpcraft/probe_log_${split(3)}.txt")

                            if (logFile.exists())
                                beacon.logPath = logFile.getAbsolutePath
                        }
                    }

                    Some(beacon)

                case None ⇒
                    // Attempt to clean up stale beacon file.
                    new File(SystemUtils.getUserHome, PRB_BEACON_PATH).delete()

                    None
            }
        }
        catch {
            case _: Exception ⇒ None
        }

        beaconOpt match {
            case Some(_) ⇒ state.isProbeOnline = true
            case None ⇒ state.resetProbe()
        }

        beaconOpt
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdQuit(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        if (repl) {
            loadServerBeacon() match {
                case Some(b) ⇒ warn(s"Local server (pid ${c(b.pid)}) is still running.")
                case None ⇒ ()
            }

            loadProbeBeacon() match {
                case Some(b) ⇒ warn(s"Local probe (pid ${c(b.pid)}) is still running.")
                case None ⇒ ()
            }
        }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdStop(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        doCommand(Seq(STOP_SRV_CMD.name), repl)
        doCommand(Seq(STOP_PRB_CMD.name), repl)
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdStopServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        loadServerBeacon() match {
            case Some(beacon) ⇒
                val pid = beacon.pid

                if (beacon.ph.destroy()) {
                    logln(s"Server (pid ${c(pid)}) has been stopped.")

                    // Attempt to delete beacon file right away.
                    new File(beacon.beaconPath).delete()

                    // Reset REPL state right away.
                    state.resetServer()
                }
                else
                    error(s"Failed to stop the local server (pid ${c(pid)}).")

            case None ⇒ throw NoLocalServer()
        }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdStopProbe(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        loadProbeBeacon() match {
            case Some(beacon) ⇒
                val pid = beacon.pid

                if (beacon.ph.destroy()) {
                    logln(s"Probe (pid ${c(pid)}) has been stopped.")

                    // Attempt to delete beacon file right away.
                    new File(beacon.beaconPath).delete()

                    // Reset REPL state right away.
                    state.resetProbe()
                }
                else
                    error(s"Failed to stop the local probe (pid ${c(pid)}).")

            case None ⇒ throw NoLocalProbe()
        }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdNoLogo(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        warn("This command should be used together with other command in a command line mode.")
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdNoAnsi(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        NCAnsi.setEnabled(false)

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdAnsi(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        NCAnsi.setEnabled(true)

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdHelp(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        /**
         *
         */
        def header(): Unit = logln(
            s"""|${ansiBold("NAME")}
                |$T___${y(s"'$SCRIPT_NAME'")} - command line interface to control NLPCraft.
                |
                |${ansiBold("USAGE")}
                |$T___${y(s"'$SCRIPT_NAME'")} [COMMAND] [PARAMETERS]
                |
                |${T___}Without any arguments the script starts in REPL mode. The REPL mode supports all
                |${T___}the same commands as command line mode. In REPL mode you need to put values that
                |${T___}can have spaces (like JSON or file paths) inside of single or double quotes both
                |${T___}of which can be escaped using '\\' character.
                |
                |${ansiBold("COMMANDS")}""".stripMargin
        )

        /**
         *
         * @param cmd
         * @return
         */
        def mkCmdLines(cmd: Command): Seq[String] = {
            val lines = mutable.Buffer.empty[String]

            if (cmd.desc.isDefined)
                lines += cmd.synopsis + " " + cmd.desc.get
            else
                lines += cmd.synopsis

            if (cmd.params.nonEmpty) {
                lines += ""
                lines += ansiBold("PARAMETERS")

                for (param ← cmd.params) {
                    val line =
                        if (param.value.isDefined)
                            T___ + param.names.zip(Stream.continually(param.value.get)).map(t ⇒ s"${t._1}=${t._2}").mkString(", ")
                        else
                            s"$T___${param.names.mkString(", ")}"

                    lines += c(line)

                    if (param.optional)
                        lines += s"$T___$T___${g("Optional.")}"

                    lines += s"$T___$T___${param.desc}"
                    lines += ""
                }

                lines.remove(lines.size - 1) // Remove last empty line.
            }

            if (cmd.examples.nonEmpty) {
                lines += ""
                lines += ansiBold("EXAMPLES")

                for (ex ← cmd.examples) {
                    lines ++= ex.usage.map(s ⇒ y(s"$T___$s"))
                    lines += s"$T___$T___${ex.desc}"
                }
            }

            lines
        }

        def helpHelp(): Unit =
            logln(s"" +
                s"\n" +
                s"Type ${c("help --cmd=xxx")} to get help for ${c("xxx")} command.\n" +
                s"\n" +
                s"You can also execute any OS specific command by prepending '${c("$")}' in front of it:\n" +
                s"  ${y("> $cmd /c dir")}\n" +
                s"    Runs Windows ${c("dir")} command in a separate shell.\n" +
                s"  ${y("> $ls -la")}\n" +
                s"    Runs Linux/Unix ${c("ls -la")} command.\n"
            )

        if (args.isEmpty) { // Default - show abbreviated help.
            if (!repl)
                header()

            CMDS.groupBy(_.group).toSeq.sortBy(_._1).foreach(entry ⇒ {
                val grp = entry._1
                val grpCmds = entry._2

                val tbl = NCAsciiTable().margin(left = if (repl) 0 else 4)

                grpCmds.sortBy(_.name).foreach(cmd ⇒ tbl +/ (
                    "" → s"${g(cmd.name)}",
                    "align:left, maxWidth:85" → cmd.synopsis
                ))

                logln(s"\n$B$grp:$RST\n${tbl.toString}")
            })

            helpHelp()
        }
        else if (args.size == 1 && args.head.parameter.id == "all") { // Show a full format help for all commands.
            if (!repl)
                header()

            val tbl = NCAsciiTable().margin(left = if (repl) 0 else 4)

            CMDS.foreach(cmd ⇒
                tbl +/ (
                    "" → s"${g(cmd.name)}",
                    "align:left, maxWidth:85" → mkCmdLines(cmd)
                )
            )

            logln(tbl.toString)

            helpHelp()
        }
        else { // Help for individual commands.
            var err = false
            val seen = mutable.Buffer.empty[String]

            val tbl = NCAsciiTable().margin(left = if (repl) 0 else 4)

            for (arg ← args) {
                val cmdName = arg.value.get

                CMDS.find(_.name == cmdName) match {
                    case Some(c) ⇒
                        if (!seen.contains(c.name)) {
                            tbl +/ (
                                "" → s"${g(c.name)}",
                                "align:left, maxWidth:85" → mkCmdLines(c)
                            )

                            seen += c.name
                        }
                    case None ⇒
                        err = true

                        throw UnknownCommand(cmdName)
                }
            }

            if (!err) {
                if (!repl)
                    header()

                logln(tbl.toString)
            }
        }
    }

    /**
     *
     * @param beacon
     * @return
     */
    private def logProbeInfo(beacon: NCCliProbeBeacon): Unit = {
        val tbl = new NCAsciiTable

        val logPath = if (beacon.logPath != null) g(beacon.logPath) else y("<not available>")
        val jarsFolder = if (beacon.jarsFolder != null) g(beacon.jarsFolder) else y("<not set>")
        val mdlSeq = beacon.models.split(",").map(s ⇒ s"${g(s.strip)}").toSeq

        tbl += ("PID", s"${g(beacon.pid)}")
        tbl += ("Probe ID", s"${g(beacon.id)}")
        tbl += ("Probe Up-Link", s"${g(beacon.upLink)}")
        tbl += ("Probe Down-Link", s"${g(beacon.downLink)}")
        tbl += ("JARs Folder", jarsFolder)
        tbl += (s"Deployed Models (${mdlSeq.size})", mdlSeq)
        tbl += ("Log file", logPath)
        tbl += ("Started on", s"${g(DateFormat.getDateTimeInstance.format(new Date(beacon.startMs)))}")

        logln(s"Local probe:\n${tbl.toString}")
    }

    /**
     *
     * @param beacon
     * @return
     */
    private def logServerInfo(beacon: NCCliServerBeacon): Unit = {
        val tbl = new NCAsciiTable

        val logPath = if (beacon.logPath != null) g(beacon.logPath) else y("<not available>")

        tbl += ("PID", s"${g(beacon.pid)}")
        tbl += ("Database:", "")
        tbl += ("  URL", s"${g(beacon.dbUrl)}")
        tbl += ("  Driver", s"${g(beacon.dbDriver)}")
        tbl += ("  Pool min", s"${g(beacon.dbPoolMin)}")
        tbl += ("  Pool init", s"${g(beacon.dbPoolInit)}")
        tbl += ("  Pool max", s"${g(beacon.dbPoolMax)}")
        tbl += ("  Pool increment", s"${g(beacon.dbPoolInc)}")
        tbl += ("  Reset on start", s"${g(beacon.dbInit)}")
        tbl += ("REST:", "")
        tbl += ("  Endpoint", s"${g(beacon.restUrl)}")
        tbl += ("  API provider", s"${g(beacon.restApi)}")
        tbl += ("Probe:", "")
        tbl += ("  Uplink", s"${g(beacon.upLink)}")
        tbl += ("  Downlink", s"${g(beacon.downLink)}")
        tbl += ("Token providers", s"${g(beacon.tokenProviders)}")
        tbl += ("NLP engine", s"${g(beacon.nlpEngine)}")
        tbl += ("Access tokens:", "")
        tbl += ("  Scan frequency", s"$G${beacon.acsToksScanMins} mins$RST")
        tbl += ("  Expiration timeout", s"$G${beacon.acsToksExpireMins} mins$RST")
        tbl += ("External config:", "")
        tbl += ("  URL", s"${g(beacon.extConfigUrl)}")
        tbl += ("  Check MD5", s"${g(beacon.extConfigCheckMd5)}")
        tbl += ("Log file", logPath)
        tbl += ("Started on", s"${g(DateFormat.getDateTimeInstance.format(new Date(beacon.startMs)))}")

        logln(s"Local server:\n${tbl.toString}")

        logConnectedProbes()

        logSignedInUser()
    }

    /**
     *
     */
    private def logSignedInUser(): Unit = {
        if (state.accessToken.isDefined) {
            val tbl = new NCAsciiTable()

            tbl += (s"${g("Email")}", state.userEmail.get)
            tbl += (s"${g("Access token")}", state.accessToken.get)

            logln(s"Signed in user account:\n$tbl")
        }
    }

    /**
     *
     */
    private def logConnectedProbes(): Unit = {
        val tbl = new NCAsciiTable

        tbl #= (
            "Probe ID",
            "Uptime",
            "Host / OS",
            "Model IDs"
        )

        def addProbeToTable(tbl: NCAsciiTable, probe: Probe): NCAsciiTable = {
            tbl += (
                Seq(
                    probe.probeId,
                    s"  ${c("guid")}: ${probe.probeGuid}",
                    s"  ${c("tok")}: ${probe.probeToken}"
                ),
                DurationFormatUtils.formatDurationHMS(currentTime - probe.startTstamp),
                Seq(
                    s"${probe.hostName} (${probe.hostAddr})",
                    s"${probe.osName} ver. ${probe.osVersion}"
                ),
                probe.models.toList.map(m ⇒ s"${b(m.id)}, v${m.version}")
            )

            tbl
        }

        state.probes.foreach(addProbeToTable(tbl, _))

        logln(s"All server connected probes (${state.probes.size}):\n${tbl.toString}")
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdInfo(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        doCommand(Seq(SRV_INFO_CMD.name), repl)
        doCommand(Seq(PRB_INFO_CMD.name), repl)
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdInfoServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        loadServerBeacon() match {
            case Some(beacon) ⇒ logServerInfo(beacon)
            case None ⇒ throw NoLocalServer()
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdInfoProbe(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        loadProbeBeacon() match {
            case Some(beacon) ⇒ logProbeInfo(beacon)
            case None ⇒ throw NoLocalProbe()
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdClear(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        term.puts(Capability.clear_screen)

    /**
     *
     * @param body
     * @return
     */
    private def mkHttpHandler[T](body: HttpResponse ⇒ T): ResponseHandler[T] =
        (resp: HttpResponse) => body(resp)

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdSignIn(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        state.accessToken match {
            case None ⇒
                val email = getParam(cmd, args, "email", DFLT_USER_EMAIL)
                val passwd = getParam(cmd, args, "passwd", DFLT_USER_PASSWD)

                httpRest(
                    cmd,
                    "signin",
                    s"""
                       |{
                       |    "email": ${jsonQuote(email)},
                       |    "passwd": ${jsonQuote(passwd)}
                       |}
                       |""".stripMargin
                )

            case Some(_) ⇒ warn(s"Already signed in. Use ${c("'signout'")} command to sign out first, if necessary.")
        }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdSignOut(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        state.accessToken match {
            case Some(acsTok) ⇒
                httpRest(
                    cmd,
                    "signout",
                    s"""
                       |{"acsTok": ${jsonQuote(acsTok)}}
                       |""".stripMargin
                )

            case None ⇒ throw NotSignedIn()
        }

    /**
     * Quotes given string in double quotes unless it is already quoted as such.
     *
     * @param s
     * @return
     */
    private def jsonQuote(s: String): String = {
        if (s == null)
            null
        else {
            val ss = s.trim()

            if (ss.startsWith("\"") && ss.endsWith("\""))
                ss
            else
                s""""$ss""""
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdSqlGen(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        // Mandatory parameters check (unless --help is specified).
        if (!isParam(cmd, args, "help")) {
            getParam(cmd, args, "driver")
            getParam(cmd, args, "schema")
            getParam(cmd, args, "out")
            getParam(cmd, args, "url")
        }

        val addCp = getCpParam(cmd, args, "cp")
        val jvmOpts = getParamOpt(cmd, args, "jvmopts") match {
            case Some(opts) ⇒ U.splitTrimFilter(U.trimQuotes(opts), " ")
            case None ⇒ Seq("-ea", "-Xms1024m")
        }

        val jvmArgs = mutable.ArrayBuffer.empty[String]

        jvmArgs += JAVA
        jvmArgs ++= jvmOpts

        JVM_OPTS_RT_WARNS.foreach(jvmArgs += _)

        if (!NCAnsi.isEnabled)
            jvmArgs += "-DNLPCRAFT_ANSI_COLOR_DISABLED=true"

        jvmArgs += "-cp"

        if (addCp != null)
            jvmArgs += s"$JAVA_CP$CP_SEP$addCp".replace(s"$CP_SEP$CP_SEP", CP_SEP)
        else
            jvmArgs += JAVA_CP

        jvmArgs += "org.apache.nlpcraft.model.tools.sqlgen.NCSqlModelGenerator"

        for (arg ← args)
            if (arg.parameter.id != "cp" && arg.parameter.id != "jvmopts") {
                val p = arg.parameter.names.head

                arg.value match {
                    case None ⇒ jvmArgs += p
                    case Some(_) ⇒ jvmArgs += s"$p=${arg.value.get}"
                }
            }

        val pb = new ProcessBuilder(jvmArgs.asJava)

        pb.directory(new File(USR_WORK_DIR))
        pb.inheritIO()

        try {
            pb.start().onExit().get()
        }
        catch {
            case _: InterruptedException ⇒ () // Ignore.
            case e: Exception ⇒ error(s"Failed to run SQL model generator: ${y(e.getMessage)}")
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdSugSyn(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        state.accessToken match {
            case Some(acsTok) ⇒
                val mdlId = getParam(cmd, args, "mdlId")
                val minScore = getDoubleParam(cmd, args, "minScore", 0.5)

                httpRest(
                    cmd,
                    "model/sugsyn",
                    s"""
                       |{
                       |    "acsTok": ${jsonQuote(acsTok)},
                       |    "mdlId": ${jsonQuote(mdlId)},
                       |    "minScore": $minScore
                       |}
                       |""".stripMargin
                )

            case None ⇒ throw NotSignedIn()
        }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdAsk(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        state.accessToken match {
            case Some(acsTok) ⇒
                val mdlId = getParam(cmd, args, "mdlId")
                val txt = getParam(cmd, args, "txt")
                val data = getParamOrNull(cmd, args, "data")
                val enableLog = getFlagParam(cmd, args, "enableLog", dflt = false)

                httpRest(
                    cmd,
                    "ask/sync",
                    s"""
                       |{
                       |    "acsTok": ${jsonQuote(acsTok)},
                       |    "mdlId": ${jsonQuote(mdlId)},
                       |    "txt": ${jsonQuote(txt)},
                       |    "data": ${jsonQuote(data)},
                       |    "enableLog": $enableLog
                       |}
                       |""".stripMargin
                )

            case None ⇒ throw NotSignedIn()
        }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdCall(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val normArgs = args.filter(!_.parameter.synthetic)
        val synthArgs = args.filter(_.parameter.synthetic)

        val path = normArgs.find(_.parameter.id == "path").getOrElse(throw MissingParameter(cmd, "path")).value.get

        var first = true
        val buf = new StringBuilder()

        val spec = REST_SPEC.find(_.path == path).getOrElse(throw InvalidParameter(cmd, "path"))

        var mandatoryParams = spec.params.filter(!_.optional)

        for (arg ← synthArgs) {
            val jsName = arg.parameter.id

            spec.params.find(_.name == jsName) match {
                case Some(param) ⇒
                    mandatoryParams = mandatoryParams.filter(_.name != jsName)

                    if (!first)
                        buf ++= ","

                    first = false

                    buf ++= "\"" + jsName + "\":"

                    val value = arg.value.getOrElse(throw InvalidJsonParameter(cmd, arg.parameter.names.head))

                    param.kind match {
                        case STRING ⇒ buf ++= "\"" + U.escapeJson(U.trimQuotes(value)) + "\""
                        case OBJECT | ARRAY ⇒ buf ++= U.trimQuotes(value)
                        case BOOLEAN | NUMERIC ⇒ buf ++= value
                    }

                case None ⇒ throw InvalidJsonParameter(cmd, jsName)
            }
        }

        if (mandatoryParams.nonEmpty)
            throw MissingMandatoryJsonParameters(cmd, mandatoryParams, path)

        httpRest(cmd, path, s"{${buf.toString()}}")
    }

    /**
      *
      * @param cmd
      * @param name
      * @param value
      * @param supported
      */
    @throws[InvalidParameter]
    private def checkSupported(cmd: Command, name: String, value: String, supported: String*): Unit =
        if (!supported.contains(value))
            throw InvalidParameter(cmd, name)

    /**
      *
      * @param lines
      * @param cmtBegin Comment begin sequence.
      * @param cmtEnd Comment end sequence.
      */
    private def extractHeader0(lines: Seq[String], cmtBegin: String, cmtEnd: String): (Int, Int) = {
        var startIdx, endIdx = -1

        for ((line, idx) ← lines.zipWithIndex if startIdx == -1 || endIdx == -1) {
            val t = line.trim

            if (t == cmtBegin) {
                if (startIdx == -1)
                    startIdx = idx
            }
            else if (t == cmtEnd) {
                if (startIdx != -1 && endIdx == -1)
                    endIdx = idx
            }
        }

        if (startIdx == -1) (-1, -1) else (startIdx, endIdx)
    }

    /**
      *
      * @param lines
      * @param cmtBegin One-line comment begin sequence.
      */
    private def extractHeader0(lines: Seq[String], cmtBegin: String = "#"): (Int, Int) = {
        var startIdx, endIdx = -1

        for ((line, idx) ← lines.zipWithIndex if startIdx == -1 || endIdx == -1)
            if (line.trim.startsWith(cmtBegin)) {
                if (startIdx == -1)
                    startIdx = idx
            }
            else {
                if (startIdx != -1 && endIdx == -1) {
                    require(idx > 0)

                    endIdx = idx - 1
                }
            }

        if (startIdx == -1)
            (-1, -1)
        else if (endIdx == -1)
            (startIdx, lines.size - 1)
        else
            (startIdx, endIdx)
    }

    def extractJavaHeader(lines: Seq[String]): (Int, Int) = extractHeader0(lines, "/*", "*/")
    def extractJsonHeader(lines: Seq[String]): (Int, Int) = extractHeader0(lines, "/*", "*/")
    def extractGradleHeader(lines: Seq[String]): (Int, Int) = extractHeader0(lines, "/*", "*/")
    def extractSbtHeader(lines: Seq[String]): (Int, Int) = extractHeader0(lines, "/*", "*/")
    def extractXmlHeader(lines: Seq[String]): (Int, Int) = extractHeader0(lines, "<!--", "-->")
    def extractYamlHeader(lines: Seq[String]): (Int, Int) = extractHeader0(lines)
    def extractPropertiesHeader(lines: Seq[String]): (Int, Int) = extractHeader0(lines)

    /**
      *
      * @param zipInDir
      * @param dst
      * @param inEntry
      * @param outEntry
      * @param repls
      */
    @throws[NCE]
    private def copy(
        zipInDir: String,
        dst: File,
        inEntry: String,
        outEntry: String,
        extractHeader: Option[Seq[String] ⇒ (Int, Int)],
        repls: (String, String)*
    ) {
        val key = s"$zipInDir/$inEntry"

        require(PRJ_TEMPLATES.contains(key), s"Unexpected template entry for: $key")

        var lines = PRJ_TEMPLATES(key)

        val outFile = if (dst != null) new File(dst, outEntry) else new File(outEntry)
        val parent = outFile.getAbsoluteFile.getParentFile

        if (parent == null || !parent.exists() && !parent.mkdirs())
            throw new NCE(s"Invalid folder: ${parent.getAbsolutePath}")

        // Drops headers.
        extractHeader match {
            case Some(ext) ⇒
                val (hdrFrom, hdrTo) = ext(lines)

                lines = lines.zipWithIndex.flatMap {
                    case (line, idx) ⇒ if (idx < hdrFrom || idx > hdrTo) Some(line) else None
                }
            case None ⇒ // No-op.
        }

        // Drops empty line in begin and end of the file.
        lines = lines.dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse

        val buf = mutable.ArrayBuffer.empty[(String, String)]

        for (line ← lines) {
            val t = line.trim

            // Drops duplicated empty lines, which can be appeared because header deleting.
            if (buf.isEmpty || t.nonEmpty || t != buf.last._2)
                buf += (line → t)
        }

        var cont = buf.map(_._1).mkString("\n")

        cont = repls.foldLeft(cont)((s, repl) ⇒ s.replaceAll(repl._1, repl._2))

        try
            managed(new FileWriter(outFile)) acquireAndGet { w ⇒
                managed(new BufferedWriter(w)) acquireAndGet { bw ⇒
                    bw.write(cont)
                }
            }
        catch {
            case e: IOException ⇒ throw new NCE(s"Error writing $outEntry", e)
        }
    }

    /**
      *
      * @param cmd Command descriptor.
      * @param args Arguments, if any, for this command.
      * @param repl Whether or not executing from REPL.
      */
    private [cmdline] def cmdGenModel(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val filePath = refinePath(getParam(cmd, args, "filePath"))
        val overrideFlag = getFlagParam(cmd, args, "override", dflt = false)
        val mdlId = getParam(cmd, args, "modelId")

        val out = new File(filePath)

        if (out.isDirectory)
            throw new IllegalArgumentException(s"Invalid file path: ${c(out.getAbsolutePath)}")

        if (out.exists()) {
            if (overrideFlag) {
                if (!out.delete())
                    throw new IllegalArgumentException(s"Couldn't delete file: ${c(out.getAbsolutePath)}")
            }
            else
                throw new IllegalArgumentException(s"File already exists: ${c(out.getAbsolutePath)}")
        }

        val (fileExt, extractHdr) = {
            val lc = filePath.toLowerCase

            if (lc.endsWith(".yaml") || lc.endsWith(".yml"))
                ("yaml", extractYamlHeader _)
            else if (lc.endsWith(".json") || lc.endsWith(".js"))
                ("json", extractJsonHeader _)
            else
                throw new IllegalArgumentException(s"Unsupported model file type (extension): ${c(filePath)}")
        }

        copy(
            "nlpcraft-java-mvn",
            out.getParentFile,
            s"src/main/resources/template_model.$fileExt",
            out.getName,
            Some(extractHdr),
            "templateModelId" → mdlId
        )

        logln(s"Model file stub created: ${c(out.getCanonicalPath)}")
    }

    /**
      *
      * @param cmd Command descriptor.
      * @param args Arguments, if any, for this command.
      * @param repl Whether or not executing from REPL.
      */
    private [cmdline] def cmdGenProject(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val outputDir = refinePath(getParam(cmd, args, "outputDir", USR_WORK_DIR))
        val baseName = getParam(cmd, args, "baseName")
        val lang = getParam(cmd, args, "lang", "java").toLowerCase
        val buildTool = getParam(cmd, args, "buildTool", "mvn").toLowerCase
        val pkgName = getParam(cmd, args, "packageName", "org.apache.nlpcraft.demo").toLowerCase
        val fileType = getParam(cmd, args, "modelType", "yaml").toLowerCase
        val overrideFlag = getFlagParam(cmd, args, "override", dflt = false)

        val dst = new File(outputDir, baseName)
        val pkgDir = pkgName.replaceAll("\\.", "/")
        val clsName = s"${baseName.head.toUpper}${baseName.tail}"
        val variant = s"$lang-$buildTool"
        val inFolder = s"nlpcraft-$variant"
        val isJson = fileType == "json" || fileType == "js"

        checkSupported(cmd, "lang", lang, "java", "scala", "kotlin")
        checkSupported(cmd, "buildTool", buildTool, "mvn", "gradle", "sbt")
        checkSupported(cmd, "fileType", fileType, "yaml", "yml", "json", "js")

        def checkJavaName(v: String, name: String): Unit =
            if (!SourceVersion.isName(v))
                throw InvalidParameter(cmd, name)

        checkJavaName(clsName, "baseName")
        checkJavaName(pkgName, "packageName")

        // Prepares output folder.
        if (dst.isFile)
            throw new IllegalArgumentException(s"Invalid output folder: ${c(dst.getAbsolutePath)}")
        else {
            if (!dst.exists()) {
                if (!dst.mkdirs())
                    throw new IllegalArgumentException(s"Failed to create folder: ${c(dst.getAbsolutePath)}")
            }
            else {
                if (overrideFlag)
                    U.clearFolder(dst.getAbsolutePath)
                else
                    throw new IllegalArgumentException(s"Folder already exists (use ${c("'-o'")} to override): ${c(dst.getAbsolutePath)}")
            }
        }

        @throws[NCE]
        def cp(in: String, extractHeader: Option[Seq[String] ⇒ (Int, Int)], repls: (String, String)*): Unit =
            copy(inFolder, dst, in, in, extractHeader, repls :_*)

        @throws[NCE]
        def cpAndRename(in: String, out: String, extractHdr: Option[Seq[String] ⇒ (Int, Int)], repls: (String, String)*): Unit =
            copy(inFolder, dst, in, out, extractHdr, repls :_*)

        @throws[NCE]
        def cpCommon(langDir: String, langExt: String): Unit = {
            cp(".gitignore", None)

            val (startClause, exampleClause) =
                langExt match {
                    case "java" ⇒ (s"NCEmbeddedProbe.start($clsName.class);", "Java example")
                    case "kt" ⇒ (s"NCEmbeddedProbe.start($clsName::class.java)", "Kotlin example")
                    case "scala" ⇒ (s"NCEmbeddedProbe.start(classOf[$clsName])", "Scala example")

                    case  _ ⇒ throw new AssertionError(s"Unexpected language extension: $langExt")
                }

            cp(
                "readme.txt",
                None,
                "com.company.nlp.TemplateModel" → s"$pkgName.$clsName",
                "NCEmbeddedProbe.start\\(TemplateModel.class\\);" → startClause,
                "Java example" → exampleClause,
                "templateModelId" → baseName
            )

            val resFileName =
                if (baseName.contains("_")) baseName else CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, baseName)

            cpAndRename(
                s"src/main/$langDir/com/company/nlp/TemplateModel.$langExt",
                s"src/main/$langDir/$pkgDir/$clsName.$langExt",
                // Suitable for all supported languages.
                Some(extractJavaHeader),
                "com.company.nlp" → s"$pkgName",
                "TemplateModel" → clsName,
                "template_model.yaml" → s"$resFileName.$fileType"
            )
            cpAndRename(
                s"src/main/resources/template_model.${if (isJson) "json" else "yaml"}",
                s"src/main/resources/$resFileName.$fileType",
                Some(if (isJson) extractJsonHeader else extractYamlHeader),
                "templateModelId" → baseName
            )
        }

        @throws[NCE]
        def cpPom(): Unit =
            cp(
                "pom.xml",
                Some(extractXmlHeader),
                "com.company.nlp" → pkgName,
                "myapplication" → baseName,
                "<nlpcraft.ver>(.*)</nlpcraft.ver>" → s"<nlpcraft.ver>${VER.version}</nlpcraft.ver>"
            )

        @throws[NCE]
        def cpGradle(): Unit = {
            cp("build.gradle",
                Some(extractGradleHeader),
                "com.company.nlp" → pkgName,
                "myapplication" → baseName,
                "'org.apache.nlpcraft:nlpcraft:(.*)'" → s"'org.apache.nlpcraft:nlpcraft:${VER.version}'"
            )
            cp(
                "settings.gradle",
                Some(extractGradleHeader),
                "myapplication" → baseName
            )
            cp("gradlew", None)
            cp("gradlew.bat", None)
        }

        @throws[NCE]
        def cpSbt(): Unit = {
            cp("build.sbt",
                Some(extractSbtHeader),
                "com.company.nlp" → pkgName,
                "myapplication" → baseName,
                (s"""libraryDependencies""" + " \\+= " + """"org.apache.nlpcraft" % "nlpcraft" % "(.*)"""") →
                (s"""libraryDependencies""" + " \\+= " +  s""""org.apache.nlpcraft" % "nlpcraft" % "${VER.version}"""")
            )
            cp("project/build.properties", Some(extractPropertiesHeader))
        }

        def folder2String(dir: File): String = {
            val sp = System.getProperty("line.separator")

            def get(f: File): List[StringBuilder] = {
                val name = if (f.isFile) s"${y(f.getName)}" else f.getName

                val buf = mutable.ArrayBuffer.empty[StringBuilder] :+ new StringBuilder().append(name)

                val children = {
                    val list = f.listFiles()

                    if (list == null) List.empty else list.sortBy(_.getName).toList
                }

                for {
                    child ← children
                    (v1, v2) = if (child != children.last) (s"${c("|")}-- ", s"${c("|")}   ") else (s"${c("+")}-- ", "    ")
                    sub = get(child)
                } {
                    buf += sub.head.insert(0, v1)
                    sub.tail.foreach(p ⇒ buf += p.insert(0, v2))
                }

                buf.toList
            }

            get(dir).map(line ⇒ s"$line$sp").mkString
        }

        try {
            variant match {
                case "java-mvn" ⇒ cpCommon("java", "java"); cpPom()
                case "java-gradle" ⇒ cpCommon("java", "java"); cpGradle()

                case "kotlin-mvn" ⇒ cpCommon("kotlin", "kt"); cpPom()
                case "kotlin-gradle" ⇒ cpCommon("kotlin", "kt"); cpGradle()

                case "scala-mvn" ⇒ cpCommon("scala", "scala"); cpPom()
                case "scala-gradle" ⇒ cpCommon("scala", "scala"); cpGradle()
                case "scala-sbt" ⇒ cpCommon("scala", "scala"); cpSbt()

                case _ ⇒ throw new IllegalArgumentException(s"Unsupported combination of '${c(lang)}' and '${c(buildTool)}'.")
            }

            logln(s"Project created in: ${c(dst.getCanonicalPath)}")
            logln(folder2String(dst))
        }
        catch {
            case e: Exception ⇒
                try
                    U.clearFolder(dst.getAbsolutePath, delFolder = true)
                catch {
                    case _: Exception ⇒ // No-op.
                }

                throw e
        }
    }

    /**
     *
     * @param cmd
     * @param path
     * @param json
     */
    private def httpRest(cmd: Command, path: String, json: String): Unit = {
        if (!U.isValidJson(json))
            throw MalformedJson()

        if (!REST_SPEC.exists(_.path == path))
            throw InvalidParameter(cmd, "path")

        val spinner = new NCAnsiSpinner(
            term.writer(),
            // ANSI is NOT disabled & we ARE NOT running from IDEA or Eclipse...
            NCAnsi.isEnabled && IS_SCRIPT
        )

        spinner.start()

        // Make the REST call.
        val resp =
            try
                httpPostResponse(getRestEndpointFromBeacon, path, json)
            finally
                spinner.stop()

        // Ack HTTP response code.
        logln(s"HTTP ${if (resp.code == 200) g("200") else r(resp.code)}")

        if (U.isValidJson(resp.data))
            logln(U.colorJson(U.prettyJson(resp.data)))
        else {
            if (resp.code == 200)
                logln(s"${g("HTTP response:")} ${resp.data}")
            else
                error(s"${r("HTTP error:")} ${resp.data}")
        }

        if (resp.code == 200) {
            if (path == "signin") {
                state.userEmail = Some(U.getJsonStringField(json, "email"))
                state.accessToken = Some(U.getJsonStringField(resp.data, "acsTok"))
            }
            else if (path == "signout") {
                state.userEmail = None
                state.accessToken = None
            }
        }
    }

    /**
     *
     */
    private def doRepl(): Unit = {
        loadServerBeacon(autoSignIn = true) match {
            case Some(beacon) ⇒ logServerInfo(beacon)
            case None ⇒ ()
        }
        loadProbeBeacon() match {
            case Some(beacon) ⇒ logProbeInfo(beacon)
            case None ⇒ ()
        }

        val parser = new DefaultParser()

        parser.setEofOnUnclosedBracket(Bracket.CURLY, Bracket.ROUND, Bracket.SQUARE)
        parser.setEofOnUnclosedQuote(true)
        parser.regexCommand("")
        parser.regexVariable("")

        val completer: Completer = new Completer {
            private val cmds = CMDS.map(c ⇒ (c.name, c.synopsis, c.group))

            /**
             *
             * @param disp
             * @param desc
             * @param completed
             * @return
             */
            private def mkCandidate(disp: String, grp: String, desc: String, completed: Boolean): Candidate =
                new Candidate(disp, disp, grp, desc, null, null, completed)

            override def complete(reader: LineReader, line: ParsedLine, candidates: util.List[Candidate]): Unit = {
                val words = line.words().asScala

                if (words.nonEmpty && words.head.nonEmpty && words.head.head == '$') { // Don't complete if the line starts with '$'.
                    // No-op.
                }
                else if (words.isEmpty || !cmds.map(_._1).contains(words.head))
                    candidates.addAll(cmds.map(n ⇒ {
                        val name = n._1
                        val desc = n._2.substring(0, n._2.length - 1) // Remove last '.'.
                        val grp = s"${n._3}:"

                        mkCandidate(
                            disp = name,
                            grp = grp,
                            desc = desc,
                            completed = true
                        )
                    }).asJava)
                else {
                    val cmd = words.head

                    val OPTIONAL_GRP = "Optional:"
                    val MANDATORY_GRP = "Mandatory:"
                    val DFTL_USER_GRP = "Default user:"
                    val CMDS_GRP = "Commands:"

                    candidates.addAll(CMDS.find(_.name == cmd) match {
                        case Some(c) ⇒
                            c.params.filter(!_.synthetic).flatMap(param ⇒ {
                                val hasVal = param.value.isDefined
                                val names = param.names.filter(_.startsWith("--")) // Skip shorthands from auto-completion.

                                names.map(name ⇒ mkCandidate(
                                    disp = if (hasVal) name + "=" else name,
                                    grp = if (param.optional) OPTIONAL_GRP else MANDATORY_GRP,
                                    desc = null,
                                    completed = !hasVal
                                ))
                            })
                                .asJava

                        case None ⇒ Seq.empty[Candidate].asJava
                    })

                    // For 'help' - add additional auto-completion/suggestion candidates.
                    if (cmd == HELP_CMD.name)
                        candidates.addAll(CMDS.map(c ⇒ s"--cmd=${c.name}").map(s ⇒
                            mkCandidate(
                                disp = s,
                                grp = CMDS_GRP,
                                desc = null,
                                completed = true
                            ))
                            .asJava
                        )

                    // For 'rest' or 'call' - add '--path' auto-completion/suggestion candidates.
                    if (cmd == REST_CMD.name || cmd == CALL_CMD.name) {
                        val pathParam = REST_CMD.findParameterById("path")
                        val hasPathAlready = words.exists(w ⇒ pathParam.names.exists(x ⇒ w.startsWith(x)))

                        if (!hasPathAlready)
                            candidates.addAll(
                                REST_SPEC.map(cmd ⇒ {
                                    val name = s"--path=${cmd.path}"

                                    mkCandidate(
                                        disp = name,
                                        grp = s"REST ${cmd.group}:",
                                        desc = cmd.desc,
                                        completed = true
                                    )
                                })
                                    .asJava
                            )
                    }

                    // For 'ask' and 'sugysn' - add additional model IDs auto-completion/suggestion candidates.
                    if (cmd == ASK_CMD.name || cmd == SUGSYN_CMD.name)
                        candidates.addAll(
                            state.probes.flatMap(_.models.toList).map(mdl ⇒ {
                                mkCandidate(
                                    disp = s"--mdlId=${mdl.id}",
                                    grp = MANDATORY_GRP,
                                    desc = null,
                                    completed = true
                                )
                            })
                                .asJava
                        )

                    // For 'call' - add additional auto-completion/suggestion candidates.
                    if (cmd == CALL_CMD.name) {
                        val pathParam = CALL_CMD.findParameterById("path")

                        words.find(w ⇒ pathParam.names.exists(x ⇒ w.startsWith(x))) match {
                            case Some(p) ⇒
                                val path = p.substring(p.indexOf('=') + 1)

                                REST_SPEC.find(_.path == path) match {
                                    case Some(spec) ⇒
                                        candidates.addAll(
                                            spec.params.map(param ⇒ {
                                                mkCandidate(
                                                    disp = s"--${param.name}",
                                                    grp = if (param.optional) OPTIONAL_GRP else MANDATORY_GRP,
                                                    desc = null,
                                                    completed = false
                                                )
                                            })
                                                .asJava
                                        )

                                        // Add 'acsTok' auto-suggestion.
                                        if (spec.params.exists(_.name == "acsTok") && state.accessToken.isDefined)
                                            candidates.add(
                                                mkCandidate(
                                                    disp = s"--acsTok=${state.accessToken.get}",
                                                    grp = MANDATORY_GRP,
                                                    desc = null,
                                                    completed = true
                                                )
                                            )

                                        // Add 'mdlId' auto-suggestion.
                                        if (spec.params.exists(_.name == "mdlId") && state.probes.nonEmpty)
                                            candidates.addAll(
                                                state.probes.flatMap(_.models.toList).map(mdl ⇒ {
                                                    mkCandidate(
                                                        disp = s"--mdlId=${mdl.id}",
                                                        grp = MANDATORY_GRP,
                                                        desc = null,
                                                        completed = true
                                                    )
                                                })
                                                    .asJava
                                            )

                                        // Add default 'email' and 'passwd' auto-suggestion for 'signin' path.
                                        if (path == "signin") {
                                            candidates.add(
                                                mkCandidate(
                                                    disp = s"--email=$DFLT_USER_EMAIL",
                                                    grp = DFTL_USER_GRP,
                                                    desc = null,
                                                    completed = true
                                                )
                                            )
                                            candidates.add(
                                                mkCandidate(
                                                    disp = s"--passwd=$DFLT_USER_PASSWD",
                                                    grp = DFTL_USER_GRP,
                                                    desc = null,
                                                    completed = true
                                                )
                                            )
                                        }

                                    case None ⇒ ()
                                }

                            case None ⇒ ()
                        }
                    }
                }
            }
        }

        class ReplHighlighter extends Highlighter {
            override def highlight(reader: LineReader, buffer: String): AttributedString =
                AttributedString.fromAnsi(
                    CMD_NAME.matcher(
                        CMD_PARAM.matcher(
                            buffer
                        )
                        .replaceAll("$1" + c("$2"))
                    )
                    .replaceAll(bo(g("$1")) + "$2")
                )

            override def setErrorPattern(errorPattern: Pattern): Unit = ()
            override def setErrorIndex(errorIndex: Int): Unit = ()
        }

        val reader = LineReaderBuilder
            .builder
            .appName("NLPCraft")
            .terminal(term)
            .completer(completer)
            .parser(parser)
            .highlighter(new ReplHighlighter())
            .history(new DefaultHistory())
            .variable(LineReader.SECONDARY_PROMPT_PATTERN, s"${g("...>")} ")
            .variable(LineReader.INDENTATION, 2)
            .build

        reader.setOpt(LineReader.Option.AUTO_FRESH_LINE)
        reader.unsetOpt(LineReader.Option.INSERT_TAB)
        reader.unsetOpt(LineReader.Option.BRACKETED_PASTE)
        reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)
        reader.setVariable(
            LineReader.HISTORY_FILE,
            new File(SystemUtils.getUserHome, HIST_PATH).getAbsolutePath
        )

        logln()
        logln(s"${y("Tip:")} Hit ${rv(bo(" Tab "))} for auto suggestions and completion.")
        logln(s"     Type '${c("help")}' to get help and ${rv(bo(" ↑ "))} or ${rv(bo(" ↓ "))} to scroll through history.")
        logln(s"     Type '${c("quit")}' to exit.")

        var exit = false

        val pinger = U.mkThread("repl-server-pinger") { t ⇒
            while (!t.isInterrupted) {
                loadServerBeacon()

                Thread.sleep(10.secs)
            }
        }

        pinger.start()

        var wasLastLineEmpty = false

        while (!exit) {
            val rawLine = try {
                val acsTokStr = bo(s"${state.accessToken.getOrElse("N/A")} ")

                val prompt1 = if (state.isServerOnline) gb(k(s" server: ${BO}ON$RST$GB ")) else rb(w(s" server: ${BO}OFF$RST$RB "))
                val prompt2 = if (state.isProbeOnline) gb(k(s" probe: ${BO}ON$RST$GB ")) else rb(w(s" probe: ${BO}OFF$RST$RB "))
                val prompt3 = wb(k(s" acsTok: $acsTokStr")) // Access token, if any.
                val prompt4 = kb(g(s" $USR_WORK_DIR ")) // Current working directory.

                if (!wasLastLineEmpty)
                    reader.printAbove("\n" + prompt1 + ":" + prompt2 + ":" + prompt3 + ":" + prompt4)

                reader.readLine(s"${g(">")} ")
            }
            catch {
                case _: UserInterruptException ⇒ "" // Ignore.
                case _: EndOfFileException ⇒ null
                case _: Exception ⇒ "" // Guard against JLine hiccups.
            }

            if (rawLine == null)
                exit = true
            else {
                val line = rawLine
                    .trim()
                    .replace("\n", "")
                    .replace("\t", " ")
                    .trim()

                if (line.nonEmpty) {
                    wasLastLineEmpty = false

                    try
                        doCommand(splitAndClean(line), repl = true)
                    catch {
                        case e: SplitError ⇒
                            val idx = e.index
                            val lineX = line.substring(0, idx) + r(line.substring(idx, idx + 1) ) + line.substring(idx + 1)
                            val dashX = c("-" * idx) + r("^") + c("-" * (line.length - idx - 1))

                            error(s"Uneven quotes or brackets:")
                            error(s"  ${r("+--")} $lineX")
                            error(s"  ${r("+--")} $dashX")
                    }

                    if (line == QUIT_CMD.name)
                        exit = true
                }
                else
                    wasLastLineEmpty = true
            }
        }

        U.stopThread(pinger)

        // Save command history.
        ignoring(classOf[IOException]) {
            reader.getHistory.save()
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private [cmdline] def cmdVersion(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val isS = getFlagParam(cmd, args, "semver", dflt = false)
        val isD = getFlagParam(cmd, args, "reldate", dflt = false)

        if (!isS && !isD)
            logln((
                new NCAsciiTable
                    += ("Version:", c(VER.version))
                    += ("Release date:", c(VER.date.toString))
                ).toString
            )
        else if (isS)
            logln(s"${VER.version}")
        else
            logln(s"${VER.date}")
    }

    /**
     *
     * @param msg
     */
    private def error(msg: String = ""): Unit = {
        // Make sure we exit with non-zero status.
        exitStatus = 1

        if (msg != null && msg.nonEmpty)
            logln(s"${r("X")} ${if (msg.head.isLower) msg.head.toUpper + msg.tail else msg}")
    }

    /**
     *
     * @param msg
     */
    private def warn(msg: String = ""): Unit =
        if (msg != null && msg.nonEmpty)
            logln(s"${y("!")} ${if (msg.head.isLower) msg.head.toUpper + msg.tail else msg}")

    /**
     *
     * @param msg
     */
    private def logln(msg: String = ""): Unit = {
        term.writer().println(msg)
        term.flush()
    }

    /**
     *
     * @param msg
     */
    private def log(msg: String = ""): Unit = {
        term.writer().print(msg)
        term.flush()
    }

    /**
     * Prints out the version and copyright title header.
     */
    private def title(): Unit = {
        logln(U.asciiLogo())
        logln(s"$NAME ver. ${VER.version}")
        logln()
    }

    /**
     *
     * @param baseUrl
     * @param cmd
     * @return
     */
    private def prepRestUrl(baseUrl: String, cmd: String): String =
        if (baseUrl.endsWith("/")) s"$baseUrl$cmd" else s"$baseUrl/$cmd"

    /**
     * Posts HTTP POST request.
     *
     * @param baseUrl Base endpoint URL.
     * @param cmd REST call command.
     * @param resp
     * @param json JSON string.
     * @return
     * @throws IOException
     */
    private def httpPost[T](baseUrl: String, cmd: String, resp: ResponseHandler[T], json: String): T = {
        val post = new HttpPost(prepRestUrl(baseUrl, cmd))

        post.setHeader("Content-Type", "application/json")
        post.setEntity(new StringEntity(json, "UTF-8"))

        try
            HttpClients.createDefault().execute(post, resp)
        finally
            post.releaseConnection()
    }

    /**
     *
     * @param endpoint
     * @param path
     * @param json
     * @return
     */
    private def httpPostResponse(endpoint: String, path: String, json: String): HttpRestResponse =
        httpPost(endpoint, path, mkHttpHandler(resp ⇒ {
            val status = resp.getStatusLine

            HttpRestResponse(
                status.getStatusCode,
                Option(EntityUtils.toString(resp.getEntity)).getOrElse(
                    throw new IllegalStateException(s"Unexpected REST error: ${status.getReasonPhrase}")
                )
            )
        }), json)

    /**
     *
     * @param endpoint
     * @param path
     * @param json
     * @return
     */
    private def httpPostResponseJson(endpoint: String, path: String, json: String): Option[String] =
        httpPost(endpoint, path, mkHttpHandler(resp ⇒ {
            val status = resp.getStatusLine

            if (status.getStatusCode == 200)
                Option(EntityUtils.toString(resp.getEntity))
            else
                None
        }), json)

    /**
     * Posts HTTP GET request.
     *
     * @param endpoint Base endpoint URL.
     * @param path REST call command.
     * @param resp
     * @param jsParams
     * @return
     * @throws IOException
     */
    private def httpGet[T](endpoint: String, path: String, resp: ResponseHandler[T], jsParams: (String, AnyRef)*): T = {
        val bldr = new URIBuilder(prepRestUrl(endpoint, path))

        jsParams.foreach(p ⇒ bldr.setParameter(p._1, p._2.toString))

        val get = new HttpGet(bldr.build())

        try
            HttpClients.createDefault().execute(get, resp)
        finally
            get.releaseConnection()
    }

    /**
     * Splits given string by spaces taking into an account double and single quotes,
     * '\' escaping as well as checking for uneven <>, {}, [], () pairs.
     *
     * @param line
     * @return
     */
    @throws[SplitError]
    private def splitAndClean(line: String): Seq[String] = {
        val lines = mutable.Buffer.empty[String]
        val buf = new StringBuilder
        var stack = List.empty[Char]
        var escape = false
        var index = 0

        def stackHead: Char = stack.headOption.getOrElse(Char.MinValue)

        for (ch ← line) {
            if (ch.isWhitespace && !stack.contains('"') && !stack.contains('\'') && !escape) {
                if (buf.nonEmpty) {
                    lines += buf.toString()
                    buf.clear()
                }
            }
            else if (ch == '\\') {
                if (escape)
                    buf += ch
                else
                    // SKip '\'.
                    escape = true
            }
            else if (ch == '"' || ch == '\'') {
                if (!escape) {
                    if (!stack.contains(ch))
                        stack ::= ch // Push.
                    else if (stackHead == ch)
                        stack = stack.tail // Pop.
                    else
                        throw SplitError(index)
                }

                buf += ch
            }
            else if (OPEN_BRK.contains(ch)) {
                stack ::= ch // Push.

                buf += ch
            }
            else if (CLOSE_BRK.contains(ch)) {
                if (stackHead != BRK_PAIR(ch))
                    throw SplitError(index)

                stack = stack.tail // Pop.

                buf += ch
            }
            else {
                if (escape)
                    buf += '\\' // Put back '\'.

                buf += ch
            }

            // Drop escape flag.
            if (escape && ch != '\\')
                escape = false

            index += 1
        }

        if (stack.nonEmpty)
            throw SplitError(index - 1)

        if (buf.nonEmpty)
            lines += buf.toString()

        lines.map(_.strip)
    }

    /**
     *
     * @param cmd
     * @param args
     * @return
     */
    private def processParameters(cmd: Command, args: Seq[String]): Seq[Argument] =
        args.map { arg ⇒
            val parts = arg.split("=")

            def mkError() = new IllegalArgumentException(s"Invalid parameter: ${c(arg)}, type $C'help --cmd=${cmd.name}'$RST to get help.")

            if (parts.size > 2)
                throw mkError()

            val name = if (parts.size == 1) arg.strip else parts(0).trim
            val value = if (parts.size == 1) None else Some(U.trimQuotes(parts(1).strip))
            val hasSynth = cmd.params.exists(_.synthetic)

            if (name.endsWith("=")) // Missing value or extra '='.
                throw mkError()

            cmd.findParameterByNameOpt(name) match {
                case None ⇒
                    if (hasSynth)
                        Argument(Parameter(
                            id = name.substring(2), // Remove single '--' from the beginning.
                            names = Seq(name),
                            value = value,
                            synthetic = true,
                            desc = null
                        ), value) // Synthetic argument.
                    else
                        throw mkError()

                case Some(param) ⇒
                    if ((param.value.isDefined && value.isEmpty) || (param.value.isEmpty && value.isDefined))
                        throw mkError()

                    Argument(param, value)
            }
        }

    /**
     *
     * @param args
     * @param repl
     */
    private def processAnsi(args: Seq[String], repl: Boolean): Unit = {
        args.find(_ == NO_ANSI_CMD.name) match {
            case Some(_) ⇒ NO_ANSI_CMD.body(NO_ANSI_CMD, Seq.empty, repl)
            case None ⇒ ()
        }
        args.find(_ == ANSI_CMD.name) match {
            case Some(_) ⇒ ANSI_CMD.body(ANSI_CMD, Seq.empty, repl)
            case None ⇒ ()
        }
    }

    /**
     *
     * @param args
     */
    private def execOsCmd(args: Seq[String]): Unit = {
        val pb = new ProcessBuilder(args.asJava).inheritIO()

        val proc = pb.start()

        try
            proc.waitFor()
        catch {
            case _: InterruptedException ⇒ () // Exit.
        }
    }

    /**
     * Processes a single command defined by the given arguments.
     *
     * @param args
     * @param repl Whether or not called from 'repl' mode.
     */
    @throws[Exception]
    private def doCommand(args: Seq[String], repl: Boolean): Unit = {
        if (args.nonEmpty) {
            try
                if (args.head.head == '$') {
                    val head = args.head.tail.strip // Remove '$' from 1st argument.
                    val tail = args.tail.toList

                    execOsCmd(if (head.isEmpty) tail else head :: tail)
                }
                else {
                    // Process 'no-ansi' and 'ansi' commands first.
                    processAnsi(args, repl)

                    // Remove 'no-ansi' and 'ansi' commands from the argument list, if any.
                    val xargs = args.filter(arg ⇒ arg != NO_ANSI_CMD.name && arg != ANSI_CMD.name)

                    if (xargs.nonEmpty) {
                        val cmd = xargs.head

                        // Reset error code.
                        exitStatus = 0

                        CMDS.find(_.name == cmd) match {
                            case Some(cmd) ⇒ cmd.body(cmd, processParameters(cmd, xargs.tail), repl)
                            case None ⇒ throw UnknownCommand(cmd)
                        }
                    }
                }
            catch {
                case e: Exception ⇒ error(e.getLocalizedMessage)
            }
        }
    }

    /**
     *
     * @param args
     */
    private def boot(args: Array[String]): Unit = {
        new Thread() {
            override def run(): Unit = {
                U.gaScreenView("cli")
            }
        }
        .start()

        // Initialize OS-aware terminal.
        term = TerminalBuilder.builder()
            .name(NAME)
            .system(true)
            .nativeSignals(true)
            .signalHandler(Terminal.SignalHandler.SIG_IGN)
            .dumb(true)
            .jansi(true)
            .build()

        // Process 'no-ansi' and 'ansi' commands first (before ASCII title is shown).
        processAnsi(args, repl = false)

        if (!args.contains(NO_LOGO_CMD.name))
            title() // Show logo unless we have 'no-logo' command.

        // Remove all auxiliary commands that are combined with other commands, if any.
        val xargs = args.filter(arg ⇒
            arg != NO_ANSI_CMD.name &&
            arg != ANSI_CMD.name &&
            arg != NO_LOGO_CMD.name
        )

        if (xargs.isEmpty)
            doRepl()
        else
            doCommand(xargs.toSeq, repl = false)

        sys.exit(exitStatus)
    }

    cleanUpTempFiles()

    // Boot up.
    boot(args)
}
