#!/usr/bin/env python
# coding: utf-8

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

from transformers import AutoModelWithLMHead, AutoTokenizer
import logging
import torch
import re
import pandas as pd
from sklearn.metrics.pairwise import cosine_similarity
import time
from pathlib import Path
import fasttext.util
from .utils import ROOT_DIR


def lget(lst, pos):
    return list(map(lambda x: x[pos], lst))


# TODO: make Model configurable
# TODO: add type check
class Pipeline:
    def __init__(self, use_cuda=True):
        self.log = logging.getLogger("bertft")

        self.use_cuda = use_cuda and torch.cuda.is_available()

        if self.use_cuda:
            self.log.debug("CUDA is available")
            self.device = torch.device('cuda')
        else:
            self.log.warning("CUDA is not available")
            self.device = torch.device('cpu')

        start_time = time.time()
        # ft_size = 100 # ~2.6 GB
        ft_size = 200  # ~4.5 GB
        # ft_size = 300  # ~8 GB

        self.ft_size = ft_size

        def get_ft_path(n):
            return ROOT_DIR + "/data/cc.en." + str(n) + ".bin"

        cur_path = get_ft_path(ft_size)

        self.log.warning("Initializing fast text")

        if Path(cur_path).exists():
            self.log.info("Found existing model, loading.")
            ft = fasttext.load_model(cur_path)
        else:
            self.log.info("Configured model is not found. Loading default model.")
            ft = fasttext.load_model(get_ft_path(300))

            self.log.info("Compressing model")
            fasttext.util.reduce_model(ft, ft_size)

            ft.save_model(cur_path)

        self.ft = ft
        self.ft_dict = set(ft.get_words())

        self.log.info("Loading bert")
        # ~3 GB
        self.tokenizer = AutoTokenizer.from_pretrained("roberta-large")
        self.model = AutoModelWithLMHead.from_pretrained("roberta-large")

        if self.use_cuda:
            self.model.cuda()

        self.log.info("Server started in %s seconds", ('{0:.4f}'.format(time.time() - start_time)))

    def find_top(self, input_data, k, top_bert, min_ftext, weights, min_score):
        with torch.no_grad():
            tokenizer = self.tokenizer
            model = self.model
            ft = self.ft

            k = 10 if k is None else k
            min_score = 0 if min_score is None else min_score

            start_time = time.time()
            req_start_time = start_time

            sentences = list(map(lambda x: self.replace_with_mask(x[0], x[1]), input_data))

            encoded = tokenizer.batch_encode_plus(list(map(lambda x: x[1], sentences)), pad_to_max_length=True)
            input_ids = torch.tensor(encoded['input_ids'], device=self.device)
            attention_mask = torch.tensor(encoded['attention_mask'], device=self.device)

            start_time = self.print_time(start_time, "Tokenizing finished")
            forward = model(input_ids=input_ids, attention_mask=attention_mask)

            start_time = self.print_time(start_time, "Batch finished (Bert)")

            mask_token_index = torch.where(input_ids == tokenizer.mask_token_id)[1]
            token_logits = forward[0]
            mask_token_logits = token_logits[0, mask_token_index, :]

            # Filter top <top_bert> results of bert output
            topk = torch.topk(mask_token_logits, top_bert, dim=1)

            nvl = []

            for d in topk.values:
                nmin = torch.min(d)
                nmax = torch.max(d)
                nvl.append((d - nmin) / (nmax - nmin))

            start_time = self.print_time(start_time, "Bert post-processing")

            suggestions = []
            for index in topk.indices:
                lst = list(index)
                tmp = []
                for single in lst:
                    tmp.append(tokenizer.decode([single]).strip())
                suggestions.append(tuple(tmp))

            start_time = self.print_time(start_time, "Bert decoding")

            cos = torch.nn.CosineSimilarity()

            result = []

            for i in range(0, len(sentences)):
                target = sentences[i][0]
                suggest_embeddings = torch.tensor(list(map(lambda x: ft[x], suggestions[i])), device=self.device)
                targ_tenzsor = torch.tensor(ft[target], device=self.device).expand(suggest_embeddings.shape)
                similarities = cos(targ_tenzsor, suggest_embeddings)

                scores = nvl[i] * weights[0] + similarities * weights[1]

                result.append(
                    sorted(
                        filter(
                            lambda x: x[0] > min_score and x[1] > min_ftext,
                            zip(scores.tolist(), similarities.tolist(), suggestions[i], nvl[i].tolist())
                        ),
                        key=lambda x: x[0],
                        reverse=True
                    )[:k]
                )

            self.print_time(start_time, "Fast text similarities found")

            self.print_time(req_start_time, "Request processed")

            if (self.use_cuda):
                torch.cuda.empty_cache()

            return result

    def replace_with_mask(self, sentence, index):
        lst = sentence.split()

        target = lst[index]

        seqlst = lst[:index]
        seqlst.append(self.tokenizer.mask_token)
        seqlst.extend(lst[(index + 1):])

        return (target, " ".join(seqlst))

    def print_time(self, start, message):
        current = time.time()
        self.log.info(message + " in %s ms", '{0:.4f}'.format((current - start) * 1000))
        return current

    def do_find(self, data, limit, min_score):
        return self.find_top(data, limit, 100, 0.25, [1, 1], min_score)

    def dget(self, lst, pos):
        return list(map(lambda x: '{0:.2f}'.format(x[pos]), lst)) if self.on_run is not None else lget(lst, pos)
