package org.apache.nlpcraft.probe.mgrs.nlp.enrichers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SyTest {
    public static void main(String[] args) {
//        List<List<String>> words = asList(
//            asList("A", "B", "C"),
//            asList("B", "C", "D"),
//            asList("B", "D")
//        );
        List<List<String>> words = asList(
            asList("A", "B"),
            asList("C", "B"),
            asList("D", "E"),
            asList("D", "F"),
            asList("G", "H"),
            asList("I", "H"),
            asList("J", "K"),
            asList("L", "K"),
            asList("M", "N"),
            asList("M", "O"),
            asList("P", "Q"),
            asList("P", "R"),
            asList("S", "T"),
            asList("S", "U"),
            asList("V", "W"),
            asList("X", "W")
            ,
            asList("Y", "Z"),
            asList("A1", "A2"),
            asList("A3", "A3"),
            asList("A4", "A5", "A6")
        );

        System.out.println(
            "Dictionary size:"
                + words.stream()
                .flatMap(Collection::stream)
                .distinct()
                .count()
        );

        System.out.println("===== Performance =====");

        for (int i = 0; i < 1; i++) {
            long t = System.currentTimeMillis();

            Set<Set<String>> combos = findCombos(words);



            System.out.println("Iteration " + i + " Time: " + (System.currentTimeMillis() - t) + ", resCnt=" + combos.size());
        }

        if (true) {
            return;
        }

        Set<Set<String>> combos = findCombos(words);

        System.out.println();
        System.out.println("===== Result =====");
        System.out.println("Total combos: " + combos.size());
        System.out.println();
//        combos.stream()
//            .sorted(Comparator.comparing(Collection::size))
//            .forEach(combo ->
//                print(words, combo)
//            );
    }

    public static <T extends Comparable<T>> Set<Set<T>> findCombos(List<List<T>> inp) {


        List<List<T>> uniqueInp = inp.stream()
            .filter(row -> inp.stream().noneMatch(it -> it != row && it.containsAll(row)))
            .map(i -> i.stream().distinct().sorted().collect(toList()))
            .collect(toList());

        // Build dictionary of unique words.
        List<T> dict = uniqueInp.stream()
            .flatMap(Collection::stream)
            .distinct()
            .sorted()
            .collect(toList());

        if (dict.size() > Integer.SIZE) {
            // Note: Power set of 32 words results in 4294967296 combinations.
            throw new IllegalArgumentException("Can handle more than " + Integer.SIZE + " unique words in the dictionary.");
        }

        // Convert words to bitmasks (each bit corresponds to an index in the dictionary).
        int[] wordBits = uniqueInp.stream()
            .sorted(Comparator.comparingInt(List::size))
            .mapToInt(row -> wordsToBits(row, dict))
            .toArray();

        // Cache words count per row.
        int[] wordCounts = uniqueInp.stream()
            .sorted(Comparator.comparingInt(List::size))
            .mapToInt(List::size)
            .toArray();

        int min = 1;
        int max = (int)Math.pow(2, dict.size()) - 1;

        int batchFactor = 100;
        int threads = 13;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch cdl = new CountDownLatch(batchFactor);

        int divRes = max / batchFactor;
        int divRest = max % batchFactor;

        int to = 0;

        List<Integer> result = new CopyOnWriteArrayList<>();

        for (int k = 0; k < batchFactor; k++) {
            to += divRes;

            if (k == divRes - 1) {
                to += divRest;
            }

            int toFinal = to;
            int fromFinal = min + k * divRes;

            pool.execute(
                () -> {
                    List<Integer> locRes = new ArrayList<>();

                    for (int comboBits = fromFinal; comboBits < toFinal; comboBits++) {
                        boolean match = true;

                        // For each input row we check if subtracting the current combination of words
                        // from the input row would give us the expected result.
                        for (int j = 0; j < wordBits.length; j++) {
                            // Get bitmask of how many words can be subtracted from the row.
                            int commonBits = wordBits[j] & comboBits;

                            int wordsToRemove = Integer.bitCount(commonBits);

                            // Check if there are more than 1 word remaining after subtraction.
                            if (wordCounts[j] - wordsToRemove > 1) {
                                // Skip this combination.
                                match = false;

                                break;
                            }
                        }

                        if (match && !includes(comboBits, locRes)) {
                            locRes.add(comboBits);
                        }
                    }

                    result.addAll(locRes);

                    cdl.countDown();
                }
            );
        }

// Iterate over the power set.

        //pool.shutdown();
        try {
            cdl.await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            //pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Convert found results from bitmasks back to words.
        TreeSet<Set<T>> treeSet = new TreeSet<>(Comparator.comparingInt(Set::size));

        treeSet.addAll(result.stream().map(bits -> bitsToWords(bits, dict)).collect(toSet()));

        Set<Set<T>> normCombs = new HashSet<>();

        for (Set<T> set : treeSet) {
            boolean b = true;

            for (Set<T> added : normCombs) {
                if (added.containsAll(set)) {
                    b = false;

                    break;
                }
            }

            if (b) {
                normCombs.add(set);
            }
        }

        return normCombs;

    }

    private static <T> Set<Set<T>> squeeze(Set<Set<T>> combs) {
        Set<Set<T>> normCombs = new HashSet<>();

        combs.stream().sorted(Comparator.comparingInt(Set::size)).forEach(comb -> {
            // Skips already added shorter variants.
            if (normCombs.stream().filter(comb::containsAll).findAny().isEmpty()) {
                normCombs.add(comb);
            }
        });

        return normCombs;
    }


    private static boolean includes(int bits, List<Integer> allBits) {
        for (int existing : allBits) {
            if ((bits & existing) == existing) {
                return true;
            }
        }

        return false;
    }

    private static <T> int wordsToBits(List<T> words, List<T> dict) {
        int bits = 0;

        for (int i = 0; i < dict.size(); i++) {
            if (words.contains(dict.get(i))) {
                bits |= 1 << i;
            }
        }

        return bits;
    }

    private static <T> Set<T> bitsToWords(int bits, List<T> dict) {
        Set<T> words = new HashSet<>(Integer.bitCount(bits));

        for (int i = 0; i < dict.size(); i++) {
            if ((bits & 1 << i) != 0) {
                words.add(dict.get(i));
            }
        }

        return words;
    }

    private static void print(List<List<String>> inp, List<String> combo) {
        System.out.println("==== " + combo + "(" + combo.size() + ')');
        inp.stream().forEach(row -> {
            Set<String> s = new TreeSet<>(row);
            s.removeAll(combo);
            System.out.println(s);
        });
    }
}
