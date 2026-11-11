package com.itsm.domain.enums;

/** How important an IT service is to the business. Used in the priority score. */
public enum BusinessCriticality {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    BusinessCriticality(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
