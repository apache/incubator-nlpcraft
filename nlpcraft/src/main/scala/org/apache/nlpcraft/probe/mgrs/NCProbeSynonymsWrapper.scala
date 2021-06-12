/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.probe.mgrs

/**
  * Synonyms sequence holder.
  */
case class NCProbeSynonymsWrapper(
    txtDirectSynonyms: Map[String, NCProbeSynonym],
    txtNotDirectSynonyms: Map[String, NCProbeSynonym],
    notTxtDirectSynonyms: Seq[NCProbeSynonym],
    notTxtNotDirectSynonyms: Seq[NCProbeSynonym],
    count: Int
)

object NCProbeSynonymsWrapper {
    def apply(syns: Seq[NCProbeSynonym]): NCProbeSynonymsWrapper = {
        // When it converted to map, more important values will be last and previous
        // (less important elements) will be overridden.
        val txtSyns = syns.filter(_.isTextOnly).sorted

        // Required order by importance.
        val other = syns.filter(!_.isTextOnly).sorted.reverse

        def filter(seq: Seq[NCProbeSynonym], direct: Boolean): Seq[NCProbeSynonym] = seq.filter(_.isDirect == direct)
        def toMap(seq: Seq[NCProbeSynonym]): Map[String, NCProbeSynonym] = seq.map(s => s.stems -> s).toMap

        NCProbeSynonymsWrapper(
            txtDirectSynonyms = toMap(filter(txtSyns, direct = true)),
            txtNotDirectSynonyms = toMap(filter(txtSyns, direct = false)),
            notTxtDirectSynonyms = filter(other, direct = true),
            notTxtNotDirectSynonyms = filter(other, direct = false),
            count = syns.size
        )
    }
}