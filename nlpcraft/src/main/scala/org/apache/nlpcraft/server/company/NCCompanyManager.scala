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

package org.apache.nlpcraft.server.company

import io.opencensus.trace.Span
import org.apache.ignite.{IgniteAtomicSequence, IgniteSemaphore}
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.mdo.{NCCompanyMdo, NCCompanyPropertyMdo}
import org.apache.nlpcraft.server.sql.{NCSql, NCSqlManager}
import org.apache.nlpcraft.server.user.NCUserManager

import scala.util.control.Exception._

/**
  *
  * @param companyId Company ID.
  * @param token
  * @param adminId
  */
case class NCCompanyCreationData(companyId: Long, token: String, adminId: Long)

/**
  * Company CRUD manager.
  */
object NCCompanyManager extends NCService with NCIgniteInstance {
    @volatile private var compSeq: IgniteAtomicSequence = _
    @volatile private var compLock: IgniteSemaphore = _

    /**
      *
      */
    @throws[NCE]
    private def mkToken(): String = {
        val tok = U.genGuid()

        if (NCSqlManager.getCompanyByHashToken(U.mkSha256Hash(tok), null).isDefined)
            throw new NCE(s"Invalid attempt. Try again.")

        tok
    }

    /**
     *
     * @param parent Optional parent span.
     */
    override def stop(parent: Span): Unit = startScopedSpan("start", parent) { _ ⇒
        ackStopping()
        ackStopped()
    }

    /**
     *
     * @param parent Optional parent span.
     * @return
     */
    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        ackStarting()

        catching(wrapIE) {
            compSeq = NCSql.mkSeq(ignite, "compSeq", "nc_company", "id")
        }

        compLock = ignite.semaphore("compSemaphore", 1, true, true)

        NCSql.sql {
            if (!NCSql.exists("nc_company"))
                try {
                    val compName = "ETH Zurich"
                    val adminEmail = "admin@admin.com"
                    val adminPwd = "admin"

                    addCompany0(
                        name = compName,
                        website = None,
                        country = None,
                        region = None,
                        city = None,
                        address = None,
                        postalCode = None,
                        adminEmail = adminEmail,
                        adminPwd = adminPwd,
                        adminFirstName = "Hermann",
                        adminLastName = "Minkowski",
                        adminAvatarUrl = None,
                        props = None,
                        mkToken = () ⇒ U.DFLT_PROBE_TOKEN,
                        parent = span
                    )

                    logger.info(s"Default admin user ($adminEmail/$adminPwd) created for default company: $compName")
                }
                catch {
                    case e: NCE ⇒ U.prettyError(
                        logger,
                        s"Failed to add default admin user: ${e.getLocalizedMessage}",
                        e
                    )
                }
        }

