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

package org.apache.nlpcraft.server.user

import java.util.{Timer, TimerTask}

import io.opencensus.trace.Span
import org.apache.commons.validator.routines.EmailValidator
import org.apache.ignite.{IgniteAtomicSequence, IgniteCache, IgniteSemaphore, IgniteState, Ignition}
import org.apache.nlpcraft.common.blowfish.NCBlowfishHasher
import org.apache.nlpcraft.common.config.NCConfigurable
import org.apache.nlpcraft.common.{NCService, _}
import org.apache.nlpcraft.server.ignite.NCIgniteHelpers._
import org.apache.nlpcraft.server.ignite.NCIgniteInstance
import org.apache.nlpcraft.server.mdo.{NCUserMdo, NCUserPropertyMdo}
import org.apache.nlpcraft.server.sql.{NCSql, NCSqlManager}
import org.apache.nlpcraft.server.tx.NCTxManager

import scala.collection.JavaConverters._
import scala.util.control.Exception._

/**
  * User CRUD manager.
  */
object NCUserManager extends NCService with NCIgniteInstance {
    // Static email validator.
    private final val EMAIL_VALIDATOR = EmailValidator.getInstance()

    // Caches.
    @volatile private var tokenSigninCache: IgniteCache[String, SigninSession] = _
    @volatile private var idSigninCache: IgniteCache[Long, Set[String]] = _

    @volatile private var usersSeq: IgniteAtomicSequence = _
    @volatile private var pwdSeq: IgniteAtomicSequence = _
    @volatile private var userLock: IgniteSemaphore = _

    // Access token timeout scanner.
    @volatile private var scanner: Timer = _

    // Session holder.
    private case class SigninSession(
        acsToken: String,
        userId: Long,
        signinMs: Long,
        lastAccessMs: Long
    )

    private object Config extends NCConfigurable {
        final private val pre = "nlpcraft.server.user"

        def pwdPoolBlowup: Int = getInt(s"$pre.pwdPoolBlowup")
        def timeoutScannerFreqMins: Int = getInt(s"$pre.timeoutScannerFreqMins")
        def accessTokenExpireTimeoutMins: Int = getInt(s"$pre.accessTokenExpireTimeoutMins")

        def scannerMs: Int = timeoutScannerFreqMins * 60 * 1000
        def expireMs: Int = accessTokenExpireTimeoutMins * 60 * 1000

        /**
          *
          */
        def check(): Unit = {
            if (pwdPoolBlowup <= 1)
                abortWith(s"Configuration parameter '$pre.pwdPoolBlowup' must be > 1")
            if (timeoutScannerFreqMins <= 0)
                abortWith(s"Configuration parameter '$pre.timeoutScannerFreqMins' must be > 0")
            if (accessTokenExpireTimeoutMins <= 0)
                abortWith(s"Configuration parameter '$pre.accessTokenExpireTimeoutMins' must be > 0")
        }
    }

    Config.check()

    override def stop(parent: Span): Unit = startScopedSpan("stop", parent) { _ ⇒
        if (scanner != null)
            scanner.cancel()

        scanner = null

        tokenSigninCache = null
        idSigninCache = null

        super.stop()
    }

    override def start(parent: Span = null): NCService = startScopedSpan("start", parent) { span ⇒
        addTags(
            span,
            "pwdPoolBlowup" → Config.pwdPoolBlowup,
            "timeoutScannerFreqMins" → Config.timeoutScannerFreqMins,
            "accessTokenExpireTimeoutMins" → Config.accessTokenExpireTimeoutMins
        )

        catching(wrapIE) {
            usersSeq = NCSql.mkSeq(ignite, "usersSeq", "nc_user", "id")
            pwdSeq = NCSql.mkSeq(ignite, "pwdSeq", "passwd_pool", "id")

            tokenSigninCache = ignite.cache[String, SigninSession]("user-token-signin-cache")
            idSigninCache = ignite.cache[Long, Set[String]]("user-id-signin-cache")

            require(tokenSigninCache != null)
            require(idSigninCache != null)
        }

        scanner = new Timer("timeout-scanner")

        scanner.scheduleAtFixedRate(
            new TimerTask() {
                def run() {
                    // This doesn't 100% guarantee that we won't run into a race condition
                    // with the shutdown hook on Ignite.
                    if (Ignition.state() == IgniteState.STARTED)
                        try {
                            val now = U.nowUtcMs()

                            // Check access tokens for expiration.
                            catching(wrapIE) {
                                NCTxManager.startTx {
                                    for (ses ← tokenSigninCache.asScala.map(_.getValue)
                                        if now - ses.lastAccessMs >= Config.expireMs
                                    ) {
                                        tokenSigninCache -= ses.acsToken
                                        clearSigninCache(ses)

                                        logger.trace(s"Access token timed out: ${ses.acsToken}")
                                    }
                                }
                            }
                        }
                        catch {
                            case e: IllegalStateException ⇒
                                // Attempt to hide possible race condition with Ignite on a shutdown.
                                if (!e.getMessage.startsWith("Grid is in invalid state to perform this operation"))
                                    logger.error("Error during timeout scanner process.", e)

                            case e: Throwable ⇒ logger.error("Error during timeout scanner process.", e)
                        }
                }
            },
            Config.scannerMs,
            Config.scannerMs
        )

        userLock = ignite.semaphore("userSemaphore", 1, true, true)

        logger.info(s"Access tokens will be scanned for timeout every ${Config.timeoutScannerFreqMins} min.")
        logger.info(s"Access tokens inactive for ${Config.accessTokenExpireTimeoutMins} min will be invalidated.")

        super.start()
    }

