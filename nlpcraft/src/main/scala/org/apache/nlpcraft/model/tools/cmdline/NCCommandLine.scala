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

import org.apache.nlpcraft.common.ascii.NCAsciiTable
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.ansi.NCAnsiColor._
import org.apache.nlpcraft.common.version.NCVersion
import scala.collection.mutable

/**
 * 'nlpcraft' script entry point.
 */
object NCCommandLine extends App {
    private final val NAME = "Apache NLPCraft CLI"

    private final lazy val VER = NCVersion.getCurrent
    private final lazy val INSTALL_HOME = U.sysEnv("NLPCRAFT_CLI_INSTALL_HOME").getOrElse(
        assert(assertion = false, "System property 'NLPCRAFT_CLI_INSTALL_HOME' not defined.")
    )
    private final lazy val SCRIPT_NAME = U.sysEnv("NLPCRAFT_CLI_SCRIPT").getOrElse(
        assert(assertion = false, "System property 'NLPCRAFT_CLI_SCRIPT' not defined.")
    )

    private final val T___ = "    "

    private var exitStatus = 0

    // Single CLI command.
    case class Command(
        id: String,
        names: Seq[String],
        synopsis: String,
        desc: Option[String] = None,
        params: Seq[Parameter] = Seq.empty,
        examples: Seq[Example] = Seq.empty,
        body: (Command, Seq[String]) ⇒ Unit
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
         * @param paramId
         * @param cliParams
         * @return
         */
        def isParamPresent(paramId: String, cliParams: Seq[String]): Boolean =
            params.find(_.id == paramId).get.names.intersect(cliParams).nonEmpty
    }
    // Single command's example.
    case class Example(
        code: String,
        desc: String
    )
    // Single command's parameter.
    case class Parameter(
        id: String,
        names: Seq[String],
        valueDesc: Option[String] = None,
        optional: Boolean = false, // Mandatory by default.
        desc: String
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
                    valueDesc = Some("{path}"),
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
                    valueDesc = Some("{path}"),
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
                    valueDesc = Some("{path}"),
                    optional = true,
                    desc =
                        "File path for both REST server stdout and stderr output. If not provided, the REST server" +
                        s"output will be piped into '$${USER_HOME}/.nlpcraft/server-output.txt' file."
                )
            ),
            examples = Seq(
                Example(
                    code = s"$$ $SCRIPT_NAME start-server",
                    desc = "Starts REST server with default configuration."
                ),
                Example(
                    code = s"$$ $SCRIPT_NAME start-server -c=/opt/nlpcraft/nlpcraft.conf",
                    desc = "Starts REST server with alternative configuration file."
                )
            )
        ),
        Command(
            id = "stop-server",
            names = Seq("stop-server"),
            synopsis = s"Stops local REST server.",
            desc = Some(
                s"Local REST server must be started via $SCRIPT_NAME or ."
            ),
            body = cmdStartServer,
            params = Seq(
                Parameter(
                    id = "config",
                    names = Seq("--config", "-c"),
                    valueDesc = Some("{path}"),
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
                    valueDesc = Some("{path}"),
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
                    valueDesc = Some("{path}"),
                    optional = true,
                    desc =
                        "File path for both REST server stdout and stderr output. If not provided, the REST server" +
                            s"output will be piped into '$${USER_HOME}/.nlpcraft/server-output.txt' file."
                )
            ),
            examples = Seq(
                Example(
                    code = s"$$ $SCRIPT_NAME start-server",
                    desc = "Starts REST server with default configuration."
                ),
                Example(
                    code = s"$$ $SCRIPT_NAME start-server -c=/opt/nlpcraft/nlpcraft.conf",
                    desc = "Starts REST server with alternative configuration file."
                )
            )
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
                    valueDesc = Some("{cmd}"),
                    optional = true,
                    desc = "Set of commands to show the manual for."
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
                    code = s"$$ $SCRIPT_NAME help -c=repl --cmd=ver",
                    desc = "Displays help for 'repl' and 'version' commands."
                ),
                Example(
                    code = s"$$ $SCRIPT_NAME help -all",
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
                    valueDesc = None,
                    optional = true,
                    desc = s"Display only the semantic version value, e.g. ${VER.version}."
                ),
                Parameter(
                    id = "reldate",
                    names = Seq("--rel-date", "-d"),
                    valueDesc = None,
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

    private final val HELP_CMD = CMDS.find(_.id == "help").get
    private final val DFLT_CMD = CMDS.find(_.id ==  "repl").get

    /**
     *
     * @param param Expected parameter.
     * @param str String to parse.
     * @return
     */
    private def getParamValue(param: Parameter, str: String): Option[String] = {
        val arr = str.split("=")

        if (arr.size != 2) {
            error(s"Invalid parameter format: $str")

            None
        }
        else if (!param.names.contains(arr.head)) {
            error(s"Unknown parameter in: $str")

            None

        }
        else
            Some(arr.last)
    }

    /**
     * @param cmd Command descriptor.
     * @param params Parameters, if any, for this command.
     */
    private def cmdStartServer(cmd: Command, params: Seq[String]): Unit = {
        title()

        // TODO
    }

    /**
     * @param cmd Command descriptor.
     * @param params Parameters, if any, for this command.
     */
    private def cmdHelp(cmd: Command, params: Seq[String]): Unit = {
        title()

        /**
         *
         */
        def header(): Unit = log(
            s"""|${U.asciiLogo()}
                |${ansiBold}NAME${ansiReset}
                |$T___$SCRIPT_NAME - command line interface to control NLPCraft.
                |
                |${ansiBold}USAGE${ansiReset}
                |$T___$SCRIPT_NAME [COMMAND] [PARAMETERS]
                |
                |${ansiBold}COMMANDS${ansiReset}""".stripMargin
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
                lines += s"${ansiBold}PARAMETERS:${ansiReset}"

                for (param ← cmd.params) {
                    val line =
                        if (param.valueDesc.isDefined)
                            T___ + param.names.zip(Stream.continually(param.valueDesc.get)).map(t ⇒ s"${t._1}=${t._2}").mkString(", ")
                        else
                            s"$T___${param.names.mkString(", ")}"

                    lines += s"${ansiCyanFg}$line${ansiReset}"

                    if (param.optional)
                        lines += s"$T___${T___}Optional."

                    lines += s"$T___$T___${param.desc}"
                    lines += ""
                }

                lines.remove(lines.size - 1) // Remove last empty line.
            }

            if (cmd.examples.nonEmpty) {
                lines += ""
                lines += s"${ansiBold}EXAMPLES:${ansiReset}"

                for (ex ← cmd.examples) {
                    lines += s"$T___${ex.code}"
                    lines += s"$T___$T___${ex.desc}"
                }
            }

            lines
        }

        val tbl = NCAsciiTable().margin(left = 4)

        if (params.isEmpty) { // Default - show abbreviated help.
            header()

            CMDS.foreach(cmd ⇒ tbl +/ (
                "" → cmd.names.mkString(ansiGreenFg, ", ", ansiReset),
                "align:left, maxWidth:65" → cmd.synopsis
            ))

            log(tbl.toString)
        }
        else if (cmd.isParamPresent("all", params)) { // Show a full format help for all commands.
            header()

            CMDS.foreach(cmd ⇒
                tbl +/ (
                    "" → cmd.names.mkString(ansiGreenFg, ", ", ansiReset),
                    "align:left, maxWidth:65" → mkCmdLines(cmd)
                )
            )

            log(tbl.toString)
        }
        else { // Help for individual commands.
            var err = false
            val cmdParam = cmd.params.find(_.id == "cmd").get
            val seen = mutable.Buffer.empty[String]

            // At this point it should only be '--cmd' parameters.
            for (param ← params) {
                getParamValue(cmdParam, param) match {
                    case Some(value) ⇒
                        CMDS.find(_.names.contains(value)) match {
                            case Some(c) ⇒
                                if (!seen.contains(c.id)) {
                                    tbl +/ (
                                        "" → cmd.names.mkString(ansiGreenFg, ", ", ansiReset),
                                        "align:left, maxWidth:65" → mkCmdLines(c)
                                    )

                                    seen += c.id
                                }
                            case None ⇒
                                err = true
                                error(s"Unknown command '$value' to get help for in: $param")
                        }

                    case None ⇒ err = true
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
     * @param params Parameters, if any, for this command.
     */
    private def cmdRepl(cmd: Command, params: Seq[String]): Unit = {
        title()

        // TODO
    }

    /**
     *
     * @param cmd Command descriptor.
     * @param params Parameters, if any, for this command.
     */
    private def cmdVersion(cmd: Command, params: Seq[String]): Unit =
        if (params.isEmpty)
            title()
        else {
            val isS = cmd.isParamPresent("semver", params)
            val isD = cmd.isParamPresent("reldate", params)

            if (isS || isD) {
                if (isS)
                    log(s"${VER.version}")
                if (isD)
                    log(s"${VER.date}")
            }
            else
                error(s"Invalid parameters for command '${cmd.mainName}': ${params.mkString(", ")}")
        }


    /**
     *
     * @param msg
     */
    private def error(msg: String = ""): Unit = {
        // Make sure we exit with non-zero status.
        exitStatus = 1

        System.err.println(s"ERROR: $msg")
    }

    /**
     *
     * @param msg
     */
    private def log(msg: String = ""): Unit = System.out.println(msg)

    /**
     *
     */
    private def errorHelp(): Unit =
        error(s"Run '$SCRIPT_NAME ${HELP_CMD.mainName}' to read the manual.")

    /**
     * Prints out the version and copyright title header.
     */
    private def title(): Unit = {
        log(s"$NAME ver. ${VER.version}, rel. ${VER.date}")
        log(NCVersion.copyright)
        log()
    }

    /**
     *
     * @param args
     */
    private def boot(args: Array[String]): Unit = {
        if (args.isEmpty)
            NCCommandLine.DFLT_CMD.body(DFLT_CMD, Seq.empty)
        else {
            val cmdName = args.head

            CMDS.find(_.extNames.contains(cmdName)) match {
                case Some(cmd) ⇒ cmd.body(cmd, args.tail)
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
