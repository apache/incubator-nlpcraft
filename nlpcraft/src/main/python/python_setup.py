#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

import os
import subprocess
import time
import logging
from nc_pyutils.ncutilities import get_nc_setup_config

# getting the configuration
nc_setup_conf = get_nc_setup_config()

log_filename = f'python_setup_{time.strftime("%Y%m%d-%H%M%S")}.log'
log_file_path = os.path.join(nc_setup_conf['NLPCRAFT_PROJ_HOME'], log_filename)
logging.basicConfig(
                    filename=log_file_path,
                    filemode='a',
                    format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
                    datefmt='%H:%M:%S',
                    level=logging.DEBUG)

logger = logging.getLogger(__name__)
logger.addHandler(logging.StreamHandler())

logger.debug("Starting python environment setup:\n")
for items in nc_setup_conf.items():
    logger.debug(items)


try:
    # Check if conda is installed
    subprocess.call(['conda', '-V'])
    conda_version: str = subprocess.run(['conda', '-V'], stdout=subprocess.PIPE).stdout.decode('utf-8').split()[1][:4]
    # Checking conda version
    if float(conda_version) < nc_setup_conf['MIN_CONDA_VERSION']:
        raise SystemExit(f'[ERROR] Invalid conda version. The version you have is {float(conda_version)} .'
                           'See requirements: https://nlpcraft.apache.org/download.html')
except FileNotFoundError:
    raise SystemExit('[ERROR] Conda is not installed. See requirements: https://nlpcraft.apache.org/download.html')
except Exception as err:
    raise SystemExit(f'[ERROR] Conda verification error. See requirements: https://nlpcraft.apache.org/download.html')

