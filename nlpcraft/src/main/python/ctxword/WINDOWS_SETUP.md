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

### Windows Setup
To set up `ctxword` module under Windows, you would need to repeat steps from `src/main/python/ctxword/bin/install_dependencies.sh` script:
 1. Before starting, make sure you have the following installed:
    - `python3`
    - `pip3` (included with the latest versions of python3)
    - `git`
 2. Download pre-trained [FastText](https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.en.300.bin.gz) model.
 3. Extract GZIP model into `data` folder (i.e. `/nlpcraft/src/main/python/ctxword/data`).
 4. Git clone [FastText](https://github.com/facebookresearch/fastText.git) into some temporary folder.
 5. Ensure that you have [Microsoft Windows 10 SDK](https://developer.microsoft.com/en-US/windows/downloads/windows-10-sdk/) installed. Step 6. will fail unless this SDK is installed.
 6. Run `pip3 install fastText` (where `fastText` is root of the cloned git repository from the previous step).
 7. Install PyTorch depending on whether you have NVIDIA CUDA support:
    - Without CUDA support: `pip3 install torch==1.6.0+cpu -f https://download.pytorch.org/whl/torch_stable.html`
    - With CUDA support: `pip3 install torch==1.6.0 -f https://download.pytorch.org/whl/torch_stable.html`
 8. Install the rest of required python packages from `bin/py_requirements` by running `pip3 install -r bin/py_requirements`
 9. You can remove the local clone of FastText git repository after its setup is finished.
 
 ### Copyright
 Copyright (C) 2020 Apache Software Foundation
 
 <img src="https://www.apache.org/img/ASF20thAnniversary.jpg" height="64px" alt="ASF Logo"> 
