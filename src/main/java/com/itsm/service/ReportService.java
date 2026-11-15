package com.itsm.service;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Technician;
import com.itsm.repository.IncidentRepository;
import com.itsm.repository.SlaViolationRepository;
import com.itsm.repository.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final IncidentRepository incidentRepository;
    private final TechnicianRepository technicianRepository;
    private final SlaViolationRepository slaViolationRepository;

    public ReportService(IncidentRepository incidentRepository,
                         TechnicianRepository technicianRepository,
                         SlaViolationRepository slaViolationRepository) {
        this.incidentRepository = incidentRepository;
        this.technicianRepository = technicianRepository;
        this.slaViolationRepository = slaViolationRepository;
    }

    public Map<String, Long> incidentsByStatus() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (IncidentStatus s : IncidentStatus.values()) {
            map.put(s.getLabel(), 0L);
        }
        for (Incident i : incidentRepository.findAll()) {
            map.merge(i.getStatus().getLabel(), 1L, Long::sum);
        }
        return map;
    }

    public Map<String, Long> incidentsByPriority() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            map.put(p.name() + " " + p.getLabel(), 0L);
        }
        map.put("Not set", 0L);
        for (Incident i : incidentRepository.findAll()) {
            String key = i.getPriority() == null
                    ? "Not set"
                    : i.getPriority().name() + " " + i.getPriority().getLabel();
            map.merge(key, 1L, Long::sum);
        }
        return map;
    }

    public Map<String, Long> incidentsByService() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Incident i : incidentRepository.findAll()) {
            String key = i.getService() == null ? "No service" : i.getService().getName();
            map.merge(key, 1L, Long::sum);
        }
        return map;
    }

    public Map<String, Long> incidentsByCategory() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Incident i : incidentRepository.findAll()) {
            String key = i.getCategory() == null ? "Uncategorised" : i.getCategory().getName();
            map.merge(key, 1L, Long::sum);
        }
        return map;
    }

    public Map<String, Long> technicianWorkload() {
        List<IncidentStatus> openStatuses = Arrays.stream(IncidentStatus.values())
                .filter(IncidentStatus::isOpen)
                .toList();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Technician t : technicianRepository.findAll()) {
            map.put(t.getFullName(),
                    incidentRepository.countByAssignedTechnicianAndStatusIn(t, openStatuses));
        }
        return map;
    }

    public long slaViolationCount() {
        return slaViolationRepository.count();
    }

    /** Average minutes from creation to resolution, over resolved incidents. 0 if none. */
    public double averageResolutionMinutes() {
        long totalMinutes = 0;
        int count = 0;
        for (Incident i : incidentRepository.findAll()) {
            if (i.getResolvedAt() != null && i.getCreatedAt() != null) {
                totalMinutes += Duration.between(i.getCreatedAt(), i.getResolvedAt()).toMinutes();
                count++;
            }
        }
        return count == 0 ? 0.0 : (double) totalMinutes / count;
    }
}