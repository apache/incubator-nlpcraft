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

from pyhocon import ConfigFactory
from flask import Flask
from ctxword.ctxserver_blueprint import blueprint_ctxserver
from nc_tokenizers.nc_spacy.spacy_tokenizer_blueprint import blueprint_spacy

import os
import logging


logging.basicConfig(level=logging.DEBUG,
                    format=f'[%(asctime)s]: {os.getpid()} %(levelname)s %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')

logger = logging.getLogger()

CURRENT_FILE_NAME = os.path.abspath(__file__)
CURRENT_DIR = os.path.dirname(CURRENT_FILE_NAME)

conf = ConfigFactory.parse_file(os.path.join(CURRENT_DIR, os.pardir, 'resources', 'nlpcraft.conf'))

NC_TOKEN_PROVIDERS = conf.get_string('nlpcraft.server.tokenProviders').split(',')

app = Flask(__name__)

# Registering blueprints
logger.debug('Registering ctxserver blueprint')
app.register_blueprint(blueprint_ctxserver, url_prefix="/api/v1/ctxserver")

if 'spacy' in NC_TOKEN_PROVIDERS:
    logger.debug("Tokenizer: 'spacy' is present. Starting up endpoint.")
    app.register_blueprint(blueprint_spacy, url_prefix="/api/v1/spacy")

if __name__ == '__main__':
    # TODO add port to config
    logger.debug("Starting up REST API (Python)")
    app.run(host='0.0.0.0', port=5000)
