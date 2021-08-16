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
import sys
import subprocess
import time
import argparse
import logging
from distutils.version import LooseVersion
from nc_pyutils.ncutilities import get_nc_setup_config


#TODO: Documentation and refactoring

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

parser = argparse.ArgumentParser(description='NLPCraft Python Setup Parameters')

parser.add_argument('--requirements',
                    help='Check if conda is installed',
                    action='store_true')

parser.add_argument('--setupdir',
                    help='Create the <USER_HOME>/.nlpcraft-python directory',
                    action='store_true')

parser.add_argument('--pytorch',
                    help='Install the correct PyTorch version depending on the OS',
                    action='store_true')

args = parser.parse_args()

logger.debug("Starting python environment setup:\n")
for items in nc_setup_conf.items():
    logger.debug(items)

if args.requirements:
    logger.debug('Checking system requirements for NLPCraft to run:')
    try:
        # Check if conda is installed
        subprocess.call(['conda', '-V'])
        conda_version: str = subprocess.run(['conda', '-V'], stdout=subprocess.PIPE).stdout.decode('utf-8').split()[1]
        logger.debug(f'Conda version of your system is: {conda_version}')
        # Checking conda version
        if LooseVersion(str(conda_version)) < LooseVersion(str(nc_setup_conf['MIN_CONDA_VERSION'])):
            raise SystemExit(f'[ERROR] Invalid conda version. The version you have is {float(conda_version)} .'
                               'See requirements: https://nlpcraft.apache.org/download.html')
    except FileNotFoundError:
        logger.error('Conda is not installed. See requirements: https://nlpcraft.apache.org/download.html')
        raise SystemExit('[ERROR] Conda is not installed. See requirements: https://nlpcraft.apache.org/download.html')
    except Exception as err:
        logger.error(err)
        logger.error('Conda verification error. See requirements: https://nlpcraft.apache.org/download.html')
        raise SystemExit(f'[ERROR] Conda verification error. See requirements: https://nlpcraft.apache.org/download.html')

    logger.info(f'Log: {log_file_path}')


if args.setupdir:
    user_home = os.path.expanduser("~")
    nlpcraft_python_path = os.path.join(user_home, '.nlpcraft-python')
    logger.debug(f'Creating directory for NLPCraft Python Environment: {nlpcraft_python_path}')
    if os.path.exists(nlpcraft_python_path):
        logger.warning(f'NLPCraft Python folder already exists at: {nlpcraft_python_path}')
    else:
        os.mkdir(nlpcraft_python_path)
        logger.debug(f'NLPCraft Python folder created.')

    logger.info(f'Log: {log_file_path}')

if args.pytorch:
    """Pytorch Installation: OS Independent"""
    def pytorch_osx_install():
        subprocess.check_call([sys.executable, "-m", "pip", "install", 'torch', 'torchvision', 'torchaudio'])

    def pytorch_win_install():
        subprocess.check_call([sys.executable, "-m", "pip", "install", 'torch==1.9.0+cu111', 'torchvision==0.10.0+cu111',
                               'torchaudio===0.9.0', '-f', 'https://download.pytorch.org/whl/torch_stable.html'])

    def pytorch_linux_install():
        subprocess.check_call([sys.executable, "-m", "pip", "install", 'torch==1.9.0+cu111', 'torchvision==0.10.0+cu111',
                               'torchaudio==0.9.0', '-f', 'https://download.pytorch.org/whl/torch_stable.html'])

    logger.debug(f'OS detected as {sys.platform}')
    logger.debug(f'Python executable at: {sys.executable}')
    logger.debug(f'NLPCraft attempting to install PyTorch for your OS {sys.platform} ......')

    try:
        if sys.platform == "linux" or sys.platform == "linux2":
            pytorch_linux_install()
        elif sys.platform == "darwin":
            pytorch_osx_install()
        elif sys.platform == "win32":
            pytorch_win_install()
    except Exception as err:
        logger.error(err)
        logger.critical('PyTorch was not installed successfully. Please visit https://pytorch.org/get-started/locally/'
                        f"and install it manually into the {os.path.join(os.path.expanduser('~'), '.nlpcraft-python')}"
                        ' conda environment.\nFor more information see nlpcraft/src/main/python/ctxword/README.md')

    logger.info(f'Log: {log_file_path}')
