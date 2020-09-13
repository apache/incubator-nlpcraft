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

<img src="https://nlpcraft.apache.org/images/nlpcraft_logo_black.gif" height="80px" alt="">
<br>

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)
[![build](https://github.com/apache/incubator-nlpcraft/workflows/build/badge.svg)](https://github.com/apache/incubator-nlpcraft/actions)
[![Documentation Status](https://img.shields.io/:docs-latest-green.svg)](https://nlpcraft.apache.org/docs.html)
[![Gitter](https://badges.gitter.im/apache-nlpcraft/community.svg)](https://gitter.im/apache-nlpcraft/community)

### Overview
`ctxword` module provides Python-based internal tool for finding a contextually related words for a given word from the
input sentence. This utility provides a single REST endpoint and is based on Google's [BERT](https://github.com/google-research/bert) 
models and Facebook's [FastText](https://fasttext.cc/) library.

### Dependencies
To install necessary dependency:
 * **Linux/MacOS**: run `src/main/python/ctxword/bin/install_dependencies.sh` script.  
 * **Windows**: read `WINDOWS_SETUP.md` file for manual installation.

### Start REST Server
To start 'ctxword' module REST server:
 * Run `src/main/python/ctxword/bin/start_server.{sh|cmd}` script.  
 
NOTE: on the 1st start the server will try to load compressed BERT model which is not yet available. It will
then download this library and compress it which will take a several minutes and may require 10 GB+ of 
available memory. Subsequent starts will skip this step, and the server will start much faster.

### REST API
Once the REST server is started you can issue REST calls to get suggestions for the contextual related words.
REST server provides a single `application/json` endpoint:
 
##### `/suggestions` (POST)
Returns contextual word replacement(s) for the specified word in the input sentence. Accepts JSON object parameter 
with the following fields:
 * `"sentences"`
   - List of sentences. Each sentence encoded as object with the following fields:
     - `"text"` represents the sentence text.
     - `"indexes"` array of positions in the sentence of the words to generate suggestions for.  
 * `"simple"` 
   - Optional, defaults to `false`. If set to `true`, returns simple objects. If set to `false` returns
   expanded objects with total, BERT and fasttext scores.  
 * `"limit"` 
   - Optional, defaults to 10. Sets limit of result words number. 
 * `"min_score"` 
   - Optional, defaults to 0. Sets the minimal requirement for total score.
 * `"min_ftext"` 
   - Optional, default to 0.25. Sets the minimal requirement of FastText score.  
*  `"min_bert"` 
   - Optional, default to 0. Sets the minimal requirement of Bert score.
     
Endpoint returns one or more JSON objects with the following fields (depending on `"simple"` request field):
 * If `"simple"` set to `true`: `[word1, word2, ...]`
 * If `"simple"` set to `false`:`[{word1, total_score1, ft_score1, bert_score1}, {...}]`

#### Examples
Here's the sample request and response JSON objects:
 * Request JSON: 
   - ``"simple": true, "sentences": [{"text": "foo bar baz", "indexes": [0, 2]}, {"text": "sample second sentence", indexes:[1]}]``
 * Response JSON:
   - `[["word1", "word2", "word3"]]`
 
### `suggest.{sh|cmd}`
You can use Curl-based `src/main/python/ctxword/bin/suggest.{sh|cmd}` scripts for the suggestion processing of single sentences from the command line.
Following call returns list of contextual suggestions for the 5th word (counting from zero) in the given sentence: 

```
$ bin/suggest.sh "what is the chance of rain tomorrow?" 5
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   214  100   104  100   110    954   1009 --:--:-- --:--:-- --:--:--  1963
[
    [
        "rain",
        "snow",
        "rainfall",
        "precipitation",
        "rains",
        "flooding",
        "storms",
        "raining",
        "sunshine",
        "showers"
    ]
]
```                                     

### Copyright
Copyright (C) 2020 Apache Software Foundation

<img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px">
