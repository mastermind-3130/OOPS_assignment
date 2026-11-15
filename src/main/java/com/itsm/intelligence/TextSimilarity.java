package com.itsm.intelligence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Stateless keyword-based Jaccard similarity. */
public final class TextSimilarity {
    private static final Set<String> STOP_WORDS = Set.of(
        "the", "and", "for", "not", "are", "with", "this", "that",
        "have", "from", "cannot", "unable", "when", "please", "into",
        "there", "does", "was", "were", "been", "but", "can"
    );

    private TextSimilarity() { }

    public static Set<String> keywords(String text) {
        if (text == null || text.isBlank()) return Set.of();
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
            .filter(word -> word.length() >= 3)
            .filter(word -> !STOP_WORDS.contains(word))
            .collect(Collectors.toSet());
    }

    public static double similarity(String a, String b) {
        Set<String> first = keywords(a);
        Set<String> second = keywords(b);
        if (first.isEmpty() || second.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(first);
        intersection.retainAll(second);
        Set<String> union = new HashSet<>(first);
        union.addAll(second);
        return (double) intersection.size() / union.size();
    }
}
