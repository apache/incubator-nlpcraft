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

import functools
import logging
import operator
import time
from pathlib import Path
import os

import fasttext.util
import torch
from transformers import AutoModelWithLMHead, AutoTokenizer

from .utils import ROOT_DIR, download_file, gunzip

"""
Main class for processing sentences and predicting words 
"""
print(f"BERT ROOT DIR: {ROOT_DIR}")


class Pipeline:
    """
    :param use_cuda: specifies if CUDA should be used (if available) or not.
    """

    def __init__(self, use_cuda=True):
        self.log = logging.getLogger("bertft")

        self.use_cuda = use_cuda and torch.cuda.is_available()

        if self.use_cuda:
            self.log.debug("CUDA is available for Torch.")
            self.device = torch.device('cuda')
        else:
            self.log.warning("CUDA is not available for Torch.")
            self.device = torch.device('cpu')

        # TODO: Make this configurable
        DEFAULT_MODEL = 'cc.en.300.bin'
        DEFAULT_MODEL_PATH_ZIP = os.path.join(ROOT_DIR, 'data', 'cc.en.300.bin.gz')
        DEFAULT_MODEL_PATH = os.path.join(ROOT_DIR, 'data', DEFAULT_MODEL)

        # TODO: Add link to config
        if not os.path.isfile(DEFAULT_MODEL_PATH_ZIP):
            self.log.debug(f'Default model not found. Downloading {DEFAULT_MODEL}')
            download_file(dl_url='https://dl.fbaipublicfiles.com/fasttext/vectors-crawl/cc.en.300.bin.gz',
                          save_path=DEFAULT_MODEL_PATH_ZIP)

        if os.path.isfile(DEFAULT_MODEL_PATH_ZIP) and not os.path.isfile(DEFAULT_MODEL_PATH):
            self.log.debug(f'Default model zip file found. Gzip decompressing: {DEFAULT_MODEL}')
            gunzip(source_filepath=DEFAULT_MODEL_PATH_ZIP, dest_filepath=DEFAULT_MODEL_PATH)

        start_time = time.time()
        # ft_size = 100 # ~2.6 GB
        ft_size = 200  # ~4.5 GB
        # ft_size = 300  # ~8 GB

        self.ft_size = ft_size

        def get_ft_path(n):
            return ROOT_DIR + "/data/cc.en." + str(n) + ".bin"

        cur_path = get_ft_path(ft_size)

        self.log.info("Initializing fast text")

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

    """
    Finds top words suggestions for provided data with given parameters
    :param: input_data list of lists, first element is sentence and elements from second to last are indexes 
            in the sentence of words to find synonyms for
    :param k limits number of top words
    :param top_bert limits number of Bert suggestions
    :param min_ftext minimal FastText score is required for word to get 
    :param weights array of Bert and FastText score multipliers
    :param min_score minimal FastText score is required for word to get 
    :param min_bert minimal Bert score is required for word to get 
    """

    def find_top(self, input_data, k, top_bert, min_ftext, weights, min_score, min_bert):
        with torch.no_grad():
            tokenizer = self.tokenizer
            model = self.model
            ft = self.ft

            start_time = time.time()
            req_start_time = start_time

            sentences = functools.reduce(
                operator.concat,
                (map(lambda x: self.replace_with_mask(x[0], x[1:]), input_data))
            )

            encoded = tokenizer.batch_encode_plus(list(map(lambda x: x[1], sentences)), pad_to_max_length=True)
            input_ids = torch.tensor(encoded['input_ids'], device=self.device)
            attention_mask = torch.tensor(encoded['attention_mask'], device=self.device)

            start_time = self.print_time(start_time, "Tokenizing finished")
            forward = model(input_ids=input_ids, attention_mask=attention_mask)

            start_time = self.print_time(start_time, "Batch finished (Bert)")

            mask_token_index = torch.where(input_ids == tokenizer.mask_token_id)[1]
            token_logits = forward[0]
            tmp = []

            for i in range(0, len(mask_token_index)):
                tmp.append(token_logits[i][mask_token_index[i]])

            mask_token_logits = torch.stack(tmp)

            # Filter top <top_bert> results of bert output
            topk = torch.topk(mask_token_logits, top_bert, dim=1)

            nvl = []

            for d in topk.values:
                nmin = torch.min(d)
                nmax = torch.max(d)
                nvl.append((d - nmin) / (nmax - nmin))

            start_time = self.print_time(start_time, "Bert post-processing.")

            suggestions = []
            for index in topk.indices:
                lst = list(index)
                tmp = []
                for single in lst:
                    tmp.append(tokenizer.decode([single]).strip())
                suggestions.append(tuple(tmp))

            start_time = self.print_time(start_time, "Bert decoding.")

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
                            lambda x: x[1] > min_score and x[2] > min_ftext and x[3] > min_bert,
                            zip(suggestions[i], scores.tolist(), similarities.tolist(), nvl[i].tolist())
                        ),
                        key=lambda x: x[1],
                        reverse=True
                    )[:k]
                )

            self.print_time(start_time, "Fast text similarities found.")

            self.print_time(req_start_time, "Request processed.")

            if self.use_cuda:
                torch.cuda.empty_cache()

            return result

    """
    Replaces words in sentence with mask
    :param sentence target
    :param indexes of words to replace 
    """

    def replace_with_mask(self, sentence, indexes):
        lst = sentence.split()

        result = []

        for index in indexes:
            target = lst[index]

            seqlst = lst[:index]
            seqlst.append(self.tokenizer.mask_token)
            seqlst.extend(lst[(index + 1):])

            result.append((target, " ".join(seqlst)))

        return result

    def print_time(self, start, message):
        current = time.time()
        self.log.info(message + " in %s ms", '{0:.4f}'.format((current - start) * 1000))
        return current

    def do_find(self, data, limit, min_score, min_ftext, min_bert):
        return self.find_top(data, limit, 100, min_ftext, [1, 1], min_score, min_bert)
