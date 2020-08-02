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

package org.apache.nlpcraft.model.tools.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.apache.nlpcraft.probe.embedded.NCEmbeddedProbe;
import org.apache.nlpcraft.probe.embedded.NCEmbeddedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Test client builder for {@link NCTestClient} instances. Note that all configuration values
 * have sensible defaults. Most of the time only user {@link #setUser(String, String) credentials}
 * will have to be changed if not testing with default account.
 */
public class NCTestClientBuilder {
    /** Default public REST API URL (endpoint). */
    public static final String DFLT_BASEURL = "http://localhost:8081/api/v1/";
    /** Default client email. */
    public static final String DFLT_EMAIL = "admin@admin.com";
    /** Default client password. */
    public static final String DFLT_PASSWORD = "admin";
    /** Default embedded mode flag. */
    public static final boolean DFLT_EMBEDDED = false;
    /** Default response log flag. */
    public static final boolean DFLT_RESP_LOG = true;
    /** Maximum test time. */
    public static final long DFLT_MAX_WAIT_TIME = 60000;

    private static final Logger log = LoggerFactory.getLogger(NCTestClientBuilder.class);

    private NCTestClientImpl impl;

    /**
     * Creates new builder instance with all defaults set.
     *
     * @return Builder instance.
     */
    public NCTestClientBuilder newBuilder() {
        impl = new NCTestClientImpl();

        return this;
    }

    /**
     * Sets optional HTTP REST client configuration parameters.
     *
     * @param reqCfg HTTP REST client configuration parameters.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder setRequestConfig(RequestConfig reqCfg) {
        impl.setRequestConfig(reqCfg);

        return this;
    }

    /**
     * Sets non-default {@link CloseableHttpClient} custom supplier.
     * By default {@link CloseableHttpClient} created with {@link HttpClients#createDefault()}.
     *
     * @param cliSup {@link CloseableHttpClient} custom supplier.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder setHttpClientSupplier(Supplier<CloseableHttpClient> cliSup) {
        impl.setClientSupplier(cliSup);

        return this;
    }

    /**
     * Sets non-default API base URL. Only change it if your server is not running on localhost.
     * By default {@link NCTestClientBuilder#DFLT_BASEURL} is used.
     *
     * @param baseUrl API base URL.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder setBaseUrl(String baseUrl) {
        String s = baseUrl;

        if (!s.endsWith("/")) s += '/';

        impl.setBaseUrl(s);

        return this;
    }

    /**
     * Sets non-default user credentials.
     * By default {@link NCTestClientBuilder#DFLT_EMAIL} and {@link NCTestClientBuilder#DFLT_PASSWORD} are used
     * and they match the default NLPCraft server user.
     *
     * @param email User email.
     * @param pswd  User password.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder setUser(String email, String pswd) {
        impl.setEmail(email);
        impl.setPassword(pswd);

        return this;
    }

    /**
     * Sets the embedded probe mode flag. Default value is {@link #DFLT_EMBEDDED}. If set to {@code true} the test client
     * will expect the {@link NCEmbeddedProbe embedded probe} running in the same JVM
     * and will use local callbacks for quicker results instead of a full REST roundtrip.
     *
     * @param embedded Embedded probe mode flag.
     * @return Builder instance for chaining calls.
     * @see NCEmbeddedProbe
     */
    public NCTestClientBuilder setEmbeddedMode(boolean embedded) {
        impl.setEmbedded(embedded);

        return this;
    }

    /**
     * Sets whether or not to log responses from the probe. Default value is {@link #DFLT_RESP_LOG}.
     *
     * @param respLog {@code true} to log responses, {@code false} otherwise.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder setResponseLog(boolean respLog) {
        impl.setResponseLog(respLog);

        return this;
    }

    /**
     * Build new configured test client instance.
     *
     * @return Newly built test client instance.
     */
    public NCTestClient build() {
        checkNotNull("email", impl.getEmail());
        checkNotNull("password", impl.getPassword());
        checkNotNull("baseUrl", impl.getBaseUrl());

        impl.prepareClient();

        return impl;
    }

    /**
     * JSON helper class.
     */
    static class NCRequestStateJson {
        @SerializedName("srvReqId") private String srvReqId;
        @SerializedName("txt") private String text;
        @SerializedName("usrId") private long userId;
        @SerializedName("resType") private String resType;
        @SerializedName("resBody") private Object resBody;
        @SerializedName("status") private String status;
        @SerializedName("error") private String error;
        @SerializedName("createTstamp") private long createTstamp;
        @SerializedName("updateTstamp") private long updateTstamp;
        @SerializedName("intentId") private String intentId;

        /**
         *
         * @return
         */
        public String getServerRequestId() {
            return srvReqId;
        }

        /**
         *
         * @return
         */
        public void setServerRequestId(String srvReqId) {
            this.srvReqId = srvReqId;
        }

        /**
         *
         * @return
         */
        public String getText() {
            return text;
        }

        /**
         *
         * @return
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         *
         * @return
         */
        public long getUserId() {
            return userId;
        }

        /**
         *
         * @return
         */
        public void setUserId(long userId) {
            this.userId = userId;
        }

        /**
         *
         * @return
         */
        public String getStatus() {
            return status;
        }

        /**
         *
         * @param status
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         *
         * @return
         */
        public String getResultType() {
            return resType;
        }

        /**
         *
         * @param resType
         */
        public void setResultType(String resType) {
            this.resType = resType;
        }

        /**
         *
         * @return
         */
        public Object getResultBody() {
            return resBody;
        }

        /**
         *
         * @param resBody
         */
        public void setResultBody(String resBody) {
            this.resBody = resBody;
        }

        /**
         *
         * @return
         */
        public String getError() {
            return error;
        }

        /**
         *
         * @param error
         */
        public void setError(String error) {
            this.error = error;
        }

        /**
         *
         * @return
         */
        public long getCreateTstamp() {
            return createTstamp;
        }

        /**
         *
         * @param createTstamp
         */
        public void setCreateTstamp(long createTstamp) {
            this.createTstamp = createTstamp;
        }

        /**
         *
         * @return
         */
        public long getUpdateTstamp() {
            return updateTstamp;
        }

        /**
         *
         * @param updateTstamp
         */
        public void setUpdateTstamp(long updateTstamp) {
            this.updateTstamp = updateTstamp;
        }

        /**
         *
         * @return
         */
        public String getIntentId() {
            return intentId;
        }

        /**
         *
         * @param intentId
         */
        public void setIntentId(String intentId) {
            this.intentId = intentId;
        }
    }

    /**
     * JSON helper class.
     */
    static class NCRequestResultJson {
        @SerializedName("status") private String status;
        @SerializedName("state") private NCRequestStateJson state;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public NCRequestStateJson getState() {
            return state;
        }

        public void setState(NCRequestStateJson state) {
            this.state = state;
        }
    }

    /**
     * Client implementation.
     */
    private static class NCTestClientImpl implements NCTestClient {
        private static final String STATUS_API_OK = "API_OK";

        private final Type TYPE_RESP = new TypeToken<HashMap<String, Object>>() {}.getType();
        private final Type TYPE_RES = new TypeToken<NCRequestResultJson>() {}.getType();

        private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        private String baseUrl = DFLT_BASEURL;
        private String email = DFLT_EMAIL;
        private String pswd = DFLT_PASSWORD;
        private boolean embedded = DFLT_EMBEDDED;
        private boolean respLog = DFLT_RESP_LOG;

        private CloseableHttpClient httpCli;
        private RequestConfig reqCfg;
        private Supplier<CloseableHttpClient> cliSup;
        private String acsTok;
        private String mdlId;

        private volatile boolean opened = false;
        private volatile boolean closed = false;

        private final Map<String, NCEmbeddedResult> embeddedResMap = new ConcurrentHashMap<>();

        private Consumer<NCEmbeddedResult> embeddedCb;

        private final Object mux = new Object();

        /**
         *
         * @return
         */
        RequestConfig getRequestConfig() {
            return reqCfg;
        }

        /**
         *
         * @param reqCfg
         */
        void setRequestConfig(RequestConfig reqCfg) {
            this.reqCfg = reqCfg;
        }

        /**
         *
         * @return
         */
        String getBaseUrl() {
            return baseUrl;
        }

        /**
         *
         * @param baseUrl
         */
        void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        /**
         *
         * @return
         */
        String getEmail() {
            return email;
        }

        /**
         *
         * @param email
         */
        void setEmail(String email) {
            this.email = email;
        }

        /**
         *
         * @return
         */
        String getPassword() {
            return pswd;
        }

        /**
         *
         * @param pswd
         */
        void setPassword(String pswd) {
            this.pswd = pswd;
        }

        /**
         *
         * @return
         */
        boolean isEmbedded() {
            return embedded;
        }

        /**
         *
         * @param embedded
         */
        void setEmbedded(boolean embedded) {
            this.embedded = embedded;
        }

        /**
         *
         * @return
         */
        Supplier<CloseableHttpClient> getClientSupplier() {
            return cliSup;
        }

        /**
         *
         * @param cliSup
         */
        void setClientSupplier(Supplier<CloseableHttpClient> cliSup) {
            this.cliSup = cliSup;
        }

        /**
         *
         * @param respLog
         */
        void setResponseLog(boolean respLog) {
            this.respLog = respLog;
        }

        /**
         *
         */
        void prepareClient() {
            httpCli = cliSup != null ? cliSup.get() : HttpClients.createDefault();
        }

        @Override
        public NCTestResult ask(String txt) throws NCTestClientException, IOException {
            if (txt == null)
                throw new IllegalArgumentException("Test sentence text cannot be 'null'.");

            if (!opened) throw new IllegalStateException("Client is not opened.");
            if (closed) throw new IllegalStateException("Client already closed.");

            return embedded ? askEmbedded(txt) : askNotEmbedded(txt);
        }

        /**
         *
         * @param txt
         * @return
         * @throws IOException
         */
        private NCTestResult askNotEmbedded(String txt) throws IOException {
            NCRequestResultJson resJs;

            long now = System.currentTimeMillis();

            try {
                resJs = restAskSync(txt);
            }
            catch (NCTestClientException e) {
                log.info("'ask' request '{}' answered unsuccessfully with error: {}",
                    txt,
                    e.getLocalizedMessage()
                );

                return mkResult(txt, mdlId, null, null, e.getLocalizedMessage(), null, 0);
            }

            NCRequestStateJson state = resJs.getState();

            return processResult(
                txt,
                mkResult(
                    state.getText(),
                    mdlId,
                    state.getResultType(),
                    state.getResultBody() != null ?
                        "json".equals(state.getResultType()) ?
                            gson.toJson(state.getResultBody()) :
                            (String)state.getResultBody() :
                        null,
                    state.getError(),
                    state.getIntentId(),
                    System.currentTimeMillis() - now
                )
            );
        }

        /**
         *
         * @param txt
         * @return
         * @throws IOException
         */
        private NCTestResult askEmbedded(String txt) throws IOException {
            String srvReqId;

            long now = System.currentTimeMillis();

            try {
                srvReqId = restAsk(txt);
            }
            catch (NCTestClientException e) {
                log.info("'ask' request '{}' answered unsuccessfully with error: {}",
                    txt,
                    e.getLocalizedMessage()
                );

                return mkResult(txt, mdlId, null, null, e.getLocalizedMessage(), null,0);
            }

            long maxTime = System.currentTimeMillis() + DFLT_MAX_WAIT_TIME;

            NCEmbeddedResult res;

            while (true) {
                res = embeddedResMap.get(srvReqId);

                if (res != null)
                    break;

                long sleepTime = maxTime - System.currentTimeMillis();

                if (sleepTime <= 0)
                    throw new NCTestClientException("Max wait time elapsed.");

                synchronized (mux) {
                    try {
                        mux.wait(sleepTime);
                    }
                    catch (InterruptedException e) {
                        throw new NCTestClientException("Result wait thread interrupted.", e);
                    }
                }
            }

            return processResult(
                txt,
                mkResult(
                    res.getOriginalText(),
                    mdlId,
                    res.getType(),
                    res.getBody() != null ?
                        "json".equals(res.getType()) ? gson.toJson(res.getBody()) : res.getBody() :
                        null,
                    res.getErrorMessage(),
                    res.getIntentId(),
                    System.currentTimeMillis() - now
                )
            );
        }

        /**
         *
         * @param txt
         * @param res
         * @return
         */
        private NCTestResult processResult(String txt, NCTestResult res) {
            if (res.isOk()) {
                assert res.getResultType().isPresent() && res.getResult().isPresent();

                if (respLog) {
                    log.info("'ask' request '{}' answered successfully with '{}' result:\n{}", txt,
                        res.getResultType().get(), mkPrettyString(res.getResultType().get(), res.getResult().get()));
                }
            }
            else {
                assert res.getResultError().isPresent();

                if (respLog) {
                    log.info("'ask' request '{}' answered unsuccessfully with result:\n{}", txt,
                        res.getResultError().get());
                }
            }

            return res;
        }

        /**
         *
         * @param type
         * @param body
         * @return
         */
        private String mkPrettyString(String type, String body) {
            try {
                switch (type) {
                    case "html": return Jsoup.parseBodyFragment(body).outerHtml();
                    case "text":
                    case "yaml":
                    case "json": // JSON already configured for pretty printing.
                        return body;

                    default: return body;
                }
            }
            catch (Exception e) {
                log.error(
                    "Error during result decoding [type={}, body={}, error={}]", type, body, e.getLocalizedMessage()
                );

                return body;
            }
        }

        @Override
        public void open(String mdlId) throws NCTestClientException, IOException {
            assert mdlId != null;

            if (opened) throw new IllegalStateException("Client already opened.");
            if (closed) throw new IllegalStateException("Client already closed.");

            acsTok = restSignin();

            if (embedded) {
                embeddedCb = (NCEmbeddedResult res) -> {
                    embeddedResMap.put(res.getServerRequestId(), res);

                    synchronized (mux) {
                        mux.notifyAll();
                    }
                };

                NCEmbeddedProbe.registerCallback(embeddedCb);
            }

            this.mdlId = mdlId;

            this.opened = true;
        }

        @Override
        public void close() throws NCTestClientException, IOException {
            if (!opened) throw new IllegalStateException("Client is not opened.");
            if (closed) throw new IllegalStateException("Client is already closed.");

            if (embedded && embeddedCb != null)
                NCEmbeddedProbe.unregisterCallback(embeddedCb);

            restCancel();
            restSignout();

            closed = true;
        }

        @Override
        public void clearConversation() throws NCTestClientException, IOException {
            if (!opened) throw new IllegalStateException("Client is not opened.");
            if (closed) throw new IllegalStateException("Client is already closed.");

            restClearConversation();
        }

        @SuppressWarnings("unchecked")
        private <T> T getField(Map<String, Object> m, String fn) throws NCTestClientException {
            Object o = m.get(fn);

            if (o == null)
                throw new NCTestClientException(String.format("Missed expected field [fields=%s, field=%s]",
                    m.keySet(), fn));

            try {
                return (T) o;
            }
            catch (ClassCastException e) {
                throw new NCTestClientException(String.format("Invalid field type: %s", o), e);
            }
        }

        /**
         *
         * @param m
         * @throws NCTestClientException
         */
        private void checkStatus(Map<String, Object> m) throws NCTestClientException {
            String status = getField(m, "status");

            if (!status.equals(STATUS_API_OK))
                throw new NCTestClientException(String.format("Unexpected message status: %s", status));
        }

        /**
         *
         * @param js
         * @param t
         * @param <T>
         * @return
         * @throws NCTestClientException
         */
        private <T> T extract(JsonElement js, Type t) throws NCTestClientException {
            try {
                return gson.fromJson(js, t);
            }
            catch (JsonSyntaxException e) {
                throw new NCTestClientException(String.format("Invalid field type [json=%s, type=%s]", js, t), e);
            }
        }

        /**
         *
         * @param js
         * @param name
         * @param type
         * @param <T>
         * @return
         * @throws NCTestClientException
         */
        private <T> T checkAndExtract(String js, String name, Type type) throws NCTestClientException {
            Map<String, Object> m = gson.fromJson(js, TYPE_RESP);

            checkStatus(m);

            return extract(gson.toJsonTree(getField(m, name)), type);
        }

        /**
         *
         * @param url
         * @param ps
         * @return
         * @throws NCTestClientException
         * @throws IOException
         */
        @SafeVarargs
        private final String post(String url, Pair<String, Object>... ps) throws NCTestClientException, IOException {
            HttpPost post = new HttpPost(baseUrl + url);

            try {
                if (reqCfg != null)
                    post.setConfig(reqCfg);

                StringEntity entity = new StringEntity(
                    gson.toJson(Arrays.stream(ps).
                        filter(p -> p.getValue() != null).
                        collect(Collectors.toMap(Pair::getKey, Pair::getValue))),
                    "UTF-8"
                );

                post.setHeader("Content-Type", "application/json");
                post.setEntity(entity);

                ResponseHandler<String> respHdl = resp -> {
                    int code = resp.getStatusLine().getStatusCode();

                    HttpEntity e = resp.getEntity();

                    String js = e != null ? EntityUtils.toString(e) : null;

                    if (js == null)
                        throw new NCTestClientException(String.format("Unexpected empty response [code=%d]", code));

                    switch (code) {
                        case 200: return js;
                        case 400: throw new NCTestClientException(js);
                        default:
                            throw new NCTestClientException(
                                String.format("Unexpected response [code=%d, text=%s]", code, js)
                            );
                    }
                };

                return httpCli.execute(post, respHdl);
            }
            finally {
                post.releaseConnection();
            }
        }

        /**
         * @throws IOException Thrown in case of IO errors.
         * @throws NCTestClientException Thrown in case of test client errors.
         */
        private void restCancel() throws IOException, NCTestClientException {
            log.info("'cancel' request sent.");

            checkStatus(
                gson.fromJson(
                    post(
                        "cancel",
                        Pair.of("acsTok", acsTok)
                    ),
                    TYPE_RESP
                )
            );
        }

        /**
         *
         * @throws IOException Thrown in case of IO errors.
         * @throws NCTestClientException Thrown in case of test client errors.
         */
        private void restClearConversation() throws IOException, NCTestClientException {
            log.info("'clear/conversation' request sent for data model: {}", mdlId);

            checkStatus(gson.fromJson(
                post(
                    "clear/conversation",
                    Pair.of("acsTok", acsTok),
                    Pair.of("mdlId", mdlId)
                ),
                TYPE_RESP)
            );
        }

        /**
         * @return Access token.
         * @throws IOException Thrown in case of IO errors.
         * @throws NCTestClientException Thrown in case of test client errors.
         */
        private String restSignin() throws IOException, NCTestClientException {
            log.info("'/signin' request sent for: {}", email);

            String res = post("/signin", Pair.of("email", email), Pair.of("passwd", pswd));

            Map<String, Object> m = gson.fromJson(res, TYPE_RESP);

            checkStatus(m);

            return extract(gson.toJsonTree(getField(m, "acsTok")), String.class);
        }

        /**
         * @param txt
         * @return
         * @throws IOException Thrown in case of IO errors.
         * @throws NCTestClientException Thrown in case of test client errors.
         */
        private NCRequestResultJson restAskSync(String txt) throws IOException, NCTestClientException {
            log.info("'ask/sync' request '{}' sent for data model ID: {}", txt, mdlId);

            return
                gson.fromJson(
                    post(
                        "ask/sync",
                        Pair.of("acsTok", acsTok),
                        Pair.of("txt", txt),
                        Pair.of("mdlId", mdlId)
                    ),
                    TYPE_RES
                );
        }

        /**
         * @param txt
         * @return
         * @throws IOException Thrown in case of IO errors.
         * @throws NCTestClientException Thrown in case of test client errors.
         */
        private String restAsk(String txt) throws IOException, NCTestClientException {
            log.info("'ask' request '{}' sent for data model ID: {}", txt, mdlId);

            Map<String, Object> m = gson.fromJson(post(
                "ask",
                Pair.of("acsTok", acsTok),
                Pair.of("txt", txt),
                Pair.of("mdlId", mdlId)
            ), TYPE_RESP);

            checkStatus(m);

            return extract(gson.toJsonTree(getField(m, "srvReqId")), String.class);
        }

        /**
         * @throws IOException Thrown in case of IO errors.
         * @throws NCTestClientException Thrown in case of test client errors.
         */
        private void restSignout() throws IOException, NCTestClientException {
            log.info("'/signout' request sent for: {}", email);

            checkStatus(gson.fromJson(
                post(
                    "signout",
                    Pair.of("acsTok", acsTok)
                ),
                TYPE_RESP)
            );
        }

        /**
         *
         * @param txt
         * @param mdlId Model ID.
         * @param resType
         * @param resBody
         * @param errMsg
         * @param intentId
         * @param time
         * @return
         */
        private NCTestResult mkResult(
            String txt,
            String mdlId,
            String resType,
            String resBody,
            String errMsg,
            String intentId,
            long time
        ) {
            assert txt != null;
            assert mdlId != null;
            assert (resType != null && resBody != null) ^ errMsg != null;

            return new NCTestResult() {
                private<T> Optional<String> convert(String s) {
                    return s == null ? Optional.empty() : Optional.of(s);
                }

                @Override
                public String getText() {
                    return txt;
                }

                @Override
                public long getProcessingTime() {
                    return time;
                }

                @Override
                public String getModelId() {
                    return mdlId;
                }

                @Override
                public Optional<String> getResult() {
                    return convert(resBody);
                }

                @Override
                public Optional<String> getResultType() {
                    return convert(resType);
                }

                @Override
                public Optional<String> getResultError() {
                    return convert(errMsg);
                }

                @Override
                public String getIntentId() {
                    return intentId;
                }
            };
        }
    }

    /**
     * @param name
     * @param v
     * @throws IllegalArgumentException
     */
    private void checkNotNull(String name, Object v) throws IllegalArgumentException {
        if (v == null) throw new IllegalArgumentException(String.format("Test client property cannot be null: '%s'", name));
    }
}
