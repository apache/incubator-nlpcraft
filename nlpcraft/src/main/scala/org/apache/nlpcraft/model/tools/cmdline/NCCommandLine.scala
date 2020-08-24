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

import org.apache.nlpcraft.common.util.NCUtils
import org.apache.nlpcraft.common.version.NCVersion

/**
 * 'nlpcraft' script entry point.
 */
object NCCommandLine extends App {
    private final val NAME = "Apache NLPCraft CLI"

    private final lazy val VER = NCVersion.getCurrent
    private final lazy val SCRIPT = NCUtils.sysEnv("NLPCRAFT_CLI_SCRIPT")

    // Single CLI command.
    case class Command(
        name: String,
        desc: String,
        params: Seq[Parameter] = Seq.empty,
        examples: Seq[String] = Seq.empty,
        body: (Seq[String]) => Unit
    )
    // Single command's parameter.
    case class Parameter(
        names: Seq[String],
        optional: Boolean = false,
        desc: String
    )

    // All supported commands.
    private final val CMDS = Seq(
        Command(
            name = "help",
            desc = s"Displays manual page for $NAME.",
            body = cmdHelp
        ),
        Command(
            name = "version",
            desc = s"Displays version of $NAME runtime.",
            body = cmdVersion
        ),
        Command(
            name = "repl",
            desc = s"Starts '$NAME' in interactive REPL mode.",
            body = cmdRepl
        )
    )

    private final val HELP_CMD = CMDS.find(_.name == "help").get
    private final val DFLT_CMD = CMDS.find(_.name == "repl").get

    /**
     *
     * @param params
     */
    private def cmdHelp(params: Seq[String]): Unit = {

    }

    /**
     *
     * @param params
     */
    private def cmdRepl(params: Seq[String]): Unit = {

    }

    /**
     *
     * @param params
     */
    private def cmdVersion(params: Seq[String]): Unit = {
        // Nothing - common header with version will be printed before anyways.
    }

    private def error(msg: String = ""): Unit = System.err.println(msg)
    private def log(msg: String = ""): Unit = System.out.println(msg)
    private def errorHelp(): Unit = SCRIPT match {
        // Running from *.{s|cmd} script.
        case Some(script) => error(s"Run '$script ${HELP_CMD.name}' to read the manual.")
        // Running from IDE.
        case None => error(s"Run the process with '${HELP_CMD.name}' parameter to read the manual.")
    }
    
    /**
     *
     * @param args
     */
    private def boot(args: Array[String]): Unit = {
        var status = 0

        // Print common version & copyright header.
        log(s"$NAME ver. ${VER.version}, rel. ${VER.date}")
        log(NCVersion.copyright)
        log()

        if (args.isEmpty)
            NCCommandLine.DFLT_CMD.body(Seq.empty)
        else {
            val cmdName = args.head
            val extCmdNames = Seq( // Safeguard against "common" errors.
                cmdName,
                s"-$cmdName",
                s"--$cmdName",
                s"/$cmdName",
                s"\\$cmdName"
            )

            CMDS.find(c => extCmdNames.contains(c.name)) match {
                case Some(cmd) =>
                case None =>
                    error(s"Unknown '$cmdName' command.")
                    errorHelp()

                    status = 1
            }
        }

        sys.exit(status)
    }

    // Boot up.
    boot(args)
}