    /**
      * Gets the list of all current users for given company ID.
      *
      * @param compId Company ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getAllUsers(compId: Long, parent: Span = null): Map[NCUserMdo, Seq[NCUserPropertyMdo]] =
        NCSql.sql {
            NCSqlManager.getAllUsers(compId, parent)
        }

    /**
      * Gets flag which indicates there are another admin users in the system or not.
      *
      * @param id User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def isOtherAdminsExist(id: Long, parent: Span = null): Boolean =
        NCSql.sql {
            NCSqlManager.isOtherAdminsExist(id, parent)
        }

    /**
      *
      * @param ses
      */
    private def clearSession(ses: SigninSession): Unit = {
        clearSigninCache(ses)
        
        logger.info(s"User signed out: ${ses.userId}")
    }

    /**
      *
      * @param acsTok Access token to sign out.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def signout(acsTok: String, parent: Span = null): Unit =
        startScopedSpan("signout", parent, "acsTok" → acsTok) { _ ⇒
            catching(wrapIE) {
                NCTxManager.startTx {
                    tokenSigninCache -== acsTok match {
                        case Some(ses) ⇒ clearSession(ses)
                        case None ⇒ // No-op.
                    }
                }
            }
        }

    /**
      *
      * @param userId User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def signoutAllSessions(userId: Long, parent: Span = null): Unit =
        startScopedSpan("signout", parent, "userId" → userId) { _ ⇒
            catching(wrapIE) {
                NCTxManager.startTx {
                    idSigninCache -== userId match {
                        case Some(acsToks) ⇒
                            acsToks.foreach(acsTok ⇒
                                tokenSigninCache -== acsTok match {
                                    case Some(ses) ⇒ clearSession(ses)
                                    case None ⇒ // No-op.
                                }
                            )

                        case None ⇒ // No-op.
                    }
                }
            }
        }

    /**
      * Gets user ID associated with active access token, if any.
      *
      * @param acsTok Access token.
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def getUserForAccessToken(acsTok: String, parent: Span = null): Option[NCUserMdo] =
        startScopedSpan("getUserForAccessToken", parent, "acsTok" → acsTok) { span ⇒
            getUserIdForAccessToken(acsTok, span).flatMap(getUserById(_, span))
        }

    /**
      * Gets user ID associated with active access token, if any.
      *
      * @param acsTok Access token.
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def getUserIdForAccessToken(acsTok: String, parent: Span = null): Option[Long] =
        startScopedSpan("getUserIdForAccessToken", parent, "acsTok" → acsTok) { _ ⇒
            catching(wrapIE) {
                tokenSigninCache(acsTok) match {
                    case Some(ses) ⇒
                        val now = U.nowUtcMs()

                        // Update login session.
                        tokenSigninCache += acsTok → SigninSession(acsTok, ses.userId, ses.signinMs, now)

                        Some(ses.userId) // Bingo!

                    case None ⇒ None
                }
            }
        }

    /**
      * Gets user for given user ID.
      *
      * @param id User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getUserById(id: Long, parent: Span = null): Option[NCUserMdo] =
        startScopedSpan("getUser", parent, "usrId" → id) { span ⇒
            NCSql.sql {
                NCSqlManager.getUserById(id, span)
            }
        }

    /**
      * Gets user properties for given user ID.
      *
      * @param id User ID.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def getUserProperties(id: Long, parent: Span = null): Seq[NCUserPropertyMdo] =
        startScopedSpan("getUserProperties", parent, "usrId" → id) { span ⇒
            NCSql.sql {
                NCSqlManager.getUserProperties(id, span)
            }
        }

    /**
      *
      * @param email User email (as username).
      * @param passwd User password.
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def signin(email: String, passwd: String, parent: Span = null): Option[String] =
        startScopedSpan("signin", parent, "email" → email) { span ⇒
            catching(wrapIE) {
                NCTxManager.startTx {
                    NCSql.sql {
                        NCSqlManager.getUserByEmail(email, span)
                    } match {
                        case Some(usr) ⇒
                            require(usr.passwordSalt.isDefined)

                            NCSql.sql {
                                if (!NCSqlManager.isKnownPasswordHash(NCBlowfishHasher.hash(passwd, usr.passwordSalt.get), span))
                                    None
                                else {
                                    val acsTkn = U.genGuid()
                                    val now = U.nowUtcMs()

                                    tokenSigninCache += acsTkn → SigninSession(acsTkn, usr.id, now, now)

                                    idSigninCache(usr.id) match {
                                        case Some(toks) ⇒ idSigninCache += usr.id → (toks ++ Set(acsTkn))
                                        case None ⇒ idSigninCache += usr.id → Set(acsTkn)
                                    }

                                    require(usr.email.isDefined && usr.firstName.isDefined && usr.lastName.isDefined)

                                    logger.info(s"User signed in [" +
                                        s"id=${usr.id}, " +
                                        s"email=${usr.email.get}, " +
                                        s"name=${usr.firstName.get} ${usr.lastName.get}" +
                                        s"]")

                                    Some(acsTkn)
                                }
                            }
                        case None ⇒ None
                    }
                }
            }
        }

    /**
      *
      * @param id
      * @param firstName
      * @param lastName
      * @param avatarUrl
      * @param props
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def updateUser(
        id: Long,
        firstName: String,
        lastName: String,
        avatarUrl: Option[String],
        props: Option[Map[String, String]],
        parent: Span = null): Unit =
        startScopedSpan("updateUser", parent, "usrId" → id) { span ⇒
            NCSql.sql {
                if (NCSqlManager.updateUser(id, firstName, lastName, avatarUrl, props, span) != 1)
                    throw new NCE(s"Unknown user ID: $id")
            }
        }

    /**
      *
      * @param id
      * @param isAdmin
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def updateUserPermissions(id: Long, isAdmin: Boolean, parent: Span = null): Unit =
        startScopedSpan("updateUserPermissions", parent, "usrId" → id, "isAdmin" → isAdmin) { span ⇒
            NCSql.sql {
                if (NCSqlManager.updateUserAdmin(id, isAdmin, span) != 1)
                    throw new NCE(s"Unknown user ID: $id")
            }
        }

    /**
      *
      * @param id
      * @param parent Optional parent span.
      * @return
      */
    @throws[NCE]
    def deleteUser(id: Long, parent: Span = null): Unit =
        startScopedSpan("deleteUser", parent, "usrId" → id) { span ⇒
            NCSql.sql {
                if (NCSqlManager.deleteUser(id, span) != 1)
                    throw new NCE(s"Unknown user ID: $id")
            }
        }

