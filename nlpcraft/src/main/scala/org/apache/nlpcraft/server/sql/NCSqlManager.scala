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

package org.apache.nlpcraft.server.sql

import java.sql.Timestamp

import io.opencensus.trace.Span
import org.apache.ignite.IgniteAtomicSequence
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.server.apicodes.NCApiStatusCode._
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.mdo._
import org.apache.nlpcraft.server.sql.NCSql.Implicits._

import scala.util.control.Exception.catching

/**
  * Provides basic CRUD and often used operations on RDBMS.
  * Note that all functions in this class expect outside `NCSql.sql()` block.
  */
object NCSqlManager extends NCService with NCIgniteInstance {
    private final val DB_TABLES = Seq("nc_company", "nc_user", "nc_user_property", "passwd_pool", "proc_log", "feedback")
    private final val CACHE_2_CLEAR = Seq(
        "user-token-signin-cache",
        "user-id-signin-cache",
        "qry-state-cache",
        "sentence-cache",
        "stanford-cache",
        "opennlp-cache"
    )
    
    private object Config extends NCConfigurable {
        def init: Boolean = getBoolOpt("nlpcraft.server.database.igniteDbInitialize").getOrElse(false)
    }

    @volatile private var usersPropsSeq: IgniteAtomicSequence = _

    /**
      * Starts manager.
      */
    @throws[NCE]
    override def start(parent: Span): NCService = startScopedSpan("start", parent) { span ⇒
        addTags(span, "isIgniteDb" → NCSql.isIgniteDb)

        if (NCSql.isIgniteDb)
            prepareIgniteSchema()
     
        catching(wrapIE) {
            usersPropsSeq = NCSql.mkSeq(ignite, "usersPropsSeq", "nc_user_property", "id")
        }
     
        super.start()
    }

