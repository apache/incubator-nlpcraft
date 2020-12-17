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
import org.apache.nlpcraft.common._
import org.apache.nlpcraft.common.version.NCVersion

// Parsed command line argument.
private [cmdline] case class Argument(
    parameter: Parameter, // Formal parameter this argument refers to.
    value: Option[String]
) {
    /**
     * Gets the original argument string.
     *
     * @return
     */
    def origString(): String =  value match {
        case Some(s) ⇒ s"${parameter.names.head}=$s"
        case None ⇒ parameter.names.head
    }
}

// Single CLI command.
private [cmdline] case class Command(
    name: String,
    group: String,
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
    def findParameterByNameOpt(name: String): Option[Parameter] =
        params.find(_.names.contains(name))

    /**
     *
     * @param id
     * @return
     */
    def findParameterByIdOpt(id: String): Option[Parameter] =
        params.find(_.id == id)

    /**
     *
     * @param id
     * @return
     */
    def findParameterById(id: String): Parameter =
        findParameterByIdOpt(id).get
}

// Single command's example.
private [cmdline] case class Example(
    usage: Seq[String],
    desc: String
)

// Single command's parameter.
private [cmdline] case class Parameter(
    id: String,
    names: Seq[String],
    value: Option[String] = None,
    optional: Boolean = false, // Mandatory by default.
    synthetic: Boolean = false,
    desc: String
)

