/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.model.tools.cmdline

private [cmdline] case class RestSpec(
    path: String,
    desc: String,
    group: String,
    params: Seq[RestSpecParameter]
)

private [cmdline] sealed trait JsonType

private [cmdline] case object STRING extends JsonType
private [cmdline] case object BOOLEAN extends JsonType
private [cmdline] case object NUMERIC extends JsonType
private [cmdline] case object OBJECT extends JsonType
private [cmdline] case object ARRAY extends JsonType

private [cmdline] case class RestSpecParameter(
    name: String,
    kind: JsonType,
    optional: Boolean = false // Mandatory by default.
)

// TODO: this needs to be loaded dynamically from OpenAPI spec.
/**
 * NLPCraft REST specification.
 */
private [cmdline] object NCCliRestSpec {
    //noinspection DuplicatedCode
    private [cmdline] final val REST_SPEC = Seq(
        RestSpec(
            path = "clear/conversation",
            desc = "Clears conversation STM",
            group = "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "mdlId", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true)
            )
        ),
        RestSpec(
            "clear/dialog",
            "Clears dialog flow",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "mdlId", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true)
            )
        ),
        RestSpec(
            "model/sugsyn",
            "Runs model synonym suggestion tool",
            "Tools",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "mdlId", kind = STRING),
                RestSpecParameter(name = "minScore", kind = NUMERIC)
            )
        ),
        RestSpec(
            "model/syns",
            "Gets model element synonyms",
            "Tools",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "mdlId", kind = STRING),
                RestSpecParameter(name = "elmId", kind = STRING)
            )
        ),
        RestSpec(
            "check",
            "Gets status and result of submitted requests",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true),
                RestSpecParameter(name = "srvReqIds", kind = ARRAY, optional = true),
                RestSpecParameter(name = "maxRows", kind = NUMERIC, optional = true)
            )
        ),
        RestSpec(
            "cancel",
            "Cancels a question",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true),
                RestSpecParameter(name = "srvReqIds", kind = ARRAY, optional = true),
            )
        ),
        RestSpec(
            "ask",
            "Asks a question",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true),
                RestSpecParameter(name = "txt", kind = STRING),
                RestSpecParameter(name = "mdlId", kind = STRING),
                RestSpecParameter(name = "data", kind = OBJECT, optional = true),
                RestSpecParameter(name = "enableLog", kind = BOOLEAN, optional = true),
            )
        ),
        RestSpec(
            "ask/sync",
            "Asks a question in synchronous mode",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true),
                RestSpecParameter(name = "txt", kind = STRING),
                RestSpecParameter(name = "mdlId", kind = STRING),
                RestSpecParameter(name = "data", kind = OBJECT, optional = true),
                RestSpecParameter(name = "enableLog", kind = BOOLEAN, optional = true),
            )
        ),
        RestSpec(
            "user/get",
            "Gets current user information",
            "User",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "id", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true)
            )
        ),
        RestSpec(
            "user/all",
            "Gets all users",
            "User",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
            )
        ),
        RestSpec(
            "user/update",
            "Updates regular user",
            "User",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "firstName", kind = STRING),
                RestSpecParameter(name = "lastName", kind = STRING),
                RestSpecParameter(name = "id", kind = STRING, optional = true),
                RestSpecParameter(name = "avatarUrl", kind = STRING, optional = true),
                RestSpecParameter(name = "properties", kind = OBJECT, optional = true)
            )
        ),
        RestSpec(
            "user/delete",
            "Deletes user",
            "User",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "id", kind = STRING, optional = true),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true)
            )
        ),
        RestSpec(
            "user/admin",
            "Updates user admin permissions",
            "User",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "id", kind = STRING, optional = true),
                RestSpecParameter(name = "isAdmin", kind = BOOLEAN)
            )
        ),
        RestSpec(
            "user/passwd/reset",
            "Resets password for the user",
            "User",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "id", kind = STRING, optional = true),
                RestSpecParameter(name = "newPasswd", kind = STRING)
            )
        ),
        RestSpec(
            "user/add",
            "Adds new user",
            "User",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "firstName", kind = STRING),
                RestSpecParameter(name = "lastName", kind = STRING),
                RestSpecParameter(name = "email", kind = STRING),
                RestSpecParameter(name = "passwd", kind = STRING),
                RestSpecParameter(name = "isAdmin", kind = BOOLEAN),
                RestSpecParameter(name = "usrExtId", kind = STRING, optional = true),
                RestSpecParameter(name = "avatarUrl", kind = STRING, optional = true),
                RestSpecParameter(name = "properties", kind = OBJECT, optional = true)
            )
        ),
        RestSpec(
            "company/get",
            "Gets current user company information",
            "Company",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
            )
        ),
        RestSpec(
            "company/add",
            "Adds new company",
            "Company",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "name", kind = STRING),
                RestSpecParameter(name = "website", kind = STRING, optional = true),
                RestSpecParameter(name = "country", kind = STRING, optional = true),
                RestSpecParameter(name = "region", kind = STRING, optional = true),
                RestSpecParameter(name = "city", kind = STRING, optional = true),
                RestSpecParameter(name = "address", kind = STRING, optional = true),
                RestSpecParameter(name = "postalCode", kind = STRING, optional = true),
                RestSpecParameter(name = "adminEmail", kind = STRING),
                RestSpecParameter(name = "adminPasswd", kind = STRING),
                RestSpecParameter(name = "adminFirstName", kind = STRING),
                RestSpecParameter(name = "adminLastName", kind = STRING),
                RestSpecParameter(name = "adminAvatarUrl", kind = STRING, optional = true)
            )
        ),
        RestSpec(
            "company/update",
            "Updates company data",
            "Company",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "name", kind = STRING),
                RestSpecParameter(name = "website", kind = STRING, optional = true),
                RestSpecParameter(name = "country", kind = STRING, optional = true),
                RestSpecParameter(name = "region", kind = STRING, optional = true),
                RestSpecParameter(name = "city", kind = STRING, optional = true),
                RestSpecParameter(name = "address", kind = STRING, optional = true),
                RestSpecParameter(name = "postalCode", kind = STRING, optional = true)
            )
        ),
        RestSpec(
            "company/delete",
            "Deletes company",
            "Company",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
            )
        ),
        RestSpec(
            "company/token/reset",
            "Resets company probe auth token",
            "Company",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
            )
        ),
        RestSpec(
            "feedback/add",
            "Adds feedback",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "extUsrId", kind = STRING, optional = true),
                RestSpecParameter(name = "comment", kind = STRING, optional = true),
                RestSpecParameter(name = "srvReqId", kind = STRING),
                RestSpecParameter(name = "score", kind = STRING)
            )
        ),
        RestSpec(
            "feedback/delete",
            "Deletes feedback",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "id", kind = NUMERIC)
            )
        ),
        RestSpec(
            "feedback/all",
            "Gets all feedback",
            "Asking",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
                RestSpecParameter(name = "usrId", kind = STRING, optional = true),
                RestSpecParameter(name = "extUsrId", kind = STRING, optional = true),
                RestSpecParameter(name = "srvReqId", kind = STRING, optional = true)
            )
        ),
        RestSpec(
            "signin",
            "Signs in and obtains new access token",
            "Authentication",
            params = Seq(
                RestSpecParameter(name = "email", kind = STRING),
                RestSpecParameter(name = "passwd", kind = STRING)
            )
        ),
        RestSpec(
            "signout",
            "Signs out and releases access token",
            "Authentication",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
            )
        ),
        RestSpec(
            "probe/all",
            "Gets all probes",
            "Probe",
            params = Seq(
                RestSpecParameter(name = "acsTok", kind = STRING),
            )
        )
    )
}
