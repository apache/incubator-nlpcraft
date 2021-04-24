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

import org.apache.commons.lang3.SystemUtils
import org.apache.nlpcraft.common.version.NCVersion
import org.apache.nlpcraft.common._
import java.lang.{IllegalStateException => ISE}

import java.io.File
import java.lang.management.ManagementFactory
import java.util.regex.Pattern

/**
 *
 */
class NCCliBase extends App {
    final val NAME = "NLPCraft CLI"

    private final val dir = ".nlpcraft"

    /*
     * Disable warnings from Ignite on JDK 11.
     */
    final val JVM_OPTS_RT_WARNS = Seq (
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED",
        "--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
        "--add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED",
        "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
        "--illegal-access=permit"
    )

    //noinspection RegExpRedundantEscape
    final val TAILER_PTRN = Pattern.compile("^.*NC[a-zA-Z0-9]+ started \\[[\\d]+ms\\]$")
    final val CMD_NAME = Pattern.compile("(^\\s*[\\w-]+)(\\s)")
    final val CMD_PARAM = Pattern.compile("(\\s)(--?[\\w-]+)")

    // Number of server and probe services that need to be started + 1 progress start.
    // Used for progress bar functionality.
    // +==================================================================+
    // | MAKE SURE TO UPDATE THIS VAR WHEN NUMBER OF SERVICES IS CHANGED. |
    // +^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^+
    final val NUM_SRV_SERVICES = 31 /*services*/ + 1 /*progress start*/
    final val NUM_PRB_SERVICES = 23 /*services*/ + 1 /*progress start*/
    // +^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^+
    // | MAKE SURE TO UPDATE THIS VAR WHEN NUMBER OF SERVICES IS CHANGED. |
    // +==================================================================+

    final val SRV_BEACON_PATH = s"$dir/server_beacon"
    final val PRB_BEACON_PATH = s"$dir/probe_beacon"
    final val HIST_PATH = s"$dir/.cli_history"

    final val DFLT_USER_EMAIL = "admin@admin.com"
    final val DFLT_USER_PASSWD = "admin"

    final val VER = NCVersion.getCurrent
    final val CP_WIN_NIX_SEPS_REGEX = "[:;]"
    final val CP_SEP = File.pathSeparator
    final val JAVA = U.sysEnv("NLPCRAFT_CLI_JAVA").getOrElse(new File(SystemUtils.getJavaHome, s"bin/java${if (SystemUtils.IS_OS_UNIX) "" else ".exe"}").getAbsolutePath)
    final val USR_WORK_DIR = SystemUtils.USER_DIR
    final val USR_HOME_DIR = SystemUtils.USER_HOME
    final val INSTALL_HOME = U.sysEnv("NLPCRAFT_CLI_INSTALL_HOME").getOrElse(USR_WORK_DIR)
    final val JAVA_CP = U.sysEnv("NLPCRAFT_CLI_CP").getOrElse(ManagementFactory.getRuntimeMXBean.getClassPath)
    final val SCRIPT_NAME = U.sysEnv("NLPCRAFT_CLI_SCRIPT").getOrElse(s"nlpcraft.${if (SystemUtils.IS_OS_UNIX) "sh" else "cmd"}")
    final val PROMPT = if (SCRIPT_NAME.endsWith("cmd")) ">" else "$"
    final val IS_SCRIPT = U.sysEnv("NLPCRAFT_CLI").isDefined
    final val T___ = "    "
    final val OPEN_BRK = Seq('[', '{', '(')
    final val CLOSE_BRK = Seq(']', '}', ')')
    final val BRK_PAIR = OPEN_BRK.zip(CLOSE_BRK).toMap ++ CLOSE_BRK.zip(OPEN_BRK).toMap // Pair for each open or close bracket.

    // See NCProbeMdo.
    case class Probe(
        probeToken: String,
        probeId: String,
        probeGuid: String,
        probeApiVersion: String,
        probeApiDate: String,
        osVersion: String,
        osName: String,
        osArch: String,
        startTstamp: Long,
        tmzId: String,
        tmzAbbr: String,
        tmzName: String,
        userName: String,
        javaVersion: String,
        javaVendor: String,
        hostName: String,
        hostAddr: String,
        macAddr: String,
        models: Array[ProbeModel]
    )

    // See NCProbeModelMdo.
    case class ProbeModel(
         id: String,
         name: String,
         version: String,
         enabledBuiltInTokens: Array[String]
    )

    case class ProbeAllResponse(
       probes: Array[Probe],
       status: String
    )

    private def help(cmd: Command): String = s"type $C'help --cmd=${cmd.name}'$RST to get help."

    case class SplitError(index: Int) extends Exception
    case class UnknownCommand(cmd: String) extends ISE(s"Unknown command ${c("'" + cmd + "'")}, type ${c("'help'")} to get help.")
    case class NoLocalServer() extends ISE(s"Local server not found, use $C'start-server'$RST command to start one.")
    case class NotSignedIn() extends ISE(s"Not signed in. Use ${c("'signin'")} command to sign in first.")
    case class NoLocalProbe() extends ISE(s"Local probe not found, use $C'start-probe'$RST command to start one.")
    case class HttpError(httpCode: Int) extends ISE(s"REST error (HTTP ${c(httpCode)}).")
    case class MalformedJson() extends ISE(s"Malformed JSON. ${c("Tip:")} on Windows make sure to escape double quotes.")
    case class TooManyArguments(cmd: Command) extends ISE(s"Too many arguments, ${help(cmd)}")
    case class NotEnoughArguments(cmd: Command) extends ISE(s"Not enough arguments, ${help(cmd)}")
    case class MissingParameter(cmd: Command, paramId: String) extends ISE( s"Missing mandatory parameter $C${"'" + cmd.params.find(_.id == paramId).get.names.head + "'"}$RST, ${help(cmd)}")
    case class MissingMandatoryJsonParameters(cmd: Command, missingParams: Seq[RestSpecParameter], path: String) extends ISE(s"Missing mandatory JSON parameters (${missingParams.map(s ⇒ y(s.name)).mkString(",")}) for $C${"'" + cmd.name + s" --path=$path'"}$RST, ${help(cmd)}")
    case class InvalidParameter(cmd: Command, paramId: String) extends ISE(s"Invalid parameter $C${"'" + cmd.params.find(_.id == paramId).get.names.head + "'"}$RST, ${help(cmd)}")
    case class InvalidJsonParameter(cmd: Command, param: String) extends ISE(s"Invalid JSON parameter $C${"'" + param + "'"}$RST, ${help(cmd)}")
}
