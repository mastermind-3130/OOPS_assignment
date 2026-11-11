package com.itsm.domain.enums;

/**
 * Lifecycle statuses of an incident.
 * Which transitions are allowed is decided by the State pattern classes
 * in the com.itsm.state package, not here.
 */
public enum IncidentStatus {
    NEW("New"),
    CLASSIFIED("Classified"),
    ASSIGNED("Assigned"),
    ACKNOWLEDGED("Acknowledged"),
    IN_PROGRESS("In Progress"),
    PENDING_INFO("Pending Information"),
    RESOLVED("Resolved"),
    VERIFIED("Verified"),
    CLOSED("Closed");

    private final String label;

    IncidentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** An incident still needs work from IT while it is open. */
    public boolean isOpen() {
        return this != RESOLVED && this != VERIFIED && this != CLOSED;
    }

    /** No further changes are allowed once an incident is closed. */
    public boolean isTerminal() {
        return this == CLOSED;
    }
}
