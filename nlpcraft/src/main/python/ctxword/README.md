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
To start server:  
`$ bin/start_server.sh`  

Server has single route in root which accepts POST json requests with parameters: 
* "sentence": Target sentence. Number of word to find synonyms for must be passed as argument
* "lower", "upper": Positions in the sentence of start and end of collocation to find synonyms for.  
Note: sentence is split via whitespaces, upper bound is inclusive. 
* "simple" (Optional): If set to true omits verbose data.  
* "limit": Sets limit of result words number.  

Simple request could be made with a script, e.g.  
`$ bin/predict.sh "what is the chance of rain tomorrow?" 5`  
Would find synonym for word "rain" in this sentence.
