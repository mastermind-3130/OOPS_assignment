package com.itsm.intelligence;

import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Deterministic keyword-based incident classification. */
@Component
public class CategorySuggester {
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYWORDS.put("NETWORK", List.of("vpn", "wifi", "network", "internet", "connection", "lan", "router", "dns"));
        CATEGORY_KEYWORDS.put("AUTHENTICATION", List.of("login", "password", "sso", "otp", "locked", "authentication", "signin", "account"));
        CATEGORY_KEYWORDS.put("DATABASE", List.of("database", "sql", "query", "records", "postgres", "mysql"));
        CATEGORY_KEYWORDS.put("HARDWARE", List.of("laptop", "printer", "keyboard", "monitor", "mouse", "battery", "screen"));
        CATEGORY_KEYWORDS.put("SECURITY", List.of("virus", "malware", "phishing", "suspicious", "hacked", "breach"));
        CATEGORY_KEYWORDS.put("STORAGE", List.of("disk", "storage", "backup", "drive", "space"));
        CATEGORY_KEYWORDS.put("CLOUD", List.of("cloud", "server", "hosting", "deployment", "aws", "azure"));
        CATEGORY_KEYWORDS.put("APPLICATION", List.of("application", "portal", "page", "payment", "website", "error", "transaction"));
        CATEGORY_KEYWORDS.put("SOFTWARE", List.of("install", "software", "licence", "license", "update", "crash"));
    }

    public Optional<String> suggestCode(String text) {
        Set<String> words = TextSimilarity.keywords(text);
        if (words.isEmpty()) return Optional.empty();
        String winner = null;
        int bestScore = 0;
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (words.contains(keyword)) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                winner = entry.getKey();
            }
        }
        return Optional.ofNullable(winner);
    }
}
