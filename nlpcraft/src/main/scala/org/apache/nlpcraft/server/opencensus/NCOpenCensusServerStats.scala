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

package org.apache.nlpcraft.server.opencensus

import io.opencensus.stats.Measure._
import io.opencensus.stats._
import org.apache.nlpcraft.common.opencensus.NCOpenCensusStats

/**
  * OpenCensus stats instrumentation.
  */
trait NCOpenCensusServerStats extends NCOpenCensusStats {
    val M_HEALTH_MS: MeasureLong = MeasureLong.create("health_latency", "The latency of '/health' REST call", "ms")
    val M_ASK_LATENCY_MS: MeasureLong = MeasureLong.create("ask_latency", "The latency of '/ask' REST call", "ms")
    val M_CHECK_LATENCY_MS: MeasureLong = MeasureLong.create("check_latency", "The latency of '/check' REST call", "ms")
    val M_MODEL_SUGSYN_LATENCY_MS: MeasureLong = MeasureLong.create("model_inspect_latency", "The latency of '/model/sugsyn' REST call", "ms")
    val M_MODEL_SYNS_LATENCY_MS: MeasureLong = MeasureLong.create("model_synonyms_latency", "The latency of '/model/syns' REST call", "ms")
    val M_CANCEL_LATENCY_MS: MeasureLong = MeasureLong.create("cancel_latency", "The latency of '/cancel' REST call", "ms")
    val M_SIGNIN_LATENCY_MS: MeasureLong = MeasureLong.create("signin_latency", "The latency of '/signin' REST call", "ms")
    val M_SIGNOUT_LATENCY_MS: MeasureLong = MeasureLong.create("signout_latency", "The latency of '/signout' REST call", "ms")
    val M_ASK_SYNC_LATENCY_MS: MeasureLong = MeasureLong.create("ask_sync_latency", "The latency of '/ask/sync' REST call", "ms")
    val M_CLEAR_CONV_LATENCY_MS: MeasureLong = MeasureLong.create("clear_conv_latency", "The latency of '/clear/conversation' REST call", "ms")
    val M_CLEAR_DIALOG_LATENCY_MS: MeasureLong = MeasureLong.create("clear_dialog_latency", "The latency of '/clear/dialog' REST call", "ms")
    val M_COMPANY_ADD_LATENCY_MS: MeasureLong = MeasureLong.create("company_add_latency", "The latency of '/company/add' REST call", "ms")
    val M_COMPANY_GET_LATENCY_MS: MeasureLong = MeasureLong.create("company_get_latency", "The latency of '/company/get' REST call", "ms")
    val M_COMPANY_UPDATE_LATENCY_MS: MeasureLong = MeasureLong.create("company_update_latency", "The latency of '/company/update' REST call", "ms")
    val M_COMPANY_TOKEN_LATENCY_MS: MeasureLong = MeasureLong.create("company_token_latency", "The latency of '/company/token' REST call", "ms")
    val M_COMPANY_DELETE_LATENCY_MS: MeasureLong = MeasureLong.create("company_delete_latency", "The latency of '/company/delete' REST call", "ms")
    val M_FEEDBACK_ADD_LATENCY_MS: MeasureLong = MeasureLong.create("feedback_add_latency", "The latency of '/feedback/add' REST call", "ms")
    val M_FEEDBACK_GET_LATENCY_MS: MeasureLong = MeasureLong.create("feedback_get_latency", "The latency of '/feedback/get' REST call", "ms")
    val M_FEEDBACK_DELETE_LATENCY_MS: MeasureLong = MeasureLong.create("feedback_delete_latency", "The latency of '/feedback/delete' REST call", "ms")
    val M_USER_ADD_LATENCY_MS: MeasureLong = MeasureLong.create("user_add_latency", "The latency of '/user/add' REST call", "ms")
    val M_USER_GET_LATENCY_MS: MeasureLong = MeasureLong.create("user_get_latency", "The latency of '/user/get' REST call", "ms")
    val M_USER_DELETE_LATENCY_MS: MeasureLong = MeasureLong.create("user_delete_latency", "The latency of '/user/delete' REST call", "ms")
    val M_USER_UPDATE_LATENCY_MS: MeasureLong = MeasureLong.create("user_update_latency", "The latency of '/user/update' REST call", "ms")
    val M_USER_ADMIN_LATENCY_MS: MeasureLong = MeasureLong.create("user_admin_latency", "The latency of '/user/admin' REST call", "ms")
    val M_USER_PASSWD_RESET_LATENCY_MS: MeasureLong = MeasureLong.create("user_passwd_reset_latency", "The latency of '/user/passwd/reset' REST call", "ms")
    val M_USER_ALL_LATENCY_MS: MeasureLong = MeasureLong.create("user_all_latency", "The latency of '/user/all' REST call", "ms")
    val M_PROBE_ALL_LATENCY_MS: MeasureLong = MeasureLong.create("probe_all_latency", "The latency of '/probe/all' REST call", "ms")
    val M_ROUND_TRIP_LATENCY_MS: MeasureLong = MeasureLong.create("round_trip_latency", "The latency of a full server<->probe round trip", "ms")

