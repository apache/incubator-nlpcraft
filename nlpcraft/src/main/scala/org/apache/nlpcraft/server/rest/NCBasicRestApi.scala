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

package org.apache.nlpcraft.server.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import com.google.gson.Gson
import com.typesafe.scalalogging.LazyLogging
import io.opencensus.stats.Measure
import io.opencensus.trace.{Span, Status}
import org.apache.commons.validator.routines.UrlValidator
import org.apache.nlpcraft.common.opencensus.NCOpenCensusTrace
import org.apache.nlpcraft.common.{NCE, NCException, U}
import org.apache.nlpcraft.server.apicodes.NCApiStatusCode.{API_OK, _}
import org.apache.nlpcraft.server.company.NCCompanyManager
import org.apache.nlpcraft.server.feedback.NCFeedbackManager
import org.apache.nlpcraft.server.mdo.{NCQueryStateMdo, NCUserMdo}
import org.apache.nlpcraft.server.opencensus.NCOpenCensusServerStats
import org.apache.nlpcraft.server.probe.NCProbeManager
import org.apache.nlpcraft.server.query.NCQueryManager
import org.apache.nlpcraft.server.user.NCUserManager
import spray.json.DefaultJsonProtocol._
import spray.json.{JsValue, RootJsonFormat}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * REST API default implementation.
  */
class NCBasicRestApi extends NCRestApi with LazyLogging with NCOpenCensusTrace with NCOpenCensusServerStats {
    protected final val GSON = new Gson()
    protected final val URL_VALIDATOR = new UrlValidator(Array("http", "https"), UrlValidator.ALLOW_LOCAL_URLS)
    
    final val API_VER = 1
    final val API = "api" / s"v$API_VER"
    
