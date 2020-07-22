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
To setup project for work under Windows, you would need to repeat steps from `bin/install_dependencies.sh`
1. Before starting, make sure that you have python3, pip3 (included in latest versions of python3) and git installed.
2. [Download pre-trained FastText model](https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.en.300.bin.gz)
3. Extract achieve into `data` folder (`/nlpcraft/src/main/python/ctxword/data`)
4. Clone [FastText repository](https://github.com/facebookresearch/fastText.git)
5. Install it with pip running `pip3 install fastText` (where `fastText` is root of cloned git repository)
6. Install the rest of required python packages from `bin/py_requirements` running `pip3 install -r bin/py_requirements`  

FastTest repository may be removed after setup is finished. 
