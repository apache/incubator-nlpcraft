<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

<img src="https://nlpcraft.apache.org/images/nlpcraft_logo_black.gif" height="80px">
<br>

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)
[![build](https://github.com/apache/incubator-nlpcraft/workflows/build/badge.svg)](https://github.com/apache/incubator-nlpcraft/actions)
[![Documentation Status](https://img.shields.io/:docs-latest-green.svg)](https://nlpcraft.apache.org/docs.html)
[![Gitter](https://badges.gitter.im/apache-nlpcraft/community.svg)](https://gitter.im/apache-nlpcraft/community)

### Overview
`ctxword` module provides Python-based internal tool for finding a contextually related words for a given word from the
input sentence. This utility provides a single REST endpoint and is based on Google's [BERT](https://github.com/google-research/bert) 
models and Facebook's [fasttext](https://fasttext.cc/) library.

### Dependencies
 * **Linux/MacOS**: run `bin/install_dependencies.sh` script.  
 * **Windows**: read `WINDOWS_SETUP.md` in the same folder.

### Start REST Server
 * **Linux/MacOS**: `$ bin/start_server.sh`  
 * **Windows**: read `WINDOWS_SETUP.md` in the same folder.
 
 NOTE: on the 1st start the server will try to load compressed BERT model which is not yet available. It will
 then download this library and compress it which will take a several minutes and may require 10GB+ of 
 available memory. Subsequent starts will skip this step, and the server will start much faster.

### REST API
##### /suggestions (POST)
Returns word replacement suggestions for following word in the sentence.  
Accepts JSON object with fields:
* "sentences": List of sentences information. Each sentence encoded as object, argument `"text"` of which is sentence itself,
  `"indexes"` are positions in the sentence of the words to generate suggestions for.  
  Example: ``"sentences": [{"text": "foo bar baz", "indexes": [0, 2]}, {"text": "sample second sentence", indexes:[1]}]``
* "simple" (Optional, default to false): If set to true omits verbose data.  
* "limit" (Optional, default to 10): Sets limit of result words number. 
* "min_score" (Optional, default to 0): Sets minimal requirement of total score.
* "min_ftext" (Optional, default to 0.25): Sets minimal requirement of FastText score.  
* "min_bert" (Optional, default to 0): Sets minimal requirement of Bert score.  
Endpoint returns object with elements `[word, total score, FastText score, Bert score]`

### `bin/suggest.sh`
Simple request with single sentence could be made with a script, e.g.  
`$ bin/suggest.sh "what is the chance of rain tomorrow?" 5`  
Would find suggestions for word "rain" in this sentence.    

### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px">