    /**
      * Stop manager.
      */
    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        super.stop()
    }

    /**
      * Checks if given hash exists in the password pool.
      *
      * @param hash Hash to check.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def isKnownPasswordHash(hash: String, parent: Span): Boolean =
        startScopedSpan("isKnownPasswordHash", parent, "hash" → hash) { _ ⇒
            NCSql.exists("passwd_pool WHERE passwd_hash = ?", hash)
        }

    /**
      * Checks if request with given ID already registered.
      *
      * @param srvReqId Server request ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def isLogExists(srvReqId: String, parent: Span): Boolean =
        startScopedSpan("isLogExists", parent, "srvReqId" → srvReqId) { _ ⇒
            NCSql.exists("proc_log WHERE srv_req_id = ?", srvReqId)
        }

    /**
      * Inserts password hash into anonymous password pool.
      *
      * @param id Id.
      * @param hash Password hash to insert into anonymous password pool.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def addPasswordHash(id: Long, hash: String, parent: Span): Unit =
        startScopedSpan("addPasswordHash", parent, "id" → id, "hash" → hash) { _ ⇒
            NCSql.insert("INSERT INTO passwd_pool (id, passwd_hash) VALUES (?, ?)", id, hash)
        }

    /**
      * Removes password hash from anonymous password pool.
      *
      * @param hash Password hash to remove.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def erasePasswordHash(hash: String, parent: Span): Unit =
        startScopedSpan("erasePasswordHash", parent, "hash" → hash) { _ ⇒
            NCSql.delete("DELETE FROM passwd_pool WHERE passwd_hash = ?", hash)
        }

    /**
      * Gets user for given email.
      *
      * @param email User's normalized email.
      * @return User MDO.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getUserByEmail(email: String, parent: Span): Option[NCUserMdo] =
        startScopedSpan("getUserByEmail", parent, "email" → email) { _ ⇒
            NCSql.selectSingle[NCUserMdo](
                """
                  |SELECT *
                  |FROM nc_user
                  |WHERE email = ?
                  """.stripMargin,
                email
            )
        }

    /**
      * Deletes user with given ID.
      *
      * @param id User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def deleteUser(id: Long, parent: Span): Int =
        startScopedSpan("deleteUser", parent, "usrId" → id) { _ ⇒
            NCSql.delete("DELETE FROM nc_user_property WHERE user_id = ?", id)
            NCSql.delete("DELETE FROM nc_user WHERE id = ?", id)
        }

    /**
      * Deletes company with given ID.
      *
      * @param id Company ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def deleteCompany(id: Long, parent: Span): Int =
        startScopedSpan("deleteCompany", parent, "compId" → id) { _ ⇒
            NCSql.delete("DELETE FROM nc_user_property WHERE user_id IN (SELECT id FROM nc_user WHERE company_id = ?)", id)
            NCSql.delete("DELETE FROM nc_user WHERE company_id = ?", id)
            NCSql.delete("DELETE FROM nc_company WHERE id = ?", id)
        }

    /**
      * Updates user.
      *
      * @param id ID of the user to update.
      * @param firstName First name.
      * @param lastName Last name.
      * @param avatarUrl Avatar URL.
      * @param propsOpt Properties.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateUser(
        id: Long,
        firstName: String,
        lastName: String,
        avatarUrl: Option[String],
        propsOpt: Option[Map[String, String]],
        parent: Span
    ): Int =
        startScopedSpan("updateUser", parent, "usrId" → id) { span ⇒
            val n =
                NCSql.update(
                    s"""
                       |UPDATE nc_user
                       |SET
                       |    first_name = ?,
                       |    last_name = ?,
                       |    avatar_url = ?,
                       |    last_modified_on = ?
                       |WHERE id = ?
                        """.stripMargin,
                    firstName,
                    lastName,
                    avatarUrl.orNull,
                    U.nowUtcTs(),
                    id
                )
         
            NCSql.delete("DELETE FROM nc_user_property WHERE user_id = ?", id)
         
            addUserProperties(id, propsOpt, span)
         
            n
        }

    /**
      * Updates user.
      *
      * @param id ID of the user to update.
      * @param email User's normalized email. Optional.
      * @param passwdSalt Salt for password Blowfish hashing. Optional.
      * @param firstName First name.
      * @param lastName Last name.
      * @param avatarUrl Avatar URL.
      * @param propsOpt Properties.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateUser(
        id: Long,
        email: String,
        passwdSalt:String,
        firstName: String,
        lastName: String,
        avatarUrl: Option[String],
        propsOpt: Option[Map[String, String]],
        parent: Span
    ): Int =
        startScopedSpan("updateUser", parent, "usrId" → id) { span ⇒
            val n =
                NCSql.update(
                    s"""
                       |UPDATE nc_user
                       |SET
                       |    email = ?,
                       |    passwd_salt = ?,
                       |    first_name = ?,
                       |    last_name = ?,
                       |    avatar_url = ?,
                       |    last_modified_on = ?
                       |WHERE id = ?
                        """.stripMargin,
                    email,
                    passwdSalt,
                    firstName,
                    lastName,
                    avatarUrl.orNull,
                    U.nowUtcTs(),
                    id
                )

            NCSql.delete("DELETE FROM nc_user_property WHERE user_id = ?", id)

            addUserProperties(id, propsOpt, span)

            n
        }


    /**
      *
      * @param id
      * @param propsOpt
      * @param parent Optional parent span.
      */
    private def addUserProperties(id: Long, propsOpt: Option[Map[String, String]], parent: Span): Unit =
        startScopedSpan("addUserProperties", parent, "usrId" → id) { _ ⇒
            propsOpt match {
                case Some(props) ⇒
                    val now = U.nowUtcTs()
         
                    props.foreach { case (k, v) ⇒
                        NCSql.insert(
                            s"""
                               |INSERT INTO nc_user_property (id, user_id, property, value, created_on, last_modified_on)
                               |VALUES(?, ?, ?, ?, ?, ?)
                            """.stripMargin,
                            usersPropsSeq.getAndIncrement(),
                            id,
                            k,
                            v,
                            now,
                            now
                        )
                    }
                case None ⇒ // No-op.
            }
        }

    /**
      * Updates user.
      *
      * @param id ID of the user to update.
      * @param isAdmin Admin flag.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateUserAdmin(id: Long, isAdmin: Boolean, parent: Span): Int =
        startScopedSpan("updateUserAdmin", parent, "usrId" → id, "isAdmin" → isAdmin) { _ ⇒
            NCSql.update(
                s"""
                   |UPDATE nc_user
                   |SET
                   |    is_admin = ?,
                   |    last_modified_on = ?
                   |WHERE id = ?
                    """.stripMargin,
                isAdmin,
                U.nowUtcTs(),
                id
            )
        }

    /**
      * Updates company.
      *
      * @param id Company's ID.
      * @param name Company's name.
      * @param website Company's website. Optional.
      * @param country Company's country. Optional.
      * @param region Company's region. Optional.
      * @param city Company's city. Optional.
      * @param address Company's address. Optional.
      * @param postalCode Company's postal code. Optional.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateCompany(
        id: Long,
        name: String,
        website: Option[String],
        country: Option[String],
        region: Option[String],
        city: Option[String],
        address: Option[String],
        postalCode: Option[String],
        parent: Span): Int =
        startScopedSpan("updateCompany", parent, "compId" → id) { _ ⇒
            NCSql.update(
                s"""
                   |UPDATE nc_company
                   |SET
                   |    name = ?,
                   |    website = ?,
                   |    country = ?,
                   |    region = ?,
                   |    city = ?,
                   |    address = ?,
                   |    postal_code = ?,
                   |    last_modified_on = ?
                   |WHERE id = ?
                    """.stripMargin,
                name,
                website.orNull,
                country.orNull,
                region.orNull,
                city.orNull,
                address.orNull,
                postalCode.orNull,
                U.nowUtcTs(),
                id
            )
        }

    /**
      * Updates company token.
      *
      * @param id Company's ID.
      * @param tkn Company's token.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateCompanyToken(id: Long, tkn: String, parent: Span): Int =
        startScopedSpan("updateCompanyToken", parent, "compId" → id, "tkn" → tkn) { _ ⇒
            NCSql.update(
                s"""
                   |UPDATE nc_company
                   |SET
                   |    auth_token = ?,
                   |    auth_token_hash = ?,
                   |    last_modified_on = ?
                   |WHERE id = ?
                    """.stripMargin,
                tkn,
                U.mkSha256Hash(tkn),
                U.nowUtcTs(),
                id
            )
        }

    /**
      * Gets user for given ID.
      *
      * @param id User ID.
      * @return User MDO.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getUserById(id: Long, parent: Span): Option[NCUserMdo] =
        startScopedSpan("getUser", parent, "usrId" → id) { _ ⇒
            NCSql.selectSingle[NCUserMdo](
                s"""
                   |SELECT *
                   |FROM nc_user
                   |WHERE id = ?
                """.stripMargin,
                id
            )
        }

    /**
      * Gets company for given ID.
      *
      * @param id Company ID.
      * @return Company MDO.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getCompany(id: Long, parent: Span): Option[NCCompanyMdo] =
        startScopedSpan("getCompany", parent, "compId" → id) { _ ⇒
            NCSql.selectSingle[NCCompanyMdo](
                s"""
                   |SELECT *
                   |FROM nc_company
                   |WHERE id = ?
                """.stripMargin,
                id
            )
        }

    /**
      * Gets company for given token hash.
      *
      * @param hash Company token hash.
      * @return Company MDO.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getCompanyByHashToken(hash: String, parent: Span): Option[NCCompanyMdo] =
        startScopedSpan("getCompanyByHashToken", parent, "hash" → hash) { _ ⇒
            NCSql.selectSingle[NCCompanyMdo](
                s"""
                   |SELECT *
                   |FROM nc_company
                   |WHERE auth_token_hash = ?
            """.stripMargin,
                hash
            )
        }

    /**
      * Gets company for given name.
      *
      * @param name Company name.
      * @return Company MDO.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getCompanyByName(name: String, parent: Span): Option[NCCompanyMdo] =
        startScopedSpan("getCompanyByName", parent, "name" → name) { _ ⇒
            NCSql.selectSingle[NCCompanyMdo](
                s"""
                   |SELECT *
                   |FROM nc_company
                   |WHERE name = ?
            """.stripMargin,
                name
            )
        }

    /**
      * Gets user properties for given ID.
      *
      * @param id User ID.
      * @param parent Optional parent span.
      * @return User properties.
      *
      */
    @throws[NCE]
    def getUserProperties(id: Long, parent: Span): Seq[NCUserPropertyMdo] =
        startScopedSpan("getUserProperties", parent, "usrId" → id) { _ ⇒
            NCSql.select[NCUserPropertyMdo]("SELECT * FROM nc_user_property WHERE user_id = ?", id)
        }

    /**
      * Gets user properties for given external ID.
      *
      * @param companyId Company ID.
      * @param extUsrId External user ID.
      * @param parent Optional parent span.
      *
      * @return User ID.
      */
    @throws[NCE]
    def getUserId(companyId: Long, extUsrId: String, parent: Span): Option[Long] =
        startScopedSpan("getUserId", parent, "companyId" → companyId, "extUsrId" → extUsrId) { _ ⇒
            NCSql.selectSingle[Long](
                "SELECT id FROM nc_user WHERE company_id = ? AND ext_id = ?", companyId, extUsrId
            )
        }

    /**
      * Gets processing log records count.
      *
      * @return Records count.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getLogsCount(parent: Span): Int =
        startScopedSpan("getLogsCount", parent) { _ ⇒
            NCSql.selectSingle[Int]("SELECT count(*) FROM proc_log").getOrElse(0)
        }

    /**
      * Gets all users for given company ID.
      *
      * @return All users.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getAllUsers(compId: Long, parent: Span): Map[NCUserMdo, Seq[NCUserPropertyMdo]] =
        startScopedSpan("getAllUsers", parent, "compId" → compId) { _ ⇒
            val props = NCSql.select[NCUserPropertyMdo](
                "SELECT * FROM nc_user_property WHERE user_id IN (SELECT id FROM nc_user WHERE company_id = ?)",
                compId
            ).groupBy(_.userId)
         
            NCSql.select[NCUserMdo]("SELECT * FROM nc_user WHERE company_id = ?", compId).
                map(p ⇒ p → props.getOrElse(p.id, Nil)).toMap
        }

    /**
      * Gets flag which indicates there are another admin users in the system or not.
      *
      * @param usrId User ID.
      * @return Flag.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def isOtherAdminsExist(usrId: Long, parent: Span): Boolean =
        startScopedSpan("isOtherAdminsExist", parent, "usrId" → usrId) { _ ⇒
            NCSql.exists("nc_user WHERE id <> ? AND is_admin = ?", usrId, true)
        }

    /**
      * Adds new company with given parameters.
      *
      * @param id Company's ID.
      * @param name Company's name.
      * @param tkn Company's token.
      * @param website Company's website. Optional.
      * @param country Company's country. Optional.
      * @param region Company's region. Optional.
      * @param city Company's city. Optional.
      * @param address Company's address. Optional.
      * @param postalCode Company's postal code. Optional.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def addCompany(
        id: Long,
        name: String,
        tkn: String,
        website: Option[String],
        country: Option[String],
        region: Option[String],
        city: Option[String],
        address: Option[String],
        postalCode: Option[String],
        parent: Span): Unit =
        startScopedSpan("addCompany", parent, "compId" → id, "name" → name, "tkn" → tkn) { _ ⇒
            val now = U.nowUtcTs()
    
            // Insert user.
            NCSql.insert(
                """
                  |INSERT INTO nc_company(
                  |    id,
                  |    name,
                  |    website,
                  |    country,
                  |    region,
                  |    city,
                  |    address,
                  |    postal_code,
                  |    auth_token,
                  |    auth_token_hash,
                  |    created_on,
                  |    last_modified_on
                  |)
                  | VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
              """.stripMargin,
                id,
                name,
                website.orNull,
                country.orNull,
                region.orNull,
                city.orNull,
                address.orNull,
                postalCode.orNull,
                tkn,
                U.mkSha256Hash(tkn),
                now,
                now
            )
        }

    /**
      * Adds new user with given parameters.
      *
      * @param id User's ID.
      * @param compId User's company ID.
      * @param extId User's external ID. Optional.
      * @param email User's normalized email. Optional.
      * @param firstName User's first name. Optional.
      * @param lastName User's last name. Optional.
      * @param avatarUrl User's avatar URL. Optional.
      * @param passwdSalt Salt for password Blowfish hashing. Optional.
      * @param isAdmin Whether or not the user is admin.
      * @param propsOpt User properties.  Optional.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def addUser(
        id: Long,
        compId: Long,
        extId: Option[String],
        email: Option[String],
        firstName: Option[String],
        lastName: Option[String],
        avatarUrl: Option[String],
        passwdSalt: Option[String],
        isAdmin: Boolean,
        propsOpt: Option[Map[String, String]],
        parent: Span
    ): Unit = {
        require(extId.isDefined ^ email.isDefined)
        require(email.isDefined || !isAdmin && firstName.isEmpty && lastName.isEmpty && avatarUrl.isEmpty && passwdSalt.isEmpty)
        require(email.isEmpty || firstName.isDefined && lastName.isDefined && passwdSalt.isDefined)

        startScopedSpan(
            "addUser",
            parent,
            "usrId" → id,
            "compId" → compId,
            "email" → email.orNull,
            "username" → extId.orNull) { span ⇒
            val now = U.nowUtcTs()

            // Insert user.
            NCSql.insert(
                """
                  | INSERT INTO nc_user(
                  |    id,
                  |    company_id,
                  |    first_name,
                  |    last_name,
                  |    ext_id,
                  |    email,
                  |    passwd_salt,
                  |    avatar_url,
                  |    is_admin,
                  |    created_on,
                  |    last_modified_on
                  | )
                  | VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.stripMargin,
                id,
                compId,
                firstName.orNull,
                lastName.orNull,
                extId.orNull,
                email.orNull,
                passwdSalt.orNull,
                avatarUrl.orNull,
                isAdmin,
                now,
                now
            )

            addUserProperties(id, propsOpt, span)
        }
    }
    
    /**
      * Adds processing log.
      *
      * @param id Id.
      * @param usrId User Id.
      * @param srvReqId Server request ID.
      * @param txt Original text.
      * @param mdlId Data model ID.
      * @param usrAgent User agent string.
      * @param rmtAddr Remote user address.
      * @param rcvTstamp Receive timestamp.
      * @param data Optional sentence additional data.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def newProcessingLog(
        id: Long,
        usrId: Long,
        srvReqId: String,
        txt: String,
        mdlId: String,
        status: NCApiStatusCode,
        usrAgent: String,
        rmtAddr: String,
        rcvTstamp: Timestamp,
        data: String,
        parent: Span): Unit =
        startScopedSpan("newProcessingLog", parent, "usrId" → id, "srvReqId" → srvReqId, "txt" → txt, "modelId" → mdlId) { _ ⇒
            NCSql.insert(
                """
                  |INSERT INTO proc_log (
                  |     id,
                  |     user_id,
                  |     srv_req_id,
                  |     txt,
                  |     model_id,
                  |     status,
                  |     user_agent,
                  |     rmt_address,
                  |     recv_tstamp,
                  |     sen_data
                  | )
                  | VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
              """.stripMargin,
                id,
                usrId,
                srvReqId,
                txt,
                mdlId,
                status.toString,
                usrAgent,
                rmtAddr,
                rcvTstamp,
                data
            )
        }

    /**
      *
      * @param srvReqId Server request ID.
      * @param tstamp
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateCancelProcessingLog(
        srvReqId: String,
        tstamp: Timestamp,
        parent: Span): Unit =
        startScopedSpan("updateCancelProcessingLog", parent, "srvReqId" → srvReqId) { _ ⇒
            NCSql.update(
                """
                  |UPDATE proc_log
                  |SET
                  |    status = ?,
                  |    cancel_tstamp = ?
                  |WHERE srv_req_id = ?
                """.stripMargin,
                "QRY_CANCELLED",
                tstamp,
                srvReqId
            )
        }

    /**
      * Updates processing log.
      *
      * @param srvReqId Server request ID.
      * @param errMsg
      * @param resType
      * @param resBody
      * @param intentId
      * @param tstamp
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateReadyProcessingLog(
        srvReqId: String,
        errMsg: String,
        resType: String,
        resBody: String,
        intentId: String,
        tstamp: Timestamp,
        parent: Span): Unit =
        startScopedSpan("updateReadyProcessingLog", parent, "srvReqId" → srvReqId) { _ ⇒
            NCSql.update(
                """
                  |UPDATE proc_log
                  |SET
                  |    status = ?,
                  |    error = ?,
                  |    res_type = ?,
                  |    res_body_gzip = ?,
                  |    intent_id = ?,
                  |    resp_tstamp = ?
                  |WHERE srv_req_id = ?
                """.stripMargin,
                QRY_READY.toString,
                errMsg,
                resType,
                if (resBody == null) null else U.compress(resBody),
                intentId,
                tstamp,
                srvReqId
            )
        }

    /**
      * Updates processing log.
      *
      * @param srvReqId Server request ID.
      * @param probeToken
      * @param probeId Probe ID.
      * @param probeGuid
      * @param probeApiVersion
      * @param probeApiDate
      * @param osVersion
      * @param osName
      * @param osArch
      * @param startTstamp
      * @param tmzId
      * @param tmzAbbr
      * @param tmzName
      * @param userName
      * @param javaVersion
      * @param javaVendor
      * @param hostName
      * @param hostAddr
      * @param macAddr
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def updateProbeProcessingLog(
        srvReqId: String,
        probeToken: String,
        probeId: String,
        probeGuid: String,
        probeApiVersion: String,
        probeApiDate: java.sql.Date,
        osVersion: String,
        osName: String,
        osArch: String,
        startTstamp: Timestamp,
        tmzId: String,
        tmzAbbr: String,
        tmzName: String,
        userName: String,
        javaVersion: String,
        javaVendor: String,
        hostName: String,
        hostAddr: String,
        macAddr: String,
        parent: Span): Unit =
        startScopedSpan("updateProbeProcessingLog", parent, "srvReqId" → srvReqId) { _ ⇒
            NCSql.update(
                """
                  |UPDATE proc_log
                  |SET
                  |    probe_token = ?,
                  |    probe_id = ?,
                  |    probe_guid = ?,
                  |    probe_api_version = ?,
                  |    probe_api_date = ?,
                  |    probe_os_version = ?,
                  |    probe_os_name = ?,
                  |    probe_os_arch = ?,
                  |    probe_start_tstamp = ?,
                  |    probe_tmz_id = ?,
                  |    probe_tmz_abbr = ?,
                  |    probe_tmz_name = ?,
                  |    probe_user_name = ?,
                  |    probe_java_version = ?,
                  |    probe_java_vendor = ?,
                  |    probe_host_name = ?,
                  |    probe_host_addr = ?,
                  |    probe_mac_addr = ?
                  |WHERE srv_req_id = ?
              """.stripMargin,
                probeToken,
                probeId,
                probeGuid,
                probeApiVersion,
                probeApiDate,
                osVersion,
                osName,
                osArch,
                startTstamp,
                tmzId,
                tmzAbbr,
                tmzName,
                userName,
                javaVersion,
                javaVendor,
                hostName,
                hostAddr,
                macAddr,
                srvReqId
            )
        }

    /**
      *
      * @param id
      * @param srvReqId Server request ID.
      * @param userId
      * @param score
      * @param comment
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def addFeedback(id: Long, srvReqId: String, userId: Long, score: Double, comment: Option[String], parent: Span): Long = {
        startScopedSpan("addFeedback", parent, "srvReqId" → srvReqId, "userId" → userId) { _ ⇒
            NCSql.insert(
                "INSERT INTO feedback(id, srv_req_id, user_id, score, comment, created_on) VALUES(?, ?, ?, ?, ?, ?)",
                id, srvReqId, userId, score, comment.orNull, U.nowUtcTs()
            )

            id
        }
    }

    /**
      *
      * @param id
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def deleteFeedback(id: Long, parent: Span): Unit = {
        startScopedSpan("deleteFeedback", parent) { _ ⇒
            NCSql.delete("DELETE FROM feedback WHERE id = ?", id)
        }
    }

    /**
      *
      * @param companyId Company ID.
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def deleteAllFeedback(companyId: Long, parent: Span): Unit = {
        startScopedSpan("deleteFeedback", parent) { _ ⇒
            NCSql.delete("DELETE FROM feedback WHERE user_id IN (SELECT id FROM nc_user WHERE company_id = ?)", companyId);
        }
    }

    /**
      *
      * @param companyId Company ID.
      * @param srvReqId Server request ID.
      * @param userId
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def getFeedback(companyId: Long, srvReqId: Option[String], userId: Option[Long], parent: Span): Seq[NCFeedbackMdo] = {
        startScopedSpan(
            "getFeedback",
            parent,
            "companyId" → companyId,
            "srvReqId" → srvReqId.orNull,
            "userId" → userId.getOrElse(() ⇒ null)
        ) { _ ⇒
            var sql = "SELECT * FROM feedback WHERE user_id IN (SELECT id from nc_user WHERE company_id = ?)"
            val params = collection.mutable.Buffer.empty[Any]

            params += companyId

            def add(name: String, vOpt: Option[Any]): Unit =
                vOpt match {
                    case Some(v) ⇒
                        sql = s"$sql AND $name = ?"
                        params += v
                    case None ⇒ // No-op.
                }

            add("srv_req_id", srvReqId)
            add("user_id", userId)

            NCSql.select[NCFeedbackMdo](sql, params :_*)
        }
    }

    /**
      *
      * @param id
      * @param parent Parent tracing span.
      */
    @throws[NCE]
    def getFeedback(id: Long, parent: Span): Option[NCFeedbackMdo] = {
        startScopedSpan("getFeedback", parent) { _ ⇒
            NCSql.selectSingle[NCFeedbackMdo]("SELECT * FROM feedback WHERE id = ?", id)
        }
    }

    /**
      *
      * @param sqlPath
      */
    @throws[NCE]
    private def executeScript(sqlPath: String): Unit = startScopedSpan("executeScript", "sqlPath" → sqlPath) { _ ⇒
        U.readResource(sqlPath, "UTF-8").
            map(_.trim).
            filter(p ⇒ !p.startsWith("--")).
            mkString("\n").
            split(";").
            map(_.trim).
            filter(!_.isEmpty).
            foreach(p ⇒ NCSql.ddl(p))
    }

    /**
      *
      */
    @throws[NCE]
    private def prepareIgniteSchema(): Unit = startScopedSpan("prepareIgniteSchema") { _ ⇒
        val schema = NCSql.sql {
            NCSql.getSchema
        }

        if ((if (schema == null) "" else schema.toLowerCase) != "nlpcraft")
            throw new NCE(s"Invalid Ignite database schema ('nlpcraft' only): $schema")

        def safeClear(): Unit =
            try
                executeScript("sql/drop_schema.sql")
            catch {
                case _: NCE ⇒ // No-op.
            }
        
        var init = Config.init

        if (init)
            logger.info(s"Ignite database schema initialization property found.")
        else {
            val existingTbls = NCSql.sql { NCSql.getSchemaTables("nlpcraft").map(_.toLowerCase) }
            val missingTbls = DB_TABLES.filter(t ⇒ !existingTbls.contains(t))

            missingTbls.size match {
                // All tables created.
                case 0 ⇒ // No-op.

                // First start.
                case size if size == existingTbls.size ⇒
                    init = true

                // Invalid previous start.
                case _  ⇒
                    logger.warn(
                        s"Missing Ignite tables detected: ${missingTbls.mkString(", ")}. " +
                        "Ignite database is going to be cleared and re-created again."
                    )

                    init = true
            }
        }

        if (init)
            NCSql.sql {
                try {
                    safeClear()
                    
                    executeScript("sql/create_schema.sql")

                    CACHE_2_CLEAR.foreach(name ⇒ ignite.cache(name).clear())

                    logger.info("Ignite database schema initialized.")
                }
                catch {
                    case e: NCE ⇒
                        safeClear()

                        throw e
                }
            }
    }
}