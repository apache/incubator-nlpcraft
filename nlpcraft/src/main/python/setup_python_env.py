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
import json
import subprocess
import time
import logging

USER_HOME_DIR = os.path.expanduser('~')
NLPCRAFT_PROJ_HOME = os.getcwd()
PYTHON_PROJ_REL_PATH = os.path.join('nlpcraft', 'src', 'main', 'python')
PYTHON_JSON_CONFIG = os.path.join(PYTHON_PROJ_REL_PATH, 'py_config.json')
CONF_REL_PATH = os.path.join('nlpcraft', 'src', 'main', 'resources', 'nlpcraft.conf')
CONF_ABS_PATH = os.path.join(NLPCRAFT_PROJ_HOME, CONF_REL_PATH)
CONDA_ENV_PATH = os.path.join(USER_HOME_DIR, '.nlpcraft-python', 'nlpcraft-condaenv')
PY_REQUIREMENTS_PATH = os.path.join(NLPCRAFT_PROJ_HOME, PYTHON_PROJ_REL_PATH, 'requirements.txt')
NLPCRAFT_PYTHON_FOLDER = os.path.join(USER_HOME_DIR, '.nlpcraft-python')
NLPCRAFT_PYTHON3 = os.path.join(CONDA_ENV_PATH, 'bin', 'python3')

log_filename = f'python_setup_{time.strftime("%Y%m%d-%H%M%S")}.log'
log_file_path = os.path.join(NLPCRAFT_PYTHON_FOLDER, log_filename)
logging.basicConfig(filename=log_file_path,
                    filemode='a',
                    format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
                    datefmt='%H:%M:%S',
                    level=logging.DEBUG)

logger = logging.getLogger(__name__)
logger.addHandler(logging.StreamHandler())

logger.debug("Starting python environment setup:\n")
logger.debug(f'\n\nUSER_HOME_DIR: {USER_HOME_DIR}\n' f'NLPCRAFT_PROJ_HOME: {NLPCRAFT_PROJ_HOME}\n'
             f'PYTHON_PROJ_REL_PATH: {PYTHON_PROJ_REL_PATH}\n' f'PYTHON_JSON_CONFIG: {PYTHON_JSON_CONFIG}\n'
             f'CONF_REL_PATH: {CONF_REL_PATH}\n' f'CONF_ABS_PATH: {CONF_ABS_PATH}\n'
             f'CONDA_ENV_PATH: {CONDA_ENV_PATH}\n' f'PY_REQUIREMENTS_PATH: {PY_REQUIREMENTS_PATH}\n'
             f'NLPCRAFT_PYTHON_FOLDER: {NLPCRAFT_PYTHON_FOLDER}\n' f'NLPCRAFT_PYTHON3: {NLPCRAFT_PYTHON3}\n')

# load the python json config file
with open(file=PYTHON_JSON_CONFIG, mode='r') as py_config_file:
    py_config = json.load(fp=py_config_file)

try:
    # Check if conda is installed
    subprocess.call(['conda', '-V'])
    conda_version: str = subprocess.run(['conda', '-V'], stdout=subprocess.PIPE).stdout.decode('utf-8').split()[1][:3]
    # Checking conda version
    if float(conda_version) < py_config['conda_version']:
        raise RuntimeError('Invalid conda version. See requirements: https://nlpcraft.apache.org/download.html')
except FileNotFoundError:
    raise ModuleNotFoundError('Conda is not installed. See requirements: https://nlpcraft.apache.org/download.html')
except Exception as err:
    logger.critical('Conda verification failure.')
    logger.error(err)


# TODO: log the output of conda create into the log file
logger.info('Conda environment creation starting...')
subprocess.run(['conda', 'create', '-p', CONDA_ENV_PATH,
                f"python={py_config['python_version']}"])


# TODO: log the output of python dependency installation into the log file
logger.info('Installing python dependencies...')
subprocess.run([NLPCRAFT_PYTHON3, '-m', 'pip', 'install', '-r', PY_REQUIREMENTS_PATH])

logger.info('Python environment setup successful.')
