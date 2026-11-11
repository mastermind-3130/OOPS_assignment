package com.itsm.domain.catalog;

import com.itsm.domain.enums.Priority;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Response and resolution targets for one priority.
 * If service is null, this is the default policy for that priority.
 * A policy with a service overrides the default for that service.
 */
@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Priority priority;

    /** Optional: null means "applies to all services". */
    @ManyToOne
    @JoinColumn(name = "service_id")
    private ItService service;

    @Column(nullable = false)
    private int responseMinutes;

    @Column(nullable = false)
    private int resolutionMinutes;

    protected SlaPolicy() {
    }

    public SlaPolicy(Priority priority, int responseMinutes, int resolutionMinutes, ItService service) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }
        this.priority = priority;
        this.service = service;
        changeTargets(responseMinutes, resolutionMinutes);
    }

    public void changeTargets(int responseMinutes, int resolutionMinutes) {
        if (responseMinutes <= 0 || resolutionMinutes <= 0) {
            throw new IllegalArgumentException("SLA targets must be positive");
        }
        if (resolutionMinutes < responseMinutes) {
            throw new IllegalArgumentException("Resolution target cannot be shorter than response target");
        }
        this.responseMinutes = responseMinutes;
        this.resolutionMinutes = resolutionMinutes;
    }

    public LocalDateTime responseDeadlineFrom(LocalDateTime start) {
        return start.plusMinutes(responseMinutes);
    }

    public LocalDateTime resolutionDeadlineFrom(LocalDateTime start) {
        return start.plusMinutes(resolutionMinutes);
    }

    public boolean isDefaultPolicy() {
        return service == null;
    }

    public Long getId() { return id; }
    public Priority getPriority() { return priority; }
    public ItService getService() { return service; }
    public int getResponseMinutes() { return responseMinutes; }
    public int getResolutionMinutes() { return resolutionMinutes; }
}