    /**
      *
      * @param id ID of the user to reset password for.
      * @param newPasswd New password to set.
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def resetPassword(id: Long, newPasswd: String, parent: Span = null): Unit =
        startScopedSpan("resetPassword", parent, "usrId" → id) { span ⇒
            NCSql.sql {
                val usr = NCSqlManager.getUserById(id, span).getOrElse(throw new NCE(s"Unknown user ID: $id"))

                require(usr.email.isDefined)

                val salt = NCBlowfishHasher.hash(usr.email.get)

                // Add actual hash for the password.
                // NOTE: we don't "stir up" password pool for password resets.
                NCSqlManager.addPasswordHash(pwdSeq.incrementAndGet(), NCBlowfishHasher.hash(newPasswd, salt), span)
            }

            catching(wrapIE) {
                NCTxManager.startTx {
                    idSigninCache(id) match {
                        case Some(toks) ⇒
                            tokenSigninCache --= toks
                            idSigninCache -= id

                        case None ⇒ // No-op.
                    }
                }
            }
        }

    /**
      *
      * @param compId
      * @param email
      * @param pwd
      * @param firstName
      * @param lastName
      * @param avatarUrl
      * @param isAdmin
      * @param props
      * @param extIdOpt
      * @param parent Optional parent span.
      */
    @throws[NCE]
    def addUser(
        compId: Long,
        email: String,
        pwd: String,
        firstName: String,
        lastName: String,
        avatarUrl: Option[String],
        isAdmin: Boolean,
        props: Option[Map[String, String]],
        extIdOpt: Option[String],
        parent: Span = null
    ): Long =
        startScopedSpan(
            "addUser",
            parent,
            "compId" → compId,
            "email" → email,
            "firstName" → firstName,
            "lastName" → lastName,
            "exitId" → extIdOpt.orNull,
            "isAdmin" → isAdmin) { span ⇒
            val normEmail = U.normalizeEmail(email)

            if (!EMAIL_VALIDATOR.isValid(normEmail))
                throw new NCE(s"New user email is invalid: $normEmail")

            val salt = NCBlowfishHasher.hash(normEmail)

            NCSql.sql {
                // Some database implementations (including Ignite database) may not support unique constraints.
                // Because we have to support user email unique values, adding user operation is synchronized.
                val id =
                    try {
                        userLock.acquire()

                        if (NCSqlManager.getUserByEmail(normEmail, span).isDefined)
                            throw new NCE(s"User with this email already exists: $normEmail")

                        extIdOpt match {
                            case Some(extId) ⇒
                                val id = NCSqlManager.
                                    getUserId(compId, extId, span).
                                    getOrElse(throw new NCE(s"User not found [companyId=$compId, extId=$extId]"))

                                    NCSqlManager.updateUser(
                                        id = id,
                                        email = normEmail,
                                        passwdSalt = salt,
                                        firstName = firstName,
                                        lastName = lastName,
                                        avatarUrl = avatarUrl,
                                        propsOpt = props,
                                        parent = span
                                    )

                                logger.info(s"User converted [extId=$extId, email=$email]")

                                id
                            case None ⇒
                                val newUsrId = usersSeq.incrementAndGet()

                                NCSqlManager.addUser(
                                    id = newUsrId,
                                    compId = compId,
                                    extId = None,
                                    email = Some(normEmail),
                                    firstName = Some(firstName),
                                    lastName = Some(lastName),
                                    avatarUrl = avatarUrl,
                                    passwdSalt = Some(salt),
                                    isAdmin = isAdmin,
                                    propsOpt = props,
                                    parent = span
                                )

                                logger.info(s"User $email created.")

                                newUsrId
                        }
                    }
                    finally
                        userLock.release()

                // Add actual hash for the password.
                NCSqlManager.addPasswordHash(pwdSeq.incrementAndGet(), NCBlowfishHasher.hash(pwd, salt), span)

                // "Stir up" password pool with each user.
                (0 to Math.round((Math.random() * Config.pwdPoolBlowup) + Config.pwdPoolBlowup).toInt).foreach(_ ⇒
                    NCSqlManager.addPasswordHash(pwdSeq.incrementAndGet(), NCBlowfishHasher.hash(U.genGuid()), span)
                )

                id
            }
        }