        ackStarted()
    }

    /**
      * Gets user for given user ID.
      *
      * @param id User ID.
      * parent Optional parent span.
      */
    @throws[NCE]
    def getCompany(id: Long, parent: Span = null): Option[NCCompanyMdo] =
        startScopedSpan("getCompany", parent, "id" → id) { span ⇒
            NCSql.sql {
                NCSqlManager.getCompany(id, span)
            }
        }

    /**
      * Gets user for given token hash.
      *
      * @param hash Token hash.
      * parent Optional parent span.
      */
    @throws[NCE]
    def getCompanyByHashToken(hash: String, parent: Span = null): Option[NCCompanyMdo] =
        startScopedSpan("getCompanyByHashToken", parent, "hash" → hash) { span ⇒
            NCSql.sql {
                NCSqlManager.getCompanyByHashToken(hash, span)
            }
        }

    /**
      *
      * @param id
      * @param name
      * @param website
      * @param country
      * @param region
      * @param city
      * @param address
      * @param postalCode
      * @param props
      * @param parent
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
        props: Option[Map[String, String]],
        parent: Span = null
    ): Unit =
        startScopedSpan("updateCompany", parent, "id" → id) { span ⇒
            try {
                compLock.acquire()
         
                NCSql.sql {
                    NCSqlManager.getCompanyByName(name, span) match {
                        case Some(c) ⇒
                            if (c.id != id)
                                throw new NCE(s"Company with this name already exists: $name")
         
                        case None ⇒ // No-op.
                    }
         
                    val n =
                        NCSqlManager.updateCompany(
                            id = id,
                            name = name,
                            website = website,
                            country = country,
                            region = region,
                            city = city,
                            address = address,
                            postalCode = postalCode,
                            propsOpt = props,
                            parent = span
                        )
         
                    if (n == 0)
                        throw new NCE(s"Unknown company ID: $id")

                    logger.info(s"Company updated: $id")
                }
            }
            finally
                compLock.release()
        }

    /**
      *
      * @param id
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def resetToken(id: Long, parent: Span): String = startScopedSpan("resetToken", parent, "id" → id) { span ⇒
        try {
            compLock.acquire()

            NCSql.sql {
                val tnk = mkToken()

                val n = NCSqlManager.updateCompanyToken(id, tnk, span)

                if (n == 0)
                    throw new NCE(s"Unknown company ID: $id")

                tnk
            }
        }
        finally
            compLock.release()
    }

    /**
      *
      * @param id
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def deleteCompany(id: Long, parent: Span = null): Unit =
        startScopedSpan("deleteCompany", parent, "id" → id) { span ⇒
            NCSql.sql {
                if (NCSqlManager.deleteCompany(id, span) != 1)
                    throw new NCE(s"Unknown company ID: $id")
            }

            logger.info(s"Company deleted: $id")
        }

    /**
      *
      * @param name
      * @param website
      * @param country
      * @param region
      * @param city
      * @param address
      * @param postalCode
      * @param adminEmail
      * @param adminPwd
      * @param adminFirstName
      * @param adminLastName
      * @param adminAvatarUrl
      * @param props
      * @param mkToken
      * @param parent
      */
    @throws[NCE]
    private def addCompany0(
        name: String,
        website: Option[String],
        country: Option[String],
        region: Option[String],
        city: Option[String],
        address: Option[String],
        postalCode: Option[String],
        adminEmail: String,
        adminPwd: String,
        adminFirstName: String,
        adminLastName: String,
        adminAvatarUrl: Option[String],
        props: Option[Map[String, String]],
        mkToken: () ⇒ String,
        parent: Span = null
    ): NCCompanyCreationData = {
        val compId = compSeq.incrementAndGet()
    
        startScopedSpan("addCompany0", parent, "id" → compId, "name" → name) { span ⇒
            NCSql.sql {
                val tkn =
                    // Some database implementations (including Ignite database) may not support unique constraints.
                    // Because we have to support user email unique values, adding user operation is synchronized.
                    try {
                        compLock.acquire()
    
                        if (NCSqlManager.getCompanyByName(name, span).isDefined)
                            throw new NCE(s"Company with this name already exists: $name")
    
                        val tkn = mkToken()
    
                        NCSqlManager.addCompany(
                            compId,
                            name,
                            tkn,
                            website,
                            country,
                            region,
                            city,
                            address,
                            postalCode,
                            props,
                            span
                        )
    
                        tkn
                    }
                    finally
                        compLock.release()
    
                val adminId = NCUserManager.addUser(
                    compId,
                    adminEmail,
                    adminPwd,
                    adminFirstName,
                    adminLastName,
                    adminAvatarUrl,
                    isAdmin = true,
                    props = None,
                    usrExtIdOpt = None,
                    parent = span
                )
    
                logger.info(s"Company '$name' created.")
    
                NCCompanyCreationData(compId, tkn, adminId)
            }
        }
    }

    /**
      *
      * @param name
      * @param website
      * @param country
      * @param region
      * @param city
      * @param address
      * @param postalCode
      * @param adminEmail
      * @param adminPwd
      * @param adminFirstName
      * @param adminLastName
      * @param props
      * @param adminAvatarUrl
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def addCompany(
        name: String,
        website: Option[String],
        country: Option[String],
        region: Option[String],
        city: Option[String],
        address: Option[String],
        postalCode: Option[String],
        adminEmail: String,
        adminPwd: String,
        adminFirstName: String,
        adminLastName: String,
        adminAvatarUrl: Option[String],
        props: Option[Map[String, String]],
        parent: Span = null
    ): NCCompanyCreationData = startScopedSpan("addCompany", parent, "name" → name) { _ ⇒
        addCompany0(
            name,
            website,
            country,
            region,
            city,
            address,
            postalCode,
            adminEmail,
            adminPwd,
            adminFirstName,
            adminLastName,
            adminAvatarUrl,
            props,
            () ⇒ mkToken()
        )
    }

    /**
      * Gets company properties for given company ID.
      *
      * @param id User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getCompanyProperties(id: Long, parent: Span = null): Seq[NCCompanyPropertyMdo] =
        startScopedSpan("getCompanyProperties", parent, "companyId" → id) { span ⇒
            NCSql.sql {
                NCSqlManager.getCompanyProperties(id, span)
            }
        }
}
