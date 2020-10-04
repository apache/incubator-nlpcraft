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

import java.io.{File, FileInputStream, IOException, ObjectInputStream}

import com.google.gson._
import javax.net.ssl.SSLException
import org.apache.commons.lang3.SystemUtils
import org.apache.http.HttpResponse
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.{NCAnsi, NCAnsiSpinner}
import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.apache.nlpcraft.common.version.NCVersion
import java.lang.ProcessBuilder.Redirect
import java.lang.management.ManagementFactory
import java.text.DateFormat
import java.util
import java.util.Date
import java.util.regex.PatternSyntaxException

import org.apache.nlpcraft.common.util.NCUtils.IntTimeUnits
import org.jline.reader.Completer
import org.jline.reader.impl.DefaultParser
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.reader.{Candidate, EndOfFileException, LineReader, LineReaderBuilder, ParsedLine, UserInterruptException}
import org.jline.reader.impl.DefaultParser.Bracket
import org.jline.reader.impl.history.DefaultHistory
import org.jline.widget.AutosuggestionWidgets
import resource.managed

import scala.collection.mutable
import scala.compat.java8.OptionConverters._
import scala.collection.JavaConverters._
import scala.compat.Platform.currentTime
import scala.util.Try
import scala.util.control.Exception.ignoring

/**
 * NLPCraft CLI.
 */
object NCCli extends App {
    private final val NAME = "Apache NLPCraft CLI"

    private final val SRV_BEACON_PATH = ".nlpcraft/server_beacon"
    private final val HIST_PATH = ".nlpcraft/cli_history"

    private final lazy val VER = NCVersion.getCurrent
    private final lazy val JAVA = U.sysEnv("NLPCRAFT_CLI_JAVA").getOrElse(new File(SystemUtils.getJavaHome,s"bin/java${if (SystemUtils.IS_OS_UNIX) "" else ".exe"}").getAbsolutePath)
    private final lazy val INSTALL_HOME = U.sysEnv("NLPCRAFT_CLI_INSTALL_HOME").getOrElse(SystemUtils.USER_DIR)
    private final lazy val JAVA_CP = U.sysEnv("NLPCRAFT_CLI_JAVA_CP").getOrElse(ManagementFactory.getRuntimeMXBean.getClassPath)
    private final lazy val SCRIPT_NAME = U.sysEnv("NLPCRAFT_CLI_SCRIPT").getOrElse(s"nlpcraft.${if (SystemUtils.IS_OS_UNIX) "sh" else "cmd"}")
    private final lazy val PROMPT = if (SCRIPT_NAME.endsWith("cmd")) ">" else "$"
    private final lazy val IS_SCRIPT = U.sysEnv("NLPCRAFT_CLI").isDefined

    private final val T___ = "    "
    private val OPEN_BRK = Seq('[', '{', '(', '<')
    private val CLOSE_BRK = Seq(']', '}', ')', '>')
    // Pair for each open or close bracket.
    private val BRK_PAIR = OPEN_BRK.zip(CLOSE_BRK).toMap ++ CLOSE_BRK.zip(OPEN_BRK).toMap

    private var exitStatus = 0

    private var term: Terminal = _

    private val gson = new GsonBuilder().setPrettyPrinting().create

    case class SplitError(index: Int) extends Exception

    case class State(
        var isServer: Boolean,
        var accessToken: Option[String]
    )

    private val state = State(isServer = false, None)

    // Single CLI command.
    case class Command(
        name: String,
        synopsis: String,
        desc: Option[String] = None,
        params: Seq[Parameter] = Seq.empty,
        examples: Seq[Example] = Seq.empty,
        body: (Command, Seq[Argument], Boolean) ⇒ Unit
    ) {
        /**
         *
         * @param name
         * @return
         */
        def findParameterByName(name: String): Option[Parameter] =
            params.find(_.names.contains(name))

        /**
         *
         * @param id
         * @return
         */
        def findParameterById(id: String): Option[Parameter] =
            params.find(_.id == id)
    }
    // Single command's example.
    case class Example(
        usage: Seq[String],
        desc: String
    )
    // Single command's parameter.
    case class Parameter(
        id: String,
        names: Seq[String],
        value: Option[String] = None,
        optional: Boolean = false, // Mandatory by default.
        desc: String
    )

