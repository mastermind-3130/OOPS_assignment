package com.itsm.domain.enums;

/** How much of the business is affected by an incident. */
public enum Impact {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    Impact(int weight) {
        this.weight = weight;
    }

    /** Numeric weight used in the priority score. */
    public int getWeight() {
        return weight;
    }
}
