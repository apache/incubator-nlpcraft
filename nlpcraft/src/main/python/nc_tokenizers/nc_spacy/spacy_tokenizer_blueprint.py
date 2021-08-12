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

import urllib.parse

import spacy
from flask import Blueprint
from flask import request
from flask import jsonify

import logging

logger = logging.getLogger(__name__)

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s', level=logging.DEBUG)

blueprint_spacy = Blueprint("blueprint_spacy", __name__)

#
# This is an example of spaCy REST proxy. It only should be used during development.
# For production usage we recommend WSGI server instead.
#

#
# Add your own or modify spaCy libraries here.
# By default, the English model 'en_core_web_sm' is loaded.
#
# TODO: Add the model to the config file
spacy_model_name = "en_core_web_sm"
try:
    nlp = spacy.load(spacy_model_name)
except Exception as err:
    logger.error(f"Error loading spaCy model: {spacy_model_name}")
    logger.error(err)


@blueprint_spacy.route('/spacy', methods=['POST'])
def get_tokens():
    """TODO: documentation"""

    doc = nlp(urllib.parse.unquote_plus(request.args.get('text')))
    res = []
    for e in doc.ents:
        meta = {}

        # Change the following two lines to implements your own logic for
        # filling up meta object with custom user attributes. 'meta' should be a dictionary (JSON)
        # with types 'string:string'.
        for key in e._.span_extensions:
            meta[key] = e._.__getattr__(key)

        res.append(
            {
                "text": e.text,
                "from": e.start_char,
                "to": e.end_char,
                "ner": e.label_,
                "vector": str(e.vector_norm),
                "sentiment": str(e.sentiment),
                "meta": meta
            }
        )

    return jsonify(res)
