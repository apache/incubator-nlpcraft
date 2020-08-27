#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Quick shortcut script for localhost testing with curl (w/o excessive command line).
#
# Usage:
# - 1st parameter is REST URL unique suffix (i.e. /signin) w/o leading '/'
# - 2nd parameter is JSON payload string
#
# Example usage:
#   $./nccurl.sh signin '{"email": "admin@admin.com", "passwd": "admin"}'
#   $./nccurl.sh ask '{"acsTok": "OgJanjDzk", "txt": "Hi!", "mdlId": "nlpcraft.helloworld.ex"}'
#   $./nccurl.sh check '{"acsTok": "OgJanjDzk"}'

abort() {
  echo "$1"
  exit 1
}

[ -x "$(command -v curl)" ] || abort "'curl' not found."
[ -x "$(command -v python3)" ] || abort "'python3' not found."

# shellcheck disable=SC2086
curl -s -d "$2" -H 'Content-Type: application/json' http://localhost:8081/api/v1/$1 | python3 -m json.tool
