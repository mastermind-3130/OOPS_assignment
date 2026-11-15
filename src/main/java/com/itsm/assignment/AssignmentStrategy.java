package com.itsm.assignment;

import com.itsm.domain.incident.Incident;
import com.itsm.domain.user.Technician;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AssignmentStrategy {
    String getName();
    String getLabel();

    Optional<Technician> select(Incident incident,
                                List<Technician> candidates,
                                Map<Long, Long> activeCounts);
}
