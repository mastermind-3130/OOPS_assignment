package com.itsm.intelligence;

import com.itsm.domain.incident.Incident;
import com.itsm.repository.IncidentRepository;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

/** Suggests possible matches, but never silently merges incident records. */
@Component
public class DuplicateDetector {
    private final IncidentRepository incidentRepository;

    public DuplicateDetector(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<Incident> findSimilarOpen(Incident incident) {
        return rankedMatches(incident, true);
    }

    public List<Incident> findSimilarResolved(Incident incident) {
        return rankedMatches(incident, false);
    }

    private List<Incident> rankedMatches(Incident target, boolean open) {
        if (target == null) return List.of();
        return StreamSupport.stream(incidentRepository.findAll().spliterator(), false)
            .filter(candidate -> candidate != null)
            .filter(candidate -> candidate != target)
            .filter(candidate -> target.getId() == null || !Objects.equals(candidate.getId(), target.getId()))
            .filter(candidate -> open ? candidate.isOpen() : hasResolution(candidate))
            .filter(candidate -> similarity(target, candidate) >= threshold(target, candidate))
            .sorted(Comparator.comparingDouble((Incident match) -> similarity(target, match)).reversed())
            .limit(5)
            .toList();
    }

    private boolean hasResolution(Incident incident) {
        return incident.getResolutionSummary() != null && !incident.getResolutionSummary().isBlank();
    }

    private double threshold(Incident first, Incident second) {
        return first.getService() != null && second.getService() != null
            && Objects.equals(first.getService().getId(), second.getService().getId())
            ? 0.15 : 0.30;
    }

    private double similarity(Incident first, Incident second) {
        return TextSimilarity.similarity(
            first.getTitle() + " " + first.getDescription(),
            second.getTitle() + " " + second.getDescription());
    }
}