    // Parsed command line argument.
    case class Argument(
        parameter: Parameter, // Formal parameter this argument refers to.
        value: Option[String]
    )

    // All supported commands.
    private final val CMDS = Seq(
        Command(
            name = "start-server",
            synopsis = s"Starts local REST server.",
            desc = Some(
                s"REST server is started in the external JVM process with both stdout and stderr piped out into log file. " +
                s"Command will block until the server is started unless ${y("--no-wait")} parameter is used."
            ),
            body = cmdStartServer,
            params = Seq(
                Parameter(
                    id = "config",
                    names = Seq("--config", "-c"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Configuration absolute file path. Server will automatically look for ${y("nlpcraft.conf")} " +
                        s"configuration file in the same directory as NLPCraft JAR file. If the configuration file has " +
                        s"different name or in different location use this parameter to provide an alternative path. " +
                        s"Note that the REST server and the data probe can use the same file for their configuration."
                ),
                Parameter(
                    id = "igniteConfig",
                    names = Seq("--ignite-config", "-i"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Apache Ignite configuration absolute file path. Note that Apache Ignite is used as a cluster " +
                        s"computing plane and a default distributed storage. REST server will automatically look for " +
                        s"${y("ignite.xml")} configuration file in the same directory as NLPCraft JAR file. If the " +
                        s"configuration file has different name or in different location use this parameter to " +
                        s"provide an alternative path."
                ),
                Parameter(
                    id = "output",
                    names = Seq("--output-path", "-o"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        "File path for both REST server stdout and stderr output. If not provided, the REST server" +
                        s"output will be piped into ${y("${USER_HOME}/.nlpcraft/server-output-xxx.txt")}' file."
                ),
                Parameter(
                    id = "noWait",
                    names = Seq("--no-wait"),
                    optional = true,
                    desc =
                        s"Instructs command not to wait for the server startup and return immediately."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME start-server"),
                    desc = "Starts local  server with default configuration."
                ),
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME start-server -c=/opt/nlpcraft/nlpcraft.conf"),
                    desc = "Starts local REST server with alternative configuration file."
                )
            )
        ),
        Command(
            name = "get-server",
            synopsis = s"Basic information about locally running REST server.",
            body = cmdGetServer
        ),
        Command(
            name = "no-ansi",
            synopsis = s"Disables usage of ANSI escape codes for colors & terminal controls.",
            desc = Some(
                s"This is a special command that can be combined with any other commands."
            ),
            body = cmdNoAnsi,
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -c=repl no-ansi"),
                    desc = "Displays help for 'repl' commands without using ANSI color and escape sequences."
                )
            )
        ),
        Command(
            name = "ansi",
            synopsis = s"Enables usage of ANSI escape codes for colors & terminal controls.",
            desc = Some(
                s"This is a special command that can be combined with any other commands."
            ),
            body = cmdAnsi,
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -c=repl ansi"),
                    desc = "Displays help for 'repl' commands with ANSI color and escape sequences."
                )
            )
        ),
        Command(
            name = "ping-server",
            synopsis = s"Pings local REST server.",
            desc = Some(
                s"REST server is pinged using '/health' REST call to check its online status."
            ),
            body = cmdPingServer,
            params = Seq(
                Parameter(
                    id = "number",
                    names = Seq("--number", "-n"),
                    value = Some("num"),
                    optional = true,
                    desc =
                        "Number of pings to perform. Must be a number > 0. Default is 1."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME ping-server -n=10"
                    ),
                    desc = "Pings local REST server 10 times."
                )
            )
        ),
        Command(
            name = "stop-server",
            synopsis = s"Stops local REST server.",
            desc = Some(
                s"Local REST server must be started via $SCRIPT_NAME or similar way."
            ),
            body = cmdStopServer
        ),
        Command(
            name = "quit",
            synopsis = s"Quits REPL session when in REPL mode.",
            body = cmdQuit
        ),
        Command(
            name = "help",
            synopsis = s"Displays manual page for '$SCRIPT_NAME'.",
            desc = Some(
                s"By default, without '-all' or '-cmd' parameters, displays the abbreviated form of manual " +
                s"only listing the commands without parameters or examples."
            ),
            body = cmdHelp,
            params = Seq(
                Parameter(
                    id = "cmd",
                    names = Seq("--cmd", "-c"),
                    value = Some("cmd"),
                    optional = true,
                    desc = "Set of commands to show the manual for. Can be used multiple times."
                ),
                Parameter(
                    id = "all",
                    names = Seq("--all", "-a"),
                    optional = true,
                    desc = "Flag to show full manual for all commands."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -c=repl --cmd=ver"),
                    desc = "Displays help for 'repl' and 'version' commands."
                ),
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -all"),
                    desc = "Displays help for all commands."
                )
            )
        ),
        Command(
            name = "version",
            synopsis = s"Displays full version of '$SCRIPT_NAME' script.",
            desc = Some(
                "Depending on the additional parameters can display only the semantic version or the release date."
            ),
            body = cmdVersion,
            params = Seq(
                Parameter(
                    id = "semver",
                    names = Seq("--sem-ver", "-s"),
                    value = None,
                    optional = true,
                    desc = s"Display only the semantic version value, e.g. ${VER.version}."
                ),
                Parameter(
                    id = "reldate",
                    names = Seq("--rel-date", "-d"),
                    value = None,
                    optional = true,
                    desc = s"Display only the release date, e.g. ${VER.date}."
                )
            )
        ),
        Command(
            name = "repl",
            synopsis = s"Starts '$SCRIPT_NAME' in interactive REPL mode.",
            desc = Some(
                s"REPL mode supports all the same commands as command line mode. " +
                s"REPL is the default mode for when '$SCRIPT_NAME' is started without parameters. " +
                s"In REPL mode you need to put values that can have spaces (like JSON or file paths) " +
                s"inside of single or double quotes both of which can be escaped using '\\' character, when necessary."
            ),
            body = cmdRepl
        )
    ).sortBy(_.name)

    require(
        U.getDups(CMDS.map(_.name)).isEmpty,
        "Dup commands."
    )

    private final val DFLT_CMD = CMDS.find(_.name ==  "repl").get
    private final val NO_ANSI_CMD = CMDS.find(_.name ==  "no-ansi").get
    private final val ANSI_CMD = CMDS.find(_.name ==  "ansi").get
    private final val QUIT_CMD = CMDS.find(_.name ==  "quit").get
    private final val HELP_CMD = CMDS.find(_.name ==  "help").get

    /**
     *
     * @param s
     * @return
     */
    private def stripQuotes(s: String): String = {
        var x = s
        var found = true

        while (found) {
            found = false

            if (x.startsWith("\"") && x.endsWith("\"")) {
                found = true

                x = x.substring(1, x.length - 1)
            }

            if (x.startsWith("'") && x.endsWith("'")) {
                found = true

                x = x.substring(1, x.length - 1)
            }
        }

        x
    }

    /**
     *
     * @param endpoint
     * @return
     */
    private def restHealth(endpoint: String): Int =
        httpGet(endpoint, "health", new ResponseHandler[Int]() {
            override def handleResponse(resp: HttpResponse): Int = resp.getStatusLine.getStatusCode
        })

    /**
     *
     * @param pathOpt
     */
    private def checkFilePath(pathOpt: Option[Argument]): Unit = {
        if (pathOpt.isDefined) {
            val file = new File(stripQuotes(pathOpt.get.value.get))

            if (!file.exists() || !file.isFile)
                throw new IllegalArgumentException(s"File not found: ${c(file.getAbsolutePath)}")
        }
    }

    /**
     *
     * @param pathOpt
     */
    private def checkDirPath(pathOpt: Option[Argument]): Unit = {
        if (pathOpt.isDefined) {
            val file = new File(stripQuotes(pathOpt.get.value.get))

            if (!file.exists() || !file.isDirectory)
                throw new IllegalArgumentException(s"Directory not found: ${c(file.getAbsolutePath)}")
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not running from REPL.
     */
    private def cmdStartServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val cfgPath = args.find(_.parameter.id == "config")
        val igniteCfgPath = args.find(_.parameter.id == "igniteConfig")
        val noWait = args.exists(_.parameter.id == "noWait")
        val output = args.find(_.parameter.id == "output") match {
            case Some(arg) ⇒ new File(stripQuotes(arg.value.get))
            case None ⇒ new File(SystemUtils.getUserHome, s".nlpcraft/server-output-$currentTime.txt")
        }

        checkFilePath(cfgPath)
        checkFilePath(igniteCfgPath)

        loadServerBeacon() match {
            case Some(b) ⇒ throw new IllegalStateException(s"Existing local server (pid ${c(b.pid)}) detected.")
            case None ⇒ ()
        }

        val srvPb = new ProcessBuilder(
            JAVA,
            "-ea",
            "-Xms2048m",
            "-XX:+UseG1GC",
            // Required by Ignite 2.x running on JDK 11+.
            "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED",
            "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED",
            "--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
            "--add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED",
            "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
            "--illegal-access=permit",
            "-DNLPCRAFT_ANSI_COLOR_DISABLED=true", // No ANSI colors for text log output to the file.
            "-cp",
            s"$JAVA_CP",
            "org.apache.nlpcraft.NCStart",
            "-server",
            cfgPath match {
                case Some(path) ⇒ s"-config=${stripQuotes(path.value.get)}"
                case None ⇒ ""
            },
            igniteCfgPath match {
                case Some(path) ⇒ s"-igniteConfig=${stripQuotes(path.value.get)}"
                case None ⇒ ""
            },
        )

        srvPb.directory(new File(INSTALL_HOME))
        srvPb.redirectErrorStream(true)

        val bleachPb = new ProcessBuilder(
            JAVA,
            "-ea",
            "-cp",
            s"$JAVA_CP",
            "org.apache.nlpcraft.model.tools.cmdline.NCCliAnsiBleach"
        )

        bleachPb.directory(new File(INSTALL_HOME))
        bleachPb.redirectOutput(Redirect.appendTo(output))

        try {
            ProcessBuilder.startPipeline(Seq(srvPb, bleachPb).asJava)

            logln(s"Server output > ${c(output.getAbsolutePath)}")

            if (noWait)
                logln(s"Server is starting...")
            else {
                log(s"Server is starting ")

                var beacon = loadServerBeacon().orNull
                var online = false
                val spinner = mkSpinner()
                val timeout = currentTime + 5.mins
                val warnTimeout = currentTime + 60.secs

                spinner.start()

                while (currentTime < timeout && !online) {
                    if (beacon == null)
                        beacon = loadServerBeacon().orNull
                    else
                        online = Try(restHealth("http://" + beacon.restEndpoint) == 200).getOrElse(false)

                    if (!online) {
                        if (currentTime > warnTimeout)
                            // Warn if it's taking too long.
                            spinner.setSuffix(s" ${r("(taking too long - check logs)")}")

                        Thread.sleep(2.secs) // Check every 2 secs.
                    }
                }

                spinner.stop()

                if (!online) {
                    logln()
                    error(s"Cannot detect live server.")
                    error(s"Check output for errors: ${c(output.getAbsolutePath)}")
                }
                else {
                    logln(g("OK"))
                    logln(mkServerBeaconTable(beacon).toString)
                }
            }

            val tbl = new NCAsciiTable()

            tbl += (s"${g("stop-server")}", "Stop the server.")
            tbl += (s"${g("ping-server")}", "Ping the server.")
            tbl += (s"${g("get-server")}", "Get server information.")

            logln(s"Handy commands:\n${tbl.toString}")
        }
        catch {
            case e: Exception ⇒ error(s"Server failed to start: ${y(e.getLocalizedMessage)}")
        }
    }

    /**
     * Makes default spinner.
     *
     * @return
     */
    private def mkSpinner() = new NCAnsiSpinner(
        term.writer(),
        ansiCyanFg,
        // ANSI is NOT disabled & we ARE NOT running from IDEA or Eclipse...
        NCAnsi.isEnabled && IS_SCRIPT
    )

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdPingServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        val endpoint = loadServerBeacon() match {
            case Some(beacon) ⇒ s"http://${beacon.restEndpoint}"
            case None ⇒ throw new IllegalStateException(s"Cannot detect locally running REST server.")
        }

        val num = args.find(_.parameter.id == "number") match {
            case Some(arg) ⇒
                try
                    Integer.parseInt(arg.value.get)
                catch {
                    case _ :Exception ⇒ throw new IllegalArgumentException(s"Invalid number of pings: ${arg.value.get}")
                }

            case None ⇒ 1 // Default.
        }

        var i = 0

        while (i < num) {
            log(s"(${i + 1} of $num) pinging REST server at ${b(endpoint)} ")

            val spinner = mkSpinner()

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
     * @return
     */
    private def loadServerBeacon(): Option[NCCliServerBeacon] = {
        val beacon = try {
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

        state.isServer = beacon.isDefined

        beacon
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdQuit(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        // No-op.
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdStopServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        loadServerBeacon() match {
            case Some(beacon) ⇒
                val pid = beacon.pid

                if (beacon.ph.destroy()) {
                    logln(s"Local REST server (pid ${c(pid)}) has been stopped.")

                    // Update state right away.
                    state.isServer = false
                } else
                    error(s"Failed to stop the local REST server (pid ${c(pid)}).")

            case None ⇒
                error("Cannot detect locally running REST server.")
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdNoAnsi(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        NCAnsi.setEnabled(false)
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdAnsi(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        NCAnsi.setEnabled(true)
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdHelp(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        /**
         *
         */
        def header(): Unit = logln(
            s"""|${ansiBold("NAME")}
                |$T___$SCRIPT_NAME - command line interface to control NLPCraft.
                |
                |${ansiBold("USAGE")}
                |$T___$SCRIPT_NAME [COMMAND] [PARAMETERS]
                |
                |${ansiBold("COMMANDS")}""".stripMargin
        )

        /**
         *
         * @param cmd
         * @return
         */
        def mkCmdLines(cmd: Command): Seq[String] = {
            var lines = mutable.Buffer.empty[String]

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
                        lines += s"$T___${T___}Optional."

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

        val tbl = NCAsciiTable().margin(left = if (repl) 0 else 4)

        if (args.isEmpty) { // Default - show abbreviated help.
            if (!repl)
                header()

            CMDS.foreach(cmd ⇒ tbl +/ (
                "" → s"${g(cmd.name)}",
                "align:left, maxWidth:85" → cmd.synopsis
            ))

            logln(tbl.toString)
        }
        else if (args.size == 1 && args.head.parameter.id == "all") { // Show a full format help for all commands.
            if (!repl)
                header()

            CMDS.foreach(cmd ⇒
                tbl +/ (
                    "" → s"${g(cmd.name)}",
                    "align:left, maxWidth:85" → mkCmdLines(cmd)
                )
            )

            logln(tbl.toString)
        }
        else { // Help for individual commands.
            var err = false
            val seen = mutable.Buffer.empty[String]

            for (arg ← args) {
                val cmdName = arg.value.get

                CMDS.find(_.name.contains(cmdName)) match {
                    case Some(c) ⇒
                        if (!seen.contains(c.name)) {
                            tbl +/ (
                                "" → s"${g(cmd.name)}",
                                "align:left, maxWidth:85" → mkCmdLines(c)
                            )

                            seen += c.name
                        }
                    case None ⇒
                        err = true

                        unknownCommand(cmdName)
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
    private def mkServerBeaconTable(beacon: NCCliServerBeacon): NCAsciiTable = {
        val tbl = new NCAsciiTable

        tbl += ("PID", s"${g(beacon.pid)}")
        tbl += ("Database URL", s"${g(beacon.dbUrl)}")
        tbl += ("  Driver", s"${g(beacon.dbDriver)}")
        tbl += ("  Pool min", s"${g(beacon.dbPoolMin)}")
        tbl += ("  Pool init", s"${g(beacon.dbPoolInit)}")
        tbl += ("  Pool max", s"${g(beacon.dbPoolMax)}")
        tbl += ("  Pool increment", s"${g(beacon.dbPoolInc)}")
        tbl += ("  Reset on start", s"${g(beacon.dbInit)}")
        tbl += ("REST endpoint", s"${g(beacon.restEndpoint)}")
        tbl += ("Probe uplink", s"${g(beacon.upLink)}")
        tbl += ("Probe downlink", s"${g(beacon.downLink)}")
        tbl += ("Token providers", s"${g(beacon.tokenProviders)}")
        tbl += ("NLP engine", s"${g(beacon.nlpEngine)}")
        tbl += ("External config URL", s"${g(beacon.extConfigUrl)}")
        tbl += ("Beacon file path", s"${g(beacon.filePath)}")
        tbl += ("Started on", s"${g(DateFormat.getDateTimeInstance.format(new Date(beacon.startMs)))}")

        tbl
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdGetServer(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        loadServerBeacon() match {
            case Some(beacon) ⇒ logln(s"Local REST server:\n${mkServerBeaconTable(beacon).toString}")
            case None ⇒ error(s"Cannot detect local REST server.")
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     * @param repl Whether or not executing from REPL.
     */
    private def cmdRepl(cmd: Command, args: Seq[Argument], repl: Boolean): Unit = {
        loadServerBeacon() match {
            case Some(beacon) ⇒ logln(s"Local REST server detected:\n${mkServerBeaconTable(beacon).toString}")
            case None ⇒ ()
        }

        val appName = s"$NAME ver. ${VER.version}"

        val parser = new DefaultParser()

        parser.setEofOnUnclosedBracket(Bracket.CURLY, Bracket.ROUND, Bracket.SQUARE)
        parser.setEofOnUnclosedQuote(true)

        val completer = new Completer {
            private val cmds = CMDS.map(c ⇒ c.name → c.synopsis)

            /**
             *
             * @param id
             * @param disp
             * @param desc
             * @param completed
             * @return
             */
            private def mkCandidate(id: String, disp: String, desc: String, completed: Boolean): Candidate =
                new Candidate(id, disp, null, desc, null, null, completed)

            override def complete(reader: LineReader, line: ParsedLine, candidates: util.List[Candidate]): Unit = {
                val words = line.words().asScala

                if (words.isEmpty || !cmds.map(_._1).contains(words.head))
                    candidates.addAll(cmds.map(n ⇒ {
                        val name = n._1
                        val desc = n._2.substring(0, n._2.length - 1) // Remove last '.'.

                        mkCandidate(name, name, desc, completed = true)
                    }).asJava)
                else {
                    val cmd = words.head

                    candidates.addAll(CMDS.find(_.name == cmd) match {
                        case Some(c) ⇒
                            c.params.flatMap(param ⇒ {
                                val hasVal = param.value.isDefined
                                val names = param.names.filter(_.startsWith("--")) // Skip shorthands from auto-completion.

                                names.map(name ⇒ mkCandidate(if (hasVal) name + "=" else name, name,null, !hasVal))
                            })
                            .asJava

                        case None ⇒ Seq.empty[Candidate].asJava
                    })

                    // For 'help' - add additional auto-completion candidates.
                    if (cmd == HELP_CMD.name)
                        candidates.addAll(CMDS.map(c ⇒ s"--cmd=${c.name}").map(s ⇒ mkCandidate(s, s, null, completed = true)).asJava)
                }
            }
        }

        val reader = LineReaderBuilder
            .builder
            .appName(appName)
            .terminal(term)
            .completer(completer)
            .parser(parser)
            .history(new DefaultHistory())
            .variable(LineReader.SECONDARY_PROMPT_PATTERN, s"${g("\u2026\u25b6")} ")
            .variable(LineReader.INDENTATION, 2)
            .build

        reader.setOpt(LineReader.Option.AUTO_FRESH_LINE)
        reader.unsetOpt(LineReader.Option.INSERT_TAB)
        reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)
        reader.setVariable(
            LineReader.HISTORY_FILE,
            new File(SystemUtils.getUserHome, HIST_PATH).getAbsolutePath
        )

        new AutosuggestionWidgets(reader).enable()

        logln(s"Hit ${rv(" Tab ")} or type '${c("help")}' to get help, '${c("quit")}' to exit.")

        var exit = false

        val pinger = U.mkThread("repl-server-pinger") { t ⇒
            while (!t.isInterrupted) {
                loadServerBeacon()

                Thread.sleep(10.secs)
            }
        }

        pinger.start()

        while (!exit) {
            val rawLine = try {
                val srvStr = bo(s"${if (state.isServer) s"ON " else s"OFF "}")
                val acsTokStr = bo(s"${state.accessToken.getOrElse("")} ")

                reader.printAbove("\n" + rb(w(s" server: $srvStr")) + wb(k(s" acsTok: $acsTokStr")))
                reader.readLine(s"${g("\u25b6")} ")
            }
            catch {
                case _: PatternSyntaxException ⇒ "" // Guard against JLine hiccups.
                case _: UserInterruptException ⇒ "" // Ignore.
                case _: EndOfFileException ⇒ null
            }

            if (rawLine == null || QUIT_CMD.name == rawLine.trim)
                exit = true
            else {
                val line = rawLine.trim().replace("\n", "").replace("\t", "")

                if (line.nonEmpty)
                    try {
                        doCommand(splitBySpace(line), repl = true)
                    }
                    catch {
                        case e: SplitError ⇒
                            val idx = e.index
                            val lineX = line.substring(0, idx) + r(line.substring(idx, idx + 1) ) + line.substring(idx + 1)
                            val dashX = c("-" * idx) + r("^") + c("-" * (line.length - idx - 1))

                            error(s"Uneven quotes or brackets:")
                            error(s"  ${r("+-")} $lineX")
                            error(s"  ${r("+-")} $dashX")
                    }
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
    private def cmdVersion(cmd: Command, args: Seq[Argument], repl: Boolean): Unit =
        if (args.isEmpty)
            logln((
                new NCAsciiTable
                    += ("Version:", c(VER.version))
                    += ("Release date:", c(VER.date.toString))
                ).toString
            )
        else {
            val isS = args.exists(_.parameter.id == "semver")
            val isD = args.exists(_.parameter.id == "reldate")

            if (isS || isD) {
                if (isS)
                    logln(s"${VER.version}")
                if (isD)
                    logln(s"${VER.date}")
            }
            else
                error(s"Invalid parameters: ${args.mkString(", ")}")
        }


    /**
     *
     * @param msg
     */
    private def error(msg: String = ""): Unit = {
        // Make sure we exit with non-zero status.
        exitStatus = 1

        term.writer().println(s"${y("ERR:")} ${if (msg.head.isLower) msg.head.toUpper + msg.tail else msg}")
        term.flush()
    }

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
     *
     */
    private def unknownCommand(cmd: String): Unit = {
        error(s"Unknown command: ${y(cmd)}")
        error(s"Use '${c("help")}' command to read the manual.")
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
        if (baseUrl.endsWith("/")) s"${baseUrl}api/v1/$cmd" else s"$baseUrl/api/v1/$cmd"

    /**
     * Posts HTTP POST request.
     *
     * @param baseUrl Base endpoint URL.
     * @param cmd REST call command.
     * @param resp
     * @param jsParams
     * @return
     * @throws IOException
     */
    private def httpPost[T](baseUrl: String, cmd: String, resp: ResponseHandler[T], jsParams: (String, AnyRef)*): T = {
        val post = new HttpPost(prepRestUrl(baseUrl, cmd))

        post.setHeader("Content-Type", "application/json")
        post.setEntity(new StringEntity(gson.toJson(jsParams.filter(_._2 != null).toMap.asJava), "UTF-8"))

        try
            HttpClients.createDefault().execute(post, resp)
        finally
            post.releaseConnection()
    }

    /**
     * Posts HTTP GET request.
     *
     * @param baseUrl Base endpoint URL.
     * @param cmd REST call command.
     * @param resp
     * @param jsParams
     * @return
     * @throws IOException
     */
    private def httpGet[T](baseUrl: String, cmd: String, resp: ResponseHandler[T], jsParams: (String, AnyRef)*): T = {
        val bldr = new URIBuilder(prepRestUrl(baseUrl, cmd))

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
    private def splitBySpace(line: String): Seq[String] = {
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

        lines.map(_.trim)
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

            def mkError() = new IllegalArgumentException(s"Invalid parameter: ${c(arg)}")

            if (parts.size > 2)
                throw mkError()

            val name = if (parts.size == 1) arg else parts(0)
            val value = if (parts.size == 1) None else Some(parts(1))

            cmd.findParameterByName(name) match {
                case None ⇒ throw mkError()
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
        args.find(arg ⇒ NO_ANSI_CMD.name.contains(arg)) match {
            case Some(_) ⇒ NO_ANSI_CMD.body(NO_ANSI_CMD, Seq.empty, repl)
            case None ⇒ ()
        }
        args.find(arg ⇒ ANSI_CMD.name.contains(arg)) match {
            case Some(_) ⇒ ANSI_CMD.body(ANSI_CMD, Seq.empty, repl)
            case None ⇒ ()
        }
    }

    /**
     * Processes a single command defined by the given arguments.
     *
     * @param args
     * @param repl Whether or not called from 'repl' command.
     */
    private def doCommand(args: Seq[String], repl: Boolean): Unit = {
        // Process 'no-ansi' and 'ansi' commands first.
        processAnsi(args, repl)

        // Remove 'no-ansi' and 'ansi' commands from the argument list, if any.
        val xargs = args.filter(arg ⇒ !NO_ANSI_CMD.name.contains(arg) && !ANSI_CMD.name.contains(arg))

        if (xargs.nonEmpty) {
            val cmd = xargs.head

            CMDS.find(_.name == cmd) match {
                case Some(cmd) ⇒
                    exitStatus = 0

                    if (!(repl && cmd.name == "repl")) // Don't call 'repl' from 'repl'.
                        try
                            cmd.body(cmd, processParameters(cmd, xargs.tail), repl)
                        catch {
                            case e: Exception ⇒ error(e.getLocalizedMessage)
                        }

                case None ⇒ unknownCommand(cmd)
            }
        }
    }

    /**
     *
     * @param args
     */
    private def boot(args: Array[String]): Unit = {
        // Initialize OS-aware terminal.
        term = TerminalBuilder.builder()
            .name(NAME)
            .system(true)
            .dumb(true)
            .jansi(true)
            .build()

        // Process 'no-ansi' and 'ansi' commands first (before ASCII title is shown).
        processAnsi(args, repl = false)

        title()

        if (args.isEmpty)
            DFLT_CMD.body(DFLT_CMD, Seq.empty, false)
        else
            doCommand(args.toSeq, repl = false)

        sys.exit(exitStatus)
    }

    // Boot up.
    boot(args)
}
