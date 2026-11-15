package com.itsm.sla;

import com.itsm.domain.catalog.SlaPolicy;
import com.itsm.domain.incident.Incident;
import com.itsm.exception.SlaPolicyNotFoundException;
import com.itsm.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;

    public SlaService(SlaPolicyRepository slaPolicyRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
    }

    public void applyDeadlines(Incident incident) {
        if (incident.getPriority() == null) {
            return;
        }
        var priority = incident.getPriority();
        var service = incident.getService();

        Optional<SlaPolicy> policy = Optional.empty();
        if (service != null) {
            policy = slaPolicyRepository.findByPriorityAndService(priority, service);
        }
        if (policy.isEmpty()) {
            policy = slaPolicyRepository.findByPriorityAndServiceIsNull(priority);
        }
        SlaPolicy found = policy.orElseThrow(() ->
                new SlaPolicyNotFoundException("No SLA policy for priority " + priority));

        LocalDateTime start = LocalDateTime.now();
        incident.applySlaDeadlines(found.responseDeadlineFrom(start),
                                   found.resolutionDeadlineFrom(start));
    }

    public String statusOf(Incident incident) {
        LocalDateTime deadline = incident.getResolutionDeadline();
        if (deadline == null) return "NONE";
        if (incident.isResolutionBreached() || incident.isResponseBreached()) return "BREACHED";
        if (incident.getResolvedAt() != null) return "MET";

        LocalDateTime created = incident.getCreatedAt();
        if (created == null) return "ON_TRACK";   // not saved yet

        long total = Duration.between(created, deadline).toSeconds();
        long remaining = Duration.between(LocalDateTime.now(), deadline).toSeconds();
        if (total > 0 && remaining < total * 0.25) return "AT_RISK";
        return "ON_TRACK";
    }
}