    /**
      *
      * @param ses
      */
    @throws[NCE]
    private def clearSigninCache(ses: SigninSession): Unit =
        startScopedSpan("clearSigninCache", "usrId" → ses.userId) { _ ⇒
            catching(wrapIE) {
                idSigninCache(ses.userId) match {
                    case Some(toks) ⇒
                        val fixedToks = toks -- Seq(ses.acsToken)

                        if (fixedToks.isEmpty)
                            idSigninCache -= ses.userId
                        else
                            idSigninCache += ses.userId → fixedToks

                    case None ⇒ // No-op.
                }
            }
        }

    /**
      * Gets technical user ID for given external ID.
      * If user with given external ID is missing, it creates a new one and returns its user ID.
      *
      * @param companyId Company ID.
      * @param extUsrId External user ID.
      * @param parent Parent.
      */
    @throws[NCE]
    def getOrInsertExternalUserId(companyId: Long, extUsrId: String, parent: Span = null): Long =
        startScopedSpan(
            "getOrInsertExternalUserId",
            parent,
            "companyId" → companyId,
            "extUsrId" → extUsrId) { span ⇒
            NCSql.sql {
                NCSqlManager.getUserId(companyId, extUsrId, span) match {
                    case Some(id) ⇒ id
                    case None ⇒
                        try {
                            userLock.acquire()

                            NCSqlManager.getUserId(companyId, extUsrId, span) match {
                                case Some(id) ⇒ id
                                case None ⇒
                                    val id = usersSeq.incrementAndGet()

                                    NCSqlManager.addUser(
                                        id,
                                        companyId,
                                        extId = Some(extUsrId),
                                        email = None,
                                        firstName = None,
                                        lastName = None,
                                        avatarUrl = None,
                                        passwdSalt = None,
                                        isAdmin = false,
                                        propsOpt = None,
                                        parent = parent
                                    )

                                    id
                            }
                        }
                        finally
                            userLock.release()
                }
            }
        }

    /**
      * Gets technical user ID by given external ID.
      *
      * @param companyId Company ID.
      * @param extUserId External user ID.
      * @param parent Parent.
      */
    @throws[NCE]
    def getUserId(companyId: Long, extUserId: String, parent: Span = null): Option[Long] =
        startScopedSpan("getUser", parent, "companyId" → companyId, "extUserId" → extUserId) { span ⇒
            NCSql.sql {
                NCSqlManager.getUserId(companyId, extUserId, span)
            }
        }
}
