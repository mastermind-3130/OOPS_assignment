package com.itsm.assignment;

import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Technician;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class LoadBasedAssignment implements AssignmentStrategy {
    @Override
    public String getName() { return "LOAD_BASED"; }

    @Override
    public String getLabel() { return "Load based"; }

    @Override
    public Optional<Technician> select(Incident incident,
                                       List<Technician> candidates,
                                       Map<Long, Long> activeCounts) {
        return candidates.stream()
                .min(Comparator
                        .comparingLong((Technician t) ->
                                activeCounts.getOrDefault(t.getId(), 0L))
                        .thenComparing(Technician::getId));
    }
}
