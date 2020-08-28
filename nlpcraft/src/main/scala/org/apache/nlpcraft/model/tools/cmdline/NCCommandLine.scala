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
import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.common.version.NCVersion

import scala.collection.mutable

/**
 * 'nlpcraft' script entry point.
 */
object NCCommandLine extends App {
    private final val NAME = "Apache NLPCraft CLI"

    private final lazy val VER = NCVersion.getCurrent
    private final lazy val SCRIPT = NCUtils.sysEnv("NLPCRAFT_CLI_SCRIPT")
    private final lazy val SCRIPT_NAME = SCRIPT.getOrElse("<nlpcraft-cli>")

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
        body: (Command, Seq[String]) => Unit
    ) {
        final val extNames = names.flatMap(name => // Safeguard against "common" errors.
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
                    code = s"$$ $SCRIPT_NAME help repl",
                    desc = "Displays help for 'repl' command."
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
            synopsis = s"Displays full version of '$SCRIPT_NAME' runtime.",
            desc = Some(
                "Depending on the additional parameters can display only the semantic version or the release date."
            ),
            body = cmdVersion,
            params = Seq(
                Parameter(
                    id = "semver",
                    names = Seq("--semver", "-s"),
                    valueDesc = None,
                    optional = true,
                    desc = s"Display only the semantic version value, e.g. ${VER.version}."
                ),
                Parameter(
                    id = "reldate",
                    names = Seq("--reldate", "-d"),
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
    )

    private final val HELP_CMD = CMDS.find(_.id == "help").get
    private final val DFLT_CMD = CMDS.find(_.id ==  "repl").get

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
            s"""|NAME
                |$T___$SCRIPT_NAME - command line interface to control NLPCraft.
                |
                |USAGE
                |$T___$SCRIPT_NAME [COMMAND] [PARAMETERS]
                |
                |COMMANDS""".stripMargin
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
                lines += "PARAMETERS:"

                for (param <- cmd.params) {
                    var nameLine = s"$T___${param.names.mkString(", ")}"

                    if (param.valueDesc.isDefined)
                        nameLine += s"=${param.valueDesc.get}"

                    lines += nameLine

                    if (param.optional)
                        lines += s"$T___${T___}Optional."

                    val descLine = s"$T___$T___${param.desc}"

                    lines += descLine
                }
            }

            if (cmd.examples.nonEmpty) {
                lines += ""
                lines += "EXAMPLES:"

                for (ex <- cmd.examples) {
                    lines += s"$T___${ex.code}"
                    lines += s"$T___$T___${ex.desc}"
                }
            }

            lines
        }

        val tbl = NCAsciiTable().margin(left = 4)

        if (params.isEmpty) { // Default - show abbreviated help.
            header()

            CMDS.foreach(cmd => tbl +/ (
                "" -> cmd.names.mkString(", "),
                "align:left, maxWidth:65" -> cmd.synopsis
            ))

            log(tbl.toString)
        } else if (cmd.isParamPresent("all", params)) { // Show a full format help for all commands.
            header()

            CMDS.foreach(cmd =>
                tbl +/ (
                    "" -> cmd.names.mkString(", "),
                    "align:left, maxWidth:65" -> mkCmdLines(cmd)
                )
            )

            log(tbl.toString)
        } else {

            for (param <- params) {
                var err = false

                CMDS.find(_.names.contains(param)) match {
                    case Some(cmd) =>
                        tbl +/ (
                            "" -> cmd.names.mkString(", "),
                            "align:left, maxWidth:65" -> mkCmdLines(cmd)
                        )
                    case None =>
                        err = true
                        error(s"Unknown command to get help for: $param")
                }

                if (!err) {
                    header()

                    log(tbl.toString)
                }
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


    private def error(msg: String = ""): Unit = {
        // Make sure we exit with non-zero status.
        exitStatus = 1

        System.err.println(s"ERROR: $msg")
    }

    private def log(msg: String = ""): Unit = System.out.println(msg)

    private def errorHelp(): Unit = SCRIPT match {
        // Running from *.{s|cmd} script.
        case Some(script) => error(s"Run '$script ${HELP_CMD.mainName}' to read the manual.")
        // Running from IDE.
        case None => error(s"Run the process with '${HELP_CMD.mainName}' parameter to read the manual.")
    }

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
                case Some(cmd) => cmd.body(cmd, args.tail)
                case None => error(s"Unknown command: $cmdName")
            }
        }

        if (exitStatus != 0)
            errorHelp()

        sys.exit(exitStatus)
    }

    // Boot up.
    boot(args)
}
