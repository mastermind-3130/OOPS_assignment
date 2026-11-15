package com.itsm.sla;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.enums.Role;
import com.itsm.domain.enums.SlaType;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.incident.SlaViolation;
import com.itsm.event.IncidentEvent;
import com.itsm.repository.*;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public SlaMonitor(IncidentRepository incidentRepository,
                      SlaViolationRepository slaViolationRepository,
                      ApplicationEventPublisher eventPublisher,
                      UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.slaViolationRepository = slaViolationRepository;
        this.eventPublisher = eventPublisher;
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
                eventPublisher.publishEvent(new IncidentEvent(i, "SLA_VIOLATED",
                        "SYSTEM", null, null, "Response deadline missed"));

                if (i.getPriority() == Priority.P1) {
                    String msg = "P1 not acknowledged in time; escalated to IT Manager";
                    eventPublisher.publishEvent(new IncidentEvent(i, "ESCALATED",
                            "SYSTEM", null, null, msg));
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
                eventPublisher.publishEvent(new IncidentEvent(i, "SLA_VIOLATED",
                        "SYSTEM", null, null, "Resolution deadline missed"));
            }

            incidentRepository.save(i);
        }
    }
}
