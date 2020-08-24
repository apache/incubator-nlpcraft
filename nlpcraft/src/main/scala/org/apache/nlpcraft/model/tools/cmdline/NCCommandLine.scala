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

import org.apache.nlpcraft.common.version.NCVersion

/**
 * 'nlpcraft' script entry point.
 */
object NCCommandLine extends App {
    private final val NAME = "NLPCraft CLI"

    case class Parameter(
        names: Seq[String],
        optional: Boolean = false,
        desc: String
    )
    case class Command(
        name: String,
        desc: String,
        params: Seq[Parameter] = Seq.empty,
        examples: Seq[String] = Seq.empty,
        body: (Seq[String]) => Unit
    )

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
        val curVer = NCVersion.getCurrent

        log(s"$NAME ver. ${curVer.version} released on ${curVer.date}")
    }

    private def error(msg: String): Unit = System.err.println(msg)
    private def log(msg: String): Unit = System.out.println(msg)

    /**
     *
     * @param args
     */
    private def boot(args: Array[String]): Unit = {

    }

    // Boot up.
    boot(args)
}
