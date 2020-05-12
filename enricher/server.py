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
from flask import abort
from flask import Response
from bertft import Pipeline

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s', level=logging.DEBUG)

app = Flask(__name__)

pipeline = Pipeline()


@app.route('/', methods=['POST'])
def main():
    if not request.is_json:
        abort(Response("Json expected"))

    json = request.json
    sentence = json['sentence']
    upper = None if 'upper' not in json else json['upper']
    lower = None if 'lower' not in json else json['lower']
    positions = None if upper is None or lower is None else [lower, upper]
    data = pipeline.do_find(sentence, positions)
    if 'simple' not in json or not json['simple']:
        json_data = data.to_json(orient='table', index=False)
    else:
        json_data = data['word'].to_json(orient='values')
    return app.response_class(response=json_data, status=200, mimetype='application/json')
