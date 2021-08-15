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

# NOTE:
# ----
# This script may not be suitable for production usage. Please see official Flask documentation for
# more info on how to deploy Flask applications.

# get the location of this file
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

abort() {
  echo "$1"
  exit 1
}

# Use/Uncomment the command below if needed
#conda init <your-shell-name>

# ********************************* IMPORTANT *****************************************************
# Set conda.sh path below: See incubator-nlpcraft/nlpcraft/src/main/python/ctxword/README.md

echo "IMPORTANT REMINDER: Please do not forget to set the path to your conda.sh file"
# Replace your conda.sh location here
source ~/miniconda3/etc/profile.d/conda.sh

# deactivating any virtual environments that are already running
deactivate || echo "Tried to deactivate any virtual environments"
conda deactivate || echo "Tried to deactivate any conda environment"

conda activate ~/.nlpcraft-python/nlpcraft-condaenv

[ -x "$(command -v python3)" ] || abort "'python3' not found."
echo "Python executable used:" $(which python3)

export FLASK_ENV=development
export FLASK_APP=$SCRIPT_DIR/../nc_server.py

python3 -m flask run

# Use the command below to deactivate the conda environment
# conda deactivate