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

import java.io.{BufferedReader, File, FileInputStream, IOException, InputStreamReader, ObjectInputStream}
import java.net.URL

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
import java.util.Date

import resource.managed

import scala.collection.mutable
import scala.compat.java8.OptionConverters._
import scala.collection.JavaConverters._
import scala.compat.Platform.currentTime

/**
 * 'nlpcraft' script entry point.
 */
object NCCli extends App {
    private final val NAME = "Apache NLPCraft CLI"

    private final val SRV_BEACON_PATH = ".nlpcraft/server_beacon"

    private final lazy val VER = NCVersion.getCurrent
    private final lazy val JAVA = U.sysEnv("NLPCRAFT_CLI_JAVA").getOrElse(new File(SystemUtils.getJavaHome,s"bin/java${if (SystemUtils.IS_OS_UNIX) "" else ".exe"}").getAbsolutePath)
    private final lazy val INSTALL_HOME = U.sysEnv("NLPCRAFT_CLI_INSTALL_HOME").getOrElse(SystemUtils.USER_DIR)
    private final lazy val JAVA_CP = U.sysEnv("NLPCRAFT_CLI_JAVA_CP").getOrElse(ManagementFactory.getRuntimeMXBean.getClassPath)
    private final lazy val SCRIPT_NAME = U.sysEnv("NLPCRAFT_CLI_SCRIPT").getOrElse(s"nlpcraft.${if (SystemUtils.IS_OS_UNIX) "sh" else "cmd"}")
    private final lazy val PROMPT = if (SCRIPT_NAME.endsWith("cmd")) ">" else "$"

    private final val T___ = "    "
    private val OPEN_BRK = Seq('[', '{', '(', '<')
    private val CLOSE_BRK = Seq(']', '}', ')', '>')
    // Pair for each open or close bracket.
    private val BRK_PAIR = OPEN_BRK.zip(CLOSE_BRK).toMap ++ CLOSE_BRK.zip(OPEN_BRK).toMap

    private var exitStatus = 0

    private val gson = new GsonBuilder().setPrettyPrinting().create

