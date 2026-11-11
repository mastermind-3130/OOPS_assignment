package com.itsm.domain.enums;

/** How quickly the incident needs to be fixed. */
public enum Urgency {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    Urgency(int weight) {
        this.weight = weight;
    }

    /** Numeric weight used in the priority score. */
    public int getWeight() {
        return weight;
    }
}
