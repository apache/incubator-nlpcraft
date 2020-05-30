/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.sort

import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.NCTestSortTokenType._
import org.apache.nlpcraft.probe.mgrs.nlp.enrichers.{
    NCEnricherBaseSpec,
    NCTestNlpToken ⇒ nlp,
    NCTestSortToken ⇒ srt,
    NCTestUserToken ⇒ usr
}
import org.junit.jupiter.api.Test

/**
 * Sort enricher test.
 */
class NCEnricherSortSpec extends NCEnricherBaseSpec {
    /**
     *
     * @throws Exception
     */
    @Test
    def test(): Unit =
        runBatch(
            _ ⇒ checkExists(
                "sort A",
                srt(text = "sort", typ = SUBJ_ONLY, note = "A", index = 1),
                usr("A", "A")
            ),
            _ ⇒ checkExists(
                "sort A by A",
                srt(text = "sort", subjNote = "A", subjIndex = 1, byNote = "A", byIndex = 3),
                usr(text = "A", id = "A"),
                nlp(text = "by", isStop = true),
                usr(text = "A", id = "A")
            ),
            _ ⇒ checkExists(
                "sort A, C by A, C",
                srt(text = "sort", subjNotes = Seq("A", "C"), subjIndexes = Seq(1, 3), byNotes = Seq("A", "C"), byIndexes = Seq(5, 7)),
                usr(text = "A", id = "A"),
                nlp(text = ",", isStop = true),
                usr(text = "C", id = "C"),
                nlp(text = "by", isStop = true),
                usr(text = "A", id = "A"),
                nlp(text = ",", isStop = true),
                usr(text = "C", id = "C")
            ),
            _ ⇒ checkExists(
                "sort A C by A C",
                srt(text = "sort", subjNotes = Seq("A", "C"), subjIndexes = Seq(1, 2), byNotes = Seq("A", "C"), byIndexes = Seq(4, 5)),
                usr(text = "A", id = "A"),
                usr(text = "C", id = "C"),
                nlp(text = "by", isStop = true),
                usr(text = "A", id = "A"),
                usr(text = "C", id = "C")
            ),
            _ ⇒ checkExists(
                "sort A B by A B",
                srt(text = "sort", subjNotes = Seq("A", "B"), subjIndexes = Seq(1, 2), byNotes = Seq("A", "B"), byIndexes = Seq(4, 5)),
                usr(text = "A", id = "A"),
                usr(text = "B", id = "B"),
                nlp(text = "by", isStop = true),
                usr(text = "A", id = "A"),
                usr(text = "B", id = "B")
            ),
            _ ⇒ checkExists(
                "sort A B by A B",
                srt(text = "sort", subjNote = "AB", subjIndex = 1, byNote = "AB", byIndex = 3),
                usr(text = "A B", id = "AB"),
                nlp(text = "by", isStop = true),
                usr(text = "A B", id = "AB")
            ),
            _ ⇒ checkExists(
                "A classify",
                usr(text = "A", id = "A"),
                srt(text = "classify", typ = SUBJ_ONLY, note = "A", index = 0)
            ),
            _ ⇒ checkExists(
                "the A the classify",
                nlp(text = "the", isStop = true),
                usr(text = "A", id = "A"),
                nlp(text = "the", isStop = true),
                srt(text = "classify", typ = SUBJ_ONLY, note = "A", index = 1)
            ),
            _ ⇒ checkExists(
                "segment A by top down",
                srt(text = "segment", typ = SUBJ_ONLY, note = "A", index = 1, asc = false),
                usr(text = "A", id = "A"),
                nlp(text = "by top down", isStop = true)
            ),
            _ ⇒ checkExists(
                "segment A in bottom up order",
                srt(text = "segment", typ = SUBJ_ONLY, note = "A", index = 1, asc = true),
                usr(text = "A", id = "A"),
                nlp(text = "in bottom up order", isStop = true)
            ),
            // `by` is redundant word here
            _ ⇒ checkExists(
                "segment A by in bottom up order",
                srt(text = "segment", typ = SUBJ_ONLY, note = "A", index = 1),
                usr(text = "A", id = "A"),
                nlp(text = "by"),
                nlp(text = "in"),
                nlp(text = "bottom"),
                nlp(text = "up"),
                nlp(text = "order")
            ),
            _ ⇒ checkExists(
                "the segment the A the in bottom up the order the",
                nlp(text = "the", isStop = true),
                srt(text = "segment", typ = SUBJ_ONLY, note = "A", index = 3, asc = true),
                nlp(text = "the", isStop = true),
                usr(text = "A", id = "A"),
                nlp(text = "the in bottom up the order the", isStop = true)
            ),
            _ ⇒ checkExists(
                "the segment the A the by bottom up the order the",
                nlp(text = "the", isStop = true),
                srt(text = "segment", typ = SUBJ_ONLY, note = "A", index = 3, asc = true),
                nlp(text = "the", isStop = true),
                usr(text = "A", id = "A"),
                nlp(text = "the by bottom up the order the", isStop = true)
            ),
            _ ⇒ checkExists(
                "A classify",
                usr(text = "A", id = "A"),
                srt(text = "classify", typ = SUBJ_ONLY, note = "A", index = 0)
            ),
            _ ⇒ checkAll(
                "A B classify",
                Seq(
                    usr(text = "A B", id = "AB"),
                    srt(text = "classify", typ = SUBJ_ONLY, note = "AB", index = 0)
                ),
                Seq(
                    usr(text = "A", id = "A"),
                    usr(text = "B", id = "B"),
                    srt(text = "classify", subjNotes = Seq("A", "B"), subjIndexes = Seq(0, 1))
                ),
                Seq(
                    usr(text = "A", id = "A"),
                    usr(text = "B", id = "B"),
                    srt(text = "classify", subjNotes = Seq("B"), subjIndexes = Seq(1))
                )
            ),
            _ ⇒ checkAll(
                "D classify",
                Seq(
                    usr(text = "D", id = "D1"),
                    srt(text = "classify", typ = SUBJ_ONLY, note = "D1", index = 0)
                ),
                Seq(
                    usr(text = "D", id = "D2"),
                    srt(text = "classify", typ = SUBJ_ONLY, note = "D2", index = 0)
                )
            ),
            _ ⇒ checkAll(
                "sort by A",
                Seq(
                    srt(text = "sort by", typ = BY_ONLY, note = "A", index = 1),
                    usr(text = "A", id = "A")
                )
            ),
            _ ⇒ checkExists(
                "organize by A, B top down",
                srt(text = "organize by", byNotes = Seq("A", "B"), byIndexes = Seq(1, 3), asc = Some(false)),
                usr(text = "A", id = "A"),
                nlp(text = ",", isStop = true),
                usr(text = "B", id = "B"),
                nlp(text = "top down", isStop = true)
            ),
            _ ⇒ checkExists(
                "organize by A, B from bottom up order",
                srt(text = "organize by", byNotes = Seq("A", "B"), byIndexes = Seq(1, 3), asc = Some(true)),
                usr(text = "A", id = "A"),
                nlp(text = ",", isStop = true),
                usr(text = "B", id = "B"),
                nlp(text = "from bottom up order", isStop = true)
            ),
            _ ⇒ checkExists(
                "organize by A, B the descending",
                srt(text = "organize by", byNotes = Seq("A", "B"), byIndexes = Seq(1, 3), asc = Some(false)),
                usr(text = "A", id = "A"),
                nlp(text = ",", isStop = true),
                usr(text = "B", id = "B"),
                nlp(text = "the descending", isStop = true)
            ),
            _ ⇒ checkExists(
                "organize by A, B, asc",
                srt(text = "organize by", byNotes = Seq("A", "B"), byIndexes = Seq(1, 3), asc = Some(true)),
                usr(text = "A", id = "A"),
                nlp(text = ",", isStop = true),
                usr(text = "B", id = "B"),
                nlp(text = ", asc", isStop = true)
            )
        )
}
