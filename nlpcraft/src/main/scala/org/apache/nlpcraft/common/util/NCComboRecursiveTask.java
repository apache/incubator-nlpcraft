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

package org.apache.nlpcraft.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class NCComboRecursiveTask extends RecursiveTask<List<Long>> {
    private static final long THRESHOLD = (long)Math.pow(2, 20);

    private final long lo;
    private final long hi;
    private final long[] wordBits;
    private final int[] wordCounts;

    private NCComboRecursiveTask(long lo, long hi, long[] wordBits, int[] wordCounts) {
        this.lo = lo;
        this.hi = hi;
        this.wordBits = wordBits;
        this.wordCounts = wordCounts;
    }

    public static <T> List<List<T>> findCombinations(List<List<T>> inp, ForkJoinPool pool) {
        List<List<T>> uniqueInp = inp;

        // Build dictionary of unique words.
        List<T> dict = uniqueInp.stream()
            .flatMap(Collection::stream)
            .distinct()
            .collect(toList());

        System.out.println("inp=" + inp);
        System.out.println("dict=" + dict);

        if (dict.size() > Long.SIZE) {
            // Note: Power set of 64 words results in 9223372036854775807 combinations.
            throw new IllegalArgumentException("Dictionary is too long: " + dict.size());
        }

        // Convert words to bitmasks (each bit corresponds to an index in the dictionary).
        long[] wordBits = uniqueInp.stream()
            .sorted(Comparator.comparingInt(List::size))
            .mapToLong(row -> wordsToBits(row, dict))
            .toArray();

        // Cache words count per row.
        int[] wordCounts = uniqueInp.stream().sorted(Comparator.comparingInt(List::size)).mapToInt(List::size).toArray();

        // Prepare Fork/Join task to iterate over the power set of all combinations.
        int lo = 1;
        long hi = (long)Math.pow(2, dict.size());

        NCComboRecursiveTask task = new NCComboRecursiveTask(lo, hi, wordBits, wordCounts);

        final List<List<T>> res = pool.invoke(task).stream().map(bits -> bitsToWords(bits, dict)).collect(toList());

        System.out.println("res=" + res);

        return res;
    }

    @Override
    protected List<Long> compute() {
        return hi - lo <= THRESHOLD ? computeLocal() : forkJoin();
    }

    private List<Long> computeLocal() {
        List<Long> result = new ArrayList<>();

        for (long comboBits = lo; comboBits < hi; comboBits++) {
            boolean match = true;

            // For each input row we check if subtracting the current combination of words
            // from the input row would give us the expected result.
            for (int j = 0; j < wordBits.length; j++) {
                // Get bitmask of how many words can be subtracted from the row.
                long commonBits = wordBits[j] & comboBits;

                int wordsToRemove = Long.bitCount(commonBits);

                // Check if there is more than 1 word remaining after subtraction.
                if (wordCounts[j] - wordsToRemove > 1) {
                    // Skip this combination.
                    match = false;

                    break;
                }
            }

            if (match && !includes(comboBits, result)) {
                result.add(comboBits);
            }
        }

        return result;
    }

    private List<Long> forkJoin() {
        long mid = lo + hi >>> 1L;

        NCComboRecursiveTask t1 = new NCComboRecursiveTask(lo, mid, wordBits, wordCounts);
        NCComboRecursiveTask t2 = new NCComboRecursiveTask(mid, hi, wordBits, wordCounts);

        t2.fork();

        return merge(t1.compute(), t2.join());
    }

    private List<Long> merge(List<Long> l1, List<Long> l2) {
        if (l1.isEmpty()) {
            return l2;
        }
        else if (l2.isEmpty()) {
            return l1;
        }

        int size1 = l1.size();
        int size2 = l2.size();

        if (size1 == 1 && size2 > 1 || size2 == 1 && size1 > 1) {
            // Minor optimization in case if one of the lists has only one element.
            List<Long> list = size1 == 1 ? l2 : l1;
            Long val = size1 == 1 ? l1.get(0) : l2.get(0);

            if (!includes(val, list)) {
                list.add(val);
            }

            return list;
        }
        else {
            List<Long> result = new ArrayList<>(size1 + size2);

            for (int i = 0, max = Math.max(size1, size2); i < max; i++) {
                Long v1 = i < size1 ? l1.get(i) : null;
                Long v2 = i < size2 ? l2.get(i) : null;

                if (v1 != null && v2 != null) {
                    if (containsAllBits(v1, v2)) {
                        v1 = null;
                    }
                    else if (containsAllBits(v2, v1)) {
                        v2 = null;
                    }
                }

                if (v1 != null && !includes(v1, result)) {
                    result.add(v1);
                }

                if (v2 != null && !includes(v2, result)) {
                    result.add(v2);
                }
            }

            return result;
        }
    }

    private static boolean includes(long bits, List<Long> allBits) {
        for (int i = 0, size = allBits.size(); i < size; i++) {
            long existing = allBits.get(i);

            if (containsAllBits(bits, existing)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsAllBits(long bitSet1, long bitSet2) {
        return (bitSet1 & bitSet2) == bitSet2;
    }

    private static <T> long wordsToBits(List<T> words, List<T> dict) {
        long bits = 0;

        for (int i = 0; i < dict.size(); i++) {
            if (words.contains(dict.get(i))) {
                bits |= 1L << i;
            }
        }

        return bits;
    }

    private static <T> List<T> bitsToWords(long bits, List<T> dict) {
        List<T> words = new ArrayList<>(Long.bitCount(bits));

        for (int i = 0; i < dict.size(); i++) {
            if ((bits & 1L << i) != 0) {
                words.add(dict.get(i));
            }
        }

        return words;
    }
}
