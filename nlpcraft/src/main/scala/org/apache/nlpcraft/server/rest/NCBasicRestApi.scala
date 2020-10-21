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

import akka.http.scaladsl.coding.Coders
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server._
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
import org.apache.nlpcraft.server.sugsyn.NCSuggestSynonymManager
import org.apache.nlpcraft.server.user.NCUserManager
import spray.json.DefaultJsonProtocol._
import spray.json.{JsObject, JsValue, RootJsonFormat}

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
    case class AccessTokenFailure(acsTkn: String) extends NCE(s"Unknown or expired access token: $acsTkn")
    case class SignInFailure(email: String) extends NCE(s"Invalid or unknown user email: $email")
    case class AdminRequired(email: String) extends NCE(s"Admin privileges required: $email")
    case class InvalidOperation(email: String) extends NCE(s"Invalid operation.")
    case class NotImplemented() extends NCE("Not implemented.")

    class InvalidArguments(msg: String) extends NCE(msg)
    case class OutOfRangeField(fn: String, from: Number, to: Number) extends InvalidArguments(s"Parameter ($fn) value is out of range ($from, $to).")
    case class TooLargeField(fn: String, max: Int) extends InvalidArguments(s"Parameter ($fn) value exceeded max length of $max.")
    case class InvalidField(fn: String) extends InvalidArguments(s"Parameter invalid: $fn")
    case class EmptyField(fn: String) extends InvalidArguments(s"Parameter cannot be empty: $fn")
    case class InvalidExternalUserId(usrExtId: String) extends InvalidArguments(s"External user ID is invalid or unknown: $usrExtId")
    case class InvalidUserId(id: Long) extends InvalidArguments(s"User ID is invalid or unknown: $id")
    case class InvalidModelId(id: String) extends InvalidArguments(s"Unknown model ID: $id")

    case class AskReqHolder(
        usrId: Long,
        txt: String,
        mdlId: String,
        usrAgent: Option[String],
        rmtAddr: Option[String],
        data: Option[String],
        enabledLog: Boolean,
        parent: Span
    )

    /*
     * Maximum length specification for frequently used standard REST parameters,
     */
    private final val STD_FIELD_LENGTHS = Map[String, Int](
        "srvReqId" → 32,
        "mdlId" → 32,
        "country" → 32,
        "postalCode" → 32,

        "usrExtId" → 64,
        "name" → 64,
        "firstName" → 64,
        "lastName" → 64,
        "passwd" → 64,
        "newPasswd" → 64,
        "email" → 64,
        "adminEmail" → 64,
        "adminPasswd" → 64,
        "adminFirstName" → 64,
        "adminLastName" → 64,

        "website" → 256,
        "acsTok" → 256,

        "city" → 512,
        "region" → 512,
        "address" → 512,

        "txt" → 1024,
        "comment" → 1024,

        "avatarUrl" → 512000,
        "adminAvatarUrl" → 512000
    )

    /**
      *
      * @param acsTkn Access token to check.
      * @param shouldBeAdmin Admin flag.
      * @return
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
      * @param acsUsr
      * @param srvReqIdsOpt
      * @param usrIdOpt
      * @param usrExtIdOpt
      * @param span
      */
    @throws[AdminRequired]
    private def getRequests(
        acsUsr: NCUserMdo,
        srvReqIdsOpt: Option[Set[String]],
        usrIdOpt: Option[Long],
        usrExtIdOpt: Option[String],
        span: Span
    ): Set[NCQueryStateMdo] = {
        require(acsUsr.email.isDefined)

        val userId = getUserId(acsUsr, usrIdOpt, usrExtIdOpt)

        val states = srvReqIdsOpt match {
            case Some(srvReqIds) ⇒
                val states = NCQueryManager.getForServerRequestIds(srvReqIds, span)

                if (usrIdOpt.isDefined || usrExtIdOpt.isDefined) states.filter(_.userId == userId) else states
            case None ⇒ NCQueryManager.getForUserId(userId, span)
        }

        if (states.exists(_.companyId != acsUsr.companyId) || !acsUsr.isAdmin && states.exists(_.userId != acsUsr.id))
            throw AdminRequired(acsUsr.email.get)

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
      * @param acsUsr Currently signed in user.
      * @param usrIdOpt User ID. Optional.
      * @param usrExtIdOpt User 'on-behalf-of' external ID. Optional.
      */
    @throws[AdminRequired]
    @throws[InvalidUserId]
    @throws[InvalidExternalUserId]
    protected def getUserId(
        acsUsr: NCUserMdo,
        usrIdOpt: Option[Long],
        usrExtIdOpt: Option[String]
    ): Long = {
        require(acsUsr.email.isDefined)

        val id1Opt = usrIdOpt match {
            case Some(userId) ⇒
                if (!acsUsr.isAdmin && userId != acsUsr.id)
                    throw AdminRequired(acsUsr.email.get)

                val usr = NCUserManager.getUserById(userId).getOrElse(throw InvalidUserId(userId))

                if (usr.companyId != acsUsr.companyId)
                    throw InvalidUserId(userId)

                Some(userId)

            case None ⇒ None
        }

        val id2Opt = usrExtIdOpt match {
            case Some(usrExtId) ⇒
                if (!acsUsr.isAdmin)
                    throw AdminRequired(acsUsr.email.get)

                Some(NCUserManager.getOrInsertExternalUserId(acsUsr.companyId, usrExtId))

            case None ⇒ None
        }

        if (id1Opt.isDefined && id2Opt.isDefined && id1Opt.get != id2Opt.get)
            throw new InvalidArguments("User ID and external user ID are inconsistent.")

        id1Opt.getOrElse(id2Opt.getOrElse(acsUsr.id))
    }

    /**
      *
      * @param acsTkn Access token to check.
      */
    @throws[NCE]
    protected def authenticate(acsTkn: String): NCUserMdo = authenticate0(acsTkn, shouldBeAdmin = false)

    /**
      *
      * @param acsTkn Access token to check.
      */
    @throws[NCE]
    protected def authenticateAsAdmin(acsTkn: String): NCUserMdo = authenticate0(acsTkn, shouldBeAdmin = true)

    /**
      *
      * @param mdlId
      * @param compId
      */
    @throws[InvalidField]
    protected def checkModelId(mdlId: String, compId: Long): Unit =
        if (!NCProbeManager.existsForModel(compId, mdlId))
            throw InvalidModelId(mdlId)

    /**
      * Checks length of field value.
      *
      * @param name Field name.
      * @param v Field value.
      * @param maxLen Optional maximum length. If not specified - the standard range for 'name' will be used.
      */
    @throws[TooLargeField]
    @throws[EmptyField]
    protected def checkLength(name: String, v: String, maxLen: Int = -1): Unit = {
        val max: Int =
            if (maxLen == -1)
                STD_FIELD_LENGTHS.
                    getOrElse(name, throw new AssertionError(s"Unknown standard REST field: $name"))
            else
                maxLen

        if (v.length > max)
            throw TooLargeField(name, max)
        else if (v.length < 1)
            throw EmptyField(name)
    }

    /**
      * Checks length of field value.
      *
      * @param name Field name.
      * @param v Field value.
      * @param maxLen Maximum length.
      */
    @throws[TooLargeField]
    @throws[EmptyField]
    def checkLengthOpt(name: String, v: Option[_], maxLen: Int = -1): Unit =
        if (v.isDefined)
            checkLength(name, v.get.toString, maxLen)

    /**
      *
      * @param pairs
      */
    @throws[TooLargeField]
    @throws[EmptyField]
    protected def checkLength(pairs: (String, Any)*): Unit =
        pairs.foreach {
            case (name, value: Option[_]) ⇒ checkLengthOpt(name, value)
            case (name, value: String) ⇒ checkLength(name, value)

            case _ ⇒ assert(false)
        }

    /**
      * Checks numeric range of field value.
      *
      * @param name Field name.
      * @param v Field value.
      * @param from Minimum from.
      * @param to Maximum to.
      */
    @throws[OutOfRangeField]
    protected def checkRange[T](name: String, v: T, from: T, to: T)(implicit e: T ⇒ Number): Unit = {
        val d = v.doubleValue()

        if (d < from.doubleValue() || d > to.doubleValue())
            throw OutOfRangeField(name, from, to)
    }

    /**
      * Checks numeric range of field value.
      *
      * @param name Field name.
      * @param vOpt Field value. Optional.
      * @param from Minimum from.
      * @param to Maximum to.
      */
    @throws[OutOfRangeField]
    protected def checkRangeOpt[T](name: String, vOpt: Option[T], from: T, to: T)(implicit e: T ⇒ Number): Unit =
        if (vOpt.isDefined)
            checkRange(name, vOpt.get, from, to)

    /**
      *
      * @param js
      * @param fn
      */
    @throws[InvalidField]
    protected def convert[T](js: JsObject, fn: String, extractor: JsValue ⇒ T): T =
        try
            extractor(js.fields(fn))
        catch {
            case _: Exception ⇒ throw InvalidField(fn)
        }

    /**
      *
      * @param js
      * @param fn
      * @param extractor
      * @tparam T
      * @return
      */
    @throws[InvalidField]
    protected def convertOpt[T](js: JsObject, fn: String, extractor: JsValue ⇒ T): Option[T] =
        try
            js.fields.get(fn) match {
                case Some(v) ⇒ Some(extractor(v))
                case None ⇒ None
            }
        catch {
            case _: Exception ⇒ throw InvalidField(fn)
        }

    /**
      *
      * @return
      */
    protected def signin$(): Route = {
        case class Req$Signin$(
            email: String,
            passwd: String
        )
        case class Res$Signin$(
            status: String,
            acsTok: String
        )
        implicit val reqFmt: RootJsonFormat[Req$Signin$] = jsonFormat2(Req$Signin$)
        implicit val resFmt: RootJsonFormat[Res$Signin$] = jsonFormat2(Res$Signin$)

        // NOTE: no authentication requires on signin.
        entity(as[Req$Signin$]) { req ⇒
            startScopedSpan("signin$", "email" → req.email) { span ⇒
                checkLength("email" → req.email, "passwd" → req.passwd)

                NCUserManager.signin(
                    req.email,
                    req.passwd,
                    span
                ) match {
                    case None ⇒ throw SignInFailure(req.email) // Email is unknown (user hasn't signed up).
                    case Some(acsTkn) ⇒ complete {
                        Res$Signin$(API_OK, acsTkn)
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
        case class Res$Health$(status: String)

        implicit val resFmt: RootJsonFormat[Res$Health$] = jsonFormat1(Res$Health$)

        complete {
            Res$Health$(API_OK)
        }
    }

    /**
      *
      * @return
      */
    protected def signout$(): Route = {
        case class Req$Signout$(
            acsTok: String
        )
        case class Res$Signout$(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Signout$] = jsonFormat1(Req$Signout$)
        implicit val resFmt: RootJsonFormat[Res$Signout$] = jsonFormat1(Res$Signout$)

        entity(as[Req$Signout$]) { req ⇒
            startScopedSpan("signout$", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok" → req.acsTok)

                val usr = authenticate(req.acsTok)

                for (mdlId ← NCProbeManager.getAllProbes(usr.companyId, span).flatMap(_.models.map(_.id))) {
                    NCProbeManager.clearConversation(usr.id, mdlId, span)
                    NCProbeManager.clearDialog(usr.id, mdlId, span)
                }

                NCUserManager.signout(req.acsTok, span)

                complete {
                    Res$Signout$(API_OK)
                }
            }
        }
    }

    /**
      *
      * @param fut
      */
    private def successWithJs(fut: Future[String]): Route = onSuccess(fut) {
        js ⇒ complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, js)))
    }

    /**
      *
      * @param e
      */
    private def onError(e: Throwable): Unit = {
        U.prettyError(logger,s"Unexpected system error.", e)

        completeError(StatusCodes.InternalServerError, "NC_ERROR", e.getLocalizedMessage)
    }

    /**
      *
      * @return
      */
    private def ask0(process: AskReqHolder ⇒ Route): Route = {
        case class Req$Ask$(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            txt: String,
            mdlId: String,
            data: Option[spray.json.JsValue],
            enableLog: Option[Boolean]
        )

        implicit val reqFmt: RootJsonFormat[Req$Ask$] = jsonFormat7(Req$Ask$)

        entity(as[Req$Ask$]) { req ⇒
            startScopedSpan(
                "ask$",
                "usrId" → req.usrId.getOrElse(-1),
                "usrExtId" → req.usrExtId.orNull,
                "acsTok" → req.acsTok,
                "txt" → req.txt,
                "mdlId" → req.mdlId) { span ⇒
                checkLength(
                    "acsTok" → req.acsTok, "usrExtId" → req.usrExtId, "mdlId" → req.mdlId, "txt" → req.txt
                )

                val dataJsOpt =
                    req.data match {
                        case Some(data) ⇒ Some(data.compactPrint)
                        case None ⇒ None
                    }

                checkLengthOpt("data", dataJsOpt, 512000)

                val acsUsr = authenticate(req.acsTok)

                checkModelId(req.mdlId, acsUsr.companyId)

                optionalHeaderValueByName("User-Agent") { usrAgent ⇒
                    extractClientIP { rmtAddr ⇒
                        process(AskReqHolder(
                            usrId = getUserId(acsUsr, req.usrId, req.usrExtId),
                            txt = req.txt,
                            mdlId = req.mdlId,
                            usrAgent = usrAgent,
                            rmtAddr = getAddress(rmtAddr),
                            data = dataJsOpt,
                            req.enableLog.getOrElse(false),
                            parent = span
                        ))
                    }
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def ask$(): Route =
        ask0(
            (h: AskReqHolder) ⇒ {
                val newSrvReqId = NCQueryManager.asyncAsk(
                    h.usrId,
                    h.txt,
                    h.mdlId,
                    h.usrAgent,
                    h.rmtAddr,
                    h.data,
                    h.enabledLog,
                    h.parent
                )

                case class Res$Ask$(
                    status: String,
                    srvReqId: String
                )

                implicit val resFmt: RootJsonFormat[Res$Ask$] = jsonFormat2(Res$Ask$)

                complete {
                    Res$Ask$(API_OK, newSrvReqId)
                }
            }
        )

    /**
      *
      * @return
      */
    protected def ask$Sync(): Route =
        ask0(
            (h: AskReqHolder) ⇒ {
                val fut = NCQueryManager.futureAsk(
                    h.usrId,
                    h.txt,
                    h.mdlId,
                    h.usrAgent,
                    h.rmtAddr,
                    h.data,
                    h.enabledLog,
                    h.parent
                )

                fut.failed.collect {
                    case e ⇒ onError(e)
                }

                successWithJs(
                    fut.collect {
                        // We have to use GSON (not spray) here to serialize 'resBody' field.
                        case res ⇒ GSON.toJson(
                            Map(
                                "status" → API_OK.toString,
                                "state" → queryStateToMap(res)
                            ).asJava
                        )
                    }
                )
            }
        )

    /**
      *
      * @return
      */
    protected def cancel$(): Route = {
        case class Req$Cancel$(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            srvReqIds: Option[Set[String]]
        )
        case class Res$Cancel$(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Cancel$] = jsonFormat4(Req$Cancel$)
        implicit val resFmt: RootJsonFormat[Res$Cancel$] = jsonFormat1(Res$Cancel$)

        entity(as[Req$Cancel$]) { req ⇒
            startScopedSpan("cancel$",
                "acsTok" → req.acsTok,
                "usrId" → req.usrId.getOrElse(-1),
                "usrExtId" → req.usrExtId.orNull,
                "srvReqIds" → req.srvReqIds.getOrElse(Nil).mkString(",")) { span ⇒
                checkLength("acsTok" → req.acsTok, "usrExtId" → req.usrExtId)

                val acsUsr = authenticate(req.acsTok)

                val srvReqs = getRequests(acsUsr, req.srvReqIds, req.usrId, req.usrExtId, span)

                NCQueryManager.cancelForServerRequestIds(srvReqs.map(_.srvReqId), span)

                complete {
                    Res$Cancel$(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def check$(): Route = {
        case class Req$Check$(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            srvReqIds: Option[Set[String]],
            maxRows: Option[Int]
        )

        implicit val reqFmt: RootJsonFormat[Req$Check$] = jsonFormat5(Req$Check$)

        entity(as[Req$Check$]) { req ⇒
            startScopedSpan(
                "check$",
                "usrId" → req.usrId.getOrElse(-1),
                "usrExtId" → req.usrExtId.orNull,
                "acsTok" → req.acsTok,
                "srvReqIds" → req.srvReqIds.getOrElse(Nil).mkString(",")
            ) { span ⇒
                checkLength("acsTok" → req.acsTok, "usrExtId" → req.usrExtId)
                checkRangeOpt("maxRows", req.maxRows, 1, java.lang.Integer.MAX_VALUE)

                val acsUsr = authenticate(req.acsTok)

                val states =
                    getRequests(acsUsr, req.srvReqIds, req.usrId, req.usrExtId, span).
                        toSeq.sortBy(-_.createTstamp.getTime).
                        take(req.maxRows.getOrElse(Integer.MAX_VALUE))

                // We have to use GSON (not spray) here to serialize 'resBody' field.
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
    protected def sugsyn$(): Route = {
        case class Req$Model$Sugsyn$(
            acsTok: String,
            mdlId: String,
            minScore: Option[Double]
        )

        implicit val reqFmt: RootJsonFormat[Req$Model$Sugsyn$] = jsonFormat3(Req$Model$Sugsyn$)

        entity(as[Req$Model$Sugsyn$]) { req ⇒
            startScopedSpan(
                "model$sugsyn",
                "acsTok" → req.acsTok,
                "mdlId" → req.mdlId,
                "minScore" → req.minScore.getOrElse(-1)) { span ⇒
                checkLength("acsTok" → req.acsTok, "mdlId" → req.mdlId)
                checkRangeOpt("minScore", req.minScore, 0.0, 1.0)

                val admUsr = authenticateAsAdmin(req.acsTok)

                checkModelId(req.mdlId, admUsr.companyId)

                val fut = NCSuggestSynonymManager.suggest(req.mdlId, req.minScore, span)

                fut.failed.collect {
                    case e ⇒ onError(e)
                }

                successWithJs(
                    fut.collect {
                        // We have to use GSON (not spray) here to serialize 'result' field.
                        case res ⇒ GSON.toJson(Map("status" → API_OK.toString, "result" → res).asJava)
                    }
                )
            }
        }
    }

    /**
      *
      * @return
      */
    protected def clear$Conversation(): Route = {
        case class Req$Clear$Conversation(
            acsTok: String,
            mdlId: String,
            usrId: Option[Long],
            usrExtId: Option[String]
        )
        case class Res$Clear$Conversation(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Clear$Conversation] = jsonFormat4(Req$Clear$Conversation)
        implicit val resFmt: RootJsonFormat[Res$Clear$Conversation] = jsonFormat1(Res$Clear$Conversation)

        entity(as[Req$Clear$Conversation]) { req ⇒
            startScopedSpan(
                "clear$Conversation",
                "acsTok" → req.acsTok,
                "mdlId" → req.mdlId,
                "usrExtId" → req.usrExtId.orNull,
                "usrId" → req.usrId.getOrElse(-1)) { span ⇒
                checkLength("acsTok" → req.acsTok, "mdlId" → req.mdlId, "usrExtId" → req.usrExtId)

                val acsUsr = authenticate(req.acsTok)

                checkModelId(req.mdlId, acsUsr.companyId)

                NCProbeManager.clearConversation(getUserId(acsUsr, req.usrId, req.usrExtId), req.mdlId, span)

                complete {
                    Res$Clear$Conversation(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def clear$Dialog(): Route = {
        case class Req$Clear$Dialog(
            acsTok: String,
            mdlId: String,
            usrId: Option[Long],
            usrExtId: Option[String]
        )
        case class Res$Clear$Dialog(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Clear$Dialog] = jsonFormat4(Req$Clear$Dialog)
        implicit val resFmt: RootJsonFormat[Res$Clear$Dialog] = jsonFormat1(Res$Clear$Dialog)

        entity(as[Req$Clear$Dialog]) { req ⇒
            startScopedSpan(
                "clear$Dialog",
                "acsTok" → req.acsTok,
                "usrExtId" → req.usrExtId.orNull,
                "mdlId" → req.mdlId,
                "usrId" → req.usrId.getOrElse(-1)) { span ⇒
                checkLength("acsTok" → req.acsTok, "mdlId" → req.mdlId, "usrExtId" → req.usrExtId)

                val acsUsr = authenticate(req.acsTok)

                checkModelId(req.mdlId, acsUsr.companyId)

                NCProbeManager.clearDialog(getUserId(acsUsr, req.usrId, req.usrExtId), req.mdlId, span)

                complete {
                    Res$Clear$Dialog(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def company$Add(): Route = {
        case class Req$Company$Add(
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
        case class Res$Company$Add(
            status: String,
            token: String,
            adminId: Long
        )

        implicit val reqFmt: RootJsonFormat[Req$Company$Add] = jsonFormat13(Req$Company$Add)
        implicit val resFmt: RootJsonFormat[Res$Company$Add] = jsonFormat3(Res$Company$Add)

        //noinspection DuplicatedCod
        entity(as[Req$Company$Add]) { req ⇒
            startScopedSpan("company$Add", "name" → req.name) { span ⇒
                checkLength(
                    "acsTok" → req.acsTok,
                    "name" → req.name,
                    "website" → req.website,
                    "country" → req.country,
                    "region" → req.region,
                    "city" → req.city,
                    "address" → req.address,
                    "postalCode" → req.postalCode,
                    "adminEmail" → req.adminEmail,
                    "adminPasswd" → req.adminPasswd,
                    "adminFirstName" → req.adminFirstName,
                    "adminLastName" → req.adminLastName,
                    "adminAvatarUrl" → req.adminAvatarUrl
                )

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
                    Res$Company$Add(API_OK, res.token, res.adminId)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def company$Get(): Route = {
        case class Req$Company$Get(
            acsTok: String
        )
        case class Res$Company$Get(
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

        implicit val reqFmt: RootJsonFormat[Req$Company$Get] = jsonFormat1(Req$Company$Get)
        implicit val resFmt: RootJsonFormat[Res$Company$Get] = jsonFormat9(Res$Company$Get)

        entity(as[Req$Company$Get]) { req ⇒
            startScopedSpan("company$get", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok" → req.acsTok)

                val acsUsr = authenticate(req.acsTok)

                val company = NCCompanyManager.
                    getCompany(acsUsr.companyId, span).
                    getOrElse(throw new NCE(s"Company not found: ${acsUsr.companyId}"))

                complete {
                    Res$Company$Get(API_OK,
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
        case class Req$Company$Update(
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
        case class Res$Company$Update(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Company$Update] = jsonFormat8(Req$Company$Update)
        implicit val resFmt: RootJsonFormat[Res$Company$Update] = jsonFormat1(Res$Company$Update)

        entity(as[Req$Company$Update]) { req ⇒
            startScopedSpan("company$Update", "acsTok" → req.acsTok, "name" → req.name) { span ⇒
                checkLength("acsTok" → req.acsTok,
                    "name" → req.name,
                    "website" → req.website,
                    "country" → req.country,
                    "region" → req.region,
                    "city" → req.city,
                    "address" → req.address,
                    "postalCode" → req.postalCode
                )

                val admUsr = authenticateAsAdmin(req.acsTok)

                NCCompanyManager.updateCompany(
                    admUsr.companyId,
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
                    Res$Company$Update(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def feedback$Add(): Route = {
        case class Req$Feedback$Add(
            acsTok: String,
            usrId : Option[Long],
            usrExtId: Option[String],
            srvReqId: String,
            score: Double,
            comment: Option[String]
        )
        case class Res$Feedback$Add(
            status: String,
            id: Long
        )

        implicit val reqFmt: RootJsonFormat[Req$Feedback$Add] = jsonFormat6(Req$Feedback$Add)
        implicit val resFmt: RootJsonFormat[Res$Feedback$Add] = jsonFormat2(Res$Feedback$Add)

        entity(as[Req$Feedback$Add]) { req ⇒
            startScopedSpan(
                "feedback$Add",
                "usrId" → req.usrId.getOrElse(-1),
                "usrExtId" → req.usrExtId.orNull,
                "srvReqId" → req.srvReqId) { span ⇒
                checkLength(
                    "acsTok" → req.acsTok,
                    "usrExtId" → req.usrExtId,
                    "srvReqId" → req.srvReqId,
                    "comment" → req.comment
                )
                checkRange("score", req.score, 0, 1)

                // Via REST only administrators of already created companies can create new companies.
                val acsUsr = authenticate(req.acsTok)

                val id = NCFeedbackManager.addFeedback(
                    req.srvReqId,
                    getUserId(acsUsr, req.usrId, req.usrExtId),
                    req.score,
                    req.comment,
                    span
                )

                complete {
                    Res$Feedback$Add(API_OK, id)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def feedback$Delete(): Route = {
        case class Req$Feedback$Delete(
            acsTok: String,
            // Feedback IDs to delete (optional).
            id: Option[Long]
        )
        case class Res$Feedback$Delete(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Feedback$Delete] = jsonFormat2(Req$Feedback$Delete)
        implicit val resFmt: RootJsonFormat[Res$Feedback$Delete] = jsonFormat1(Res$Feedback$Delete)

        entity(as[Req$Feedback$Delete]) { req ⇒
            startScopedSpan("feedback$Delete") { span ⇒
                checkLength("acsTok" → req.acsTok)

                // Via REST only administrators of already created companies can create new companies.
                val acsUsr = authenticate(req.acsTok)

                require(acsUsr.email.isDefined)

                req.id match {
                    case Some(id) ⇒
                        NCFeedbackManager.getFeedback(id, span) match {
                            case Some(f) ⇒
                                val companyId =
                                    NCUserManager.
                                        getUserById(f.userId, span).
                                        getOrElse(throw new NCE(s"Company not found for user: ${f.userId}")).
                                        companyId

                                if (companyId != acsUsr.companyId || (f.userId != acsUsr.id && !acsUsr.isAdmin))
                                    throw AdminRequired(acsUsr.email.get)

                                NCFeedbackManager.deleteFeedback(f.id, span)
                            case None ⇒ // No-op.
                        }

                    case None ⇒
                        if (!acsUsr.isAdmin)
                            throw AdminRequired(acsUsr.email.get)

                        NCFeedbackManager.deleteAllFeedback(acsUsr.companyId, span)
                }

                complete {
                    Res$Feedback$Delete(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def feedback$All(): Route = {
        case class Req$Feedback$All(
            acsTok: String,
            usrId: Option[Long],
            usrExtId: Option[String],
            srvReqId: Option[String]
        )
        case class Feedback_Feedback$All(
            id: Long,
            srvReqId: String,
            usrId: Long,
            score: Double,
            comment: Option[String],
            createTstamp: Long
        )
        case class Res$Feedback$All(
            status: String,
            feedback: Seq[Feedback_Feedback$All]
        )

        implicit val reqFmt: RootJsonFormat[Req$Feedback$All] = jsonFormat4(Req$Feedback$All)
        implicit val fbFmt: RootJsonFormat[Feedback_Feedback$All] = jsonFormat6(Feedback_Feedback$All)
        implicit val resFmt: RootJsonFormat[Res$Feedback$All] = jsonFormat2(Res$Feedback$All)

        entity(as[Req$Feedback$All]) { req ⇒
            startScopedSpan(
                "feedback$All",
                "usrId" → req.usrId.getOrElse(-1),
                "usrExtId" → req.usrExtId.orNull
            ) { span ⇒
                checkLength("acsTok" → req.acsTok, "srvReqId" → req.srvReqId, "usrExtId" → req.usrExtId)

                val acsUsr = authenticate(req.acsTok)

                require(acsUsr.email.isDefined)

                val feedback =
                    NCFeedbackManager.getFeedback(
                        acsUsr.companyId,
                        req.srvReqId,
                        if (req.usrId.isDefined || req.usrExtId.isDefined)
                            Some(getUserId(acsUsr, req.usrId, req.usrExtId))
                        else
                            None,
                        span
                    ).map(f ⇒
                        Feedback_Feedback$All(
                            f.id,
                            f.srvReqId,
                            f.userId,
                            f.score,
                            f.comment,
                            f.createdOn.getTime
                        )
                    )

                if (!acsUsr.isAdmin && feedback.exists(_.usrId != acsUsr.id))
                    throw AdminRequired(acsUsr.email.get)

                complete {
                    Res$Feedback$All(API_OK, feedback)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def company$Token$Reset(): Route = {
        case class Req$Company$Token$Reset(
            // Caller.
            acsTok: String
        )
        case class Res$Company$Token$Reset(
            status: String,
            token: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Company$Token$Reset] = jsonFormat1(Req$Company$Token$Reset)
        implicit val resFmt: RootJsonFormat[Res$Company$Token$Reset] = jsonFormat2(Res$Company$Token$Reset)

        entity(as[Req$Company$Token$Reset]) { req ⇒
            startScopedSpan("company$Token$Reset", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok" → req.acsTok)

                val admUsr = authenticateAsAdmin(req.acsTok)

                val tkn = NCCompanyManager.resetToken(admUsr.companyId, span)

                complete {
                    Res$Company$Token$Reset(API_OK, tkn)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def company$Delete(): Route = {
        case class Req$Company$Delete(
            // Caller.
            acsTok: String
        )
        case class Res$Company$Delete(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$Company$Delete] = jsonFormat1(Req$Company$Delete)
        implicit val resFmt: RootJsonFormat[Res$Company$Delete] = jsonFormat1(Res$Company$Delete)

        entity(as[Req$Company$Delete]) { req ⇒
            startScopedSpan("company$Delete", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok" → req.acsTok)

                val admUSr = authenticateAsAdmin(req.acsTok)

                NCCompanyManager.deleteCompany(admUSr.companyId, span)

                complete {
                    Res$Company$Delete(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Add(): Route = {
        case class Req$User$Add(
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
            usrExtId: Option[String]
        )
        case class Res$User$Add(
            status: String,
            id: Long
        )

        implicit val reqFmt: RootJsonFormat[Req$User$Add] = jsonFormat9(Req$User$Add)
        implicit val resFmt: RootJsonFormat[Res$User$Add] = jsonFormat2(Res$User$Add)

        entity(as[Req$User$Add]) { req ⇒
            startScopedSpan("user$Add", "acsTok" → req.acsTok, "email" → req.email) { span ⇒
                checkLength(
                    "acsTok" → req.acsTok,
                    "email" → req.email,
                    "passwd" → req.passwd,
                    "firstName" → req.firstName,
                    "lastName" → req.lastName,
                    "avatarUrl" → req.avatarUrl,
                    "usrExtId" → req.usrExtId
                )

                checkUserProperties(req.properties)

                val admUsr = authenticateAsAdmin(req.acsTok)

                val id = NCUserManager.addUser(
                    admUsr.companyId,
                    req.email,
                    req.passwd,
                    req.firstName,
                    req.lastName,
                    req.avatarUrl,
                    req.isAdmin,
                    req.properties,
                    req.usrExtId,
                    span
                )

                complete {
                    Res$User$Add(API_OK, id)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Update(): Route = {
        case class Req$User$Update(
            // Caller.
            acsTok: String,

            // Update user.
            id: Option[Long],
            firstName: String,
            lastName: String,
            avatarUrl: Option[String],
            properties: Option[Map[String, String]]
        )
        case class Res$User$Update(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$User$Update] = jsonFormat6(Req$User$Update)
        implicit val resFmt: RootJsonFormat[Res$User$Update] = jsonFormat1(Res$User$Update)

        entity(as[Req$User$Update]) { req ⇒
            startScopedSpan("user$Update", "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(-1)) { span ⇒
                checkLength(
                    "acsTok" → req.acsTok,
                    "firstName" → req.firstName,
                    "lastName" → req.lastName,
                    "avatarUrl" → req.avatarUrl
                )

                checkUserProperties(req.properties)

                val acsUsr = authenticate(req.acsTok)

                NCUserManager.updateUser(
                    getUserId(acsUsr, req.id, None),
                    req.firstName,
                    req.lastName,
                    req.avatarUrl,
                    req.properties,
                    span
                )

                complete {
                    Res$User$Update(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Delete(): Route = {
        case class Req$User$Delete(
            acsTok: String,
            id: Option[Long],
            usrExtId: Option[String]
        )
        case class Res$User$Delete(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$User$Delete] = jsonFormat3(Req$User$Delete)
        implicit val resFmt: RootJsonFormat[Res$User$Delete] = jsonFormat1(Res$User$Delete)

        entity(as[Req$User$Delete]) { req ⇒
            startScopedSpan(
                "user$Delete", "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(-1)
            ) { span ⇒
                checkLength("acsTok" → req.acsTok, "usrExtId" → req.usrExtId)

                val acsUsr = authenticate(req.acsTok)

                require(acsUsr.email.isDefined)

                def delete(id: Long): Unit = {
                    NCUserManager.signoutAllSessions(id, span)
                    NCUserManager.deleteUser(id, span)

                    logger.info(s"User deleted: $id")
                }

                // Deletes all users from company except initiator.
                if (req.id.isEmpty && req.usrExtId.isEmpty) {
                    if (!acsUsr.isAdmin)
                        throw AdminRequired(acsUsr.email.get)

                    NCUserManager.
                        getAllUsers(acsUsr.companyId, span).
                        keys.
                        filter(_.id != acsUsr.id).
                        map(_.id).
                        foreach(delete)
                }
                else {
                    val delUsrId = getUserId(acsUsr, req.id, req.usrExtId)

                    // Tries to delete own account.
                    if (delUsrId == acsUsr.id &&
                        acsUsr.isAdmin &&
                        !NCUserManager.isOtherAdminsExist(acsUsr.id)
                    )
                        throw InvalidOperation(s"Last admin user cannot be deleted: ${acsUsr.email.get}")

                    delete(delUsrId)
                }

                complete {
                    Res$User$Delete(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Admin(): Route = {
        case class Req$User$Admin(
            acsTok: String,
            id: Option[Long],
            admin: Boolean
        )
        case class Res$User$Admin(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$User$Admin] = jsonFormat3(Req$User$Admin)
        implicit val resFmt: RootJsonFormat[Res$User$Admin] = jsonFormat1(Res$User$Admin)

        entity(as[Req$User$Admin]) { req ⇒
            startScopedSpan(
                "user$Admin", "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(-1), "admin" → req.admin
            ) { span ⇒
                checkLength("acsTok" → req.acsTok)

                val initUsr = authenticateAsAdmin(req.acsTok)
                val usrId = req.id.getOrElse(initUsr.id)

                // Self update.
                if (
                    usrId == initUsr.id &&
                        !req.admin &&
                        !NCUserManager.isOtherAdminsExist(initUsr.id, span)
                )
                    throw InvalidOperation(s"Last admin user cannot lose admin privileges: ${initUsr.email}")

                NCUserManager.updateUserPermissions(usrId, req.admin, span)

                complete {
                    Res$User$Admin(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Password$Reset(): Route = {
        case class Req$User$Password$Reset(
            // Caller.
            acsTok: String,
            id: Option[Long],
            newPasswd: String
        )
        case class Res$User$Password$Reset(
            status: String
        )

        implicit val reqFmt: RootJsonFormat[Req$User$Password$Reset] = jsonFormat3(Req$User$Password$Reset)
        implicit val resFmt: RootJsonFormat[Res$User$Password$Reset] = jsonFormat1(Res$User$Password$Reset)

        entity(as[Req$User$Password$Reset]) { req ⇒
            startScopedSpan(
                "user$Password$Reset",
                "acsTok" → req.acsTok, "usrId" → req.id.getOrElse(-1)) { span ⇒
                checkLength("acsTok" → req.acsTok, "newPasswd" → req.newPasswd)

                val acsUsr = authenticate(req.acsTok)

                NCUserManager.resetPassword(getUserId(acsUsr, req.id, None), req.newPasswd, span)

                complete {
                    Res$User$Password$Reset(API_OK)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$All(): Route = {
        case class Req$User$All(
            // Caller.
            acsTok: String
        )
        case class ResUser_User$All(
            id: Long,
            email: Option[String],
            usrExtId: Option[String],
            firstName: Option[String],
            lastName: Option[String],
            avatarUrl: Option[String],
            isAdmin: Boolean,
            companyId: Long,
            properties: Option[Map[String, String]]
        )
        case class Res$User$All(
            status: String,
            users: Seq[ResUser_User$All]
        )

        implicit val reqFmt: RootJsonFormat[Req$User$All] = jsonFormat1(Req$User$All)
        implicit val usrFmt: RootJsonFormat[ResUser_User$All] = jsonFormat9(ResUser_User$All)
        implicit val resFmt: RootJsonFormat[Res$User$All] = jsonFormat2(Res$User$All)

        entity(as[Req$User$All]) { req ⇒
            startScopedSpan("user$All", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok" → req.acsTok)

                val admUSr = authenticateAsAdmin(req.acsTok)

                val usrLst =
                    NCUserManager.getAllUsers(admUSr.companyId, span).map { case (u, props) ⇒
                        ResUser_User$All(
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
                    Res$User$All(API_OK, usrLst)
                }
            }
        }
    }

    /**
      *
      * @return
      */
    protected def user$Get(): Route = {
        case class Req$User$Get(
            // Caller.
            acsTok: String,
            id: Option[Long],
            usrExtId: Option[String]
        )
        case class Res$User$Get(
            status: String,
            id: Long,
            email: Option[String],
            usrExtId: Option[String],
            firstName: Option[String],
            lastName: Option[String],
            avatarUrl: Option[String],
            isAdmin: Boolean,
            properties: Option[Map[String, String]]
        )

        implicit val reqFmt: RootJsonFormat[Req$User$Get] = jsonFormat3(Req$User$Get)
        implicit val resFmt: RootJsonFormat[Res$User$Get] = jsonFormat9(Res$User$Get)

        entity(as[Req$User$Get]) { req ⇒
            startScopedSpan(
                "user$Get", "acsTok" → req.acsTok, "id" → req.id.orElse(null), "usrExtId" → req.usrExtId.orNull
            ) { span ⇒
                checkLength("acsTok" → req.acsTok, "usrExtId" → req.usrExtId)

                val acsUsr = authenticate(req.acsTok)
                val usrId = getUserId(acsUsr, req.id, req.usrExtId)

                if (acsUsr.id != usrId && !acsUsr.isAdmin)
                    throw AdminRequired(acsUsr.email.get)

                val usr = NCUserManager.getUserById(usrId, span).getOrElse(throw new NCE(s"User not found: $usrId"))
                val props = NCUserManager.getUserProperties(usrId, span)

                complete {
                    Res$User$Get(API_OK,
                        usr.id,
                        usr.email,
                        usr.extId,
                        usr.firstName,
                        usr.lastName,
                        usr.avatarUrl,
                        usr.isAdmin,
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
        case class Req$Probe$All(
            acsTok: String
        )
        case class Model_Probe$All(
            id: String,
            name: String,
            version: String,
            enabledBuiltInTokens: Set[String]
        )
        case class Probe_Probe$All(
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
            models: Set[Model_Probe$All]
        )
        case class Res$Probe$All(
            status: String,
            probes: Seq[Probe_Probe$All]
        )

        implicit val reqFmt: RootJsonFormat[Req$Probe$All] = jsonFormat1(Req$Probe$All)
        implicit val mdlFmt: RootJsonFormat[Model_Probe$All] = jsonFormat4(Model_Probe$All)
        implicit val probFmt: RootJsonFormat[Probe_Probe$All] = jsonFormat19(Probe_Probe$All)
        implicit val resFmt: RootJsonFormat[Res$Probe$All] = jsonFormat2(Res$Probe$All)

        entity(as[Req$Probe$All]) { req ⇒
            startScopedSpan("probe$All", "acsTok" → req.acsTok) { span ⇒
                checkLength("acsTok" → req.acsTok)

                val admUsr = authenticateAsAdmin(req.acsTok)

                val probeLst = NCProbeManager.getAllProbes(admUsr.companyId, span).map(mdo ⇒ Probe_Probe$All(
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
                    mdo.models.map(m ⇒ Model_Probe$All(
                        m.id,
                        m.name,
                        m.version,
                        m.enabledBuiltInTokens
                    ))
                ))

                complete {
                    Res$Probe$All(API_OK, probeLst)
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
    def getExceptionHandler: ExceptionHandler = ExceptionHandler {
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
            val errMsg = "Unexpected system error."
            val code = "NC_ERROR"

            // We have to log error reason because even general exceptions are not expected here.
            U.prettyError(logger,s"Unexpected top level REST API error: $errMsg", e)

            completeError(StatusCodes.BadRequest, code, errMsg)

        // Unexpected errors.
        case e: Throwable ⇒
            val errMsg = "Unexpected system error."
            val code = "NC_ERROR"

            U.prettyError(logger,s"Unexpected system error: $errMsg", e)

            completeError(StatusCodes.InternalServerError, code, errMsg)
    }

    /**
      *
      * @return
      */
    def getRejectionHandler: RejectionHandler = {
        def complete(err: String): Route = completeError(StatusCodes.BadRequest, "NC_ERROR", err)

        RejectionHandler.newBuilder().
            handle {
                case _: UnsupportedRequestContentTypeRejection ⇒ complete("Unsupported content type.")
                case _: SchemeRejection ⇒ complete("Unsupported scheme.")
                case _: UnsatisfiableRangeRejection ⇒ complete("Invalid range.")
                case _: TransformationRejection ⇒ complete("Transformation error.")
                case _: MalformedRequestContentRejection ⇒ complete("Malformed or missing content.")
                case _: AuthenticationFailedRejection ⇒ complete("Authentication error.")
                case _: TooManyRangesRejection ⇒ complete("Too many ranges.")
                case err: MissingFormFieldRejection ⇒ complete(s"Missing form field: ${err.fieldName}")
                case _: UnacceptedResponseContentTypeRejection ⇒ complete(s"Unsupported response content type.")
                case _: MethodRejection ⇒ complete(s"Invalid method.")
                case _: UnacceptedResponseEncodingRejection ⇒ complete(s"Unsupported response encoding.")
                case _: UnsupportedRequestEncodingRejection ⇒ complete(s"Unsupported request encoding.")
                case err: MissingCookieRejection ⇒ complete(s"Missing cookie: ${err.cookieName}'")
                case err: InvalidRequiredValueForQueryParamRejection ⇒ complete(s"Invalid parameter value: ${err.parameterName}")
                case _: UnsupportedWebSocketSubprotocolRejection ⇒ complete(s"Unsupported web socket sub-protocol.")
                case _: CircuitBreakerOpenRejection ⇒ complete(s"Circuit breaker error.")
                case _: MissingHeaderRejection ⇒ complete(s"Missing header.")
                case err: MissingQueryParamRejection ⇒ complete(s"Missing parameter: ${err.parameterName}")
                case _: InvalidOriginRejection ⇒ complete(s"Invalid origin.")

                // Common case.
                case _: Rejection ⇒ complete(s"Bad request.")
            }.result
    }

    /**
      *
      * @param m
      * @param f
      * @return
      */
    private def withMetric(m: Measure, f: () ⇒ Route): Route = {
        val start = System.currentTimeMillis()

        try
            f()
        finally {
            recordStats(m → (System.currentTimeMillis() - start))
        }
    }

    /**
      *
      * @return
      */
    override def getRoute: Route = {
        val timeoutResp = HttpResponse(
            StatusCodes.EnhanceYourCalm,
            entity = "Unable to serve response within time limit."
        )

        handleExceptions(getExceptionHandler) {
            handleRejections(getRejectionHandler) {
                corsHandler (
                    get {
                        withRequestTimeoutResponse(_ ⇒ timeoutResp) {
                            path(API / "health") { health$() } // Also duplicated for POST.
                        }
                    } ~
                        post {
                            encodeResponseWith(Coders.NoCoding, Coders.Gzip) {
                                withRequestTimeoutResponse(_ ⇒ timeoutResp) {
                                    path(API / "health") { health$() } // Duplicate for POST.
                                    path(API / "signin") { withMetric(M_SIGNIN_LATENCY_MS, signin$) } ~
                                        path(API / "signout") { withMetric(M_SIGNOUT_LATENCY_MS, signout$) } ~
                                        path(API / "cancel") { withMetric(M_CANCEL_LATENCY_MS, cancel$) } ~
                                        path(API / "check") { withMetric(M_CHECK_LATENCY_MS, check$) } ~
                                        path(API / "clear"/ "conversation") { withMetric(M_CLEAR_CONV_LATENCY_MS, clear$Conversation) } ~
                                        path(API / "clear"/ "dialog") { withMetric(M_CLEAR_DIALOG_LATENCY_MS, clear$Dialog) } ~
                                        path(API / "company"/ "add") { withMetric(M_COMPANY_ADD_LATENCY_MS, company$Add) } ~
                                        path(API / "company"/ "get") { withMetric(M_COMPANY_GET_LATENCY_MS, company$Get) } ~
                                        path(API / "company" / "update") { withMetric(M_COMPANY_UPDATE_LATENCY_MS, company$Update) } ~
                                        path(API / "company" / "token" / "reset") { withMetric(M_COMPANY_TOKEN_LATENCY_MS, company$Token$Reset) } ~
                                        path(API / "company" / "delete") { withMetric(M_COMPANY_DELETE_LATENCY_MS, company$Delete) } ~
                                        path(API / "user" / "get") { withMetric(M_USER_GET_LATENCY_MS, user$Get) } ~
                                        path(API / "user" / "add") { withMetric(M_USER_ADD_LATENCY_MS, user$Add) } ~
                                        path(API / "user" / "update") { withMetric(M_USER_UPDATE_LATENCY_MS, user$Update) } ~
                                        path(API / "user" / "delete") { withMetric(M_USER_DELETE_LATENCY_MS, user$Delete) } ~
                                        path(API / "user" / "admin") { withMetric(M_USER_ADMIN_LATENCY_MS, user$Admin) } ~
                                        path(API / "user" / "passwd" / "reset") { withMetric(M_USER_PASSWD_RESET_LATENCY_MS, user$Password$Reset) } ~
                                        path(API / "user" / "all") { withMetric(M_USER_ALL_LATENCY_MS, user$All) } ~
                                        path(API / "feedback"/ "add") { withMetric(M_FEEDBACK_ADD_LATENCY_MS, feedback$Add) } ~
                                        path(API / "feedback"/ "all") { withMetric(M_FEEDBACK_GET_LATENCY_MS, feedback$All) } ~
                                        path(API / "feedback" / "delete") { withMetric(M_FEEDBACK_DELETE_LATENCY_MS, feedback$Delete) } ~
                                        path(API / "probe" / "all") { withMetric(M_PROBE_ALL_LATENCY_MS, probe$All) } ~
                                        path(API / "model" / "sugsyn") { withMetric(M_MODEL_SUGSYN_LATENCY_MS, sugsyn$) } ~
                                        path(API / "ask") { withMetric(M_ASK_LATENCY_MS, ask$) } ~
                                        path(API / "ask" / "sync") { withMetric(M_ASK_SYNC_LATENCY_MS, ask$Sync) }
                                }
                            }
                        }
                )
            }
        }
    }
}
