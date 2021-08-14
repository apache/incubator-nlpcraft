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

def get_nc_setup_config() -> dict:
    """return nlpcraft config"""
    import os

    USER_HOME_DIR = os.path.expanduser('~')
    NLPCRAFT_PROJ_HOME = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                      os.pardir, os.pardir, os.pardir, os.pardir, os.pardir)
    PYTHON_PROJ_REL_PATH = os.path.join('nlpcraft', 'src', 'main', 'python')
    CONF_REL_PATH = os.path.join('nlpcraft', 'src', 'main', 'resources', 'nlpcraft.conf')
    CONF_ABS_PATH = os.path.join(NLPCRAFT_PROJ_HOME, CONF_REL_PATH)
    CONDA_ENV_PATH = os.path.join(NLPCRAFT_PROJ_HOME, '.nlpcraft-python', 'nlpcraft-condaenv')
    PY_REQUIREMENTS_PATH = os.path.join(NLPCRAFT_PROJ_HOME, PYTHON_PROJ_REL_PATH, 'requirements.txt')
    NLPCRAFT_PYTHON_FOLDER = os.path.join(NLPCRAFT_PROJ_HOME, '.nlpcraft-python')
    NLPCRAFT_PYTHON3 = os.path.join(CONDA_ENV_PATH, 'bin', 'python3')
    MIN_CONDA_VERSION = 4.08

    config = {'USER_HOME_DIR': USER_HOME_DIR,
              'NLPCRAFT_PROJ_HOME': NLPCRAFT_PROJ_HOME,
              'PYTHON_PROJ_REL_PATH': PYTHON_PROJ_REL_PATH,
              'CONF_REL_PATH': CONF_REL_PATH,
              'CONF_ABS_PATH': CONF_ABS_PATH,
              'CONDA_ENV_PATH': CONDA_ENV_PATH,
              'PY_REQUIREMENTS_PATH': PY_REQUIREMENTS_PATH,
              'NLPCRAFT_PYTHON_FOLDER': NLPCRAFT_PYTHON_FOLDER,
              'NLPCRAFT_PYTHON3': NLPCRAFT_PYTHON3,
              'MIN_CONDA_VERSION': MIN_CONDA_VERSION
              }

    return config