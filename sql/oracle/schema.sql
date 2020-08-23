--
-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

--
-- +=================================+
-- | ORACLE SQL schema definition. |
-- +=================================+
--
-- NOTE: connect as 'nlpcraft'.
--

--
-- Company table.
--
CREATE TABLE nc_company (
    id NUMBER PRIMARY KEY,
    name VARCHAR2(64) NOT NULL,
    website VARCHAR2(256),
    country VARCHAR2(32),
    region VARCHAR2(512),
    city VARCHAR2(512),
    address VARCHAR2(512),
    postal_code VARCHAR2(32),
    auth_token VARCHAR2(64) NOT NULL,
    auth_token_hash VARCHAR2(64) NOT NULL,
    created_on DATE DEFAULT sysdate NOT NULL,
    last_modified_on DATE DEFAULT sysdate NOT NULL
);

CREATE UNIQUE INDEX nc_company_idx_1 ON nc_company(name);
CREATE UNIQUE INDEX nc_company_idx_2 ON nc_company(auth_token);
CREATE UNIQUE INDEX nc_company_idx_3 ON nc_company(auth_token_hash);

--
-- User table.
--
CREATE TABLE nc_user (
    id NUMBER PRIMARY KEY,
    company_id NUMBER NOT NULL,
    ext_id VARCHAR2(64) NULL, -- External user ID.
    email VARCHAR2(64) NULL, -- Used as username during login.
    avatar_url VARCHAR2(2083) NULL, -- URL or encoding of avatar for this user, if any.
    first_name VARCHAR2(64) NULL,
    last_name VARCHAR2(64) NULL,
    is_admin NUMBER(1) NOT NULL, -- Whether or not created with admin token.
    passwd_salt VARCHAR2(64) NULL,
    created_on DATE DEFAULT sysdate NOT NULL,
    last_modified_on DATE DEFAULT sysdate NOT NULL,
    CONSTRAINT fk_company_id FOREIGN KEY (company_id)REFERENCES nc_company(id)
);

CREATE UNIQUE INDEX nc_user_idx_1 ON nc_user(email);
CREATE UNIQUE INDEX nc_user_idx_2 ON nc_user(company_id, ext_id);
CREATE INDEX nc_user_idx_3 ON nc_user(company_id);

--
-- User properties table.
--
CREATE TABLE nc_user_property (
    id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    property VARCHAR2(64) NOT NULL,
    value VARCHAR2(512) NULL,
    created_on DATE DEFAULT sysdate NOT NULL,
    last_modified_on DATE DEFAULT sysdate NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)REFERENCES nc_user(id)
);

CREATE INDEX nc_user_property_idx1 ON nc_user_property(user_id);

--
-- Pool of password hashes.
--
CREATE TABLE passwd_pool (
    id NUMBER PRIMARY KEY,
    passwd_hash VARCHAR2(64) NOT NULL
);

--
-- Processing log.
--
CREATE TABLE proc_log (
    id NUMBER PRIMARY KEY,
    srv_req_id VARCHAR2(64) NOT NULL,
    txt VARCHAR2(1024) NULL,
    user_id NUMBER NULL,
    model_id VARCHAR2(64) NULL,
    status VARCHAR2(32) NULL,
    user_agent VARCHAR2(512) NULL,
    rmt_address VARCHAR2(256) NULL,
    sen_data CLOB NULL,
    -- Ask and result timestamps.
    recv_tstamp DATE NOT NULL, -- Initial receive timestamp.
    resp_tstamp DATE NULL, -- Result or error response timestamp.
    cancel_tstamp DATE NULL, -- Cancel timestamp.
    -- Result parts.
    res_type VARCHAR2(32) NULL,
    res_body_gzip CLOB NULL, -- GZIP-ed result body.
    intent_id VARCHAR2(256) NULL,
    error CLOB NULL,
    -- Probe information for this request.
    probe_token VARCHAR2(256) NULL,
    probe_id VARCHAR2(512) NULL,
    probe_guid VARCHAR2(512) NULL,
    probe_api_version VARCHAR2(512) NULL,
    probe_api_date DATE NULL,
    probe_os_version VARCHAR2(512) NULL,
    probe_os_name VARCHAR2(512) NULL,
    probe_os_arch VARCHAR2(512) NULL,
    probe_start_tstamp DATE NULL,
    probe_tmz_id VARCHAR2(64) NULL,
    probe_tmz_abbr VARCHAR2(64) NULL,
    probe_tmz_name VARCHAR2(64) NULL,
    probe_user_name VARCHAR2(512) NULL,
    probe_java_version VARCHAR2(512) NULL,
    probe_java_vendor VARCHAR2(512) NULL,
    probe_host_name VARCHAR2(1024) NULL,
    probe_host_addr VARCHAR2(512) NULL,
    probe_mac_addr VARCHAR2(512) NULL
);

CREATE UNIQUE INDEX proc_log_idx_1 ON proc_log(srv_req_id);

--
-- Request processing user feedback.
--
CREATE TABLE feedback (
    id NUMBER PRIMARY KEY,
    srv_req_id VARCHAR2(64) NOT NULL,
    user_id NUMBER NOT NULL,
    score NUMBER NOT NULL,
    comment VARCHAR2(1024) NULL,
    created_on DATE DEFAULT sysdate NOT NULL
);

CREATE INDEX feedback_idx_1 ON feedback(srv_req_id);