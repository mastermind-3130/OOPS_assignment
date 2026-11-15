package com.itsm.event;

import com.itsm.domain.incident.Incident;

public record IncidentEvent(Incident incident, String action, String actor,
                            String previousState, String newState, String description) {
}
