package com.itsm.event;

import com.itsm.domain.audit.AuditLog;
import com.itsm.repository.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Observer 1: writes the audit trail for every incident event. */
@Component
public class AuditListener {

    private final AuditLogRepository auditLogRepository;

    public AuditListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @EventListener
    public void onIncidentEvent(IncidentEvent event) {
        auditLogRepository.save(new AuditLog(event.actor(), event.action(),
                event.incident().getId(), event.previousState(), event.newState(),
                event.description()));
    }
}
