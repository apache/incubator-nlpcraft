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

To install dependencies:  
`$ bin/install_dependencies.sh`  
For Windows install please read WINDOWS_SETUP.md
To start server (is not intendeed for production):    
`$ bin/start_server.sh`  

### Routes
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

Simple request with single sentence could be made with a script, e.g.  
`$ bin/suggest.sh "what is the chance of rain tomorrow?" 5`  
Would find suggestions for word "rain" in this sentence.    
