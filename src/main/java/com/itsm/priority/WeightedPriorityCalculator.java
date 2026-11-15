package com.itsm.priority;

import com.itsm.domain.enums.Priority;
import com.itsm.domain.incident.Incident;
import org.springframework.stereotype.Component;

@Component
public class WeightedPriorityCalculator implements PriorityCalculator {

    @Override
    public Priority calculate(Incident incident) {
        int criticalityWeight = incident.getService() == null
                ? 2
                : incident.getService().getCriticality().getWeight();

        int score = incident.getImpact().getWeight()
                * incident.getUrgency().getWeight()
                * criticalityWeight;

        Priority priority;
        if (score >= 24) {
            priority = Priority.P1;
        } else if (score >= 12) {
            priority = Priority.P2;
        } else if (score >= 4) {
            priority = Priority.P3;
        } else {
            priority = Priority.P4;
        }

        // A larger affected population raises priority by one level.
        if (incident.getAffectedUsers() >= 50 && priority != Priority.P1) {
            priority = Priority.values()[priority.ordinal() - 1];
        }

        return priority;
    }
}
