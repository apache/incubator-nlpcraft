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

#
# This script installs/downloads all necessary dependencies for fasttext
# into './data' sub-folder.
#

abort() {
  echo "$1"
  exit 1
}

[ -x "$(command -v wget)" ] || abort "'wget' not found."
[ -x "$(command -v gunzip)" ] || abort "'gunzip' not found."
[ -x "$(command -v python3)" ] || abort "'python3' not found."
[ -x "$(command -v pip3)" ] || abort "'pip3' not found."

[ ! -f data/cc.en.300.bin.gz ] && \
  [ ! -f data/cc.en.300.bin ] && \
  { wget https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.en.300.bin.gz -P data || \
  abort "Failed to download fasttext data."; }

[ ! -f data/cc.en.300.bin ] && { gunzip -v data/cc.en.300.bin.gz || abort "Failed to extract files."; }