    /** */
    private final val CORS_HDRS = List(
        `Access-Control-Allow-Origin`.*,
        `Access-Control-Allow-Credentials`(true),
        `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With")
    )
    
    /*
     * General control exception.
     * Note that these classes must be public because scala 2.11 internal errors (compilations problems).
     */
    case class AccessTokenFailure(acsTkn: String) extends NCE(s"Unknown access token: $acsTkn")
    case class SignInFailure(email: String) extends NCE(s"Invalid or unknown user credentials for user with: $email")
    case class AdminRequired(email: String) extends NCE(s"Admin privileges required for user with: $email")
    case class InvalidOperation(email: String) extends NCE(s"Invalid operation.")
    case class NotImplemented() extends NCE("Not implemented.")

    class InvalidArguments(msg: String) extends NCE(msg)
    case class OutOfRangeField(fn: String, from: Double, to: Double) extends InvalidArguments(s"API field '$fn' value is out of range ($from, $to).")
    case class TooLargeField(fn: String, max: Int) extends InvalidArguments(s"API field '$fn' value exceeded max length of $max.")
    case class InvalidField(fn: String) extends InvalidArguments(s"API invalid field '$fn'")
    case class EmptyField(fn: String) extends InvalidArguments(s"API field '$fn' value cannot be empty.")
    case class InvalidExternalUserId(extId: String) extends InvalidArguments(s"External user IS is invalid or unknown: $extId")
    case class InvalidUserId(id: Long) extends InvalidArguments(s"User ID is invalid or unknown: $id")

    /**
      *
      * @param acsTkn Access token to check.
      * @param shouldBeAdmin Admin flag.
      */
    @throws[NCE]
    private def authenticate0(acsTkn: String, shouldBeAdmin: Boolean): NCUserMdo =
        startScopedSpan("authenticate0", "acsTkn" → acsTkn, "shouldBeAdmin" → shouldBeAdmin) { span ⇒
            NCUserManager.getUserForAccessToken(acsTkn, span) match {
                case None ⇒ throw AccessTokenFailure(acsTkn)
                case Some(usr) ⇒
                    require(usr.email.isDefined)

                    if (shouldBeAdmin && !usr.isAdmin)
                        throw AdminRequired(usr.email.get)

                    usr
            }
        }

    /**
      *
      * @param rmtAddr
      * @return
      */
    private def getAddress(rmtAddr: RemoteAddress): Option[String] =
        rmtAddr.toOption match {
            // 127.0.0.1 used to avoid local addresses like 0:0:0:0:0:0:0:1 (IPv6)
            case Some(a) ⇒ Some(if (a.getHostName == "localhost") "127.0.0.1" else a.getHostAddress)
            case None ⇒ None
        }

    /**
      *
      * @param connUser
      * @param srvReqIdsOpt
      * @param userIdOpt
      * @param userExtIdOpt
      * @param span
      */
    @throws[AdminRequired]
    private def getRequests(
        connUser: NCUserMdo,
        srvReqIdsOpt: Option[Set[String]],
        userIdOpt: Option[Long],
        userExtIdOpt: Option[String],
        span: Span
    ): Set[NCQueryStateMdo] = {
        require(connUser.email.isDefined)

        val userId = getUserId(connUser, userIdOpt, userExtIdOpt)

        val states = srvReqIdsOpt match {
            case Some(srvReqIds) ⇒
                val states = NCQueryManager.getForServerRequestIds(srvReqIds, span)

                if (userIdOpt.isDefined || userExtIdOpt.isDefined) states.filter(_.userId == userId) else states
            case None ⇒ NCQueryManager.getForUserId(userId, span)
        }

        if (states.exists(_.companyId != connUser.companyId) || !connUser.isAdmin && states.exists(_.userId != connUser.id))
            throw AdminRequired(connUser.email.get)

        states
    }

    /**
      *
      * @param s  Query state MDO to convert to map.
      * @return
      */
    private def queryStateToMap(s: NCQueryStateMdo): java.util.Map[String, Any] =
        Map(
            "srvReqId" → s.srvReqId,
            "txt" → s.text,
            "usrId" → s.userId,
            "mdlId" → s.modelId,
            "probeId" → s.probeId.orNull,
            "status" → s.status,
            "resType" → s.resultType.orNull,
            "resBody" → (
                if (s.resultBody.isDefined &&
                    s.resultType.isDefined &&
                    s.resultType.get == "json"
                )
                    U.js2Obj(s.resultBody.get)
                else
                    s.resultBody.orNull
                ),
            "error" → s.error.orNull,
            "errorCode" → s.errorCode.map(Integer.valueOf).orNull,
            "logHolder" → (if (s.logJson.isDefined) U.js2Obj(s.logJson.get) else null),
            "intentId" → s.intentId.orNull
        ).filter(_._2 != null).asJava

    /**
      * Checks properties.
      *
      * @param propsOpt Optional properties.
      */
    @throws[TooLargeField]
    private def checkUserProperties(propsOpt: Option[Map[String, String]]): Unit =
        propsOpt match {
            case Some(props) ⇒
                props.foreach { case (k, v) ⇒
                    checkLength(k, k, 64)

                    if (v != null && v.nonEmpty && v.length > 512)
                        throw TooLargeField(v, 512)
                }
            case None ⇒ // No-op.
        }

    /**
      *
      * @param r Route to CORS enable.
      */
    protected def corsHandler(r: Route): Route = respondWithHeaders(CORS_HDRS) {
        options {
            complete(HttpResponse(StatusCodes.OK).
                withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
        } ~ r
    }

    /**
      * Checks operation permissions and gets user ID.
      *
      * @param curUsr Currently signed in user.
      * @param usrIdOpt User ID. Optional.
      * @param usrExtIdOpt User 'on-behalf-of' external ID. Optional.
      */
    @throws[AdminRequired]
    @throws[InvalidUserId]
    @throws[InvalidExternalUserId]
    protected def getUserId(
        curUsr: NCUserMdo,
        usrIdOpt: Option[Long],
        usrExtIdOpt: Option[String]
    ): Long = {
        require(curUsr.email.isDefined)

        val id1Opt = usrIdOpt match {
            case Some(userId) ⇒
                if (!curUsr.isAdmin && userId != curUsr.id)
                    throw AdminRequired(curUsr.email.get)

                val usr = NCUserManager.getUserById(userId).getOrElse(throw InvalidUserId(userId))

                if (usr.companyId != curUsr.companyId)
                    throw InvalidUserId(userId)

                Some(userId)
                
            case None ⇒ None
        }

        val id2Opt = usrExtIdOpt match {
            case Some(extId) ⇒
                if (!curUsr.isAdmin)
                    throw AdminRequired(curUsr.email.get)

                Some(NCUserManager.getOrInsertExternalUserId(curUsr.companyId, extId))

            case None ⇒ None
        }

        if (id1Opt.isDefined && id2Opt.isDefined && id1Opt.get != id2Opt.get)
            throw new InvalidArguments("User ID and external user ID are inconsistent.")

        id1Opt.getOrElse(id2Opt.getOrElse(curUsr.id))
    }

    /**
      *
      * @param acsTkn Access token to check.
      */
    @throws[NCE]
    protected def authenticate(acsTkn: String): NCUserMdo = authenticate0(acsTkn, false)

    /**
      *
      * @param acsTkn Access token to check.
      */
    @throws[NCE]
    protected def authenticateAsAdmin(acsTkn: String): NCUserMdo = authenticate0(acsTkn, true)

    /**
      * Checks length of field value.
      *
      * @param name Field name.
      * @param v Field value.
      * @param maxLen Maximum length.
      */
    @throws[TooLargeField]
    protected def checkLength(name: String, v: String, maxLen: Int): Unit =
        if (v.length > maxLen)
            throw TooLargeField(name, maxLen)
        else if (v.length < 1)
            throw EmptyField(name)

    /**
      * Checks range of field value.
      *
      * @param name Field name.
      * @param v Field value.
      * @param from Minimum from.
      * @param to Maximum to.
      */
    @throws[TooLargeField]
    protected def checkRange(name: String, v: Double, from: Double, to: Double): Unit =
        if (v < from || v > to)
            throw OutOfRangeField(name, from, to)

    /**
      * Checks range of field value.
      *
      * @param name Field name.
      * @param v Field value.
      * @param from Minimum from.
      * @param to Maximum to.
      */
    @throws[TooLargeField]
    protected def checkRangeOpt(name: String, v: Option[Double], from: Double, to: Double): Unit =
        if (v.isDefined)
            checkRange(name, v.get, from, to)

    /**
      * Checks length of field value.
      *
      * @param name Field name.
      * @param v Field value.
      * @param maxLen Maximum length.
      */
    @throws[TooLargeField]
    protected def checkLengthOpt(name: String, v: Option[String], maxLen: Int): Unit =
        if (v.isDefined)
            checkLength(name, v.get, maxLen)

    /**
      *
      * @return
      */
    protected def signin$(): Route = {
        case class Req(
            email: String,
            passwd: String
        )
        case class Res(
            status: String,
            acsTok: String
        )
        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat2(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)

        // NOTE: no authentication requires on signin.
        entity(as[Req]) { req ⇒
            startScopedSpan("signin$", "email" → req.email) { span ⇒
                checkLength("email", req.email, 64)
                checkLength("passwd", req.passwd, 64)
                
                NCUserManager.signin(
                    req.email,
                    req.passwd,
                    span
                ) match {
                    case None ⇒ throw SignInFailure(req.email) // Email is unknown (user hasn't signed up).
                    case Some(acsTkn) ⇒ complete {
                        Res(API_OK, acsTkn)
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     */
    protected def health$(): Route = {
        case class Res(status: String)

        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        complete {
            Res(API_OK)
        }
    }

    /**
      *
      * @return
      */
    protected def signout$(): Route = {
        case class Req(
            acsTok: String
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat1(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)
        
        entity(as[Req]) { req ⇒
            startScopedSpan("signout$", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                
                authenticate(req.acsTok)
    
                NCUserManager.signout(req.acsTok, span)
                
                complete {
                    Res(API_OK)
                }
            }
        }
    }
    
    /**
      *
      * @param reqJs
      * @param usrAgent
      * @param rmtAddr
      * @return
      */
    protected def ask$Sync(reqJs: JsValue, usrAgent: Option[String], rmtAddr: RemoteAddress): Future[String] = {
        val obj = reqJs.asJsObject()

        def getOpt[T](name: String, convert: JsValue ⇒ T): Option[T] =
            obj.fields.get(name) match {
                case Some(v) ⇒ Some(convert(v))
                case None ⇒ None
            }

        val acsTok = obj.fields("acsTok").convertTo[String]
        val txt = obj.fields("txt").convertTo[String]
        val mdlId = obj.fields("mdlId").convertTo[String]
        val data = getOpt("data", (js: JsValue) ⇒ js.compactPrint)
        val enableLog = getOpt("enableLog", (js: JsValue) ⇒ js.convertTo[Boolean])
        val usrExtIdOpt = getOpt("usrExtId", (js: JsValue) ⇒ js.convertTo[String])
        val usrIdOpt = getOpt("usrId", (js: JsValue) ⇒ js.convertTo[Long])

        startScopedSpan(
            "ask$Sync",
            "acsTok" → acsTok,
            "usrId" → usrIdOpt.orElse(null),
            "usrExtId" → usrExtIdOpt.orNull,
            "txt" → txt,
            "mdlId" → mdlId) { span ⇒
            checkLength("acsTok", acsTok, 256)
            checkLength("txt", txt, 1024)
            checkLength("mdlId", mdlId, 32)
            checkLengthOpt("data", data, 512000)
            checkLengthOpt("userExtId", data, 64)
        
            val connUser = authenticate(acsTok)
        
            NCQueryManager.futureAsk(
                getUserId(connUser, usrIdOpt, usrExtIdOpt),
                txt,
                mdlId,
                usrAgent,
                getAddress(rmtAddr),
                data,
                enableLog.getOrElse(false),
                span
            ).collect {
                // We have to use GSON (not spray) here to serialize `resBody` field.
                case res ⇒ GSON.toJson(
                    Map(
                        "status" → API_OK.toString,
                        "state" → queryStateToMap(res)
                    )
                    .asJava
                )
            }
        }
    }

    /**
      *
      * @return
      */
    protected def ask$(): Route = {
        case class Req(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            txt: String,
            mdlId: String,
            data: Option[spray.json.JsValue],
            enableLog: Option[Boolean]
        )
        case class Res(
            status: String,
            srvReqId: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat7(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)
    
        entity(as[Req]) { req ⇒
            //noinspection GetOrElseNull
            startScopedSpan(
                "ask$",
                "usrId" → req.usrId.getOrElse(null),
                "usrExtId" → req.usrExtId.orNull,
                "acsTok" → req.acsTok,
                "txt" → req.txt,
                "mdlId" → req.mdlId) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("userExtId", req.usrExtId, 64)
                checkLength("txt", req.txt, 1024)
                checkLength("mdlId", req.mdlId, 32)
                
                val dataJsOpt =
                    req.data match {
                        case Some(data) ⇒ Some(data.compactPrint)
                        case None ⇒ None
                    }
                
                checkLengthOpt("data", dataJsOpt, 512000)
               
                val connUser = authenticate(req.acsTok)
                
                optionalHeaderValueByName("User-Agent") { usrAgent ⇒
                    extractClientIP { rmtAddr ⇒
                        val newSrvReqId = NCQueryManager.asyncAsk(
                            getUserId(connUser, req.usrId, req.usrExtId),
                            req.txt,
                            req.mdlId,
                            usrAgent,
                            getAddress(rmtAddr),
                            dataJsOpt,
                            req.enableLog.getOrElse(false),
                            span
                        )
                        
                        complete {
                            Res(API_OK, newSrvReqId)
                        }
                    }
                }
            }
        }
    }
    
    /**
      *
      * @return
      */
    protected def cancel$(): Route = {
        case class Req(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            srvReqIds: Option[Set[String]]
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat4(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            //noinspection GetOrElseNull
            startScopedSpan("cancel$",
                "acsTok" → req.acsTok,
                "usrId" → req.usrId.getOrElse(null),
                "usrExtId" → req.usrExtId.orNull,
                "srvReqIds" → req.srvReqIds.getOrElse(Nil).mkString(",")) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("userExtId", req.usrExtId, 64)
    
                val connUser = authenticate(req.acsTok)

                val srvReqs = getRequests(connUser, req.srvReqIds, req.usrId, req.usrExtId, span)

                NCQueryManager.cancelForServerRequestIds(srvReqs.map(_.srvReqId), span)

                complete {
                    Res(API_OK)
                }
            }
        }
    }
    
    /**
      *
      * @return
      */
    protected def check$(): Route = {
        case class Req(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            srvReqIds: Option[Set[String]],
            maxRows: Option[Int]
        )
        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat5(Req)

        entity(as[Req]) { req ⇒
            //noinspection GetOrElseNull
            startScopedSpan(
                "check$",
                "usrId" → req.usrId.getOrElse(null),
                "usrExtId" → req.usrExtId.orNull,
                "acsTok" → req.acsTok,
                "srvReqIds" → req.srvReqIds.getOrElse(Nil).mkString(",")
            ) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("userExtId", req.usrExtId, 64)
    
                val connUser = authenticate(req.acsTok)

                val states =
                    getRequests(connUser, req.srvReqIds, req.usrId, req.usrExtId, span).
                    toSeq.sortBy(-_.createTstamp.getTime).
                    take(req.maxRows.getOrElse(Integer.MAX_VALUE))

                // We have to use GSON (not spray) here to serialize `resBody` field.
                val js = GSON.toJson(
                    Map(
                        "status" → API_OK.toString,
                        "states" → states.map(queryStateToMap).asJava
                    )
                    .asJava
                )
    
                complete(
                    HttpResponse(
                        entity = HttpEntity(ContentTypes.`application/json`, js)
                    )
                )
            }
        }
    }
    
    /**
      *
      * @return
      */
    protected def clear$Conversation(): Route = {
        case class Req(
            acsTok: String,
            mdlId: String,
            usrId: Option[Long],
            usrExtId: Option[String]
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat4(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            //noinspection GetOrElseNull
            startScopedSpan("clear$Conversation",
                "acsTok" → req.acsTok,
                "mdlId" → req.mdlId,
                "usrExtId" → req.usrExtId.orNull,
                "usrId" → req.usrId.getOrElse(null)) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("usrExtId", req.usrExtId, 64)
    
                val connUser = authenticate(req.acsTok)

                NCProbeManager.clearConversation(getUserId(connUser, req.usrId, req.usrExtId), req.mdlId, span)
    
                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def clear$Dialog(): Route = {
        case class Req(
            acsTok: String,
            mdlId: String,
            usrId: Option[Long],
            usrExtId: Option[String]
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat4(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            //noinspection GetOrElseNull
            startScopedSpan("clear$Dialog",
                "acsTok" → req.acsTok,
                "mdlId" → req.mdlId,
                "usrExtId" → req.usrExtId.orNull,
                "usrId" → req.usrId.getOrElse(null)) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("userExtId", req.usrExtId, 64)
    
                val connUser = authenticate(req.acsTok)
    
                NCProbeManager.clearDialog(getUserId(connUser, req.usrId, req.usrExtId), req.mdlId, span)

                complete {
                    Res(API_OK)
                }
            }
        }
    }
    
    /**
      *
      * @return
      */
    protected def company$Add(): Route = {
        case class Req(
            acsTok: String,
            // New company.
            name: String,
            website: Option[String],
            country: Option[String],
            region: Option[String],
            city: Option[String],
            address: Option[String],
            postalCode: Option[String],
            // New company admin.
            adminEmail: String,
            adminPasswd: String,
            adminFirstName: String,
            adminLastName: String,
            adminAvatarUrl: Option[String]
        )
        case class Res(
            status: String,
            token: String,
            adminId: Long
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat13(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat3(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("company$Add", "name" → req.name) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLength("name", req.name, 64)
                checkLengthOpt("website", req.website, 256)
                checkLengthOpt("country", req.country, 32)
                checkLengthOpt("region", req.region, 512)
                checkLengthOpt("city", req.city, 512)
                checkLengthOpt("address", req.address, 512)
                checkLengthOpt("postalCode", req.postalCode, 32)
                checkLength("adminEmail", req.adminEmail, 64)
                checkLength("adminPasswd", req.adminPasswd, 64)
                checkLength("adminFirstName", req.adminFirstName, 64)
                checkLength("adminLastName", req.adminLastName, 64)
                checkLengthOpt("adminAvatarUrl", req.adminAvatarUrl, 512000)
    
                // Via REST only administrators of already created companies can create new companies.
                authenticateAsAdmin(req.acsTok)
    
                val res = NCCompanyManager.addCompany(
                    req.name,
                    req.website,
                    req.country,
                    req.region,
                    req.city,
                    req.address,
                    req.postalCode,
                    req.adminEmail,
                    req.adminPasswd,
                    req.adminFirstName,
                    req.adminLastName,
                    req.adminAvatarUrl,
                    span
                )
    
                complete {
                    Res(API_OK, res.token, res.adminId)
                }
            }
        }
    }
    
    /**
      *
      * @return
      */
    protected def company$Get(): Route = {
        case class Req(
            acsTok: String
        )
        case class Res(
            status: String,
            id: Long,
            name: String,
            website: Option[String],
            country: Option[String],
            region: Option[String],
            city: Option[String],
            address: Option[String],
            postalCode: Option[String]
        )
        
        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat1(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat9(Res)
        
        entity(as[Req]) { req ⇒
            startScopedSpan("company$get", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                
                val connUser = authenticate(req.acsTok)
                
                val company = NCCompanyManager.getCompany(connUser.companyId, span) match {
                    case Some(c) ⇒ c
                    case None ⇒ throw InvalidOperation(s"Failed to find company with ID: ${connUser.companyId}")
                }
                
                complete {
                    Res(API_OK,
                        company.id,
                        company.name,
                        company.website,
                        company.country,
                        company.region,
                        company.city,
                        company.address,
                        company.postalCode
                    )
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def company$Update(): Route = {
        case class Req(
            // Caller.
            acsTok: String,

            // Updated company.
            name: String,
            website: Option[String],
            country: Option[String],
            region: Option[String],
            city: Option[String],
            address: Option[String],
            postalCode: Option[String]
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat8(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("company$Update", "acsTok" → req.acsTok, "name" → req.name) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLength("name", req.name, 64)
                checkLengthOpt("website", req.website, 256)
                checkLengthOpt("country", req.country, 32)
                checkLengthOpt("region", req.region, 512)
                checkLengthOpt("city", req.city, 512)
                checkLengthOpt("address", req.address, 512)
                checkLengthOpt("postalCode", req.postalCode, 32)
    
                val admin = authenticateAsAdmin(req.acsTok)
    
                NCCompanyManager.updateCompany(
                    admin.companyId,
                    req.name,
                    req.website,
                    req.country,
                    req.region,
                    req.city,
                    req.address,
                    req.postalCode,
                    span
                )
    
                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def feedback$Add(): Route = {
        case class Req(
            acsTok: String,
            usrId : Option[Long],
            usrExtId: Option[String],
            srvReqId: String,
            score: Double,
            comment: Option[String]
        )
        case class Res(
            status: String,
            id: Long
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat6(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)

        entity(as[Req]) { req ⇒
            //noinspection GetOrElseNull
            startScopedSpan(
                "feedback$Add",
                "usrId" → req.usrId.getOrElse(null),
                "usrExtId" → req.usrExtId.orNull,
                "srvReqId" → req.srvReqId) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("userExtId", req.usrExtId, 64)
                checkLength("srvReqId", req.srvReqId, 64)
                checkRange("score", req.score, 0, 1)
                checkLengthOpt("comment", req.comment, 1024)

                // Via REST only administrators of already created companies can create new companies.
                val connUser = authenticate(req.acsTok)

                val id = NCFeedbackManager.addFeedback(
                    req.srvReqId,
                    getUserId(connUser, req.usrId, req.usrExtId),
                    req.score,
                    req.comment,
                    span
                )

                complete {
                    Res(API_OK, id)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def feedback$Delete(): Route = {
        case class Req(
            acsTok: String,
            // Deleted feedback ID.
            id: Option[Long]
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat2(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("feedback$Delete") { span ⇒
                checkLength("acsTok", req.acsTok, 256)

                // Via REST only administrators of already created companies can create new companies.
                val connUser = authenticate(req.acsTok)

                require(connUser.email.isDefined)

                req.id match {
                    case Some(id) ⇒
                        NCFeedbackManager.getFeedback(id, span) match {
                            case Some(f) ⇒
                                val companyId =
                                    NCUserManager.
                                        getUserById(f.userId, span).
                                        getOrElse(throw new NCE(s"Company not found for user: ${f.userId}")).
                                        companyId

                                if (companyId != connUser.companyId || (f.userId != connUser.id && !connUser.isAdmin))
                                    throw AdminRequired(connUser.email.get)

                                NCFeedbackManager.deleteFeedback(f.id, span)
                            case None ⇒ // No-op.
                        }

                    case None ⇒
                        if (!connUser.isAdmin)
                            throw AdminRequired(connUser.email.get)

                        NCFeedbackManager.deleteAllFeedback(connUser.companyId, span)
                }

                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def feedback$All(): Route = {
        case class Req(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            srvReqId: Option[String]
        )
        case class Feedback(
            id: Long,
            srvReqId: String,
            usrId: Long,
            score: Double,
            comment: Option[String],
            createTstamp: Long
        )
        case class Res(
            status: String,
            feedback: Seq[Feedback]
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat4(Req)

        implicit val fbFmt: RootJsonFormat[Feedback] = jsonFormat6(Feedback)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)

        entity(as[Req]) { req ⇒
            //noinspection GetOrElseNull
            startScopedSpan(
                "feedback$All",
                "usrId" → req.usrId.getOrElse(null),
                "usrExtId" → req.usrExtId.orNull
            ) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("srvReqId", req.srvReqId, 64)
                checkLengthOpt("userExtId", req.usrExtId, 64)

                val connUser = authenticate(req.acsTok)

                require(connUser.email.isDefined)

                val feedback =
                    NCFeedbackManager.getFeedback(
                        connUser.companyId,
                        req.srvReqId,
                        if (req.usrId.isDefined || req.usrExtId.isDefined)
                            Some(getUserId(connUser, req.usrId, req.usrExtId))
                        else
                            None,
                        span
                    ).map(f ⇒
                        Feedback(
                            f.id,
                            f.srvReqId,
                            f.userId,
                            f.score,
                            f.comment,
                            f.createdOn.getTime
                        )
                    )

                if (!connUser.isAdmin && feedback.exists(_.usrId != connUser.id))
                    throw AdminRequired(connUser.email.get)

                complete {
                    Res(API_OK, feedback)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def company$Token$Reset(): Route = {
        case class Req(
            // Caller.
            acsTok: String
        )
        case class Res(
            status: String,
            token: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat1(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("company$Token$Reset", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok", req.acsTok, 256)

                val admin = authenticateAsAdmin(req.acsTok)
                val tkn = NCCompanyManager.resetToken(admin.companyId, span)

                complete {
                    Res(API_OK, tkn)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def company$Delete(): Route = {
        case class Req(
            // Caller.
            acsTok: String
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat1(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("company$Delete", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok", req.acsTok, 256)

                val admin = authenticateAsAdmin(req.acsTok)

                NCCompanyManager.deleteCompany(admin.companyId, span)

                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Add(): Route = {
        case class Req(
            // Caller.
            acsTok: String,

            // New user.
            email: String,
            passwd: String,
            firstName: String,
            lastName: String,
            avatarUrl: Option[String],
            isAdmin: Boolean,
            properties: Option[Map[String, String]],
            extId: Option[String]
        )
        case class Res(
            status: String,
            id: Long
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat9(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("user$Add", "acsTok" → req.acsTok, "email" → req.email) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLength("email", req.email, 64)
                checkLength("passwd", req.passwd, 64)
                checkLength("firstName", req.firstName, 64)
                checkLength("lastName", req.lastName, 64)
                checkLengthOpt("avatarUrl", req.avatarUrl, 512000)
                checkLengthOpt("extId", req.extId, 64)

                checkUserProperties(req.properties)

                val admin = authenticateAsAdmin(req.acsTok)

                val id = NCUserManager.addUser(
                    admin.companyId,
                    req.email,
                    req.passwd,
                    req.firstName,
                    req.lastName,
                    req.avatarUrl,
                    req.isAdmin,
                    req.properties,
                    req.extId,
                    span
                )

                complete {
                    Res(API_OK, id)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Update(): Route = {
        case class Req(
            // Caller.
            acsTok: String,

            // Update user.
            id: Option[Long],
            firstName: String,
            lastName: String,
            avatarUrl: Option[String],
            properties: Option[Map[String, String]]
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat6(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("user$Update", "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(() ⇒ null)) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLength("firstName", req.firstName, 64)
                checkLength("lastName", req.lastName, 64)
                checkLengthOpt("avatarUrl", req.avatarUrl, 512000)

                checkUserProperties(req.properties)

                val connUser = authenticate(req.acsTok)

                NCUserManager.updateUser(
                    getUserId(connUser, req.id, None),
                    req.firstName,
                    req.lastName,
                    req.avatarUrl,
                    req.properties,
                    span
                )

                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Delete(): Route = {
        case class Req(
            acsTok: String,
            id: Option[Long],
            extId: Option[String]
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat3(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("user$Delete", "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(() ⇒ null)) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("extId", req.extId, 64)

                val connUser = authenticate(req.acsTok)

                require(connUser.email.isDefined)

                def delete(id: Long): Unit = {
                    NCUserManager.signoutAllSessions(id, span)
                    NCUserManager.deleteUser(id, span)

                    logger.info(s"User deleted: $id")
                }

                // Deletes all users from company except initiator.
                if (req.id.isEmpty && req.extId.isEmpty) {
                    if (!connUser.isAdmin)
                        throw AdminRequired(connUser.email.get)

                    NCUserManager.
                        getAllUsers(connUser.companyId, span).
                            keys.
                            filter(_.id != connUser.id).
                            map(_.id).
                            foreach(delete)
                }
                else {
                    val delUsrId = getUserId(connUser, req.id, req.extId)

                    // Tries to delete own account.
                    if (delUsrId == connUser.id &&
                        connUser.isAdmin &&
                        !NCUserManager.isOtherAdminsExist(connUser.id)
                    )
                        throw InvalidOperation(s"Last admin user cannot be deleted: ${connUser.email.get}")

                    delete(delUsrId)
                }

                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Admin(): Route = {
        case class Req(
            acsTok: String,
            id: Option[Long],
            admin: Boolean
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat3(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("user$Admin", "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(-1), "admin" → req.admin) { span ⇒
                checkLength("acsTok", req.acsTok, 256)

                val initiatorUsr = authenticateAsAdmin(req.acsTok)
                val usrId = req.id.getOrElse(initiatorUsr.id)

                // Self update.
                if (
                    usrId == initiatorUsr.id &&
                        !req.admin &&
                        !NCUserManager.isOtherAdminsExist(initiatorUsr.id, span)
                )
                    throw InvalidOperation(s"Last admin user cannot lose admin privileges: ${initiatorUsr.email}")

                NCUserManager.updateUserPermissions(usrId, req.admin, span)

                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Password$Reset(): Route = {
        case class Req(
            // Caller.
            acsTok: String,
            id: Option[Long],
            newPasswd: String
        )
        case class Res(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat3(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat1(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("user$Password$Reset", "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(-1)) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLength("newPasswd", req.newPasswd, 64)

                val connUser = authenticate(req.acsTok)

                NCUserManager.resetPassword(getUserId(connUser, req.id, None), req.newPasswd, span)

                complete {
                    Res(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$All(): Route = {
        case class Req(
            // Caller.
            acsTok: String
        )
        case class ResUser(
            id: Long,
            email: Option[String],
            extId: Option[String],
            firstName: Option[String],
            lastName: Option[String],
            avatarUrl: Option[String],
            isAdmin: Boolean,
            companyId: Long,
            properties: Option[Map[String, String]]
        )
        case class Res(
            status: String,
            users: Seq[ResUser]
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat1(Req)
        implicit val usrFmt: RootJsonFormat[ResUser] = jsonFormat9(ResUser)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("user$All", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok", req.acsTok, 256)

                val admin = authenticateAsAdmin(req.acsTok)

                val usrLst =
                    NCUserManager.getAllUsers(admin.companyId, span).map { case (u, props) ⇒
                        ResUser(
                            u.id,
                            u.email,
                            u.extId,
                            u.firstName,
                            u.lastName,
                            u.avatarUrl,
                            u.isAdmin,
                            u.companyId,
                            if (props.isEmpty) None else Some(props.map(p ⇒ p.property → p.value).toMap)
                        )
                    }.toSeq

                complete {
                    Res(API_OK, usrLst)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Get(): Route = {
        case class Req(
            // Caller.
            acsTok: String,
            id: Option[Long],
            extId: Option[String]
        )
        case class Res(
            status: String,
            id: Long,
            email: Option[String],
            extId: Option[String],
            firstName: Option[String],
            lastName: Option[String],
            avatarUrl: Option[String],
            isAdmin: Boolean,
            properties: Option[Map[String, String]]
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat3(Req)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat9(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan(
                "user$Get", "acsTok" → req.acsTok, "id" → req.id.orElse(null), "extId" → req.extId.orNull
            ) { span ⇒
                checkLength("acsTok", req.acsTok, 256)
                checkLengthOpt("extId", req.extId, 64)

                val connUser = authenticate(req.acsTok)

                val userId = getUserId(connUser, req.id, req.extId)

                if (connUser.id != userId && !connUser.isAdmin)
                    throw AdminRequired(connUser.email.get)

                val user = NCUserManager.getUserById(userId, span).getOrElse(throw new NCE(s"User not found: $userId"))
                val props = NCUserManager.getUserProperties(userId, span)

                complete {
                    Res(API_OK,
                        user.id,
                        user.email,
                        user.extId,
                        user.firstName,
                        user.lastName,
                        user.avatarUrl,
                        user.isAdmin,
                        if (props.isEmpty) None else Some(props.map(p ⇒ p.property → p.value).toMap)
                    )
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def probe$All(): Route = {
        case class Req(
            acsTok: String
        )
        case class Model(
            id: String,
            name: String,
            version: String,
            enabledBuiltInTokens: Set[String]
        )
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
            models: Set[Model]
        )
        case class Res(
            status: String,
            probes: Seq[Probe]
        )

        implicit val reqFmt: RootJsonFormat[Req] = jsonFormat1(Req)
        implicit val mdlFmt: RootJsonFormat[Model] = jsonFormat4(Model)
        implicit val probFmt: RootJsonFormat[Probe] = jsonFormat19(Probe)
        implicit val resFmt: RootJsonFormat[Res] = jsonFormat2(Res)

        entity(as[Req]) { req ⇒
            startScopedSpan("probe$All", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok", req.acsTok, 256)

                val admin = authenticateAsAdmin(req.acsTok)

                val probeLst = NCProbeManager.getAllProbes(admin.companyId, span).map(mdo ⇒ Probe(
                    mdo.probeToken,
                    mdo.probeId,
                    mdo.probeGuid,
                    mdo.probeApiVersion,
                    mdo.probeApiDate.toString,
                    mdo.osVersion,
                    mdo.osName,
                    mdo.osArch,
                    mdo.startTstamp.getTime,
                    mdo.tmzId,
                    mdo.tmzAbbr,
                    mdo.tmzName,
                    mdo.userName,
                    mdo.javaVersion,
                    mdo.javaVendor,
                    mdo.hostName,
                    mdo.hostAddr,
                    mdo.macAddr,
                    mdo.models.map(m ⇒ Model(
                        m.id,
                        m.name,
                        m.version,
                        m.enabledBuiltInTokens
                    ))
                ))

                complete {
                    Res(API_OK, probeLst)
                }
            }
        }
    }

    /**
      *
      * @param statusCode
      * @param errCode
      * @param errMsg
      */
    protected def completeError(statusCode: StatusCode, errCode: String, errMsg: String): Route = {
        currentSpan().setStatus(Status.INTERNAL.withDescription(s"code: $errCode, message: $errMsg"))

        corsHandler(
            complete(
                HttpResponse(
                    status = statusCode,
                    entity = HttpEntity(
                        ContentTypes.`application/json`,
                        GSON.toJson(Map("code" → errCode, "msg" → errMsg).asJava)
                    )
                )
            )
        )
    }

    /**
      *
      * @return
      */
    override def getExceptionHandler: ExceptionHandler = ExceptionHandler {
        case e: AccessTokenFailure ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_INVALID_ACCESS_TOKEN"

            completeError(StatusCodes.Unauthorized, code, errMsg)

        case e: SignInFailure ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_SIGNIN_FAILURE"

            completeError(StatusCodes.Unauthorized, code, errMsg)

        case e: NotImplemented ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_NOT_IMPLEMENTED"

            completeError(StatusCodes.NotImplemented, code, errMsg)

        case e: InvalidArguments ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_INVALID_FIELD"

            completeError(StatusCodes.BadRequest, code, errMsg)

        case e: AdminRequired ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_ADMIN_REQUIRED"

            completeError(StatusCodes.Forbidden, code, errMsg)

        case e: InvalidOperation ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_INVALID_OPERATION"

            completeError(StatusCodes.Forbidden, code, errMsg)

        // General exception.
        case e: NCException ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_ERROR"

            // We have to log error reason because even general exceptions are not expected here.
            logger.warn(s"Unexpected error: $errMsg", e)

            completeError(StatusCodes.BadRequest, code, errMsg)

        // Unexpected errors.
        case e: Throwable ⇒
            val errMsg = e.getLocalizedMessage
            val code = "NC_ERROR"

            logger.error(s"Unexpected system error: $errMsg", e)

            completeError(StatusCodes.InternalServerError, code, errMsg)
    }

    /**
      *
      * @return
      */
    override def getRejectionHandler: RejectionHandler =
        RejectionHandler.newBuilder().
            handle {
                // It doesn't try to process all rejections special way.
                // There is only one reason to wrap rejections - use `cors` support in completeError() method.
                // We assume that all rejection implementations have human readable toString() implementations.
                case err ⇒ completeError(StatusCodes.BadRequest, "NC_ERROR", s"Bad request: $err")
            }.result

    /**
      *
      * @param m
      * @param f
      * @return
      */
    private def withLatency(m: Measure, f: () ⇒ Route): Route = {
        val start = System.currentTimeMillis()

        try
            f()
        finally {
            recordStats(m → (System.currentTimeMillis() - start))
        }
    }

    /**
      *
      * @param m
      * @param f
      * @return
      */
    private def withLatency[T](m: Measure, f: Future[T]): Future[T] = {
        val start = System.currentTimeMillis()

        f.onComplete(_ ⇒ recordStats(m → (System.currentTimeMillis() - start)))

        f
    }

    /**
      *
      * @return
      */
    override def getRoute: Route = {
        val timeoutResp =
            HttpResponse(
                StatusCodes.EnhanceYourCalm,
                entity = "Unable to serve response within time limit, please enhance your calm."
            )

        corsHandler (
            get {
                withRequestTimeoutResponse(_ ⇒ timeoutResp) {
                    path(API / "health") { health$() }
                }
            } ~
            post {
                withRequestTimeoutResponse(_ ⇒ timeoutResp) {
                    path(API / "signin") { withLatency(M_SIGNIN_LATENCY_MS, signin$) } ~
                    path(API / "signout") { withLatency(M_SIGNOUT_LATENCY_MS, signout$) } ~ {
                    path(API / "cancel") { withLatency(M_CANCEL_LATENCY_MS, cancel$) } ~
                    path(API / "check") { withLatency(M_CHECK_LATENCY_MS, check$) } ~
                    path(API / "clear"/ "conversation") { withLatency(M_CLEAR_CONV_LATENCY_MS, clear$Conversation) } ~
                    path(API / "clear"/ "dialog") { withLatency(M_CLEAR_DIALOG_LATENCY_MS, clear$Dialog) } ~
                    path(API / "company"/ "add") { withLatency(M_COMPANY_ADD_LATENCY_MS, company$Add) } ~
                    path(API / "company"/ "get") { withLatency(M_COMPANY_GET_LATENCY_MS, company$Get) } ~
                    path(API / "company" / "update") { withLatency(M_COMPANY_UPDATE_LATENCY_MS, company$Update) } ~
                    path(API / "company" / "token" / "reset") { withLatency(M_COMPANY_TOKEN_LATENCY_MS, company$Token$Reset) } ~
                    path(API / "company" / "delete") { withLatency(M_COMPANY_DELETE_LATENCY_MS, company$Delete) } ~
                    path(API / "user" / "get") { withLatency(M_USER_GET_LATENCY_MS, user$Get) } ~
                    path(API / "user" / "add") { withLatency(M_USER_ADD_LATENCY_MS, user$Add) } ~
                    path(API / "user" / "update") { withLatency(M_USER_UPDATE_LATENCY_MS, user$Update) } ~
                    path(API / "user" / "delete") { withLatency(M_USER_DELETE_LATENCY_MS, user$Delete) } ~
                    path(API / "user" / "admin") { withLatency(M_USER_ADMIN_LATENCY_MS, user$Admin) } ~
                    path(API / "user" / "passwd" / "reset") { withLatency(M_USER_PASSWD_RESET_LATENCY_MS, user$Password$Reset) } ~
                    path(API / "user" / "all") { withLatency(M_USER_ALL_LATENCY_MS, user$All) } ~
                    path(API / "feedback"/ "add") { withLatency(M_FEEDBACK_ADD_LATENCY_MS, feedback$Add) } ~
                    path(API / "feedback"/ "all") { withLatency(M_FEEDBACK_GET_LATENCY_MS, feedback$All) } ~
                    path(API / "feedback" / "delete") { withLatency(M_FEEDBACK_DELETE_LATENCY_MS, feedback$Delete) } ~
                    path(API / "probe" / "all") { withLatency(M_PROBE_ALL_LATENCY_MS, probe$All) } ~
                    path(API / "ask") { withLatency(M_ASK_LATENCY_MS, ask$) } ~
                    (path(API / "ask" / "sync") &
                        entity(as[JsValue]) &
                        optionalHeaderValueByName("User-Agent") &
                        extractClientIP
                    ) {
                        (req, userAgentOpt, rmtAddr) ⇒
                            onSuccess(withLatency(M_ASK_SYNC_LATENCY_MS, ask$Sync(req, userAgentOpt, rmtAddr))) {
                                js ⇒ complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, js)))
                            }
                    }}
                }
            }
        )
    }
}
