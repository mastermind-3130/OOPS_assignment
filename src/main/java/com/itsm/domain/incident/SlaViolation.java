package com.itsm.domain.incident;

import com.itsm.domain.enums.SlaType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** A record that an incident missed its response or resolution deadline. */
@Entity
@Table(name = "sla_violations")
public class SlaViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlaType type;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    protected SlaViolation() {
    }

    public SlaViolation(Incident incident, SlaType type, LocalDateTime deadline) {
        if (incident == null || type == null || deadline == null) {
            throw new IllegalArgumentException("Incident, type and deadline are required");
        }
        this.incident = incident;
        this.type = type;
        this.deadline = deadline;
        this.detectedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public SlaType getType() { return type; }
    public LocalDateTime getDeadline() { return deadline; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
}
