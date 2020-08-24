@echo off

rem
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem
rem Simple Curl-based script for getting contextual related words suggestions for a single input sentence.
rem Example:
rem     > bin\suggest.cmd "what is the chance of rain tomorrow?" 5
rem       % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
rem                                      Dload  Upload   Total   Spent    Left  Speed
rem     100   214  100   104  100   110    104    110  0:00:01 --:--:--  0:00:01   804
rem     [
rem         [
rem             "rain",
rem             "snow",
rem             "rainfall",
rem             "precipitation",
rem             "rains",
rem             "flooding",
rem             "storms",
rem             "raining",
rem             "sunshine",
rem             "showers"
rem         ]
rem     ]
rem
rem NOTE: You need to have REST server running (see 'start_server.{cmd|ps1}' scripts in the same folder).
rem

where curl >nul 2>&1 || echo 'curl' not found && exit /b
where python3 >nul 2>&1 || echo 'python3' not found && exit /b

curl http://localhost:5000/suggestions -d "{\"sentences\": [{\"text\": \"%~1\", \"indexes\": [%~2]}], \"simple\": true, \"limit\": 10}" -H "Content-Type: application/json" | python3 -m json.tool