private [cmdline] object NCCliCommands {
    private final lazy val SCRIPT_NAME = U.sysEnv("NLPCRAFT_CLI_SCRIPT").getOrElse(s"nlpcraft.${if (SystemUtils.IS_OS_UNIX) "sh" else "cmd"}")
    private final lazy val PROMPT = if (SCRIPT_NAME.endsWith("cmd")) ">" else "$"
    private final lazy val VER = NCVersion.getCurrent

    //noinspection DuplicatedCode
    // All supported commands.
    private [cmdline] final val CMDS = Seq(
        Command(
            name = "rest",
            group = "2. REST Commands",
            synopsis = s"REST call in a convenient way for command line mode.",
            desc = Some(
                s"When using this command you supply all call parameters as a single ${y("'--json'")} parameter with a JSON string. " +
                s"In REPL mode, you can hit ${rv(" Tab ")} to see auto-suggestion and auto-completion candidates for " +
                s"commonly used paths. However, ${y("'call'")} command provides more convenient way to issue REST " +
                s"calls when in REPL mode."
            ),
            body = NCCli.cmdRest,
            params = Seq(
                Parameter(
                    id = "path",
                    names = Seq("--path", "-p"),
                    value = Some("path"),
                    desc =
                        s"REST path, e.g. ${y("'signin'")} or ${y("'ask/sync'")}. " +
                        s"Note that you don't need supply '/' at the beginning. " +
                        s"See more details at https://nlpcraft.apache.org/using-rest.html " +
                        s"In REPL mode, hit ${rv(" Tab ")} to see auto-suggestion for possible REST paths."
                ),
                Parameter(
                    id = "json",
                    names = Seq("--json", "-j"),
                    value = Some("'json'"),
                    desc =
                        s"REST call parameters as JSON object. Since standard JSON only supports double " +
                        s"quotes the entire JSON string should be enclosed in single quotes. You can " +
                        s"find full OpenAPI specification for NLPCraft REST API at " +
                        s"https://nlpcraft.apache.org/using-rest.html"
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"$$ nlpcraft.sh rest ",
                        "  -p=signin",
                        "  -j='{\"email\": \"admin@admin.com\", \"passwd\": \"admin\"}'"
                    ),
                    desc = s"${bo("Unix/Linux:")} issues ${y("'signin'")} REST call with given JSON payload."
                ),
                Example(
                    usage = Seq(
                        s"> nlpcraft.cmd rest ",
                        "  -p=signin",
                        "  -j='{\\\"email\\\": \\\"admin@admin.com\\\", \\\"passwd\\\": \\\"admin\\\"}'"
                    ),
                    desc =
                        s"${bo("Windows:")} issues ${y("'signin'")} REST call with given JSON payload. " +
                        s"Note the necessary escaping of double quotes."
                )
            )
        ),
        Command(
            name = "signin",
            group = "2. REST Commands",
            synopsis = s"Wrapper for ${c("'/signin'")} REST call.",
            desc = Some(
                s"If no arguments provided, it signs in with the " +
                    s"default 'admin@admin.com' user account. NOTE: please make sure to remove this account when " +
                    s"running in production."
            ),
            body = NCCli.cmdSignIn,
            params = Seq(
                Parameter(
                    id = "email",
                    names = Seq("--email", "-e"),
                    value = Some("email"),
                    optional = true,
                    desc =
                        s"Email of the user. If not provided, 'admin@admin.com' will be used."
                ),
                Parameter(
                    id = "passwd",
                    names = Seq("--passwd", "-p"),
                    value = Some("****"),
                    optional = true,
                    desc =
                        s"User password to sign in. If not provided, the default password will be used."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME signin"
                    ),
                    desc = s"Signs in with the default ${c("admin@admin.com")} user account."
                )
            )
        ),
        Command(
            name = "signout",
            group = "2. REST Commands",
            synopsis = s"Wrapper for ${c("'/signout'")} REST call in REPL mode.",
            desc = Some(
                s"Signs out currently signed in user. Note that this command makes sense only in REPL mode."
            ),
            body = NCCli.cmdSignOut,
            examples = Seq(
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME signout"
                    ),
                    desc = s"Signs out currently signed in user, if any."
                )
            )
        ),
        Command(
            name = "call",
            group = "2. REST Commands",
            synopsis = s"REST call in a convenient way for REPL mode.",
            desc = Some(
                s"When using this command you supply all call parameters separately through their own parameters named " +
                    s"after their corresponding parameters in REST specification. " +
                    s"In REPL mode, hit ${rv(" Tab ")} to see auto-suggestion and " +
                    s"auto-completion candidates for commonly used paths and call parameters."
            ),
            body = NCCli.cmdCall,
            params = Seq(
                Parameter(
                    id = "path",
                    names = Seq("--path", "-p"),
                    value = Some("path"),
                    desc =
                        s"REST path, e.g. ${y("'signin'")} or ${y("'ask/sync'")}. " +
                        s"Note that you don't need supply '/' at the beginning. " +
                        s"See more details at https://nlpcraft.apache.org/using-rest.html " +
                        s"In REPL mode, hit ${rv(" Tab ")} to see auto-suggestion for possible REST paths."
                ),
                Parameter(
                    id = "xxx",
                    names = Seq("--xxx"),
                    value = Some("value"),
                    optional = true,
                    synthetic = true,
                    desc =
                        s"${y("'xxx'")} name corresponds to the REST call parameter that can be found at https://nlpcraft.apache.org/using-rest.html " +
                        s"The value of this parameter should be a valid JSON value using valid JSON syntax. Note that strings " +
                        s"don't have to be in double quotes. JSON objects and arrays should be specified as a JSON string in single quotes. You can have " +
                        s"as many ${y("'--xxx=value'")} parameters as requires by the ${y("'--path'")} parameter. " +
                        s"In REPL mode, hit ${rv(" Tab ")} to see auto-suggestion for possible parameters and their values."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME call -p=signin",
                        "  --email=admin@admin.com",
                        "  --passwd=admin"
                    ),
                    desc =
                        s"Issues ${y("'signin'")} REST call with given JSON payload provided as a set of parameters. " +
                        s"Note that ${y("'--email'")} and ${y("'--passwd'")} parameters correspond to the REST call " +
                        s"specification for ${y("'/signin'")} path."
                ),
                Example(
                    usage = Seq(
                        s"$$ nlpcraft.sh call --path=ask/sync",
                        "  --acsTok=qwerty123456",
                        "  --txt=\"User request\"",
                        "  --mdlId=my.model.id",
                        "  --data='{\"data1\": true, \"data2\": 123, \"data3\": \"some text\"}'",
                        "  --enableLog=false"
                    ),
                    desc =
                        s"${bo("Unix/Linux:")} issues ${y("'ask/sync'")} REST call with given JSON payload provided as a set of parameters."
                ),
                Example(
                    usage = Seq(
                        s"> nlpcraft.cmd call --path=ask/sync",
                        "  --acsTok=qwerty123456",
                        "  --txt=\"User request\"",
                        "  --mdlId=my.model.id",
                        "  --data='{\\\"data1\\\": true, \\\"data2\\\": 123, \\\"data3\\\": \\\"some text\\\"}'",
                        "  --enableLog=false"
                    ),
                    desc =
                        s"${bo("Windows:")} issues ${y("'ask/sync'")} REST call with given JSON payload provided " +
                        s"as a set of parameters. Note the necessary double quote escaping."
                )
            )
        ),
        Command(
            name = "ask",
            group = "2. REST Commands",
            synopsis = s"Wrapper for ${c("'/ask/sync'")} REST call.",
            desc = Some(
                s"Requires user to be already signed in. This command ${bo("only makes sense in the REPL mode")} as " +
                s"it requires user to be signed in. REPL session keeps the currently active access " +
                s"token after user signed in. For command line mode, use ${c("'rest'")} command with " +
                s"corresponding parameters."
            ),
            body = NCCli.cmdAsk,
            params = Seq(
                Parameter(
                    id = "mdlId",
                    names = Seq("--mdlId"),
                    value = Some("model.id"),
                    desc =
                        s"ID of the data model to send the request to. " +
                        s"In REPL mode, hit ${rv(" Tab ")} to see auto-suggestion for possible model IDs."
                ),
                Parameter(
                    id = "txt",
                    names = Seq("--txt"),
                    value = Some("txt"),
                    desc =
                        s"Text of the question."
                ),
                Parameter(
                    id = "data",
                    names = Seq("--data"),
                    value = Some("'{}'"),
                    optional = true,
                    desc = s"Additional JSON data with maximum JSON length of 512000 bytes. Default is ${c("'null'")}."
                ),
                Parameter(
                    id = "enableLog",
                    names = Seq("--enableLog"),
                    value = Some("true|false"),
                    optional = true,
                    desc = s"Flag to enable detailed processing log to be returned with the result. Default is ${c("'false'")}."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"""> ask --txt="User request" --mdlId=my.model.id"""
                    ),
                    desc =
                        s"Issues ${y("'ask/sync'")} REST call with given text and model ID."
                )
            )
        ),
        Command(
            name = "gen-sql",
            group = "3. Miscellaneous",
            synopsis = s"Generates NLPCraft model stub from SQL databases.",
            desc = Some(
                s"You can choose database schema, set of tables and columns for which you want to generate NLPCraft " +
                s"model. After the model is generated you can further configure and customize it for your specific needs. " +
                s"Find more information at https://nlpcraft.apache.org/tools/sql_model_gen.html"
            ),
            body = NCCli.cmdSqlGen,
            params = Seq(
                Parameter(
                    id = "url",
                    names = Seq("--url", "-r"),
                    value = Some("url"),
                    desc =
                        s"Database JDBC URL."
                ),
                Parameter(
                    id = "driver",
                    names = Seq("--driver", "-d"),
                    value = Some("class"),
                    desc =
                        s"Mandatory JDBC driver class. Note that 'class' must be a fully qualified class name. " +
                        s"It should also be available on the classpath."
                ),
                Parameter(
                    id = "schema",
                    names = Seq("--schema", "-s"),
                    value = Some("schema"),
                    desc =
                        s"Database schema to scan."
                ),
                Parameter(
                    id = "out",
                    names = Seq("--out", "-o"),
                    value = Some("filename"),
                    desc =
                        s"Name of the output JSON or YAML model file. " +
                        s"It should have one of the following extensions: .js, .json, .yml, or .yaml. " +
                        s"File extension determines the output file format."
                ),
                Parameter(
                    id = "user",
                    names = Seq("--user", "-u"),
                    value = Some("username"),
                    optional = true,
                    desc = s"Database user name."
                ),
                Parameter(
                    id = "password",
                    names = Seq("--password", "-w"),
                    value = Some("password"),
                    optional = true,
                    desc = s"Database password."
                ),
                Parameter(
                    id = "modelId",
                    names = Seq("--model-id", "-x"),
                    value = Some("id"),
                    optional = true,
                    desc = s"Generated model ID. By default, the model ID is ${c("'sql.model.id'")}."
                ),
                Parameter(
                    id = "modelVer",
                    names = Seq("--model-ver", "-v"),
                    value = Some("version"),
                    optional = true,
                    desc = s"Generated model version. By default, the model version is ${c("'1.0.0-timestamp'")}."
                ),
                Parameter(
                    id = "modelName",
                    names = Seq("--model-name", "-n"),
                    value = Some("name"),
                    optional = true,
                    desc = s"Generated model name. By default, the model name is ${c("'SQL-based-model'")}."
                ),
                Parameter(
                    id = "exclude",
                    names = Seq("--exclude", "-e"),
                    value = Some("list"),
                    optional = true,
                    desc =
                        s"Semicolon-separate list of tables and/or columns to exclude. By default, none of the " +
                        s"tables and columns in the schema are excluded. See ${c("--help")} parameter to get more details."
                ),
                Parameter(
                    id = "include",
                    names = Seq("--include", "-i"),
                    value = Some("list"),
                    optional = true,
                    desc =
                        s"Semicolon-separate list of tables and/or columns to include. By default, all of the " +
                        s"tables and columns in the schema are included. See ${c("--help")} parameter to get more details."
                ),
                Parameter(
                    id = "prefix",
                    names = Seq("--prefix", "-f"),
                    value = Some("list"),
                    optional = true,
                    desc =
                        s"Comma-separate list of table or column name prefixes to remove. These prefixes will be " +
                        s"removed when name is used for model elements synonyms. By default, no prefixes will be removed."
                ),
                Parameter(
                    id = "suffix",
                    names = Seq("--suffix", "-q"),
                    value = Some("list"),
                    optional = true,
                    desc =
                        s"Comma-separate list of table or column name suffixes to remove. These suffixes will be " +
                        s"removed when name is used for model elements synonyms. By default, no suffixes will be removed."
                ),
                Parameter(
                    id = "synonyms",
                    names = Seq("--synonyms", "-y"),
                    value = Some("true|false"),
                    optional = true,
                    desc = s"Flag on whether or not to generated auto synonyms for the model elements. Default is ${c("'true'")}."
                ),
                Parameter(
                    id = "override",
                    names = Seq("--override", "-z"),
                    value = Some("true|false"),
                    optional = true,
                    desc =
                        s"Flag to determine whether or not to override output file if it already exist. " +
                        s"If override is disabled (default) and output file exists - a unique file name " +
                        s"will be used instead. Default is ${c("'false'")}."
                ),
                Parameter(
                    id = "parent",
                    names = Seq("--parent", "-p"),
                    value = Some("true|false"),
                    optional = true,
                    desc =
                        s"Flag on whether or not to use element's parent relationship for defining " +
                        s"SQL columns and their containing (i.e. parent) tables. Default is ${c("'false'")}."
                ),
                Parameter(
                    id = "help",
                    names = Seq("--help", "-h"),
                    optional = true,
                    desc =
                        s"Gets extended help and usage information for the ${c("'gen-sql'")} command. " +
                        s"Includes information on how to run this tool standalone."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME gen-sql --help"
                    ),
                    desc =
                        s"Shows full help and usage information for ${c("gen-sql")} command."
                ),
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME gen-sql",
                        "  --url=jdbc:postgresql://localhost:5432/mydb",
                        "  --driver=org.postgresql.Driver",
                        """  --prefix="tbl_, col_"""",
                        """  --suffix="_tmp, _old, _unused"""",
                        "  --schema=public",
                        """  --exclude="#_.+"""",
                        "  --out=model.json"
                    ),
                    desc =
                        s"Generates model stub from given SQL database connection."
                )
            )
        ),
        Command(
            name = "sugsyn",
            group = "2. REST Commands",
            synopsis = s"Wrapper for ${c("'/model/sugsyn'")} REST call.",
            desc = Some(
                s"Requires user to be already signed in. This command ${bo("only makes sense in the REPL mode")} as " +
                s"it requires user to be signed in. REPL session keeps the currently active access " +
                s"token after user signed in. For command line mode, use ${c("'rest'")} command with " +
                s"corresponding parameters. Note also that it requires a local probe running that hosts " +
                s"the specified model. Find more information about this tool at https://nlpcraft.apache.org/tools/syn_tool.html"
            ),
            body = NCCli.cmdSugSyn,
            params = Seq(
                Parameter(
                    id = "mdlId",
                    names = Seq("--mdlId"),
                    value = Some("model.id"),
                    desc =
                        s"ID of the model to run synonym suggestion on. " +
                        s"In REPL mode, hit ${rv(" Tab ")} to see auto-suggestion for possible model IDs. " +
                        s"Note that the probe hosting this model must be connected to the server."
                ),
                Parameter(
                    id = "minScore",
                    names = Seq("--minScore"),
                    value = Some("0.5"),
                    optional = true,
                    desc = s"Minimal score to include into the result (from 0 to 1). Default is ${c("0.5")}."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"""> sugsyn --mdlId=my.model.id"""
                    ),
                    desc =
                        s"Issues ${y("'model/sugsyn'")} REST call with default min score and given model ID."
                )
            )
        ),
        Command(
            name = "tail-server",
            group = "1. Server & Probe Commands",
            synopsis = s"Shows last N lines from the local server log.",
            desc = Some(
                s"Only works for the server started via this script."
            ),
            body = NCCli.cmdTailServer,
            params = Seq(
                Parameter(
                    id = "lines",
                    names = Seq("--lines", "-l"),
                    value = Some("num"),
                    desc =
                        s"Number of the server log lines from the end to display. Default is 20."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME tail-server --lines=20 "),
                    desc = s"Prints last 20 lines from the local server log."
                )
            )
        ),
        Command(
            name = "tail-probe",
            group = "1. Server & Probe Commands",
            synopsis = s"Shows last N lines from the local probe log.",
            desc = Some(
                s"Only works for the probe started via this script."
            ),
            body = NCCli.cmdTailProbe,
            params = Seq(
                Parameter(
                    id = "lines",
                    names = Seq("--lines", "-l"),
                    value = Some("num"),
                    desc =
                        s"Number of the probe log lines from the end to display. Default is 20."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME tail-probe --lines=20 "),
                    desc = s"Prints last 20 lines from the local probe log."
                )
            )
        ),
        Command(
            name = "start-server",
            group = "1. Server & Probe Commands",
            synopsis = s"Starts local server.",
            desc = Some(
                s"Server is started in the external JVM process with both stdout and stderr piped out into log file. " +
                s"Command will block until the server is started unless ${y("'--no-wait'")} parameter is used or timeout is expired."
            ),
            body = NCCli.cmdStartServer,
            params = Seq(
                Parameter(
                    id = "config",
                    names = Seq("--config", "-c"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Configuration file path. Server will automatically look for ${y("'nlpcraft.conf'")} " +
                        s"configuration file in the same directory as NLPCraft JAR file. If the configuration file has " +
                        s"different name or in different location use this parameter to provide an alternative path. " +
                        s"Note that the server and the probe can use the same file for their configuration."
                ),
                Parameter(
                    id = "igniteConfig",
                    names = Seq("--ignite-config", "-i"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Apache Ignite configuration file path. Note that Apache Ignite is used as a cluster " +
                        s"computing plane and a default distributed storage. Server will automatically look for " +
                        s"${y("'ignite.xml'")} configuration file in the same directory as NLPCraft JAR file. If the " +
                        s"configuration file has different name or in different location use this parameter to " +
                        s"provide an alternative path."
                ),
                Parameter(
                    id = "jvmopts",
                    names = Seq("--jvm-opts", "-j"),
                    value = Some("<jvm flags>"),
                    optional = true,
                    desc =
                        s"Space separated quoted string of JVM flags to use. If not provided, the " +
                        s"default ${y("'-ea -Xms2048m -XX:+UseG1GC'")} flags will be used."
                ),
                Parameter(
                    id = "noWait",
                    names = Seq("--no-wait"),
                    optional = true,
                    desc =
                        s"Instructs command not to wait for the server startup and return immediately."
                ),
                Parameter(
                    id = "timeoutMins",
                    names = Seq("--timeout-mins", "-t"),
                    optional = true,
                    value = Some("3"),
                    desc =
                        s"Timeout in minutes to wait until server is started. If not specified the default is 2 minutes."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME start-server"),
                    desc = "Starts local server with default configuration."
                ),
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME start-server -c=/opt/nlpcraft/nlpcraft.conf -t=5"),
                    desc = "Starts local server with alternative configuration file and timeout of 5 mins."
                )
            )
        ),
        Command(
            name = "start-probe",
            group = "1. Server & Probe Commands",
            synopsis = s"Starts local probe.",
            desc = Some(
                s"Probe is started in the external JVM process with both stdout and stderr piped out into log file. " +
                s"Command will block until the probe is started unless ${y("'--no-wait'")} parameter is used or timeout is expired."
            ),
            body = NCCli.cmdStartProbe,
            params = Seq(
                Parameter(
                    id = "config",
                    names = Seq("--config", "-c"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Configuration file path. Probe will automatically look for ${y("'nlpcraft.conf'")} " +
                        s"configuration file in the same directory as NLPCraft JAR file. If the configuration file has " +
                        s"different name or in different location use this parameter to provide an alternative path. " +
                        s"Note that the server and the probe can use the same file for their configuration."
                ),
                Parameter(
                    id = "cp",
                    names = Seq("--cp", "-p"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Additional JVM classpath that will be appended to the default NLPCraft JVM classpath. " +
                        s"Although this configuration property is optional, when deploying your own models you must " +
                        s"provide this additional classpath for the models and their dependencies this probe will be hosting. " +
                        s"NOTE: this is only optional if you are running example models shipped with NLPCraft."
                ),
                Parameter(
                    id = "models",
                    names = Seq("--models", "-m"),
                    value = Some("<model list>"),
                    optional = true,
                    desc =
                        s"Comma separated list of fully qualified class names for models to deploy. This will override " +
                        s"${y("'nlpcraft.probe.models'")} configuration property from either default configuration file " +
                        s"or the one provided by ${y("--config")} parameter. NOTE: if you provide the list of your " +
                        s"own models here or in configuration file - you must also provide the additional classpath " +
                        s"for them via ${y("--cp")} parameter."
                ),
                Parameter(
                    id = "jvmopts",
                    names = Seq("--jvm-opts", "-j"),
                    value = Some("<jvm flags>"),
                    optional = true,
                    desc =
                        s"Space separated quoted string of JVM flags to use. If not provided, the " +
                        s"default ${y("'-ea -Xms1024m'")} flags will be used."
                ),
                Parameter(
                    id = "noWait",
                    names = Seq("--no-wait"),
                    optional = true,
                    desc =
                        s"Instructs command not to wait for the probe startup and return immediately."
                ),
                Parameter(
                    id = "timeoutMins",
                    names = Seq("--timeout-mins", "-t"),
                    optional = true,
                    value = Some("3"),
                    desc =
                        s"Timeout to wait until probe is started. If not specified the default is 1 minute."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME start-probe"),
                    desc = "Starts local probe with default configuration and parameters."
                ),
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME start-probe ",
                        "  --config=/opt/nlpcraft.conf ",
                        "  --models=my.package.Model ",
                        "  --cp=/opt/target/classes ",
                        "  --jmv-opts=\"-ea -Xms2048m\" ",
                        "  --timeout-mins=5"
                    ),
                    desc =
                        s"Starts local probe for ${y("'my.package.Model'")} model with alternative configuration " +
                        s"file and additional parameters."
                )
            )
        ),
        Command(
            name = "test-model",
            group = "3. Miscellaneous",
            synopsis = s"Runs ${y("'NCTestAutoModelValidator'")} model auto-validator.",
            desc = Some(
                s"Validation consists " +
                s"of starting an embedded probe, scanning all deployed models for ${y("'NCIntentSample'")} annotations and their corresponding " +
                s"callback methods, submitting each sample input sentences from ${y("'NCIntentSample'")} annotation and " +
                s"checking that resulting intent matches the intent the sample was attached to. " +
                s"See more details at https://nlpcraft.apache.org/tools/test_framework.html"
            ),
            body = NCCli.cmdTestModel,
            params = Seq(
                Parameter(
                    id = "config",
                    names = Seq("--config", "-c"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Configuration file path. By default, the embedded probe will automatically look for ${y("'nlpcraft.conf'")} " +
                        s"configuration file in the same directory as NLPCraft JAR file. If the configuration file has " +
                        s"different name or in different location use this parameter to provide an alternative path."
                ),
                Parameter(
                    id = "cp",
                    names = Seq("--cp", "-p"),
                    value = Some("path"),
                    optional = true,
                    desc =
                        s"Additional JVM classpath that will be appended to the default NLPCraft JVM classpath. " +
                        s"Although this configuration property is optional, when testing your own models you must " +
                        s"provide this additional classpath for the models and their dependencies. " +
                        s"NOTE: this is only optional if you are testing example models shipped with NLPCraft."
                ),
                Parameter(
                    id = "models",
                    names = Seq("--models", "-m"),
                    value = Some("<model list>"),
                    optional = true,
                    desc =
                        s"Comma separated list of fully qualified class names for models to test. NOTE: if you provide " +
                        s"the list of your own models here - you must also provide the additional classpath " +
                        s"for them via ${y("--cp")} parameter."
                ),
                Parameter(
                    id = "jvmopts",
                    names = Seq("--jvm-opts", "-j"),
                    value = Some("<jvm flags>"),
                    optional = true,
                    desc =
                        s"Space separated quoted string of JVM flags to use. If not provided, the " +
                        s"default ${y("'-ea -Xms1024m'")} flags will be used."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq(
                        s"$PROMPT $SCRIPT_NAME test-model ",
                        "  --models=my.package.Model ",
                        "  --cp=/opt/target/classes ",
                        "  --jmv-opts=\"-ea -Xms2048m\""
                    ),
                    desc =
                        s"Runs model auto-validator for ${y("'my.package.Model'")} model."
                )
            )
        ),
        Command(
            name = "info-server",
            group = "1. Server & Probe Commands",
            synopsis = s"Info about local server.",
            body = NCCli.cmdInfoServer
        ),
        Command(
            name = "info-probe",
            group = "1. Server & Probe Commands",
            synopsis = s"Info about local probe.",
            body = NCCli.cmdInfoProbe
        ),
        Command(
            name = "info",
            group = "1. Server & Probe Commands",
            synopsis = s"Info about local probe & server.",
            body = NCCli.cmdInfo
        ),
        Command(
            name = "clear",
            group = "3. Miscellaneous",
            synopsis = s"Clears terminal screen.",
            body = NCCli.cmdClear
        ),
        Command(
            name = "no-ansi",
            group = "3. Miscellaneous",
            synopsis = s"Disables ANSI escape codes for terminal colors & controls.",
            desc = Some(
                s"This is a special command that can be combined with any other commands."
            ),
            body = NCCli.cmdNoAnsi,
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -c=rest no-ansi"),
                    desc = s"Displays help for ${y("'rest'")} commands without using ANSI color and escape sequences."
                )
            )
        ),
        Command(
            name = "no-logo",
            group = "3. Miscellaneous",
            synopsis = s"Disables showing NLPCraft logo at the start.",
            desc = Some(
                s"This is a special command that can be combined with any other command in a command line mode."
            ),
            body = NCCli.cmdNoLogo,
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME version -s no-logo"),
                    desc =
                        s"Displays just the version information without any additional logo output. " +
                            s"This command makes sense only in command lime mode (NOT in REPL mode)."
                )
            )
        ),
        Command(
            name = "ansi",
            group = "3. Miscellaneous",
            synopsis = s"Enables ANSI escape codes for terminal colors & controls.",
            desc = Some(
                s"This is a special command that can be combined with any other commands."
            ),
            body = NCCli.cmdAnsi,
            examples = Seq(
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -c=rest ansi"),
                    desc = s"Displays help for ${y("'rest'")} commands with ANSI color and escape sequences."
                )
            )
        ),
        Command(
            name = "ping-server",
            group = "1. Server & Probe Commands",
            synopsis = s"Pings local server.",
            desc = Some(
                s"Server is pinged using ${y("'/health'")} REST call to check its online status."
            ),
            body = NCCli.cmdPingServer,
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
                    desc = "Pings local server 10 times."
                )
            )
        ),
        Command(
            name = "stop-server",
            group = "1. Server & Probe Commands",
            synopsis = s"Stops local server.",
            desc = Some(
                s"Local server must be started via ${y(s"'$SCRIPT_NAME'")} or other compatible way."
            ),
            body = NCCli.cmdStopServer
        ),
        Command(
            name = "stop-probe",
            group = "1. Server & Probe Commands",
            synopsis = s"Stops local probe.",
            desc = Some(
                s"Local probe must be started via ${y(s"'$SCRIPT_NAME'")} or other compatible way."
            ),
            body = NCCli.cmdStopProbe
        ),
        Command(
            name = "stop",
            group = "1. Server & Probe Commands",
            synopsis = s"Stops both local server & probe.",
            desc = Some(
                s"Both local server & probe must be started via ${y(s"'$SCRIPT_NAME'")} or other compatible way."
            ),
            body = NCCli.cmdStop
        ),
        Command(
            name = "quit",
            group = "3. Miscellaneous",
            synopsis = s"Quits REPL mode. Note that started server and probe, if any, will remain running.",
            body = NCCli.cmdQuit
        ),
        Command(
            name = "help",
            group = "3. Miscellaneous",
            synopsis = s"Displays help for ${y(s"'$SCRIPT_NAME'")}.",
            desc = Some(
                s"By default, without ${y("'--all'")} or ${y("'--cmd'")} parameters, displays the abbreviated form of manual " +
                s"only listing the commands without parameters or examples."
            ),
            body = NCCli.cmdHelp,
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
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -c=rest --cmd=version"),
                    desc = s"Displays help for ${y("'rest'")} and ${y("'version'")} commands."
                ),
                Example(
                    usage = Seq(s"$PROMPT $SCRIPT_NAME help -all"),
                    desc = "Displays help for all commands."
                )
            )
        ),
        Command(
            name = "version",
            group = "3. Miscellaneous",
            synopsis = s"Displays full version of ${y(s"'$SCRIPT_NAME'")} script.",
            desc = Some(
                "Depending on the additional parameters can display only the semantic version or the release date."
            ),
            body = NCCli.cmdVersion,
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
            name = "gen-project",
            group = "3. Miscellaneous",
            synopsis = s"Generates project stub with default configuration.",
            desc = Some(
                "This command supports Java, Scala, and Kotlin languages with either Maven, Gradle or SBT " +
                "as a build tool. Generated projects compiles and runs and can be used as a quick development sandbox."
            ),
            body = NCCli.cmdGenProject,
            params = Seq(
                Parameter(
                    id = "outputDir",
                    names = Seq("--outputDir", "-d"),
                    value = Some("path"),
                    optional = true,
                    desc = s"Output directory. Default value is the current working directory."
                ),
                Parameter(
                    id = "baseName",
                    names = Seq("--baseName", "-n"),
                    value = Some("name"),
                    desc =
                        s"Base name for the generated files. For example, if base name is ${y("'MyApp'")}, " +
                        s"then generated Java file will be named as ${y("'MyAppModel.java'")} and model file as ${y("'my_app_model.yaml'")}."
                ),
                Parameter(
                    id = "lang",
                    names = Seq("--lang", "-l"),
                    value = Some("name"),
                    optional = true,
                    desc =
                        s"Language to generate source files in. Supported value are ${y("'java'")}, ${y("'scala'")}, ${y("'kotlin'")}. " +
                        s"Default value is ${y("'java'")}."
                ),
                Parameter(
                    id = "buildTool",
                    names = Seq("--buildTool", "-b"),
                    value = Some("name"),
                    optional = true,
                    desc =
                        s"Build tool name to use. Supported values are ${y("'mvn'")} and ${y("'gradle'")} for ${y("'java'")}, " +
                        s"${y("'scala'")}, ${y("'kotlin'")}, and ${y("'sbt'")} for ${y("'scala'")} language. Default value is ${y("'mvn'")}."
                ),
                Parameter(
                    id = "packageName",
                    names = Seq("--packageName", "-p"),
                    value = Some("name"),
                    optional = true,
                    desc = s"JVM package name to use in generated source code. Default value is ${y("'org.apache.nlpcraft.demo'")}."
                ),
                Parameter(
                    id = "modelType",
                    names = Seq("--modelType", "-m"),
                    value = Some("type"),
                    optional = true,
                    desc = s"Type of generated model file. Supported value are ${y("'yaml'")} or ${y("'json'")}. Default value is ${y("'yaml'")}."
                ),
                Parameter(
                    id = "override",
                    names = Seq("--override", "-o"),
                    value = Some("true|false"),
                    optional = true,
                    desc = s"Whether or not to override existing output directory. Default value is ${y("'false'")}."
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq("> gen-project -n=MyProject -l=scala -b=sbt"),
                    desc = s"Generates Scala SBT project."
                ),
                Example(
                    usage = Seq("> gen-project -n=MyProject -l=kotlin -p=com.mycompany.nlp -o=true"),
                    desc = s"Generates Kotlin Maven project."
                )
            )
        ),
        Command(
            name = "gen-model",
            group = "3. Miscellaneous",
            synopsis = s"Generates data model file stub.",
            desc = Some(
                "Generated model stub will have all default configuration. Model file can be either YAML or JSON."
            ),
            body = NCCli.cmdGenModel,
            params = Seq(
                Parameter(
                    id = "filePath",
                    names = Seq("--filePath", "-f"),
                    value = Some("path"),
                    desc =
                        s"File path for the model stub. File path can either be an absolute path, relative path or " +
                        s"just a file name in which case the current folder will be used. File must have one of the " +
                        s"following extensions: ${y("'json'")}, ${y("'js'")}, ${y("'yaml'")}, or ${y("'yml'")}."
                ),
                Parameter(
                    id = "modelId",
                    names = Seq("--modelId", "-n"),
                    value = Some("id"),
                    desc = "Model ID."
                ),
                Parameter(
                    id = "override",
                    names = Seq("--override", "-o"),
                    value = Some("true|false"),
                    optional = true,
                    desc = s"Override output directory flag. Supported: ${y("'true'")}, ${y("'false'")}. Default value is ${y("'false'")}"
                )
            ),
            examples = Seq(
                Example(
                    usage = Seq("> gen-model -f=myModel.json -n=my.model.id"),
                    desc = s"Generates JSON model file stub in the current folder."
                ),
                Example(
                    usage = Seq("> gen-model -f=c:/tmp/myModel.yaml -n=my.model.id --override=true"),
                    desc = s"Generates YAML model file stub in ${y("'c:/temp'")} folder overriding existing file, if any."
                )
            )
        )
    ).sortBy(_.name)

    require(
        U.getDups(CMDS.map(_.name)).isEmpty,
        "Dup commands."
    )
}