    init()
    
    /**
      *
      */
    private def init(): Unit = {
        val views = List(
            mkViews(M_HEALTH_MS, "health"),
            mkViews(M_ASK_LATENCY_MS, "ask"),
            mkViews(M_ASK_SYNC_LATENCY_MS, "ask/sync"),
            mkViews(M_CANCEL_LATENCY_MS, "cancel"),
            mkViews(M_CHECK_LATENCY_MS, "check"),
            mkViews(M_SIGNIN_LATENCY_MS, "signin"),
            mkViews(M_SIGNOUT_LATENCY_MS, "signout"),
            mkViews(M_CLEAR_CONV_LATENCY_MS, "clear/conversation"),
            mkViews(M_CLEAR_DIALOG_LATENCY_MS, "clear/dialog"),
            mkViews(M_COMPANY_ADD_LATENCY_MS, "company/add"),
            mkViews(M_COMPANY_GET_LATENCY_MS, "company/get"),
            mkViews(M_COMPANY_UPDATE_LATENCY_MS, "company/update"),
            mkViews(M_COMPANY_TOKEN_LATENCY_MS, "company/token"),
            mkViews(M_COMPANY_DELETE_LATENCY_MS, "company/delete"),
            mkViews(M_MODEL_SUGSYN_LATENCY_MS, "model/sugsyn"),
            mkViews(M_MODEL_SYNS_LATENCY_MS, "model/syns"),
            mkViews(M_USER_ADD_LATENCY_MS, "user/add"),
            mkViews(M_USER_GET_LATENCY_MS, "user/get"),
            mkViews(M_USER_DELETE_LATENCY_MS, "user/delete"),
            mkViews(M_USER_UPDATE_LATENCY_MS, "user/update"),
            mkViews(M_USER_ADMIN_LATENCY_MS, "user/admin"),
            mkViews(M_USER_PASSWD_RESET_LATENCY_MS, "user/passwd/reset"),
            mkViews(M_USER_ALL_LATENCY_MS, "user/all"),
            mkViews(M_FEEDBACK_ADD_LATENCY_MS, "feedback/add"),
            mkViews(M_FEEDBACK_DELETE_LATENCY_MS, "feedback/delete"),
            mkViews(M_FEEDBACK_GET_LATENCY_MS, "feedback/get"),
            mkViews(M_PROBE_ALL_LATENCY_MS, "probe/all"),
            mkViews(M_ROUND_TRIP_LATENCY_MS, "roundTrip/latdist"),
        ).flatten
        
        val viewMgr = Stats.getViewManager
        
        // Add all views.
        for (view <- views)
            viewMgr.registerView(view)
    }
}
