package com.itsm.sla;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.enums.Role;
import com.itsm.domain.enums.SlaType;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.incident.SlaViolation;
import com.itsm.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class SlaMonitor {

    private final IncidentRepository incidentRepository;
    private final SlaViolationRepository slaViolationRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public SlaMonitor(IncidentRepository incidentRepository,
                      SlaViolationRepository slaViolationRepository,
                      AuditLogRepository auditLogRepository,
                      UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.slaViolationRepository = slaViolationRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkBreaches() {
        LocalDateTime now = LocalDateTime.now();
        List<IncidentStatus> openStatuses = Arrays.stream(IncidentStatus.values())
                .filter(IncidentStatus::isOpen).toList();

        for (Incident i : incidentRepository.findByStatusIn(openStatuses)) {

            // Response breach (+ escalation for P1)
            if (i.getResponseDeadline() != null
                    && i.getAcknowledgedAt() == null
                    && now.isAfter(i.getResponseDeadline())
                    && !i.isResponseBreached()) {

                i.markResponseBreached();
                slaViolationRepository.save(
                        new SlaViolation(i, SlaType.RESPONSE, i.getResponseDeadline()));
                auditLogRepository.save(new AuditLog("SYSTEM", "SLA_VIOLATED",
                        i.getId(), null, null, "Response deadline missed"));

                if (i.getPriority() == Priority.P1) {
                    String msg = "P1 not acknowledged in time; escalated to IT Manager";
                    auditLogRepository.save(new AuditLog("SYSTEM", "ESCALATED",
                            i.getId(), null, null, msg));
                    userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.ADMIN)
                            .findFirst()
                            .ifPresent(admin -> i.addNote(admin, msg));
                }
            }

            // Resolution breach
            if (i.getResolutionDeadline() != null
                    && i.getResolvedAt() == null
                    && now.isAfter(i.getResolutionDeadline())
                    && !i.isResolutionBreached()) {

                i.markResolutionBreached();
                slaViolationRepository.save(
                        new SlaViolation(i, SlaType.RESOLUTION, i.getResolutionDeadline()));
                auditLogRepository.save(new AuditLog("SYSTEM", "SLA_VIOLATED",
                        i.getId(), null, null, "Resolution deadline missed"));
            }

            incidentRepository.save(i);
        }
    }
}
