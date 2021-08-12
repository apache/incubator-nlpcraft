::
:: Licensed to the Apache Software Foundation (ASF) under one or more
:: contributor license agreements.  See the NOTICE file distributed with
:: this work for additional information regarding copyright ownership.
:: The ASF licenses this file to You under the Apache License, Version 2.0
:: (the "License"); you may not use this file except in compliance with
:: the License.  You may obtain a copy of the License at
::
::      http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
::
:: Simple Curl-based script for getting contextual related words suggestions for a single input sentence.
:: Example:
::     > bin\suggest.cmd "what is the chance of rain tomorrow?" 5
::       % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
::                                      Dload  Upload   Total   Spent    Left  Speed
::     100   214  100   104  100   110    104    110  0:00:01 --:--:--  0:00:01   804
::     [
::         [
::             "rain",
::             "snow",
::             "rainfall",
::             "precipitation",
::             "rains",
::             "flooding",
::             "storms",
::             "raining",
::             "sunshine",
::             "showers"
::         ]
::     ]
::
:: NOTE: You need to have REST server running (see 'start_server.{cmd|ps1}' scripts in the same folder).
::

@echo off

where curl >nul 2>&1 || echo 'curl' not found && exit /b
where python3 >nul 2>&1 || echo 'python3' not found && exit /b

curl http://localhost:5000/api/v1/ctxserver/suggestions -d "{\"sentences\": [{\"text\": \"%~1\", \"indexes\": [%~2]}], \"simple\": true, \"limit\": 10}" -H "Content-Type: application/json" | python3 -m json.tool