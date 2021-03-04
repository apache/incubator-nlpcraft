package org.apache.nlpcraft.probe.mgrs.nlp.enrichers.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Test2 {
//    private static List<Set<String>> ROWS =
//        Arrays.asList(
//            ImmutableSet.of("A", "B", "C"),
//            ImmutableSet.of("B", "C", "D"),
//            ImmutableSet.of("B", "D")
//        );

    // Uncomment it. Works too long time. Normalized result is 256.
    private static List<Set<String>> ROWS = Arrays.asList(
        ImmutableSet.of("A", "B"),
        ImmutableSet.of("C", "B"),
        ImmutableSet.of("D", "E"),
        ImmutableSet.of("D", "F"),
        ImmutableSet.of("G", "H"),
        ImmutableSet.of("I", "H"),
        ImmutableSet.of("J", "K"),
        ImmutableSet.of("L", "K"),
        ImmutableSet.of("M", "N"),
        ImmutableSet.of("M", "O"),
        ImmutableSet.of("P", "Q"),
        ImmutableSet.of("P", "R"),
        ImmutableSet.of("S", "T"),
        ImmutableSet.of("S", "U"),
        ImmutableSet.of("V", "W"),
        ImmutableSet.of("X", "W")
    );

    private static Set<String> ALL = ROWS.stream().flatMap(Collection::stream).collect(Collectors.toSet());

    // Goal: Find minimal set of combinations with following feature.
    // After removing combination values from each row - list should contain rows with size <= 1.

    // Expected solution: [C, B], [A, C, D], [A, B, D]
    // Example:
    // list - [C, B] = {{A}, {D}, {D}}
    // list - [A, C, D] = {{B}, {B}, {B}}
    // list - [A, B, D] = {{C}, {C}, {null}}


    // Additional. Redundant solutions: [A, B, C] ([C, B] enough),  [A, B, C, D] ([A, C, D] enough) etc

    // Easiest.
    public static void main(String[] args) {
        long t = System.currentTimeMillis();

        System.out.println("1. start [time=" + (System.currentTimeMillis() - t) + ']');

        Set<Set<String>> combs = new HashSet<>();

        for (int i = 1; i < ALL.size(); i++) {
            combs.addAll(
                Sets.combinations(ALL, i).
                    stream().
                    filter(Test2::isSuitable).
                    collect(Collectors.toSet())
            );
        }

        System.out.println("2. calculated [size=" + combs.size() + ", time=" + (System.currentTimeMillis() - t) + ']');

        // Normalize variants (keeps only minimal valid subsets, see task description)
        Set<Set<String>> normCombs = squeeze(combs);

        System.out.println("3. normalized [size=" + normCombs.size() + ", time=" + (System.currentTimeMillis() - t) + ']');
        System.out.println("Norm results:" + normCombs);
    }

    private static Set<Set<String>> squeeze(Set<Set<String>> combs) {
        Set<Set<String>> normCombs = new HashSet<>();

        for (Set<String> comb : combs.stream().sorted(Comparator.comparingInt(Set::size)).collect(Collectors.toList())) {
            // Skips already added shorter variants.
            if (normCombs.stream().filter(comb::containsAll).findAny().isEmpty()) {
                normCombs.add(comb);
            }
        }
        return normCombs;
    }

    /**
     * Removes `candidate` from each row of ROWS.
     * Return true if result list doesn't contain any row with size > 1.
     * <p>
     * If ROWS is {{a, b}, {a, c}}. Candidate {a, b} - ok, candidate {a} - ok, candidate {b} - no.
     */
    private static boolean isSuitable(Set<String> candidate) {
        for (Set<String> row : ROWS) {
            Set<String> copy = new HashSet<>(row);

            copy.removeAll(candidate);

            if (copy.size() > 1) {
                return false;
            }
        }

        return true;
    }
}

