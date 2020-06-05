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


def calc_w(x, y, w):
    return x * w[0] + y * w[1]


# TODO: make Model configurable
# TODO: add type check
class Pipeline:
    def __init__(self, on_run=None):
        self.log = logging.getLogger("bertft")

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

        self.on_run = on_run

        self.log.info("Server started in %s seconds", ('{0:.4f}'.format(time.time() - start_time)))

    def find_top(self, sentence, positions, k, top_bert, bert_norm, min_ftext, weights):
        tokenizer = self.tokenizer
        model = self.model
        ft = self.ft

        self.log.debug("Input: %s", sentence)
        start_time = time.time()

        lst = sentence.split()
        lower = positions[0]
        upper = positions[1] + 1
        target = "-".join(lst[lower:upper])
        if lower == positions[1] or target in self.ft_dict:
            seqlst = lst[:lower]
            seqlst.append(tokenizer.mask_token)
            seqlst.extend(lst[upper:])
            sequence = " ".join(seqlst)
        else:
            rec = list()

            for i in range(lower, upper):
                seqlst = lst[:lower]
                seqlst.append(lst[i])
                seqlst.extend(lst[upper:])
                rec.append(
                    self.find_top(" ".join(seqlst), [lower, lower], k, top_bert, bert_norm, min_ftext, weights))

            rec = sorted(rec, key=lambda x: x.score.mean(), reverse=True)

            return rec[0]

        self.log.debug("Target word: %s; sequence: %s", target, sequence)

        input = tokenizer.encode(sequence, return_tensors="pt")
        mask_token_index = torch.where(input == tokenizer.mask_token_id)[1]

        token_logits = model(input)[0]
        mask_token_logits = token_logits[0, mask_token_index, :]

        # Filter top <top_bert> results of bert output
        topk = torch.topk(mask_token_logits, top_bert, dim=1)
        top_tokens = list(zip(topk.indices[0].tolist(), topk.values[0].tolist()))

        unfiltered = list()
        filtered = list()

        norm_d = top_tokens[bert_norm - 1][1]
        norm_k = top_tokens[0][1] - norm_d

        self.log.info("Bert finished in %s seconds", '{0:.4f}'.format(time.time() - start_time))

        # Filter bert output by <min_ftext>
        # TODO: calculate batch similarity
        for token, value in top_tokens:
            word = tokenizer.decode([token]).strip()
            norm_value = (value - norm_d) / norm_k

            sim = cosine_similarity(ft[target].reshape(1, -1), ft[word].reshape(1, -1))[0][0]

            sentence_sim = cosine_similarity(
                ft.get_sentence_vector(sentence).reshape(1, -1),
                ft.get_sentence_vector(re.sub(tokenizer.mask_token, word, sequence)).reshape(1, -1)
            )[0][0]

            # Continue only for jupyter
            if self.on_run is None and word == target:
                continue

            if sim >= min_ftext:
                filtered.append((word, value, norm_value, sim, sentence_sim, calc_w(norm_value, sim, weights)))

            unfiltered.append((word, value, norm_value, sim, sentence_sim, calc_w(norm_value, sim, weights)))

        done = (time.time() - start_time)

        kfiltered = filtered[:k]
        kunfiltered = unfiltered[:k]

        kfiltered = sorted(kfiltered, key=lambda x: -x[len(x) - 1])
        kunfiltered = sorted(kunfiltered, key=lambda x: -x[len(x) - 1])

        filtered_top = pd.DataFrame({
            'word': lget(kfiltered, 0),
            'bert': self.dget(kfiltered, 1),
            'normalized': self.dget(kfiltered, 2),
            'ftext': self.dget(kfiltered, 3),
            'ftext-sentence': self.dget(kfiltered, 4),
            'score': lget(kfiltered, 5),
        })

        if self.on_run != None:
            self.on_run(self, kunfiltered, unfiltered, filtered_top, target, tokenizer, top_tokens)

        self.log.info("Processing finished in %s seconds", '{0:.4f}'.format(done))

        return filtered_top

    def do_find(self, s, positions, limit):
        return self.find_top(s, positions, limit, 200, 200, 0.25, [1, 1])

    def dget(self, lst, pos):
        return list(map(lambda x: '{0:.2f}'.format(x[pos]), lst)) if self.on_run is not None else lget(lst, pos)
