package com.itsm.event;

import com.itsm.domain.incident.Incident;

/** Announces that something happened to an incident. Listeners react to it. */
public record IncidentEvent(Incident incident, String action, String actor,
                            String previousState, String newState, String description) {
}