    // Single CLI command.
    case class Command(
        id: String,
        names: Seq[String],
        synopsis: String,
        desc: Option[String] = None,
        params: Seq[Parameter] = Seq.empty,
        examples: Seq[Example] = Seq.empty,
        body: (Command, Seq[Argument]) ⇒ Unit
    ) {
        final val extNames = names.flatMap(name ⇒ // Safeguard against "common" errors.
            Seq(
                name,
                s"-$name",
                s"--$name",
                s"/$name",
                s"\\$name"
            )
        )

        final val mainName = names.head

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
            id = "start-server",
            names = Seq("start-server"),
            synopsis = s"Starts local REST server.",
            desc = Some(
                s"REST server is started in the external JVM process with both stdout and stderr piped out into log file."
            ),
            body = cmdStartServer,
            params = Seq(
                Parameter(
                    id = "config",
                    names = Seq("--config", "-c"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        "Configuration absolute file path. Server will automatically look for 'nlpcraft.conf' " +
                        "configuration file in the same directory as NLPCraft JAR file. If the configuration file has " +
                        "different name or in different location use this parameter to provide an alternative path. " +
                        "Note that the REST server and the data probe can use the same file for their configuration."
                ),
                Parameter(
                    id = "igniteConfig",
                    names = Seq("--ignite-config", "-i"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        "Apache Ignite configuration absolute file path. Note that Apache Ignite is used as a cluster " +
                        "computing plane and a default distributed storage. REST server will automatically look for " +
                        "'ignite.xml' configuration file in the same directory as NLPCraft JAR file. If the " +
                        "configuration file has different name or in different location use this parameter to " +
                        "provide an alternative path."
                ),
                Parameter(
                    id = "output",
                    names = Seq("--output-path", "-o"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        "File path for both REST server stdout and stderr output. If not provided, the REST server" +
                        s"output will be piped into '$${USER_HOME}/.nlpcraft/server-output-xxx.txt' file."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME start-server"),
                    desc = "Starts REST server with default configuration."
                ),
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME start-server -c=/opt/nlpcraft/nlpcraft.conf"),
                    desc = "Starts REST server with alternative configuration file."
                )
            )
        ),
        Command(
            id = "no-ansi",
            names = Seq("no-ansi"),
            synopsis = s"Disables usage of ANSI escape codes (colors & terminal controls).",
            desc = Some(
                s"This is a special command that can be combined with any other commands."
            ),
            body = cmdNoAnsi,
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -c=repl no-ansi"),
                    desc = "Displays help for 'repl' commands without using ANSI escape sequences."
                )
            )
        ),
        Command(
            id = "ping-server",
            names = Seq("ping-server"),
            synopsis = s"Pings REST server.",
            desc = Some(
                s"REST server is pinged using '/health' REST call to check its live status."
            ),
            body = cmdPingServer,
            params = Seq(
                Parameter(
                    id = "endpoint",
                    names = Seq("--endpoint", "-e"),
                    value = Some("url"),
                    optional = true,
                    desc =
                        "REST server endpoint in 'http{s}://hostname:port' format. " +
                        "Default value is http://localhost:8081"
                ),
                Parameter(
                    id = "number",
                    names = Seq("--number", "-n"),
                    value = Some("num"),
                    optional = true,
                    desc =
                        "Number of pings to perform. Must be an integer > 0."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME ping-server"),
                    desc = "Pings REST server one time."
                ),
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME ping-server ",
                        s"    --endpoint=http://localhost:1234 ",
                        s"    -n=10"
                    ),
                    desc = "Pings REST server at 'http://localhost:1234' endpoint 10 times."
                )
            )
        ),
        Command(
            id = "stop-server",
            names = Seq("stop-server"),
            synopsis = s"Stops local REST server.",
            desc = Some(
                s"Local REST server must be started via $SCRIPT_NAME or similar way."
            ),
            body = cmdStopServer
        ),
        Command(
            id = "help",
            names = Seq("help", "?"),
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
            id = "ver",
            names = Seq("version", "ver"),
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
            id = "repl",
            names = Seq("repl"),
            synopsis = s"Starts '$SCRIPT_NAME' in interactive REPL mode.",
            body = cmdRepl
        )
    ).sortBy(_.id)

    private final val HELP_CMD = CMDS.find(_.id ==  "help").get
    private final val DFLT_CMD = CMDS.find(_.id ==  "repl").get
    private final val NO_ANSI_CMD = CMDS.find(_.id ==  "no-ansi").get

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdStartServer(cmd: Command, args: Seq[Argument]): Unit = {
        val cfgPath = args.find(_.parameter.id == "config")
        val igniteCfgPath = args.find(_.parameter.id == "igniteConfig")
        val output = args.find(_.parameter.id == "output") match {
            case Some(arg) ⇒ new File(arg.value.get)
            case None ⇒ new File(SystemUtils.getUserHome, s".nlpcraft/server-output-$currentTime.txt")
        }

        val pb = new ProcessBuilder(
            JAVA,
            "-ea",
            "-Xms2048m",
            "-XX:+UseG1GC",
            "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED",
            "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
            "--add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED",
            "--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
            "--add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED",
            "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
            "--illegal-access=permit",
            "-DNLPCRAFT_ANSI_COLOR_DISABLED=true",
            "-cp",
            s"$JAVA_CP",
            "org.apache.nlpcraft.NCStart",
            "-server",
            cfgPath match {
                case Some(path) ⇒ s"-config=$path"
                case None ⇒ ""
            },
            igniteCfgPath match {
                case Some(path) ⇒ s"-igniteConfig=$path"
                case None ⇒ ""
            },
        )

        pb.directory(new File(INSTALL_HOME))
        pb.redirectErrorStream(true)
        pb.redirectOutput(Redirect.appendTo(output))

        try {
            pb.start()

            logln(s"REST server is starting, output redirected to ${c(output.getAbsolutePath)}")
            logln(s"Use ${g("stop-server")} command to stop it.")
        }
        catch {
            case e: Exception ⇒ error(s"REST server failed to start: ${y(e.getLocalizedMessage)}")
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdPingServer(cmd: Command, args: Seq[Argument]): Unit = {
        val endpoint = args.find(_.parameter.id == "endpoint") match {
            case Some(arg) ⇒ new URL(arg.value.get).toURI.toString
            case None ⇒ "http://localhost:8081"
        }
        val num = args.find(_.parameter.id == "number") match {
            case Some(arg) ⇒
                try
                    Integer.parseInt(arg.value.get)
                catch {
                    case _ :Exception ⇒ throw new IllegalArgumentException(s"Invalid number of pings: ${arg.value.get}")
                }

            case None ⇒ 1
        }

        var i = 0

        while (i < num) {
            log(s"Pinging REST server at ${b(endpoint)} ")

            val spinner = new NCAnsiSpinner(
                System.out,
                ansiCyanFg,
                // ANSI is NOT disabled & we ARE NOT running from IDEA or Eclipse...
                NCAnsi.isEnabled && U.sysEnv("NLPCRAFT_CLI").isDefined
            )

            spinner.start()

            val startMs = currentTime

            try
                httpGet(endpoint, "health", new ResponseHandler[Int]() {
                    override def handleResponse(resp: HttpResponse): Int = resp.getStatusLine.getStatusCode
                }) match {
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

            // Pause between pings.
            Thread.sleep(1000)
        }
    }

    /**
     * Loads server beacon file and return its data and its corresponding process handle.
     *
     * @return
     */
    private def loadServerBeacon(): Option[(NCCliServerBeacon, ProcessHandle)] =
        try {
            val rawObj = managed(
                new ObjectInputStream(
                    new FileInputStream(
                        new File(SystemUtils.getUserHome, SRV_BEACON_PATH)
                    )
                )
            ) acquireAndGet {
                _.readObject()
            }

            val beacon = rawObj.asInstanceOf[NCCliServerBeacon]

            ProcessHandle.of(beacon.pid).asScala.map(beacon → _)
        }
        catch {
            case _: Exception ⇒ None
        }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdStopServer(cmd: Command, args: Seq[Argument]): Unit = {
        loadServerBeacon() match {
            case Some((beacon, ph)) ⇒
                val pid = beacon.pid

                if (ph.destroy())
                    logln(s"Local REST server (pid ${c(pid.toString)}) has been stopped.")
                else
                    error(s"Failed to stop the local REST server (pid ${c(pid.toString)}).")

            case None ⇒
                error("Cannot detect locally running REST server.")
        }
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdNoAnsi(cmd: Command, args: Seq[Argument]): Unit = {
        NCAnsi.setEnabled(false)
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdHelp(cmd: Command, args: Seq[Argument]): Unit = {
        /**
         *
         */
        def header(): Unit = logln(
            s"""|${U.asciiLogo()}
                |${ansiBold("NAME")}
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

        val tbl = NCAsciiTable().margin(left = 4)

        if (args.isEmpty) { // Default - show abbreviated help.
            header()

            CMDS.foreach(cmd ⇒ tbl +/ (
                "" → cmd.names.mkString(ansiGreenFg, ", ", ansiReset),
                "align:left, maxWidth:85" → cmd.synopsis
            ))

            logln(tbl.toString)
        }
        else if (args.size == 1 && args.head.parameter.id == "all") { // Show a full format help for all commands.
            header()

            CMDS.foreach(cmd ⇒
                tbl +/ (
                    "" → cmd.names.mkString(ansiGreenFg, ", ", ansiReset),
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

                CMDS.find(_.names.contains(cmdName)) match {
                    case Some(c) ⇒
                        if (!seen.contains(c.id)) {
                            tbl +/ (
                                "" → c.names.mkString(ansiGreenFg, ", ", ansiReset),
                                "align:left, maxWidth:85" → mkCmdLines(c)
                            )

                            seen += c.id
                        }
                    case None ⇒
                        err = true
                        error(s"Unknown command to get help for: $cmdName")
                }
            }

            if (!err) {
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
        tbl += ("JDBC URL", s"${g(beacon.jdbcUrl)}")
        tbl += ("REST endpoint", s"${g(beacon.restEndpoint)}")
        tbl += ("Uplink", s"${g(beacon.upLink)}")
        tbl += ("Downlink", s"${g(beacon.downLink)}")
        tbl += ("Started on", s"${g(DateFormat.getDateTimeInstance.format(new Date(beacon.startMs)))}")

        tbl
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdRepl(cmd: Command, args: Seq[Argument]): Unit = {
        loadServerBeacon() match {
            case Some((beacon, _)) ⇒ logln(s"Local REST server detected:\n${mkServerBeaconTable(beacon).toString}")
            case None ⇒ ()
        }

        logln(s"Type ${c("help")} or ${c("help -c=repl")} to get help.")
        logln(s"Type ${c("quit")} to exit.")

        val in = new BufferedReader(new InputStreamReader(System.in))

        val QUITS = Seq(
            "quit", "exit", "/q", "\\q"
        )

        var exit = false

        while (!exit) {
            log(s"${g(">")} ")

            val rawLine = in.readLine()

            if (rawLine == null || QUITS.contains(rawLine.trim))
                exit = true
            else {
                val line = rawLine.trim()

                try {
                    val args = splitBySpace(line)

                    logln(args.mkString(", "))
                }
                catch {
                    case e: SplitError ⇒ logln(s"Error around index ${e.index}")
                }
            }
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdVersion(cmd: Command, args: Seq[Argument]): Unit =
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
                error(s"Invalid parameters for command '${cmd.mainName}': ${args.mkString(", ")}")
        }


    /**
     *
     * @param msg
     */
    private def error(msg: String = ""): Unit = {
        // Make sure we exit with non-zero status.
        exitStatus = 1

        val msg2 = if (msg.head.isLower) msg.head.toUpper + msg.tail else msg

        System.err.println(s"${r("ERR:")} $msg2")
    }

    /**
     *
     * @param msg
     */
    private def logln(msg: String = ""): Unit = System.out.println(msg)

    /**
     *
     * @param msg
     */
    private def log(msg: String = ""): Unit = System.out.print(msg)

    /**
     *
     */
    private def errorHelp(): Unit =
        error(s"Run '${c(SCRIPT_NAME + " " + HELP_CMD.mainName)}' to read the manual.")

    /**
     * Prints out the version and copyright title header.
     */
    private def title(): Unit = {
        logln(s"$NAME ver. ${VER.version}")
        logln()
    }

    /**
     *
     * @param baseUrl
     * @param cmd
     * @return
     */
    private def prepUrl(baseUrl: String, cmd: String): String =
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
        val post = new HttpPost(prepUrl(baseUrl, cmd))

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
        val bldr = new URIBuilder(prepUrl(baseUrl, cmd))

        jsParams.foreach(p ⇒ bldr.setParameter(p._1, p._2.toString))

        val get = new HttpGet(bldr.build())

        try
            HttpClients.createDefault().execute(get, resp)
        finally
            get.releaseConnection()
    }

    case class SplitError(index: Int) extends Exception

    /**
     * Splits given string by spaces taking into an account double and single quotes,
     * and checking for uneven <>, {}, [], () pairs.
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

        def head: Char = stack.headOption.getOrElse(Char.MinValue)

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
                    else if (head == ch)
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
                if (head != BRK_PAIR(ch))
                    throw SplitError(index)

                stack = stack.tail // Pop.

                buf += ch
            }
            else
                buf += ch

            // Drop escape flag.
            if (escape && ch != '\\')
                escape = false

            index += 1
        }

        if (stack.nonEmpty)
            throw SplitError(index)

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
     * Processes a single command defined by the given arguments.
     *
     * @param args
     * @param repl Whether or not called from 'repl' command.
     */
    private def doCommand(args: Seq[String], repl: Boolean = false): Unit = {
        // Process 'no-ansi' command first, if any, and remove it from the list.
        args.find(arg ⇒ NO_ANSI_CMD.names.contains(arg)) match {
            case Some(_) ⇒ NO_ANSI_CMD.body(NO_ANSI_CMD, Seq.empty)
            case None ⇒ ()
        }

        // Remove 'no-ansi' command from the argument list, if any.
        val xargs = args.filter(arg ⇒ !NO_ANSI_CMD.names.contains(arg))

        val cmd = xargs.head

        CMDS.find(_.extNames.contains(cmd)) match {
            case Some(cmd) ⇒
                if (!(repl && cmd.id == "repl")) // Don't call 'repl' from 'repl'.
                    try
                        cmd.body(cmd, processParameters(cmd, xargs.tail))
                    catch {
                        case e: Exception ⇒ error(e.getLocalizedMessage)
                    }

            case None ⇒ error(s"Unknown command: $cmd")
        }
    }

    /**
     *
     * @param args
     */
    private def boot(args: Array[String]): Unit = {
        title()

        if (args.isEmpty)
            DFLT_CMD.body(DFLT_CMD, Seq.empty)
        else
            doCommand(args.toSeq)

        if (exitStatus != 0)
            errorHelp()

        sys.exit(exitStatus)
    }

    // Boot up.
    boot(args)
}
