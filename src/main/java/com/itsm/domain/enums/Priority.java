package com.itsm.domain.enums;

/**
 * Incident priority. P1 is the most urgent.
 * The calculation that produces a priority lives in the
 * PriorityCalculator strategy (com.itsm.priority).
 */
public enum Priority {
    P1("Critical", 1),
    P2("High", 2),
    P3("Medium", 3),
    P4("Low", 4);

    private final String label;
    private final int level;

    Priority(String label, int level) {
        this.label = label;
        this.level = level;
    }

    public String getLabel() {
        return label;
    }

    /** 1 = most urgent, 4 = least urgent. Useful for sorting. */
    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(Priority other) {
        return this.level < other.level;
    }
}
