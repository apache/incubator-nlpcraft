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

import pathlib
import os
import subprocess
from pyhocon import ConfigFactory

CURRENT_FILE_NAME = os.path.abspath(__file__)
CURRENT_DIR = os.path.dirname(CURRENT_FILE_NAME)

conf = ConfigFactory.parse_file(os.path.join(CURRENT_DIR, os.pardir, 'resources', 'nlpcraft.conf'))

FLASK_ENV = conf.get_string('nlpcraft.server.python.api.env')

if FLASK_ENV is not None:
    os.environ['FLASK_ENV'] = FLASK_ENV
else:
    os.environ['FLASK_ENV'] = "development"

# Get the absolute path of parent folder
parent_folder = pathlib.Path(__file__).parent.absolute()

os.environ['FLASK_APP'] = os.path.join(parent_folder, 'nc_server.py')
subprocess.call(['python3', '-m', 'flask', 'run'])
