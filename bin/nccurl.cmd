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

:: Quick shortcut script for localhost testing with curl (w/o excessive command line).
::
:: Usage:
:: - 1st parameter is REST URL unique suffix (i.e. /signin) w/o leading '/'
:: - 2nd parameter is JSON payload string (note that double quotes in JSON must be escaped).
::
:: Example usage:
::   >./nccurl.cmd signin "{\"email\": \"admin@admin.com\", \"passwd\": \"admin\"}"
::   >./nccurl.cmd ask "{\"acsTok\": \"OgJanjDzk\", \"txt\": \"Hi!\", \"mdlId\": \"nlpcraft.helloworld.ex\"}"
::   >./nccurl.cmd check "{\"acsTok\": \"OgJanjDzk\"}"
::

@echo OFF

where curl >nul 2>&1 || echo 'curl' not found && exit /b
where python3 >nul 2>&1 || echo 'python3' not found && exit /b

curl http://localhost:8081/api/v1/%~1 -s -d "%~2" -H "Content-Type: application/json" | python3 -m json.tool
