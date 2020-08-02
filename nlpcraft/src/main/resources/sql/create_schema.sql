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
-- Company table.
--
DROP TABLE IF EXISTS nc_company CASCADE;
CREATE TABLE nc_company (
    id LONG PRIMARY KEY,
    name VARCHAR NOT NULL, -- Unique.
    website VARCHAR,
    country VARCHAR,
    region VARCHAR,
    city VARCHAR,
    address VARCHAR,
    postal_code VARCHAR,
    auth_token VARCHAR NOT NULL, -- Unique.
    auth_token_hash VARCHAR NOT NULL, -- Unique.
    created_on TIMESTAMP NOT NULL,
    last_modified_on TIMESTAMP NOT NULL
) WITH "template=replicated, atomicity=transactional";

CREATE INDEX company_idx_1 ON nc_company(name);
CREATE INDEX company_idx_2 ON nc_company(auth_token);
CREATE INDEX company_idx_3 ON nc_company(auth_token_hash);

DROP TABLE IF EXISTS nc_user;
CREATE TABLE nc_user (
    id LONG PRIMARY KEY,
    company_id LONG NOT NULL, -- Foreign key nc_company.id.
    ext_id VARCHAR NULL, -- Unique for company.
    email VARCHAR NULL, -- Unique.
    avatar_url VARCHAR NULL,
    first_name VARCHAR NULL,
    last_name VARCHAR NULL,
    is_admin BOOL NOT NULL,
    passwd_salt VARCHAR NULL,
    created_on TIMESTAMP NOT NULL,
    last_modified_on TIMESTAMP NOT NULL
) WITH "template=replicated, atomicity=transactional";

CREATE INDEX nc_user_idx_1 ON nc_user(email);
CREATE INDEX nc_user_idx_2 ON nc_user(company_id, ext_id);
CREATE INDEX nc_user_idx_3 ON nc_user(company_id);


DROP TABLE IF EXISTS nc_user_property;
CREATE TABLE nc_user_property (
    id LONG PRIMARY KEY,
    user_id LONG NOT NULL, -- Foreign key nc_user.id.
    property VARCHAR NOT NULL,
    value VARCHAR NULL,
    created_on TIMESTAMP NOT NULL,
    last_modified_on TIMESTAMP NOT NULL
) WITH "template=replicated, atomicity=transactional";

CREATE INDEX nc_user_property_idx_1 ON nc_user_property(user_id);

DROP TABLE IF EXISTS passwd_pool;
CREATE TABLE passwd_pool (
    id LONG PRIMARY KEY,
    passwd_hash VARCHAR NOT NULL
) WITH "template=replicated, atomicity=transactional";

DROP TABLE IF EXISTS proc_log;
CREATE TABLE proc_log (
    id LONG PRIMARY KEY,
    srv_req_id VARCHAR,
    txt VARCHAR NULL,
    user_id LONG NULL,
    model_id VARCHAR NULL,
    status VARCHAR NULL,
    user_agent VARCHAR NULL,
    rmt_address VARCHAR NULL,
    sen_data VARCHAR NULL,
    recv_tstamp TIMESTAMP NOT NULL,
    resp_tstamp TIMESTAMP NULL,
    cancel_tstamp TIMESTAMP NULL,
    res_type VARCHAR NULL,
    res_body_gzip VARCHAR NULL,
    intent_id VARCHAR NULL,
    error VARCHAR NULL,
    probe_token VARCHAR NULL,
    probe_id VARCHAR NULL,
    probe_guid VARCHAR NULL,
    probe_api_version VARCHAR NULL,
    probe_api_date DATE NULL,
    probe_os_version VARCHAR NULL,
    probe_os_name VARCHAR NULL,
    probe_os_arch VARCHAR NULL,
    probe_start_tstamp TIMESTAMP NULL,
    probe_tmz_id VARCHAR NULL,
    probe_tmz_abbr VARCHAR NULL,
    probe_tmz_name VARCHAR NULL,
    probe_user_name VARCHAR NULL,
    probe_java_version VARCHAR NULL,
    probe_java_vendor VARCHAR NULL,
    probe_host_name VARCHAR NULL,
    probe_host_addr VARCHAR NULL,
    probe_mac_addr VARCHAR NULL
) WITH "template=replicated, atomicity=transactional";

CREATE INDEX proc_log_idx_1 ON proc_log(srv_req_id);

CREATE TABLE feedback (
    id LONG PRIMARY KEY,
    srv_req_id VARCHAR NOT NULL,
    user_id LONG NOT NULL,
    score DOUBLE NOT NULL,
    comment VARCHAR NULL,
    created_on TIMESTAMP NOT NULL
);

CREATE INDEX feedback_idx_1 ON feedback(srv_req_id);