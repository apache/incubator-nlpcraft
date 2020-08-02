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
-- | Postgres SQL schema definition. |
-- +=================================+
--
-- NOTE: database 'nlpcraft' should be created and owned by 'nlpcraft' user.
--

--
-- Company table.
--
CREATE TABLE nc_company (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    website VARCHAR(256),
    country VARCHAR(32),
    region VARCHAR(512),
    city VARCHAR(512),
    address VARCHAR(512),
    postal_code VARCHAR(32),
    auth_token VARCHAR(64) NOT NULL,
    auth_token_hash VARCHAR(64) NOT NULL,
    created_on TIMESTAMP(3) NOT NULL DEFAULT current_timestamp(3),
    last_modified_on TIMESTAMP(3) NOT NULL DEFAULT current_timestamp(3)
);

CREATE UNIQUE INDEX nc_company_idx_1 ON nc_company(name);
CREATE UNIQUE INDEX nc_company_idx_2 ON nc_company(auth_token);
CREATE UNIQUE INDEX nc_company_idx_3 ON nc_company(auth_token_hash);

--
--
-- User table.
--
CREATE TABLE nc_user (
    id SERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    ext_id VARCHAR(64) NULL, -- External user ID.
    email VARCHAR(64) NULL, -- Used as username during login.
    avatar_url TEXT NULL, -- URL or encoding of avatar for this user, if any.
    first_name VARCHAR(64) NULL,
    last_name VARCHAR(64) NULL,
    is_admin BOOL NOT NULL, -- Whether or not created with admin token.
    passwd_salt VARCHAR(64) NULL,
    created_on TIMESTAMP(3) NOT NULL DEFAULT current_timestamp(3),
    last_modified_on TIMESTAMP(3) NOT NULL DEFAULT current_timestamp(3),
    FOREIGN KEY (company_id) REFERENCES nc_company(id)
);

CREATE UNIQUE INDEX nc_user_idx_1 ON nc_user(email);
CREATE UNIQUE INDEX nc_user_idx_2 ON nc_user(company_id, ext_id);
CREATE INDEX nc_user_idx_3 ON nc_user(company_id);

--
-- Pool of password hashes.
--
CREATE TABLE passwd_pool (
    id SERIAL PRIMARY KEY,
    passwd_hash VARCHAR(64) NOT NULL
);

--
-- User properties table.
--
CREATE TABLE nc_user_property (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    property VARCHAR(64) NOT NULL,
    value VARCHAR(512) NULL,
    created_on TIMESTAMP(3) NOT NULL DEFAULT current_timestamp(3),
    last_modified_on TIMESTAMP(3) NOT NULL DEFAULT current_timestamp(3),
    FOREIGN KEY (user_id) REFERENCES nc_user(id)
);

CREATE INDEX nc_user_property_idx1 ON nc_user_property(user_id);

--
-- Processing log.
--
CREATE TABLE proc_log (
    id SERIAL PRIMARY KEY,
    srv_req_id VARCHAR(64) NOT NULL,
    txt VARCHAR(1024) NULL,
    user_id BIGINT NULL,
    model_id VARCHAR(64) NULL,
    status VARCHAR(32) NULL,
    user_agent VARCHAR(512) NULL,
    rmt_address VARCHAR(256) NULL,
    sen_data TEXT NULL,
    -- Ask and result timestamps.
    recv_tstamp TIMESTAMP NOT NULL, -- Initial receive timestamp.
    resp_tstamp TIMESTAMP NULL, -- Result or error response timestamp.
    cancel_tstamp TIMESTAMP NULL, -- Cancel timestamp.
    -- Result parts.
    res_type VARCHAR(32) NULL,
    res_body_gzip TEXT NULL, -- GZIP-ed result body.
    intent_id VARCHAR(256) NULL,
    error TEXT NULL,
    -- Probe information for this request.
    probe_token VARCHAR(256) NULL,
    probe_id VARCHAR(512) NULL,
    probe_guid VARCHAR(512) NULL,
    probe_api_version VARCHAR(512) NULL,
    probe_api_date DATE NULL,
    probe_os_version VARCHAR(512) NULL,
    probe_os_name VARCHAR(512) NULL,
    probe_os_arch VARCHAR(512) NULL,
    probe_start_tstamp TIMESTAMP NULL,
    probe_tmz_id VARCHAR(64) NULL,
    probe_tmz_abbr VARCHAR(64) NULL,
    probe_tmz_name VARCHAR(64) NULL,
    probe_user_name VARCHAR(512) NULL,
    probe_java_version VARCHAR(512) NULL,
    probe_java_vendor VARCHAR(512) NULL,
    probe_host_name VARCHAR(1024) NULL,
    probe_host_addr VARCHAR(512) NULL,
    probe_mac_addr VARCHAR(512) NULL
);

CREATE UNIQUE INDEX proc_log_idx_1 ON proc_log(srv_req_id);

--
-- Request processing user feedback.
--
CREATE TABLE feedback (
    id SERIAL PRIMARY KEY,
    srv_req_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    score NUMERIC NOT NULL,
    comment VARCHAR(1024) NULL,
    created_on TIMESTAMP(3) NOT NULL DEFAULT current_timestamp(3)
);

CREATE INDEX feedback_idx_1 ON feedback(srv_req_id);