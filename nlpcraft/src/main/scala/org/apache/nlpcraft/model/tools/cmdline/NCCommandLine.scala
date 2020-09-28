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
import org.apache.commons.lang3.SystemUtils
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsi
import org.apache.nlpcraft.common.ansi.NCAnsi._
import org.apache.nlpcraft.common.version.NCVersion
import resource.managed

import scala.collection.mutable
import scala.compat.java8.OptionConverters._
import scala.util.Try
import scala.collection.JavaConverters._

/**
 * 'nlpcraft' script entry point.
 */
object NCCommandLine extends App {
    private final val NAME = "Apache NLPCraft CLI"

    private final val SRV_PID_PATH = ".nlpcraft/server_pid"

    private final lazy val VER = NCVersion.getCurrent
    private final lazy val INSTALL_HOME = U.sysEnv("NLPCRAFT_CLI_INSTALL_HOME").getOrElse(
        SystemUtils.USER_DIR
    )
    private final lazy val SCRIPT_NAME = U.sysEnv("NLPCRAFT_CLI_SCRIPT").getOrElse(
        s"nlpcraft.${if (SystemUtils.IS_OS_UNIX) "sh" else "cmd"}"
    )
    private final lazy val PROMPT = if (SCRIPT_NAME.endsWith("cmd")) ">" else "$"

    private final val T___ = "    "

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
                    id = "outputPath",
                    names = Seq("--output-path", "-o"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        "File path for both REST server stdout and stderr output. If not provided, the REST server" +
                        s"output will be piped into '$${USER_HOME}/.nlpcraft/server-output.txt' file."
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
                        "Default value is https://localhost:8081"
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
                        s"    --endpoint=https://localhost:1234 ",
                        s"    -n=10"
                    ),
                    desc = "Pings REST server at 'https://localhost:1234' endpoint 10 times."
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
        title()

        // TODO
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdPingServer(cmd: Command, args: Seq[Argument]): Unit = {
        title()

        var endpoint = "https://localhost:8081"
        var num = 1

        val endpointParam = cmd.params.find(_.id == "endpoint").get
        val numberParam = cmd.params.find(_.id == "number").get

        // TODO
    }

    /**
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdStopServer(cmd: Command, args: Seq[Argument]): Unit = {
        title()

        val path = new File(SystemUtils.getUserHome, SRV_PID_PATH)
        var pid = -1L

        if (path.exists())
            pid =
                Try {
                    managed(new ObjectInputStream(new FileInputStream(path))) acquireAndGet { _.readLong() }
                }
                .getOrElse(-1L)

        if (pid == -1)
            error("Cannot detect locally running server.")
        else {
            ProcessHandle.of(pid).asScala match {
                case Some(ph) ⇒
                    if (ph.destroy())
                        `>`("Local server has been stopped.")
                    else
                        error(s"Unable to stop the local server [pid=$pid]")


                case None ⇒ error("Cannot find locally running server.")
            }
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
        title()

        /**
         *
         */
        def header(): Unit = log(
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

                    lines += ansiCyan(line)

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
                    lines ++= ex.usage.map(s ⇒ ansiYellow(s"$T___$s"))
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

            log(tbl.toString)
        }
        else if (args.size == 1 && args.head.parameter.id == "all") { // Show a full format help for all commands.
            header()

            CMDS.foreach(cmd ⇒
                tbl +/ (
                    "" → cmd.names.mkString(ansiGreenFg, ", ", ansiReset),
                    "align:left, maxWidth:85" → mkCmdLines(cmd)
                )
            )

            log(tbl.toString)
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

                log(tbl.toString)
            }
        }
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdRepl(cmd: Command, args: Seq[Argument]): Unit = {
        title()

        // TODO
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param args Arguments, if any, for this command.
     */
    private def cmdVersion(cmd: Command, args: Seq[Argument]): Unit =
        if (args.isEmpty)
            `>`(s"$NAME ver. ${VER.version}, released on ${VER.date}")
        else {
            val isS = args.exists(_.parameter.id == "semver")
            val isD = args.exists(_.parameter.id == "reldate")

            if (isS || isD) {
                if (isS)
                    `>`(s"${VER.version}")
                if (isD)
                    `>`(s"${VER.date}")
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

        System.err.println(s"${ansiRed("ERR")} $msg")
    }

    /**
     *
     * @param msg
     */
    private def log(msg: String = ""): Unit = System.out.println(msg)

    /**
     *
     * @param msg
     */
    private def `>`(msg: String): Unit = System.out.println(s"${ansiGreen(">")} $msg")

    /**
     *
     * @param msg
     */
    private def `>>`(msg: String): Unit = System.out.print(s"${ansiGreen(">")} $msg")

    /**
     *
     */
    private def errorHelp(): Unit =
        error(s"Run '${ansiCyan(SCRIPT_NAME + " " + HELP_CMD.mainName)}' to read the manual.")

    /**
     * Prints out the version and copyright title header.
     */
    private def title(): Unit = {
        log(s"$NAME ver. ${VER.version}")
        log()
    }

    /**
     * Posts REST request.
     *
     * @param url
     * @param resp
     * @param jsParams
     * @return
     * @throws IOException
     */
    private def post[T](url: String, resp: ResponseHandler[T], jsParams: (String, AnyRef)*): T = {
        val post = new HttpPost(url)

        post.setHeader("Content-Type", "application/json")
        post.setEntity(new StringEntity(gson.toJson(jsParams.filter(_._2 != null).toMap.asJava), "UTF-8"))

        try
            HttpClients.createDefault().execute(post, resp)
        finally
            post.releaseConnection()
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

            def mkError() = new IllegalArgumentException(s"Invalid parameter: ${ansiCyan(arg)}")

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
     */
    private def boot(args: Array[String]): Unit = {
        if (args.isEmpty)
            DFLT_CMD.body(DFLT_CMD, Seq.empty)
        else {
            // Handle 'no-ansi' command right away and remove it from the list.
            args.find(arg ⇒ NO_ANSI_CMD.names.contains(arg)) match {
                case Some(_) ⇒ NO_ANSI_CMD.body(NO_ANSI_CMD, Seq.empty)
                case None ⇒ ()
            }

            val xargs = args.filter(arg ⇒ !NO_ANSI_CMD.names.contains(arg))

            val cmdName = xargs.head

            CMDS.find(_.extNames.contains(cmdName)) match {
                case Some(cmd) ⇒
                    try
                        cmd.body(cmd, processParameters(cmd, xargs.tail))
                    catch {
                        case e: IllegalArgumentException ⇒ error(e.getLocalizedMessage)
                    }

                case None ⇒ error(s"Unknown command: $cmdName")
            }
        }

        if (exitStatus != 0)
            errorHelp()

        sys.exit(exitStatus)
    }

    // Boot up.
    boot(args)
}
