package com.itsm.priority;

import com.itsm.domain.enums.Priority;
import com.itsm.domain.incident.Incident;

public interface PriorityCalculator {
    Priority calculate(Incident incident);
}
