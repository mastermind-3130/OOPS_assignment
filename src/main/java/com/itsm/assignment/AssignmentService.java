package com.itsm.assignment;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Technician;
import com.itsm.exception.TechnicianUnavailableException;
import com.itsm.exception.WorkloadExceededException;
import com.itsm.repository.IncidentRepository;
import com.itsm.repository.TechnicianRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignmentService {
    private final List<AssignmentStrategy> strategies;
    private final TechnicianRepository technicianRepository;
    private final IncidentRepository incidentRepository;
    private volatile String activeStrategyName = "SKILL_BASED";

    public AssignmentService(List<AssignmentStrategy> strategies,
                             TechnicianRepository technicianRepository,
                             IncidentRepository incidentRepository) {
        this.strategies = strategies;
        this.technicianRepository = technicianRepository;
        this.incidentRepository = incidentRepository;
    }

    public List<AssignmentStrategy> getStrategies() {
        return strategies;
    }

    public String getActiveStrategyName() {
        return activeStrategyName;
    }

    public void setActiveStrategy(String name) {
        boolean exists = strategies.stream().anyMatch(s -> s.getName().equals(name));
        if (!exists) {
            throw new IllegalArgumentException("Unknown assignment strategy: " + name);
        }
        activeStrategyName = name;
    }

    public Optional<Technician> assignCandidate(Incident incident) {
        List<IncidentStatus> openStatuses = Arrays.stream(IncidentStatus.values())
                .filter(IncidentStatus::isOpen)
                .toList();

        List<Technician> all = technicianRepository.findAll();
        Map<Long, Long> activeCounts = all.stream().collect(Collectors.toMap(
                Technician::getId,
                t -> incidentRepository.countByAssignedTechnicianAndStatusIn(t, openStatuses)
        ));

        List<Technician> available = technicianRepository.findByAvailableTrue();
        if (available.isEmpty()) {
            throw new TechnicianUnavailableException("No technician is currently available.");
        }

        List<Technician> underCapacity = available.stream()
                .filter(t -> t.canTakeMore(activeCounts.getOrDefault(t.getId(), 0L).intValue()))
                .toList();

        if (underCapacity.isEmpty()) {
            throw new WorkloadExceededException("All available technicians have reached their workload limit.");
        }

        AssignmentStrategy strategy = strategies.stream()
                .filter(s -> s.getName().equals(activeStrategyName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Active strategy is unavailable."));

        return strategy.select(incident, underCapacity, activeCounts);
    }
}
