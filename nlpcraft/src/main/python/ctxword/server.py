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

import logging
from flask import Flask
from flask import request
from flask import jsonify
from bertft import Pipeline

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s', level=logging.DEBUG)

app = Flask(__name__)

pipeline = Pipeline()


class ValidationException(Exception):
    def __init__(self, message):
        super().__init__(message)


@app.errorhandler(ValidationException)
def handle_bad_request(e):
    return str(e), 400


def check_condition(condition, supplier, message):
    if condition:
        return supplier()
    else:
        raise ValidationException(message)


def present(json, name):
    return check_condition(name in json, lambda: json[name],
                           "Required '" + name + "' argument is not present")


@app.route('/suggestions', methods=['POST'])
def main():
    if not request.is_json:
        raise ValidationException("Json expected")

    json = request.json

    sentences = present(json, 'sentences')
    limit = json['limit'] if 'limit' in json else 10
    min_score = json['min_score'] if 'min_score' in json else 0
    min_ftext = json['min_ftext'] if 'min_ftext' in json else 0.25
    min_bert = json['min_bert'] if 'min_bert' in json else 0

    data = pipeline.do_find(sentences, limit, min_score, min_ftext, min_bert)
    if 'simple' not in json or not json['simple']:
        return jsonify(data)
    else:
        return jsonify(list(map(lambda x: list(map(lambda y: y[0], x)), data)))
