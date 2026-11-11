package com.itsm.domain.audit;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * One entry in the audit trail. Immutable once created: no setters.
 *
 * Stores the actor's username and the incident ID as plain values instead of
 * foreign keys, so the history survives even if a user or incident is later removed.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /** Username of who did it, or "SYSTEM" for automatic actions. */
    @Column(nullable = false, updatable = false, length = 50)
    private String actor;

    /** Short action code, e.g. INCIDENT_CREATED, STATUS_CHANGED, ASSIGNED. */
    @Column(nullable = false, updatable = false, length = 50)
    private String action;

    @Column(updatable = false)
    private Long incidentId;

    @Column(updatable = false, length = 30)
    private String previousState;

    @Column(updatable = false, length = 30)
    private String newState;

    @Column(updatable = false, length = 1000)
    private String description;

    protected AuditLog() {
    }

    public AuditLog(String actor, String action, Long incidentId,
                    String previousState, String newState, String description) {
        if (actor == null || actor.isBlank() || action == null || action.isBlank()) {
            throw new IllegalArgumentException("Actor and action are required");
        }
        this.timestamp = LocalDateTime.now();
        this.actor = actor;
        this.action = action;
        this.incidentId = incidentId;
        this.previousState = previousState;
        this.newState = newState;
        this.description = description;
    }

    public Long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public Long getIncidentId() { return incidentId; }
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
    public String getDescription() { return description; }
}
