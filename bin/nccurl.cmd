@echo OFF

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

rem Quick shortcut script for localhost testing with curl (w/o excessive command line).
rem
rem Usage:
rem - 1st parameter is REST URL unique suffix (i.e. /signin) w/o leading '/'
rem - 2nd parameter is JSON payload string
rem
rem Example usage:
rem   >./nccurl.cmd signin '{"email": "admin@admin.com", "passwd": "admin"}'
rem   >./nccurl.cmd ask '{"acsTok": "OgJanjDzk", "txt": "Hi!", "mdlId": "nlpcraft.helloworld.ex"}'
rem   >./nccurl.cmd check '{"acsTok": "OgJanjDzk"}'
rem
rem For pretty JSON output pipe curl to 'python -m json.tool':
rem   $./nccurl.sh check '{"acsTok": "OgJanjDzk"}' | python -m json.tool

curl http://localhost:8081/api/v1/%~1 -s -d "%~2" -H "Content-Type: application/json" | python -m json.tool
